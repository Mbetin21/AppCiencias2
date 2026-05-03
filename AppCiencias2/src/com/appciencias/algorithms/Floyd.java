package com.appciencias.algorithms;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Algoritmo de Floyd-Warshall — Camino Minimo entre todos los pares.
 *
 * Usa GrafoPonderado (dirigido con pesos positivos). Aplica numeracion ordinal
 * para ordenar los vertices.
 *
 * Condicion: Dij + Djk < Dik  -> Dik = Dij + Djk
 *
 * Orden de iteracion: j externo (1..n) i medio (1..n) k interno (1..n)
 *
 * Matrices que se guardan: - Matriz inicial (antes de cualquier iteracion) -
 * Una matriz por cada valor de j (snapshot al terminar cada j) - La ultima
 * snapshot ES la matriz final con todas las distancias minimas
 *
 * Matriz inicial: - Diagonal = 0 - Camino directo = peso de la arista - Sin
 * camino = infinito
 */
public class Floyd {

    /**
     * Snapshot de la matriz D en un momento del algoritmo.
     */
    public static class MatrizSnapshot {

        public final int j;              // valor de j al terminar esta iteracion (-1 = inicial)
        public final String nombreJ;     // nombre del vertice j (-1 = "inicial")
        public final double[][] dist;    // copia de la matriz en ese momento
        public final ArrayList<String> vertices;

        public MatrizSnapshot(int j, String nombreJ,
                double[][] dist, ArrayList<String> vertices) {
            this.j = j;
            this.nombreJ = nombreJ;
            this.vertices = new ArrayList<>(vertices);
            // Copiar la matriz profundamente
            int n = dist.length;
            this.dist = new double[n][n];
            for (int i = 0; i < n; i++) {
                for (int k = 0; k < n; k++) {
                    this.dist[i][k] = dist[i][k];
                }
            }
        }

        /**
         * Formatea la matriz para mostrar al usuario.
         */
        public String formatear() {
            int n = vertices.size();
            StringBuilder sb = new StringBuilder();
            if (j == -1) {
                sb.append("Matriz inicial:\n");
            } else {
                sb.append("Matriz despues de j = ").append(nombreJ).append(":\n");
            }
            // Encabezado
            sb.append(String.format("%-6s", ""));
            for (String v : vertices) {
                sb.append(String.format("%-6s", v));
            }
            sb.append("\n");
            for (int i = 0; i < n; i++) {
                sb.append(String.format("%-6s", vertices.get(i)));
                for (int k = 0; k < n; k++) {
                    String val = (dist[i][k] == Double.MAX_VALUE)
                            ? "∞" : String.valueOf((int) dist[i][k]);
                    sb.append(String.format("%-6s", val));
                }
                sb.append("\n");
            }
            return sb.toString();
        }

        @Override
        public String toString() {
            return formatear();
        }
    }

    /**
     * Resultado completo de Floyd-Warshall.
     */
    public static class Resultado {

        // Vertices en orden ordinal
        public final ArrayList<String> vertices;
        // Todas las matrices: [0] = inicial, [1] = j=1, [2] = j=2 ... [n] = final
        public final ArrayList<MatrizSnapshot> matrices;
        // Matriz de predecesores final [i][k] = indice del vertice intermedio j
        public final int[][] pred;
        // Nodo origen y destino elegidos por el usuario
        public final String nodoOrigen;
        public final String nodoDestino;
        // Camino reconstruido origen -> destino
        public final ArrayList<String> camino;
        // Costo del camino origen -> destino
        public final double costoTotal;

        public Resultado(ArrayList<String> vertices,
                ArrayList<MatrizSnapshot> matrices,
                int[][] pred,
                String nodoOrigen, String nodoDestino) {
            this.vertices = new ArrayList<>(vertices);
            this.matrices = new ArrayList<>(matrices);
            this.pred = pred;
            this.nodoOrigen = nodoOrigen;
            this.nodoDestino = nodoDestino;

            // Camino y costo usando la matriz final (ultima snapshot)
            double[][] distFinal = matrices.get(matrices.size() - 1).dist;
            this.camino = reconstruirCamino(vertices, distFinal, pred, nodoOrigen, nodoDestino);
            int io = vertices.indexOf(nodoOrigen);
            int id = vertices.indexOf(nodoDestino);
            this.costoTotal = (io == -1 || id == -1) ? Double.MAX_VALUE : distFinal[io][id];
        }

        /**
         * Retorna solo la matriz final
         */
        public MatrizSnapshot getMatrizFinal() {
            return matrices.get(matrices.size() - 1);
        }

        /**
         * Retorna la matriz inicial
         */
        public MatrizSnapshot getMatrizInicial() {
            return matrices.get(0);
        }

        /**
         * Formatea la matriz de predecesores final.
         */
        public String getMatrizPredecesores() {
            int n = vertices.size();
            double[][] distFinal = getMatrizFinal().dist;
            StringBuilder sb = new StringBuilder();
            sb.append("Matriz de predecesores P:\n");
            sb.append(String.format("%-6s", ""));
            for (String v : vertices) {
                sb.append(String.format("%-6s", v));
            }
            sb.append("\n");
            for (int i = 0; i < n; i++) {
                sb.append(String.format("%-6s", vertices.get(i)));
                for (int k = 0; k < n; k++) {
                    String val;
                    if (i == k) {
                        val = "0";
                    } else if (distFinal[i][k] == Double.MAX_VALUE) {
                        val = "∞";
                    } else if (pred[i][k] == -1) {
                        val = "-"; // arista directa, sin intermediario
                    } else {
                        val = vertices.get(pred[i][k]);
                    }
                    sb.append(String.format("%-6s", val));
                }
                sb.append("\n");
            }
            return sb.toString();
        }

        /**
         * Reconstruye el camino minimo de origen a destino.
         */
        private static ArrayList<String> reconstruirCamino(
                ArrayList<String> vertices, double[][] dist, int[][] pred,
                String origen, String destino) {
            int io = vertices.indexOf(origen);
            int id = vertices.indexOf(destino);
            if (io == -1 || id == -1 || dist[io][id] == Double.MAX_VALUE) {
                return new ArrayList<>();
            }
            ArrayList<String> camino = new ArrayList<>();
            construir(io, id, pred, vertices, camino);
            return camino;
        }

        private static void construir(int i, int k, int[][] pred,
                ArrayList<String> vertices,
                ArrayList<String> camino) {
            if (pred[i][k] == -1) {
                if (camino.isEmpty()) {
                    camino.add(vertices.get(i));
                }
                if (i != k) {
                    camino.add(vertices.get(k));
                }
            } else {
                int j = pred[i][k];
                construir(i, j, pred, vertices, camino);
                camino.remove(camino.size() - 1); // evitar duplicado del nodo j
                construir(j, k, pred, vertices, camino);
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== FLOYD-WARSHALL ===\n\n");
            for (MatrizSnapshot m : matrices) {
                sb.append(m.formatear()).append("\n");
            }
            sb.append(getMatrizPredecesores()).append("\n");
            sb.append("--- Camino minimo de '").append(nodoOrigen)
                    .append("' a '").append(nodoDestino).append("' ---\n");
            if (camino.isEmpty()) {
                sb.append("  No existe camino.\n");
            } else {
                sb.append("  Ruta: ").append(String.join(" -> ", camino)).append("\n");
                sb.append("  Costo total: ").append(costoTotal).append("\n");
            }
            return sb.toString();
        }
    }

    // =====================================================================
    //  ALGORITMO
    // =====================================================================
    /**
     * Ejecuta Floyd-Warshall sobre el grafo ponderado.
     *
     * @param grafo Grafo dirigido ponderado.
     * @param nodoOrigen Vertice de inicio para mostrar el camino.
     * @param nodoDestino Vertice de llegada para mostrar el camino.
     * @return Resultado con todas las matrices iteracion por iteracion y
     * camino.
     */
    public static Resultado calcular(GrafoPonderado grafo,
            String nodoOrigen, String nodoDestino) {
        if (grafo == null || grafo.getNumVertices() == 0) {
            throw new IllegalArgumentException("El grafo no puede estar vacío.");
        }

        nodoOrigen = nodoOrigen.trim().toLowerCase();
        nodoDestino = nodoDestino.trim().toLowerCase();

        if (!grafo.contieneVertice(nodoOrigen)) {
            throw new IllegalArgumentException(
                    "El nodo origen '" + nodoOrigen + "' no existe.");
        }
        if (!grafo.contieneVertice(nodoDestino)) {
            throw new IllegalArgumentException(
                    "El nodo destino '" + nodoDestino + "' no existe.");
        }

        // Paso 1: ordenar vertices por ordinal
        Ordinal.Resultado ordinalRes = Ordinal.calcularDirigido(grafo);
        LinkedHashMap<String, Integer> numeracion = ordinalRes.numeracion;

        int n = grafo.getNumVertices();
        ArrayList<String> vertices = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            for (String v : numeracion.keySet()) {
                if (numeracion.get(v) == i) {
                    vertices.add(v);
                    break;
                }
            }
        }

        // Paso 2: inicializar matriz D y predecesores
        double[][] dist = new double[n][n];
        int[][] pred = new int[n][n];

        for (int i = 0; i < n; i++) {
            for (int k = 0; k < n; k++) {
                dist[i][k] = (i == k) ? 0 : Double.MAX_VALUE;
                pred[i][k] = -1;
            }
        }

        // Cargar aristas directas
        for (GrafoPonderado.AristaPonderada a : grafo.getAristas()) {
            int i = vertices.indexOf(a.origen);
            int k = vertices.indexOf(a.destino);
            if (i != -1 && k != -1) {
                dist[i][k] = a.peso;
                // pred queda -1: arista directa sin intermediario
            }
        }

        // Guardar snapshot de la matriz inicial
        ArrayList<MatrizSnapshot> matrices = new ArrayList<>();
        matrices.add(new MatrizSnapshot(-1, "inicial", dist, vertices));

        // Paso 3: Floyd-Warshall
        // Condicion: Dij + Djk < Dik
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < n; i++) {
                if (i == j) {
                    continue;
                }
                if (dist[i][j] == Double.MAX_VALUE) {
                    continue; // Dij = inf, nunca mejora

                                }for (int k = 0; k < n; k++) {
                    if (k == j || k == i) {
                        continue;
                    }
                    if (dist[j][k] == Double.MAX_VALUE) {
                        continue; // Djk = inf, skip
                    }
                    double nueva = dist[i][j] + dist[j][k];
                    if (nueva < dist[i][k]) {
                        dist[i][k] = nueva;
                        pred[i][k] = j; // j es el intermediario
                    }
                }
            }
            // Snapshot al terminar cada j
            matrices.add(new MatrizSnapshot(j, vertices.get(j), dist, vertices));
        }

        return new Resultado(vertices, matrices, pred, nodoOrigen, nodoDestino);
    }
}
