package com.example.audiolibros

import android.app.Activity
import android.os.Bundle

import com.example.audiolibros.fragments.PreferenciasFragment

/**
 * Created by vicch on 28/01/2018.
 */

class PreferenciasActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentManager.beginTransaction().replace(android.R.id.content, PreferenciasFragment()).commit()
    }

}
