package aenadon.viruscomplete;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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

            ArrayList<AvCheck> arrayList = new ArrayList<>();
            Iterator<String> i = jsonScans.keys(); // # http://stackoverflow.com/questions/13573913/android-jsonobject-how-can-i-loop-through-a-flat-json-object-to-get-each-key-a#13573965
            for (int loop = json.getInt("total"); loop > 0; loop--) {
                String key = i.next();
                if (jsonScans.getJSONObject(key).getBoolean("detected")) {
                    arrayList.add(new AvCheck(key, "Unsafe")); // TODO XML!
                } else if (jsonScans.getJSONObject(key).getString("result").equals("unrated site")) {
                    arrayList.add(new AvCheck(key, "Unrated"));
                } else {
                    arrayList.add(new AvCheck(key, "Safe"));
                }
            }
            ListView list = (ListView) findViewById(R.id.list_view_result_list);
            list.setAdapter(new URLDetectionAdapter(this,arrayList));

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    public class AvCheck { // Contains the results of the scan for passing them to the adapter
        String name;
        String detection;

        public AvCheck(String name, String detection) {
            this.name = name;
            this.detection = detection;
        }
    }

}
