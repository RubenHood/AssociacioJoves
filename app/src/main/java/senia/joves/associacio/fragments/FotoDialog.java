package senia.joves.associacio.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.FileNotFoundException;
import java.io.InputStream;

import senia.joves.associacio.R;
import senia.joves.associacio.adaptadores.AdaptadorDialogoFotos;
import senia.joves.associacio.adaptadores.AdaptadorSocios;
import senia.joves.associacio.entidades.ItemDialogo;
import senia.joves.associacio.entidades.Socio;

import static senia.joves.associacio.Static.Recursos.ARRAY_RECIBIDO;
import static senia.joves.associacio.Static.Recursos.LISTA_DIALOGO;
import static senia.joves.associacio.Static.Recursos.LISTA_SOCIOS;
import static senia.joves.associacio.Static.Recursos.SELECT_FILE;

/**
 * Created by Ruben on 16/05/2017.
 */

public class FotoDialog extends DialogFragment {

    ListView listaDialogo;

    public static FotoDialog newInstance(Socio socio) {
        FotoDialog f = new FotoDialog();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putSerializable("socio", socio);
        f.setArguments(args);

        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.dialog_foto, container, false);

        this.getDialog().setCanceledOnTouchOutside(false);

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

        //Listener para cuando clicamos en la lista
        listaDialogo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        //si clicamos en la primer opcion, abrimos la camara
                        break;
                    case 1:
                        //si clicamos en la segnda opcion, abrimos la galeria
                        abrirGaleria();
                        break;
                }
            }
        });

    }

    //metodo que se ejecuta al cargar las vistas. sirve para cambiar la animacion del fragment
    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
    }

    //Metodo que abre la galeria del sistema
    public void abrirGaleria() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Seleccione una imagen"), SELECT_FILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Uri selectedImageUri = null;
        Uri selectedImage;

        String filePath = null;
        switch (requestCode) {
            case SELECT_FILE:
                if (resultCode == Activity.RESULT_OK) {
                    selectedImage = data.getData();
                    String selectedPath = selectedImage.getPath();
                    if (requestCode == SELECT_FILE) {

                        if (selectedPath != null) {
                            InputStream imageStream = null;
                            try {
                                imageStream = getActivity().getContentResolver().openInputStream(selectedImage);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }

                            // Transformamos la URI de la imagen a inputStream y este a un Bitmap
                            Bitmap bmp = BitmapFactory.decodeStream(imageStream);

                            //creamos un fragment y le añadimos la imagen para hacerla servir luego
                            NewUserFragment d = new NewUserFragment();
                            Bundle b = new Bundle();
                            b.putParcelable("imagen", bmp);
                            b.putSerializable("socio", getArguments().getSerializable("socio"));
                            d.setArguments(b);

                            //cerramos el dialogo
                            this.dismiss();

                            //vamos al fragment de detalle
                            //salimos del actual fragment
                            getFragmentManager().beginTransaction()
                                    .replace(R.id.contenido, d).addToBackStack("detalleFragment").commit();

                        }
                    }
                }
                break;
        }
    }



}
