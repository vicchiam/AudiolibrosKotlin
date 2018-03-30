package com.example.audiolibros;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vicch on 27/01/2018.
 */

public class AdaptadorLibrosFiltro extends AdaptadorLibros {

    private List<Libro> listaSinFiltro; // Lista con todos los libros
    private List<Integer> indiceFiltro; // Índice en listaSinFiltro de
    // Cada elemento de listaLibros
    private String busqueda = ""; // Búsqueda sobre autor o título
    private String genero = ""; // Género seleccionado
    private boolean novedad = false; // Si queremos ver solo novedades
    private boolean leido = false;


    public AdaptadorLibrosFiltro(Context contexto, List<Libro> listaLibros){
        super(contexto, listaLibros);
        listaSinFiltro = listaLibros;
        recalculaFiltro();
    }
    public void setBusqueda(String busqueda) {
        this.busqueda = busqueda.toLowerCase();
        recalculaFiltro();
    }
    public void setGenero(String genero) {
        this.genero = genero;
        recalculaFiltro();
    }
    public void setNovedad(boolean novedad) {
        this.novedad = novedad;
        recalculaFiltro();
    }
    public void setLeido(boolean leido) {
        this.leido = leido;
        recalculaFiltro();
    }

    public void recalculaFiltro() {
        listaLibros = new ArrayList<Libro>();
        indiceFiltro = new ArrayList<Integer>();
        for (int i = 0; i < listaSinFiltro.size(); i++) {
            Libro libro = listaSinFiltro.get(i);
            if ((libro.getTitulo().toLowerCase().contains(busqueda) || libro.getAutor().toLowerCase().contains(busqueda))
                    && (libro.getGenero().startsWith(genero))
                    && (!novedad || (novedad && libro.getNovedad()))
                    && (!leido || (leido && libro.getLeido()))) {
                listaLibros.add(libro);
                indiceFiltro.add(i);
            }
        }
    }
    public Libro getItem(int posicion) { return listaSinFiltro.get(indiceFiltro.get(posicion)); }

    public long getItemId(int posicion) {
        return indiceFiltro.get(posicion);
    }

    public void borrar(int posicion){
        listaSinFiltro.remove((int)getItemId(posicion));
        recalculaFiltro();
    }
    public void insertar(Libro libro){
        listaSinFiltro.add(0,libro);
        recalculaFiltro();
    }
}
