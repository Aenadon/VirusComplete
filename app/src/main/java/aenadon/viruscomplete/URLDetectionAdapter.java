package aenadon.viruscomplete;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class URLDetectionAdapter extends ArrayAdapter<ShowResults.AvCheck> {
    private final Context context;
    private final ArrayList<ShowResults.AvCheck> values;

    public URLDetectionAdapter(Context context, ArrayList<ShowResults.AvCheck> list) {
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

        holder.bigText.setText(values.get(position).name);
        holder.smallText.setText(values.get(position).detection);

        String detect = values.get(position).detection;
        switch (detect) { // TODO XML Strings!
            case "Safe":
                holder.imageView.setImageResource(R.drawable.ic_check);
                break;
            case "Unrated":
                holder.imageView.setImageResource(R.drawable.ic_unrated);
                break;
            case "Unsafe":
                holder.imageView.setImageResource(R.drawable.ic_warning);
                break;
        }
        return convertView;
    }

    private static class ResultViewHolder {
        TextView bigText;
        TextView smallText;
        ImageView imageView;
    }
}
