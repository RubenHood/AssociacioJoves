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

//array que recibimos desde Sociosfragment

public class AdaptadorDialogoFotos extends BaseAdapter {

    private Context context;

    ArrayList<ItemDialogo> listaDialogo;

    public AdaptadorDialogoFotos(Context context, ArrayList<ItemDialogo> listaDialogo) {
        this.context = context;
        this.listaDialogo = listaDialogo;
    }

    @Override
    public int getCount() {
        return listaDialogo.size();
    }

    @Override
    public Object getItem(int position) {
        return listaDialogo.get(position);
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
            rowView = inflater.inflate(R.layout.fila_dialogo_fotos, parent, false);
        }

        // recogemos los objetos de la vista
        ImageView img = (ImageView) rowView.findViewById(R.id.img_dialogo);
        TextView txt = (TextView) rowView.findViewById(R.id.texto_dialogo);


        //obtenemos el objeto de la tabla a partir de la lista
        ItemDialogo item = listaDialogo.get(position);

        //Seteamos la vista con los valores que le tocan a cada fila
        txt.setText(item.getTexto());

//        Picasso.with(context).load(item.getIcono()).into(img);

//        //comprobamos si tiene foto de perfil para ponerla o para no ponerla
//        if(item.getImagen() == null || item.getImagen().isEmpty()){
//            Picasso.with(context).load(R.drawable.no_perfil).fit().transform(new ImagenCircular()).into(fotoSocio);
//        }else {
//            Picasso.with(context).load(item.getImagen()).fit().transform(new ImagenCircular()).into(fotoSocio);
//        }


        return rowView;
    }


}
