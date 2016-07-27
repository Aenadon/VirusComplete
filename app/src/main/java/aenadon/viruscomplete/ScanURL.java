package aenadon.viruscomplete;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
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

public class ScanURL extends AppCompatActivity{

    String apikey = BuildConfig.API_KEY;
    String urlToCheck;
    String scanQueuedMsg = "Scan request successfully queued, come back later for the report";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_url);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    public void scanURL(View view) {
        EditText editText = (EditText)findViewById(R.id.box_urlCheck);
        urlToCheck = editText.getText().toString();
        retrieveReports(urlToCheck);
    }

    public void retrieveReports(final String urlToCheck) { // TODO XML Strings!!!
        RetrofitDeclaration.getRetrofit().create(VirusTotalService.class).getURLScanResults(apikey, urlToCheck, 1).enqueue(new Callback<VirusTotalURLResponse>() {
            @Override
            public void onResponse(Call<VirusTotalURLResponse> call, Response<VirusTotalURLResponse> response) {
                if (!response.isSuccessful()) {
                    Log.e("retrieveReports", "Error: Unsuccessful response");
                    return;
                }
                final VirusTotalURLResponse results = response.body();
                if (results.getResponse_code() == -1) {
                    new AlertDialog.Builder(ScanURL.this)
                            .setTitle("Invalid URL")
                            .setMessage("The URL you entered is invalid. Please delete any unnecessary characters and try again.")
                            .setPositiveButton("Try again", null)
                            .show();
                } else if (results.getResponse_code() == -2) {
                    new AlertDialog.Builder(ScanURL.this)
                            .setTitle("Scan queued")
                            .setMessage("The website is still queued. Please come back later.")
                            .setPositiveButton("Come back later", null)
                            .show();
                } else if (results.getVerbose_msg().equals(scanQueuedMsg)) {
                    new AlertDialog.Builder(ScanURL.this)
                            .setTitle("Scan queued")
                            .setMessage("The website is queued for scan. Come back in about a minute for the report.")
                            .setPositiveButton("Come back later", null)
                            .show();
                } else {
                    new AlertDialog.Builder(ScanURL.this)
                            .setTitle("Report available")
                            .setMessage("The last scan of this website is from "+ results.getScan_date() +
                                    ". Do you want to scan again or view the report of the previous scan?")
                            .setPositiveButton("Scan again", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    forceScan(urlToCheck);
                                }
                            })
                            .setNegativeButton("View report", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    displayResults(results);
                                }
                            })
                            .setNeutralButton("Cancel", null)
                            .show();
                }

            }

            @Override
            public void onFailure(Call<VirusTotalURLResponse> call, Throwable t) {
                Log.e("retrieveReports", t.getMessage());
            }
        });
    }

    public void forceScan(String urlToCheck) { // TODO XML Strings!
        RetrofitDeclaration.getRetrofit().create(VirusTotalService.class).forceURLScan(apikey, urlToCheck).enqueue(new Callback<VirusTotalURLResponse>() {
            @Override
            public void onResponse(Call<VirusTotalURLResponse> call, Response<VirusTotalURLResponse> response) {
                if (response.isSuccessful() && response.body().getResponse_code() == 1) {
                    new AlertDialog.Builder(ScanURL.this)
                            .setTitle("Scan queued")
                            .setMessage("The website is queued for scan. Come back in about a minute for the report.")
                            .setPositiveButton("Come back later", null)
                            .show();
                } else {
                    new AlertDialog.Builder(ScanURL.this)
                            .setTitle("Error")
                            .setMessage("Some really strange error happened")
                            .setPositiveButton("Come back later", null)
                            .show();
                }

            }

            @Override
            public void onFailure(Call<VirusTotalURLResponse> call, Throwable t) {

            }
        });
    }

    public void displayResults(VirusTotalURLResponse scanResults) { // displays all the results in the ListView below the input field
        JsonObject jsonScans = scanResults.getScans().getAsJsonObject();

        ArrayList<AvCheck> everythingTogether = new ArrayList<>();
        // we save each of them in their own list, sort them individually and then bring them together
        ArrayList<AvCheck> unsafes = new ArrayList<>();
        ArrayList<AvCheck> safes = new ArrayList<>();
        ArrayList<AvCheck> unrateds = new ArrayList<>();

        Set<Map.Entry<String, JsonElement>> entries = jsonScans.entrySet(); //will return members of your object
        for (Map.Entry<String, JsonElement> entry: entries) {
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
        Collections.sort(unsafes); Collections.sort(safes); Collections.sort(unrateds);
        everythingTogether.addAll(unsafes); everythingTogether.addAll(safes); everythingTogether.addAll(unrateds);

        ListView list = (ListView) findViewById(R.id.list_urlScanResults); // inside R.layout.content_scan_url
        list.setAdapter(new URLDetectionAdapter(this, everythingTogether));

        String detectionCount = String.format(getString(R.string.detectCount), scanResults.getPositives(), scanResults.getTotal());
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











  /*  public class getURLReport extends AsyncTask<String, Void, String> {

        private ProgressDialog waitingDialog; // tells user to wait

        @Override
        protected void onPreExecute() {
            waitingDialog = new ProgressDialog(ScanURL.this);
            waitingDialog.setMessage(getString(R.string.please_wait));
            waitingDialog.setIndeterminate(true);
            waitingDialog.setCancelable(false);
            waitingDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {

            FetchJSON fetchJSON = new FetchJSON(strings[0]); // the heavy lifting happens here
            return fetchJSON.jsonResponse; // if jsonResponse is empty, null will be returned
        }

        // TODO ---> XML STRINGS!!! <---
        @Override
        protected void onPostExecute(final String response) {
            if (response == null) return; // if response is null, cancel

            // TODO remove this as soon as the feature works!!
            if (response.equals("[]")) {
                new AlertDialog.Builder(ScanURL.this)
                        .setTitle("Broken API feature")
                        .setMessage("The URL re-scanning API feature seems to be broken. Hopefully this will be fixed some time later.")
                        .setPositiveButton("Cancel", null)
                        .show();
                return;
            }

            final JSONObject json;
            try { // TODO lots of XML!
                json = new JSONObject(response);
                if (json.getInt("response_code") == -1) { // invalid URL

                } else if (json.getString("verbose_msg").equals(scanQueuedMsg)) {

                } else {

                }
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                waitingDialog.dismiss();
            }
        }

    } */
}