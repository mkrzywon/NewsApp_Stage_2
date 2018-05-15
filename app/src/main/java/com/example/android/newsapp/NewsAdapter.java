package com.example.android.newsapp;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * An {@link NewsAdapter} knows how to create a list item layout for each article
 * in the data source (a list of {@link News} objects).
 * These list item layouts will be provided to an adapter view like ListView
 * to be displayed to the user.
 */
class NewsAdapter extends ArrayAdapter<News> {

    private static final String TITLE_SEPARATOR = "\\| ";

    private static final String FROM_HTML_BR1 = " <br />";

    private static final String FROM_HTML_BR2 = "<br>";

    private static final String FROM_HTML_STRONG1 = "<strong>";

    private static final String FROM_HTML_STRONG2 = "</strong>";

    /**
     * Constructs a new {@link NewsAdapter}.
     *
     * @param context  of the app
     * @param articles is the list of articles, which is the data source of the adapter
     */
    NewsAdapter(Context context, List<News> articles) {
        super(context, 0, articles);
    }

    /**
     * Returns a list item view that displays information about the article at the given position
     * in the list of articles.
     */
    @TargetApi(Build.VERSION_CODES.O)
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.news_list_item, null);
            holder = new ViewHolder();

            holder.articleTitle = convertView.findViewById(R.id.title);
            holder.articleCategory = convertView.findViewById(R.id.category);
            holder.articleAuthor = convertView.findViewById(R.id.author);
            holder.articleDate = convertView.findViewById(R.id.date);
            holder.trailText = convertView.findViewById(R.id.trailText);
            holder.readMore = convertView.findViewById(R.id.readMore);
            holder.thumbnail = convertView.findViewById(R.id.thumbnail);

            convertView.setTag(holder);

        } else {

            holder = (ViewHolder) convertView.getTag();

        }

        // Find the article at the given position in the list of articles
        News article = getItem(position);

        if (article != null) {

            // Extracting the title without author's name as this one is already present
            // in author's field
            String originalTitle = article.getArticleTitle();
            String authorName = article.getArticleAuthor();
            String slimTitle;

            if (originalTitle.contains(authorName)) {

                String[] parts = originalTitle.split(TITLE_SEPARATOR + authorName);
                slimTitle = parts[0];

            } else {

                slimTitle = originalTitle;
            }

            // Setting the title field
            holder.articleTitle.setTypeface(null, Typeface.BOLD);
            holder.articleTitle.setText(slimTitle);

            // Setting the category field
            holder.articleCategory.setTypeface(null, Typeface.BOLD + Typeface.ITALIC);
            holder.articleCategory.setText(article.getSectionName());

            // Setting the author name field
            String formattedAuthor = (convertView.getResources().getString(R.string.writtenBy) + article.getArticleAuthor());
            holder.articleAuthor.setText(formattedAuthor);

            // Setting the date field
            String date = article.getDatePublished();
            String dateFormatted = (convertView.getResources().getString(R.string.publishedOn) + formattedDate(date));
            holder.articleDate.setText(dateFormatted);

            // Some trail texts contain html tags. Code below extracts trail text without these tags.
            String trailText = article.getTrailText();
            String preFormattedTrailText;
            String[] parts;
            if (trailText.contains(FROM_HTML_BR1)) {

                parts = trailText.split(FROM_HTML_BR1);
                preFormattedTrailText = parts[0];

            } else if (trailText.contains(FROM_HTML_BR2)) {

                parts = trailText.split(FROM_HTML_BR2);
                preFormattedTrailText = parts[0];

            } else if (trailText.contains(FROM_HTML_STRONG1) && trailText.contains(FROM_HTML_STRONG2)){

                parts = trailText.split(FROM_HTML_STRONG1);
                String tempTrailText = parts[1];

                preFormattedTrailText = tempTrailText.replace(FROM_HTML_STRONG2, "");

            } else {

                preFormattedTrailText = trailText;

            }

            // Setting the trail text field
            String formattedTrailText = (preFormattedTrailText + convertView.getResources().getString(R.string.dots));
            holder.trailText.setText(formattedTrailText);

            // Setting read more TextView with slight animation
            holder.readMore.setText(R.string.readMore);
            setReadMoreAnimation(holder.readMore);

            // Using the Picasso plugin to set the proper thumbnail with given url
            Picasso.get().load(article.getThumbnailUrl()).into(holder.thumbnail);

        }

        // Return the convertView that is now showing the appropriate data
        return convertView;

    }

    // This method set the animation for read more TextView
    private void setReadMoreAnimation(TextView textView) {

        final TranslateAnimation ta = new TranslateAnimation(-10, 10, 0, 0);
        ta.setInterpolator(new BounceInterpolator());
        ta.setStartOffset(3000);
        ta.setDuration(400);
        ta.setRepeatMode(TranslateAnimation.RESTART);
        ta.setRepeatCount(TranslateAnimation.INFINITE);
        textView.setAnimation(ta);

    }

    // Returns the formatted date String
    private String formattedDate(String inputDate) {

        String inputFormat = getContext().getResources().getString(R.string.input_format);

        String outputFormat = getContext().getResources().getString(R.string.output_format);

        Date parsed;
        String outputDate = "";

        SimpleDateFormat dfInput = new SimpleDateFormat(inputFormat, java.util.Locale.getDefault());
        SimpleDateFormat dfOutput = new SimpleDateFormat(outputFormat, java.util.Locale.getDefault());

        try {

            parsed = dfInput.parse(inputDate);
            outputDate = dfOutput.format(parsed);

        } catch (Exception e) {

            Log.e(getContext().getResources().getString(R.string.adapter_date_log_tag), getContext().getResources().getString(R.string.adapter_date_log_msg) + e.getMessage());

        }

        return outputDate;
    }

    private static class ViewHolder {

        private TextView articleTitle;
        private TextView articleCategory;
        private TextView articleAuthor;
        private TextView articleDate;
        private TextView trailText;
        private TextView readMore;
        private ImageView thumbnail;

    }
}