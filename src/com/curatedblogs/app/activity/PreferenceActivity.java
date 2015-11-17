package com.curatedblogs.app.activity;

import android.content.SharedPreferences;

import com.curatedblogs.app.R;
import com.curatedblogs.app.common.BlogApplication;
import com.curatedblogs.app.utils.ParseUtility;

import java.util.List;

/**
 * Created by Anshu on 16/04/15.
 */
public class PreferenceActivity extends android.preference.PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
    public static final String CREATE_PREF = "createPref";
    public static final String UPDATE_PREF = "updatePref";

    @Override
    public void onBuildHeaders(List<Header> target)
    {
        loadHeadersFromResource(R.xml.headers_preference, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName)
    {
        return BlogPreferenceFragment.class.getName().equals(fragmentName);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        registerForNotifications();
    }

    public static void registerForNotifications() {
        if (BlogApplication.preferences.getBoolean(PreferenceActivity.CREATE_PREF, true)) {
            ParseUtility.subscribeToChannel("create");
        }else {
            ParseUtility.unSubscribeFromChannel("create");
        }

        if (BlogApplication.preferences.getBoolean(PreferenceActivity.UPDATE_PREF, true)) {
            ParseUtility.subscribeToChannel("update");
        }else {
            ParseUtility.unSubscribeFromChannel("update");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        BlogApplication.preferences
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        BlogApplication.preferences
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
