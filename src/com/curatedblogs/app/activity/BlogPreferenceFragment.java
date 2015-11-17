package com.curatedblogs.app.activity;

import android.os.Bundle;

import com.curatedblogs.app.R;

/**
 * Created by Anshu on 16/04/15.
 */
public class BlogPreferenceFragment extends android.preference.PreferenceFragment
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fragment_preference);
    }
}