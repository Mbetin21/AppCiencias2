package com.appciencias.algorithms;

import com.appciencias.models.ClaveUtil;
import java.util.ArrayList;

/**
 * Busqueda Externa Binaria (por bloques, CON ordenamiento).
 *
 * El archivo se divide en bloques de tamaño TB = (int) sqrt(N). Los datos se
 * mantienen ORDENADOS igual que el Binario interno.
 *
 * Búsqueda por bloques: Para cada bloque compara la clave con el ultimo
 * elemento: - Si clave <= ultimo del bloque -> entra al bloque y busca uno a
 * uno. - Si clave > ultimo del bloque -> pasa al siguiente bloque. - Si paso
 * todos los bloques sin entrar -> no existe.
 */
public class BinarioExterno {

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
                    + ", posición " + posEnBloque
                    + ". Bloques visitados: " + bloquesVisitados
                    + ", comparaciones: " + comparaciones + ".";
        }
    }

    private final int N;
    private final int TB;
    private final int numBloques;
    private final int longClave;

    /**
     * bloques.get(i) = claves del bloque i, siempre ordenadas.
     */
    private final ArrayList<ArrayList<String>> bloques;

    private int contador;

    /**
     * @param N Número máximo de registros.
     * @param longClave Cantidad de caracteres por clave.
     */
    public BinarioExterno(int N, int longClave) {
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
     * Inserta la clave manteniendo el orden global, igual que el Binario
     * interno.
     *
     * @throws IllegalStateException si el archivo está lleno o la clave ya
     * existe.
     * @throws IllegalArgumentException si la longitud de clave es incorrecta.
     */
    public void insertar(String clave) {
        ClaveUtil.validar(clave, longClave);

        if (contador >= N) {
            throw new IllegalStateException(
                    "El archivo está lleno. Capacidad máxima: " + N + " registros.");
        }
        if (buscar(clave).encontrada) {
            throw new IllegalStateException(
                    "La clave '" + clave + "' ya existe.");
        }

        // Obtener lista plana ordenada e insertar en posicion correcta
        ArrayList<String> plana = obtenerListaPlana();
        int pos = posicionDeInsercion(plana, clave);
        plana.add(pos, clave);
        contador++;

        redistribuirEnBloques(plana);
    }

    /**
     * Busca la clave usando la logica de bloques ordenados: Compara con el
     * ÚLTIMO elemento de cada bloque. Si clave <= último -> entra al bloque y
     * busca uno a uno. Si clave > último -> pasa al siguiente bloque.
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
            String ultimo = bloque.get(bloque.size() - 1);
            comparaciones++;

            if (clave.compareTo(ultimo) <= 0) {
                // La clave podria estar en este bloque -> buscar uno a uno
                for (int j = 0; j < bloque.size(); j++) {
                    comparaciones++;
                    if (bloque.get(j).equals(clave)) {
                        return new ResultadoBusqueda(b + 1, j + 1,
                                bloquesVisitados, comparaciones, true);
                    }
                }
                // Entro al bloque pero no la encontro -> no existe
                return new ResultadoBusqueda(-1, -1, bloquesVisitados, comparaciones, false);
            }
            // clave > ultimo -> continuar al siguiente bloque
        }

        return new ResultadoBusqueda(-1, -1, bloquesVisitados, comparaciones, false);
    }

    /**
     * Elimina la clave y organiza los bloques manteniendo el orden
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

        ArrayList<String> plana = obtenerListaPlana();
        plana.remove(clave);
        contador--;

        redistribuirEnBloques(plana);
    }

    /**
     * Devuelve todos los registros en una lista plana ordenada.
     */
    private ArrayList<String> obtenerListaPlana() {
        ArrayList<String> plana = new ArrayList<>();
        for (ArrayList<String> bloque : bloques) {
            plana.addAll(bloque);
        }
        return plana;
    }

    /**
     * Encuentra la posición de inserción usando busqueda binaria sobre la lista
     */
    private int posicionDeInsercion(ArrayList<String> lista, String clave) {
        int izq = 0, der = lista.size();
        while (izq < der) {
            int mid = (izq + der) / 2;
            if (lista.get(mid).compareTo(clave) < 0) {
                izq = mid + 1;
            } else {
                der = mid;
            }
        }
        return izq;
    }

    /**
     * Redistribuye la lista plana ordenada en los bloques de tamaño TB.
     */
    private void redistribuirEnBloques(ArrayList<String> plana) {
        for (ArrayList<String> bloque : bloques) {
            bloque.clear();
        }
        int idx = 0;
        for (int b = 0; b < numBloques && idx < plana.size(); b++) {
            for (int j = 0; j < TB && idx < plana.size(); j++) {
                bloques.get(b).add(plana.get(idx++));
            }
        }
    }

    /**
     * Copia de los bloques
     */
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
