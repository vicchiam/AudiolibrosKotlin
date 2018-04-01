package com.example.audiolibros

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import kotlinx.android.synthetic.main.configura_widget.*

/**
 * Created by vicch on 04/02/2018.
 */

class ConfiguraWidget : Activity() {
    var widgetId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.configura_widget)
        setResult(Activity.RESULT_CANCELED)
        val extras = intent.extras
        if (extras == null) {
            finish()
        }
        widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
        }
    }


    fun buttonOK(view: View) {
        val prefs = getSharedPreferences("com.example.audiolibros_internal", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putBoolean("color_" + widgetId, switch1.isChecked)
        editor.commit()
        MiAppWidgetProvider.actualizaWidget(this, widgetId)
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }

}
