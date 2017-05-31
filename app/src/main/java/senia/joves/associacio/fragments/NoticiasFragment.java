package senia.joves.associacio.fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import com.google.firebase.storage.UploadTask;
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

import static senia.joves.associacio.Static.Recursos.LISTA_SOCIOS;
import static senia.joves.associacio.Static.Recursos.LISTA_IMAGENES;

/**
 * Created by Ruben on 08/05/2017.
 */

public class NoticiasFragment extends PadreFragment {

    //Listener que escucha si hay cambios en la BD de FIREBASE
    ValueEventListener postListener;

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

    //metodos para el menu del appbar
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

    //menu contextual de las imagenes
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
                        .setTitle(R.string.titulo_eliminar_imagen)
                        .setMessage(R.string.texto_eliminar_imagenes)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Obtenemos el id del elemento seleccionado
                                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

                                // Borramos ese registro
                                eliminarSocio(LISTA_IMAGENES.get(info.position).getId());
                            }
                        })
                        .show();


                // Indicamos que hemos manejado la opción del menú
                return true;
        }
        return super.onContextItemSelected(item);
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
        //comprobamos si el array esta lleno, para pedir los datos a internet o no
        if (LISTA_IMAGENES == null) {
            //mostramos una ventana de carga
            mostrarCarga();

            //recuperamos los nombres de las imagenes para poder recuperarlas de Storage Firebase
            recuperarNombresImagenes();
        } else {
            //rellenamos el interfaz
            rellenarInterfaz();
        }

    }

    //metodo que se conecta a database firebase y se descarga la url de las imagenes
    private void recuperarNombresImagenes() {

        postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // recogemos los datos
                obtenerNombresIMG(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                FirebaseCrash.log("Error al recuperar datos: " + databaseError.toException());
            }
        };

    }

    //metodo que a partir de un dato Snapshot, rellenamos un arraylist con todos las imagenes
    private void obtenerNombresIMG(DataSnapshot dataSnapshot) {

        //iniciamos la variable
        LISTA_IMAGENES = new ArrayList<>();

        LISTA_IMAGENES.clear();

        //rellenamos la lista de nombres, desde la BD REALTIME DB
        for (DataSnapshot ds : dataSnapshot.getChildren()) {

            noticia = new Noticia();

            //obtenemos a partir del DataSnapshot cada dato, lo pasamos al objeto
            noticia.setNombre(ds.child("nombre").getValue().toString());
            noticia.setId(ds.child("id").getValue().toString());

            //añadimos el objeto al arrray
            LISTA_IMAGENES.add(noticia);
        }

        //ordenamos la lista por nombre
        Collections.sort(LISTA_IMAGENES);

        //le damos la vuelta al array
        Collections.reverse(LISTA_IMAGENES);

        //rellenamos el interfaz
        rellenarInterfaz();

    }

    //metodo que a partir del array, rellenamos la interfaz
    private void rellenarInterfaz() {

        //escondemos el dialogo de carga
        esconderCarga();

        //capturamos la lista
        lstLista = (ListView) getActivity().findViewById(R.id.listaNoticias);

        //creamos un adaptador a partir del Array lleno y el contexto
        ad = new AdaptadorNoticias(getActivity());

        //pasamos el adapter a la lista
        lstLista.setAdapter(ad);

        //agregamos a la lista la posibilidad de un menu contextual
        registerForContextMenu(lstLista);

    }

    //metodo que abre un dialogo para abrir una imagen de la galeria o camara, y la almacena e inserta en in imageview
    private void abrirDialogoImagen() {

        //seteamos la ventana para elegir camara o galeria
        PickSetup setup = new PickSetup();
        setup.setTitle(getActivity().getResources().getString(R.string.titulo_dialogo));
        setup.setProgressText("");
        setup.setSystemDialog(true);

        PickImageDialog.build(setup)
                .setOnPickResult(new IPickResult() {
                    @Override
                    public void onPickResult(PickResult r) {

                        //mostramos una ventana de carga
                        mostrarCarga();

                        //almacenamos la imagen seleccionada en la nube (Storage de Firebase)
                        subirImagen(r);
                    }
                }).show(getFragmentManager());

    }

    //metodo que coge la imagen seleccionada por el usuario, la sube a la nube (Storage Firebase),
    //descarga la url de esa imagen, y la insertamos en una bd de Realtime DB
    private void subirImagen(PickResult pick) {

        //referencia al almacenamiento de imagenes de perfil
        storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://associaciojoves-70d35.appspot.com/noticias/" + generarToken(12) + ".png");

        //si hemos elegido imagen la subimos al Storage
        storageRef.putFile(pick.getUri())
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //una vez subida, obtenemos la url para añadirla al objeto socio
                        @SuppressWarnings("VisibleForTests")
                        String downloadUrl = taskSnapshot.getDownloadUrl().toString();

                        //insertamos la url en Realtime DB (firebase)
                        insertarURL(downloadUrl);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        FirebaseCrash.log("Error al subir la imagen: " + exception.toString());
                        Toast.makeText(getActivity(), getResources().getString(R.string.error_añadir), Toast.LENGTH_LONG).show();

                    }
                });

    }

    //metodo que a partir de una url, la inserta en la bd junto a un id
    private void insertarURL(String url) {
        //pasamos los datos al objeto noticia e insertamos en Realtime DB
        Noticia n = new Noticia();

        //creamos una noticia para subir a partir de los datos
        n.setNombre(url);

        //obtenemos el ultimo id, y le sumamos uno para insertarlo en la bd
        int idfinal = Integer.parseInt(LISTA_IMAGENES.get(0).getId()) + 1;

        n.setId(idfinal + "");

        //añadimos una url en la base de datos
        mDatabase.child("img" + n.getId()).setValue(n);

        //mostramos un mensaje de éxito
        Toast.makeText(getActivity(), getResources().getString(R.string.exito_subir), Toast.LENGTH_LONG).show();
    }

    //metodo que elimina un registro a partir de su nombre
    private void eliminarSocio(String id) {

        //mostramos un dialogo de carga
        mostrarCarga();

        //añadimos un nuevo usuario
        mDatabase.child("img" + id).removeValue();

        //mostramos un mensaje de éxito
        Toast.makeText(getActivity(), getResources().getString(R.string.exito_eliminar_imagen), Toast.LENGTH_LONG).show();

    }

    //al empezar la actividad
    @Override
    public void onStart() {
        super.onStart();
        try {
            mDatabase.addValueEventListener(postListener);
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

    //metodo que genera un token aleatorio para el nombre de la imagen
    public static String generarToken(int count) {
        final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
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