package aenadon.viruscomplete;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScanHashLookup extends AppCompatActivity {

    private ProgressDialog waitingDialog;
    private String hashToCheck;
    private String apikey = BuildConfig.API_KEY;

    private EditText textbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_hash_lookup);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set up the waiting dialog here and call it where it is needed
        waitingDialog = new ProgressDialog(this);
        waitingDialog.setMessage(getString(R.string.please_wait));
        waitingDialog.setIndeterminate(true);
        waitingDialog.setCancelable(false);

        textbox = (EditText) findViewById(R.id.box_filehash);
    }

    public void paste(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            android.content.ClipboardManager clipboardNew = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (clipboardNew.hasPrimaryClip()) textbox.setText(clipboardNew.getPrimaryClip().getItemAt(0).getText().toString());
        } else {
            @SuppressWarnings("deprecation") // this runs only when an old Android version is used
            android.text.ClipboardManager clipboardOld = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (clipboardOld.hasText()) textbox.setText(clipboardOld.getText());
        }
    }

    public void lookupHash(View view) {
        // close the keyboard
        View v = this.getCurrentFocus();
        if (v != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
        hashToCheck = textbox.getText().toString();
        if (hashToCheck.trim().isEmpty()) return; // no input - no scan
        // show a "please wait" dialog and retrieve the latest existing report
        waitingDialog.show();

        RetrofitBase.getRetrofit().create(VirusTotalApiCalls.class).getFileReportForHash(apikey, hashToCheck).enqueue(new Callback<VirusTotalResponse>() {
            @Override
            public void onResponse(Call<VirusTotalResponse> call, Response<VirusTotalResponse> response) {
                waitingDialog.dismiss(); // dismiss the waiting dialog before showing anything
                if (response.code() == 204 || !response.isSuccessful()) {
                    C.errorCheck(response.code(), ScanHashLookup.this);
                    return; // we show the error message before, then we interrupt the task
                }
                final VirusTotalResponse results = response.body();
                if (results.getResponse_code() == -1) {
                    AlertDialogs.strangeError(ScanHashLookup.this);
                } else if (results.getResponse_code() == 0) {
                    if (results.getVerbose_msg().equals("Invalid resource, check what you are submitting")) {
                        AlertDialogs.notAHash(ScanHashLookup.this);
                    } else {
                        AlertDialogs.resNotFound(ScanHashLookup.this);
                    }
                } else {
                    String title = getString(R.string.report_available_title);
                    String message = String.format(getString(R.string.report_available_message), C.getAdjustedDate(results.getScan_date()));
                    String positiveButton = getString(R.string.button_report_available_newscan);
                    String negativeButton = getString(R.string.button_report_available_viewold);

                    new AlertDialog.Builder(ScanHashLookup.this)
                            .setTitle(title)
                            .setMessage(message)
                            .setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    C.forceHashRescan(ScanHashLookup.this, hashToCheck); // inside C because ScanHashLookup uses it too
                                }
                            })
                            .setNegativeButton(negativeButton, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    C.displayResults(ScanHashLookup.this, results, R.id.list_file_hash_lookup_results);
                                }
                            })
                            .show();
                }
            }

            @Override
            public void onFailure(Call<VirusTotalResponse> call, Throwable t) {
                AlertDialogs.onFailureMessage(ScanHashLookup.this, t.getLocalizedMessage());
            }
        });
    }


}
