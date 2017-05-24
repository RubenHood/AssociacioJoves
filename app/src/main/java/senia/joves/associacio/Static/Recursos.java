package senia.joves.associacio.Static;

import java.util.ArrayList;

import senia.joves.associacio.entidades.Noticia;
import senia.joves.associacio.entidades.Socio;

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

    //array donde guardo las urls de cada imagen
    public static ArrayList<Noticia> LISTA_URL_IMAGENES = null;



}
