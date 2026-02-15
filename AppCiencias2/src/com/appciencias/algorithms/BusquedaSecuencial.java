package com.appciencias.algorithms;

import java.util.ArrayList;

/**
 * Algoritmo de búsqueda secuencial. Recorriendo el arreglo desde el inicio hasta
 * el final, comparando cada elemento con el valor buscado, hasta encontrarlo o
 * llegar al final sin encontrarlo.
 * 
 * Complejidad de tiempo: O(n)
 * Complejidad espacial: O(1)
 */
public class BusquedaSecuencial {

    /**
     * Realiza una búsqueda secuencial en una lista de enteros.
     * 
     * @param datos Lista de datos donde buscar
     * @param valor Valor a buscar
     * @return Índice del elemento si lo encuentra, -1 si no existe
     */
    public static int buscar(ArrayList<Integer> datos, int valor) {
        for (int i = 0; i < datos.size(); i++) {
            if (datos.get(i) == valor) {
                return i;
            }
        }
        return -1;
    }
}
