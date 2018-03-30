package com.example.audiolibros;

import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.example.audiolibros.fragments.DetalleFragment;
import com.example.audiolibros.fragments.PreferenciasFragment;
import com.example.audiolibros.fragments.SelectorFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private AdaptadorLibrosFiltro adaptador;

    private AppBarLayout appBarLayout;
    private TabLayout tabs;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Navigation Drawer
        drawer = (DrawerLayout) findViewById( R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.drawer_open, R.string. drawer_close);
        toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById( R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irUltimoVisitado();
            }
        });

        /*
        if((findViewById(R.id.contenedor_pequeno) != null) && (getFragmentManager().findFragmentById(R.id.contenedor_pequeno) == null)){
            SelectorFragment primerFragment = new SelectorFragment();
            getFragmentManager().beginTransaction().add(R.id.contenedor_pequeno, primerFragment).commit();
        }
        */

        int idContenedor = (findViewById(R.id.contenedor_pequeno) != null) ? R.id.contenedor_pequeno : R.id.contenedor_izquierdo;
        SelectorFragment primerFragment = new SelectorFragment();
        getFragmentManager().beginTransaction().add(idContenedor, primerFragment).commit();

        Aplicacion app = (Aplicacion) getApplication();

        adaptador = ((Aplicacion) getApplicationContext()).getAdaptador();

        //Pestañas
        tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.addTab(tabs.newTab().setText("Todos"));
        tabs.addTab(tabs.newTab().setText("Nuevos"));
        tabs.addTab(tabs.newTab().setText("Leidos"));

        tabs.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: //Todos
                        adaptador.setNovedad(false);
                        adaptador.setLeido(false);
                        break;
                    case 1: //Nuevos
                        adaptador.setNovedad(true);
                        adaptador.setLeido(false);
                        break;
                    case 2: //Leidos
                        adaptador.setNovedad(false);
                        adaptador.setLeido(true);
                        break;
                }
                adaptador.notifyDataSetChanged();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        appBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);

        /*
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setAdapter(app.getAdaptador());
        //Arrastre Horizontal layoutManager = new GridLayoutManager(this, 2, LinearLayoutManager.HORIZONTAL, false);
        layoutManager = new GridLayoutManager(this,2);
        recyclerView.setLayoutManager(layoutManager);

        app.getAdaptador().setOnItemClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Toast.makeText(MainActivity.this,"Sleccionado elemeto: "+recyclerView.getChildAdapterPosition(v),Toast.LENGTH_SHORT).show();
            }
        });
        */


    }

    @Override
    protected void onResume() {
        Aplicacion app = (Aplicacion) getApplication();
        app.stop();
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_preferencias) {
            //Intent i = new Intent(this, PreferenciasActivity.class);
            // startActivity(i);
            abrePreferencias();
            return true;
        }
        else if (id == R.id.menu_acerca) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Mensaje de Acerca De");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.create().show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_todos) {
            adaptador.setGenero("");
            adaptador.notifyDataSetChanged();
        }
        else if (id == R.id.nav_epico) {
            adaptador.setGenero(Libro.G_EPICO);
            adaptador.notifyDataSetChanged();
        }
        else if (id == R.id.nav_XIX) {
            adaptador.setGenero(Libro.G_S_XIX);
            adaptador.notifyDataSetChanged();
        }
        else if (id == R.id.nav_suspense) {
            adaptador.setGenero(Libro.G_SUSPENSE);
            adaptador.notifyDataSetChanged();
        }
        else if (id == R.id.nav_preferencias){
            /*
            Intent i = new Intent(this, PreferenciasActivity.class);
            startActivity(i);
            */
            abrePreferencias();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById( R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById( R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    public void mostrarDetalle(int id) {
        DetalleFragment detalleFragment = (DetalleFragment) getFragmentManager().findFragmentById(R.id.detalle_fragment);
        if (detalleFragment != null) {
            detalleFragment.ponInfoLibro(id);
        } else {
            DetalleFragment nuevoFragment = new DetalleFragment();
            Bundle args = new Bundle();
            args.putInt(DetalleFragment.ARG_ID_LIBRO, id);
            nuevoFragment.setArguments(args);
            FragmentTransaction transaccion = getFragmentManager().beginTransaction();
            transaccion.replace(R.id.contenedor_pequeno, nuevoFragment);
            transaccion.addToBackStack(null);
            transaccion.commit();
        }

        Libro libro = ((Aplicacion) getApplication()).getListaLibros().get(id);

        SharedPreferences pref = getSharedPreferences("com.example.audiolibros_internal", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("ultimo", id);
        editor.putString("titulo", libro.titulo);
        editor.putString("autor", libro.autor);

        editor.commit();
    }

    public void irUltimoVisitado() {
        SharedPreferences pref = getSharedPreferences("com.example.audiolibros_internal", MODE_PRIVATE);
        int id = pref.getInt("ultimo", -1);
        if (id >= 0) {
            mostrarDetalle(id);
        } else {
            Toast.makeText(this,"Sin última vista",Toast.LENGTH_LONG).show();
        }
    }

    public void mostrarElementos(boolean mostrar) {
        appBarLayout.setExpanded(mostrar);
        toggle.setDrawerIndicatorEnabled(mostrar);
        if (mostrar) {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            tabs.setVisibility(View.VISIBLE);
        }
        else {
            tabs.setVisibility(View.GONE);
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }

    public void abrePreferencias() {
        //int idContenedor = (findViewById(R.id.contenedor_pequeno) != null) ? R.id.contenedor_pequeno : R.id.contenedor_izquierdo;
        if(findViewById(R.id.contenedor_pequeno)!=null){
            Intent i = new Intent(this, PreferenciasActivity.class);
            startActivity(i);
        }
        else {
            int idContenedor = R.id.contenedor_izquierdo;
            PreferenciasFragment prefFragment = new PreferenciasFragment();
            getFragmentManager().beginTransaction()
                    .replace(idContenedor, prefFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }



}
