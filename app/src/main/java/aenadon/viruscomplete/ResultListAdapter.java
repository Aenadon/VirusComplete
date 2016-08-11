package aenadon.viruscomplete;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

class ResultListAdapter extends ArrayAdapter<AvCheck> {
    private final Context context;
    private final ArrayList<AvCheck> values;

    private static final String LOG_TAG = ResultListAdapter.class.getName();

    public ResultListAdapter(Context context, ArrayList<AvCheck> list) {
        super(context, -1, list);
        this.context = context;
        this.values = list;
    }

    @SuppressLint({"ViewHolder", "InflateParams"})
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ResultViewHolder holder;
        LayoutInflater infl = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        convertView = infl.inflate(R.layout.list_item_result_single, null);

        holder = new ResultViewHolder();
        holder.bigText = (TextView) convertView.findViewById(R.id.list_item_result_bigtext);
        holder.smallText = (TextView) convertView.findViewById(R.id.list_item_result_smalltext);
        holder.imageView = (ImageView) convertView.findViewById(R.id.list_item_result_icon);


        if (values.get(position).isFirstRow) {

            // For the first row, we don't need those 2 layouts
            holder.smallText.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.GONE);

            String adjustedDate = C.getAdjustedDate(values.get(position).date);
            if (adjustedDate.isEmpty()) {
                Log.e(LOG_TAG, "Date parse error in C.getAdjustedDate(String initialDate)");
                return null;
            }

            String unstyledString = String.format(context.getString(R.string.scan_date_text),
                    adjustedDate,
                    values.get(position).positives,
                    values.get(position).total);

            SpannableString styledString = new SpannableString(unstyledString);
            styledString.setSpan(new StyleSpan(Typeface.ITALIC), 0, styledString.length(), 0);
            if (values.get(position).positives == 0) {
                styledString.setSpan(new ForegroundColorSpan(0xFF008800), styledString.length() - 5, styledString.length(), 0); // "00/99" --> 5 characters in
            } else {
                styledString.setSpan(new ForegroundColorSpan(0xFF880000), styledString.length() - 5, styledString.length(), 0);
            }
            styledString.setSpan(new StyleSpan(Typeface.ITALIC), 0, styledString.length(), 0);
            holder.bigText.setText(styledString);

        } else if (values.get(position).isFileScan) {
            String avName = values.get(position).name;
            String malwareName = values.get(position).malwareName;
            int imageRes;
            if (malwareName != null) {
                malwareName = values.get(position).malwareName;
                imageRes = R.drawable.ic_warning;
            } else {
                malwareName = context.getString(R.string.safe);
                imageRes = R.drawable.ic_check;
            }

            holder.bigText.setText(avName);
            holder.smallText.setText(malwareName);
            holder.imageView.setImageResource(imageRes);
        } else {
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
                    return convertView;
            }
            holder.bigText.setText(avName);
            holder.smallText.setText(rating);
            holder.imageView.setImageResource(imageRes);
        }

        return convertView;
    }

    private static class ResultViewHolder {
        TextView bigText;
        TextView smallText;
        ImageView imageView;
    }
}
