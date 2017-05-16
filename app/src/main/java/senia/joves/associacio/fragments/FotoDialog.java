package senia.joves.associacio.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import senia.joves.associacio.R;
import senia.joves.associacio.adaptadores.AdaptadorDialogoFotos;
import senia.joves.associacio.adaptadores.AdaptadorSocios;
import senia.joves.associacio.entidades.ItemDialogo;

import static senia.joves.associacio.Static.Recursos.ARRAY_RECIBIDO;
import static senia.joves.associacio.Static.Recursos.LISTA_DIALOGO;
import static senia.joves.associacio.Static.Recursos.LISTA_SOCIOS;

/**
 * Created by Ruben on 16/05/2017.
 */

public class FotoDialog extends DialogFragment {

    ListView listaDialogo;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.dialog_foto, container, false);

        //capturamos la lista
        listaDialogo = (ListView) getActivity().findViewById(R.id.listaDialogo);

        LISTA_DIALOGO.add(new ItemDialogo(R.drawable.ic_action_camera_alt, "Camara"));
        LISTA_DIALOGO.add(new ItemDialogo(R.drawable.gallery, "Galeria"));

        //creamos un adaptador a partir del Array lleno y el contexto
        AdaptadorDialogoFotos ad = new AdaptadorDialogoFotos(getActivity(), LISTA_DIALOGO);

        //pasamos el adapter a la lista
        listaDialogo.setAdapter(ad);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }
}
