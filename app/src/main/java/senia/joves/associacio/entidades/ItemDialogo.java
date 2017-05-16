package senia.joves.associacio.entidades;

import java.io.Serializable;

/**
 * Created by Ruben on 16/05/2017.
 */

public class ItemDialogo implements Serializable {

    private int icono;
    private String texto;

    public ItemDialogo() {

    }

    public ItemDialogo(int icono, String texto) {
        this.setIcono(icono);
        this.texto = texto;

    }


    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public int getIcono() {
        return icono;
    }

    public void setIcono(int icono) {
        this.icono = icono;
    }
}
