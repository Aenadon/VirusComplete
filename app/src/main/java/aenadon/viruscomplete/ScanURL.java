package aenadon.viruscomplete;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

public class ScanURL extends AppCompatActivity{

    String apiKey = BuildConfig.API_KEY;
    String baseUrl = "https://www.virustotal.com/vtapi/v2/url/report";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_url);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    public void scanURL(View view) {
        EditText editText = (EditText)findViewById(R.id.box_urlCheck);
        String urlToCheck = editText.getText().toString();

        String apiCall = Uri.parse(baseUrl).buildUpon()
                .appendQueryParameter("resource", urlToCheck)
                .appendQueryParameter("apikey", apiKey)
                .build()
                .toString();

        new getURLReport().execute(apiCall);

    }

    // TODO change all of this - probably move AsyncTask into FetchJSON (and have FetchJSON as Asynctask)
    public class getURLReport extends AsyncTask<String, Void, String> {

        private ProgressDialog waitingDialog; // tells user to wait

        @Override
        protected void onPreExecute() {
            waitingDialog = new ProgressDialog(ScanURL.this);
            waitingDialog.setTitle("");
            waitingDialog.setMessage(getString(R.string.please_wait));
            waitingDialog.setIndeterminate(true);
            waitingDialog.setCancelable(false);
            waitingDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {

            FetchJSON fetchJSON = new FetchJSON(strings[0]); // the heavy lifting happens here
            return fetchJSON.jsonResponse; // if jsonResponse is empty, we return null
        }

        @Override
        protected void onPostExecute(String response) {
            if (response == null) return; // if response is null, cancel
            JSONObject json;
            try {
                json = new JSONObject(response);
                if (json.getInt("response_code") == 0) {
                    // TODO show verbose_msg ("not in dataset") and offer scan through dialog box
                } else {
                    // TODO show results and supply them to ListView
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
            waitingDialog.dismiss();
            Log.d("JSONResponse", response); // TODO remove!
        }
    }




}
