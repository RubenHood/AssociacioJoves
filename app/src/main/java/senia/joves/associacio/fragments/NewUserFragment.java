package senia.joves.associacio.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.regex.Pattern;

import senia.joves.associacio.LoginActivity;
import senia.joves.associacio.R;
import senia.joves.associacio.entidades.Socio;

import static android.app.Activity.RESULT_OK;
import static com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE;
import static com.theartofdev.edmodo.cropper.CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE;
import static com.theartofdev.edmodo.cropper.CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE;
import static com.theartofdev.edmodo.cropper.CropImage.getActivityResult;
import static com.theartofdev.edmodo.cropper.CropImage.getPickImageChooserIntent;
import static senia.joves.associacio.Static.Recursos.NUMERO_ULTIMO_SOCIO;
import static senia.joves.associacio.Static.Recursos.SELECT_FILE;

/**
 * Created by Ruben on 08/05/2017.
 */

public class NewUserFragment extends Fragment {

    //variables para cargar las imagenes
    Uri mCropImageUri;

    //variable que guarda la imagen seleccionada en bitmap
    Bitmap imgBitmap;

    //variable que guarda la imagen seleccionada en URI
    Uri imgURI;


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
    private CropImageView imgPerfil;

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

        ref = FirebaseDatabase.getInstance().getReference("socios");

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
        imgPerfil = (CropImageView) getActivity().findViewById(R.id.imgNuevoSocio);
        fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        fabImagenes = (FloatingActionButton) getActivity().findViewById(R.id.fabFoto);

        //modificamos el tamaño del switch
        swSwitch.setSwitchMinWidth(200);
        swSwitch.setSwitchPadding(20);

        //comprobamos si hay imagen para cargarla y datos escritos anteriormente por el usuario
//        if (getArguments() != null) {
//            Bitmap bmp = getArguments().getParcelable("imagen");
//            imgPerfil.setImageBitmap(bmp);
//
//            Socio s = (Socio) getArguments().getSerializable("socio");
//
//            //mostramos los datos en sus textView
//            txfNombre.setText(s.getNombre());
//            txfDireccion.setText(s.getDireccion());
//            txfDni.setText(s.getDni());
//            txfEmail.setText(s.getEmail());
//            txfPoblacion.setText(s.getPoblacion());
//            txfTelefono.setText(s.getTelefono());
//        }

        //listener para el boton de añadir imagenes
        fabImagenes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //comprobamos si el movil necesita permisos, si los necesita, los pedimos. Si no abrimos el dialogo picker
                abrirDialogo();

//                CropImage.startPickImageActivity(getActivity());

                //obtenemos los valores escritos por el usuario
//                final String nombre = txfNombre.getText().toString();
//                final String dni = txfDni.getText().toString();
//                final String email = txfEmail.getText().toString();
//                final String direccion = txfDireccion.getText().toString();
//                final String poblacion = txfPoblacion.getText().toString();
//                final String telefono = txfTelefono.getText().toString();
//                final String quota;
//
//                //comprobamos que eleccion hay en el switch //si es true HA PAGADO si es false no ha pagado
//                if (swSwitch.isChecked()) {
//                    quota = "PAGADO";
//                } else {
//                    quota = "";
//                }
//
//                //creamos un socio para almacenar los datos ya escritos
//                Socio s = new Socio();
//                s.setDireccion(direccion);
//                s.setNombre(nombre);
//                s.setDni(dni);
//                s.setEmail(email);
//                s.setPoblacion(poblacion);
//                s.setTelefono(telefono);
//                s.setQuota(quota);
//
//                //Abrimos el dialogo de añadir imagenes
//                FotoDialog dialog = FotoDialog.newInstance(s);
//                dialog.show(getFragmentManager(), "foto");

            }
        });


        //listener para cuando clicamos en el boton flotante añadimos un socio nuevo en firebase
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
                    if (validar(nombre, dni, email, direccion, poblacion, telefono)) {

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

                                        Toast.makeText(getActivity(), getResources().getString(R.string.exito_añadir) + nombre, Toast.LENGTH_LONG).show();

                                        //salimos del actual fragment
                                        getFragmentManager().beginTransaction()
                                                .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right,
                                                        R.anim.enter_from_right, R.anim.exit_to_left)
                                                .replace(R.id.contenido, new SociosFragment()).commit();
                                    }
                                })
                                .show();


                    }
                } catch (Exception e) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_añadir), Toast.LENGTH_SHORT).show();
                    FirebaseCrash.log(e.getMessage());
                }
            }
        });
    }

    //comprobamos los permisos para lanzar o no el dialogo de elegir imagen
    @SuppressLint("NewApi")
    public void abrirDialogo() {
        if (CropImage.isExplicitCameraPermissionRequired(getActivity())) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
        } else {
            startPickImageActivity(getActivity());

        }
    }

    //metodo que abre el dialogo de abrir imagen
    public void startPickImageActivity(@NonNull Activity activity) {
        this.startActivityForResult(getPickImageChooserIntent(activity), PICK_IMAGE_CHOOSER_REQUEST_CODE);
    }

    //metodo que abre la actividad para cortar la imagen
    private void startCropImageActivity(Uri imageUri) {
        Intent intent = CropImage.activity(imageUri).setFixAspectRatio(true).setActivityTitle("Recorte su imagen").setNoOutputImage(true)
                .getIntent(getContext());
        this.startActivityForResult(intent, CROP_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    //metodo que se ejecuta al cerrarse el dialogo de los permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startPickImageActivity(getActivity());
            } else {
                Toast.makeText(getActivity(), "Debe dar permiso a la aplicación, para poder cargar una imagen.", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // si ya tenemos los permisos abirmos la actividad de crop
                startCropImageActivity(mCropImageUri);
            } else {
                Toast.makeText(getActivity(), "Debe dar permiso a la aplicación, para poder cargar una imagen.", Toast.LENGTH_LONG).show();
            }
        }
    }

    //metodo que se ejecuta al acabar de elegir imagen
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // comprobamos si tenemos permisoss y vamos a cortar la imagen
        if (requestCode == PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(getActivity(), data);

            // For API >= 23 necesitamos los permisos
            if (CropImage.isReadExternalStoragePermissionsRequired(getActivity(), imageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                mCropImageUri = imageUri;
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
            } else {
                // si no requerimos permisos
                startCropImageActivity(imageUri);
            }
        }

        Uri resultUri = null;

        //comprobamos si venimos de cortar la imagen, entonces la mostramos al usuario, la subimos a Firebase
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            resultUri = result.getUri();


            imgPerfil.setImageUriAsync(resultUri);
        }
    }

    @Override
    public void onDestroy() {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        super.onDestroy();
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
