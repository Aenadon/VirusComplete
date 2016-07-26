package aenadon.viruscomplete;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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
            grid = (View) convertView;
        }

        return grid;
    }

    // references to our images
    private Integer[] imageRes = {
            R.drawable.ic_submitfile,
            R.drawable.ic_lookupfile,
            R.drawable.ic_netlookup
    };
    private Integer[] stringRes = {
            R.string.submitfile,
            R.string.lookupfile,
            R.string.title_activity_scan_url
    };

}
