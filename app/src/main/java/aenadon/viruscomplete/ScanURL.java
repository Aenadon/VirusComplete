package aenadon.viruscomplete;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScanURL extends AppCompatActivity {

    String apikey = BuildConfig.API_KEY;
    String urlToCheck;
    String scanQueuedMsg = "Scan request successfully queued, come back later for the report";
    ProgressDialog waitingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_url);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set up the waiting dialog here and call it down where it is needed
        waitingDialog = new ProgressDialog(this);
        waitingDialog.setMessage(getString(R.string.please_wait));
        waitingDialog.setIndeterminate(true);
        waitingDialog.setCancelable(false);

    }

    public void scanURL(View view) {
        EditText editText = (EditText) findViewById(R.id.box_urlCheck);
        // close the keyboard
        View v = this.getCurrentFocus();
        if (v != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        urlToCheck = editText.getText().toString();
        // show a "please wait" dialog and retrieve the latest existing report
        waitingDialog.show();
        retrieveReports(urlToCheck);
    }

    private void retrieveReports(final String urlToCheck) {
        RetrofitDeclaration.getRetrofit().create(VirusTotalService.class).getURLScanResults(apikey, urlToCheck, 1).enqueue(new Callback<VirusTotalURLResponse>() {
            @Override
            public void onResponse(Call<VirusTotalURLResponse> call, Response<VirusTotalURLResponse> response) {
                errorCheck(response.code());
                if (response.code() == 204 || !response.isSuccessful()) return; // we show the error message, then we finish execution
                final VirusTotalURLResponse results = response.body();
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
                } else if (results.getVerbose_msg().equals(scanQueuedMsg)) {
                    new AlertDialog.Builder(ScanURL.this)
                            .setTitle(getString(R.string.scan_queued_title))
                            .setMessage(getString(R.string.scan_queued_message))
                            .setPositiveButton(getString(R.string.come_back_later), null)
                            .show();
                } else {
                    SimpleDateFormat sourceDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // this is the source format we need to parse
                    sourceDateFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // source date is UTC

                    Date scanDate;
                    try {
                        scanDate = sourceDateFormat.parse(results.getScan_date());
                    } catch (ParseException e) {
                        e.printStackTrace();
                        return;
                    }
                    waitingDialog.dismiss(); // dismiss the waiting dialog before showing the message box
                    new AlertDialog.Builder(ScanURL.this)
                            .setTitle(getString(R.string.report_available))
                            .setMessage(String.format(getString(R.string.last_scan_question), DateFormat.getDateTimeInstance().format(scanDate))) // adjusts the date to locale automatically (format+timezone)
                            .setPositiveButton(getString(R.string.scan_again), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    waitingDialog.show(); // show waiting dialog again before working
                                    forceScan(urlToCheck);
                                }
                            })
                            .setNegativeButton(getString(R.string.view_report), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    displayResults(results);
                                }
                            })
                            .setNeutralButton(getString(android.R.string.cancel), null)
                            .show();
                }

            }

            @Override
            public void onFailure(Call<VirusTotalURLResponse> call, Throwable t) {
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
        RetrofitDeclaration.getRetrofit().create(VirusTotalService.class).forceURLScan(apikey, urlToCheck).enqueue(new Callback<VirusTotalURLResponse>() {
            @Override
            public void onResponse(Call<VirusTotalURLResponse> call, Response<VirusTotalURLResponse> response) {
                errorCheck(response.code());
                if (response.code() == 204 || !response.isSuccessful()) return; // we show the error message before, then we interrupt the task

                waitingDialog.dismiss();
                new AlertDialog.Builder(ScanURL.this)
                        .setTitle(getString(R.string.scan_queued_title))
                        .setMessage(getString(R.string.scan_queued_message))
                        .setPositiveButton(getString(R.string.come_back_later), null)
                        .show();


            }
            @Override
            public void onFailure(Call<VirusTotalURLResponse> call, Throwable t) {
                waitingDialog.dismiss();
                new AlertDialog.Builder(ScanURL.this)
                        .setTitle(getString(R.string.data_error_title))
                        .setMessage(getString(R.string.data_error_message_forcescan) + t.getLocalizedMessage())
                        .setPositiveButton(getString(R.string.sorry), null)
                        .show();
            }
        });
    }

    private void errorCheck(int responseCode) {
        waitingDialog.dismiss();
        switch (responseCode) {
            case 204:
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.api_limit_exceeded_title))
                        .setMessage(getString(R.string.api_limit_exceeded_message))
                        .setPositiveButton(getString(R.string.come_back_later), null)
                        .show();
                break;
            case 403: // This shouldn't ever happen, but, just in case...
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.not_permitted_title))
                        .setMessage(getString(R.string.not_permitted_message))
                        .setPositiveButton(getString(R.string.leave), null)
                        .show();
                break;
        }
    }

    private void displayResults(VirusTotalURLResponse scanResults) { // displays all the results in the ListView below the input field
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
        everythingTogether.addAll(unsafes);
        everythingTogether.addAll(safes);
        everythingTogether.addAll(unrateds);

        ListView list = (ListView) findViewById(R.id.list_urlScanResults); // inside R.layout.content_scan_url
        list.setAdapter(new URLDetectionAdapter(this, everythingTogether));

        String detectionCount = String.format(getString(R.string.detect_count), scanResults.getPositives(), scanResults.getTotal());
        Toast.makeText(ScanURL.this, detectionCount, Toast.LENGTH_LONG).show();
    }

    public class AvCheck implements Comparable<AvCheck> { // Contains the results of the scan for passing them to the adapter
        String name;
        int detection;

        public AvCheck(String name, int detection) {
            this.name = name;
            this.detection = detection;
        }

        @Override
        public int compareTo(@NonNull AvCheck avCheck) {
            return this.name.compareTo(avCheck.name);
        }
    }
}