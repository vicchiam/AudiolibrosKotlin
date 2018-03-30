package com.example.audiolibros.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.example.audiolibros.R;

/**
 * Created by vicch on 28/01/2018.
 */

public class PreferenciasFragment extends PreferenceFragment {

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

}
