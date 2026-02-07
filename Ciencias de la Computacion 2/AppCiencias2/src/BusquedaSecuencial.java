
import java.util.ArrayList;

/**
 * Algoritmo de búsqueda secuencial Recorriendo el arreglo desde el inicio hasta
 * el final, comparando cada elemento con el valor buscado, hasta encontrarlo o
 * llegar al final sin encontrarlo
 */
public class BusquedaSecuencial {

    public static int buscar(ArrayList<Integer> datos, int valor) {
        for (int i = 0; i < datos.size(); i++) {
            if (datos.get(i) == valor) {
                return i;
            }
        }
        return -1;
    }
}
