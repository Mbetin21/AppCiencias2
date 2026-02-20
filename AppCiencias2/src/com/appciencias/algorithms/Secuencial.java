package com.appciencias.algorithms;

import java.util.ArrayList;

/**
 * Búsqueda Secuencial con claves alfanuméricas. Modos: ORDENADO -> Se detiene
 * temprano si el elemento actual ya es mayor al buscado. NO ORDENADO -> Recorre
 * todo hasta encontrar o llegar al final.
 */
public class Secuencial {

    private String[] tabla;
    private int tamaño;       // tamaño maximo definido por el usuario
    private int longClave;    // cantidad de caracteres por clave
    private int contador;     // elementos actuales
    private boolean ordenado; // si el arreglo se mantiene ordenado

    /**
     * @param tamaño Tamaño maximo del arreglo
     * @param longClave Cantidad de caracteres por clave recibe las 3 formas
     * @param ordenado true = mantener ordenado alfabéticamente
     */
    public Secuencial(int tamaño, int longClave, boolean ordenado) {
        if (tamaño <= 0) {
            throw new IllegalArgumentException("El tamaño debe ser mayor que 0.");
        }
        if (longClave <= 0) {
            throw new IllegalArgumentException("La longitud de clave debe ser mayor que 0.");
        }
        this.tamaño = tamaño;
        this.longClave = longClave;
        this.ordenado = ordenado;
        this.tabla = new String[tamaño];
        this.contador = 0;
    }

    /**
     * Inserta una clave en el arreglo. Si está ordenado, la ubica en la
     * posición correcta.
     *
     * @throws IllegalStateException si el arreglo está lleno o la clave ya
     * existe
     * @throws IllegalArgumentException si la clave tiene longitud incorrecta
     */
    public void insertar(String clave) {
        ClaveUtil.validar(clave, longClave);
        if (contador >= tamaño) {
            throw new IllegalStateException("El arreglo está lleno. Capacidad máxima: " + tamaño + ".");
        }
        if (buscar(clave) != -1) {
            throw new IllegalStateException("La clave '" + clave + "' ya existe en el arreglo.");
        }

        tabla[contador] = clave;
        contador++;

        if (ordenado) {
            for (int i = contador - 1; i > 0 && tabla[i].compareTo(tabla[i - 1]) < 0; i--) {
                String temp = tabla[i];
                tabla[i] = tabla[i - 1];
                tabla[i - 1] = temp;
            }
        }
    }

    /**
     * Busca una clave en el arreglo.
     *
     * @return Índice (base 0) donde está, o -1 si no existe
     */
    public int buscar(String clave) {
        if (clave == null) {
            return -1;
        }
        return ordenado ? buscarOrdenado(clave) : buscarNoOrdenado(clave);
    }

    private int buscarNoOrdenado(String clave) {
        for (int i = 0; i < contador; i++) {
            if (tabla[i] != null && tabla[i].equals(clave)) {
                return i;
            }
        }
        return -1;
    }

    private int buscarOrdenado(String clave) {
        for (int i = 0; i < contador; i++) {
            if (tabla[i] == null) {
                continue;
            }
            int cmp = tabla[i].compareTo(clave);
            if (cmp == 0) {
                return i;
            }
            if (cmp > 0) {
                return -1; // ya pasamos el punto donde deberia estar

            }
        }
        return -1;
    }

    /**
     * Elimina una clave y cierra el espacio corriendo los elementos.
     *
     * @throws IllegalArgumentException si no existe
     */
    public void eliminar(String clave) {
        int pos = buscar(clave);
        if (pos == -1) {
            throw new IllegalArgumentException("La clave '" + clave + "' no existe en el arreglo.");
        }
        for (int i = pos; i < contador - 1; i++) {
            tabla[i] = tabla[i + 1];
        }
        tabla[contador - 1] = null;
        contador--;
    }

    // ── DATOS ─────────────────────────────────────────────────────
    /**
     * Arreglo completo (posiciones vacías = null por si la va a usar Front)
     */
    public String[] obtenerTabla() {
        return tabla.clone();
    }

    /**
     * Solo los elementos activos (sin nulos).
     */
    public ArrayList<String> obtenerDatos() {
        ArrayList<String> lista = new ArrayList<>();
        for (int i = 0; i < contador; i++) {
            lista.add(tabla[i]);
        }
        return lista;
    }

    public int getTamaño() {
        return tamaño;
    }

    public int getLongClave() {
        return longClave;
    }

    public int getContador() {
        return contador;
    }

    public boolean isOrdenado() {
        return ordenado;
    }

    public boolean estaLleno() {
        return contador >= tamaño;
    }
}
