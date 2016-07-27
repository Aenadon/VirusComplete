package aenadon.viruscomplete;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class URLDetectionAdapter extends ArrayAdapter<ScanURL.AvCheck> {
    private final Context context;
    private final ArrayList<ScanURL.AvCheck> values;

    public URLDetectionAdapter(Context context, ArrayList<ScanURL.AvCheck> list) {
        super(context, -1, list);
        this.context = context;
        this.values = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) { // position * 2 (+1)
        ResultViewHolder holder;
        LayoutInflater infl = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        convertView = infl.inflate(R.layout.list_item_result_single, null);

        holder = new ResultViewHolder();
        holder.bigText = (TextView) convertView.findViewById(R.id.list_item_result_bigtext);
        holder.smallText = (TextView) convertView.findViewById(R.id.list_item_result_smalltext);
        holder.imageView = (ImageView) convertView.findViewById(R.id.list_item_result_icon);


        String avName = values.get(position).name;
        String rating;
        int imageRes;

        int detect = values.get(position).detection;
        switch (detect) {
            case C.safe:
                imageRes = R.drawable.ic_check;
                rating = context.getString(R.string.safe);
                break;
            case C.unrated:
                imageRes = R.drawable.ic_unrated;
                rating = context.getString(R.string.unrated);
                break;
            case C.unsafe:
                imageRes = R.drawable.ic_warning;
                rating = context.getString(R.string.unsafe);
                break;
            default:
                Log.e("URLDetectionAdapter", "AvCheck class: no int assigned");
                return null;
        }
        holder.bigText.setText(avName);
        holder.smallText.setText(rating);
        holder.imageView.setImageResource(imageRes);

        return convertView;
    }

    private static class ResultViewHolder {
        TextView bigText;
        TextView smallText;
        ImageView imageView;
    }
}
