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
import java.util.Locale;

import senia.joves.associacio.R;
import senia.joves.associacio.entidades.Socio;
import senia.joves.associacio.librerias.ImagenCircular;

import static senia.joves.associacio.Static.Recursos.ARRAY_RECIBIDO;

public class AdaptadorSocios extends BaseAdapter {

    private Context context;

    //variable para el filtrado
    private ArrayList<Socio> arrayApoyo;

    public AdaptadorSocios(Context context, ArrayList<Socio> items) {
        ARRAY_RECIBIDO = items;
        this.context = context;
        this.arrayApoyo = new ArrayList<>();
        this.arrayApoyo.addAll(items);
    }

    @Override
    public int getCount() {
        return ARRAY_RECIBIDO.size();
    }

    @Override
    public Object getItem(int position) {
        return ARRAY_RECIBIDO.get(position);
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
            rowView = inflater.inflate(R.layout.fila_fragment_socios, parent, false);
        }

        // recogemos los objetos de la vista
        ImageView fotoSocio = (ImageView) rowView.findViewById(R.id.icono);
        TextView lblNombre = (TextView) rowView.findViewById(R.id.nombre);
        ImageView fotoPagado = (ImageView) rowView.findViewById(R.id.pagado);


        //obtenemos el objeto de la tabla a partir de la lista
        Socio item = ARRAY_RECIBIDO.get(position);

        //Seteamos la vista con los valores que le tocan a cada fila
        lblNombre.setText(item.getNombre());

        //comprobamos si tiene foto de perfil para ponerla o para no ponerla
        if(item.getImagen() == null || item.getImagen().isEmpty()){
            Picasso.with(context).load(R.drawable.no_perfil).fit().transform(new ImagenCircular()).into(fotoSocio);
        }else {
            Picasso.with(context).load(item.getImagen()).fit().transform(new ImagenCircular()).into(fotoSocio);
        }

        //comprobamos si ha pagado o no, para mostrar un dibujo u otro
        if(item.getQuota().equals("PAGADO")) {
            Picasso.with(context).load(R.drawable.pagadob).into(fotoPagado);
        }else{
            Picasso.with(context).load(R.drawable.no_pagadob).into(fotoPagado);
        }

        return rowView;
    }

    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        ARRAY_RECIBIDO.clear();
        if (charText.length() == 0) {
            ARRAY_RECIBIDO.addAll(arrayApoyo);
        } else {
            for (Socio wp : arrayApoyo) {
                if (wp.getNombre().toLowerCase(Locale.getDefault()).contains(charText)) {
                    ARRAY_RECIBIDO.add(wp);
                }
            }
        }
        notifyDataSetChanged();
    }

}
