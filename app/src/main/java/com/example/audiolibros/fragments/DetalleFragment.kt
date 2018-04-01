package com.example.audiolibros.fragments

import android.app.Fragment
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import android.util.Log
import android.util.LruCache
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.MediaController
import android.widget.RemoteViews
import android.widget.TextView

import com.android.volley.RequestQueue
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.NetworkImageView
import com.android.volley.toolbox.Volley
import com.example.audiolibros.Aplicacion
import com.example.audiolibros.Libro
import com.example.audiolibros.MainActivity
import com.example.audiolibros.R
import com.example.audiolibros.zoomSeekBar.OnValListener
import com.example.audiolibros.zoomSeekBar.ZoomSeekBar
import kotlinx.android.synthetic.main.fragment_detalle.view.*

import java.io.IOException

/**
 * Created by vicch on 26/01/2018.
 */

class DetalleFragment : Fragment(), View.OnTouchListener, MediaPlayer.OnPreparedListener, MediaController.MediaPlayerControl, OnValListener {
    private var mediaPlayer: MediaPlayer? =null
    private var mediaController: MediaController? = null

    private lateinit var zoomSeekBar: ZoomSeekBar
    private lateinit var manejador: Handler
    private var notificManager: NotificationManager? = null
    private var notificacion: NotificationCompat.Builder? = null
    private var remoteViews: RemoteViews? = null

    private val updateProgress = object : Runnable {
        override fun run() {
            val pos = mediaPlayer?.currentPosition ?: 0
            Log.d("POS", pos.toString() + " - " + zoomSeekBar.valMin + "-" + zoomSeekBar.valMax)
            zoomSeekBar.setValNoEvent(pos / 1000)
            manejador?.postDelayed(this, 1000)
        }
    }

    override fun onCreateView(inflador: LayoutInflater, contenedor: ViewGroup?, savedInstanceState: Bundle?): View? {
        val vista = inflador.inflate(R.layout.fragment_detalle, contenedor, false)
        val args = arguments
        if (args != null) {
            val position = args.getInt(ARG_ID_LIBRO)
            ponInfoLibro(position, vista)
        } else {
            ponInfoLibro(0, vista)
        }

        zoomSeekBar = vista.zoomSeekBar
        zoomSeekBar.setOnValListener(this)
        manejador = Handler()

        return vista
    }

    override fun onPrepared(mediaPlayer: MediaPlayer) {
        Log.d("Audiolibros", "Entramos en onPrepared de MediaPlayer")

        val preferencias = PreferenceManager.getDefaultSharedPreferences(activity)
        if (preferencias.getBoolean("pref_autoreproducir", true)) {
            this.start()
        }

        mediaController?.setMediaPlayer(this)
        mediaController?.setAnchorView(view.fragment_detalle)
        mediaController?.isEnabled = true
        mediaController?.show()


        val duration = mediaPlayer.duration / 1000 //En segundos


        Log.d("Duration", duration.toString() + "")

        zoomSeekBar.valMin = 0
        zoomSeekBar.valMax = duration
        zoomSeekBar.escalaMin = 0
        zoomSeekBar.escalaMax = duration
        zoomSeekBar.escalaIni = 0
        zoomSeekBar.escalaRaya = duration / 20
        zoomSeekBar.escalaRayaLarga = 5

    }

    override fun onTouch(vista: View, evento: MotionEvent): Boolean {
        mediaController?.show()
        return false
    }

    override fun onStop() {
        mediaController?.hide()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.d("Audiolibros", "Error en mediaPlayer.stop()")
        }

        super.onStop()
    }

    override fun canPause(): Boolean {
        return true
    }

    override fun canSeekBackward(): Boolean {
        return true
    }

    override fun canSeekForward(): Boolean {
        return true
    }

    override fun getBufferPercentage(): Int {
        return 0
    }

    override fun getCurrentPosition(): Int {
        try {
            return mediaPlayer?.currentPosition ?: 0
        } catch (e: Exception) {
            return 0
        }

    }

    override fun getDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }

    override fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }

    override fun pause() {
        mediaPlayer?.pause()
    }

    override fun seekTo(pos: Int) {
        mediaPlayer?.seekTo(pos)
    }

    override fun start() {
        mediaPlayer?.start()
        updateProgress()
    }

    override fun getAudioSessionId(): Int {
        return 0
    }

    override fun onResume() {
        val detalleFragment = fragmentManager.findFragmentById(R.id.detalle_fragment) as DetalleFragment?
        if (detalleFragment == null) {
            (activity as MainActivity).mostrarElementos(false)
        }
        super.onResume()
    }

    private fun ponInfoLibro(id: Int, vista: View?) {
        val libro = (activity.application as Aplicacion).listaLibros[id]
        vista?.titulo?.text = libro.titulo
        vista?.autor?.text = libro.autor

        val aplicacion = activity.application as Aplicacion

        val colaPeticiones = Volley.newRequestQueue(aplicacion)
        ((vista?.portada) as NetworkImageView).setImageUrl(libro.urlImagen, ImageLoader(
                colaPeticiones,
                object : ImageLoader.ImageCache {
                    private val cache = LruCache<String, Bitmap>(10)

                    override fun getBitmap(url: String): Bitmap? {
                        return cache.get(url)
                    }

                    override fun putBitmap(url: String, bitmap: Bitmap) {
                        crearNotificacion(libro, bitmap, aplicacion)
                        cache.put(url, bitmap)
                    }
                }))

        vista.setOnTouchListener(this)
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer()
        mediaPlayer?.setOnPreparedListener(this)
        mediaController = MediaController(activity)
        val audio = Uri.parse(libro.urlAudio)
        try {
            mediaPlayer?.setDataSource(activity, audio)
            mediaPlayer?.prepareAsync()
        } catch (e: IOException) {
            Log.e("Audiolibros", "ERROR: No se puede reproducir " + audio, e)
        }

        //crearNotificacion(libro, bitmap, aplicacion);
    }

    fun ponInfoLibro(id: Int) {
        ponInfoLibro(id, view)
    }

    override fun onChangeVal(newVal: Int) {
        this.seekTo(newVal * 1000)
    }

    fun updateProgress() {
        manejador?.postDelayed(updateProgress, 1000)
    }

    override fun onPause() {
        manejador?.removeCallbacks(updateProgress)
        super.onPause()
    }

    private fun crearNotificacion(libro: Libro, bitmap: Bitmap, aplicacion: Aplicacion) {
        remoteViews = RemoteViews(aplicacion.packageName, R.layout.custom_notification)
        remoteViews?.setImageViewBitmap(R.id.imagen_notificacion, bitmap)
        remoteViews?.setImageViewResource(R.id.accion_notificacion, R.drawable.ic_play_arrow_white_24dp)
        remoteViews?.setTextViewText(R.id.titulo_notificacion, libro.titulo)
        remoteViews?.setTextViewText(R.id.autor_notificacion, libro.autor)

        val intent = Intent(aplicacion, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(aplicacion, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        notificacion = NotificationCompat.Builder(aplicacion)
                .setContent(remoteViews)
                .setPriority(Notification.PRIORITY_MAX)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Custom Notification")
                .setContentIntent(pendingIntent)
        notificManager = aplicacion.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificManager?.notify(ID_NOTIFICACION, notificacion?.build())


    }

    companion object {

        var ARG_ID_LIBRO = "id_libro"

        //Notificaciones
        private val ID_NOTIFICACION = 1
    }

}
