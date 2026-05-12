package com.appciencias.algorithms;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Numeracion Ordinal de vertices de un grafo.
 *
 * No dirigido: se numeran en el orden en que fueron ingresados (izq a derecha).
 *
 * Dirigido: ordenamiento topologico por predecesores. Si el grafo tiene ciclos,
 * los vertices del ciclo se numeran en orden de insercion al final y se marca
 * tieneCiclo = true.
 */
public class Ordinal {

    /**
     * Representa el numero asignado a un vertice en un paso del proceso.
     */
    public static class Paso {

        public final int numero;
        public final String vertice;
        public final String motivo;

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

        public final LinkedHashMap<String, Integer> numeracion;
        public final ArrayList<Paso> pasos;
        public final boolean dirigido;
        public final boolean tieneCiclo; // true si se detecto un ciclo en el grafo

        public Resultado(LinkedHashMap<String, Integer> numeracion,
                ArrayList<Paso> pasos,
                boolean dirigido,
                boolean tieneCiclo) {
            this.numeracion = new LinkedHashMap<>(numeracion);
            this.pasos = new ArrayList<>(pasos);
            this.dirigido = dirigido;
            this.tieneCiclo = tieneCiclo;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== NUMERACIÓN ORDINAL (")
                    .append(dirigido ? "DIRIGIDO" : "NO DIRIGIDO")
                    .append(") ===\n\n");

            if (tieneCiclo) {
                sb.append("⚠ Advertencia: el grafo tiene un ciclo. "
                        + "Los vértices del ciclo se numeraron por orden de inserción.\n\n");
            }

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
    //  NO DIRIGIDO
    // =====================================================================
    /**
     * Numeracion ordinal para grafo NO dirigido. Los vertices se numeran en el
     * orden en que fueron ingresados al grafo.
     *
     * @param grafo Grafo no dirigido (Grafo.java).
     * @return Resultado con la numeracion.
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

        return new Resultado(numeracion, pasos, false, false);
    }

    // =====================================================================
    //  DIRIGIDO
    // =====================================================================
    /**
     * Numeracion ordinal para grafo DIRIGIDO (GrafoPonderado).
     *
     * Algoritmo: 1. Buscar nodos sin predecesor (nadie apunta hacia ellos). 2.
     * Numerarlos uno por uno en orden de aparicion. 3. Marcarlos como numerados
     * y repetir: un nodo puede numerarse cuando todos sus predecesores ya estan
     * numerados. 4. Si no hay candidatos (hay ciclo), numerar los restantes en
     * orden de insercion y marcar tieneCiclo = true.
     *
     * @param grafo Grafo dirigido ponderado.
     * @return Resultado con la numeracion, el paso a paso y flag de ciclo.
     */
    public static Resultado calcularDirigido(GrafoPonderado grafo) {
        if (grafo == null || grafo.getNumVertices() == 0) {
            throw new IllegalArgumentException("El grafo no puede estar vacío.");
        }

        ArrayList<String> vertices = grafo.getVertices();
        ArrayList<GrafoPonderado.AristaPonderada> aristas = grafo.getAristas();

        LinkedHashMap<String, Integer> numeracion = new LinkedHashMap<>();
        ArrayList<Paso> pasos = new ArrayList<>();
        ArrayList<String> numerados = new ArrayList<>();

        int num = 1;
        boolean cicloDetectado = false;

        while (numerados.size() < vertices.size()) {

            // Buscar candidatos: vertices cuyos predecesores ya estan todos numerados
            ArrayList<String> candidatos = new ArrayList<>();

            for (String v : vertices) {
                if (numerados.contains(v)) {
                    continue;
                }

                ArrayList<String> predecesores = getPredecesores(v, aristas);

                if (predecesores.isEmpty()) {
                    candidatos.add(v);
                } else {
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

            // Si no hay candidatos -> hay ciclo
            if (candidatos.isEmpty()) {
                cicloDetectado = true;
                // Numerar restantes en orden de insercion
                for (String v : vertices) {
                    if (!numerados.contains(v)) {
                        numeracion.put(v, num);
                        pasos.add(new Paso(num, v,
                                "ciclo detectado — numerado por orden de inserción"));
                        numerados.add(v);
                        num++;
                    }
                }
                break;
            }

            // Numerar candidatos uno por uno en orden de aparicion
            for (String candidato : candidatos) {
                ArrayList<String> predecesores = getPredecesores(candidato, aristas);
                String motivo = predecesores.isEmpty()
                        ? "sin predecesor"
                        : "predecesores " + predecesores.toString() + " ya numerados";

                numeracion.put(candidato, num);
                pasos.add(new Paso(num, candidato, motivo));
                numerados.add(candidato);
                num++;
            }
        }

        return new Resultado(numeracion, pasos, true, cicloDetectado);
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
