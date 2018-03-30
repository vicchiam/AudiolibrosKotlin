package com.example.audiolibros.fragments;

import android.app.Fragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.example.audiolibros.Aplicacion;
import com.example.audiolibros.Libro;
import com.example.audiolibros.MainActivity;
import com.example.audiolibros.R;
import com.example.audiolibros.zoomSeekBar.OnValListener;
import com.example.audiolibros.zoomSeekBar.ZoomSeekBar;

import java.io.IOException;

/**
 * Created by vicch on 26/01/2018.
 */

public class DetalleFragment extends Fragment implements View.OnTouchListener, MediaPlayer.OnPreparedListener, MediaController.MediaPlayerControl, OnValListener {

    public static String ARG_ID_LIBRO = "id_libro";
    MediaPlayer mediaPlayer;
    MediaController mediaController;

    ZoomSeekBar zoomSeekBar;
    private Handler manejador;

    //Notificaciones
    private static final int ID_NOTIFICACION=1;
    private NotificationManager notificManager;
    private NotificationCompat.Builder notificacion;
    private RemoteViews remoteViews;

    @Override
    public View onCreateView(LayoutInflater inflador, ViewGroup contenedor, Bundle savedInstanceState) {
        View vista = inflador.inflate(R.layout.fragment_detalle, contenedor, false);
        Bundle args = getArguments();
        if (args != null) {
            int position = args.getInt(ARG_ID_LIBRO);
            ponInfoLibro(position, vista);
        } else {
            ponInfoLibro(0, vista);
        }

        zoomSeekBar = (ZoomSeekBar) vista.findViewById(R.id.zoomSeekBar);
        zoomSeekBar.setOnValListener(this);
        manejador=new Handler();

        return vista;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.d("Audiolibros", "Entramos en onPrepared de MediaPlayer");

        SharedPreferences preferencias = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (preferencias.getBoolean("pref_autoreproducir", true)) {
            this.start();
        }

        mediaController.setMediaPlayer(this);
        mediaController.setAnchorView(getView().findViewById(R.id.fragment_detalle));
        mediaController.setEnabled(true);
        mediaController.show();


        int duration=mediaPlayer.getDuration()/1000; //En segundos


        Log.d("Duration", duration+"");

        zoomSeekBar.setValMin(0);
        zoomSeekBar.setValMax(duration);
        zoomSeekBar.setEscalaMin(0);
        zoomSeekBar.setEscalaMax(duration);
        zoomSeekBar.setEscalaIni(0);
        zoomSeekBar.setEscalaRaya(duration/20);
        zoomSeekBar.setEscalaRayaLarga(5);

    }

    @Override
    public boolean onTouch(View vista, MotionEvent evento) {
        mediaController.show();
        return false;
    }

    @Override
    public void onStop() {
        mediaController.hide();
        try {
            mediaPlayer.stop();
            mediaPlayer.release();
        } catch (Exception e) {
            Log.d("Audiolibros", "Error en mediaPlayer.stop()");
        }
        super.onStop();
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        try {
            return mediaPlayer.getCurrentPosition();
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    @Override
    public void pause() {
        mediaPlayer.pause();
    }

    @Override
    public void seekTo(int pos) {
        mediaPlayer.seekTo(pos);
    }

    @Override
    public void start() {
        mediaPlayer.start();
        updateProgress();
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    @Override public void onResume(){
        DetalleFragment detalleFragment = (DetalleFragment) getFragmentManager().findFragmentById(R.id.detalle_fragment);
        if (detalleFragment == null ) {
            ((MainActivity) getActivity()).mostrarElementos(false);
        }
        super.onResume();
    }

    private void ponInfoLibro(int id, View vista) {
        final Libro libro = ((Aplicacion) getActivity().getApplication()).getListaLibros().get(id);
        ((TextView) vista.findViewById(R.id.titulo)).setText(libro.titulo);
        ((TextView) vista.findViewById(R.id.autor)).setText(libro.autor);

        final Aplicacion aplicacion = (Aplicacion) getActivity().getApplication();
        //((NetworkImageView) vista.findViewById(R.id.portada)).setImageUrl( libro.urlImagen,aplicacion.getLectorImagenes());

        RequestQueue colaPeticiones = Volley.newRequestQueue(aplicacion);
        ((NetworkImageView) vista.findViewById(R.id.portada)).setImageUrl( libro.urlImagen, new ImageLoader(
                colaPeticiones,
                new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap> cache = new LruCache<>(10);

            @Override
            public Bitmap getBitmap(String url) {
                return cache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                crearNotificacion(libro,bitmap,aplicacion);
                cache.put(url,bitmap);
            }
        }));
        //Bitmap bitmap=((BitmapDrawable)((NetworkImageView) vista.findViewById(R.id.portada)).getDrawable()).getBitmap();

        vista.setOnTouchListener(this);
        if (mediaPlayer != null){
            mediaPlayer.release();
        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        mediaController = new MediaController(getActivity());
        Uri audio = Uri.parse(libro.urlAudio);
        try {
            mediaPlayer.setDataSource(getActivity(), audio);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Log.e("Audiolibros", "ERROR: No se puede reproducir "+audio,e);
        }

        //crearNotificacion(libro, bitmap, aplicacion);
    }

    public void ponInfoLibro(int id) {
        ponInfoLibro(id, getView());
    }

    @Override
    public void onChangeVal(int newVal) {
        this.seekTo((newVal*1000));
    }

    public void updateProgress(){
        if(manejador!=null){
            manejador.postDelayed(updateProgress,1000);
        }
    }

    private Runnable updateProgress = new Runnable(){
        public void run(){
            int pos=mediaPlayer.getCurrentPosition();
            Log.d("POS", pos+" - "+zoomSeekBar.getValMin()+"-"+zoomSeekBar.getValMax());
            zoomSeekBar.setValNoEvent((pos/1000));
            manejador.postDelayed(this,1000);
        }
    };

    @Override
    public void onPause() {
        manejador.removeCallbacks(updateProgress);
        super.onPause();
    }

    private void crearNotificacion(Libro libro, Bitmap bitmap, Aplicacion aplicacion){
        remoteViews = new RemoteViews(aplicacion.getPackageName(), R.layout.custom_notification);
       // remoteViews.setImageViewResource(R.id.imagen_notificacion, R.drawable.ic_book_white_24dp);
        remoteViews.setImageViewBitmap(R.id.imagen_notificacion, bitmap);
        remoteViews.setImageViewResource(R.id.accion_notificacion, R.drawable.ic_play_arrow_white_24dp);
        remoteViews.setTextViewText(R.id.titulo_notificacion, libro.titulo);
        remoteViews.setTextViewText(R.id.autor_notificacion, libro.autor);

        Intent intent = new Intent(aplicacion, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(aplicacion, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        notificacion = new NotificationCompat.Builder(aplicacion)
                .setContent(remoteViews)
                .setPriority(Notification.PRIORITY_MAX)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Custom Notification")
                .setContentIntent(pendingIntent);
        notificManager = (NotificationManager)aplicacion.getSystemService(Context.NOTIFICATION_SERVICE);
        notificManager.notify(ID_NOTIFICACION, notificacion.build());



    }

}
