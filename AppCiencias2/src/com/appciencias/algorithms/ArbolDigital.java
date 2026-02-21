package com.appciencias.algorithms;

import com.appciencias.models.ClaveArbol;
import java.util.ArrayList;

/**
 * Arbol digital - Cada clave es UNA letra (a-z). - Conversión: letra, posición
 * alfabética (a=1…z=26), 5 bits binarios. Bit '1' = rama derecha, Bit '0' =
 * rama izquierda.
 *
 * clave → letra guardada (null = nodo vacío / no existe aún) izquierda → hijo
 * por bit '0' derecha → hijo por bit '1'
 */
public class ArbolDigital {

    public static class Nodo {

        public String clave;       // null = nodo vacio
        public Nodo izquierda;
        public Nodo derecha;

        public Nodo(String clave) {
            this.clave = clave;
            this.izquierda = null;
            this.derecha = null;
        }
    }

    private Nodo raiz;
    private int contador; // claves insertadas

    public ArbolDigital() {
        this.raiz = null;
        this.contador = 0;
    }

    // ── INSERTAR ─────────────────────────────────────────────────────────────
    /**
     * Inserta una letra en el árbol digital.
     *
     * @throws IllegalArgumentException si la clave no es una letra a-z
     * @throws IllegalStateException si la clave ya existe
     */
    public void insertar(String clave) {
        ClaveArbol.validar(clave);
        clave = clave.toUpperCase();

        if (buscar(clave)) {
            throw new IllegalStateException("La clave '" + clave + "' ya existe en el arbol.");
        }

        String bits = ClaveArbol.claveABinario(clave); // 5 bits

        // CASO 1: arbol vacio enonces la clave va en la raiz
        if (raiz == null) {
            raiz = new Nodo(clave);
            contador++;
            return;
        }

        // CASO 2: raiz vacia (existe el nodo pero sin dato) → guardar aqui
        if (raiz.clave == null) {
            raiz.clave = clave;
            contador++;
            return;
        }

        // CASO 3: raiz ocupada, recorrer el arbol bit a bit
        Nodo actual = raiz;
        for (int i = 0; i < bits.length(); i++) {
            char bit = bits.charAt(i);
            if (bit == '1') {
                // Ir a la derecha
                if (actual.derecha == null) {
                    actual.derecha = new Nodo(clave);
                    contador++;
                    return;
                }
                if (actual.derecha.clave == null) {
                    actual.derecha.clave = clave;
                    contador++;
                    return;
                }
                actual = actual.derecha;
            } else {
                // Ir a la izquierda
                if (actual.izquierda == null) {
                    actual.izquierda = new Nodo(clave);
                    contador++;
                    return;
                }
                if (actual.izquierda.clave == null) {
                    actual.izquierda.clave = clave;
                    contador++;
                    return;
                }
                actual = actual.izquierda;
            }
        }
        throw new IllegalStateException(
                "No se encontro posicion para '" + clave + "' (arbol lleno en esa rama).");
    }

    /**
     * Busca una letra en el arbol siguiendo los mismos bits que insertar.
     *
     * @return true si existe, false si no
     */
    public boolean buscar(String clave) {
        return buscarNodo(clave) != null;
    }

    /**
     * Retorna el nodo donde esta la clave, o null si no existe. Principalmente
     * esto es para eliminar.
     */
    private Nodo buscarNodo(String clave) {
        ClaveArbol.validar(clave);
        clave = clave.toUpperCase();

        if (raiz == null) {
            return null;
        }

        // Verificar raíz
        if (raiz.clave != null && raiz.clave.equals(clave)) {
            return raiz;
        }

        String bits = ClaveArbol.claveABinario(clave);
        Nodo actual = raiz;

        for (int i = 0; i < bits.length(); i++) {
            char bit = bits.charAt(i);
            if (bit == '1') {
                actual = actual.derecha;
            } else {
                actual = actual.izquierda;
            }
            if (actual == null) {
                return null;
            }
            if (actual.clave != null && actual.clave.equals(clave)) {
                return actual;
            }
        }
        return null;
    }

    /**
     * Elimina una clave del arbol. El nodo queda con clave = null (nodo vacio),
     * las ramas se conservan para no romper el camino de otras claves.
     *
     * @throws IllegalArgumentException si la clave no existe
     */
    public void eliminar(String clave) {
        Nodo nodo = buscarNodo(clave);
        if (nodo == null) {
            throw new IllegalArgumentException("La clave '" + clave.toUpperCase() + "' no existe.");
        }
        nodo.clave = null;
        contador--;
    }

    /**
     * Retorna la raíz del arbol.
     */
    public Nodo getRaiz() {
        return raiz;
    }

    /**
     * Lista de todas las claves activas en orden de insercion (recorrido
     * inorden).
     */
    public ArrayList<String> obtenerClaves() {
        ArrayList<String> lista = new ArrayList<>();
        inorden(raiz, lista);
        return lista;
    }

    private void inorden(Nodo nodo, ArrayList<String> lista) {
        if (nodo == null) {
            return;
        }
        inorden(nodo.izquierda, lista);
        if (nodo.clave != null) {
            lista.add(nodo.clave);
        }
        inorden(nodo.derecha, lista);
    }

    /**
     * Retorna la representacion binaria de una clave
     */
    public String obtenerInfo(String clave) {
        return ClaveArbol.obtenerInfo(clave);
    }

    public int getContador() {
        return contador;
    }
}
