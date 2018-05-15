package com.example.android.newsapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import java.util.HashSet;

public class SettingsActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
    }

    public static class NewsPreferenceFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_main);

            MultiSelectListPreference section = (MultiSelectListPreference) findPreference(getString(R.string.settings_section_key));
            bindPreferenceSummaryToValue(section);

            Preference fromDate = findPreference(getString(R.string.settings_from_date_key));
            bindPreferenceSummaryToValue(fromDate);

            Preference toDate = findPreference(getString(R.string.settings_to_date_key));
            bindPreferenceSummaryToValue(toDate);

            Preference keywordSearch = findPreference(getString(R.string.settings_keyword_search_key));
            bindPreferenceSummaryToValue(keywordSearch);

            Preference orderBy = findPreference(getString(R.string.settings_order_by_key));
            bindPreferenceSummaryToValue(orderBy);

            Preference pageSize = findPreference(getString(R.string.settings_page_size_key));
            bindPreferenceSummaryToValue(pageSize);

        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {

            String valueString = value.toString();

            if (preference instanceof ListPreference) {

                ListPreference lp = (ListPreference) preference;
                int index = lp.findIndexOfValue(valueString);
                if (index >= 0) {
                    CharSequence[] cs = lp.getEntries();
                    preference.setSummary(cs[index]);

                }

            } else if (preference instanceof MultiSelectListPreference){

                // showing the list of chosen options
                MultiSelectListPreference lp = (MultiSelectListPreference) preference;
                CharSequence[] cs = lp.getEntries();
                StringBuilder sb = new StringBuilder();
                for(String list : (HashSet<String>) value){
                    int index = lp.findIndexOfValue(list);
                    if (index >= 0) {
                        if (sb.length() != 0) {
                            sb.append(getResources().getString(R.string.coma));
                        }

                        sb.append(cs[index]);

                    }
                }

                preference.setSummary(sb);

            } else {

                preference.setSummary(valueString);

            }

            return true;
        }

        private void bindPreferenceSummaryToValue(Preference preference) {
            preference.setOnPreferenceChangeListener(this);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());

            Object value;

            if (preference instanceof MultiSelectListPreference) {

                value = preferences.getStringSet(preference.getKey(), null);

            } else {

                value = preferences.getString(preference.getKey(), "");
            }

            onPreferenceChange(preference, value);

        }
    }
}
