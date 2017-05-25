package senia.joves.associacio;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.sylversky.fontreplacer.FontReplacer;
import com.sylversky.fontreplacer.Replacer;

import senia.joves.associacio.fragments.EscanearFragment;
import senia.joves.associacio.fragments.NoticiasFragment;
import senia.joves.associacio.fragments.SociosFragment;
import senia.joves.associacio.fragments.error.SinConexionFragment;

public class MainActivity extends AppCompatActivity {

    //referencia al menu bottom
    BottomNavigationView navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //cambiamos la fuente del proyecto
        Replacer replacer = FontReplacer.Build(getApplicationContext());
        replacer.setDefaultFont("fonts/ProductSans-Regular.ttf");
        replacer.setBoldFont("fonts/ProductSans-Bold.ttf");
        replacer.setBoldItalicFont("fonts/ProductSans-Bold-Italic.ttf");
        replacer.setItalicFont("fonts/ProductSans-Italic.ttf");
        replacer.applyFont();

        navigation = (BottomNavigationView) findViewById(R.id.navegacion);

        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.socios:
                        if (navigation.getSelectedItemId() == R.id.eventos || navigation.getSelectedItemId() == R.id.noticias) {
                            getSupportFragmentManager().beginTransaction()
                                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left,
                                            R.anim.enter_from_left, R.anim.exit_to_right)
                                    .replace(R.id.contenido, new SociosFragment()).commit();
                        }

                        return true;
                    case R.id.eventos:
                        //comprobamos si hay internet, para lanzar la aplicación o no.
                        if (isNetDisponible() || isOnlineNet()) {

                            if (navigation.getSelectedItemId() == R.id.noticias) {
                                getSupportFragmentManager().beginTransaction()
                                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left,
                                                R.anim.enter_from_left, R.anim.exit_to_right)
                                        .replace(R.id.contenido, new EscanearFragment()).commit();
                            } else if (navigation.getSelectedItemId() == R.id.socios) {
                                getSupportFragmentManager().beginTransaction()
                                        .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right,
                                                R.anim.enter_from_right, R.anim.exit_to_left)
                                        .replace(R.id.contenido, new EscanearFragment()).commit();
                            }
                        }else{
                            getSupportFragmentManager().beginTransaction()
                                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left,
                                            R.anim.enter_from_left, R.anim.exit_to_right)
                                    .replace(R.id.contenido, new SinConexionFragment()).commit();
                        }

                        return true;
                    case R.id.noticias:
                        if (navigation.getSelectedItemId() == R.id.socios || navigation.getSelectedItemId() == R.id.eventos) {
                            getSupportFragmentManager().beginTransaction()
                                    .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right,
                                            R.anim.enter_from_right, R.anim.exit_to_left)
                                    .replace(R.id.contenido, new NoticiasFragment()).commit();
                        }
                        return true;
                }
                return false;
            }
        });

        //cargamos el fragment de noticias que va el primero
        getSupportFragmentManager().beginTransaction().replace(R.id.contenido, new NoticiasFragment()).commit();
    }

    @Override
    public void onBackPressed() {

        //comprobamos si hay algun fragment cargado en el BackStack
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); i++) {
                getSupportFragmentManager().popBackStack();
            }
        } else {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        finishAffinity();
    }

    public boolean isNetDisponible() {

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