package com.example.android.newsapp;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<News>> {

    private static final String LOG_TAG = MainActivity.class.getName();

    /**
     * URL for news data from the the Guardian dataset
     */
    private static final String GUARDIAN_REQUEST_URL =
            "https://content.guardianapis.com/search";

    /**
     * Constant value for the article loader ID
     */
    private static final int NEWS_LOADER_ID = 1;

    /**
     * prefix used in the construction of sections string
     */
    private static final String SECTION_SEPARATOR = "|";

    /**
     * Adapter for the list of articles
     */
    private NewsAdapter mAdapter;

    /**
     * TextView that is displayed when the list is empty
     */
    private TextView mEmptyList;

    /**
     * SwipeRefreshLayout for refreshing the articles
     */
    private SwipeRefreshLayout swipeRefreshLayout;

    /**
     * URL constructor
     */
    Uri.Builder uriBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find a reference to the {@link ListView} in the layout
        ListView articleListView = findViewById(R.id.list);

        mEmptyList = findViewById(R.id.no_content);
        articleListView.setEmptyView(mEmptyList);

        // Create a new adapter that takes an empty list of articles as input
        mAdapter = new NewsAdapter(this, new ArrayList<News>());

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        articleListView.setAdapter(mAdapter);

        // Set the refresher for article list
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);

        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimaryDark),
                getResources().getColor(R.color.swipeRefreshLayout_color1),
                getResources().getColor(R.color.swipeRefreshLayout_color2),
                getResources().getColor(R.color.swipeRefreshLayout_color3));

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                restartLoader();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        // Set an item click listener on the ListView, which sends an intent to a web browser
        // to open a website with more information about the selected article.
        articleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Find the current article that was clicked on
                News currentArticle = mAdapter.getItem(position);

                if (currentArticle != null) {

                    // Convert the String URL into a URI object (to pass into the Intent constructor)
                    Uri articleUri = Uri.parse(currentArticle.getArticleUrl());

                    // Create a new intent to view the article URI
                    Intent websiteIntent = new Intent(Intent.ACTION_VIEW, articleUri);

                    // Send the intent to launch a new activity
                    startActivity(websiteIntent);

                }
            }
        });

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm != null) {

            // Get details on the currently active default data network
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();

            // If there is a network connection, fetch data
            if (networkInfo != null && networkInfo.isConnected()) {
                // Get a reference to the LoaderManager, in order to interact with loaders.
                LoaderManager lm = getLoaderManager();

                // Initialize the loader. Pass in the int ID constant defined above and pass in null for
                // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
                // because this activity implements the LoaderCallbacks interface).
                lm.initLoader(NEWS_LOADER_ID, null, this);
            } else {
                // Otherwise, display error
                // First, hide loading indicator so error message will be visible
                View loadingIndicator = findViewById(R.id.loading_indicator);
                loadingIndicator.setVisibility(View.GONE);

                // Update empty state with no connection error message
                mEmptyList.setText(getResources().getString(R.string.no_connection));
            }
        }
    }

    @Override
    public Loader<List<News>> onCreateLoader(int i, Bundle bundle) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        Set<String> sectionValues = sharedPrefs.getStringSet(getString(R.string.settings_section_key), null);

        String sections = "";

        if (sectionValues != null) {

            for (String section : sectionValues) {

                String temp = sections.concat(SECTION_SEPARATOR).concat(section);

                StringBuilder sb = new StringBuilder(temp);

                int index = sb.indexOf(SECTION_SEPARATOR);

                if (index < 1) {

                    sb.delete(index, index + SECTION_SEPARATOR.length());

                }

                sections = sb.toString();

            }
        }

        String fromDate = sharedPrefs.getString(
                getString(R.string.settings_from_date_key),
                getString(R.string.settings_from_date_default));

        String toDate = sharedPrefs.getString(
                getString(R.string.settings_to_date_key),
                getString(R.string.settings_to_date_default));

        String keywordSearch = sharedPrefs.getString(
                getString(R.string.settings_keyword_search_key),
                getString(R.string.settings_keyword_search_default));

        String orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));

        String pageSize = sharedPrefs.getString(
                getString(R.string.settings_page_size_key),
                getString(R.string.settings_page_size_default));

        Uri baseUri = Uri.parse(GUARDIAN_REQUEST_URL);
        uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter("format", "json");
        uriBuilder.appendQueryParameter("use-date", "published");
        uriBuilder.appendQueryParameter("show-tags", "contributor");
        uriBuilder.appendQueryParameter("show-fields", "all");
        uriBuilder.appendQueryParameter("api-key", "6564320b-a05d-4650-9396-17c26f5f3582");

        if (pageSize.isEmpty()) {

            emptyUri();

        } else {

            uriBuilder.appendQueryParameter("page-size", pageSize);

        }

        if (sections.isEmpty()) {

            emptyUri();

        } else {

            uriBuilder.appendQueryParameter("section", sections);

        }

        if (fromDate.isEmpty()) {

            emptyUri();

        } else {

            uriBuilder.appendQueryParameter("from-date", fromDate);

        }

        if (toDate.isEmpty()) {

            emptyUri();

        } else {

            uriBuilder.appendQueryParameter("to-date", toDate);

        }

        uriBuilder.appendQueryParameter("q", keywordSearch);
        uriBuilder.appendQueryParameter("order-by", orderBy);

        // Log for checking if constructed URL is ok.
        Log.v(LOG_TAG, getResources().getString(R.string.built_url) + uriBuilder);

        // Create a new loader for the given URL
        return new NewsLoader(this, uriBuilder.toString());

    }

    private void emptyUri() {

        uriBuilder.appendQueryParameter("", "");

    }

    @Override
    public void onLoadFinished(Loader<List<News>> loader, List<News> articles) {

        // Hide loading indicator because the data has been loaded
        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        // Set empty state text to display "No articles found."
        mEmptyList.setText(getResources().getString(R.string.no_articles));

        // Clear the adapter of previous article data
        mAdapter.clear();

        // If there is a valid list of {@link News}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (articles != null && !articles.isEmpty()) {
            mAdapter.addAll(articles);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<News>> loader) {
        // Loader reset, so we can clear out our existing data.
        mAdapter.clear();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Get a reference to the LoaderManager, in order to interact with loaders.
        LoaderManager loaderManager = getLoaderManager();

        // Restart the loader.
        loaderManager.restartLoader(NEWS_LOADER_ID, null, this);
    }

    // This method restarts loader (refreshing articles)
    private void restartLoader() {

        getLoaderManager().restartLoader(1, null, this);

    }

    // Toolbar menu and icons methods
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // settings menu
        if (id == R.id.menu_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        // toolbar refresh icon for restarting loader (for someone with old phone for example)
        if (id == R.id.menu_refresh) {
            restartLoader();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
