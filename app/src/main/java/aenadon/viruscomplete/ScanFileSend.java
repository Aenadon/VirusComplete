package aenadon.viruscomplete;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

@SuppressLint("HandlerLeak")
public class ScanFileSend extends AppCompatActivity {

    private EditText selectFileBox;
    private ProgressDialog waitingDialog;
    private String pathOfFileToCheck;
    private String apikey = BuildConfig.API_KEY;
    private okhttp3.Response sendResponse; // only needed in the upload-thread

    private SharedPreferences pendingScans;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_file_send);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        selectFileBox = (EditText) findViewById(R.id.selectFileBox);

        // Set up the waiting dialog here and call it where it is needed
        waitingDialog = new ProgressDialog(this);
        waitingDialog.setMessage(getString(R.string.please_wait));
        waitingDialog.setIndeterminate(true);
        waitingDialog.setCancelable(false);

        pendingScans = getSharedPreferences(C.queuedResources, MODE_PRIVATE);

    }

    // CALLED WHEN TOUCHING THE EDITTEXT
    public void pickFile(View view) { // On SDK >= 23, we need to politely ask for permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) { // If >Lollipop AND permission not granted...
            AlertDialogs.askPolitelyForPermission(this); // Show an explanation dialog and then request permission dialog
        } else { // if SDK < 23, no extra permission is needed, just go ahead
            chooseFile();
        }
    }

    // Called if SDK >= 23 after permission was granted/denied
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        // ### https://developer.android.com/training/permissions/requesting.html ###
        switch (requestCode) {
            case C.REQUEST_FILE_ACCESS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, getString(R.string.permission_granted_thanks), Toast.LENGTH_SHORT).show();
                    chooseFile();
                } else {
                    AlertDialogs.permissionDenied(this);
                }
            }
        }
    }
    // Called if SDK < 23 or if SDK >= 23 and permission has been granted
    private void chooseFile() {
        // file picker library
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(DialogConfigs.DEFAULT_DIR);

        FilePickerDialog filePicker = new FilePickerDialog(this, properties);
        filePicker.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                if (files.length != 0) {
                    selectFileBox.setText(files[0]); // show the selected file to the user
                }
            }
        });
        filePicker.show();
    }

    // CALLED WHEN PRESSING CHECK FOR REPORT
    public void checkForAvailableReport(View view) {
        pathOfFileToCheck = selectFileBox.getText().toString();
        if (pathOfFileToCheck.trim().isEmpty()) {
            Toast.makeText(ScanFileSend.this, getString(R.string.error_select_file), Toast.LENGTH_LONG).show();
            return;
        }
        waitingDialog.show();

        final Handler handler = new Handler(){ // this is a function which will be called after the thread has finished
            @Override
            public void handleMessage(Message msg) {
                String hash = msg.getData().getString("hash"); // take the given hash and then continue working as usual (retrieve report)
                retrieveReport(hash);
            }
        };

        new Thread() { // we calculate the hash in a separate thread so the UI ("please wait") continues to run
            @Override
            public void run() {
                Message m = new Message();
                Bundle b = new Bundle();
                b.putString("hash", C.getSHA256(pathOfFileToCheck)); // get the hash (in a separate thread)
                m.setData(b);
                handler.sendMessage(m); // give the hash to the handler
            }
        }.start();
    }

    // Called after the hash has been calculated
    private void retrieveReport(final String sha256hash) {
        RetrofitBase.getRetrofit().create(VirusTotalApiCalls.class).getFileReportForHash(apikey, sha256hash).enqueue(new Callback<VirusTotalResponse>() {
            @Override
            public void onResponse(Call<VirusTotalResponse> call, retrofit2.Response<VirusTotalResponse> response) {
                waitingDialog.dismiss(); // dismiss the waiting dialog before showing anything
                if (response.code() == 204 || !response.isSuccessful()) {
                    C.errorCheck(response.code(), ScanFileSend.this);
                    return; // we show the error message before, then we interrupt the task
                }
                final VirusTotalResponse results = response.body();

                // If the scan is among the queued AND the scan date is still the same (=scan not finished)...
                if (pendingScans.contains(sha256hash)) {
                    String lastScan = pendingScans.getString(sha256hash, "");
                    if (lastScan.equals(results.getScan_date()) || results.getScan_date() == null) {
                        AlertDialogs.resourceStillQueued(ScanFileSend.this); // tell the user the scan is still pending
                        return; // and then exit
                    }
                }


                if (results.getResponse_code() == -1) {
                    AlertDialogs.strangeError(ScanFileSend.this);
                } else if (results.getResponse_code() == 0) { // File not in DB !!OR!! queued

                    String title = getString(R.string.file_unavailable_or_queued_title);
                    String message = getString(R.string.file_unavailable_or_queued_message);
                    String positiveButton = getString(R.string.button_upload_file);
                    String negativeButton = getString(R.string.button_cancel);

                    new AlertDialog.Builder(ScanFileSend.this)
                            .setTitle(title)
                            .setMessage(message)
                            .setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    upload(pathOfFileToCheck, sha256hash);
                                }
                            })
                            .setNegativeButton(negativeButton, null) // why not android.R.string.cancel? Because it would translate even to languages with no app-translation available.
                            .show();
                } else { // Report available

                    String title = getString(R.string.report_available_title);
                    String message = String.format(getString(R.string.report_available_message), C.getAdjustedDate(results.getScan_date()),
                            results.getPositives(), results.getTotal());
                    String positiveButton = getString(R.string.button_report_available_newscan);
                    String negativeButton = getString(R.string.button_report_available_viewold);

                    new AlertDialog.Builder(ScanFileSend.this)
                            .setTitle(title)
                            .setMessage(message)
                            .setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    C.forceHashRescan(ScanFileSend.this, sha256hash, results.getScan_date()); // inside C because ScanHashLookup uses it too
                                }
                            })
                            .setNegativeButton(negativeButton, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    C.displayResults(ScanFileSend.this, results, R.id.list_file_scan_send_results);
                                }
                            })
                            .show();
                }
            }
            @Override
            public void onFailure(Call<VirusTotalResponse> call, Throwable t) {
                waitingDialog.dismiss();
                AlertDialogs.onFailureMessage(ScanFileSend.this, t.getLocalizedMessage());
            }
        });
    }

    // CALLED WHEN THE UPLOAD FILE BUTTON IS PRESSED (from the result dialog after retrieving report)
    private void upload(String filePath, final String fileHash) {
        waitingDialog.show();

        File fileReference = new File(filePath); // reference to file
        final MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("apikey", apikey)
                .addFormDataPart("file", fileReference.getName(), RequestBody.create(MediaType.parse("file/*"), fileReference))
                .build();


        // Why don't we use Retrofit instead of okhttp3? I am unable to create a working upload-file-request using retrofit.
        final Handler handler = new Handler(){ // this is a function which will be called after the thread has finished
            @Override
            public void handleMessage(Message msg) {
                waitingDialog.dismiss();

                Bundle b = msg.getData();
                if (b.getBoolean("isError")) {
                    String error = msg.getData().getString("errorMsg");
                    AlertDialogs.onFailureMessage(ScanFileSend.this, error);
                    return;
                }

                if (!sendResponse.isSuccessful() || sendResponse.code() == 204) {
                    C.errorCheck(sendResponse.code(), ScanFileSend.this);
                } else {
                    // We silently hope that successful response == successfully queued
                    // because the documentation states no other case
                    // https://www.virustotal.com/en/documentation/public-api/#scanning-files

                    // Put the hash + last scan date into the sharedpreferences so we can tell the user if the scan is still pending.
                    pendingScans.edit().putString(fileHash, C.noScanYet).apply();
                    AlertDialogs.resourceIsQueued(ScanFileSend.this);
                }

            }
        };

        new Thread() {
            @Override
            public void run() {
                Request request = new Request.Builder()
                        .url("https://www.virustotal.com/vtapi/v2/file/scan")
                        .post(requestBody)
                        .build();
                OkHttpClient client = new OkHttpClient();

                Message m = handler.obtainMessage();
                Bundle b = new Bundle();
                try {
                    sendResponse = client.newCall(request).execute(); // store the response in a variable in the outer class so Handler can access it
                    b.putBoolean("isError", false);
                } catch (Exception e) {
                    e.printStackTrace();
                    b.putBoolean("isError", true);
                    b.putString("errorMsg", e.getLocalizedMessage()); // give the message to the handler
                }
                m.setData(b);
                handler.sendMessage(m);
            }
        }.start();
    }
}
