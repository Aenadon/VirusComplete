package aenadon.viruscomplete;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
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

public class ScanFileLookup extends AppCompatActivity {

    ProgressDialog waitingDialog;
    String hashToCheck;
    String apikey = BuildConfig.API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_file_lookup);
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

    public void pasteValue(View view) {
        EditText editText = (EditText) findViewById(R.id.box_filehash);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            android.content.ClipboardManager clipboardNew = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (clipboardNew.hasPrimaryClip()) editText.setText(clipboardNew.getPrimaryClip().getItemAt(0).getText().toString());
        } else {
            @SuppressWarnings("deprecation") // this runs only when an old Android version is used
            android.text.ClipboardManager clipboardOld = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (clipboardOld.hasText()) editText.setText(clipboardOld.getText());
        }
    }

    public void lookupHash(View view) {
        EditText editText = (EditText) findViewById(R.id.box_filehash);
        // close the keyboard
        View v = this.getCurrentFocus();
        if (v != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
        hashToCheck = editText.getText().toString();
        if (hashToCheck.trim().isEmpty()) return; // no input - no scan
        // show a "please wait" dialog and retrieve the latest existing report
        waitingDialog.show();

        RetrofitBase.getRetrofit().create(VirusTotalApiCalls.class).getFileReportForHash(apikey, hashToCheck).enqueue(new Callback<VirusTotalResponse>() {

            @Override
            public void onResponse(Call<VirusTotalResponse> call, Response<VirusTotalResponse> response) {
                waitingDialog.dismiss(); // dismiss the waiting dialog before showing anything
                if (response.code() == 204 || !response.isSuccessful()) {
                    C.errorCheck(response.code(), ScanFileLookup.this);
                    return; // we show the error message before, then we interrupt the task
                }
                final VirusTotalResponse results = response.body();
                if (results.getResponse_code() == -1) {
                    new AlertDialog.Builder(ScanFileLookup.this)
                            .setTitle(getString(R.string.strange_error_title))
                            .setMessage(getString(R.string.strange_error_message))
                            .setPositiveButton(getString(R.string.try_again), null)
                            .show();
                } else if (results.getResponse_code() == 0) {
                    new AlertDialog.Builder(ScanFileLookup.this)
                            .setTitle(getString(R.string.res_not_found_title))
                            .setMessage(getString(R.string.res_not_found_message))
                            .setPositiveButton(getString(R.string.try_again), null)
                            .show();
                } else {
                    displayResults(results);
                }
            }

            @Override
            public void onFailure(Call<VirusTotalResponse> call, Throwable t) {
                new AlertDialog.Builder(ScanFileLookup.this)
                        .setTitle(getString(R.string.data_error_title))
                        .setMessage(getString(R.string.data_error_message) + t.getLocalizedMessage())
                        .setPositiveButton(getString(R.string.sorry), null)
                        .show();
            }
        });
    }

    private void displayResults(VirusTotalResponse scanResults) {
        JsonObject jsonScans = scanResults.getScans().getAsJsonObject();

        ArrayList<AvCheck> everythingTogether = new ArrayList<>();
        // we save each of them in their own list, sort them individually and then bring them together
        ArrayList<AvCheck> unsafes = new ArrayList<>();
        ArrayList<AvCheck> safes = new ArrayList<>();

        Set<Map.Entry<String, JsonElement>> entries = jsonScans.entrySet(); //will return members of your object
        for (Map.Entry<String, JsonElement> entry : entries) {
            boolean isDetected = entry.getValue().getAsJsonObject().get("detected").getAsBoolean();

            if (isDetected) {
                String malwareName = entry.getValue().getAsJsonObject().get("result").getAsString();
                unsafes.add(new AvCheck(entry.getKey(), malwareName, true));
            } else {
                safes.add(new AvCheck(entry.getKey(), null, true)); // safe --> no "malware name"
            }
        }

        // sort each list and then add them together
        Collections.sort(unsafes);
        Collections.sort(safes);

        // first list row contains date and detections count
        everythingTogether.add(new AvCheck(scanResults.getScan_date(), scanResults.getPositives(), scanResults.getTotal(), true));
        everythingTogether.addAll(unsafes);
        everythingTogether.addAll(safes);

        ListView list = (ListView) findViewById(R.id.list_file_scan_lookup_results); // inside R.layout.content_file_scan_lookup
        list.setAdapter(new ResultListAdapter(this, everythingTogether));
    }


}
