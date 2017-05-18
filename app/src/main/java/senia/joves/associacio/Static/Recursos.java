package senia.joves.associacio.Static;

import java.util.ArrayList;

import senia.joves.associacio.entidades.Socio;
import senia.joves.associacio.entidades.ItemDialogo;

/**
 * Created by Usuario on 09/05/2017.
 */

public final class Recursos {

    //array original para almacenar todos los socios
    public static final ArrayList<Socio> LISTA_SOCIOS = new ArrayList<>();

    //Constante para almacenar el numero total de socios
    public static int NUMERO_ULTIMO_SOCIO = 0;

    //Array para manejar los socios filtrados
    public static ArrayList<Socio> ARRAY_RECIBIDO = new ArrayList<>();

    //array para los objetos del dialogo de abrir imagen
    public static final ArrayList<ItemDialogo> LISTA_DIALOGO = new ArrayList<>();

    //constante para darle nombre a la operacion de abrir archivo
    public static final int SELECT_FILE = 1;

}
