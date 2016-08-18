package aenadon.viruscomplete;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.ListView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.commons.codec.binary.Hex;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

class C {

   /*********************************************
    * This class is for common constants and    *
    * functions used throughout the activities. *
    *********************************************/

    // only used in ScanURL (for categorizing results)
    public static final int unsafe = 0;
    public static final int safe = 1;
    public static final int unrated = 2;

    // only used in ScanFileSend for requesting file access
    public static final int REQUEST_FILE_ACCESS = 2;

    // Used for SharedPreferences recording queued scans
    public static final String queuedResources = "queued_resources";
    public static final String noScanYet = "no scan yet";

    // every scan class uses this
    public static void errorCheck(int responseCode, Context ctx) {
        switch (responseCode) {
            case 204:
                AlertDialogs.apiLimitExceeded(ctx);
                break;
            case 403: // This shouldn't ever happen, but, just in case...
                AlertDialogs.notPermitted403(ctx);
                break;
            case 500:
                AlertDialogs.serverError(ctx);
                break;
            default:
                AlertDialogs.strangeError(ctx);
                break;
        }
    }

    // every scan class uses this
    public static void displayResults(Context c, VirusTotalResponse scanResults, int listRef) {
        JsonObject jsonScans = scanResults.getScans().getAsJsonObject();

        ArrayList<AvCheck> everythingTogether = new ArrayList<>();
        // we save each of them in their own list, sort them individually and then bring everythingTogether
        ArrayList<AvCheck> unsafes = new ArrayList<>();
        ArrayList<AvCheck> safes = new ArrayList<>();

        Set<Map.Entry<String, JsonElement>> entries = jsonScans.entrySet(); //will return members of your object
        for (Map.Entry<String, JsonElement> entry : entries) {
            boolean isDetected = entry.getValue().getAsJsonObject().get("detected").getAsBoolean();
            if (isDetected) {
                String malwareName = entry.getValue().getAsJsonObject().get("result").getAsString();
                unsafes.add(new AvCheck(entry.getKey(), malwareName, true));
            } else {
                safes.add(new AvCheck(entry.getKey(), null, true)); // safe --> "malware name" == null
            }
        }
        // sort each list
        Collections.sort(unsafes);
        Collections.sort(safes);

        // first list row contains date and detections count, then add everything together
        everythingTogether.add(new AvCheck(scanResults.getScan_date(), scanResults.getPositives(), scanResults.getTotal(), true));
        everythingTogether.addAll(unsafes);
        everythingTogether.addAll(safes);

        ListView list = (ListView) ((Activity)c).findViewById(listRef);
        list.setAdapter(new ResultListAdapter(c, everythingTogether));
    }

    public static String getSHA256(String filePath) {
        // ### http://stackoverflow.com/a/2932513/3673616 -- see comment #4 ###
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(new File(filePath));
            byte data[] = org.apache.commons.codec.digest.DigestUtils.sha256(fis);
            char sha256chars[] = Hex.encodeHex(data);
            return String.valueOf(sha256chars);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("SHA256-Calculation", e.getMessage());
            return "";
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // ScanFileSend + ScanHashLookup use that
    public static void forceHashRescan(final Context ctx, final String givenHash, final String lastScanDate) {

        final ProgressDialog waitingDialog = new ProgressDialog(ctx);
        waitingDialog.setMessage(ctx.getString(R.string.please_wait));
        waitingDialog.setIndeterminate(true);
        waitingDialog.setCancelable(false);
        waitingDialog.show();

        RetrofitBase.getRetrofit().create(VirusTotalApiCalls.class).forceHashRescan(BuildConfig.API_KEY, givenHash).enqueue(new Callback<VirusTotalResponse>() {
            @Override
            public void onResponse(Call<VirusTotalResponse> call, Response<VirusTotalResponse> response) {
                waitingDialog.dismiss();
                if (response.code() == 204 || !response.isSuccessful()) {
                    errorCheck(response.code(), ctx);
                    return;
                }
                final VirusTotalResponse results = response.body();
                // Normally, there is also error code 0 (file not in DB) -
                // but as we only allow rescan if a report has already
                // been retrieved, this case is impossible to achieve
                if (results.getResponse_code() == -1) {
                    AlertDialogs.strangeError(ctx);
                } else {
                    // Put the hash + last scan date into the sharedpreferences so we can tell the user if the scan is still pending.
                    ctx.getSharedPreferences(C.queuedResources, Context.MODE_PRIVATE).edit().putString(givenHash, lastScanDate).apply();
                    AlertDialogs.resourceIsQueued(ctx);
                }
            }
            @Override
            public void onFailure(Call<VirusTotalResponse> call, Throwable t) {
                waitingDialog.dismiss();
                AlertDialogs.onFailureMessage(ctx, t.getLocalizedMessage());
            }
        });
    }

    public static String getAdjustedDate(String initialDate) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sourceDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // this is the source format we need to parse
        sourceDateFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // source date is UTC
        String correctedDate = "";
        try {
            Date scanDate = sourceDateFormat.parse(initialDate);
            correctedDate = DateFormat.getDateTimeInstance().format(scanDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return correctedDate;
    }

}
