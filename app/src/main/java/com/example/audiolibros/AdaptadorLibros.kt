package com.example.audiolibros

import android.content.Context
import android.graphics.Bitmap
import android.support.v7.graphics.Palette
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.android.volley.VolleyError
import com.android.volley.toolbox.ImageLoader
import kotlinx.android.synthetic.main.elemento_selector.view.*

/**
 * Created by vicch on 26/01/2018.
 */

open class AdaptadorLibros(private val contexto: Context, var listaLibros: MutableList<Libro> //Lista de libros a visualizar
) : RecyclerView.Adapter<AdaptadorLibros.ViewHolder>() {

    private val inflador: LayoutInflater //Crea Layouts a partir del XML

    private var onClickListener: View.OnClickListener? = null
    private var onLongClickListener: View.OnLongClickListener? = null

    init {
        inflador = contexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    fun setOnItemClickListener(onClickListener: View.OnClickListener) {
        this.onClickListener = onClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflamos la vista desde el xml
        val v = inflador.inflate(R.layout.elemento_selector, null)
        v.setOnClickListener(onClickListener)
        v.setOnLongClickListener(onLongClickListener)
        return ViewHolder(v)
    }

    // Usando como base el ViewHolder y lo personalizamos
    override fun onBindViewHolder(holder: ViewHolder, posicion: Int) {
        val libro = listaLibros[posicion]
        holder.titulo.text = libro.titulo
        holder.itemView.rotation = 0f

        Aplicacion.lectorImagenes?.get(libro.urlImagen, object : ImageLoader.ImageListener {
            override fun onResponse(response: ImageLoader.ImageContainer, isImmediate: Boolean) {
                val bitmap = response.bitmap
                if (bitmap != null) {
                    holder.portada.setImageBitmap(bitmap)
                    if (libro.colorVibrate != -1 && libro.colorMute != -1) {
                        holder.itemView.setBackgroundColor(libro.colorMute)
                        holder.titulo.setBackgroundColor(libro.colorVibrate)
                        holder.portada.invalidate()
                    } else {
                        Palette.from(bitmap).generate { palette ->
                            libro.colorMute = palette.getLightMutedColor(0)
                            libro.colorVibrate = palette.getLightVibrantColor(1)
                            holder.itemView.setBackgroundColor(libro.colorMute)
                            holder.titulo.setBackgroundColor(libro.colorVibrate)
                        }
                    }
                }
            }

            override fun onErrorResponse(error: VolleyError) {
                holder.portada.setImageResource(R.drawable.books)
            }
        })
    }

    // Indicamos el número de elementos de la lista
    override fun getItemCount(): Int {
        return listaLibros.size
    }

    fun setOnItemLongClickListener(onLongClickListener: View.OnLongClickListener) {
        this.onLongClickListener = onLongClickListener
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var portada: ImageView
        var titulo: TextView

        init {
            portada = itemView.portada
            titulo = itemView.titulo
        }

    }

}
