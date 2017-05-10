package senia.joves.associacio.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import senia.joves.associacio.LoginActivity;
import senia.joves.associacio.MainActivity;
import senia.joves.associacio.R;
import senia.joves.associacio.adaptadores.AdaptadorSocios;
import senia.joves.associacio.entidades.Socio;

/**
 * Created by Usuario on 08/05/2017.
 */

public class SociosFragment extends Fragment {

    //Datos compartidos
    //Array que almacena todos los socios
    public static ArrayList<Socio> LISTA_SOCIOS;
    public static int NUMERO_ULTIMO_SOCIO = 0;
    //Variable para el array que recibimos en el adaptador
    public static ArrayList<Socio> ARRAY_RECIBIDO = new ArrayList<>();
    //FIN DE DATOS COMPARTIDOS

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
        }

        return super.onOptionsItemSelected(item);
    }

    public SociosFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //activamos la modificacion del appbar
        setHasOptionsMenu(true);

        //añadimos la descripcion al toolbar
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getActivity().getResources().getString(R.string.titulo_socios));

        //referencia a la base de datos
        mDatabase = FirebaseDatabase.getInstance().getReference().child("socios");

        //iniciamos el array
        LISTA_SOCIOS = new ArrayList<>();

        //devolvemos la vista inflada
        return inflater.inflate(R.layout.fragment_socios, container, false);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //comprobamos si esta lleno el array, para consultar FIREBASE
        if (LISTA_SOCIOS.isEmpty() || LISTA_SOCIOS == null) {
            //mostramos un barra de progreso
            mostrarCarga();

            // Leemos de la RealTime Database Firebase
            consultaSocios();
        } else {
            //rellenamos el interfaz
            rellenarInterfaz();
        }

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

        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            //creamos un objeto socio
            Socio s = new Socio();

            //obtenemos a partir del DataSnapshot cada dato, lo pasamos al objeto
            s.setDireccion(ds.child("direccion").getValue().toString());
            s.setDni(ds.child("dni").getValue().toString());
            s.setEmail(ds.child("email").getValue().toString());
            s.setNombre(ds.child("nombre").getValue().toString());
            s.setPoblacion(ds.child("poblacion").getValue().toString());
            s.setQuota(ds.child("quota").getValue().toString());
            s.setSocio(ds.child("socio").getValue().toString());
            s.setTelefono(ds.child("telefono").getValue().toString());

            //metemos el objeto en el array
            LISTA_SOCIOS.add(s);
        }
        //almacenamos el numero del ultimo socio
//        NUMERO_ULTIMO_SOCIO = Integer.getInteger(LISTA_SOCIOS.get(LISTA_SOCIOS.size()).getSocio());
    }

    //metodo que a partir del array, rellenamos la interfaz
    private void rellenarInterfaz() {

        //capturamos la lista
        lstLista = (ListView) getActivity().findViewById(R.id.listaSocios);

        //creamos un adaptador a partir del Array lleno y el contexto
        ad = new AdaptadorSocios(getActivity(), LISTA_SOCIOS);

        //pasamos el adapter a la lista
        lstLista.setAdapter(ad);

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
            mDatabase.orderByChild("nombre").addValueEventListener(postListener);
        }catch (Exception e ){
            FirebaseCrash.log(e.getMessage());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //igualamos las variables
        ARRAY_RECIBIDO = LISTA_SOCIOS;
    }

    @Override
    public void onStop() {
        super.onStop();
        mDatabase.removeEventListener(postListener);
    }
}
