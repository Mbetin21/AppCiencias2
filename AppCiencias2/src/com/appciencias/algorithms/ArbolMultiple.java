package com.appciencias.algorithms;

import com.appciencias.models.ClaveArbol;
import java.util.ArrayList;

/**
 * Arbol multiple
 *
 * Definicion de un m. Cada nodo tiene M = 2^m hijos. Los 5 bits de la clave se
 * agrupan de izquierda a derecha en grupos de m bits. Todos los datos quedan en
 * las hojas
 */
public class ArbolMultiple {

    public static class Nodo {

        public String clave;   // null si es nodo enlace o vacio
        public Nodo[] hijos;
        public int m;       // bits que representa este nivel
        public boolean esHoja;

        public Nodo(int m, boolean esHoja) {
            this.m = m;
            this.esHoja = esHoja;
            this.clave = null;
            this.hijos = new Nodo[(int) Math.pow(2, m)];
        }
    }

    private Nodo raiz;
    private int m;
    private int M;          // 2^m
    private int niveles;
    private int[] bitsNivel;
    private ArrayList<String> historial;  // orden original de insercion
    private int contador;

    /**
     * @param m Cantidad de bits por nivel (1 a 5).
     */
    public ArbolMultiple(int m) {
        if (m < 1 || m > 5) {
            throw new IllegalArgumentException(
                    "m debe estar entre 1 y 5. Se recibio: " + m);
        }
        this.m = m;
        this.M = (int) Math.pow(2, m);
        this.historial = new ArrayList<>();
        this.contador = 0;
        calcularNiveles();
        this.raiz = crearNodo(0);
    }

    private void calcularNiveles() {
        int totalBits = 5;
        int niv = (int) Math.ceil((double) totalBits / m);
        this.niveles = niv;
        this.bitsNivel = new int[niv];
        int restantes = totalBits;
        for (int i = 0; i < niv; i++) {
            bitsNivel[i] = Math.min(m, restantes);
            restantes -= bitsNivel[i];
        }
    }

    private Nodo crearNodo(int nivel) {
        return new Nodo(bitsNivel[nivel], nivel == niveles - 1);
    }

    /**
     * Inserta una letra en el arbol múltiple.
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
     * Inserta en el arbol sin tocar el historial
     */
    private void insertarEnArbol(String clave) {
        String bits = ClaveArbol.claveABinario(clave);
        Nodo actual = raiz;
        int bitPos = 0;

        for (int nivel = 0; nivel < niveles; nivel++) {
            int b = bitsNivel[nivel];
            String grupo = bits.substring(bitPos, bitPos + b);
            bitPos += b;
            int indice = Integer.parseInt(grupo, 2);

            if (nivel == niveles - 1) {
                if (actual.hijos[indice] == null) {
                    actual.hijos[indice] = crearNodo(nivel);
                }
                actual.hijos[indice].clave = clave;
            } else {
                if (actual.hijos[indice] == null) {
                    actual.hijos[indice] = crearNodo(nivel + 1);
                }
                actual = actual.hijos[indice];
            }
        }
    }

    /**
     * Busca una letra en el arbol
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
        Nodo actual = raiz;
        int bitPos = 0;

        for (int nivel = 0; nivel < niveles; nivel++) {
            int b = bitsNivel[nivel];
            String grupo = bits.substring(bitPos, bitPos + b);
            bitPos += b;
            int indice = Integer.parseInt(grupo, 2);

            if (actual.hijos[indice] == null) {
                return false;
            }

            if (nivel == niveles - 1) {
                return clave.equals(actual.hijos[indice].clave);
            }
            actual = actual.hijos[indice];
        }
        return false;
    }

    /**
     * Elimina una clave y rehace el arbol completo
     *
     * Proceso: 1. Verificar que la clave existe. 2. Quitarla del historial. 3.
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
     * Reconstruye el arbol desde cero usando el historial actual
     */
    private void reconstruir() {
        raiz = crearNodo(0);
        for (String c : historial) {
            insertarEnArbol(c);
        }
    }

    /**
     * Retorna la raíz (si lo necesita el front, si no vale madres)
     */
    public Nodo getRaiz() {
        return raiz;
    }

    /**
     * Lista de claves en el orden en que fueron insertadas
     */
    public ArrayList<String> obtenerClaves() {
        return new ArrayList<>(historial);
    }

    /**
     * Retorna los grupos de bits de una clave
     */
    public ArrayList<String> obtenerGrupos(String clave) {
        ClaveArbol.validar(clave);
        String bits = ClaveArbol.claveABinario(clave);
        ArrayList<String> grupos = new ArrayList<>();
        int bitPos = 0;
        for (int nivel = 0; nivel < niveles; nivel++) {
            int b = bitsNivel[nivel];
            grupos.add(bits.substring(bitPos, bitPos + b));
            bitPos += b;
        }
        return grupos;
    }

    /**
     * Info de conversion de una clave
     */
    public String obtenerInfo(String clave) {
        return ClaveArbol.obtenerInfo(clave);
    }

    public int getM() {
        return m;
    }

    public int getM_hijos() {
        return M;
    }

    public int getNiveles() {
        return niveles;
    }

    public int getContador() {
        return contador;
    }

    public int[] getBitsNivel() {
        return bitsNivel.clone();
    }
}
