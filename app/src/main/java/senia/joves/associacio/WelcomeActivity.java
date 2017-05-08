package senia.joves.associacio;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Timer;
import java.util.TimerTask;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

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

        if (isNetDisponible() && isOnlineNet()) {
            // simular un tiempo
            Timer timer = new Timer();
            timer.schedule(task, 500);
        } else {
            Snackbar.make(findViewById(android.R.id.content), getResources().getString(R.string.error_no_internet), Snackbar.LENGTH_INDEFINITE).show();

            // simular un tiempo
            Timer timer = new Timer();
            timer.schedule(task2, 10000);
        }

    }

    private boolean isNetDisponible() {

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

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
}
