package com.example.audiolibros;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import java.util.List;

/**
 * Created by vicch on 26/01/2018.
 */

public class AdaptadorLibros extends RecyclerView.Adapter<AdaptadorLibros.ViewHolder> {

    private LayoutInflater inflador; //Crea Layouts a partir del XML
    List<Libro> listaLibros; //Lista de libros a visualizar
    private Context contexto;

    private View.OnClickListener onClickListener;
    private View.OnLongClickListener onLongClickListener;

    public AdaptadorLibros(Context contexto, List<Libro> listaLibros) {
        inflador = (LayoutInflater) contexto .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.listaLibros = listaLibros;
        this.contexto = contexto;
    }

    public void setOnItemClickListener(View.OnClickListener onClickListener){
        this.onClickListener = onClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflamos la vista desde el xml
        View v = inflador.inflate(R.layout.elemento_selector, null);
        v.setOnClickListener(onClickListener);
        v.setOnLongClickListener(onLongClickListener);
        return new ViewHolder(v);
    }

    // Usando como base el ViewHolder y lo personalizamos
    @Override
    public void onBindViewHolder(final ViewHolder holder, int posicion) {
        final Libro libro = listaLibros.get(posicion);
        holder.titulo.setText(libro.getTitulo());
        holder.itemView.setRotation(0);

        Aplicacion aplicacion = (Aplicacion) contexto.getApplicationContext();
        Aplicacion.Companion.getLectorImagenes().get(libro.getUrlImagen(), new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                Bitmap bitmap = response.getBitmap();
                if(bitmap !=null){
                    holder.portada.setImageBitmap(bitmap);
                    if(libro.getColorVibrate() !=-1 && libro.getColorMute() !=-1) {
                        holder.itemView.setBackgroundColor(libro.getColorMute());
                        holder.titulo.setBackgroundColor(libro.getColorVibrate());
                        holder.portada.invalidate();
                    }
                    else{
                        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                            @Override
                            public void onGenerated(@NonNull Palette palette) {
                                libro.setColorMute(palette.getLightMutedColor(0));
                                libro.setColorVibrate(palette.getLightVibrantColor(1));
                                holder.itemView.setBackgroundColor(libro.getColorMute());
                                holder.titulo.setBackgroundColor(libro.getColorVibrate());
                            }
                        });
                    }
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                holder.portada.setImageResource(R.drawable.books);
            }
        });
    }

    // Indicamos el número de elementos de la lista
    @Override
    public int getItemCount() {
        return listaLibros.size();
    }

    public void setOnItemLongClickListener(View.OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView portada;
        public TextView titulo;

        public ViewHolder(View itemView){
            super(itemView);
            portada = (ImageView) itemView.findViewById(R.id.portada);
            titulo = (TextView) itemView.findViewById(R.id.titulo);
        }

    }

}
