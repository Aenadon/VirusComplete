package aenadon.viruscomplete;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

public class ScanURL extends AppCompatActivity{

    String apikey = BuildConfig.API_KEY;
    String baseUrl = "https://www.virustotal.com/vtapi/v2/url/report";
    String baseScanUrl = "https://www.virustotal.com/vtapi/v2/url/scan";
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

        String apiCall = Uri.parse(baseUrl).buildUpon()
                .appendQueryParameter("resource", urlToCheck)
                .appendQueryParameter("apikey", apikey)
                .appendQueryParameter("scan", "1") // if website was never scanned, this forces a scan
                .build()
                .toString();

        new getURLReport().execute(apiCall);

    }

    public class getURLReport extends AsyncTask<String, Void, String> {

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
            }

            final JSONObject json;
            try {
                Log.d("test", response);
                json = new JSONObject(response);
                if (json.getString("verbose_msg").equals(scanQueuedMsg)) {
                    new AlertDialog.Builder(ScanURL.this)
                            .setTitle("Scan queued")
                            .setMessage("The website is queued for scan. Come back in about 20-30 seconds for the report.")
                            .setPositiveButton("Come back later", null)
                            .show();
                } else {
                    new AlertDialog.Builder(ScanURL.this)
                            .setTitle("Report available")
                            .setMessage("The last scan of this website is from "+json.getString("scan_date") +
                                    ". Do you want to scan again or view the report of the previous scan?")
                            .setPositiveButton("Scan again", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // TODO beware: feature is broken atm
                                    String apiCall = Uri.parse(baseScanUrl).buildUpon()
                                            .appendQueryParameter("resource", urlToCheck)
                                            .appendQueryParameter("apikey", apikey)
                                            .build()
                                            .toString();

                                    new getURLReport().execute(apiCall);
                                }
                            })
                            .setNegativeButton("View report", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent showRes = new Intent(ScanURL.this, ShowResults.class);
                                    showRes.putExtra("json", response);
                                    showRes.putExtra("typeOfResult", "url"); // TODO maybe use some constant
                                    startActivity(showRes);
                                }
                            })
                            .setNeutralButton("Cancel", null)
                            .show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                waitingDialog.dismiss();
            }
        }
    }




}
