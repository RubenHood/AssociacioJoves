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
import senia.joves.associacio.entidades.Socio;
import senia.joves.associacio.librerias.ImagenCircular;

public class AdaptadorSocios extends BaseAdapter {

    private Context context;
    private ArrayList<Socio> items;

    public AdaptadorSocios(Context context, ArrayList<Socio> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return this.items.size();
    }

    @Override
    public Object getItem(int position) {
        return this.items.get(position);
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
        TextView lblPagado = (TextView) rowView.findViewById(R.id.pagado);
        TextView lblSocio = (TextView) rowView.findViewById(R.id.socio);

        //obtenemos el objeto de la tabla a partir de la lista
        Socio item = this.items.get(position);

        //Seteamos la vista con los valores que le tocan a cada fila
        Picasso.with(context).load(R.drawable.roberto).fit().transform(new ImagenCircular()).into(fotoSocio);
        lblNombre.setText(item.getNombre());
        lblPagado.setText(item.getQuota());
        lblSocio.setText(item.getSocio());

        return rowView;
    }

}
