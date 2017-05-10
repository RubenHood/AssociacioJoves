package senia.joves.associacio.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Switch;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

import senia.joves.associacio.LoginActivity;
import senia.joves.associacio.MainActivity;
import senia.joves.associacio.R;
import senia.joves.associacio.entidades.Socio;

import static senia.joves.associacio.fragments.SociosFragment.NUMERO_ULTIMO_SOCIO;

/**
 * Created by Ruben on 08/05/2017.
 */

public class DetalleFragment extends Fragment {

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

    //referencia a la bd
    DatabaseReference ref;

    //variable que almacena el socio que llega
    private Socio socio;

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

        //añadimos el boton de ir atras
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //añadimos el titulo al toolbar
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(socio.getNombre());

        return inflater.inflate(R.layout.fragment_detalle, container, false);
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
        swSwitch = (Switch) getActivity().findViewById(R.id.swPagado);
        fab = (FloatingActionButton) getActivity().findViewById(R.id.fab_d);

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
        if(socio.getQuota().equals("PAGADO")){
            swSwitch.setChecked(true);
        }else{
            swSwitch.setChecked(false);
        }

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
                        if(focus != null)
                            inputMethodManager.hideSoftInputFromWindow(focus.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                        //Añadimos un socio al contador
                        NUMERO_ULTIMO_SOCIO++;

                        //mostramos un dialogo para preguntar si estamos seguro de actualizar el registro
                        new AlertDialog.Builder(getActivity())
                                .setIcon(R.drawable.ic_action_person)
                                .setTitle(R.string.titulo_actualizar)
                                .setMessage(R.string.texto_actualizar)
                                .setNegativeButton(android.R.string.cancel, null)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //añadimos un nuevo usuario
//                                        ref.child(String.valueOf(NUMERO_ULTIMO_SOCIO)).setValue(new Socio(direccion, dni, email, nombre, poblacion, quota, String.valueOf(NUMERO_ULTIMO_SOCIO), telefono));
//
//                                        //salimos del actual fragment
//                                        getFragmentManager().popBackStack();
                                    }
                                })
                                .show();

                    }
                } catch (Exception e) {
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

    private boolean validar(String nombre, String dni, String email, String direccion, String poblacion, String telefono) {
        if (!validarnombre(nombre)) {
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

    private boolean validarnombre(String nombre) {
        Pattern patron = Pattern.compile("^[a-zñA-Z\\s]{5,40}$");

        return patron.matcher(nombre).matches();
    }

    private boolean validarVacio(String nombre) {
        if (nombre.length() > 60 || nombre.isEmpty()){
            return  false;
        }else {
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
