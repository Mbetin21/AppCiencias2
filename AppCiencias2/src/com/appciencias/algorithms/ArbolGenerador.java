package com.appciencias.algorithms;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Arbol de Expansion Minima y Maxima usando el algoritmo de Kruskal.
 *
 * Recibe un GrafoPonderado y genera:
 * - Expansion minima: aristas de menor a mayor peso, sin ciclos.
 * - Expansion maxima: aristas de mayor a menor peso, sin ciclos.
 * - Ramas: aristas del arbol de expansion minima.
 * - Cuerdas: aristas que NO entraron al arbol de expansion minima
 *            (las que formaban ciclo o sobraron).
 *
 * Deteccion de ciclos: Union-Find (conjuntos disjuntos).
 */
public class ArbolGenerador {

    /**
     * Tipos de expansion disponibles.
     */
    public enum Tipo {
        MINIMA,
        MAXIMA
    }

    /**
     * Registro de una arista analizada durante el proceso.
     */
    public static class PasoKruskal {
        public final int numero;                          // numero de paso
        public final GrafoPonderado.AristaPonderada arista; // arista analizada
        public final boolean aceptada;                    // true = se tomo, false = genera ciclo
        public final String motivo;                       // descripcion del resultado

        public PasoKruskal(int numero, GrafoPonderado.AristaPonderada arista,
                           boolean aceptada, String motivo) {
            this.numero   = numero;
            this.arista   = arista;
            this.aceptada = aceptada;
            this.motivo   = motivo;
        }

        @Override
        public String toString() {
            String estado = aceptada ? "ACEPTADA" : "RECHAZADA";
            return "Paso " + numero + ": " + arista.toString()
                    + " -> " + estado + " (" + motivo + ")";
        }
    }

    /**
     * Resultado completo de una expansion.
     */
    public static class Resultado {
        public final Tipo tipo;
        public final ArrayList<GrafoPonderado.AristaPonderada> arbolExpansion;
        public final ArrayList<GrafoPonderado.AristaPonderada> aristasRechazadas;
        public final ArrayList<PasoKruskal> pasos;
        public final double pesoTotal;
        // Ramas = aristas que SI forman T (arbol minimo)
        // Cuerdas = aristas de G que NO entraron en T (existen en G pero no en T)
        public final ArrayList<GrafoPonderado.AristaPonderada> ramas;
        public final ArrayList<GrafoPonderado.AristaPonderada> cuerdas;

        public Resultado(Tipo tipo,
                         ArrayList<GrafoPonderado.AristaPonderada> arbolExpansion,
                         ArrayList<GrafoPonderado.AristaPonderada> aristasRechazadas,
                         ArrayList<PasoKruskal> pasos) {
            this.tipo              = tipo;
            this.arbolExpansion    = new ArrayList<>(arbolExpansion);
            this.aristasRechazadas = new ArrayList<>(aristasRechazadas);
            this.pasos             = new ArrayList<>(pasos);
            this.ramas             = new ArrayList<>(arbolExpansion);
            this.cuerdas           = new ArrayList<>(aristasRechazadas);
            double suma = 0;
            for (GrafoPonderado.AristaPonderada a : arbolExpansion) suma += a.peso;
            this.pesoTotal = suma;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            String nt = (tipo == Tipo.MINIMA) ? "MÍNIMA" : "MÁXIMA";
            sb.append("=== EXPANSIÓN ").append(nt).append(" ===\n\n");
            sb.append("--- Paso a paso ---\n");
            for (PasoKruskal p : pasos) sb.append(p).append("\n");
            sb.append("\n--- T: Árbol de expansión ").append(nt).append(" ---\n");
            sb.append("Ramas (aristas de T): ").append(ramas.size()).append("\n");
            for (GrafoPonderado.AristaPonderada a : ramas) sb.append("  ").append(a).append("\n");
            sb.append("Peso total de T: ").append(pesoTotal).append("\n");
            sb.append("\n--- T': Complemento (aristas de G no en T) ---\n");
            sb.append("Cuerdas (aristas de T'): ").append(cuerdas.size()).append("\n");
            for (GrafoPonderado.AristaPonderada a : cuerdas) sb.append("  ").append(a).append("\n");
            return sb.toString();
        }
    }

    // ========================= UNION-FIND =========================

    /**
     * Union-Find para deteccion eficiente de ciclos.
     * Cada vertice empieza en su propio conjunto.
     * Si dos vertices ya estan en el mismo conjunto -> agregar la arista formaria ciclo.
     */
    private static class UnionFind {
        private final HashMap<String, String> padre;

        public UnionFind(ArrayList<String> vertices) {
            padre = new HashMap<>();
            for (String v : vertices) {
                padre.put(v, v); // cada vertice es su propio padre
            }
        }

        /** Encuentra la raiz del conjunto de v (con compresion de camino). */
        public String encontrar(String v) {
            if (!padre.get(v).equals(v)) {
                padre.put(v, encontrar(padre.get(v))); // compresion de camino
            }
            return padre.get(v);
        }

        /**
         * Une los conjuntos de v1 y v2.
         * @return true si se unieron (estaban en conjuntos distintos),
         *         false si ya estaban en el mismo conjunto (habria ciclo).
         */
        public boolean unir(String v1, String v2) {
            String raiz1 = encontrar(v1);
            String raiz2 = encontrar(v2);
            if (raiz1.equals(raiz2)) {
                return false; // ya conectados -> ciclo
            }
            padre.put(raiz1, raiz2); // unir los conjuntos
            return true;
        }
    }

    // ========================= ALGORITMO =========================

    /**
     * Calcula el arbol de expansion segun el tipo indicado.
     *
     * @param grafo Grafo ponderado sobre el que se trabaja (NO se modifica).
     * @param tipo  MINIMA o MAXIMA.
     * @return Resultado con pasos, arbol, ramas y cuerdas.
     * @throws IllegalArgumentException si el grafo esta vacio o no tiene aristas.
     */
    public static Resultado calcular(GrafoPonderado grafo, Tipo tipo) {
        if (grafo == null || grafo.getNumVertices() == 0) {
            throw new IllegalArgumentException("El grafo no puede estar vacío.");
        }
        if (grafo.getNumAristas() == 0) {
            throw new IllegalArgumentException("El grafo no tiene aristas.");
        }

        // Copia de aristas ordenadas segun el tipo
        ArrayList<GrafoPonderado.AristaPonderada> aristasOrdenadas =
                new ArrayList<>(grafo.getAristas());

        if (tipo == Tipo.MINIMA) {
            // Menor a mayor
            aristasOrdenadas.sort(Comparator.comparingDouble(a -> a.peso));
        } else {
            // Mayor a menor
            aristasOrdenadas.sort((a, b) -> Double.compare(b.peso, a.peso));
        }

        // Union-Find inicializado con todos los vertices
        UnionFind uf = new UnionFind(grafo.getVertices());

        ArrayList<GrafoPonderado.AristaPonderada> aceptadas  = new ArrayList<>();
        ArrayList<GrafoPonderado.AristaPonderada> rechazadas = new ArrayList<>();
        ArrayList<PasoKruskal> pasos = new ArrayList<>();

        int numeroPaso = 1;
        int verticesNecesarios = grafo.getNumVertices() - 1; // aristas para un arbol = V-1

        for (GrafoPonderado.AristaPonderada arista : aristasOrdenadas) {
            // Intentar unir los dos extremos de la arista
            boolean sePuede = uf.unir(arista.origen, arista.destino);

            if (sePuede) {
                aceptadas.add(arista);
                pasos.add(new PasoKruskal(numeroPaso, arista, true,
                        "No forma ciclo, se agrega al árbol"));
            } else {
                rechazadas.add(arista);
                pasos.add(new PasoKruskal(numeroPaso, arista, false,
                        "Forma ciclo entre " + arista.origen + " y " + arista.destino
                        + ", se descarta"));
            }
            numeroPaso++;

            // Optimizacion: si ya tenemos V-1 aristas el arbol esta completo
            // pero seguimos para identificar todas las cuerdas
        }

        return new Resultado(tipo, aceptadas, rechazadas, pasos);
    }

    /**
     * Calcula ambas expansiones de una vez y retorna las dos.
     * Util para mostrar ramas y cuerdas juntas.
     *
     * @return arreglo de 2: [0] = expansion minima, [1] = expansion maxima
     */
    public static Resultado[] calcularAmbas(GrafoPonderado grafo) {
        return new Resultado[]{
            calcular(grafo, Tipo.MINIMA),
            calcular(grafo, Tipo.MAXIMA)
        };
    }
}