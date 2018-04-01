package com.example.audiolibros.fragments

import android.os.Bundle
import android.preference.PreferenceFragment

import com.example.audiolibros.R

/**
 * Created by vicch on 28/01/2018.
 */

class PreferenciasFragment : PreferenceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)
    }

}
