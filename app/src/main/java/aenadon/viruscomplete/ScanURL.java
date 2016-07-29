package aenadon.viruscomplete;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;

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

    String apikey = BuildConfig.API_KEY;
    String urlToCheck;
    ProgressDialog waitingDialog;

    String lastCheckedWebsite = "none"; // we use this to make sure the user doesn't just press "scan again" while not having retrieved the latest report

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

    }

    public void scanURL(View pressedButton) {
        EditText editText = (EditText) findViewById(R.id.box_urlCheck);
        // close the keyboard
        View v = this.getCurrentFocus();
        if (v != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
        urlToCheck = editText.getText().toString();
        if (urlToCheck.equals("http://") || urlToCheck.trim().isEmpty()) return; // no input - no scan
        // show a "please wait" dialog and retrieve the latest existing report
        waitingDialog.show();

        switch (pressedButton.getId()) {
            case R.id.scan_url_get_report:
                retrieveReports(urlToCheck);
                break;
            case R.id.scan_url_force_scan:
                if (!urlToCheck.equals(lastCheckedWebsite)) {
                    waitingDialog.dismiss(); // Retrieve a report before scanning again - no scan, no wait
                    new AlertDialog.Builder(this)
                            .setMessage(getString(R.string.get_report_first))
                            .setPositiveButton(getString(R.string.try_again), null)
                            .show();
                } else {
                    lastCheckedWebsite = "none"; // so the user won't request reports repeatedly
                    forceScan(urlToCheck);
                }
                break;
        }
    }

    private void retrieveReports(final String urlToCheck) {
        RetrofitBase.getRetrofit().create(VirusTotalApiCalls.class).getURLScanResults(apikey, urlToCheck).enqueue(new Callback<VirusTotalResponse>() {
            @Override
            public void onResponse(Call<VirusTotalResponse> call, Response<VirusTotalResponse> response) {
                if (response.code() == 204 || !response.isSuccessful()) {
                    waitingDialog.dismiss();
                    C.errorCheck(response.code(), ScanURL.this);
                    return; // we show the error message before, then we interrupt the task
                }
                final VirusTotalResponse results = response.body();
                if (results.getResponse_code() == -1) {
                    new AlertDialog.Builder(ScanURL.this)
                            .setTitle(getString(R.string.invalid_url_title))
                            .setMessage(getString(R.string.invalid_url_message))
                            .setPositiveButton(getString(R.string.try_again), null)
                            .show();
                } else if (results.getResponse_code() == -2) {
                    new AlertDialog.Builder(ScanURL.this)
                            .setTitle(getString(R.string.scan_queued_title))
                            .setMessage(getString(R.string.scan_still_queued_message))
                            .setPositiveButton(getString(R.string.come_back_later), null)
                            .show();
                } else if (results.getResponse_code() == 0) { // no scan available, force scan
                    forceScan(urlToCheck);
                } else {
                    waitingDialog.dismiss(); // dismiss the waiting dialog before showing the results
                    lastCheckedWebsite = urlToCheck; // we do that here (here we can be sure that everything went as expected)
                    displayResults(results);
                }

            }

            @Override
            public void onFailure(Call<VirusTotalResponse> call, Throwable t) {
                waitingDialog.dismiss();
                new AlertDialog.Builder(ScanURL.this)
                        .setTitle(getString(R.string.data_error_title))
                        .setMessage(getString(R.string.data_error_message) + t.getLocalizedMessage())
                        .setPositiveButton(getString(R.string.sorry), null)
                        .show();
            }
        });
    }

    private void forceScan(String urlToCheck) {
        RetrofitBase.getRetrofit().create(VirusTotalApiCalls.class).forceURLScan(apikey, urlToCheck).enqueue(new Callback<VirusTotalResponse>() {
            @Override
            public void onResponse(Call<VirusTotalResponse> call, Response<VirusTotalResponse> response) {
                waitingDialog.dismiss();
                if (response.code() == 204 || !response.isSuccessful()) {
                    C.errorCheck(response.code(), ScanURL.this);
                    return; // we show the error message before, then we interrupt the task
                }
                new AlertDialog.Builder(ScanURL.this)
                        .setTitle(getString(R.string.scan_queued_title))
                        .setMessage(getString(R.string.scan_queued_message))
                        .setPositiveButton(getString(R.string.come_back_later), null)
                        .show();

            }
            @Override
            public void onFailure(Call<VirusTotalResponse> call, Throwable t) {
                waitingDialog.dismiss();
                new AlertDialog.Builder(ScanURL.this)
                        .setTitle(getString(R.string.data_error_title))
                        .setMessage(getString(R.string.data_error_message_forcescan) + t.getLocalizedMessage())
                        .setPositiveButton(getString(R.string.sorry), null)
                        .show();
            }
        });
    }

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

        ListView list = (ListView) findViewById(R.id.list_urlScanResults); // inside R.layout.content_scan_url
        list.setAdapter(new ResultListAdapter(this, everythingTogether));
    }

}