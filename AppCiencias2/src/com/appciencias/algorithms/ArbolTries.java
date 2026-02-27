package com.appciencias.algorithms;

import com.appciencias.models.ClaveArbol;
import java.util.ArrayList;

/**
 * Arbol simple
 *
 * - La raiz siempre esta vacia (nodo de enlace, nunca guarda dato). - Cuando
 * dos claves comparten bits iniciales, los nodos del camino compartido son Nodo
 * Enlace (clave = null), solo sirven para navegar.
 */
public class ArbolTries {

    public static class Nodo {

        public String clave;      // null = nodo de enlace
        public Nodo izquierda;
        public Nodo derecha;

        public Nodo(String clave) {
            this.clave = clave;
            this.izquierda = null;
            this.derecha = null;
        }

        public boolean esEnlace() {
            return clave == null;
        }
    }

    private Nodo raiz;
    private ArrayList<String> historial; // orden original de insercion
    private int contador;

    public ArbolTries() {
        this.raiz = new Nodo(null); // raiz siempre vacia
        this.historial = new ArrayList<>();
        this.contador = 0;
    }

    /**
     * Inserta una letra en el arbol.
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

        insertarEnArbol(clave);
        historial.add(clave);
        contador++;
    }

    /**
     * Inserta fisicamente en el arbol sin tocar el historial. Usado tanto por
     * insertar() como por reconstruir().
     */
    private void insertarEnArbol(String clave) {
        String bits = ClaveArbol.claveABinario(clave);
        insertarRecursivo(raiz, clave, bits, 0);
    }

    private void insertarRecursivo(Nodo actual, String clave, String bits, int nivel) {
        if (nivel >= bits.length()) {
            throw new IllegalStateException(
                    "No se pudo insertar '" + clave + "': se agotaron los bits.");
        }

        char bit = bits.charAt(nivel);

        if (bit == '1') {
            if (actual.derecha == null) {
                actual.derecha = new Nodo(clave);
            } else if (actual.derecha.esEnlace()) {
                insertarRecursivo(actual.derecha, clave, bits, nivel + 1);
            } else {
                resolverColision(actual, true, clave, bits, nivel);
            }
        } else {
            if (actual.izquierda == null) {
                actual.izquierda = new Nodo(clave);
            } else if (actual.izquierda.esEnlace()) {
                insertarRecursivo(actual.izquierda, clave, bits, nivel + 1);
            } else {
                resolverColision(actual, false, clave, bits, nivel);
            }
        }
    }

    /**
     * Resuelve colision convirtiendo el nodo existente en enlace y bajando
     * ambas claves al siguiente nivel.
     */
    private void resolverColision(Nodo padre, boolean esDerecha,
            String nuevaClave, String nuevosBits, int nivel) {
        Nodo colisionado = esDerecha ? padre.derecha : padre.izquierda;
        String claveVieja = colisionado.clave;
        String bitsViejos = ClaveArbol.claveABinario(claveVieja);

        colisionado.clave = null; // convertir en nodo de enlace

        insertarRecursivo(colisionado, claveVieja, bitsViejos, nivel + 1);
        insertarRecursivo(colisionado, nuevaClave, nuevosBits, nivel + 1);
    }

    /**
     * Busca una letra en el arbol.
     *
     * @return true si existe, false si no
     */
    public boolean buscar(String clave) {
        if (clave == null || clave.isEmpty()) {
            return false;
        }
        ClaveArbol.validar(clave);
        clave = clave.toUpperCase();
        String bits = ClaveArbol.claveABinario(clave);
        return buscarRecursivo(raiz, clave, bits, 0);
    }

    private boolean buscarRecursivo(Nodo actual, String clave, String bits, int nivel) {
        if (actual == null || nivel >= bits.length()) {
            return false;
        }

        char bit = bits.charAt(nivel);
        Nodo siguiente = (bit == '1') ? actual.derecha : actual.izquierda;

        if (siguiente == null) {
            return false;
        }
        if (siguiente.clave != null && siguiente.clave.equals(clave)) {
            return true;
        }
        if (siguiente.esEnlace()) {
            return buscarRecursivo(siguiente, clave, bits, nivel + 1);
        }
        return false;
    }

    /**
     * Elimina una clave y reestructura el arbol completo.
     *
     * Proceso: 1. Verificar que la clave existe. 2. Quita del historial. 3.
     * Reconstruir el arbol desde cero con las claves restantes en el mismo
     * orden original.
     *
     * @throws IllegalArgumentException si la clave no existe
     */
    public void eliminar(String clave) {
        ClaveArbol.validar(clave);
        clave = clave.toUpperCase();

        if (!buscar(clave)) {
            throw new IllegalArgumentException("La clave '" + clave + "' no existe en el arbol.");
        }

        historial.remove(clave);
        contador--;
        reconstruir();
    }

    /**
     * Reconstruye el arbol desde cero usando el historial actual.
     */
    private void reconstruir() {
        raiz = new Nodo(null); // raiz siempre vacia
        for (String c : historial) {
            insertarEnArbol(c);
        }
    }

    /**
     * Retorna la raiz.
     */
    public Nodo getRaiz() {
        return raiz;
    }

    /**
     * Lista de claves en el orden en que fueron insertadas.
     */
    public ArrayList<String> obtenerClaves() {
        return new ArrayList<>(historial);
    }

    /**
     * Recorrido recolectando solo claves reales (sin nodos de enlace).
     */
    public ArrayList<String> obtenerClavesInorden() {
        ArrayList<String> lista = new ArrayList<>();
        recolectar(raiz, lista);
        return lista;
    }

    private void recolectar(Nodo nodo, ArrayList<String> lista) {
        if (nodo == null) {
            return;
        }
        if (nodo.clave != null) {
            lista.add(nodo.clave);
        }
        recolectar(nodo.izquierda, lista);
        recolectar(nodo.derecha, lista);
    }

    /**
     * Info de conversion de una clave.
     */
    public String obtenerInfo(String clave) {
        return ClaveArbol.obtenerInfo(clave);
    }

    public int getContador() {
        return contador;
    }
}
