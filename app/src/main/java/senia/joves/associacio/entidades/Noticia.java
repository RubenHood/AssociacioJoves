package senia.joves.associacio.entidades;

import java.io.Serializable;

/**
 * Created by Usuario on 22/05/2017.
 */

public class Noticia implements Serializable, Comparable<Noticia>  {
    private String nombre;

    public Noticia() {
    }

    public Noticia(String nombre) {
       this.setNombre(nombre);
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public int compareTo(Noticia o) {
        String a = (String.valueOf(this.getNombre()));
        String b = (String.valueOf(o.getNombre()));

        return a.compareTo(b);
    }
}
