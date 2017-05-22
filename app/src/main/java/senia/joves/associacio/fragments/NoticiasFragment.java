package senia.joves.associacio.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import senia.joves.associacio.LoginActivity;
import senia.joves.associacio.R;
import senia.joves.associacio.entidades.Noticia;

import static senia.joves.associacio.Static.Recursos.LISTA_NOTICIAS;

/**
 * Created by Ruben on 08/05/2017.
 */

public class NoticiasFragment extends Fragment {

    //referencia a la base de datos
    private DatabaseReference mDatabase;

    //referencia al modulo de Storage de Firebase
    StorageReference storageRef;

    //variable para el progress dialog
    private ProgressDialog mProgressDialog;

    //variable para los objetos que nos devuelven
    Noticia noticia = new Noticia();

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

    public NoticiasFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_noticias, container, false);

        //activamos la modificacion del appbar
        setHasOptionsMenu(true);

        Toolbar mToolbar = (Toolbar) rootView.findViewById(R.id.toolbarNoticias);
        if (mToolbar != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        }

        //añadimos la descripcion al toolbar
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getActivity().getResources().getString(R.string.titulo_noticias));

        //referencia a la base de datos
        mDatabase = FirebaseDatabase.getInstance().getReference().child("noticias");

        //devolvemos la vista inflada
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //recuperamos los nombres de las imagenes para poder recuperarlas de Storage Firebase
        recuperarNombresImagenes();


    }

    //metodo que se conecta a database firebase y se descarga el nombre de las imagenes
    private void recuperarNombresImagenes(){

        mDatabase.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // recogemos los datos
                        obtenerSocios(dataSnapshot);

                        //rellenamos la interfaz
//                        rellenarInterfaz();

                        //Escondemos el elemento de carga
                        esconderCarga();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        FirebaseCrash.log("Error al recuperar datos: " + databaseError.toException());
                    }
                });

    }

    //metodo que a partir de un dato Snapshot, rellenamos un arraylist con todos los socios
    private void obtenerSocios(DataSnapshot dataSnapshot) {

        for (DataSnapshot ds : dataSnapshot.getChildren()) {

            noticia = new Noticia();

            //obtenemos a partir del DataSnapshot cada dato, lo pasamos al objeto
            noticia.setNombre(ds.child("nombre").getValue().toString());

            //añadimos el objeto al arrray
            LISTA_NOTICIAS.add(noticia);

            System.out.println(noticia.getNombre());
        }
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