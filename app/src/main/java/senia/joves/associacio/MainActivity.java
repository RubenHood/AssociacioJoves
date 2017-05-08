package senia.joves.associacio;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import senia.joves.associacio.fragments.EventosFragment;
import senia.joves.associacio.fragments.NoticiasFragment;
import senia.joves.associacio.fragments.SociosFragment;

public class MainActivity extends AppCompatActivity {

    //referencia al menu bottom
    BottomNavigationView navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navigation = (BottomNavigationView) findViewById(R.id.navegacion);


        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.socios:
                        if (navigation.getSelectedItemId() == R.id.socios) {

                        } else if (navigation.getSelectedItemId() == R.id.eventos || navigation.getSelectedItemId() == R.id.noticias) {
                            getSupportFragmentManager().beginTransaction()
                                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left,
                                            R.anim.enter_from_left, R.anim.exit_to_right)
                                    .replace(R.id.contenido, new SociosFragment())
                                    .addToBackStack("sociosFragment").commit();
                        }

                        return true;
                    case R.id.eventos:
                        if (navigation.getSelectedItemId() == R.id.eventos) {

                        } else if (navigation.getSelectedItemId() == R.id.noticias) {
                            getSupportFragmentManager().beginTransaction()
                                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left,
                                            R.anim.enter_from_left, R.anim.exit_to_right)
                                    .replace(R.id.contenido, new EventosFragment())
                                    .addToBackStack("eventosFragment").commit();
                        } else if (navigation.getSelectedItemId() == R.id.socios) {
                            getSupportFragmentManager().beginTransaction()
                                    .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right,
                                            R.anim.enter_from_right, R.anim.exit_to_left)
                                    .replace(R.id.contenido, new EventosFragment())
                                    .addToBackStack("eventosFragment").commit();
                        }
                        return true;
                    case R.id.noticias:
                        if (navigation.getSelectedItemId() == R.id.noticias) {

                        } else if (navigation.getSelectedItemId() == R.id.socios || navigation.getSelectedItemId() == R.id.eventos) {
                            getSupportFragmentManager().beginTransaction()
                                    .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right,
                                            R.anim.enter_from_right, R.anim.exit_to_left)
                                    .replace(R.id.contenido, new NoticiasFragment())
                                    .addToBackStack("noticiasFragment").commit();
                        }
                        return true;
                }
                return false;
            }
        });

        //cargamos el fragment de noticias que va el primero
        getSupportFragmentManager().beginTransaction().replace(R.id.contenido, new NoticiasFragment())
                .addToBackStack("noticiasFragment").commit();
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