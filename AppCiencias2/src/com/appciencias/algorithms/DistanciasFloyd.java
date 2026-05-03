package com.appciencias.algorithms;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Propiedades de un grafo CON PESOS NO DIRIGIDO.
 *
 * Recibe un GrafoPonderado creado con dirigido=false. Usa Floyd internamente
 * para calcular todos los caminos minimos.
 *
 * Calcula: - Tabla de distancias entre vertices (matriz de caminos minimos) -
 * Excentricidad de cada vertice (mayor distancia desde Vi a cualquier Vj) -
 * Diametro (mayor excentricidad) - Radio (menor excentricidad) - Distancia de
 * Vi (suma de todas las distancias desde Vi) - Mediana (vertice con menor
 * distancia total) - Centro / Bicentro (ya calculado por CentroGrafo, aqui se
 * referencia) - Cintura (ciclo mas corto en numero de aristas)
 */
public class DistanciasFloyd {

    /**
     * Resultado completo de todas las propiedades.
     */
    public static class Resultado {

        // Vertices en el orden usado para la tabla
        public final ArrayList<String> vertices;

        // Tabla de distancias minimas entre pares
        // tabla.get(i).get(j) = distancia minima de vertices[i] a vertices[j]
        public final ArrayList<ArrayList<Double>> tabla;

        // Excentricidad de cada vertice: max distancia desde Vi a cualquier Vj
        public final LinkedHashMap<String, Double> excentricidad;

        // Distancia total de cada vertice: suma de todas sus distancias
        public final LinkedHashMap<String, Double> distanciaTotal;

        // Diametro: mayor excentricidad
        public final double diametro;
        public final String verticeDiametro; // vertice con esa excentricidad

        // Radio: menor excentricidad
        public final double radio;
        public final String verticeRadio; // vertice con esa excentricidad

        // Mediana: vertice con menor distancia total
        public final double mediana;
        public final String verticeMediana;

        // Cintura: menor ciclo en numero de aristas (-1 si no hay ciclos)
        public final int cintura;

        public Resultado(ArrayList<String> vertices,
                ArrayList<ArrayList<Double>> tabla,
                LinkedHashMap<String, Double> excentricidad,
                LinkedHashMap<String, Double> distanciaTotal,
                double diametro, String verticeDiametro,
                double radio, String verticeRadio,
                double mediana, String verticeMediana,
                int cintura) {
            this.vertices = vertices;
            this.tabla = tabla;
            this.excentricidad = excentricidad;
            this.distanciaTotal = distanciaTotal;
            this.diametro = diametro;
            this.verticeDiametro = verticeDiametro;
            this.radio = radio;
            this.verticeRadio = verticeRadio;
            this.mediana = mediana;
            this.verticeMediana = verticeMediana;
            this.cintura = cintura;
        }

        /**
         * Formatea la tabla de distancias como en el tablero del profesor:
         * columnas = vertices origen, filas = destinos, ultima fila = total.
         */
        public String getTablaFormateada() {
            int n = vertices.size();
            StringBuilder sb = new StringBuilder();

            // Encabezado de columnas
            sb.append(String.format("%-8s", ""));
            for (String v : vertices) {
                sb.append(String.format("%-8s", v));
            }
            sb.append("\n");

            // Filas: para cada destino j, mostrar distancia desde cada origen i
            for (int j = 0; j < n; j++) {
                sb.append(String.format("%-8s", ""));
                for (int i = 0; i < n; i++) {
                    if (i == j) {
                        sb.append(String.format("%-8s", "-"));
                    } else {
                        double d = tabla.get(i).get(j);
                        String val = (d == Double.MAX_VALUE) ? "∞" : String.valueOf((int) d);
                        // Mostrar Vij como etiqueta
                        String etiqueta = "V" + (i + 1) + (j + 1) + "=" + val;
                        sb.append(String.format("%-8s", etiqueta));
                    }
                }
                sb.append("\n");
            }

            // Fila de totales
            sb.append(String.format("%-8s", "Total"));
            for (String v : vertices) {
                double total = distanciaTotal.get(v);
                sb.append(String.format("%-8s", (int) total));
            }
            sb.append("\n");

            // Fila de excentricidades
            sb.append(String.format("%-8s", "Excent."));
            for (String v : vertices) {
                double exc = excentricidad.get(v);
                String val = (exc == Double.MAX_VALUE) ? "∞" : String.valueOf((int) exc);
                sb.append(String.format("%-8s", val));
            }
            sb.append("\n");

            return sb.toString();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== PROPIEDADES DEL GRAFO ===\n\n");

            sb.append("--- Tabla de distancias minimas ---\n");
            sb.append(getTablaFormateada()).append("\n");

            sb.append("--- Excentricidad por vertice ---\n");
            for (String v : excentricidad.keySet()) {
                double exc = excentricidad.get(v);
                String val = (exc == Double.MAX_VALUE) ? "∞" : String.valueOf((int) exc);
                sb.append("  e(").append(v).append(") = ").append(val).append("\n");
            }

            sb.append("\n--- Distancia total por vertice ---\n");
            for (String v : distanciaTotal.keySet()) {
                sb.append("  d(").append(v).append(") = ")
                        .append(distanciaTotal.get(v).intValue()).append("\n");
            }

            sb.append("\nDiámetro:  ").append((int) diametro)
                    .append(" (en ").append(verticeDiametro).append(")\n");
            sb.append("Radio:     ").append((int) radio)
                    .append(" (en ").append(verticeRadio).append(")\n");
            sb.append("Mediana:   ").append((int) mediana)
                    .append(" (vertice ").append(verticeMediana).append(")\n");
            sb.append("Cintura:   ");
            sb.append(cintura == -1 ? "No hay ciclos" : cintura + " aristas").append("\n");

            return sb.toString();
        }
    }

    // =====================================================================
    //  ALGORITMO
    // =====================================================================
    /**
     * Calcula todas las propiedades del grafo no dirigido con pesos.
     *
     * @param grafo Grafo ponderado NO dirigido (creado con dirigido=false).
     * @return Resultado con todas las propiedades calculadas.
     * @throws IllegalArgumentException si el grafo esta vacio o es dirigido.
     */
    public static Resultado calcular(GrafoPonderado grafo) {
        if (grafo == null || grafo.getNumVertices() == 0) {
            throw new IllegalArgumentException("El grafo no puede estar vacío.");
        }
        if (grafo.isDirigido()) {
            throw new IllegalArgumentException(
                    "PropiedadesGrafo requiere un grafo NO dirigido. "
                    + "Crea el grafo con new GrafoPonderado(nombre, false).");
        }

        ArrayList<String> vertices = grafo.getVertices();
        int n = vertices.size();

        // ── Paso 1: Floyd para obtener todas las distancias minimas ──────────
        // Floyd interno sin ordinal (aqui no aplica ordinal porque no es dirigido
        // en el sentido topologico, usamos el orden de insercion)
        double[][] dist = calcularFloydInterno(grafo, vertices);

        // ── Paso 2: Construir tabla ─────────────────────────────────────────
        // tabla[i][j] = distancia minima de vertices[i] a vertices[j]
        ArrayList<ArrayList<Double>> tabla = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            ArrayList<Double> fila = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                fila.add(dist[i][j]);
            }
            tabla.add(fila);
        }

        // ── Paso 3: Excentricidad y distancia total ─────────────────────────
        LinkedHashMap<String, Double> excentricidad = new LinkedHashMap<>();
        LinkedHashMap<String, Double> distanciaTotal = new LinkedHashMap<>();

        for (int i = 0; i < n; i++) {
            double maxDist = 0;
            double sumaDist = 0;
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    continue;
                }
                double d = dist[i][j];
                if (d == Double.MAX_VALUE) {
                    maxDist = Double.MAX_VALUE;
                } else {
                    if (maxDist != Double.MAX_VALUE && d > maxDist) {
                        maxDist = d;
                    }
                    sumaDist += d;
                }
            }
            excentricidad.put(vertices.get(i), maxDist);
            distanciaTotal.put(vertices.get(i), sumaDist);
        }

        // ── Paso 4: Diametro (mayor excentricidad) ──────────────────────────
        double diametro = 0;
        String verticeDiametro = "";
        for (String v : excentricidad.keySet()) {
            double exc = excentricidad.get(v);
            if (exc != Double.MAX_VALUE && exc > diametro) {
                diametro = exc;
                verticeDiametro = v;
            }
        }

        // ── Paso 5: Radio (menor excentricidad) ─────────────────────────────
        double radio = Double.MAX_VALUE;
        String verticeRadio = "";
        for (String v : excentricidad.keySet()) {
            double exc = excentricidad.get(v);
            if (exc < radio) {
                radio = exc;
                verticeRadio = v;
            }
        }

        // ── Paso 6: Mediana (vertice con menor distancia total) ──────────────
        double mediana = Double.MAX_VALUE;
        String verticeMediana = "";
        for (String v : distanciaTotal.keySet()) {
            double total = distanciaTotal.get(v);
            if (total < mediana) {
                mediana = total;
                verticeMediana = v;
            }
        }

        // ── Paso 7: Cintura (ciclo mas corto en numero de aristas) ──────────
        int cintura = calcularCintura(grafo, vertices);

        return new Resultado(vertices, tabla, excentricidad, distanciaTotal,
                diametro, verticeDiametro,
                radio, verticeRadio,
                mediana, verticeMediana,
                cintura);
    }

    // =====================================================================
    //  FLOYD INTERNO (sin ordinal, para grafo no dirigido)
    // =====================================================================
    /**
     * Floyd-Warshall interno adaptado para grafo no dirigido. Orden: j externo,
     * i medio, k interno (Dij + Djk < Dik). No necesita ordinal porque el grafo
     * no es dirigido topologicamente.
     */
    private static double[][] calcularFloydInterno(GrafoPonderado grafo,
            ArrayList<String> vertices) {
        int n = vertices.size();
        double[][] dist = new double[n][n];

        // Inicializar
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                dist[i][j] = (i == j) ? 0 : Double.MAX_VALUE;
            }
        }

        // Cargar pesos (el grafo no dirigido ya tiene ambas direcciones)
        for (GrafoPonderado.AristaPonderada a : grafo.getAristas()) {
            int i = vertices.indexOf(a.origen);
            int j = vertices.indexOf(a.destino);
            if (i != -1 && j != -1) {
                dist[i][j] = a.peso;
            }
        }

        // Floyd: j externo, i medio, k interno
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < n; i++) {
                if (i == j || dist[i][j] == Double.MAX_VALUE) {
                    continue;
                }
                for (int k = 0; k < n; k++) {
                    if (k == i || k == j || dist[j][k] == Double.MAX_VALUE) {
                        continue;
                    }
                    double nueva = dist[i][j] + dist[j][k];
                    if (nueva < dist[i][k]) {
                        dist[i][k] = nueva;
                    }
                }
            }
        }

        return dist;
    }

    // =====================================================================
    //  CINTURA — ciclo mas corto en numero de aristas (BFS desde cada nodo)
    // =====================================================================
    /**
     * Calcula la cintura del grafo: longitud del ciclo mas corto medida en
     * NUMERO DE ARISTAS (no en suma de pesos).
     *
     * Usa BFS desde cada vertice para encontrar el ciclo mas corto. Si no hay
     * ningun ciclo retorna -1.
     */
    private static int calcularCintura(GrafoPonderado grafo,
            ArrayList<String> vertices) {
        int cintura = Integer.MAX_VALUE;

        for (String inicio : vertices) {
            int ciclo = bfsCicloMinimo(inicio, grafo, vertices);
            if (ciclo != -1 && ciclo < cintura) {
                cintura = ciclo;
            }
        }

        return (cintura == Integer.MAX_VALUE) ? -1 : cintura;
    }

    /**
     * BFS desde un vertice para encontrar el ciclo mas corto que lo incluya.
     * Retorna la longitud del ciclo en numero de aristas, o -1 si no hay.
     */
    private static int bfsCicloMinimo(String inicio, GrafoPonderado grafo,
            ArrayList<String> vertices) {
        // distBFS[v] = distancia en aristas desde inicio hasta v (-1 = no visitado)
        LinkedHashMap<String, Integer> distBFS = new LinkedHashMap<>();
        LinkedHashMap<String, String> padresBFS = new LinkedHashMap<>();

        for (String v : vertices) {
            distBFS.put(v, -1);
            padresBFS.put(v, null);
        }

        distBFS.put(inicio, 0);
        ArrayList<String> cola = new ArrayList<>();
        cola.add(inicio);

        while (!cola.isEmpty()) {
            String actual = cola.remove(0);
            int distActual = distBFS.get(actual);

            for (GrafoPonderado.AristaPonderada arista : grafo.getAristas()) {
                if (!arista.origen.equals(actual)) {
                    continue;
                }
                String vecino = arista.destino;

                if (distBFS.get(vecino) == -1) {
                    // No visitado: marcar y agregar a cola
                    distBFS.put(vecino, distActual + 1);
                    padresBFS.put(vecino, actual);
                    cola.add(vecino);
                } else if (!vecino.equals(padresBFS.get(actual))) {
                    // Ya visitado y no es el padre directo -> hay ciclo
                    // Longitud = distActual + distBFS[vecino] + 1
                    return distActual + distBFS.get(vecino) + 1;
                }
            }
        }

        return -1; // no hay ciclo desde este vertice
    }
}
