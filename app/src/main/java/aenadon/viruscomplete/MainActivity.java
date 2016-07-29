package aenadon.viruscomplete;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static Context context = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        GridView gvMenu = (GridView)findViewById(R.id.main_menuGrid);
        gvMenu.setAdapter(new MenuAdapter(this));

        context = this;
        gvMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                switch (position) {
                    case 0:
                        startActivity(new Intent(context, ScanFileSend.class));
                        break;
                    case 1:
                        startActivity(new Intent(context, ScanFileLookup.class));
                        break;
                    case 2:
                        startActivity(new Intent(context, ScanURL.class));
                        break;
                }

            }
        });

    }

    // https://developer.android.com/guide/topics/ui/layout/gridview.html
    // https://www.learn2crack.com/2014/01/android-custom-gridview.html

    public class MenuAdapter extends BaseAdapter {

        private Context context;

        public MenuAdapter(Context c) {
            this.context = c;
        }

        public int getCount() {
            return imageRes.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            View grid;
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                grid = inflater.inflate(R.layout.grid_single, null);
                TextView text = (TextView) grid.findViewById(R.id.grid_text);
                ImageView image = (ImageView) grid.findViewById(R.id.grid_image);
                text.setText(context.getString(stringRes[position]));
                image.setImageResource(imageRes[position]);

            } else {
                grid = convertView;
            }

            return grid;
        }

        // references to our images
        private Integer[] imageRes = {
                R.drawable.ic_file_scan_submit,
                R.drawable.ic_file_scan_lookup,
                R.drawable.ic_url_scan
        };
        private Integer[] stringRes = {
                R.string.title_activity_file_scan_submit,
                R.string.title_activity_file_scan_lookup,
                R.string.title_activity_url_scan
        };

    }


}
