package aenadon.viruscomplete;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class ShowResults extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_results);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        try { // do all the JSON stuff inside "try" because otherwise: unhandled JSONException
            JSONObject json = new JSONObject(extras.getString("json"));
            JSONObject jsonScans = json.getJSONObject("scans");
            Log.d("test", json.toString());

            ArrayList<AvCheck> everythingTogether = new ArrayList<>();
            // we save each of them in their own list, sort them individually and then bring them together
            ArrayList<AvCheck> unsafes = new ArrayList<>();
            ArrayList<AvCheck> unrateds = new ArrayList<>();
            ArrayList<AvCheck> safes = new ArrayList<>();

            Iterator<String> i = jsonScans.keys(); // # http://stackoverflow.com/questions/13573913/android-jsonobject-how-can-i-loop-through-a-flat-json-object-to-get-each-key-a#13573965
            for (int loop = json.getInt("total"); loop > 0; loop--) {
                String key = i.next();
                if (jsonScans.getJSONObject(key).getBoolean("detected")) {
                    unsafes.add(new AvCheck(key, C.unsafe));
                } else if (jsonScans.getJSONObject(key).getString("result").equals("unrated site")) {
                    unrateds.add(new AvCheck(key, C.unrated));
                } else {
                    safes.add(new AvCheck(key, C.safe));
                }
            }
            // sort each list and then add them together
            Collections.sort(unsafes);Collections.sort(safes);Collections.sort(unrateds);
            everythingTogether.addAll(unsafes);everythingTogether.addAll(safes);everythingTogether.addAll(unrateds);

            ListView list = (ListView) findViewById(R.id.list_view_result_list);
            list.setAdapter(new URLDetectionAdapter(this, everythingTogether));

            String detectionCount = String.format(getString(R.string.detectCount), json.getInt("positives"), json.getInt("total"));
            Toast.makeText(this, detectionCount, Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }

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
