package senia.joves.associacio.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.squareup.picasso.Picasso;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import java.util.regex.Pattern;

import senia.joves.associacio.LoginActivity;
import senia.joves.associacio.R;
import senia.joves.associacio.entidades.Socio;
import senia.joves.associacio.fragments.error.SinConexionFragment;
import senia.joves.associacio.librerias.ImagenCircular;

/**
 * Created by Ruben on 08/05/2017.
 */

public class DetalleFragment extends Fragment {

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
    private EditText txfSocio;
    private Switch swSwitch;
    private FloatingActionButton fab;
    private ImageView imgPerfil;

    //referencia a la bd
    DatabaseReference ref;

    //variable que almacena el socio que llega
    private Socio socio;

    //variable para la imagen del toolbar
    ImageView qr_code;

    //constructor vacio necesario
    public DetalleFragment() {

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
            case R.id.acercaDe:
                new AcercaDeFragment().show(getFragmentManager(), "AcercaDe");
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //referencia a la bd donde actualizar socios
        ref = FirebaseDatabase.getInstance().getReference("socios");

        //instanciamos el objeto
        socio = new Socio();

        //recogemos el objeto pasado como argumento
        socio = (Socio) getArguments().getSerializable("socio");

        //activamos la modificacion del appbar
        setHasOptionsMenu(true);

        View rootView = inflater.inflate(R.layout.fragment_detalle, container, false);

        //comprobamos si la toolbar no es null, para setearla
        final Toolbar mToolbar = (Toolbar) rootView.findViewById(R.id.toolbarDetalle);
        if (mToolbar != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        }

        //Añadimos un QR al toolbar
        añadirQR(rootView);

        //ponemos el titulo de la actividad vacio
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(" ");

        //añadimos el boton de ir atras
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //capturamos todos los elementos del formulario
        txfNombre = (EditText) getActivity().findViewById(R.id.txfNombre_d);
        txfDni = (EditText) getActivity().findViewById(R.id.txfDni_d);
        txfEmail = (EditText) getActivity().findViewById(R.id.txfEmail_d);
        txfDireccion = (EditText) getActivity().findViewById(R.id.txfDireccion_d);
        txfPoblacion = (EditText) getActivity().findViewById(R.id.txfPoblacion_d);
        txfTelefono = (EditText) getActivity().findViewById(R.id.txfTelefono_d);
        txfSocio = (EditText) getActivity().findViewById(R.id.txfsocio_d);
        swSwitch = (Switch) getActivity().findViewById(R.id.swPagado_d);
        fab = (FloatingActionButton) getActivity().findViewById(R.id.fab_d);
        imgPerfil = (ImageView) getActivity().findViewById(R.id.fabFotoPerfil);

        //modificamos el tamaño del switch
        swSwitch.setSwitchMinWidth(200);
        swSwitch.setSwitchPadding(20);

        //mostramos los datos en sus textView
        txfNombre.setText(socio.getNombre());
        txfDireccion.setText(socio.getDireccion());
        txfDni.setText(socio.getDni());
        txfEmail.setText(socio.getEmail());
        txfPoblacion.setText(socio.getPoblacion());
        txfTelefono.setText(socio.getTelefono());
        txfSocio.setText(socio.getSocio());

        //comprobamos si el socio ha pagado, para checkar o no el switch
        if (socio.getQuota().equals("PAGADO")) {
            swSwitch.setChecked(true);
        } else {
            swSwitch.setChecked(false);
        }

        if (!socio.getImagen().equals("")) {
            Picasso.with(getActivity()).load(socio.getImagen()).transform(new ImagenCircular()).into(imgPerfil);
        } else {
            Picasso.with(getActivity()).load(R.drawable.no_perfil).transform(new ImagenCircular()).into(imgPerfil);
        }

        //listener para cuando clicamos en el boton flotante
        //comprobamos las validaciones e insertamos un socio en firebase Database
        onClickFab();

        //listener para cuando hacemos un longClick en la imagen de perfil
        //comprobamos las validaciones e insertamos un socio en firebase Database
        onListenersImagePerfil();

    }

    //metodo que crea un qr a partir de un nombre
    private void añadirQR(View rootView) {
        qr_code = (ImageView) rootView.findViewById(R.id.imageTitulo);

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(socio.getNombre(), BarcodeFormat.QR_CODE, 400, 400);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            qr_code.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    //listener del boton flotante de actualizar socio
    private void onClickFab() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //comprobamos si hay internet, para lanzar la aplicación o no.
                if (isNetDisponible() || isOnlineNet()) {
                    //obtenemos los valores escritos por el usuario
                    final String nombre = txfNombre.getText().toString();
                    final String dni = txfDni.getText().toString();
                    final String email = txfEmail.getText().toString();
                    final String direccion = txfDireccion.getText().toString();
                    final String poblacion = txfPoblacion.getText().toString();
                    final String telefono = txfTelefono.getText().toString();
                    final String quota;


                    //comprobamos que eleccion hay en el switch //si es true HA PAGADO si es false no ha pagado
                    if (swSwitch.isChecked()) {
                        quota = "PAGADO";
                    } else {
                        quota = "";
                    }

                    try {
                        //validamos los campos
                        if (validar(nombre, dni, email, direccion, poblacion, telefono)) {

                            //escondemos el teclado virtual
                            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            View focus = getActivity().getCurrentFocus();
                            if (focus != null)
                                inputMethodManager.hideSoftInputFromWindow(focus.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                            //mostramos un dialogo para preguntar si estamos seguro de actualizar el registro
                            new AlertDialog.Builder(getActivity())
                                    .setIcon(R.drawable.ic_action_person)
                                    .setTitle(R.string.titulo_actualizar)
                                    .setMessage(R.string.texto_actualizar)
                                    .setNegativeButton(android.R.string.cancel, null)
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            //mostramos un dialogo de carga
                                            mostrarCarga();

                                            //comprobamos si hemos elegido una imagen de perfil, para actualizarla o no
                                            if (imgSeleccionada == null) {
                                                actualizarSocio(nombre, dni, email, direccion, poblacion, telefono, quota, socio, "");

                                            } else {
                                                //referencia al almacenamiento de imagenes de perfil
                                                storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://associaciojoves-70d35.appspot.com/socio_perfil/" + nombre + ".png");

                                                //si hemos elegido imagen la subimos al Storage
                                                storageRef.putFile(imgSeleccionada)
                                                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                            @Override
                                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                //una vez subida, obtenemos la url para añadirla al objeto socio
                                                                @SuppressWarnings("VisibleForTests")
                                                                String downloadUrl = taskSnapshot.getDownloadUrl().toString();

                                                                //actualizamos el socio, y su url de la imagen
                                                                actualizarSocio(nombre, dni, email, direccion, poblacion, telefono, quota, socio, downloadUrl);

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

                        }
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.error_añadir), Toast.LENGTH_SHORT).show();
                        FirebaseCrash.log(e.getMessage());
                    }

                }else{
                    getFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left,
                                    R.anim.enter_from_left, R.anim.exit_to_right)
                            .replace(R.id.contenido, new SinConexionFragment()).commit();

                }

            }
        });
    }

    //metodo que actualiza el socio si no se ha cambiado la imagen de perfil
    private void actualizarSocio(String nombre, String dni, String email, String direccion, String poblacion, String telefono, String quota, Socio socio, String url) {

        Socio s = new Socio();

        //comprobamos si la url viene vacia, para actualizarla o no
        if (url.equals("")) {
            s.setImagen(socio.getImagen());
        } else {
            s.setImagen(url);
        }
        s.setDireccion(direccion);
        s.setDni(dni);
        s.setEmail(email);
        s.setNombre(nombre);
        s.setQuota(quota);
        s.setTelefono(telefono);
        s.setPoblacion(poblacion);
        s.setSocio(socio.getSocio());

        //añadimos un nuevo usuario
        ref.child(socio.getNombre()).setValue(s);

        //escondemos el dialogo de carga
        esconderCarga();

        //mostramos un mensaje de exito
        Toast.makeText(getActivity(), getResources().getString(R.string.exito_actualizar) + nombre, Toast.LENGTH_LONG).show();

        //salimos del actual fragment
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right,
                        R.anim.enter_from_right, R.anim.exit_to_left)
                .replace(R.id.contenido, new SociosFragment()).commit();
    }

    //listener al boton
    private void onListenersImagePerfil() {

        //onclick
        imgPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), getResources().getString(R.string.error_onclick), Toast.LENGTH_LONG).show();
            }
        });

        imgPerfil.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                //abrimos un dialogo para elegir imagen de la galeria
                abrirDialogoImagen();
                return false;
            }
        });
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

                        //almacenamos la imagen seleccionada
                        imgSeleccionada = r.getUri();

                        //seteamos la imagen en la cabececera
                        Picasso.with(getActivity()).load(r.getUri()).transform(new ImagenCircular()).into(imgPerfil);
                    }
                }).show(getFragmentManager());

    }

    private boolean isNetDisponible() {

        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo actNetInfo = connectivityManager.getActiveNetworkInfo();

        return (actNetInfo != null && actNetInfo.isConnected());
    }

    public Boolean isOnlineNet() {

        try {
            Process p = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.es");

            int val = p.waitFor();
            boolean reachable = (val == 0);
            return reachable;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onStop() {
        super.onStop();
        //Escondemos el elemento de carga
        esconderCarga();
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
    public void onDestroy() {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        super.onDestroy();
    }

    //validaciones
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
