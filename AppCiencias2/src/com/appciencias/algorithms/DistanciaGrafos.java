package com.appciencias.algorithms;

import java.util.ArrayList;

/**
 * Distancia entre dos grafos ponderados.
 *
 * Proceso:
 * 1. Se calcula el arbol de expansion minima de G1 -> T1
 * 2. Se calcula el arbol de expansion minima de G2 -> T2
 * 3. Se hace T1 union T2     -> aristas que estan en T1 O en T2
 * 4. Se hace T1 interseccion T2 -> aristas que estan en T1 Y en T2
 *    (dos aristas son iguales si coinciden origen, destino Y peso)
 * 5. Distancia = (suma pesos T1∪T2 - suma pesos T1∩T2) / 2
 */
public class DistanciaGrafos {

    /**
     * Resultado completo del calculo de distancia.
     */
    public static class Resultado {
        // Arboles minimos de cada grafo
        public final ArbolGenerador.Resultado t1;
        public final ArbolGenerador.Resultado t2;

        // T1 union T2
        public final ArrayList<GrafoPonderado.AristaPonderada> union;
        public final double pesoUnion;

        // T1 interseccion T2
        public final ArrayList<GrafoPonderado.AristaPonderada> interseccion;
        public final double pesoInterseccion;

        // Distancia final
        public final double distancia;

        public Resultado(ArbolGenerador.Resultado t1,
                         ArbolGenerador.Resultado t2,
                         ArrayList<GrafoPonderado.AristaPonderada> union,
                         ArrayList<GrafoPonderado.AristaPonderada> interseccion) {
            this.t1            = t1;
            this.t2            = t2;
            this.union         = new ArrayList<>(union);
            this.interseccion  = new ArrayList<>(interseccion);

            double su = 0;
            for (GrafoPonderado.AristaPonderada a : union) su += a.peso;
            this.pesoUnion = su;

            double si = 0;
            for (GrafoPonderado.AristaPonderada a : interseccion) si += a.peso;
            this.pesoInterseccion = si;

            // Formula: (suma T1∪T2 - suma T1∩T2) / 2
            this.distancia = (pesoUnion - pesoInterseccion) / 2.0;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append("=== DISTANCIA ENTRE GRAFOS ===\n\n");

            sb.append("--- T1 (expansion minima de G1) ---\n");
            sb.append("Ramas de T1: ").append(t1.ramas.size()).append("\n");
            for (GrafoPonderado.AristaPonderada a : t1.ramas) {
                sb.append("  ").append(a).append("\n");
            }
            sb.append("Peso T1: ").append(t1.pesoTotal).append("\n\n");

            sb.append("--- T2 (expansion minima de G2) ---\n");
            sb.append("Ramas de T2: ").append(t2.ramas.size()).append("\n");
            for (GrafoPonderado.AristaPonderada a : t2.ramas) {
                sb.append("  ").append(a).append("\n");
            }
            sb.append("Peso T2: ").append(t2.pesoTotal).append("\n\n");

            sb.append("--- T1 ∪ T2 ---\n");
            sb.append("Aristas: ").append(union.size()).append("\n");
            for (GrafoPonderado.AristaPonderada a : union) {
                sb.append("  ").append(a).append("\n");
            }
            sb.append("Suma de pesos T1∪T2: ").append(pesoUnion).append("\n\n");

            sb.append("--- T1 ∩ T2 ---\n");
            sb.append("Aristas: ").append(interseccion.size()).append("\n");
            for (GrafoPonderado.AristaPonderada a : interseccion) {
                sb.append("  ").append(a).append("\n");
            }
            sb.append("Suma de pesos T1∩T2: ").append(pesoInterseccion).append("\n\n");

            sb.append("--- Cálculo ---\n");
            sb.append("D(G1, G2) = (").append(pesoUnion)
              .append(" - ").append(pesoInterseccion)
              .append(") / 2 = ").append(distancia).append("\n");

            return sb.toString();
        }
    }

    /**
     * Calcula la distancia entre dos grafos ponderados.
     *
     * @param g1 Primer grafo ponderado.
     * @param g2 Segundo grafo ponderado.
     * @return Resultado con T1, T2, union, interseccion y distancia.
     * @throws IllegalArgumentException si alguno de los grafos esta vacio.
     */
    public static Resultado calcular(GrafoPonderado g1, GrafoPonderado g2) {
        if (g1 == null || g1.getNumVertices() == 0) {
            throw new IllegalArgumentException("G1 no puede estar vacío.");
        }
        if (g2 == null || g2.getNumVertices() == 0) {
            throw new IllegalArgumentException("G2 no puede estar vacío.");
        }

        // Paso 1 y 2: expansion minima de cada grafo
        ArbolGenerador.Resultado t1 = ArbolGenerador.calcular(g1, ArbolGenerador.Tipo.MINIMA);
        ArbolGenerador.Resultado t2 = ArbolGenerador.calcular(g2, ArbolGenerador.Tipo.MINIMA);

        // Paso 3: T1 union T2
        // Una arista de T2 entra en la union solo si no esta ya en T1
        ArrayList<GrafoPonderado.AristaPonderada> union = new ArrayList<>(t1.ramas);
        for (GrafoPonderado.AristaPonderada a2 : t2.ramas) {
            if (!contieneArista(union, a2)) {
                union.add(a2);
            }
        }

        // Paso 4: T1 interseccion T2
        // Una arista esta en la interseccion si esta en T1 Y en T2
        // (coinciden origen, destino Y peso)
        ArrayList<GrafoPonderado.AristaPonderada> interseccion = new ArrayList<>();
        for (GrafoPonderado.AristaPonderada a1 : t1.ramas) {
            if (contieneArista(t2.ramas, a1)) {
                interseccion.add(a1);
            }
        }

        return new Resultado(t1, t2, union, interseccion);
    }

    /**
     * Verifica si una lista contiene una arista.
     * Dos aristas son iguales si coinciden origen, destino Y peso.
     */
    private static boolean contieneArista(ArrayList<GrafoPonderado.AristaPonderada> lista,
                                          GrafoPonderado.AristaPonderada arista) {
        for (GrafoPonderado.AristaPonderada a : lista) {
            if (a.origen.equals(arista.origen)
                    && a.destino.equals(arista.destino)
                    && Double.compare(a.peso, arista.peso) == 0) {
                return true;
            }
        }
        return false;
    }
}