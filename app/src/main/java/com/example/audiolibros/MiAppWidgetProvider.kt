package com.example.audiolibros

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.widget.RemoteViews
import android.widget.Toast

import java.util.ArrayList
import java.util.Arrays

import android.content.Context.MODE_PRIVATE

/**
 * Created by vicch on 03/02/2018.
 */

class MiAppWidgetProvider : AppWidgetProvider() {
    private lateinit var aplicacion: Aplicacion

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, widgetIds: IntArray) {

        aplicacion = context.applicationContext as Aplicacion

        for (widgetId in widgetIds) {
            actualizaWidget(context, widgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        aplicacion = context.applicationContext as Aplicacion

        val mgr = AppWidgetManager.getInstance(context)

        val remoteViews = RemoteViews(context.packageName, R.layout.widget)

        if (intent.action == ACCION_REPRODUCTOR) {
            val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            if (!aplicacion.isPlaying) {
                aplicacion.play()
                remoteViews.setImageViewResource(R.id.accion_widget, R.drawable.ic_stop_white_24dp)
            } else {
                aplicacion.stop()
                remoteViews.setImageViewResource(R.id.accion_widget, R.drawable.ic_play_arrow_white_24dp)
            }
            AppWidgetManager.getInstance(context).updateAppWidget(widgetId, remoteViews)
        }
        super.onReceive(context, intent)
    }

    companion object {

        val ACCION_REPRODUCTOR = "com.example.audiolibros.ACCION_REPRODUCTOR"

        fun actualizaWidget(context: Context, widgetId: Int) {
            val pref = context.getSharedPreferences("com.example.audiolibros_internal", MODE_PRIVATE)
            val color = pref.getBoolean("color_" + widgetId, false)

            val ultimo = obtenerUltimoLibro(context)

            val remoteViews = RemoteViews(context.packageName, R.layout.widget)
            remoteViews.setTextViewText(R.id.titulo_widget, ultimo[0])
            remoteViews.setTextViewText(R.id.autor_widget, ultimo[1])

            if (color) {
                remoteViews.setTextColor(R.id.titulo_widget, Color.parseColor("#FF5722"))
                remoteViews.setTextColor(R.id.autor_widget, Color.parseColor("#FF5722"))
            }

            //Anyadir el Intent para abrir la actividad
            val intent = Intent(context, MainActivity::class.java)
            var pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
            remoteViews.setOnClickPendingIntent(R.id.imagen_widget, pendingIntent)

            //Anyadir el intent para reproducir el libro
            val intent2 = Intent(context, MiAppWidgetProvider::class.java)
            intent2.action = ACCION_REPRODUCTOR
            intent2.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            intent2.data = Uri.parse(intent2.toUri(Intent.URI_INTENT_SCHEME))
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT)
            remoteViews.setOnClickPendingIntent(R.id.accion_widget, pendingIntent)

            AppWidgetManager.getInstance(context).updateAppWidget(widgetId, remoteViews)
        }

        private fun obtenerUltimoLibro(context: Context): List<String> {
            val pref = context.getSharedPreferences("com.example.audiolibros_internal", MODE_PRIVATE)
            val titulo = pref.getString("titulo", "No se ha visitado ningún libro")
            val autor = pref.getString("autor", "")
            return Arrays.asList(titulo, autor)
        }
    }

}
