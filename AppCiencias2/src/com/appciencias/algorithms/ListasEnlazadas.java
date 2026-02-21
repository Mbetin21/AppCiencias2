package com.appciencias.algorithms;

import java.util.ArrayList;

import com.appciencias.models.ClaveUtil;

/**
 * Solucion dee colisiones por listas enlazadas.
 */
public class ListasEnlazadas {

    /**
     * Nodo de la lista encadenada que sale de una posición de la tabla. Estos
     * nodos no tienen numero de posicion, son solo clave + puntero.
     */
    public static class NodoLista {

        public String clave;
        public NodoLista siguiente;

        public NodoLista(String clave) {
            this.clave = clave;
            this.siguiente = null;
        }
    }

    private String[] tabla;   // clave principal de cada posición (null = vacia)
    private NodoLista[] cadena;  // primer nodo de la lista encadenada de cada posicion
    private int n;
    private int longClave;
    private int contador; // total de claves activas (tabla + cadenas)

    /**
     * @param tamaño Numero de posiciones de la tabla
     * @param longClave Cantidad de caracteres por clave
     */
    public ListasEnlazadas(int tamaño, int longClave) {
        if (tamaño <= 0) {
            throw new IllegalArgumentException("El tamaño debe ser mayor que 0.");
        }
        if (longClave <= 0) {
            throw new IllegalArgumentException("La longitud de clave debe ser mayor que 0.");
        }
        this.n = tamaño;
        this.longClave = longClave;
        this.contador = 0;
        this.tabla = new String[n];    // null = vacia
        this.cadena = new NodoLista[n]; // null = sin cadena
    }

    /**
     * H(k) = (k mod n) + 1 Internamente se usa indice 0-based (posiciin - 1).
     */
    private int hash(String clave) {
        long k = ClaveUtil.aNumero(clave);
        return (int) (k % n); // 0-based
    }

    /**
     * Inserta una clave.
     *
     * @throws IllegalStateException si la clave ya existe
     * @throws IllegalArgumentException si la longitud de la clave es incorrecta
     */
    public void insertar(String clave) {
        ClaveUtil.validar(clave, longClave);
        if (buscar(clave) != null) {
            throw new IllegalStateException("La clave '" + clave + "' ya existe.");
        }

        int idx = hash(clave);

        if (tabla[idx] == null) {
            // Posicion base libre entonces insertar directamente en la tabla
            tabla[idx] = clave;
        } else {
            // COLISION
            NodoLista nuevo = new NodoLista(clave);
            if (cadena[idx] == null) {
                // Primera colision en esta posicion
                cadena[idx] = nuevo;
            } else {
                // Ya hay cadena va y llegar al ultimo nodo
                NodoLista actual = cadena[idx];
                while (actual.siguiente != null) {
                    actual = actual.siguiente;
                }
                actual.siguiente = nuevo;
            }
        }
        contador++;
    }

    /**
     * Busca una clave.
     *
     * @return ResultadoBusqueda con info de dónde se encontro, o null si no
     * existe
     */
    public ResultadoBusqueda buscar(String clave) {
        if (clave == null || clave.isEmpty()) {
            return null;
        }

        int idx = hash(clave);

        // Posición base vacia
        if (tabla[idx] == null) {
            return null;
        }

        // ¿Está en la posicion base?
        if (tabla[idx].equals(clave)) {
            return new ResultadoBusqueda(idx + 1, 0, true);
        }

        // Buscar en la cadena
        NodoLista actual = cadena[idx];
        int saltos = 1;
        while (actual != null) {
            if (actual.clave.equals(clave)) {
                return new ResultadoBusqueda(idx + 1, saltos, false);
            }
            actual = actual.siguiente;
            saltos++;
        }
        return null; // no encontrada
    }

    public static class ResultadoBusqueda {

        public final int posicionBase;  // posición 1-based de la tabla (H(k))
        public final int saltosEnCadena; // 0 = estaba en la tabla, 1+ = en la cadena
        public final boolean enTabla;   // true = en la celda de la tabla, false = en la cadena

        public ResultadoBusqueda(int posicionBase, int saltosEnCadena, boolean enTabla) {
            this.posicionBase = posicionBase;
            this.saltosEnCadena = saltosEnCadena;
            this.enTabla = enTabla;
        }

        @Override
        public String toString() {
            if (enTabla) {
                return "Encontrada en posicion " + posicionBase + " de la tabla (sin colision).";
            }
            return "Encontrada en la cadena de la posicion " + posicionBase
                    + " (nodo " + saltosEnCadena + " de la cadena).";
        }
    }

    /**
     * Elimina una clave manteniendo la integridad de las cadenas.
     *
     * CASO 1 — La clave esta en la tabla (posicion base): a. Si no hay cadena →
     * poner null. FIN. b. Si hay cadena → subir el primer nodo de la cadena a
     * la tabla y quitar ese nodo de la cadena (para que la posición base
     * siempre tenga la primera clave que hashea ahí).
     *
     * CASO 2 — La clave esta en la cadena: a. Re-encadenar: el nodo anterior
     * apunta al siguiente del nodo a eliminar. b. El nodo eliminado queda fuera
     * de la cadena (el GC lo recoge).
     *
     * @throws IllegalArgumentException si la clave no existe
     */
    public void eliminar(String clave) {
        ClaveUtil.validar(clave, longClave);

        int idx = hash(clave);

        if (tabla[idx] == null) {
            throw new IllegalArgumentException("La clave '" + clave + "' no existe.");
        }

        // CASO 1: está en la posicion base de la tabla
        if (tabla[idx].equals(clave)) {
            if (cadena[idx] == null) {
                // Sin cadena, solo vaciar la celda
                tabla[idx] = null;
            } else {
                // Hay cadena, subir el primer nodo de la cadena a la tabla
                tabla[idx] = cadena[idx].clave;
                cadena[idx] = cadena[idx].siguiente;
            }
            contador--;
            return;
        }

        // CASO 2: est en algun nodo de la cadena
        if (cadena[idx] == null) {
            throw new IllegalArgumentException("La clave '" + clave + "' no existe.");
        }

        // Si el primer nodo de la cadena es el que buscamos
        if (cadena[idx].clave.equals(clave)) {
            cadena[idx] = cadena[idx].siguiente;
            contador--;
            return;
        }

        // Buscar en el resto de la cadena
        NodoLista anterior = cadena[idx];
        NodoLista actual = cadena[idx].siguiente;
        while (actual != null) {
            if (actual.clave.equals(clave)) {
                anterior.siguiente = actual.siguiente; // reencadenar
                contador--;
                return;
            }
            anterior = actual;
            actual = actual.siguiente;
        }

        throw new IllegalArgumentException("La clave '" + clave + "' no existe.");
    }

    /**
     * Retorna la tabla completa para mostrar si lo vas a usar
     */
    public ArrayList<FilaTabla> obtenerTabla() {
        ArrayList<FilaTabla> filas = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            ArrayList<String> nodosCadena = new ArrayList<>();
            NodoLista actual = cadena[i];
            while (actual != null) {
                nodosCadena.add(actual.clave);
                actual = actual.siguiente;
            }
            filas.add(new FilaTabla(i + 1, tabla[i], nodosCadena));
        }
        return filas;
    }

    /**
     * Representa una fila (posicion) si lo usas *
     */
    public static class FilaTabla {

        public final int posicion;           // numero de posicion (1-based)
        public final String claveBase;       // clave en la celda de la tabla (null = vacia)
        public final ArrayList<String> cadena; // nodos encadenados (puede estar vacia)

        public FilaTabla(int posicion, String claveBase, ArrayList<String> cadena) {
            this.posicion = posicion;
            this.claveBase = claveBase;
            this.cadena = cadena;
        }

        public boolean estaVacia() {
            return claveBase == null;
        }

        public boolean tieneCadena() {
            return !cadena.isEmpty();
        }
    }

    /**
     * Lista plana de todas las claves activas (tabla + cadenas).
     */
    public ArrayList<String> obtenerClavesActivas() {
        ArrayList<String> lista = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (tabla[i] != null) {
                lista.add(tabla[i]);
            }
            NodoLista actual = cadena[i];
            while (actual != null) {
                lista.add(actual.clave);
                actual = actual.siguiente;
            }
        }
        return lista;
    }

    /**
     * Posición base H(k) para una clave (1-based).
     */
    public int obtenerPosicionBase(String clave) {
        return hash(clave) + 1;
    }

    public int getTamaño() {
        return n;
    }

    public int getLongClave() {
        return longClave;
    }

    public int getContador() {
        return contador;
    }
}
