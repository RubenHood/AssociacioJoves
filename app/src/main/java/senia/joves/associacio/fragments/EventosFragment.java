package senia.joves.associacio.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;

import senia.joves.associacio.LoginActivity;
import senia.joves.associacio.R;
import senia.joves.associacio.adaptadores.AdaptadorSocios;
import senia.joves.associacio.entidades.Socio;
import senia.joves.associacio.librerias.ImagenCircular;

import static senia.joves.associacio.Static.Recursos.ARRAY_RECIBIDO;
import static senia.joves.associacio.Static.Recursos.LISTA_SOCIOS;
import static senia.joves.associacio.Static.Recursos.NUMERO_ULTIMO_SOCIO;

/**
 * Created by Usuario on 08/05/2017.
 */

public class EventosFragment extends Fragment {

    //variables para las vistas
    TextView nombre;
    TextView dni;
    TextView cuota;
    TextView socio;
    TextView direccion;
    TextView email;
    TextView telefono;
    TextView poblacion;
    ImageView imgSocio;

    //referencia a la base de datos
    private DatabaseReference mDatabase;

    //variable para el progress dialog
    private ProgressDialog mProgressDialog;

    //variable para el socio devuelto
    Socio socioDev = new Socio();

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

    public EventosFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_escanear, container, false);

        //activamos la modificacion del appbar
        setHasOptionsMenu(true);

        Toolbar mToolbar = (Toolbar) rootView.findViewById(R.id.toolbarEventos);
        if (mToolbar != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        }

        //añadimos la descripcion al toolbar
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getActivity().getResources().getString(R.string.titulo_comprobar));

        //referencia a la base de datos
        mDatabase = FirebaseDatabase.getInstance().getReference().child("socios");

        //devolvemos la vista inflada
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //capturamos las vistas
        nombre = (TextView) view.findViewById(R.id.txfNombreScn);
        dni = (TextView) view.findViewById(R.id.txfDniScn);
        cuota = (TextView) view.findViewById(R.id.txfPagadoScn);
        socio = (TextView) view.findViewById(R.id.txfSocioScn);
        direccion = (TextView) view.findViewById(R.id.txfDireccionScn);
        email = (TextView) view.findViewById(R.id.txfEmailScn);
        telefono = (TextView) view.findViewById(R.id.txfTelefonoScn);
        poblacion = (TextView) view.findViewById(R.id.txfPoblacionScn);
        imgSocio = (ImageView) view.findViewById(R.id.imgSocioScn);

        //Abrimos la camara para escanear el qr
        abrirCamaraQR();
    }

    //metodo que se ejecuta al volver de la camara
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Recogemos los datos
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        //comprobamos que el resultado no sea null
        if (result != null) {
            //comprobas si el usuario a cancelado la foto
            if (result.getContents() == null) {
                Toast.makeText(getActivity().getApplicationContext(), getActivity().getResources().getString(R.string.texto_cancelar_qr), Toast.LENGTH_LONG).show();
            } else {
                //Recogemos el resultado y lo mostramos en pantalla

                //añadimos la descripcion al toolbar
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(result.getContents());

                //mostramos un barra de progreso
                mostrarCarga();

                //Buscamos al socio en firebase por el string devuelto por el QR
                consultaSocioQR(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    //metodo que consulta la bd y añade listeners para los cambios. para hacerla a tiempo real
    private void consultaSocioQR(String nombre) {

        mDatabase.orderByChild("nombre").equalTo(nombre).addListenerForSingleValueEvent(
                new ValueEventListener() {
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
                        FirebaseCrash.log("Error al recuperar datos: " + databaseError.toException());
                    }
                });
    }

    //metodo que a partir de un dato Snapshot, rellenamos un arraylist con todos los socios
    private void obtenerSocios(DataSnapshot dataSnapshot) {

        for (DataSnapshot ds : dataSnapshot.getChildren()) {

            //obtenemos a partir del DataSnapshot cada dato, lo pasamos al objeto
            socioDev.setDireccion(ds.child("direccion").getValue().toString());
            socioDev.setDni(ds.child("dni").getValue().toString());
            socioDev.setEmail(ds.child("email").getValue().toString());
            socioDev.setNombre(ds.child("nombre").getValue().toString());
            socioDev.setPoblacion(ds.child("poblacion").getValue().toString());
            socioDev.setQuota(ds.child("quota").getValue().toString());
            socioDev.setSocio(ds.child("socio").getValue().toString());
            socioDev.setTelefono(ds.child("telefono").getValue().toString());
            socioDev.setImagen(ds.child("imagen").getValue().toString());

        }
    }

    //metodo que a partir del array, rellenamos la interfaz
    private void rellenarInterfaz() {

        //a partir del objeto Socio rellenado, rellenamos los TextViews
        nombre.setText(socioDev.getNombre());
        dni.setText(socioDev.getDni());
        cuota.setText(socioDev.getQuota());
        socio.setText(socioDev.getSocio());
        direccion.setText(socioDev.getDireccion());
        email.setText(socioDev.getEmail());
        telefono.setText(socioDev.getTelefono());
        poblacion.setText(socioDev.getPoblacion());

        //comprobamos si esta vacío para cargar la imagen
        if(!socioDev.getImagen().equals("")){
            Picasso.with(getActivity().getApplicationContext()).load(socioDev.getImagen()).fit().transform(new ImagenCircular()).into(imgSocio);
        }


    }

    //metodo que abre la camara de fotos para leer un codigo qr
    private void abrirCamaraQR() {
        IntentIntegrator.forSupportFragment(this)
                .setBarcodeImageEnabled(true)
                .setOrientationLocked(false)
                .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES)
                .setPrompt(getActivity().getResources().getString(R.string.texto_camara_qr))
                .setCameraId(0)
                .setBeepEnabled(false)
                .initiateScan();
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