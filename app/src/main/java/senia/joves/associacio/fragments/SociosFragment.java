package senia.joves.associacio.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import senia.joves.associacio.LoginActivity;
import senia.joves.associacio.R;
import senia.joves.associacio.adaptadores.AdaptadorSocios;
import senia.joves.associacio.entidades.Socio;

/**
 * Created by Usuario on 08/05/2017.
 */

public class SociosFragment extends Fragment {

    //variable para el progress dialog
    private ProgressDialog mProgressDialog;

    //referencia a la base de datos
    private DatabaseReference mDatabase;

    //Array que almacena todos los socios
    private ArrayList<Socio> LISTA_SOCIOS;

    //referencia a la lista en la vista
    ListView lstLista;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_principal, menu);
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

        //ordenamos por numero socio
        mDatabase.orderByChild("nombre");

        //iniciamos el array
        LISTA_SOCIOS = new ArrayList<>();

        //devolvemos la vista inflada
        return inflater.inflate(R.layout.fragment_socios, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //mostramos un barra de progreso
        mostrarCarga();

        // Leemos de la RealTime Database Firebase
        consultaSocios();
    }

    //metodo que consulta la bd y añade un listener para hacerla a tiempo real
    private void consultaSocios() {
        mDatabase.addValueEventListener(new ValueEventListener() {

            //metodo que se ejecuta una vez, y cada vez que los datos de la bd cambian
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //Escondemos el elemento de carga
                esconderCarga();

                // recogemos los datos
                obtenerSocios(dataSnapshot);

                rellenarInterfaz();

            }

            //metodo callback cuando falla la consulta
            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
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

    }

    //metodo que a partir del array, rellenamos la interfaz
    private void rellenarInterfaz() {

        //capturamos la lista
        lstLista = (ListView) getActivity().findViewById(R.id.listaSocios);

        //creamos un adaptador a partir del Array lleno y el contexto
        AdaptadorSocios ad = new AdaptadorSocios(getActivity(), LISTA_SOCIOS);

        //pasamos el adapter a la lista
        lstLista.setAdapter(ad);
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

}
