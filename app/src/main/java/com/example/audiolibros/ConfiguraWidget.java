package com.example.audiolibros;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

/**
 * Created by vicch on 04/02/2018.
 */

public class ConfiguraWidget extends Activity {
    int widgetId;
    Switch colorSwitch;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.configura_widget);
        colorSwitch=(Switch) findViewById(R.id.switch1);
        setResult(RESULT_CANCELED);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            finish();
        }
        widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID);
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
    }


    public void buttonOK(View view) {
        SharedPreferences prefs = getSharedPreferences("com.example.audiolibros_internal", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("color_"+widgetId, colorSwitch.isChecked());
        editor.commit();
        MiAppWidgetProvider.actualizaWidget(this, widgetId);
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

}
