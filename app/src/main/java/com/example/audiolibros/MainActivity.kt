package com.example.audiolibros

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.design.widget.TabLayout
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast

import com.example.audiolibros.fragments.DetalleFragment
import com.example.audiolibros.fragments.PreferenciasFragment
import com.example.audiolibros.fragments.SelectorFragment

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.appcompat.v7.Appcompat
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var adaptador: AdaptadorLibrosFiltro

    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        // Navigation Drawer
        toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.drawer_open, R.string.drawer_close)
        toggle.toolbarNavigationClickListener = View.OnClickListener { onBackPressed() }
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        fab.onClick { irUltimoVisitado() }

        val idContenedor = if (findViewById<View>(R.id.contenedor_pequeno) != null) R.id.contenedor_pequeno else R.id.contenedor_izquierdo
        val primerFragment = SelectorFragment()
        fragmentManager.beginTransaction().add(idContenedor, primerFragment).commit()

        adaptador = (applicationContext as Aplicacion).adaptador

        //Pestañas
        tabs.addTab(tabs.newTab().setText("Todos"))
        tabs.addTab(tabs.newTab().setText("Nuevos"))
        tabs.addTab(tabs.newTab().setText("Leidos"))

        tabs.tabMode = TabLayout.MODE_SCROLLABLE
        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 //Todos
                    -> {
                        adaptador.setNovedad(false)
                        adaptador.setLeido(false)
                    }
                    1 //Nuevos
                    -> {
                        adaptador.setNovedad(true)
                        adaptador.setLeido(false)
                    }
                    2 //Leidos
                    -> {
                        adaptador.setNovedad(false)
                        adaptador.setLeido(true)
                    }
                }
                adaptador.notifyDataSetChanged()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })

    }

    override fun onResume() {
        val app = application as Aplicacion
        app.stop()
        super.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.menu_preferencias) {
            abrePreferencias()
            return true
        } else if (id == R.id.menu_acerca) {
            alert(Appcompat,"Mensaje de acerca De"){
                positiveButton(android.R.string.ok){}
            }.show()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id){
            R.id.nav_todos ->{
                adaptador.setGenero("")
                adaptador.notifyDataSetChanged()
            }
            R.id.nav_epico ->{
                adaptador.setGenero(G_EPICO)
                adaptador.notifyDataSetChanged()
            }
            R.id.nav_XIX ->{
                adaptador.setGenero(G_S_XIX)
                adaptador.notifyDataSetChanged()
            }
            R.id.nav_suspense ->{
                adaptador.setGenero(G_SUSPENSE)
                adaptador.notifyDataSetChanged()
            }
            R.id.nav_preferencias ->{
                abrePreferencias()
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    fun mostrarDetalle(id: Int) {
        val detalleFragment = fragmentManager.findFragmentById(R.id.detalle_fragment) as? DetalleFragment
        if (detalleFragment != null) {
            detalleFragment.ponInfoLibro(id)
        } else {
            val nuevoFragment = DetalleFragment()
            val args = Bundle()
            args.putInt(DetalleFragment.ARG_ID_LIBRO, id)
            nuevoFragment.arguments = args
            val transaccion = fragmentManager.beginTransaction()
            transaccion.replace(R.id.contenedor_pequeno, nuevoFragment)
            transaccion.addToBackStack(null)
            transaccion.commit()
        }

        val (titulo, autor) = (application as Aplicacion).listaLibros[id]

        val pref = getSharedPreferences("com.example.audiolibros_internal", Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putInt("ultimo", id)
        editor.putString("titulo", titulo)
        editor.putString("autor", autor)

        editor.commit()
    }

    fun irUltimoVisitado() {
        val pref = getSharedPreferences("com.example.audiolibros_internal", Context.MODE_PRIVATE)
        val id = pref.getInt("ultimo", -1)
        if (id >= 0) {
            mostrarDetalle(id)
        } else {
            Toast.makeText(this, "Sin última vista", Toast.LENGTH_LONG).show()
        }
    }

    fun mostrarElementos(mostrar: Boolean) {
        appBarLayout.setExpanded(mostrar)
        toggle.isDrawerIndicatorEnabled = mostrar
        if (mostrar) {
            drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            tabs.visibility = View.VISIBLE
        } else {
            tabs.visibility = View.GONE
            drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        }
    }

    fun abrePreferencias() {
        if (findViewById<View>(R.id.contenedor_pequeno) != null) {
            startActivity<PreferenciasActivity>()
        } else {
            val idContenedor = R.id.contenedor_izquierdo
            val prefFragment = PreferenciasFragment()
            fragmentManager.beginTransaction()
                    .replace(idContenedor, prefFragment)
                    .addToBackStack(null)
                    .commit()
        }
    }


}
