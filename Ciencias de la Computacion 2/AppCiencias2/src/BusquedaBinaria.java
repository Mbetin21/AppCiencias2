
import java.util.ArrayList;

/**
 * Algoritmo de búsqueda binaria Dividiendo y tomando la parte exacta al separar
 * por "grupos" que se van reduciendo a la mitad cada vez, hasta encontrar el
 * valor o determinar que no existe
 */
public class BusquedaBinaria {

    public static int buscar(ArrayList<Integer> datos, int valor) {
        int izquierda = 0;
        int derecha = datos.size() - 1;

        while (izquierda <= derecha) {
            int medio = (izquierda + derecha) / 2;

            if (datos.get(medio) == valor) {
                return medio;
            }
            if (datos.get(medio) < valor) {
                izquierda = medio + 1;
            } else {
                derecha = medio - 1;
            }
        }
        return -1;
    }
}
