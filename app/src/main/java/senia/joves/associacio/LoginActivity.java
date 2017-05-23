package senia.joves.associacio;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    //referencias a componentes firebase
    private FirebaseAuth mAuth;

    //referencias a componentes de la vista
    private EditText txfEmail;
    private EditText txfPass;
    private Button btnLogin;
    private Button btnRegistro;

    //variable para el progress dialog
    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //creamos una barra de carga
        mProgressDialog = new ProgressDialog(this);

        //Referencias al modulo de login
        mAuth = FirebaseAuth.getInstance();

        // Capturamos las vistas
        txfEmail = (EditText) findViewById(R.id.txfEmail);
        txfPass = (EditText) findViewById(R.id.txfPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnRegistro = (Button) findViewById(R.id.btnRegistro);

        //añadimos listeners a los botonoes
        añadirListeners();
    }

    private void añadirListeners() {

        //añadimos el listener al boton de login
        onClickLogin();

        //añadimos el listener al boton de registro
        onClickRegistro();
    }

    //listener boton login
    private void onClickLogin(){
        //listener boton login
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = txfEmail.getText().toString();
                String password = txfPass.getText().toString();

                //validamos al clicar en login
                if (validarEmail(email)) {
                    if (validarPassword(password)) {

                        //mostramos un dialogo
                        mProgressDialog.setCancelable(false);
                        mProgressDialog.setMessage(getResources().getString(R.string.texto_cargando));
                        mProgressDialog.show();

                        mAuth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {

                                        //escondemos el progres dialog
                                        if (mProgressDialog != null && mProgressDialog.isShowing()) {
                                            mProgressDialog.dismiss();
                                        }

                                        if (task.isSuccessful()) {
                                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                        } else {
                                            Snackbar.make(findViewById(android.R.id.content), getResources().getString(R.string.error_no_login), Snackbar.LENGTH_LONG).show();
                                        }
                                    }
                                });
                    } else {
                        txfPass.requestFocus();
                        txfPass.setError(getResources().getString(R.string.error_password));
                    }
                } else {
                    txfEmail.requestFocus();
                    txfEmail.setError(getResources().getString(R.string.error_email));
                }


            }
        });


    }

    //listener del boton de registro
    private void onClickRegistro(){
        btnRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = txfEmail.getText().toString();
                String password = txfPass.getText().toString();

                //validamos al clicar en login
                if (validarEmail(email)) {
                    if (validarPassword(password)) {

                        //mostramos un dialogo
                        mProgressDialog.setCancelable(false);
                        mProgressDialog.setMessage(getResources().getString(R.string.texto_cargando));
                        mProgressDialog.show();

                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {

                                        //escondemos el progres dialog
                                        if (mProgressDialog != null && mProgressDialog.isShowing()) {
                                            mProgressDialog.dismiss();
                                        }

                                        if (task.isSuccessful()) {
                                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                        } else {
                                            Snackbar.make(findViewById(android.R.id.content), getResources().getString(R.string.error_no_login), Snackbar.LENGTH_LONG).show();
                                        }
                                    }
                                });
                    } else {
                        txfPass.requestFocus();
                        txfPass.setError(getResources().getString(R.string.error_password));

                    }
                } else {
                    txfEmail.requestFocus();
                    txfEmail.setError(getResources().getString(R.string.error_email));

                }

            }
        });
    }

    //al empezar la actividad, comprobamos si el usuario esta logueado, para cargar o no el login
    @Override
    public void onStart() {
        super.onStart();

        // Si getCurrentUser no es null, avanzamos al siguiente activity
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    //validaciones
    private boolean validarPassword(String pass) {
        Pattern patron = Pattern.compile("^(?=.*\\d)(?=.*[\\u0021-\\u002b\\u003c-\\u0040])(?=.*[A-Z])(?=.*[a-z])\\S{8,16}$");

        return patron.matcher(pass).matches();
    }

    private boolean validarEmail(String correo) {
        Pattern patron = Pattern.compile("^[_a-z0-9-]+(.[_a-z0-9-]+)*@[a-z0-9-]+(.[a-z0-9-]+)*(.[a-z]{2,4})$");

        return patron.matcher(correo).matches();
    }


    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(this)
                .setIcon(R.drawable.salir)
                .setTitle(R.string.titulo_back)
                .setMessage(R.string.texto_back)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Salir
                        finishAffinity();
                    }
                })
                .show();
    }

}

