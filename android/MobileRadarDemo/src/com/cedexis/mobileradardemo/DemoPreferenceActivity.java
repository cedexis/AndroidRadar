package com.cedexis.mobileradardemo;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;


public class DemoPreferenceActivity extends PreferenceActivity {
    
    private static final String TAG = "DemoPreferenceActivity";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        
        FragmentManager fragManager = this.getFragmentManager();
        FragmentTransaction trnx = fragManager.beginTransaction();
        FragmentTransaction frag = trnx.replace(android.R.id.content, new DemoPreferenceFragment());
        frag.commit();
        
        PreferenceManager.setDefaultValues(DemoPreferenceActivity.this, R.xml.preferences, false);
    }
}
