package com.appciencias.algorithms;

/**
 * Formula general:
 * D = H(k) = (k mod n) + 1
 *
 * Todas las posiciones visibles van de 1 a n.
 * Internamente se convierten a 0 a n-1.
 */
public class Reasignacion {

    private String[] tabla;
    private int n;

    public Reasignacion(int tamaño) {

        if (tamaño <= 0) {
            throw new IllegalArgumentException("El tamaño debe ser mayor que 0.");
        }

        this.n = tamaño;
        this.tabla = new String[n];
    }

    /**
     * Formula general
     */
    private int funcionHash(long k) {
        return (int) (k % n) + 1; //Segun k%n es el (k mod n)
    }

    /**
     * Reasignacion lineal. D + i donde i = 0 hasta n-1
     */
    public void insertarLineal(String clave) {

        long k = Long.parseLong(clave);
        int D = funcionHash(k);

        for (int i = 0; i < n; i++) {

            int nuevaPos = ((D + i - 1) % n); // conversion a indice real

            if (tabla[nuevaPos] == null) {
                tabla[nuevaPos] = clave;
                return;
            }
        }

        throw new IllegalStateException("Tabla llena.");
    }

    /**
     * Insercion con reasignacion cuadratica. D + i² donde i = 0 hasta n-1
     */
    public void insertarCuadratica(String clave) {

        long k = Long.parseLong(clave);
        int D = funcionHash(k);

        for (int i = 0; i < n; i++) {

            int nuevaPos = ((D + (i * i) - 1) % n);

            if (tabla[nuevaPos] == null) {
                tabla[nuevaPos] = clave;
                return;
            }
        }

        throw new IllegalStateException("Tabla llena.");
    }

    /**
     * Insercion con doble funcion hash.
     *
     * Se recalcula: H(k = D + 1) luego H(k = D' + 1) etc.
     */
    public void insertarDobleHash(String clave) {

        long k = Long.parseLong(clave);
        int D = funcionHash(k);

        for (int i = 0; i < n; i++) {

            int nuevaPos = (D - 1);

            if (tabla[nuevaPos] == null) {
                tabla[nuevaPos] = clave;
                return;
            }

            // recalcula hash usando D + 1
            D = funcionHash(D + 1);
        }

        throw new IllegalStateException("Tabla llena.");
    }

    /**
     * Muestra la tabla completa (Por si es necesario aqui se puede adaptar para
     * mostrar en una UI).
     */
    public void mostrarTabla() {

        for (int i = 0; i < n; i++) {

            System.out.println(
                    "Posicion " + (i + 1) + " → "
                    + (tabla[i] == null ? "[vacio]" : tabla[i])
            );
        }
    }
}
