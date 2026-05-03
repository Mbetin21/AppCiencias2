package com.appciencias.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

/**
 * Algoritmo de Bellman — Camino Minimo.
 *
 * El usuario elige el nodo origen y el nodo destino. Si el origen no es el nodo
 * ordinal 1, se ignoran todos los nodos con ordinal menor al origen y se
 * arranca Bellman desde ahi.
 *
 * Algoritmo: 1. Aplicar numeracion ordinal al grafo completo. 2. Tomar como
 * punto de partida el nodo indicado por el usuario. λ_origen = 0. Los nodos
 * anteriores en ordinal se descartan. 3. Para cada nodo j en orden ordinal
 * desde el origen hasta el destino: λj = min( λi + θij ) para todo i predecesor
 * de j ya calculado. 4. Reconstruir el camino minimo desde origen hasta destino
 * guardando el predecesor que dio el minimo en cada paso.
 *
 * Solo funciona sobre GrafoPonderado (dirigido con pesos).
 */
public class Bellman {

    /**
     * Detalle del calculo de lambda para un nodo.
     */
    public static class PasoBellman {

        public final int ordinal;                  // numero ordinal del nodo
        public final String vertice;               // nombre del vertice
        public final double lambda;                // valor lambda asignado
        public final ArrayList<String> candidatos; // detalle de cada opcion evaluada

        public PasoBellman(int ordinal, String vertice, double lambda,
                ArrayList<String> candidatos) {
            this.ordinal = ordinal;
            this.vertice = vertice;
            this.lambda = lambda;
            this.candidatos = new ArrayList<>(candidatos);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("λ[").append(vertice).append("] = ");
            if (candidatos.isEmpty()) {
                sb.append("0 (nodo origen)");
            } else if (candidatos.size() == 1) {
                sb.append(candidatos.get(0));
            } else {
                sb.append("min{ ").append(String.join(" , ", candidatos)).append(" }");
            }
            sb.append(" = ");
            sb.append(lambda == Double.MAX_VALUE ? "∞" : lambda);
            return sb.toString();
        }
    }

    /**
     * Resultado completo de Bellman.
     */
    public static class Resultado {

        // Numeracion ordinal completa del grafo
        public final LinkedHashMap<String, Integer> ordinal;
        // Lambdas calculados (solo nodos desde origen hasta destino)
        public final LinkedHashMap<String, Double> lambdas;
        // Predecesor de cada nodo en el camino minimo
        // predecesor.get("b") = "a" -> en el camino minimo se llega a b desde a
        public final LinkedHashMap<String, String> predecesor;
        // Pasos del calculo
        public final ArrayList<PasoBellman> pasos;
        // Nodo origen y destino indicados por el usuario
        public final String nodoOrigen;
        public final String nodoDestino;
        // Camino minimo reconstruido: lista de vertices en orden origen->destino
        public final ArrayList<String> camino;
        // Costo total del camino minimo
        public final double costoTotal;

        public Resultado(LinkedHashMap<String, Integer> ordinal,
                LinkedHashMap<String, Double> lambdas,
                LinkedHashMap<String, String> predecesor,
                ArrayList<PasoBellman> pasos,
                String nodoOrigen,
                String nodoDestino) {
            this.ordinal = new LinkedHashMap<>(ordinal);
            this.lambdas = new LinkedHashMap<>(lambdas);
            this.predecesor = new LinkedHashMap<>(predecesor);
            this.pasos = new ArrayList<>(pasos);
            this.nodoOrigen = nodoOrigen;
            this.nodoDestino = nodoDestino;
            this.camino = reconstruirCamino(predecesor, nodoOrigen, nodoDestino);
            double costo = lambdas.getOrDefault(nodoDestino, Double.MAX_VALUE);
            this.costoTotal = costo;
        }

        /**
         * Reconstruye el camino minimo siguiendo los predecesores hacia atras.
         * Retorna lista vacia si no hay camino.
         */
        private static ArrayList<String> reconstruirCamino(
                LinkedHashMap<String, String> predecesor,
                String origen, String destino) {
            ArrayList<String> camino = new ArrayList<>();
            String actual = destino;
            int maxPasos = predecesor.size() + 2; // evitar loop infinito
            int contador = 0;
            while (actual != null && contador < maxPasos) {
                camino.add(actual);
                if (actual.equals(origen)) {
                    break;
                }
                actual = predecesor.get(actual);
                contador++;
            }
            // Si no se llego al origen no hay camino valido
            if (camino.isEmpty() || !camino.get(camino.size() - 1).equals(origen)) {
                return new ArrayList<>();
            }
            Collections.reverse(camino);
            return camino;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== BELLMAN — CAMINO MÍNIMO ===\n\n");

            sb.append("--- Numeración ordinal ---\n");
            for (String v : ordinal.keySet()) {
                sb.append("  ").append(v)
                        .append(" -> ordinal ").append(ordinal.get(v)).append("\n");
            }

            sb.append("\n--- Cálculo de lambdas (desde '")
                    .append(nodoOrigen).append("') ---\n");
            for (PasoBellman p : pasos) {
                sb.append("  ").append(p).append("\n");
            }

            sb.append("\n--- Camino mínimo de '")
                    .append(nodoOrigen).append("' a '").append(nodoDestino).append("' ---\n");

            if (camino.isEmpty()) {
                sb.append("  No existe camino desde '").append(nodoOrigen)
                        .append("' hasta '").append(nodoDestino).append("'\n");
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
     * Ejecuta Bellman desde un nodo origen hasta un nodo destino.
     *
     * Si el origen no es el primero en ordinal, los nodos anteriores se ignoran
     * y el algoritmo arranca desde el origen indicado.
     *
     * @param grafo Grafo dirigido ponderado.
     * @param nodoOrigen Vertice desde donde se calcula el camino minimo.
     * @param nodoDestino Vertice al que se quiere llegar.
     * @return Resultado con lambdas, camino reconstruido y costo total.
     * @throws IllegalArgumentException si el grafo esta vacio, tiene ciclos, o
     * los nodos no existen o el origen tiene ordinal mayor o igual al destino.
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
                    "El nodo origen '" + nodoOrigen + "' no existe en el grafo.");
        }
        if (!grafo.contieneVertice(nodoDestino)) {
            throw new IllegalArgumentException(
                    "El nodo destino '" + nodoDestino + "' no existe en el grafo.");
        }

        // Paso 1: numeracion ordinal completa
        Ordinal.Resultado ordinalRes = Ordinal.calcularDirigido(grafo);
        LinkedHashMap<String, Integer> numeracion = ordinalRes.numeracion;

        int ordinalOrigen = numeracion.get(nodoOrigen);
        int ordinalDestino = numeracion.get(nodoDestino);

        if (ordinalOrigen >= ordinalDestino) {
            throw new IllegalArgumentException(
                    "El nodo origen '" + nodoOrigen + "' (ordinal " + ordinalOrigen
                    + ") debe tener ordinal menor al destino '"
                    + nodoDestino + "' (ordinal " + ordinalDestino + ").");
        }

        // Invertir mapa: numero -> vertice
        LinkedHashMap<Integer, String> porOrdinal = new LinkedHashMap<>();
        for (String v : numeracion.keySet()) {
            porOrdinal.put(numeracion.get(v), v);
        }

        ArrayList<GrafoPonderado.AristaPonderada> aristas = grafo.getAristas();

        // Paso 2: inicializar estructuras solo para nodos en rango [origen..destino]
        LinkedHashMap<String, Double> lambdas = new LinkedHashMap<>();
        LinkedHashMap<String, String> predecesor = new LinkedHashMap<>();
        ArrayList<PasoBellman> pasos = new ArrayList<>();

        for (int ord = ordinalOrigen; ord <= ordinalDestino; ord++) {
            lambdas.put(porOrdinal.get(ord), Double.MAX_VALUE);
        }
        lambdas.put(nodoOrigen, 0.0);

        // Paso del nodo origen: lambda = 0
        pasos.add(new PasoBellman(ordinalOrigen, nodoOrigen, 0.0, new ArrayList<>()));

        // Paso 3: calcular lambda en orden ordinal
        for (int ord = ordinalOrigen + 1; ord <= ordinalDestino; ord++) {
            String vj = porOrdinal.get(ord);
            ArrayList<String> candidatos = new ArrayList<>();
            double minLambda = Double.MAX_VALUE;
            String mejorPrec = null;

            for (GrafoPonderado.AristaPonderada arista : aristas) {
                if (!arista.destino.equals(vj)) {
                    continue;
                }

                String vi = arista.origen;

                // Solo predecesores dentro del rango calculado
                if (!lambdas.containsKey(vi)) {
                    continue;
                }

                double lambdaI = lambdas.get(vi);

                if (lambdaI == Double.MAX_VALUE) {
                    candidatos.add("λ[" + vi + "](∞) + θ("
                            + vi + "→" + vj + ")(" + arista.peso + ") = ∞");
                    continue;
                }

                double valor = lambdaI + arista.peso;
                candidatos.add("λ[" + vi + "](" + lambdaI + ") + θ("
                        + vi + "→" + vj + ")(" + arista.peso + ") = " + valor);

                if (valor < minLambda) {
                    minLambda = valor;
                    mejorPrec = vi;
                }
            }

            if (candidatos.isEmpty()) {
                candidatos.add("sin predecesores en el rango");
            }

            lambdas.put(vj, minLambda);
            if (mejorPrec != null) {
                predecesor.put(vj, mejorPrec);
            }

            pasos.add(new PasoBellman(ord, vj, minLambda, candidatos));
        }

        return new Resultado(numeracion, lambdas, predecesor, pasos, nodoOrigen, nodoDestino);
    }
}
