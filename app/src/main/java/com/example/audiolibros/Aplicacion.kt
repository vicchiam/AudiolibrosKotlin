package com.example.audiolibros

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.util.LruCache

import com.android.volley.RequestQueue
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.Volley

import java.io.IOException

import com.example.audiolibros.ejemploLibros

/**
 * Created by vicch on 26/01/2018.
 */

class Aplicacion : Application() {

    lateinit var listaLibros: MutableList<Libro>
    lateinit var adaptador: AdaptadorLibrosFiltro

    private  var mediaPlayer: MediaPlayer? = null

    val isPlaying: Boolean
        get() = mediaPlayer?.isPlaying ?: false;

    @SuppressLint("MissingSuperCall")
    override fun onCreate() {
        colaPeticiones = Volley.newRequestQueue(this)
        lectorImagenes = ImageLoader(colaPeticiones, object : ImageLoader.ImageCache {
            private val cache = LruCache<String, Bitmap>(10)

            override fun getBitmap(url: String): Bitmap? {
                return cache.get(url)
            }

            override fun putBitmap(url: String, bitmap: Bitmap) {
                cache.put(url, bitmap)
            }
        })

        listaLibros = ejemploLibros()
        adaptador = AdaptadorLibrosFiltro(this, listaLibros)
    }

    fun playPause() {
        mediaPlayer?.let({
            if(it.isPlaying){
                play()
            }
            else{
                stop()
            }
        })
    }

    fun play() {
        mediaPlayer = MediaPlayer()
        mediaPlayer?.setOnPreparedListener { mediaPlayer -> mediaPlayer.start() }
        val (_, _, _, urlAudio) = adaptador.getItem(obtenerUltimoLibro())
        val audio = Uri.parse(urlAudio)
        try {
            mediaPlayer?.setDataSource(applicationContext, audio)
            mediaPlayer?.prepareAsync()
        } catch (e: IOException) {
            Log.e("Audiolibros", "ERROR: No se puede reproducir " + audio, e)
        }

    }

    fun stop() {
        mediaPlayer?.let({
            if(it.isPlaying){
                it.stop()
            }
        })
    }

    fun obtenerUltimoLibro(): Int {
        val pref = applicationContext.getSharedPreferences("com.example.audiolibros_internal", Context.MODE_PRIVATE)
        return pref.getInt("ultimo", -1)
    }

    companion object {

        var colaPeticiones: RequestQueue? = null
        var lectorImagenes: ImageLoader? = null

    }

}
