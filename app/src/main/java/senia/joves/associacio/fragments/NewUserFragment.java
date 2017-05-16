package senia.joves.associacio.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.regex.Pattern;

import senia.joves.associacio.LoginActivity;
import senia.joves.associacio.R;
import senia.joves.associacio.entidades.Socio;

import static senia.joves.associacio.Static.Recursos.NUMERO_ULTIMO_SOCIO;

/**
 * Created by Ruben on 08/05/2017.
 */

public class NewUserFragment extends Fragment {

    //variable para almacenar el nombre del archivo
    private static final int SELECT_FILE = 1;

    //referencias a componentes de la vista
    private EditText txfNombre;
    private EditText txfDni;
    private EditText txfEmail;
    private EditText txfDireccion;
    private EditText txfPoblacion;
    private EditText txfTelefono;
    private Switch swSwitch;
    private FloatingActionButton fab;
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

        //añadimos el titulo al toolbar
//        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getActivity().getResources().getString(R.string.titulo_nuevo));

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
        fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        imgPerfil = (ImageView) getActivity().findViewById(R.id.imgNuevoSocio);

        //modificamos el tamaño del switch
        swSwitch.setSwitchMinWidth(200);
        swSwitch.setSwitchPadding(20);

        imgPerfil.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                abrirGaleria(v);
                return false;

            }
        });


        //listener para cuando clicamos en el boton flotante
        //añadimos un socio nuevo en firebase
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

    @Override
    public void onDestroy() {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        super.onDestroy();
    }

    //Metodo que abre la galeria del sistema
    public void abrirGaleria(View v) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(intent, "Seleccione una imagen"), 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Uri selectedImageUri = null;
        Uri selectedImage;

        String filePath = null;
        switch (requestCode) {
            case SELECT_FILE:
                if (resultCode == Activity.RESULT_OK) {
                    selectedImage = data.getData();
                    String selectedPath = selectedImage.getPath();
                    if (requestCode == SELECT_FILE) {

                        if (selectedPath != null) {
                            InputStream imageStream = null;
                            try {
                                imageStream = getActivity().getContentResolver().openInputStream(selectedImage);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }

                            // Transformamos la URI de la imagen a inputStream y este a un Bitmap
                            Bitmap bmp = BitmapFactory.decodeStream(imageStream);

                            // Ponemos nuestro bitmap en un ImageView que tengamos en la vista
                            imgPerfil.setImageBitmap(bmp);

                        }
                    }
                }
                break;
        }
    }

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
