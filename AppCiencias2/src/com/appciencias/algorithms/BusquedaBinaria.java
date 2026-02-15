package com.appciencias.algorithms;

import java.util.ArrayList;

/**
 * Algoritmo de búsqueda binaria. Divide el arreglo a la mitad repetidamente 
 * hasta encontrar el elemento. Requiere que el arreglo esté previamente ordenado.
 * 
 * Complejidad de tiempo: O(log n)
 * Complejidad espacial: O(1)
 */
public class BusquedaBinaria {

    /**
     * Realiza una búsqueda binaria en una lista de enteros ordenada.
     * 
     * @param datos Lista de datos ordenada donde buscar
     * @param valor Valor a buscar
     * @return Índice del elemento si lo encuentra, -1 si no existe
     */
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
