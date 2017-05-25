package senia.joves.associacio.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;

import senia.joves.associacio.R;

import static senia.joves.associacio.Static.Recursos.FILTRADO;
import static senia.joves.associacio.Static.Recursos.OPCION_FILTRADO;

/**
 * Created by Usuario on 25/05/2017.
 */

public class DialogoFragmentFiltro extends DialogFragment {

    Spinner spn;
    Button btnFiltrar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //hacemos que no se pueda salir al tocar fuera del dialogo
        setCancelable(false);

        return inflater.inflate(R.layout.fragment_filtrado, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        //cogemos los elementos de la vista
        spn = (Spinner) view.findViewById(R.id.spnFiltro);
        btnFiltrar = (Button) view.findViewById(R.id.btnFiltrar);

        //seteamos el spiner en la ultima posicion conocida
        spn.setSelection(OPCION_FILTRADO);

        //listener del boton de aceptar
        btnFiltrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //cambiamos el valor estatico del filtrado para que se filtren los socios
                switch ((String) spn.getSelectedItem()){
                    case "nombre":
                        FILTRADO = "nombre";
                        break;
                    case "Nº Socio":
                        FILTRADO = "socio";
                        break;
                    case "Cuota pagada":
                        FILTRADO = "quota";
                        break;
                }


                //almacenamos el valor del spiner
                OPCION_FILTRADO = spn.getSelectedItemPosition();

                //cerramos el dialogo
                dismiss();

                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left,
                                R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.contenido, new SociosFragment()).commit();
            }
        });


        super.onViewCreated(view, savedInstanceState);
    }
}
