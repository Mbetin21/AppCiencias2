package com.appciencias.algorithms;

import java.util.ArrayList;

/**
 * Búsqueda Binaria con claves alfanuméricas.
 */
public class Binario {

    private String[] tabla;
    private int tamaño;
    private int longClave;
    private int contador;

    /**
     * @param tamaño Tamaño máximo del arreglo
     * @param longClave Cantidad de caracteres por clave
     */
    public Binario(int tamaño, int longClave) {
        if (tamaño <= 0) {
            throw new IllegalArgumentException("El tamaño debe ser mayor que 0.");
        }
        if (longClave <= 0) {
            throw new IllegalArgumentException("La longitud de clave debe ser mayor que 0.");
        }
        this.tamaño = tamaño;
        this.longClave = longClave;
        this.tabla = new String[tamaño];
        this.contador = 0;
    }

    /**
     * Inserta la clave manteniendo el arreglo ordenado. Usa búsqueda binaria
     * para encontrar la posición correcta de inserción.
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

        // Encontrar posición correcta con búsqueda binaria
        int pos = posicionDeInsercion(clave);

        // Correr elementos a la derecha para hacer espacio
        for (int i = contador; i > pos; i--) {
            tabla[i] = tabla[i - 1];
        }
        tabla[pos] = clave;
        contador++;
    }

    /**
     * Encuentra el indice donde debe insertarse la clave para mantener el
     * orden.
     */
    private int posicionDeInsercion(String clave) {
        int izq = 0, der = contador;
        while (izq < der) {
            int mid = (izq + der) / 2;
            if (tabla[mid].compareTo(clave) < 0) {
                izq = mid + 1;
            } else {
                der = mid;
            }
        }
        return izq;
    }

    /**
     * Búsqueda binaria en el arreglo ordenado.
     *
     * @return Índice (base 0) donde está, o -1 si no existe
     */
    public int buscar(String clave) {
        if (clave == null) {
            return -1;
        }
        int izq = 0, der = contador - 1;
        while (izq <= der) {
            int mid = (izq + der) / 2;
            int cmp = tabla[mid].compareTo(clave);
            if (cmp == 0) {
                return mid;
            } else if (cmp < 0) {
                izq = mid + 1;
            } else {
                der = mid - 1;
            }
        }
        return -1;
    }

    /**
     * Elimina una clave y cierra el espacio. El arreglo sigue ordenado tras la
     * eliminación.
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

    /**
     * Arreglo completo (posiciones vacías = null por como se vaya ausar en
     * front)
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

    public boolean estaLleno() {
        return contador >= tamaño;
    }
}
