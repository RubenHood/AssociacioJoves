package senia.joves.associacio.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import java.io.File;
import java.util.regex.Pattern;

import senia.joves.associacio.LoginActivity;
import senia.joves.associacio.R;
import senia.joves.associacio.entidades.Socio;

import static senia.joves.associacio.Static.Recursos.NUMERO_ULTIMO_SOCIO;

/**
 * Created by Ruben on 08/05/2017.
 */

public class NewUserFragment extends Fragment {

    //variables para cargar las imagenes
    Uri imgSeleccionada = null;

    //referencia al modulo de Storage de Firebase
    StorageReference storageRef;

    //variable para el progress dialog
    private ProgressDialog mProgressDialog;

    //referencias a componentes de la vista
    private EditText txfNombre;
    private EditText txfDni;
    private EditText txfEmail;
    private EditText txfDireccion;
    private EditText txfPoblacion;
    private EditText txfTelefono;
    private Switch swSwitch;
    private FloatingActionButton fab;
    private FloatingActionButton fabImagenes;
    private ImageView imgPerfil;

    //referencia a la bd
    DatabaseReference ref;

    public NewUserFragment() {

    }

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
            case android.R.id.home:
                getFragmentManager().popBackStack();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_nuevo_socio, container, false);

        //referencia a la tabla de socios
        ref = FirebaseDatabase.getInstance().getReference("socios");

        //referencia al almacenamiento de imagenes de perfil
        storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://associaciojoves-70d35.appspot.com/socio_perfil/");

        //activamos la modificacion del appbar
        setHasOptionsMenu(true);

        Toolbar mToolbar = (Toolbar) rootView.findViewById(R.id.toolbarNuevo);
        if (mToolbar != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        }

        //añadimos el boton de ir atras
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(" ");
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //capturamos todos los elementos del formulario
        txfNombre = (EditText) getActivity().findViewById(R.id.txfNombre);
        txfDni = (EditText) getActivity().findViewById(R.id.txfDni);
        txfEmail = (EditText) getActivity().findViewById(R.id.txfEmail);
        txfDireccion = (EditText) getActivity().findViewById(R.id.txfDireccion);
        txfPoblacion = (EditText) getActivity().findViewById(R.id.txfPoblacion);
        txfTelefono = (EditText) getActivity().findViewById(R.id.txfTelefono);
        swSwitch = (Switch) getActivity().findViewById(R.id.swPagado);
        imgPerfil = (ImageView) getActivity().findViewById(R.id.imgNuevoSocio);
        fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        fabImagenes = (FloatingActionButton) getActivity().findViewById(R.id.fabFoto);

        //modificamos el tamaño del switch
        swSwitch.setSwitchMinWidth(200);
        swSwitch.setSwitchPadding(20);

        //listener para el boton de añadir imagenes
        fabImagenes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //abrimos un dialogo
                abrirDialogoImagen();

            }
        });


        //listener para cuando clicamos en el boton flotante añadimos un socio nuevo en firebase
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //validamos e insertamos los datos
                comprobarInsertarDatos();

            }
        });
    }

    //metodo que coge los datos, los valida, coge la imagen seteada (si la hay), i lo sube en Firebase
    //Storage, luego insertamos los datos en la bd Database Realtime Firebase
    private void comprobarInsertarDatos() {
        //obtenemos los valores escritos por el usuario
        final String nombre = txfNombre.getText().toString();
        final String dni = txfDni.getText().toString();
        final String email = txfEmail.getText().toString();
        final String direccion = txfDireccion.getText().toString();
        final String poblacion = txfPoblacion.getText().toString();
        final String telefono = txfTelefono.getText().toString();
        final String quota;

        //comprobamos si el socio ha pagado, para checkar o no el switch
        //comprobamos que eleccion hay en el switch //si es true HA PAGADO si es false no ha pagado
        if (swSwitch.isChecked()) {
            quota = "PAGADO";
        } else {
            quota = "";
        }

        try {
            //validamos los campos
//            if (validar(nombre, dni, email, direccion, poblacion, telefono)) {

            //escondemos el teclado virtual
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            View focus = getActivity().getCurrentFocus();
            if (focus != null)
                inputMethodManager.hideSoftInputFromWindow(focus.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

            //Añadimos un socio al contador
            NUMERO_ULTIMO_SOCIO++;

            //mostramos un dialogo para preguntar si estamos seguro de añadir el registro
            new AlertDialog.Builder(getActivity())
                    .setIcon(R.drawable.ic_socios)
                    .setTitle(R.string.titulo_añadir)
                    .setMessage(R.string.texto_añadir)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            //mostramos un dialogo de carga
                            mostrarCarga();

                            //comprobamos si hemos elegido una imagen de perfil
                            if (imgSeleccionada == null) {

                                Socio s = new Socio();

                                //creamos un socio a partir de los datos
                                s.setImagen("");
                                s.setDireccion(direccion);
                                s.setDni(dni);
                                s.setEmail(email);
                                s.setNombre(nombre);
                                s.setQuota(quota);
                                s.setTelefono(telefono);
                                s.setPoblacion(poblacion);
                                s.setSocio(String.valueOf(NUMERO_ULTIMO_SOCIO));

                                //añadimos un nuevo usuario
                                ref.child(nombre).setValue(s);

                                //escondemos el dialogo de carga
                                esconderCarga();

                                //mostramos un mensaje de éxito
                                Toast.makeText(getActivity(), getResources().getString(R.string.exito_añadir) + nombre, Toast.LENGTH_LONG).show();

                                //salimos del actual fragment
                                getFragmentManager().beginTransaction()
                                        .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right,
                                                R.anim.enter_from_right, R.anim.exit_to_left)
                                        .replace(R.id.contenido, new SociosFragment()).commit();

                            }else{

                                //si hemos elegido imagen la subimos al Storage
                                storageRef.putFile(imgSeleccionada)
                                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                //una vez subida, obtenemos la url para añadirla al objeto socio
                                                @SuppressWarnings("VisibleForTests")
                                                String downloadUrl = taskSnapshot.getDownloadUrl().toString();

                                                //pasamos los datos al objeto socio e insertamos en Realtime DB
                                                Socio s = new Socio();

                                                //creamos un socio a partir de los datos
                                                s.setImagen(downloadUrl);
                                                s.setDireccion(direccion);
                                                s.setDni(dni);
                                                s.setEmail(email);
                                                s.setNombre(nombre);
                                                s.setQuota(quota);
                                                s.setTelefono(telefono);
                                                s.setPoblacion(poblacion);
                                                s.setSocio(String.valueOf(NUMERO_ULTIMO_SOCIO));

                                                //añadimos un nuevo usuario
                                                ref.child(nombre).setValue(s);

                                                //escondemos el dialogo de carga
                                                esconderCarga();

                                                //mostramos un mensaje de éxito
                                                Toast.makeText(getActivity(), getResources().getString(R.string.exito_añadir) + nombre, Toast.LENGTH_LONG).show();

                                                //salimos del actual fragment
                                                getFragmentManager().beginTransaction()
                                                        .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right,
                                                                R.anim.enter_from_right, R.anim.exit_to_left)
                                                        .replace(R.id.contenido, new SociosFragment()).commit();


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
                        }
                    })
                    .show();
//            }
        } catch (Exception e) {
            Toast.makeText(getActivity(), getResources().getString(R.string.error_añadir), Toast.LENGTH_SHORT).show();
            FirebaseCrash.log(e.getMessage());
        }
    }

    //metodo que abre un dialogo para abrir una imagen de la galeria o camara, y la almacena e inserta en in imageview
    private void abrirDialogoImagen() {

        //seteamos la ventana para elegir camara o galeria
        PickSetup setup = new PickSetup();
        setup.setTitle(getActivity().getResources().getString(R.string.titulo_dialogo));
        setup.setProgressText(getActivity().getResources().getString(R.string.texto_cargando));
        setup.setSystemDialog(true);

        PickImageDialog.build(setup)
                .setOnPickResult(new IPickResult() {
                    @Override
                    public void onPickResult(PickResult r) {

                        //almacenamos la imagen seleccionada
                        imgSeleccionada = r.getUri();

                        //seteamos la imagen en la cabececera
                        imgPerfil.setImageBitmap(r.getBitmap());
                    }
                }).show(getFragmentManager());

    }

    @Override
    public void onDestroy() {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        super.onDestroy();
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

    //validar el texto
    private boolean validar(String nombre, String dni, String email, String direccion, String poblacion, String telefono) {
        if (!validarVacio(nombre)) {
            txfNombre.requestFocus();
            txfNombre.setError(getResources().getString(R.string.error_nombre));
            return false;
        }

        if (!validarDni(dni)) {
            txfDni.requestFocus();
            txfDni.setError(getResources().getString(R.string.error_dni));
            return false;
        }

        if (!validarEmail(email)) {
            txfEmail.requestFocus();
            txfEmail.setError(getResources().getString(R.string.error_email));
            return false;
        }

        if (!validarVacio(direccion)) {
            txfDireccion.requestFocus();
            txfDireccion.setError(getResources().getString(R.string.error_direccion));
            return false;
        }

        if (!validarVacio(poblacion)) {
            txfPoblacion.requestFocus();
            txfPoblacion.setError(getResources().getString(R.string.error_poblacion));
            return false;
        }

        if (!validartelefono(telefono)) {
            txfTelefono.requestFocus();
            txfTelefono.setError(getResources().getString(R.string.error_telefono));
            return false;

        }

        return true;
    }

    //Aqui los metodos que utiliza el metodo de arriba
    private boolean validarVacio(String nombre) {
        if (nombre.length() > 80 || nombre.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    private boolean validartelefono(String telefono) {
        Pattern patron = Pattern.compile("^(\\+34|0034|34)?[ -]*(6|7)[ -]*([0-9][ -]*){8}$");

        return patron.matcher(telefono).matches();
    }

    private boolean validarDni(String dni) {
        Pattern patron = Pattern.compile("^(\\d{8})([-]?)([A-Z]{1})$");

        return patron.matcher(dni).matches();
    }

    private boolean validarEmail(String correo) {
        Pattern patron = Pattern.compile("^[_a-z0-9-]+(.[_a-z0-9-]+)*@[a-z0-9-]+(.[a-z0-9-]+)*(.[a-z]{2,4})$");

        return patron.matcher(correo).matches();
    }


}
