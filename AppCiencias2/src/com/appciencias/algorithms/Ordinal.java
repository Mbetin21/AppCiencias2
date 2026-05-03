package com.appciencias.algorithms;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Numeracion Ordinal de vertices de un grafo.
 *
 * No dirigido: se numeran en el orden en que fueron ingresados (izq a derecha).
 */
public class Ordinal {

    /**
     * Representa el numero asignado a un vertice en un paso del proceso.
     */
    public static class Paso {

        public final int numero;      // numero ordinal asignado
        public final String vertice;  // vertice que recibio ese numero
        public final String motivo;   // por que se numeró en este paso

        public Paso(int numero, String vertice, String motivo) {
            this.numero = numero;
            this.vertice = vertice;
            this.motivo = motivo;
        }

        @Override
        public String toString() {
            return numero + " -> " + vertice + " (" + motivo + ")";
        }
    }

    /**
     * Resultado de la numeracion ordinal.
     */
    public static class Resultado {

        // Mapa vertice -> numero ordinal asignado (en orden de asignacion)
        public final LinkedHashMap<String, Integer> numeracion;
        // Pasos del proceso para mostrar al usuario
        public final ArrayList<Paso> pasos;
        // true si el grafo es dirigido
        public final boolean dirigido;

        public Resultado(LinkedHashMap<String, Integer> numeracion,
                ArrayList<Paso> pasos,
                boolean dirigido) {
            this.numeracion = new LinkedHashMap<>(numeracion);
            this.pasos = new ArrayList<>(pasos);
            this.dirigido = dirigido;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== NUMERACIÓN ORDINAL (")
                    .append(dirigido ? "DIRIGIDO" : "NO DIRIGIDO")
                    .append(") ===\n\n");

            if (dirigido) {
                sb.append("--- Paso a paso ---\n");
                for (Paso p : pasos) {
                    sb.append("  ").append(p).append("\n");
                }
                sb.append("\n");
            }

            sb.append("--- Resultado ---\n");
            for (String v : numeracion.keySet()) {
                sb.append("  ").append(v).append(" = ").append(numeracion.get(v)).append("\n");
            }
            return sb.toString();
        }
    }

    // =====================================================================
    //  NO DIRIGIDO — Grafo.java
    //  Numeracion en orden de insercion (izquierda a derecha)
    // =====================================================================
    /**
     * Numeracion ordinal para grafo NO dirigido. Los vertices se numeran en el
     * orden en que fueron ingresados al grafo.
     *
     * @param grafo Grafo no dirigido (Grafo.java).
     * @return Resultado con la numeracion.
     * @throws IllegalArgumentException si el grafo esta vacio.
     */
    public static Resultado calcularNoDirigido(Grafo grafo) {
        if (grafo == null || grafo.getNumVertices() == 0) {
            throw new IllegalArgumentException("El grafo no puede estar vacío.");
        }

        ArrayList<String> vertices = grafo.getVertices();
        LinkedHashMap<String, Integer> numeracion = new LinkedHashMap<>();
        ArrayList<Paso> pasos = new ArrayList<>();

        int num = 1;
        for (String v : vertices) {
            numeracion.put(v, num);
            pasos.add(new Paso(num, v, "orden de inserción"));
            num++;
        }

        return new Resultado(numeracion, pasos, false);
    }

    // =====================================================================
    //  DIRIGIDO — GrafoPonderado.java
    //  Ordenamiento topologico por predecesores
    // =====================================================================
    /**
     * Numeracion ordinal para grafo DIRIGIDO (GrafoPonderado).
     *
     * Algoritmo: 1. Buscar nodos sin predecesor (nadie apunta hacia ellos). 2.
     * Numerarlos uno por uno en orden de aparicion. 3. Marcarlos como numerados
     * y repetir: un nodo puede numerarse cuando todos sus predecesores ya estan
     * numerados. 4. Continuar hasta numerar todos los nodos.
     *
     * @param grafo Grafo dirigido ponderado.
     * @return Resultado con la numeracion y el paso a paso.
     * @throws IllegalArgumentException si el grafo esta vacio o tiene ciclos
     * (no se puede hacer ordenamiento topologico).
     */
    public static Resultado calcularDirigido(GrafoPonderado grafo) {
        if (grafo == null || grafo.getNumVertices() == 0) {
            throw new IllegalArgumentException("El grafo no puede estar vacío.");
        }

        ArrayList<String> vertices = grafo.getVertices();
        ArrayList<GrafoPonderado.AristaPonderada> aristas = grafo.getAristas();

        LinkedHashMap<String, Integer> numeracion = new LinkedHashMap<>();
        ArrayList<Paso> pasos = new ArrayList<>();
        ArrayList<String> numerados = new ArrayList<>(); // ya tienen numero asignado

        int num = 1;
        int maxIteraciones = vertices.size(); // evitar loop infinito si hay ciclo

        while (numerados.size() < vertices.size()) {
            // Buscar candidatos: vertices no numerados cuyo todos los predecesores
            // ya estan numerados (o no tienen predecesores)
            ArrayList<String> candidatos = new ArrayList<>();

            for (String v : vertices) {
                if (numerados.contains(v)) {
                    continue; // ya numerado
                }
                // Obtener predecesores de v (nodos que tienen arista hacia v)
                ArrayList<String> predecesores = getPredecesores(v, aristas);

                if (predecesores.isEmpty()) {
                    // Sin predecesor -> candidato directo
                    candidatos.add(v);
                } else {
                    // Tiene predecesores -> candidato solo si TODOS ya estan numerados
                    boolean todosNumerados = true;
                    for (String pred : predecesores) {
                        if (!numerados.contains(pred)) {
                            todosNumerados = false;
                            break;
                        }
                    }
                    if (todosNumerados) {
                        candidatos.add(v);
                    }
                }
            }

            // Si no hay candidatos y quedan vertices sin numerar -> hay ciclo
            if (candidatos.isEmpty()) {
                ArrayList<String> sinNumerar = new ArrayList<>();
                for (String v : vertices) {
                    if (!numerados.contains(v)) {
                        sinNumerar.add(v);
                    }
                }
                throw new IllegalArgumentException(
                        "El grafo tiene un ciclo. No se puede hacer ordenamiento topológico. "
                        + "Vértices sin numerar: " + sinNumerar.toString());
            }

            // Numerar candidatos uno por uno en orden de aparicion en el grafo
            for (String candidato : candidatos) {
                ArrayList<String> predecesores = getPredecesores(candidato, aristas);

                String motivo;
                if (predecesores.isEmpty()) {
                    motivo = "sin predecesor";
                } else {
                    motivo = "predecesores " + predecesores.toString() + " ya numerados";
                }

                numeracion.put(candidato, num);
                pasos.add(new Paso(num, candidato, motivo));
                numerados.add(candidato);
                num++;
            }
        }

        return new Resultado(numeracion, pasos, true);
    }

    /**
     * Retorna la lista de predecesores de un vertice v. Un predecesor es un
     * nodo u tal que existe la arista u -> v.
     */
    private static ArrayList<String> getPredecesores(
            String v, ArrayList<GrafoPonderado.AristaPonderada> aristas) {
        ArrayList<String> predecesores = new ArrayList<>();
        for (GrafoPonderado.AristaPonderada a : aristas) {
            if (a.destino.equals(v)) {
                predecesores.add(a.origen);
            }
        }
        return predecesores;
    }
}
