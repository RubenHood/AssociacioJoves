package senia.joves.associacio.fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import senia.joves.associacio.LoginActivity;
import senia.joves.associacio.MainActivity;
import senia.joves.associacio.R;
import senia.joves.associacio.adaptadores.AdaptadorSocios;
import senia.joves.associacio.entidades.Socio;

import static senia.joves.associacio.Static.Recursos.FILTRADO;
import static senia.joves.associacio.Static.Recursos.LISTA_SOCIOS;
import static senia.joves.associacio.Static.Recursos.NUMERO_ULTIMO_SOCIO;
import static senia.joves.associacio.Static.Recursos.ARRAY_RECIBIDO;

/**
 * Created by Usuario on 08/05/2017.
 */

public class SociosFragment extends PadreFragment {

    //variable para el progress dialog
    private ProgressDialog mProgressDialog;

    //referencia a la base de datos
    private DatabaseReference mDatabase;

    //Listener que escucha si hay cambios en la BD de FIREBASE
    ValueEventListener postListener;

    //referencia a la lista en la vista
    ListView lstLista;

    //referencia al adaptador
    AdaptadorSocios ad;

    //referencia al buscador
    SearchView searchView;


    //metodos para el menu de appbar
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_socios, menu);

        MenuItem item = menu.findItem(R.id.buscar);
        searchView = new SearchView(((MainActivity) getActivity()).getSupportActionBar().getThemedContext());
        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        MenuItemCompat.setActionView(item, searchView);

        //añadimos el listener para buscar
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String text = newText;
                ad.filter(text);
                return false;
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.logout:
                //deslogueamos al usuario
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getContext(), LoginActivity.class));
                break;
            case R.id.acercaDe:
                new AcercaDeFragment().show(getFragmentManager(), "AcercaDe");
                break;
            case R.id.filtrar:
                new DialogoFragmentFiltro().show(getFragmentManager(), "Filtrar");
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    //metodos del menu contextual
    // Creamos el menú contextual

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.menu_contextual, menu);
    }

    // El usuario hace clic en una opción del menú contextual del listado
    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        // Buscamos la opción del menú contextual seleccionada
        switch (item.getItemId()) {
            case R.id.opcEliminar:

                new AlertDialog.Builder(getActivity())
                        .setIcon(R.drawable.ic_action_delete_forever)
                        .setTitle(R.string.titulo_eliminar)
                        .setMessage(R.string.texto_eliminar2)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Obtenemos el id del elemento seleccionado
                                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

                                // Borramos ese registro
                                eliminarSocio(LISTA_SOCIOS.get(info.position).getNombre());
                            }
                        })
                        .show();


                // Indicamos que hemos manejado la opción del menú
                return true;
        }
        return super.onContextItemSelected(item);
    }

    public SociosFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_socios, container, false);

        //activamos la modificacion del appbar
        setHasOptionsMenu(true);

        Toolbar mToolbar = (Toolbar) rootView.findViewById(R.id.toolbarSocios);
        if (mToolbar != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        }

        //añadimos la descripcion al toolbar
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getActivity().getResources().getString(R.string.titulo_socios));

        //referencia a la base de datos
        mDatabase = FirebaseDatabase.getInstance().getReference().child("socios");

        //devolvemos la vista inflada
        return rootView;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //mostramos un barra de progreso
        mostrarCarga();

        // Leemos de la RealTime Database Firebase
        consultaSocios();

        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.anadirSocio);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left,
                                R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.contenido, new NewUserFragment())
                        .addToBackStack("nuevoSocioFragment").commit();
            }
        });

    }

    //metodo que consulta la bd y añade listeners para los cambios. para hacerla a tiempo real
    private void consultaSocios() {

        postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // recogemos los datos
                obtenerSocios(dataSnapshot);

                //rellenamos la interfaz
                rellenarInterfaz();

                //Escondemos el elemento de carga
                esconderCarga();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                FirebaseCrash.log("Error al recuperar datos: " + databaseError.toException());

            }
        };


    }

    //metodo que a partir de un dato Snapshot, rellenamos un arraylist con todos los socios
    private void obtenerSocios(DataSnapshot dataSnapshot) {

        LISTA_SOCIOS.clear();

        Socio s = null;

        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            //creamos un objeto socio
            s = new Socio();

            //obtenemos a partir del DataSnapshot cada dato, lo pasamos al objeto
            s.setDireccion(ds.child("direccion").getValue().toString());
            s.setDni(ds.child("dni").getValue().toString());
            s.setEmail(ds.child("email").getValue().toString());
            s.setNombre(ds.child("nombre").getValue().toString());
            s.setPoblacion(ds.child("poblacion").getValue().toString());
            s.setQuota(ds.child("quota").getValue().toString());
            s.setSocio(ds.child("socio").getValue().toString());
            s.setTelefono(ds.child("telefono").getValue().toString());
            s.setImagen(ds.child("imagen").getValue().toString());

            //comprobamos el numero de socio mas alto
            recogerMayorID(Integer.parseInt(s.getSocio()));

            //metemos el objeto en el array
            LISTA_SOCIOS.add(s);
        }


    }

    //metodo que comprueba si el numero de socio actual es mas alto que el almacenado, para guardarlo o no
    private void recogerMayorID(int numSocio) {

        if (numSocio > NUMERO_ULTIMO_SOCIO) {
            NUMERO_ULTIMO_SOCIO = numSocio;
        }

    }

    //metodo que a partir del array, rellenamos la interfaz
    private void rellenarInterfaz() {

        //capturamos la lista
        lstLista = (ListView) getActivity().findViewById(R.id.listaSocios);

        //Limpiamos el array que vamos a enviar al adaptador
        ARRAY_RECIBIDO.clear();

        //copiamos el array original, para mandarlo al adaptador
        ARRAY_RECIBIDO.addAll(LISTA_SOCIOS);

        //creamos un adaptador a partir del Array lleno y el contexto
        ad = new AdaptadorSocios(getActivity());

        //pasamos el adapter a la lista
        lstLista.setAdapter(ad);

        //agregamos a la lista la posibilidad de un menu contextual
        registerForContextMenu(lstLista);

        lstLista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //creamos un bundle
                Bundle b = new Bundle();

                //lo llenamos del socio a ver en detalle
                b.putSerializable("socio", ARRAY_RECIBIDO.get(position));

                //referencia al fragment de detalle
                DetalleFragment df = new DetalleFragment();

                //pasamos el bundle (con el socio) al fragment
                df.setArguments(b);

                //vamos a la actividad de detalle
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left,
                                R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.contenido, df)
                        .addToBackStack("detalleFragment").commit();

            }
        });
    }

    //metodo que elimina un registro a partir de su nombre
    private void eliminarSocio(String nombre) {

        //mostramos un dialogo de carga
        mostrarCarga();

        //añadimos un nuevo usuario
        mDatabase.child(nombre).removeValue();

        //mostramos un mensaje de éxito
        Toast.makeText(getActivity(), getResources().getString(R.string.exito_eliminar) + nombre, Toast.LENGTH_LONG).show();

    }

    //metodo que muestra un dialogo de carga
    private void mostrarCarga() {

        //creamos una barra de carga
        mProgressDialog = new ProgressDialog(getActivity());

        //mostramos un dialogo
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(getResources().getString(R.string.texto_cargando));
        mProgressDialog.show();
    }

    private void esconderCarga() {
        //escondemos el progres dialog
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            //activamos el listener a tiempo real
            mDatabase.orderByChild(FILTRADO).addValueEventListener(postListener);

        } catch (Exception e) {
            FirebaseCrash.log(e.getMessage());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (postListener != null) {
            mDatabase.removeEventListener(postListener);
        }

        //Escondemos el elemento de carga
        esconderCarga();

    }
}
