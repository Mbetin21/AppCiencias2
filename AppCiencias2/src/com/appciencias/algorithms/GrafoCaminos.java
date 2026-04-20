package com.appciencias.algorithms;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Grafo dirgido con pesos en las aristas. G = (S, A) donde cada arista tiene un
 * peso Äij.
 *
 * El usuario construye el grafo paso a paso: agregarVertice("1")
 * agregarArista("1", "2", 5) → arista de 1 a 2 con peso 5
 *
 * Algoritmos: - Dijkstra : El nodo con menor distancia acumulada - Floyd :
 * todos contra todos, matriz de distancias
 */
public class GrafoCaminos {

    public static class Arista {

        public final String origen;
        public final String destino;
        public final double peso;

        public Arista(String origen, String destino, double peso) {
            this.origen = origen.trim();
            this.destino = destino.trim();
            this.peso = peso;
        }

        @Override
        public String toString() {
            return origen + " → " + destino + " (" + peso + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Arista)) {
                return false;
            }
            Arista a = (Arista) o;
            // Dirigida: origen y destino importan en orden
            return origen.equals(a.origen) && destino.equals(a.destino);
        }

        @Override
        public int hashCode() {
            return origen.hashCode() * 31 + destino.hashCode();
        }
    }

    // ── Resultado de camino minimo ────────────────────────────────────────────
    public static class ResultadoCamino {

        /**
         * λ de cada vertice: distancia minima desde el origen.
         */
        public final Map<String, Double> lambda;

        /**
         * Predecesor de cada vertice en el camino minimo.
         */
        public final Map<String, String> predecesor;

        /**
         * Nombre del algoritmo usado.
         */
        public final String algoritmo;

        /**
         * Nodo origen.
         */
        public final String origen;

        /**
         * Pasos intermedios para mostrar el proceso (texto).
         */
        public final ArrayList<String> pasos;

        public ResultadoCamino(String algoritmo, String origen,
                Map<String, Double> lambda,
                Map<String, String> predecesor,
                ArrayList<String> pasos) {
            this.algoritmo = algoritmo;
            this.origen = origen;
            this.lambda = lambda;
            this.predecesor = predecesor;
            this.pasos = pasos;
        }

        /**
         * Reconstruye el camino desde el origen hasta un destino.
         *
         * @return lista de vertices del camino, o lista vacia si no hay camino.
         */
        public ArrayList<String> getCamino(String destino) {
            ArrayList<String> camino = new ArrayList<>();
            if (!lambda.containsKey(destino) || lambda.get(destino) == Double.MAX_VALUE) {
                return camino; // no hay camino
            }
            String actual = destino;
            while (actual != null) {
                camino.add(0, actual);
                actual = predecesor.get(actual);
            }
            return camino;
        }

        /**
         * Resumen de todas las distancias
         */
        public String getResumen() {
            StringBuilder sb = new StringBuilder();
            sb.append(algoritmo).append(" desde ").append(origen).append(":\n");
            for (Map.Entry<String, Double> e : lambda.entrySet()) {
                String dist = e.getValue() == Double.MAX_VALUE ? "∞" : String.valueOf(e.getValue());
                sb.append("  λ").append(e.getKey()).append(" = ").append(dist);
                ArrayList<String> cam = getCamino(e.getKey());
                if (!cam.isEmpty()) {
                    sb.append("  (").append(String.join(" → ", cam)).append(")");
                }
                sb.append("\n");
            }
            return sb.toString();
        }
    }

    // ── Resultado Floyd ───────────────────────────────────────────────────────
    public static class ResultadoFloyd {

        /**
         * Matriz de distancias minimas entre todos los pares.
         */
        public final double[][] dist;

        /**
         * Matriz de predecesores.
         */
        public final String[][] pred;

        /**
         * Lista de vertices en el orden de la matriz.
         */
        public final ArrayList<String> vertices;

        /**
         * Pasos intermedios.
         */
        public final ArrayList<String> pasos;

        public ResultadoFloyd(double[][] dist, String[][] pred,
                ArrayList<String> vertices, ArrayList<String> pasos) {
            this.dist = dist;
            this.pred = pred;
            this.vertices = vertices;
            this.pasos = pasos;
        }

        /**
         * Distancia minima entre dos vertices.
         */
        public double getDistancia(String desde, String hasta) {
            int i = vertices.indexOf(desde);
            int j = vertices.indexOf(hasta);
            if (i == -1 || j == -1) {
                return Double.MAX_VALUE;
            }
            return dist[i][j];
        }

        /**
         * Camino minimo entre dos vertices.
         */
        public ArrayList<String> getCamino(String desde, String hasta) {
            ArrayList<String> camino = new ArrayList<>();
            int i = vertices.indexOf(desde);
            int j = vertices.indexOf(hasta);
            if (i == -1 || j == -1 || dist[i][j] == Double.MAX_VALUE) {
                return camino;
            }
            reconstruirCamino(i, j, camino);
            return camino;
        }

        private void reconstruirCamino(int i, int j, ArrayList<String> camino) {
            if (pred[i][j] == null) {
                camino.add(vertices.get(i));
                if (i != j) {
                    camino.add(vertices.get(j));
                }
            } else {
                int k = vertices.indexOf(pred[i][j]);
                reconstruirCamino(i, k, camino);
                camino.remove(camino.size() - 1); // evitar duplicado
                reconstruirCamino(k, j, camino);
            }
        }

        /**
         * Resumen de la matriz de distancias.
         */
        public String getResumen() {
            StringBuilder sb = new StringBuilder("Floyd-Warshall:\n");
            int n = vertices.size();
            // Encabezado
            sb.append(String.format("%-8s", ""));
            for (String v : vertices) {
                sb.append(String.format("%-8s", v));
            }
            sb.append("\n");
            for (int i = 0; i < n; i++) {
                sb.append(String.format("%-8s", vertices.get(i)));
                for (int j = 0; j < n; j++) {
                    String val = dist[i][j] == Double.MAX_VALUE ? "∞" : String.valueOf(dist[i][j]);
                    sb.append(String.format("%-8s", val));
                }
                sb.append("\n");
            }
            return sb.toString();
        }
    }

    private final String nombre;
    private final ArrayList<String> vertices;
    private final ArrayList<Arista> aristas;

    // ── Constructor ──────────────────────────────────────────────────────────
    public GrafoCaminos(String nombre) {
        this.nombre = nombre;
        this.vertices = new ArrayList<>();
        this.aristas = new ArrayList<>();
    }

    /**
     * Agrega un vertice al grafo.
     *
     * @throws IllegalArgumentException si el vertice ya existe o el nombre es
     * vacio.
     */
    public void agregarVertice(String v) {
        if (v == null || v.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del vertice no puede estar vacio.");
        }
        v = v.trim();
        if (vertices.contains(v)) {
            throw new IllegalArgumentException("El vertice '" + v + "' ya existe.");
        }
        vertices.add(v);
    }

    /**
     * Agrega una arista dirigida con peso desde origen hasta destino.
     *
     * @throws IllegalArgumentException si algun vertice no existe, si la arista
     * ya existe, o si el peso es negativo en Dijkstra (se permite para Bellman
     * y Floyd).
     */
    public void agregarArista(String origen, String destino, double peso) {
        origen = origen.trim();
        destino = destino.trim();

        if (!vertices.contains(origen)) {
            throw new IllegalArgumentException(
                    "El vertice '" + origen + "' no existe. Agregalo primero.");
        }
        if (!vertices.contains(destino)) {
            throw new IllegalArgumentException(
                    "El vertice '" + destino + "' no existe. Agregalo primero.");
        }

        Arista nueva = new Arista(origen, destino, peso);
        if (aristas.contains(nueva)) {
            throw new IllegalArgumentException(
                    "La arista '" + origen + " → " + destino + "' ya existe.");
        }
        aristas.add(nueva);
    }

    /**
     * Elimina un vertice y todas sus aristas asociadas.
     */
    public void eliminarVertice(String v) {
        v = v.trim();
        if (!vertices.contains(v)) {
            throw new IllegalArgumentException("El vertice '" + v + "' no existe.");
        }
        vertices.remove(v);
        final String vf = v;
        aristas.removeIf(a -> a.origen.equals(vf) || a.destino.equals(vf));
    }

    /**
     * Elimina una arista dirigida.
     */
    public void eliminarArista(String origen, String destino) {
        origen = origen.trim();
        destino = destino.trim();
        Arista a = new Arista(origen, destino, 0);
        if (!aristas.contains(a)) {
            throw new IllegalArgumentException(
                    "La arista '" + origen + " → " + destino + "' no existe.");
        }
        aristas.removeIf(ar -> ar.equals(a));
    }

    /**
     * Limpia el grafo.
     */
    public void limpiar() {
        vertices.clear();
        aristas.clear();
    }

    // ── Algoritmo Dijkstra ────────────────────────────────────────────────────
    /**
     * En cada paso selecciona el vertice no visitado con menor λ acumulado y
     * actualiza las distancias de sus vecinos
     *
     * @param origenVertex Vertice desde donde se calculan los caminos.
     * @return ResultadoCamino con λ de cada vertice y predecesores.
     * @throws IllegalArgumentException si el origen no existe o hay pesos
     * negativos.
     */
    public ResultadoCamino dijkstra(String origenVertex) {
        origenVertex = origenVertex.trim();
        if (!vertices.contains(origenVertex)) {
            throw new IllegalArgumentException(
                    "El vertice origen '" + origenVertex + "' no existe.");
        }
        for (Arista a : aristas) {
            if (a.peso < 0) {
                throw new IllegalArgumentException(
                        "Dijkstra no funciona con pesos negativos. Use Bellman.");
            }
        }

        Map<String, Double> lambda = new LinkedHashMap<>();
        Map<String, String> predecesor = new LinkedHashMap<>();
        ArrayList<String> visitados = new ArrayList<>();
        ArrayList<String> pasos = new ArrayList<>();

        // Inicializar
        for (String v : vertices) {
            lambda.put(v, v.equals(origenVertex) ? 0.0 : Double.MAX_VALUE);
            predecesor.put(v, null);
        }

        pasos.add("Inicio: λ" + origenVertex + " = 0, resto = ∞");

        while (visitados.size() < vertices.size()) {
            // Seleccionar el no visitado con menor λ
            String u = null;
            double minLambda = Double.MAX_VALUE;
            for (String v : vertices) {
                if (!visitados.contains(v) && lambda.get(v) < minLambda) {
                    minLambda = lambda.get(v);
                    u = v;
                }
            }

            if (u == null) {
                break; // resto inaccesible
            }
            visitados.add(u);
            pasos.add("Visitar " + u + " (λ" + u + " = " + lambda.get(u) + ")");

            // Actualizar vecinos
            for (Arista a : aristas) {
                if (a.origen.equals(u) && !visitados.contains(a.destino)) {
                    double nueva = lambda.get(u) + a.peso;
                    if (nueva < lambda.get(a.destino)) {
                        lambda.put(a.destino, nueva);
                        predecesor.put(a.destino, u);
                        pasos.add("  Actualizar λ" + a.destino + " = " + nueva
                                + " (via " + u + ")");
                    }
                }
            }
        }

        return new ResultadoCamino("Dijkstra", origenVertex, lambda, predecesor, pasos);
    }

    // ── Algoritmo Floyd ──────────────────────────────────────────────
    /**
     * Algoritmo de Floyd
     *
     * Calcula el camino minimo entre TODOS los pares de vertices. Funciona con
     * pesos negativos pero no con ciclos negativos.
     *
     * @return ResultadoFloyd con matriz de distancias y predecesores.
     */
    public ResultadoFloyd floyd() {
        int n = vertices.size();
        double[][] dist = new double[n][n];
        String[][] pred = new String[n][n];
        ArrayList<String> pasos = new ArrayList<>();

        // Inicializar matriz
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    dist[i][j] = 0;
                } else {
                    dist[i][j] = Double.MAX_VALUE;
                }
                pred[i][j] = null;
            }
        }

        // Poner pesos de aristas directas
        for (Arista a : aristas) {
            int i = vertices.indexOf(a.origen);
            int j = vertices.indexOf(a.destino);
            dist[i][j] = a.peso;
            pred[i][j] = a.origen;
        }

        pasos.add("Matriz inicial cargada con pesos directos.");

        // Relajar con cada vertice intermedio k
        for (int k = 0; k < n; k++) {
            pasos.add("Iteracion k = " + vertices.get(k));
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (dist[i][k] != Double.MAX_VALUE
                            && dist[k][j] != Double.MAX_VALUE) {
                        double nueva = dist[i][k] + dist[k][j];
                        if (nueva < dist[i][j]) {
                            dist[i][j] = nueva;
                            pred[i][j] = vertices.get(k);
                            pasos.add("  dist[" + vertices.get(i) + "][" + vertices.get(j)
                                    + "] = " + nueva + " via " + vertices.get(k));
                        }
                    }
                }
            }
        }

        return new ResultadoFloyd(dist, pred, new ArrayList<>(vertices), pasos);
    }

    public String getNombre() {
        return nombre;
    }

    public ArrayList<String> getVertices() {
        return new ArrayList<>(vertices);
    }

    public ArrayList<Arista> getAristas() {
        return new ArrayList<>(aristas);
    }

    public int getNumVertices() {
        return vertices.size();
    }

    public int getNumAristas() {
        return aristas.size();
    }

    public boolean contieneVertice(String v) {
        return vertices.contains(v.trim());
    }

    public ArrayList<String> getVecinos(String v) {
        ArrayList<String> vecinos = new ArrayList<>();
        for (Arista a : aristas) {
            if (a.origen.equals(v)) {
                vecinos.add(a.destino);
            }
        }
        return vecinos;
    }

    /**
     * Peso de la arista origen→destino. -1 si no existe.
     */
    public double getPeso(String origen, String destino) {
        for (Arista a : aristas) {
            if (a.origen.equals(origen) && a.destino.equals(destino)) {
                return a.peso;
            }
        }
        return -1;
    }

    public String getVerticesStr() {
        if (vertices.isEmpty()) {
            return "S = {}";
        }
        return "S = {" + String.join(", ", vertices) + "}";
    }

    public String getAristasStr() {
        if (aristas.isEmpty()) {
            return "A = {}";
        }
        StringBuilder sb = new StringBuilder("A = {");
        for (int i = 0; i < aristas.size(); i++) {
            sb.append(aristas.get(i).toString());
            if (i < aristas.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.append("}").toString();
    }

    @Override
    public String toString() {
        return nombre + "\n" + getVerticesStr() + "\n" + getAristasStr();
    }
}
