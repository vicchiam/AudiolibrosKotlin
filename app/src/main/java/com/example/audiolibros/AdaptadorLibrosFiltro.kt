package com.example.audiolibros

import android.content.Context

import java.util.ArrayList

/**
 * Created by vicch on 27/01/2018.
 */

class AdaptadorLibrosFiltro : AdaptadorLibros {

    private var listaSinFiltro: MutableList<Libro>
    private lateinit var indiceFiltro: MutableList<Int>// Índice en listaSinFiltro de
    // Cada elemento de listaLibros
    private var busqueda = "" // Búsqueda sobre autor o título
    private var genero = "" // Género seleccionado
    private var novedad = false // Si queremos ver solo novedades
    private var leido = false

    constructor(contexto: Context, listaLibros: MutableList<Libro>) : super(contexto,listaLibros){
        listaSinFiltro=listaLibros.toMutableList()
        recalculaFiltro()
    }


    fun setBusqueda(busqueda: String) {
        this.busqueda = busqueda.toLowerCase()
        recalculaFiltro()
    }

    fun setGenero(genero: String) {
        this.genero = genero
        recalculaFiltro()
    }

    fun setNovedad(novedad: Boolean) {
        this.novedad = novedad
        recalculaFiltro()
    }

    fun setLeido(leido: Boolean) {
        this.leido = leido
        recalculaFiltro()
    }

    fun recalculaFiltro() {
        listaLibros = ArrayList<Libro>()
        indiceFiltro = mutableListOf()
        for (i in requireNotNull(listaSinFiltro).indices) {
            val libro = requireNotNull(listaSinFiltro)[i]
            if ((libro.titulo.toLowerCase().contains(busqueda) || libro.autor.toLowerCase().contains(busqueda))
                    && libro.genero.startsWith(genero)
                    && (!novedad || novedad && libro.novedad)
                    && (!leido || leido && libro.leido)) {
                listaLibros.add(libro)
                indiceFiltro.add(i)
            }
        }
    }

    fun getItem(posicion: Int): Libro {
        return listaSinFiltro[indiceFiltro[posicion]]
    }

    override fun getItemId(posicion: Int): Long {
        return indiceFiltro[posicion].toLong()
    }

    fun borrar(posicion: Int) {
        listaSinFiltro.removeAt(getItemId(posicion).toInt())
        recalculaFiltro()
    }

    fun insertar(libro: Libro) {
        listaSinFiltro.add(0, libro)
        recalculaFiltro()
    }
}
