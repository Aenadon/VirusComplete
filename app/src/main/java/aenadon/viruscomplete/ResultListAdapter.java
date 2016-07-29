package aenadon.viruscomplete;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class ResultListAdapter extends ArrayAdapter<AvCheck> {
    private final Context context;
    private final ArrayList<AvCheck> values;

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

            holder.smallText.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.GONE);

            @SuppressLint("SimpleDateFormat") SimpleDateFormat sourceDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // this is the source format we need to parse
            sourceDateFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // source date is UTC

            try {
                Date scanDate = sourceDateFormat.parse(values.get(position).date);

                String tempString = String.format(context.getString(R.string.scan_date_text),
                        DateFormat.getDateTimeInstance().format(scanDate),
                        values.get(position).positives,
                        values.get(position).total);

                SpannableString spanString = new SpannableString(tempString);
                spanString.setSpan(new StyleSpan(Typeface.ITALIC), 0, spanString.length(), 0);
                if (values.get(position).positives == 0) {
                    spanString.setSpan(new ForegroundColorSpan(0xFF008800), spanString.length() - 5, spanString.length(), 0); // "00/99" --> 5 characters in
                } else {
                    spanString.setSpan(new ForegroundColorSpan(0xFF880000), spanString.length() - 5, spanString.length(), 0);
                }
                spanString.setSpan(new StyleSpan(Typeface.ITALIC), 0, spanString.length(), 0);


                holder.bigText.setText(spanString);

            } catch (ParseException e) {
                e.printStackTrace();
            }
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
