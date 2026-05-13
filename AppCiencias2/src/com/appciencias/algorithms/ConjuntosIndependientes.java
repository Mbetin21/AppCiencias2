package com.appciencias.algorithms;

import java.util.*;

/**
 * ConjuntosIndependientes.java
 *
 * Conjuntos independientes de VÉRTICES y de ARISTAS sobre un Grafo no dirigido
 * sin pesos.
 *
 * Vértices: - Un conjunto S ⊆ V es independiente si no existe arista entre
 * ningún par de vértices de S. - Maximal: independiente al que no se puede
 * agregar ningún vértice más sin perder independencia. - Máximo: el de mayor
 * tamaño. Su tamaño = α(G).
 *
 * Aristas: - Un conjunto M ⊆ A es independiente si ningún par de aristas
 * comparte vértice (también llamado matching). - Maximal: matching al que no se
 * puede agregar ninguna arista más. - Máximo: el de mayor tamaño. Su tamaño =
 * α'(G).
 */
public class ConjuntosIndependientes {

    private static final int MAX_CONJUNTOS = 200;

    // =========================================================
    //  RESULTADO — VÉRTICES
    // =========================================================
    public static class ResultadoVertices {

        /**
         * Todos los conjuntos independientes hallados (sin el vacío).
         */
        public List<List<String>> todos = new ArrayList<>();
        /**
         * Solo los maximales.
         */
        public List<List<String>> maximales = new ArrayList<>();
        /**
         * Conjunto independiente mínimo (menor tamaño, sin contar vacío).
         */
        public List<String> minimo = new ArrayList<>();
        /**
         * Conjunto independiente máximo (mayor tamaño).
         */
        public List<String> maximo = new ArrayList<>();
        /**
         * Número de independencia α(G) = tamaño del máximo.
         */
        public int numeroIndependencia = 0;
        /**
         * true si se alcanzó el límite interno de conjuntos.
         */
        public boolean limitAlcanzado = false;
    }

    // =========================================================
    //  RESULTADO — ARISTAS
    // =========================================================
    public static class ResultadoAristas {

        /**
         * Todos los conjuntos independientes de aristas hallados.
         */
        public List<List<String>> todos = new ArrayList<>();
        /**
         * Solo los maximales.
         */
        public List<List<String>> maximales = new ArrayList<>();
        /**
         * Conjunto independiente mínimo de aristas.
         */
        public List<String> minimo = new ArrayList<>();
        /**
         * Conjunto independiente máximo de aristas.
         */
        public List<String> maximo = new ArrayList<>();
        /**
         * Número de independencia de aristas α'(G).
         */
        public int numeroIndependencia = 0;
        /**
         * true si se alcanzó el límite interno de conjuntos.
         */
        public boolean limitAlcanzado = false;
    }

    // =========================================================
    //  API PÚBLICA
    // =========================================================
    /**
     * Calcula todos los conjuntos independientes de vértices del grafo.
     */
    public static ResultadoVertices calcularVertices(Grafo grafo) {
        List<String> vertices = new ArrayList<>(grafo.getVertices());
        int n = vertices.size();
        ResultadoVertices res = new ResultadoVertices();

        if (n == 0) {
            return res;
        }

        // Construir matriz de adyacencia booleana para velocidad
        boolean[][] ady = construirMatrizAdy(grafo, vertices);

        // Backtracking: generar todos los subconjuntos independientes no vacíos
        List<String> actual = new ArrayList<>();
        generarIndependientesVertices(vertices, ady, actual, 0, res);

        // Minimo y maximo
        if (!res.todos.isEmpty()) {
            res.minimo = res.todos.stream()
                    .min(Comparator.comparingInt(List::size))
                    .orElse(Collections.emptyList());
            res.maximo = res.todos.stream()
                    .max(Comparator.comparingInt(List::size))
                    .orElse(Collections.emptyList());
            res.numeroIndependencia = res.maximo.size();
        }

        // Maximales: independientes a los que no se puede agregar ningún vértice más
        for (List<String> ci : res.todos) {
            if (esMaximalVertices(ci, vertices, ady)) {
                res.maximales.add(ci);
            }
        }

        return res;
    }

    /**
     * Calcula todos los conjuntos independientes de aristas del grafo.
     */
    public static ResultadoAristas calcularAristas(Grafo grafo) {
        List<String> vertices = new ArrayList<>(grafo.getVertices());
        // Aristas en orden de inserción
        List<Grafo.Arista> aristasGrafo = new ArrayList<>(grafo.getAristas());
        int m = aristasGrafo.size();
        ResultadoAristas res = new ResultadoAristas();

        if (m == 0) {
            return res;
        }

        // Backtracking sobre aristas
        List<Grafo.Arista> actual = new ArrayList<>();
        generarIndependientesAristas(aristasGrafo, actual, 0, res);

        // Convertir a String para retorno ("v1-v2")
        // (ya se hace dentro del generador)
        // Minimo y maximo
        if (!res.todos.isEmpty()) {
            res.minimo = res.todos.stream()
                    .min(Comparator.comparingInt(List::size))
                    .orElse(Collections.emptyList());
            res.maximo = res.todos.stream()
                    .max(Comparator.comparingInt(List::size))
                    .orElse(Collections.emptyList());
            res.numeroIndependencia = res.maximo.size();
        }

        // Maximales: no se puede agregar ninguna arista más
        for (List<String> ci : res.todos) {
            if (esMaximalAristas(ci, aristasGrafo)) {
                res.maximales.add(ci);
            }
        }

        return res;
    }

    // =========================================================
    //  GENERADOR — VÉRTICES (backtracking)
    // =========================================================
    private static void generarIndependientesVertices(
            List<String> vertices,
            boolean[][] ady,
            List<String> actual,
            int inicio,
            ResultadoVertices res) {

        if (res.limitAlcanzado) {
            return;
        }

        // Guardar el conjunto actual si no está vacío
        if (!actual.isEmpty()) {
            res.todos.add(new ArrayList<>(actual));
            if (res.todos.size() >= MAX_CONJUNTOS) {
                res.limitAlcanzado = true;
                return;
            }
        }

        for (int i = inicio; i < vertices.size(); i++) {
            String v = vertices.get(i);
            // ¿Es independiente agregar v al conjunto actual?
            if (esCompatibleVertice(v, actual, vertices, ady)) {
                actual.add(v);
                generarIndependientesVertices(vertices, ady, actual, i + 1, res);
                actual.remove(actual.size() - 1);
                if (res.limitAlcanzado) {
                    return;
                }
            }
        }
    }

    /**
     * Verifica que v no sea adyacente a ningún vértice del conjunto actual.
     */
    private static boolean esCompatibleVertice(
            String v,
            List<String> actual,
            List<String> vertices,
            boolean[][] ady) {

        int idxV = vertices.indexOf(v);
        for (String u : actual) {
            int idxU = vertices.indexOf(u);
            if (ady[idxU][idxV]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Un conjunto independiente es maximal si no existe ningún vértice fuera de
     * él que pueda agregarse sin crear una arista interna.
     */
    private static boolean esMaximalVertices(
            List<String> ci,
            List<String> vertices,
            boolean[][] ady) {

        for (String v : vertices) {
            if (ci.contains(v)) {
                continue;
            }
            // ¿Se puede agregar v sin romper independencia?
            if (esCompatibleVertice(v, ci, vertices, ady)) {
                return false; // se puede agregar → NO es maximal
            }
        }
        return true;
    }

    // =========================================================
    //  GENERADOR — ARISTAS (backtracking)
    // =========================================================
    private static void generarIndependientesAristas(
            List<Grafo.Arista> aristas,
            List<Grafo.Arista> actual,
            int inicio,
            ResultadoAristas res) {

        if (res.limitAlcanzado) {
            return;
        }

        if (!actual.isEmpty()) {
            res.todos.add(aristasAStrings(actual));
            if (res.todos.size() >= MAX_CONJUNTOS) {
                res.limitAlcanzado = true;
                return;
            }
        }

        for (int i = inicio; i < aristas.size(); i++) {
            Grafo.Arista a = aristas.get(i);
            if (esCompatibleArista(a, actual)) {
                actual.add(a);
                generarIndependientesAristas(aristas, actual, i + 1, res);
                actual.remove(actual.size() - 1);
                if (res.limitAlcanzado) {
                    return;
                }
            }
        }
    }

    /**
     * Una arista es compatible con el conjunto actual si no comparte ningún
     * vértice con ninguna arista ya en el conjunto.
     */
    private static boolean esCompatibleArista(
            Grafo.Arista nueva,
            List<Grafo.Arista> actual) {

        for (Grafo.Arista a : actual) {
            if (a.v1.equals(nueva.v1) || a.v1.equals(nueva.v2)
                    || a.v2.equals(nueva.v1) || a.v2.equals(nueva.v2)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Un conjunto independiente de aristas es maximal si no existe ninguna
     * arista fuera de él que pueda agregarse sin compartir vértice.
     */
    private static boolean esMaximalAristas(
            List<String> ci,
            List<Grafo.Arista> todasAristas) {

        // Reconstruir como objetos Arista para comparar
        List<Grafo.Arista> ciAristas = new ArrayList<>();
        for (String s : ci) {
            String[] partes = s.split("-");
            ciAristas.add(new Grafo.Arista(partes[0], partes[1]));
        }

        for (Grafo.Arista a : todasAristas) {
            if (ciAristas.contains(a)) {
                continue;
            }
            if (esCompatibleArista(a, ciAristas)) {
                return false; // se puede agregar → NO es maximal
            }
        }
        return true;
    }

    // =========================================================
    //  UTILIDADES
    // =========================================================
    /**
     * Construye matriz de adyacencia booleana en el orden de 'vertices'.
     */
    private static boolean[][] construirMatrizAdy(Grafo grafo, List<String> vertices) {
        int n = vertices.size();
        boolean[][] ady = new boolean[n][n];
        for (Grafo.Arista a : grafo.getAristas()) {
            int i = vertices.indexOf(a.v1);
            int j = vertices.indexOf(a.v2);
            if (i >= 0 && j >= 0) {
                ady[i][j] = true;
                ady[j][i] = true;
            }
        }
        return ady;
    }

    /**
     * Convierte lista de Arista a lista de Strings "v1-v2".
     */
    private static List<String> aristasAStrings(List<Grafo.Arista> aristas) {
        List<String> lista = new ArrayList<>();
        for (Grafo.Arista a : aristas) {
            lista.add(a.v1 + "-" + a.v2);
        }
        return lista;
    }
}
