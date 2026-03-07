package com.appciencias.algorithms;

import com.appciencias.models.*;
import java.util.ArrayList;

/**
 * Externa Secuencial SIN ORDEN
 *
 * El archivo se divide en bloques de tamaño TB = (int) sqrt(N).
 *
 * Busqueda: recorre bloque por bloque, dentro de cada bloque compara elemento a
 * elemento hasta encontrar la clave o llegar al final.
 */
public class SecuencialExterno {

    // ── Resultado de búsqueda ────────────────────────────────────────────────
    public static class ResultadoBusqueda {

        public final int numBloque;        // 1-based, -1 si no encontrada
        public final int posEnBloque;      // 1-based dentro del bloque, -1 si no encontrada
        public final int bloquesVisitados;
        public final int comparaciones;
        public final boolean encontrada;

        public ResultadoBusqueda(int numBloque, int posEnBloque,
                int bloquesVisitados, int comparaciones,
                boolean encontrada) {
            this.numBloque = numBloque;
            this.posEnBloque = posEnBloque;
            this.bloquesVisitados = bloquesVisitados;
            this.comparaciones = comparaciones;
            this.encontrada = encontrada;
        }

        @Override
        public String toString() {
            if (!encontrada) {
                return "Clave no encontrada. Bloques visitados: " + bloquesVisitados
                        + ", comparaciones: " + comparaciones + ".";
            }
            return "Encontrada en bloque " + numBloque
                    + ", posicion " + posEnBloque
                    + ". Bloques visitados: " + bloquesVisitados
                    + ", comparaciones: " + comparaciones + ".";
        }
    }

    private final int N;
    private final int TB;
    private final int numBloques;
    private final int longClave;

    /**
     * bloques.get(i) = claves del bloque i, en orden de insercion (sin
     * ordenar).
     */
    private final ArrayList<ArrayList<String>> bloques;

    private int contador;

    /**
     * @param N Número máximo de registros.
     * @param longClave Cantidad de caracteres por clave.
     */
    public SecuencialExterno(int N, int longClave) {
        if (N <= 0) {
            throw new IllegalArgumentException("N debe ser mayor que 0.");
        }
        if (longClave <= 0) {
            throw new IllegalArgumentException("La longitud de clave debe ser mayor que 0.");
        }

        this.N = N;
        this.TB = (int) Math.sqrt(N);
        this.numBloques = (int) Math.ceil((double) N / TB);
        this.longClave = longClave;
        this.contador = 0;

        this.bloques = new ArrayList<>();
        for (int i = 0; i < numBloques; i++) {
            bloques.add(new ArrayList<>());
        }
    }

    /**
     * Inserta la clave en el primer bloque que tenga espacio disponible.
     *
     * @throws IllegalStateException si el archivo está lleno o la clave ya
     * existe.
     * @throws IllegalArgumentException si la longitud de clave es incorrecta.
     */
    public void insertar(String clave) {
        ClaveUtil.validar(clave, longClave);

        if (contador >= N) {
            throw new IllegalStateException(
                    "El archivo esta lleno. Capacidad maxima: " + N + " registros.");
        }
        if (buscar(clave).encontrada) {
            throw new IllegalStateException(
                    "La clave '" + clave + "' ya existe.");
        }

        // Primer bloque con espacio libre
        for (ArrayList<String> bloque : bloques) {
            if (bloque.size() < TB) {
                bloque.add(clave);
                contador++;
                return;
            }
        }

        throw new IllegalStateException("No se encontro espacio disponible.");
    }

    /**
     * Busca la clave recorriendo bloque por bloque, elemento a elemento. No
     * asume ningun orden VA A REVISAR TODO hasta encontrar o terminar.
     */
    public ResultadoBusqueda buscar(String clave) {
        if (clave == null || clave.isEmpty()) {
            return new ResultadoBusqueda(-1, -1, 0, 0, false);
        }

        int bloquesVisitados = 0;
        int comparaciones = 0;

        for (int b = 0; b < bloques.size(); b++) {
            ArrayList<String> bloque = bloques.get(b);

            if (bloque.isEmpty()) {
                continue;
            }

            bloquesVisitados++;

            for (int j = 0; j < bloque.size(); j++) {
                comparaciones++;
                if (bloque.get(j).equals(clave)) {
                    return new ResultadoBusqueda(b + 1, j + 1,
                            bloquesVisitados, comparaciones, true);
                }
            }
        }

        return new ResultadoBusqueda(-1, -1, bloquesVisitados, comparaciones, false);
    }

    /**
     * Elimina la clave. El hueco se cierra en el bloque subiendo los elementos.
     *
     * @throws IllegalArgumentException si la clave no existe.
     */
    public void eliminar(String clave) {
        ClaveUtil.validar(clave, longClave);

        ResultadoBusqueda r = buscar(clave);
        if (!r.encontrada) {
            throw new IllegalArgumentException(
                    "La clave '" + clave + "' no existe.");
        }

        bloques.get(r.numBloque - 1).remove(clave);
        contador--;
    }

    /*Usos para el front si los necesitas */
    public ArrayList<ArrayList<String>> obtenerBloques() {
        ArrayList<ArrayList<String>> copia = new ArrayList<>();
        for (ArrayList<String> b : bloques) {
            copia.add(new ArrayList<>(b));
        }
        return copia;
    }

    /**
     * Resumen
     */
    public String obtenerInfo() {
        return "N=" + N + "  TB=" + TB + "  Bloques=" + numBloques
                + "  Registros actuales=" + contador;
    }

    public int getN() {
        return N;
    }

    public int getTB() {
        return TB;
    }

    public int getNumBloques() {
        return numBloques;
    }

    public int getLongClave() {
        return longClave;
    }

    public int getContador() {
        return contador;
    }

    public boolean estaLleno() {
        return contador >= N;
    }
}
