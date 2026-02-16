package com.appciencias.algorithms;

public class ArreglosAnidados {

    private String[][] tabla;
    private int n;

    public ArreglosAnidados(int tamaño) {

        if (tamaño <= 0) {
            throw new IllegalArgumentException("Tamaño invalido.");
        }

        this.n = tamaño;
        this.tabla = new String[n][n]; // matriz n x n
    }

    private int hash(long k) {
        return (int) (k % n) + 1;
    }

    public void insertar(String clave) {

        long k = Long.parseLong(clave);
        int D = hash(k) - 1;

        for (int i = 0; i < n; i++) {

            if (tabla[D][i] == null) {
                tabla[D][i] = clave;
                return;
            }
        }

        throw new IllegalStateException("Fila llena.");
    }
}
