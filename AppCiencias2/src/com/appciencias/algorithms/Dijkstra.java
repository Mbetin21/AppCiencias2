package com.appciencias.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

/**
 * Algoritmo de Dijkstra — Camino Minimo con etiquetas.
 *
 * Etiqueta de cada nodo: {d, N}
 *   d = distancia acumulada desde el origen
 *   N = nodo del cual llega esa distancia (predecesor)
 *
 * Estados de etiqueta:
 *   TEMPORAL
 *   PERMANENTE
 *
 * Solo funciona sobre GrafoPonderado (dirigido con pesos positivos).
 */
public class Dijkstra {

    /**
     * Estado de una etiqueta.
     */
    public enum EstadoEtiqueta {
        TEMPORAL, PERMANENTE
    }

    /**
     * Etiqueta {d, N} de un nodo.
     */
    public static class Etiqueta {
        public final double distancia;  // d: distancia acumulada desde origen
        public final String predecesor; // N: nodo del que viene (null si es origen)
        public final EstadoEtiqueta estado;

        public Etiqueta(double distancia, String predecesor, EstadoEtiqueta estado) {
            this.distancia  = distancia;
            this.predecesor = predecesor;
            this.estado     = estado;
        }

        @Override
        public String toString() {
            String d    = (distancia == Double.MAX_VALUE) ? "∞" : String.valueOf(distancia);
            String pred = (predecesor == null) ? "-" : predecesor;
            String est  = (estado == EstadoEtiqueta.PERMANENTE) ? " [P]" : " [T]";
            return "{" + d + ", " + pred + "}" + est;
        }
    }

    /**
     * Registro de un paso del algoritmo.
     */
    public static class PasoDijkstra {
        public final int numeroPaso;
        public final String nodoActual;          // nodo que se hizo permanente en este paso
        public final Etiqueta etiquetaPermanente; // su etiqueta definitiva
        // Etiquetas temporales actualizadas en este paso: nodo -> etiqueta
        public final LinkedHashMap<String, Etiqueta> temporalesActualizadas;
        // Snapshot de todas las etiquetas al final del paso
        public final LinkedHashMap<String, Etiqueta> snapshotEtiquetas;

        public PasoDijkstra(int numeroPaso, String nodoActual,
                            Etiqueta etiquetaPermanente,
                            LinkedHashMap<String, Etiqueta> temporalesActualizadas,
                            LinkedHashMap<String, Etiqueta> snapshotEtiquetas) {
            this.numeroPaso            = numeroPaso;
            this.nodoActual            = nodoActual;
            this.etiquetaPermanente    = etiquetaPermanente;
            this.temporalesActualizadas = new LinkedHashMap<>(temporalesActualizadas);
            this.snapshotEtiquetas     = new LinkedHashMap<>(snapshotEtiquetas);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Paso ").append(numeroPaso).append(": ")
              .append(nodoActual).append(" -> PERMANENTE ")
              .append(etiquetaPermanente).append("\n");

            if (!temporalesActualizadas.isEmpty()) {
                sb.append("  Etiquetas temporales actualizadas:\n");
                for (String v : temporalesActualizadas.keySet()) {
                    sb.append("    ").append(v).append(": ")
                      .append(temporalesActualizadas.get(v)).append("\n");
                }
            }

            sb.append("  Estado actual de etiquetas:\n");
            for (String v : snapshotEtiquetas.keySet()) {
                sb.append("    ").append(v).append(": ")
                  .append(snapshotEtiquetas.get(v)).append("\n");
            }
            return sb.toString();
        }
    }

    /**
     * Resultado completo de Dijkstra.
     */
    public static class Resultado {
        public final LinkedHashMap<String, Integer> ordinal;
        // Etiqueta final de cada nodo (permanente o temporal si no fue alcanzado)
        public final LinkedHashMap<String, Etiqueta> etiquetasFinales;
        public final ArrayList<PasoDijkstra> pasos;
        public final String nodoOrigen;
        public final String nodoDestino;
        // Camino minimo reconstruido origen -> destino
        public final ArrayList<String> camino;
        public final double costoTotal;

        public Resultado(LinkedHashMap<String, Integer> ordinal,
                         LinkedHashMap<String, Etiqueta> etiquetasFinales,
                         ArrayList<PasoDijkstra> pasos,
                         String nodoOrigen, String nodoDestino) {
            this.ordinal         = new LinkedHashMap<>(ordinal);
            this.etiquetasFinales = new LinkedHashMap<>(etiquetasFinales);
            this.pasos           = new ArrayList<>(pasos);
            this.nodoOrigen      = nodoOrigen;
            this.nodoDestino     = nodoDestino;
            this.camino          = reconstruirCamino(etiquetasFinales, nodoOrigen, nodoDestino);
            Etiqueta ed          = etiquetasFinales.get(nodoDestino);
            this.costoTotal      = (ed != null) ? ed.distancia : Double.MAX_VALUE;
        }

        private static ArrayList<String> reconstruirCamino(
                LinkedHashMap<String, Etiqueta> etiquetas,
                String origen, String destino) {
            ArrayList<String> camino = new ArrayList<>();
            String actual = destino;
            int maxPasos  = etiquetas.size() + 2;
            int contador  = 0;
            while (actual != null && contador < maxPasos) {
                camino.add(actual);
                if (actual.equals(origen)) break;
                Etiqueta e = etiquetas.get(actual);
                actual = (e != null) ? e.predecesor : null;
                contador++;
            }
            if (camino.isEmpty() || !camino.get(camino.size() - 1).equals(origen)) {
                return new ArrayList<>();
            }
            Collections.reverse(camino);
            return camino;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== DIJKSTRA — CAMINO MÍNIMO ===\n\n");

            sb.append("--- Numeración ordinal ---\n");
            for (String v : ordinal.keySet()) {
                sb.append("  ").append(v)
                  .append(" -> ordinal ").append(ordinal.get(v)).append("\n");
            }

            sb.append("\n--- Paso a paso ---\n");
            for (PasoDijkstra p : pasos) {
                sb.append(p).append("\n");
            }

            sb.append("--- Etiquetas finales ---\n");
            for (String v : etiquetasFinales.keySet()) {
                sb.append("  ").append(v).append(": ")
                  .append(etiquetasFinales.get(v)).append("\n");
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
     * Ejecuta Dijkstra desde nodoOrigen hasta nodoDestino.
     *
     * @param grafo       Grafo dirigido ponderado con pesos positivos.
     * @param nodoOrigen  Vertice de inicio.
     * @param nodoDestino Vertice al que se quiere llegar.
     * @return Resultado con etiquetas, pasos y camino reconstruido.
     */
    public static Resultado calcular(GrafoPonderado grafo,
                                     String nodoOrigen, String nodoDestino) {
        if (grafo == null || grafo.getNumVertices() == 0) {
            throw new IllegalArgumentException("El grafo no puede estar vacío.");
        }

        nodoOrigen  = nodoOrigen.trim().toLowerCase();
        nodoDestino = nodoDestino.trim().toLowerCase();

        if (!grafo.contieneVertice(nodoOrigen)) {
            throw new IllegalArgumentException(
                "El nodo origen '" + nodoOrigen + "' no existe.");
        }
        if (!grafo.contieneVertice(nodoDestino)) {
            throw new IllegalArgumentException(
                "El nodo destino '" + nodoDestino + "' no existe.");
        }

        // Paso 1: numeracion ordinal
        Ordinal.Resultado ordinalRes = Ordinal.calcularDirigido(grafo);
        LinkedHashMap<String, Integer> numeracion = ordinalRes.numeracion;

        ArrayList<GrafoPonderado.AristaPonderada> aristas = grafo.getAristas();
        ArrayList<String> vertices = grafo.getVertices();

        // Paso 2: inicializar etiquetas — todas sin etiqueta (null = no visitado)
        LinkedHashMap<String, Etiqueta> etiquetas = new LinkedHashMap<>();
        for (String v : vertices) {
            etiquetas.put(v, null);
        }

        // Nodo origen: etiqueta permanente {0, null}
        etiquetas.put(nodoOrigen, new Etiqueta(0.0, null, EstadoEtiqueta.PERMANENTE));

        ArrayList<PasoDijkstra> pasos = new ArrayList<>();
        int numeroPaso = 1;
        String nodoActual = nodoOrigen;

        // Paso 3: iterar hasta que el destino sea permanente o no haya mas temporales
        while (true) {
            LinkedHashMap<String, Etiqueta> temporalesActualizadas = new LinkedHashMap<>();
            Etiqueta etActual = etiquetas.get(nodoActual);

            // Explorar aristas que salen del nodo actual
            for (GrafoPonderado.AristaPonderada arista : aristas) {
                if (!arista.origen.equals(nodoActual)) continue;

                String vDestino = arista.destino;
                Etiqueta etDestino = etiquetas.get(vDestino);

                // Si ya es permanente no se toca
                if (etDestino != null && etDestino.estado == EstadoEtiqueta.PERMANENTE) continue;

                double nuevaDistancia = etActual.distancia + arista.peso;
                Etiqueta nuevaEtiqueta = new Etiqueta(nuevaDistancia, nodoActual,
                                                      EstadoEtiqueta.TEMPORAL);

                // Si no tenia etiqueta o la nueva distancia es menor -> actualizar
                if (etDestino == null || nuevaDistancia < etDestino.distancia) {
                    etiquetas.put(vDestino, nuevaEtiqueta);
                    temporalesActualizadas.put(vDestino, nuevaEtiqueta);
                }
                // Si hay empate se conserva la existente (primera encontrada)
            }

            // Snapshot del estado actual
            LinkedHashMap<String, Etiqueta> snapshot = new LinkedHashMap<>();
            for (String v : vertices) {
                if (etiquetas.get(v) != null) snapshot.put(v, etiquetas.get(v));
            }

            pasos.add(new PasoDijkstra(numeroPaso, nodoActual,
                    etiquetas.get(nodoActual), temporalesActualizadas, snapshot));
            numeroPaso++;

            // Si el destino ya es permanente, terminamos
            Etiqueta etDestino = etiquetas.get(nodoDestino);
            if (etDestino != null && etDestino.estado == EstadoEtiqueta.PERMANENTE) break;

            // Elegir la etiqueta temporal de menor distancia para hacerla permanente
            String mejorNodo   = null;
            double mejorDist   = Double.MAX_VALUE;

            for (String v : vertices) {
                Etiqueta et = etiquetas.get(v);
                if (et == null || et.estado == EstadoEtiqueta.PERMANENTE) continue;
                // Temporal con menor distancia (empate: se toma el primero encontrado)
                if (et.distancia < mejorDist) {
                    mejorDist = et.distancia;
                    mejorNodo = v;
                }
            }

            // No quedan temporales -> no hay camino al destino
            if (mejorNodo == null) break;

            // Hacer permanente la mejor etiqueta temporal
            Etiqueta etMejor = etiquetas.get(mejorNodo);
            etiquetas.put(mejorNodo, new Etiqueta(etMejor.distancia, etMejor.predecesor,
                                                   EstadoEtiqueta.PERMANENTE));
            nodoActual = mejorNodo;
        }

        return new Resultado(numeracion, etiquetas, pasos, nodoOrigen, nodoDestino);
    }
}