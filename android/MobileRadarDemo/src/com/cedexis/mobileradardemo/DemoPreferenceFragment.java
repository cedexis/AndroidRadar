package com.cedexis.mobileradardemo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.util.Log;

public class DemoPreferenceFragment extends PreferenceFragment
    implements SharedPreferences.OnSharedPreferenceChangeListener {
    
    private static String TAG = "DemoPreferenceFragment";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.addPreferencesFromResource(R.xml.preferences);
        
        // Register as listener
        this.getPreferenceScreen().getSharedPreferences()
            .registerOnSharedPreferenceChangeListener(this);
        
        // Update summaries
        this.updateSummaryForKey("zoneId");
        this.updateSummaryForKey("customerId");
    }
    
    private void updateSummaryForKey(String key) {
        SharedPreferences prefs = this.getPreferenceScreen().getSharedPreferences();
        Log.d(TAG, String.format("%s set to %s", key, prefs.getString(key, null)));
        EditTextPreference pref = (EditTextPreference)this.findPreference(key);
        pref.setSummary(String.format("Currently set to %s", prefs.getString(key, null)));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.equals("zoneId") || key.equals("customerId")) {
            this.updateSummaryForKey(key);
        }
    }

}
