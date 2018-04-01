package com.example.audiolibros.fragments

import android.animation.Animator
import android.animation.AnimatorInflater
import android.app.Activity
import android.app.AlertDialog
import android.app.Fragment
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast

import com.example.audiolibros.AdaptadorLibros
import com.example.audiolibros.AdaptadorLibrosFiltro
import com.example.audiolibros.Aplicacion
import com.example.audiolibros.Libro
import com.example.audiolibros.MainActivity
import com.example.audiolibros.R
import kotlinx.android.synthetic.main.fragment_selector.view.*
import org.jetbrains.anko.design.indefiniteSnackbar
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.design.snackbar


/**
 * Created by vicch on 26/01/2018.
 */

class SelectorFragment : Fragment(), Animation.AnimationListener, Animator.AnimatorListener {

    private lateinit var actividad: Activity
    private lateinit var adaptador: AdaptadorLibrosFiltro
    private lateinit var listaLibros: List<Libro>

    override fun onAttach(actividad: Activity) {
        super.onAttach(actividad)
        this.actividad = actividad
        val app = actividad.application as Aplicacion
        adaptador = app.adaptador
        listaLibros = app.listaLibros
    }

    override fun onCreateView(inflador: LayoutInflater, contenedor: ViewGroup?, savedInstanceState: Bundle?): View? {
        val vista = inflador.inflate(R.layout.fragment_selector, contenedor, false)
        vista.recycler_view.layoutManager = GridLayoutManager(actividad, 2)
        vista.recycler_view.adapter = adaptador
        adaptador.setOnItemClickListener(View.OnClickListener { v -> (actividad as MainActivity).mostrarDetalle(adaptador.getItemId(vista.recycler_view.getChildAdapterPosition(v)).toInt()) })

        adaptador.setOnItemLongClickListener(View.OnLongClickListener { v ->
            val id = vista.recycler_view.getChildAdapterPosition(v)
            val menu = AlertDialog.Builder(actividad)
            val opciones = arrayOf<CharSequence>("Compartir", "Borrar", "Insertar")
            menu.setItems(opciones) { dialogInterface, opcion ->
                when (opcion) {
                    0 -> {
                        val anim = AnimatorInflater.loadAnimator(actividad, R.animator.shake)
                        anim.addListener(this@SelectorFragment)
                        anim.setTarget(v)
                        anim.start()
                        val libro = listaLibros[id]
                        val i = Intent(Intent.ACTION_SEND)
                        i.type = "text/plain"
                        i.putExtra(Intent.EXTRA_SUBJECT, libro.titulo)
                        i.putExtra(Intent.EXTRA_TEXT, libro.urlAudio)
                        startActivity(Intent.createChooser(i, "Compartir"))
                    }
                    1 -> longSnackbar(v,"¿Estas seguro?","Si"){
                            val anim = AnimationUtils.loadAnimation(actividad, R.anim.menguar)
                            anim.setAnimationListener(this@SelectorFragment)
                            v.startAnimation(anim)
                            adaptador.borrar(id)
                        }
                    2 -> {
                        val posicion = vista.recycler_view.getChildLayoutPosition(v)
                        adaptador.insertar(adaptador.getItem(posicion))
                        adaptador.notifyItemInserted(0)
                        indefiniteSnackbar(v,"Libro insetado","Ok"){}
                    }
                }
            }
            menu.create().show()
            true
        })

        val animator = DefaultItemAnimator()
        animator.addDuration = 500
        animator.moveDuration = 500
        vista.recycler_view.itemAnimator = animator

        setHasOptionsMenu(true)
        return vista
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_selector, menu)

        val searchItem = menu.findItem(R.id.menu_buscar)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(query: String): Boolean {
                adaptador.setBusqueda(query)
                adaptador.notifyDataSetChanged()
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }
        })

        MenuItemCompat.setOnActionExpandListener(searchItem, object : MenuItemCompat.OnActionExpandListener {
            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                adaptador.setBusqueda("")
                adaptador.notifyDataSetChanged()
                return true // Para permitir cierre
            }

            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                return true // Para permitir expansión
            }
        })

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.menu_ultimo) {
            (actividad as MainActivity).irUltimoVisitado()
            return true
        } else if (id == R.id.menu_buscar) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        (activity as MainActivity).mostrarElementos(true)
        super.onResume()
    }

    override fun onAnimationStart(animation: Animation) {

    }

    override fun onAnimationEnd(animation: Animation) {
        adaptador.notifyDataSetChanged()
    }

    override fun onAnimationRepeat(animation: Animation) {

    }

    override fun onAnimationStart(animator: Animator) {

    }

    override fun onAnimationEnd(animator: Animator) {
        adaptador.notifyDataSetChanged()
    }

    override fun onAnimationCancel(animator: Animator) {

    }

    override fun onAnimationRepeat(animator: Animator) {

    }
}
