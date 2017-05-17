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

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //capturamos la lista
        listaDialogo = (ListView) view.findViewById(R.id.listaDialogo);

        //limpiamos la lista
        LISTA_DIALOGO.clear();

        //añadimos dos items para la lista
        LISTA_DIALOGO.add(new ItemDialogo("camara", getActivity().getResources().getString(R.string.texto_camara)));
        LISTA_DIALOGO.add(new ItemDialogo("galeria", getActivity().getResources().getString(R.string.texto_galeria)));

        //creamos un adaptador a partir del Array lleno y el contexto
        AdaptadorDialogoFotos ad = new AdaptadorDialogoFotos(getActivity());

        //pasamos el adapter a la lista
        listaDialogo.setAdapter(ad);

    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
    }
}
