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
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScanURL extends AppCompatActivity {

    private String apikey = BuildConfig.API_KEY;
    private String urlToCheck;
    private ProgressDialog waitingDialog;

    private EditText textbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_url);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set up the waiting dialog here and call it where it is needed
        waitingDialog = new ProgressDialog(this);
        waitingDialog.setMessage(getString(R.string.please_wait));
        waitingDialog.setIndeterminate(true);
        waitingDialog.setCancelable(false);

        textbox = (EditText) findViewById(R.id.box_urlCheck);

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

    public void scanURL(View view) {

        // close the keyboard
        View v = this.getCurrentFocus();
        if (v != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
        urlToCheck = textbox.getText().toString();
        if (urlToCheck.equals("http://") || urlToCheck.trim().isEmpty()) { // no input - no scan
            Toast.makeText(this, getString(R.string.empty_url), Toast.LENGTH_LONG).show();
            return;
        }

        waitingDialog.show(); // show a "please wait" dialog and retrieve the latest existing report

        RetrofitBase.getRetrofit().create(VirusTotalApiCalls.class).getURLScanResults(apikey, urlToCheck).enqueue(new Callback<VirusTotalResponse>() {
            @Override
            public void onResponse(Call<VirusTotalResponse> call, Response<VirusTotalResponse> response) {
                waitingDialog.dismiss(); // dismiss the waiting dialog before showing the results
                if (response.code() == 204 || !response.isSuccessful()) {
                    C.errorCheck(response.code(), ScanURL.this);
                    return; // we show the error message before, then we interrupt the task
                }
                final VirusTotalResponse results = response.body();
                if (results.getResponse_code() == -1) {
                    AlertDialogs.invalidURL(ScanURL.this);
                } else if (results.getResponse_code() == -2) {
                    AlertDialogs.urlScanStillQueued(ScanURL.this);
                } else if (results.getResponse_code() == 0) { // no scan available, force scan
                    forceScan(urlToCheck);
                } else {

                    String title = getString(R.string.report_available_title);
                    String message = String.format(getString(R.string.report_available_message), C.getAdjustedDate(results.getScan_date()));
                    String positiveButton = getString(R.string.button_report_available_newscan);
                    String negativeButton = getString(R.string.button_report_available_viewold);

                    new AlertDialog.Builder(ScanURL.this)
                            .setTitle(title)
                            .setMessage(message)
                            .setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    forceScan(urlToCheck);
                                }
                            })
                            .setNegativeButton(negativeButton, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    displayResults(results);
                                }
                            })
                            .show();
                }

            }

            @Override
            public void onFailure(Call<VirusTotalResponse> call, Throwable t) {
                waitingDialog.dismiss();
                AlertDialogs.onFailureMessage(ScanURL.this, t.getLocalizedMessage());
            }
        });
    }

    // Private access, only called if user wants to scan again through the result dialog
    private void forceScan(String urlToCheck) {
        RetrofitBase.getRetrofit().create(VirusTotalApiCalls.class).forceURLRescan(apikey, urlToCheck).enqueue(new Callback<VirusTotalResponse>() {
            @Override
            public void onResponse(Call<VirusTotalResponse> call, Response<VirusTotalResponse> response) {
                waitingDialog.dismiss();
                if (response.code() == 204 || !response.isSuccessful()) {
                    C.errorCheck(response.code(), ScanURL.this);
                    return; // we show the error message before, then we interrupt the task
                }
                // Unfortunately, the "report" function does not report invalid URLs,
                // so an invalid URL consumes 2 API calls (report --> not in database, scan --> invalid URL)
                if (response.body().getResponse_code() == -1) {
                    AlertDialogs.invalidURL(ScanURL.this);
                } else {
                    AlertDialogs.urlIsQueued(ScanURL.this); // success!
                }
            }
            @Override
            public void onFailure(Call<VirusTotalResponse> call, Throwable t) {
                waitingDialog.dismiss();
                AlertDialogs.onFailureMessage(ScanURL.this, t.getLocalizedMessage());
            }
        });
    }

    // This is not the one in the C class because it features a third category --> "unrated"
    private void displayResults(VirusTotalResponse scanResults) { // displays all the results in the ListView below the input field
        JsonObject jsonScans = scanResults.getScans().getAsJsonObject();

        ArrayList<AvCheck> everythingTogether = new ArrayList<>();
        // we save each of them in their own list, sort them individually and then bring them together
        ArrayList<AvCheck> unsafes = new ArrayList<>();
        ArrayList<AvCheck> safes = new ArrayList<>();
        ArrayList<AvCheck> unrateds = new ArrayList<>();

        Set<Map.Entry<String, JsonElement>> entries = jsonScans.entrySet(); //will return members of your object
        for (Map.Entry<String, JsonElement> entry : entries) {
            String result = entry.getValue().getAsJsonObject().get("result").getAsString();
            boolean detected = entry.getValue().getAsJsonObject().get("detected").getAsBoolean();

            if (detected) {
                unsafes.add(new AvCheck(entry.getKey(), C.unsafe));
            } else if (result.equals("unrated site")) {
                unrateds.add(new AvCheck(entry.getKey(), C.unrated));
            } else {
                safes.add(new AvCheck(entry.getKey(), C.safe));
            }
        }
        // sort each list and then add them together
        Collections.sort(unsafes);
        Collections.sort(safes);
        Collections.sort(unrateds);

        // first list row contains date and detections count
        everythingTogether.add(new AvCheck(scanResults.getScan_date(), scanResults.getPositives(), scanResults.getTotal(), true));
        everythingTogether.addAll(unsafes);
        everythingTogether.addAll(safes);
        everythingTogether.addAll(unrateds);

        ListView list = (ListView) findViewById(R.id.list_urlScanResults);
        list.setAdapter(new ResultListAdapter(this, everythingTogether));
    }

}