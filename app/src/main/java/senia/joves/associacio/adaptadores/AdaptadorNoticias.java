package senia.joves.associacio.adaptadores;

/**
 * Created by Ruben on 24/03/2017.
 */

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;


import senia.joves.associacio.R;
import senia.joves.associacio.entidades.Noticia;

import static senia.joves.associacio.Static.Recursos.LISTA_URL_IMAGENES;

//array que recibimos desde Sociosfragment

public class AdaptadorNoticias extends BaseAdapter {

    private Context context;

    public AdaptadorNoticias(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return LISTA_URL_IMAGENES.size();
    }

    @Override
    public Object getItem(int position) {
        return LISTA_URL_IMAGENES.get(position);
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
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.fila_fragment_noticias, parent, false);
        }

        // recogemos los objetos de la vista
        ImageView img = (ImageView) rowView.findViewById(R.id.img_noticias);

        //obtenemos el objeto de la tabla a partir de la lista
        Noticia item = LISTA_URL_IMAGENES.get(position);

        Log.e("imagen en adaptador: " , item.getNombre());

        //comprobamos si tiene foto de perfil para ponerla o para no ponerla
        Picasso.with(context).load(item.getNombre()).fit().into(img);


        return rowView;
    }
}
