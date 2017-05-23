package senia.joves.associacio;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // A partir de la version de kitkat, Escondemos los botones virtuales
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            hideVirtualButtons();
        }

        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                // Lanza una nueva Actividad
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));

                // cerrar esta actividad
                finish();
            }
        };

        TimerTask task2 = new TimerTask() {
            @Override
            public void run() {

                startActivity(new Intent(getApplicationContext(), WelcomeActivity.class));

                // cerrar esta actividad
                finish();
            }
        };

        //comprobamos si hay internet, para lanzar la aplicación o no.
        if (isNetDisponible() && isOnlineNet()) {
            // tiempo que se mostrará el logo en pantalla, si hay internet
            Timer timer = new Timer();
            timer.schedule(task, 3000);
        } else {
            Snackbar.make(findViewById(android.R.id.content), getResources().getString(R.string.error_no_internet), Snackbar.LENGTH_INDEFINITE).show();

            // Tiempo que tardará en reiniciar la aplicación
            Timer timer = new Timer();
            timer.schedule(task2, 10000);
        }

    }

    //metodo que comprueba si estamos conectado a alguna red.
    private boolean isNetDisponible() {

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo actNetInfo = connectivityManager.getActiveNetworkInfo();

        return (actNetInfo != null && actNetInfo.isConnected());
    }

    //metodo que hace un ping a google
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

    //Al obtener el foco, volvemos a esconder los botones
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // In KITKAT (4.4) and next releases, hide the virtual buttons
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                hideVirtualButtons();
            }
        }
    }

    //ocultar botones virtuales (las opciones comentadas son para pantalla completa)
    @TargetApi(19)
    private void hideVirtualButtons() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }
}
