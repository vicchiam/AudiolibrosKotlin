package com.example.audiolibros;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import java.io.IOException;
import java.util.List;

/**
 * Created by vicch on 26/01/2018.
 */

public class Aplicacion extends Application{

    private List<Libro> listaLibros;
    private AdaptadorLibrosFiltro adaptador;

    private static RequestQueue colaPeticiones;
    private static ImageLoader lectorImagenes;

    private MediaPlayer mediaPlayer;

    @SuppressLint("MissingSuperCall")
    @Override
    public void onCreate(){
        colaPeticiones = Volley.newRequestQueue(this);
        lectorImagenes = new ImageLoader(colaPeticiones, new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap> cache = new LruCache<>(10);

            @Override
            public Bitmap getBitmap(String url) {
                return cache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                cache.put(url,bitmap);
            }
        });

        listaLibros = Libro.ejemploLibros();
        adaptador = new AdaptadorLibrosFiltro(this,listaLibros);
    }

    public AdaptadorLibrosFiltro getAdaptador(){
        return adaptador;
    }

    public List<Libro> getListaLibros(){
        return listaLibros;
    }

    public static RequestQueue getColaPeticiones() {
        return colaPeticiones;
    }

    public static void setColaPeticiones(RequestQueue colaPeticiones) {
        Aplicacion.colaPeticiones = colaPeticiones;
    }

    public static ImageLoader getLectorImagenes() {
        return lectorImagenes;
    }

    public static void setLectorImagenes(ImageLoader lectorImagenes) {
        Aplicacion.lectorImagenes = lectorImagenes;
    }

    public void playPause(){
        if(mediaPlayer==null || !mediaPlayer.isPlaying()){
            play();
        }
        else{
            stop();
        }
    }

    public void play(){
        mediaPlayer=new MediaPlayer();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
            }
        });
        Libro libro=adaptador.getItem(obtenerUltimoLibro());
        Uri audio = Uri.parse(libro.urlAudio);
        try {
            mediaPlayer.setDataSource(getApplicationContext(), audio);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Log.e("Audiolibros", "ERROR: No se puede reproducir "+audio,e);
        }
    }

    public void stop(){
        if(mediaPlayer!=null && mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
    }

    public boolean isPlaying(){
        if(mediaPlayer==null) return false;
        return mediaPlayer.isPlaying();
    }

    public int obtenerUltimoLibro(){
        SharedPreferences pref = getApplicationContext().getSharedPreferences("com.example.audiolibros_internal", MODE_PRIVATE);
        return pref.getInt("ultimo",-1);
    }

}
