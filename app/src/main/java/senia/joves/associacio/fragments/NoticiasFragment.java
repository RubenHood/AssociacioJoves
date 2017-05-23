package senia.joves.associacio.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

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
import com.squareup.picasso.Picasso;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import java.util.ArrayList;
import java.util.Collections;

import senia.joves.associacio.LoginActivity;
import senia.joves.associacio.R;
import senia.joves.associacio.adaptadores.AdaptadorNoticias;
import senia.joves.associacio.entidades.Noticia;
import senia.joves.associacio.librerias.ImagenCircular;

import static senia.joves.associacio.Static.Recursos.LISTA_NOMBRE_IMAGENES;
import static senia.joves.associacio.Static.Recursos.LISTA_URL_IMAGENES;

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

    //referencia a la lista en la vista
    ListView lstLista;

    //referencia al adaptador
    AdaptadorNoticias ad;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_noticias, menu);
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
            case R.id.anyadir:
                abrirDialogoImagen();
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

        //Ubicamos el storage donde se encuentras las imagenes
        storageRef = FirebaseStorage.getInstance().getReference().child("noticias");

        //devolvemos la vista inflada
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
            //comprobamos si el array esta lleno, para pedir los datos a internet o no
            if(LISTA_URL_IMAGENES == null){
                //mostramos una ventana de carga
                mostrarCarga();

                //recuperamos los nombres de las imagenes para poder recuperarlas de Storage Firebase
                recuperarNombresImagenes();
            }else{
                //rellenamos el interfaz
                rellenarInterfaz();
            }

    }

    //metodo que se conecta a database firebase y se descarga el nombre de las imagenes
    private void recuperarNombresImagenes(){

        mDatabase.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // recogemos los datos
                        obtenerNombresIMG(dataSnapshot);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        FirebaseCrash.log("Error al recuperar datos: " + databaseError.toException());
                    }
                });

    }

    //metodo que a partir de un dato Snapshot, rellenamos un arraylist con todos las imagenes
    private void obtenerNombresIMG(DataSnapshot dataSnapshot) {

        //iniciamos la variable
        LISTA_URL_IMAGENES = new ArrayList<>();

        LISTA_NOMBRE_IMAGENES.clear();
        LISTA_URL_IMAGENES.clear();

        for (DataSnapshot ds : dataSnapshot.getChildren()) {

            noticia = new Noticia();

            //obtenemos a partir del DataSnapshot cada dato, lo pasamos al objeto
            noticia.setNombre(ds.child("nombre").getValue().toString());

            //añadimos el objeto al arrray
            LISTA_NOMBRE_IMAGENES.add(noticia);
        }

        for (int i = 0; i < LISTA_NOMBRE_IMAGENES.size(); i++){

            storageRef.child(LISTA_NOMBRE_IMAGENES.get(i).getNombre()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {

                    Log.e("Nombre Imagen" ,uri.toString());
                    LISTA_URL_IMAGENES.add(new Noticia(uri.toString()));

                    if(LISTA_NOMBRE_IMAGENES.size() == LISTA_URL_IMAGENES.size()){

                        //ordenamos la lista por nombre
                        Collections.sort(LISTA_URL_IMAGENES);

                        //le damos la vuelta al array
                        Collections.reverse(LISTA_URL_IMAGENES);

                        rellenarInterfaz();
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    FirebaseCrash.log("ERROR IMAGENES:" + exception.toString());
                }
            });

        }

    }

    //metodo que a partir del array, rellenamos la interfaz
    private void rellenarInterfaz() {

        //escondemos la ventana de cargar
        esconderCarga();

        //capturamos la lista
        lstLista = (ListView) getActivity().findViewById(R.id.listaNoticias);

        //creamos un adaptador a partir del Array lleno y el contexto
        ad = new AdaptadorNoticias(getActivity());

        //pasamos el adapter a la lista
        lstLista.setAdapter(ad);

    }

    //metodo que abre un dialogo para abrir una imagen de la galeria o camara, y la almacena e inserta en in imageview
    private void abrirDialogoImagen() {

        //seteamos la ventana para elegir camara o galeria
        PickSetup setup = new PickSetup();
        setup.setTitle(getActivity().getResources().getString(R.string.titulo_dialogo));
        setup.setProgressText(getActivity().getResources().getString(R.string.texto_cargando));
        setup.setProgressTextColor(getActivity().getResources().getColor(R.color.colorAccent));
        setup.setSystemDialog(true);

        PickImageDialog.build(setup)
                .setOnPickResult(new IPickResult() {
                    @Override
                    public void onPickResult(PickResult r) {

//                        //almacenamos la imagen seleccionada
//                        imgSeleccionada = r.getUri();
//
//                        //seteamos la imagen en la cabececera
//                        Picasso.with(getActivity()).load(r.getUri()).transform(new ImagenCircular()).into(imgPerfil);
                    }
                }).show(getFragmentManager());

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