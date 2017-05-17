package senia.joves.associacio.adaptadores;

/**
 * Created by Ruben on 24/03/2017.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import senia.joves.associacio.R;
import senia.joves.associacio.entidades.ItemDialogo;

import static senia.joves.associacio.Static.Recursos.LISTA_DIALOGO;

//array que recibimos desde Sociosfragment

public class AdaptadorDialogoFotos extends BaseAdapter {

    private Context context;


    public AdaptadorDialogoFotos(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return LISTA_DIALOGO.size();
    }

    @Override
    public Object getItem(int position) {
        return LISTA_DIALOGO.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rowView = convertView;

        if (convertView == null) {
            // Create a new view into the list.
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.fila_dialogo_fotos, parent, false);
        }

        // recogemos los objetos de la vista
        ImageView img = (ImageView) rowView.findViewById(R.id.img_dialogo);
        TextView txt = (TextView) rowView.findViewById(R.id.texto_dialogo);


        //obtenemos el objeto de la tabla a partir de la lista
        ItemDialogo item = LISTA_DIALOGO.get(position);

        //Seteamos la vista con los valores que le tocan a cada fila
        txt.setText(item.getTexto());

        //comprobamos que imagen viene para elegir qual ponemos
        if(item.getIcono().equals("galeria")){
            Picasso.with(context).load(R.drawable.gallery).into(img);
        }else {
            Picasso.with(context).load(R.drawable.ic_action_camera_alt).into(img);
        }


        return rowView;
    }


}
