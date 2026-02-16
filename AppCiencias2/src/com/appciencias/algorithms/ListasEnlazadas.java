
import java.util.LinkedList;

public class ListasEnlazadas {

    private LinkedList<String>[] tabla;
    private int n;

    public ListasEnlazadas(int tamaño) {

        if (tamaño <= 0) {
            throw new IllegalArgumentException("Tamaño invalido.");
        }

        this.n = tamaño;
        this.tabla = new LinkedList[n];

        for (int i = 0; i < n; i++) {
            tabla[i] = new LinkedList<>();
        }
    }

    private int hash(long k) {
        return (int) (k % n) + 1;
    }

    public void insertar(String clave) {

        long k = Long.parseLong(clave);
        int D = hash(k) - 1;

        tabla[D].add(clave);
    }
}
