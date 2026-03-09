package com.appciencias.algorithms;

import com.appciencias.models.ClaveArbol;
import java.util.ArrayList;
import java.util.List;

/**
 * Árbol de Residuos Múltiples.
 *
 * Generalización del árbol Tries donde cada nodo tiene M = 2^n hijos.
 * Los 8 bits del código ASCII se agrupan en grupos de n bits de izquierda
 * a derecha. Funciona como tries por residuos: la raíz siempre queda vacía
 * como nodo de enlace, y cuando dos claves colisionan ambas se empujan al
 * siguiente nivel.
 *
 * Valores válidos de n: 1, 2, 4, 8 (divisores de 8 para simetría).
 */
public class ArbolMultipleResiduo {

    public static class Nodo {

        public String clave;   // null = nodo de enlace
        public Nodo[] hijos;

        public Nodo(int M) {
            this.hijos = new Nodo[M];
            this.clave = null;
        }

        public boolean esEnlace() {
            return clave == null;
        }
    }

    private Nodo raiz;
    private int n;           // bits por nivel
    private int M;           // 2^n hijos por nodo
    private int niveles;     // 8 / n
    private ArrayList<String> historial;
    private int contador;

    /**
     * @param n bits por nivel (1, 2, 4 u 8)
     */
    public ArbolMultipleResiduo(int n) {
        if (n < 1 || n > 8 || 8 % n != 0) {
            throw new IllegalArgumentException(
                    "n debe ser 1, 2, 4 u 8. Se recibió: " + n);
        }
        this.n = n;
        this.M = (int) Math.pow(2, n);
        this.niveles = 8 / n;
        this.raiz = new Nodo(M);
        this.historial = new ArrayList<>();
        this.contador = 0;
    }

    /**
     * Inserta una clave en el árbol.
     *
     * @throws IllegalArgumentException si la clave no es válida
     * @throws IllegalStateException si la clave ya existe
     */
    public void insertar(String clave) {
        ClaveArbol.validar8Bits(clave);

        if (buscar(clave)) {
            throw new IllegalStateException(
                    "La clave '" + clave + "' ya existe en el árbol.");
        }

        insertarEnArbol(clave);
        historial.add(clave);
        contador++;
    }

    /**
     * Inserta físicamente en el árbol sin tocar el historial.
     */
    private void insertarEnArbol(String clave) {
        String bits = ClaveArbol.claveABinario8Bits(clave);
        insertarRecursivo(raiz, clave, bits, 0);
    }

    private void insertarRecursivo(Nodo actual, String clave,
            String bits, int nivel) {
        if (nivel >= niveles) {
            throw new IllegalStateException(
                    "No se pudo insertar '" + clave + "': se agotaron los niveles.");
        }

        int inicio = nivel * n;
        String grupo = bits.substring(inicio, inicio + n);
        int indice = Integer.parseInt(grupo, 2);

        if (actual.hijos[indice] == null) {
            // Posición vacía: crear nodo con dato
            actual.hijos[indice] = new Nodo(M);
            actual.hijos[indice].clave = clave;
        } else if (actual.hijos[indice].esEnlace()) {
            // Nodo de enlace: seguir bajando
            insertarRecursivo(actual.hijos[indice], clave, bits, nivel + 1);
        } else {
            // Colisión: convertir en enlace y bajar ambas claves
            resolverColision(actual.hijos[indice], clave, bits, nivel);
        }
    }

    /**
     * Resuelve colisión convirtiendo el nodo existente en enlace y bajando
     * ambas claves al siguiente nivel.
     */
    private void resolverColision(Nodo colisionado, String nuevaClave,
            String nuevosBits, int nivel) {
        String claveVieja = colisionado.clave;
        String bitsViejos = ClaveArbol.claveABinario8Bits(claveVieja);

        colisionado.clave = null; // convertir en nodo de enlace

        insertarRecursivo(colisionado, claveVieja, bitsViejos, nivel + 1);
        insertarRecursivo(colisionado, nuevaClave, nuevosBits, nivel + 1);
    }

    /**
     * Busca una clave en el árbol.
     *
     * @return true si existe, false si no
     */
    public boolean buscar(String clave) {
        if (clave == null || clave.isEmpty()) {
            return false;
        }
        ClaveArbol.validar8Bits(clave);
        String bits = ClaveArbol.claveABinario8Bits(clave);
        return buscarRecursivo(raiz, clave, bits, 0);
    }

    private boolean buscarRecursivo(Nodo actual, String clave,
            String bits, int nivel) {
        if (actual == null || nivel >= niveles) {
            return false;
        }

        int inicio = nivel * n;
        String grupo = bits.substring(inicio, inicio + n);
        int indice = Integer.parseInt(grupo, 2);

        Nodo siguiente = actual.hijos[indice];
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
     * Elimina una clave y reconstruye el árbol completo.
     *
     * @throws IllegalArgumentException si la clave no existe
     */
    public void eliminar(String clave) {
        ClaveArbol.validar8Bits(clave);

        if (!buscar(clave)) {
            throw new IllegalArgumentException(
                    "La clave '" + clave + "' no existe en el árbol.");
        }

        historial.remove(clave);
        contador--;
        reconstruir();
    }

    /**
     * Reconstruye el árbol desde cero usando el historial actual.
     */
    private void reconstruir() {
        raiz = new Nodo(M);
        for (String c : historial) {
            insertarEnArbol(c);
        }
    }

    public Nodo getRaiz() {
        return raiz;
    }

    public int getN() {
        return n;
    }

    public int getM() {
        return M;
    }

    public int getNiveles() {
        return niveles;
    }

    public int getContador() {
        return contador;
    }

    public ArrayList<String> obtenerClaves() {
        return new ArrayList<>(historial);
    }

    /**
     * Obtiene los grupos de bits para una clave.
     */
    public List<String> obtenerGrupos(String clave) {
        ClaveArbol.validar8Bits(clave);
        String bits = ClaveArbol.claveABinario8Bits(clave);
        List<String> grupos = new ArrayList<>();
        for (int i = 0; i < niveles; i++) {
            int inicio = i * n;
            grupos.add(bits.substring(inicio, inicio + n));
        }
        return grupos;
    }

    /**
     * Info de conversión de una clave.
     */
    public String obtenerInfo(String clave) {
        return ClaveArbol.obtenerInfo8Bits(clave);
    }
}
