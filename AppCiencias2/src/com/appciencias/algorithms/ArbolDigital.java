package com.appciencias.algorithms;

import com.appciencias.models.ClaveArbol;
import java.util.ArrayList;

/**
 * Arbol Digital
 *
 * - Cada clave es UN caracter ASCII imprimible.
 * - Se convierte a su valor ASCII (8 bits binarios).
 * - Bit '1' = rama derecha, Bit '0' = rama izquierda.
 * - El arbol tiene hasta 8 niveles.
 */
public class ArbolDigital {

    public static class Nodo {

            public String clave;
        public Nodo izquierda;
        public Nodo derecha;

        public Nodo(String clave) {
            this.clave = clave;
            this.izquierda = null;
            this.derecha = null;
        }
    }

    private Nodo raiz;
    private ArrayList<String> historial; // orden original de insercion
    private int contador;

    public ArbolDigital() {
        this.raiz = null;
        this.historial = new ArrayList<>();
        this.contador = 0;
    }

    /**
     * Inserta un caracter en el arbol digital. Siempre recorre los bits
     * completos.
     *
     * @throws IllegalArgumentException si la clave no es un caracter ASCII valido
     * @throws IllegalStateException si la clave ya existe
     */
    public void insertar(String clave) {
        ClaveArbol.validarASCII(clave);

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
        String bits = ClaveArbol.claveABinarioASCII(clave);

        // Arbol vacio, la clave va en la raiz
        if (raiz == null) {
            raiz = new Nodo(clave);
            return;
        }

        // Recorrer bit a bit para insertar
        Nodo actual = raiz;
        for (int i = 0; i < bits.length(); i++) {
            char bit = bits.charAt(i);
            if (bit == '1') {
                if (actual.derecha == null) {
                    actual.derecha = new Nodo(clave);
                    return;
                }
                actual = actual.derecha;
            } else {
                if (actual.izquierda == null) {
                    actual.izquierda = new Nodo(clave);
                    return;
                }
                actual = actual.izquierda;
            }
        }
        throw new IllegalStateException(
                "No se encontro posicion para '" + clave + "' (arbol lleno en esa rama).");
    }

    /**
     * Busca una letra en el arbol siguiendo sus bits.
     *
     * @return true si existe, false si no
     */
    public boolean buscar(String clave) {
        if (clave == null || clave.isEmpty()) {
            return false;
        }
        ClaveArbol.validarASCII(clave);

        if (raiz == null) {
            return false;
        }
        if (raiz.clave != null && raiz.clave.equals(clave)) {
            return true;
        }

        String bits = ClaveArbol.claveABinarioASCII(clave);
        Nodo actual = raiz;

        for (int i = 0; i < bits.length(); i++) {
            char bit = bits.charAt(i);
            actual = (bit == '1') ? actual.derecha : actual.izquierda;
            if (actual == null) {
                return false;
            }
            if (actual.clave != null && actual.clave.equals(clave)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Elimina una clave y reestructura el arbol completo.
     *
     * Proceso: 1. Verificar que la clave existe. 2. Quitarla del historial de
     * insercion. 3. Reconstruir el arbol desde cero con las claves restantes en
     * el mismo orden original.
     *
     * @throws IllegalArgumentException si la clave no existe
     */
    public void eliminar(String clave) {
        ClaveArbol.validarASCII(clave);

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
        raiz = null;
        for (String c : historial) {
            insertarEnArbol(c);
        }
    }

    /**
     * Retorna la raiz
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
     * Recorrido inorden (para mostrar claves en orden alfabético si se desea)
     * puro desparche si se pone pero quien sabe con que salga ese señor
     */
    public ArrayList<String> obtenerClavesInorden() {
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
     * Info de conversion de una clave.
     */
    public String obtenerInfo(String clave) {
        return ClaveArbol.obtenerInfoASCII(clave);
    }

    public int getContador() {
        return contador;
    }
}
