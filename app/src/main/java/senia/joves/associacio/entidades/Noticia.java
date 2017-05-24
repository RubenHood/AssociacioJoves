package senia.joves.associacio.entidades;

import java.io.Serializable;

/**
 * Created by Usuario on 22/05/2017.
 */

public class Noticia implements Serializable, Comparable<Noticia>  {
    private String nombre;
    private String id;

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
        String a = (String.valueOf(this.getId()));
        String b = (String.valueOf(o.getId()));

        return a.compareTo(b);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
