package com.cedexis.mobileradardemo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.util.Log;

public class DemoPreferenceFragment
    extends PreferenceFragment
    implements SharedPreferences.OnSharedPreferenceChangeListener {
    
    private static final String TAG = "DemoPreferenceFragment";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        
        // Load settings from XML resource
        this.addPreferencesFromResource(R.xml.preferences);
        
        // Get screen settings
        SharedPreferences settings = this.getPreferenceScreen()
                .getSharedPreferences();
        
        // Register as listener
        settings.registerOnSharedPreferenceChangeListener(this);
        
        // Update summaries
        this.updateSummaries(settings);
    }

    private void updateSummaries(SharedPreferences settings) {
        for (PreferencesHandlers current: PreferencesHandlers.all) {
            EditTextPreference pref = (EditTextPreference)this.findPreference(current.getKey());
            pref.setSummary(String.format("Currently set to %s", settings.getString(current.getKey(), null)));
        }
    }
    
    static class PreferencesHandlers {
        
        private String _key;
        
        private PreferencesHandlers(String key) {
            this._key = key;
        }
        
        public static PreferencesHandlers fromKey(String key) {
            for (PreferencesHandlers current: PreferencesHandlers.all) {
                if (key.equals(current.getKey())) {
                    return current;
                }
            }
            return null;
        }
        
        public String getKey() {
            return this._key;
        }
        
        static final PreferencesHandlers zoneId = new PreferencesHandlers("zoneId");
        
        static final PreferencesHandlers customerId = new PreferencesHandlers("customerId");
        
        static final PreferencesHandlers[] all = new PreferencesHandlers[] {
            PreferencesHandlers.zoneId,
            PreferencesHandlers.customerId
        };
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        Log.i(TAG, "onSharedPreferenceChanged");
        this.updateSummaries(sharedPreferences);
        DemoPreferenceActivity activity = (DemoPreferenceActivity)this.getActivity();
        MobileRadarDemoApplication app = (MobileRadarDemoApplication)activity
                .getApplication();
        app.restartRadar();
    }
}
