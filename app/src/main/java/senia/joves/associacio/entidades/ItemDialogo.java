package senia.joves.associacio.entidades;

import java.io.Serializable;

/**
 * Created by Ruben on 16/05/2017.
 */

public class ItemDialogo implements Serializable {

    private String icono;
    private String texto;

    public ItemDialogo() {

    }

    public ItemDialogo(String icono, String texto) {
        this.setIcono(icono);
        this.texto = texto;

    }


    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getIcono() {
        return icono;
    }

    public void setIcono(String icono) {
        this.icono = icono;
    }
}
