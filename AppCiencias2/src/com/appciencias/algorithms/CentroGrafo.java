package com.appciencias.algorithms;

import java.util.ArrayList;

/**
 * Centro y Bicentro de un grafo NO dirigido sin pesos.
 *
 * Algoritmo: 1. Se identifican las hojas (vertices de grado 1). 2. Se eliminan
 * todas las hojas simultaneamente -> nueva iteracion. 3. Se repite hasta que
 * queden 1 o 2 vertices. - 1 vertice restante = CENTRO - 2 vertices restantes =
 * BICENTRO
 */
public class CentroGrafo {

    /**
     * Representa el estado del grafo en una iteracion.
     */
    public static class Iteracion {

        public final int numero;                  // numero de iteracion (0 = inicial)
        public final ArrayList<String> vertices;  // vertices que quedan en esta iteracion
        public final ArrayList<String> hojasEliminadas; // hojas que se eliminaron para llegar aqui

        public Iteracion(int numero, ArrayList<String> vertices, ArrayList<String> hojasEliminadas) {
            this.numero = numero;
            this.vertices = new ArrayList<>(vertices);
            this.hojasEliminadas = new ArrayList<>(hojasEliminadas);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Iteración ").append(numero).append(": ");
            sb.append("S = {").append(String.join(", ", vertices)).append("}");
            if (!hojasEliminadas.isEmpty()) {
                sb.append(" | Hojas eliminadas: {").append(String.join(", ", hojasEliminadas)).append("}");
            }
            return sb.toString();
        }
    }

    /**
     * Resultado final del algoritmo.
     */
    public static class Resultado {

        public final ArrayList<String> centro;       // 1 o 2 vertices (centro o bicentro)
        public final boolean esBicentro;             // true si son 2 vertices
        public final ArrayList<Iteracion> iteraciones; // todos los pasos

        public Resultado(ArrayList<String> centro, boolean esBicentro, ArrayList<Iteracion> iteraciones) {
            this.centro = new ArrayList<>(centro);
            this.esBicentro = esBicentro;
            this.iteraciones = new ArrayList<>(iteraciones);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (Iteracion it : iteraciones) {
                sb.append(it.toString()).append("\n");
            }
            sb.append("\nResultado: ");
            if (esBicentro) {
                sb.append("BICENTRO = {").append(String.join(", ", centro)).append("}");
            } else {
                sb.append("CENTRO = {").append(String.join(", ", centro)).append("}");
            }
            return sb.toString();
        }
    }

    /**
     * Calcula el centro o bicentro del grafo dado.
     *
     * @param grafo El grafo sobre el que se calcula
     * @return Resultado con todas las iteraciones y el centro/bicentro.
     * @throws IllegalArgumentException si el grafo esta vacio o tiene un solo
     * vertice.
     */
    public static Resultado calcular(Grafo grafo) {
        if (grafo == null || grafo.getNumVertices() == 0) {
            throw new IllegalArgumentException("El grafo no puede estar vacío.");
        }

        // Trabajar con copias para no modificar el grafo original
        ArrayList<String> verticesActuales = new ArrayList<>(grafo.getVertices());
        ArrayList<Grafo.Arista> aristasActuales = new ArrayList<>(grafo.getAristas());
        ArrayList<Iteracion> iteraciones = new ArrayList<>();

        // Iteracion 0: estado inicial
        iteraciones.add(new Iteracion(0, verticesActuales, new ArrayList<>()));

        int numIteracion = 1;

        while (verticesActuales.size() > 2) {
            // Identificar hojas: vertices con grado 1 en el grafo actual
            ArrayList<String> hojas = new ArrayList<>();
            for (String v : verticesActuales) {
                int grado = calcularGrado(v, aristasActuales);
                if (grado <= 1) {
                    hojas.add(v);
                }
            }

            // Si no hay hojas y quedan mas de 2 vertices -> grafo desconectado o caso especial
            if (hojas.isEmpty()) {
                break;
            }

            // Eliminar las hojas del conjunto de vertices y de aristas
            for (String hoja : hojas) {
                verticesActuales.remove(hoja);
                aristasActuales.removeIf(a -> a.v1.equals(hoja) || a.v2.equals(hoja));
            }

            iteraciones.add(new Iteracion(numIteracion, verticesActuales, hojas));
            numIteracion++;
        }

        // Resultado final
        ArrayList<String> centroFinal = new ArrayList<>(verticesActuales);
        boolean esBicentro = centroFinal.size() == 2;

        return new Resultado(centroFinal, esBicentro, iteraciones);
    }

    /**
     * Calcula el grado de un vertice en un conjunto de aristas dado.
     */
    private static int calcularGrado(String v, ArrayList<Grafo.Arista> aristas) {
        int grado = 0;
        for (Grafo.Arista a : aristas) {
            if (a.v1.equals(v) || a.v2.equals(v)) {
                grado++;
            }
        }
        return grado;
    }
}
