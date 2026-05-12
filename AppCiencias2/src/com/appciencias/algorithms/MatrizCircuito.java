package com.appciencias.algorithms;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Matriz de Circuitos y Matriz de Circuitos Fundamentales.
 *
 * Recibe un GrafoPonderado NO DIRIGIDO (creado con dirigido=false). Si las
 * aristas tienen peso 1 se comporta como no ponderado (matriz de 0s y 1s). Si
 * las aristas tienen pesos distintos, la matriz muestra el peso en vez de 1.
 *
 * MATRIZ DE CIRCUITOS: - Filas = todos los circuitos simples del grafo (C1, C2,
 * ...) - Columnas = aristas del grafo (identificadas como "origen-destino") -
 * Valor = peso de la arista si pertenece al circuito, 0 si no pertenece
 *
 * MATRIZ DE CIRCUITOS FUNDAMENTALES: - Se obtiene el arbol de expansion minima
 * T y sus cuerdas (aristas fuera de T) - Por cada cuerda se busca el circuito
 * fundamental: el unico circuito que contiene ESA cuerda y solo aristas de T
 * (ninguna otra cuerda) - Nulidad = numero de cuerdas = numero de circuitos
 * fundamentales - Misma estructura de matriz que la de circuitos
 */
public class MatrizCircuito {

    /**
     * Representa una arista identificada por nombre para las columnas de la
     * matriz. En grafo no dirigido, "a-b" y "b-a" son la misma arista. Se usa
     * siempre el par en orden lexicografico para evitar duplicados.
     */
    public static class AristaId {

        public final String nombre;  // "origen-destino" en orden lexico
        public final double peso;

        public AristaId(String v1, String v2, double peso) {
            // Orden lexicografico para que "a-b" == "b-a"
            if (v1.compareTo(v2) <= 0) {
                this.nombre = v1 + "-" + v2;
            } else {
                this.nombre = v2 + "-" + v1;
            }
            this.peso = peso;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof AristaId)) {
                return false;
            }
            return nombre.equals(((AristaId) o).nombre);
        }

        @Override
        public int hashCode() {
            return nombre.hashCode();
        }

        @Override
        public String toString() {
            return nombre;
        }
    }

    /**
     * Resultado de la matriz de circuitos (simples o fundamentales).
     */
    public static class MatrizResultado {

        // Nombres de los circuitos: "C1", "C2", ... o "Cf1", "Cf2", ...
        public final ArrayList<String> nombresCircuitos;
        // Aristas (columnas de la matriz)
        public final ArrayList<AristaId> aristas;
        // Circuitos: cada circuito es la lista de aristas que lo forman
        public final ArrayList<ArrayList<AristaId>> circuitos;
        // Matriz: matriz[i][j] = peso si circuito i contiene arista j, 0 si no
        public final double[][] matriz;
        // Nulidad (solo relevante para circuitos fundamentales)
        public final int nulidad;

        public MatrizResultado(ArrayList<String> nombresCircuitos,
                ArrayList<AristaId> aristas,
                ArrayList<ArrayList<AristaId>> circuitos,
                int nulidad) {
            this.nombresCircuitos = new ArrayList<>(nombresCircuitos);
            this.aristas = new ArrayList<>(aristas);
            this.circuitos = circuitos;
            this.nulidad = nulidad;

            int filas = circuitos.size();
            int cols = aristas.size();
            this.matriz = new double[filas][cols];

            for (int i = 0; i < filas; i++) {
                for (int j = 0; j < cols; j++) {
                    AristaId a = aristas.get(j);
                    // Buscar si la arista j esta en el circuito i
                    boolean encontrada = false;
                    for (AristaId ca : circuitos.get(i)) {
                        if (ca.equals(a)) {
                            matriz[i][j] = ca.peso;
                            encontrada = true;
                            break;
                        }
                    }
                    if (!encontrada) {
                        matriz[i][j] = 0;
                    }
                }
            }
        }

        /**
         * Formatea la matriz para mostrar al usuario.
         */
        public String formatear() {
            int filas = nombresCircuitos.size();
            int cols = aristas.size();
            StringBuilder sb = new StringBuilder();

            // Encabezado columnas (aristas)
            sb.append(String.format("%-6s", ""));
            for (AristaId a : aristas) {
                sb.append(String.format("%-8s", a.nombre));
            }
            sb.append("\n");

            // Filas de circuitos
            for (int i = 0; i < filas; i++) {
                sb.append(String.format("%-6s", nombresCircuitos.get(i)));
                for (int j = 0; j < cols; j++) {
                    String val = (matriz[i][j] == 0) ? "0"
                            : (matriz[i][j] == 1.0) ? "1"
                                    : String.valueOf(matriz[i][j]);
                    sb.append(String.format("%-8s", val));
                }
                sb.append("\n");
            }

            if (nulidad > 0) {
                sb.append("\nNulidad = ").append(nulidad).append("\n");
            }

            return sb.toString();
        }

        @Override
        public String toString() {
            return formatear();
        }
    }

    /**
     * Resultado completo: ambas matrices.
     */
    public static class Resultado {

        public final MatrizResultado matrizCircuitos;
        public final MatrizResultado matrizCircuitosFundamentales;
        // Cuerdas usadas para los circuitos fundamentales
        public final ArrayList<AristaId> cuerdas;

        public Resultado(MatrizResultado matrizCircuitos,
                MatrizResultado matrizCircuitosFundamentales,
                ArrayList<AristaId> cuerdas) {
            this.matrizCircuitos = matrizCircuitos;
            this.matrizCircuitosFundamentales = matrizCircuitosFundamentales;
            this.cuerdas = cuerdas;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== MATRIZ DE CIRCUITOS ===\n");
            sb.append(matrizCircuitos.formatear()).append("\n");
            sb.append("=== CUERDAS ===\n");
            ArrayList<String> nombresCuerdas = new ArrayList<>();
            for (AristaId c : cuerdas) {
                nombresCuerdas.add(c.nombre);
            }
            sb.append("{").append(String.join(", ", nombresCuerdas)).append("}\n\n");
            sb.append("Nulidad = ").append(matrizCircuitosFundamentales.nulidad).append("\n\n");
            sb.append("=== MATRIZ DE CIRCUITOS FUNDAMENTALES ===\n");
            sb.append(matrizCircuitosFundamentales.formatear());
            return sb.toString();
        }
    }

    // =====================================================================
    //  ALGORITMO PRINCIPAL
    // =====================================================================
    /**
     * Calcula las dos matrices: circuitos simples y circuitos fundamentales.
     *
     * @param grafo Grafo ponderado NO dirigido (dirigido=false).
     * @return Resultado con ambas matrices.
     */
    public static Resultado calcular(GrafoPonderado grafo) {
        if (grafo == null || grafo.getNumVertices() == 0) {
            throw new IllegalArgumentException("El grafo no puede estar vacío.");
        }
        if (grafo.isDirigido()) {
            throw new IllegalArgumentException(
                    "MatrizCircuitos requiere un grafo NO dirigido (dirigido=false).");
        }

        ArrayList<String> vertices = grafo.getVertices();

        // Obtener aristas unicas (sin duplicar a-b y b-a)
        ArrayList<AristaId> aristasUnicas = getAristasUnicas(grafo);

        // ── Paso 1: Todos los circuitos simples ──────────────────────────────
        ArrayList<ArrayList<AristaId>> todosCircuitos = encontrarCircuitos(grafo, vertices, aristasUnicas);

        ArrayList<String> nombresC = new ArrayList<>();
        for (int i = 0; i < todosCircuitos.size(); i++) {
            nombresC.add("C" + (i + 1));
        }

        MatrizResultado matrizC = new MatrizResultado(nombresC, aristasUnicas, todosCircuitos, 0);

        // ── Paso 2: Arbol de expansion minima y cuerdas ──────────────────────
        ArbolGenerador.Resultado arbol = ArbolGenerador.calcular(grafo, ArbolGenerador.Tipo.MINIMA);

        // Convertir cuerdas a AristaId
        ArrayList<AristaId> cuerdas = new ArrayList<>();
        for (GrafoPonderado.AristaPonderada a : arbol.cuerdas) {
            AristaId aid = new AristaId(a.origen, a.destino, a.peso);
            if (!cuerdas.contains(aid)) {
                cuerdas.add(aid);
            }
        }

        // Aristas del arbol T como AristaId
        ArrayList<AristaId> aristasArbol = new ArrayList<>();
        for (GrafoPonderado.AristaPonderada a : arbol.ramas) {
            AristaId aid = new AristaId(a.origen, a.destino, a.peso);
            if (!aristasArbol.contains(aid)) {
                aristasArbol.add(aid);
            }
        }

        // ── Paso 3: Circuitos fundamentales (uno por cuerda) ─────────────────
        ArrayList<ArrayList<AristaId>> circuitosFund = new ArrayList<>();

        for (AristaId cuerda : cuerdas) {
            ArrayList<AristaId> cf = encontrarCircuitoFundamental(
                    cuerda, aristasArbol, vertices, grafo);
            if (cf != null) {
                circuitosFund.add(cf);
            }
        }

        int nulidad = cuerdas.size();
        ArrayList<String> nombresCf = new ArrayList<>();
        for (int i = 0; i < circuitosFund.size(); i++) {
            nombresCf.add("Cf" + (i + 1));
        }

        MatrizResultado matrizCf = new MatrizResultado(nombresCf, aristasUnicas,
                circuitosFund, nulidad);

        return new Resultado(matrizC, matrizCf, cuerdas);
    }

    // =====================================================================
    //  ENCONTRAR TODOS LOS CIRCUITOS SIMPLES (DFS)
    // =====================================================================
    /**
     * Encuentra todos los circuitos simples del grafo usando DFS. Un circuito
     * simple es un camino que regresa al inicio sin repetir vertices.
     */
    private static ArrayList<ArrayList<AristaId>> encontrarCircuitos(
            GrafoPonderado grafo, ArrayList<String> vertices,
            ArrayList<AristaId> aristasUnicas) {

        ArrayList<ArrayList<AristaId>> resultado = new ArrayList<>();

        for (int inicio = 0; inicio < vertices.size(); inicio++) {
            ArrayList<String> caminoV = new ArrayList<>();
            ArrayList<AristaId> caminoA = new ArrayList<>();
            caminoV.add(vertices.get(inicio));

            dfsCircuitos(vertices.get(inicio), vertices.get(inicio),
                    inicio, caminoV, caminoA, grafo, vertices, resultado);
        }

        return resultado;
    }

    private static void dfsCircuitos(String inicio, String actual, int indiceInicio,
            ArrayList<String> caminoV,
            ArrayList<AristaId> caminoA,
            GrafoPonderado grafo,
            ArrayList<String> vertices,
            ArrayList<ArrayList<AristaId>> resultado) {
        for (GrafoPonderado.AristaPonderada arista : grafo.getAristas()) {
            if (!arista.origen.equals(actual)) {
                continue;
            }

            String vecino = arista.destino;
            AristaId aid = new AristaId(arista.origen, arista.destino, arista.peso);

            // Si el vecino es el inicio y el camino tiene al menos 3 vertices -> circuito
            if (vecino.equals(inicio) && caminoV.size() >= 3) {
                ArrayList<AristaId> circuito = new ArrayList<>(caminoA);
                circuito.add(aid);
                // Evitar duplicados (mismo circuito en diferente orden)
                if (!esDuplicado(circuito, resultado)) {
                    resultado.add(circuito);
                }
                continue;
            }

            // Solo continuar hacia vertices con indice mayor al inicio (evita duplicados)
            int indiceVecino = vertices.indexOf(vecino);
            if (indiceVecino <= indiceInicio) {
                continue;
            }

            // No repetir vertices ya en el camino
            if (caminoV.contains(vecino)) {
                continue;
            }

            // No repetir aristas ya en el camino
            if (caminoA.contains(aid)) {
                continue;
            }

            caminoV.add(vecino);
            caminoA.add(aid);
            dfsCircuitos(inicio, vecino, indiceInicio, caminoV, caminoA,
                    grafo, vertices, resultado);
            caminoV.remove(caminoV.size() - 1);
            caminoA.remove(caminoA.size() - 1);
        }
    }

    /**
     * Verifica si un circuito ya existe en la lista (mismo conjunto de
     * aristas).
     */
    private static boolean esDuplicado(ArrayList<AristaId> nuevo,
            ArrayList<ArrayList<AristaId>> existentes) {
        for (ArrayList<AristaId> ex : existentes) {
            if (ex.size() == nuevo.size() && ex.containsAll(nuevo)) {
                return true;
            }
        }
        return false;
    }

    // =====================================================================
    //  CIRCUITO FUNDAMENTAL POR CUERDA
    // =====================================================================
    /**
     * Encuentra el circuito fundamental para una cuerda dada. El circuito
     * contiene exactamente esa cuerda + un camino en T entre los dos extremos
     * de la cuerda.
     *
     * @param cuerda La arista cuerda (fuera del arbol T).
     * @param aristasT Aristas del arbol de expansion minima T.
     * @param vertices Lista de vertices del grafo.
     * @param grafo Grafo completo.
     * @return Lista de aristas del circuito fundamental, o null si no existe.
     */
    private static ArrayList<AristaId> encontrarCircuitoFundamental(
            AristaId cuerda, ArrayList<AristaId> aristasT,
            ArrayList<String> vertices, GrafoPonderado grafo) {

        // Los extremos de la cuerda son v1 y v2 (extraidos del nombre "v1-v2")
        String[] partes = cuerda.nombre.split("-");
        String v1 = partes[0];
        String v2 = partes[1];

        // Buscar camino en T de v1 a v2 usando solo aristas del arbol
        ArrayList<AristaId> caminoEnT = bfsCaminoEnArbol(v1, v2, aristasT, vertices);

        if (caminoEnT == null) {
            return null;
        }

        // El circuito fundamental = cuerda + camino en T
        ArrayList<AristaId> circuito = new ArrayList<>();
        circuito.add(cuerda);
        circuito.addAll(caminoEnT);
        return circuito;
    }

    /**
     * BFS para encontrar el camino entre v1 y v2 usando solo aristas del arbol
     * T.
     */
    private static ArrayList<AristaId> bfsCaminoEnArbol(
            String v1, String v2, ArrayList<AristaId> aristasT,
            ArrayList<String> vertices) {

        LinkedHashMap<String, String> padre = new LinkedHashMap<>();
        LinkedHashMap<String, AristaId> aristaUsada = new LinkedHashMap<>();
        ArrayList<String> visitados = new ArrayList<>();
        ArrayList<String> cola = new ArrayList<>();

        cola.add(v1);
        visitados.add(v1);
        padre.put(v1, null);

        while (!cola.isEmpty()) {
            String actual = cola.remove(0);

            if (actual.equals(v2)) {
                // Reconstruir camino de aristas
                ArrayList<AristaId> camino = new ArrayList<>();
                String nodo = v2;
                while (padre.get(nodo) != null) {
                    camino.add(0, aristaUsada.get(nodo));
                    nodo = padre.get(nodo);
                }
                return camino;
            }

            // Explorar vecinos usando solo aristas del arbol T
            for (AristaId a : aristasT) {
                String[] p = a.nombre.split("-");
                String va  = p[0];
                String vb = p[1];
                String vecino = null;

                if (va.equals(actual) && !visitados.contains(vb)) {
                    vecino = vb; 
                }else if (vb.equals(actual) && !visitados.contains(va)) {
                    vecino = va;
                }

                if (vecino != null) {
                    visitados.add(vecino);
                    padre.put(vecino, actual);
                    aristaUsada.put(vecino, a);
                    cola.add(vecino);
                }
            }
        }

        return null; // no hay camino en T
    }

    // =====================================================================
    //  UTILIDADES
    // =====================================================================
    /**
     * Obtiene las aristas unicas del grafo (sin duplicar a-b y b-a).
     */
    private static ArrayList<AristaId> getAristasUnicas(GrafoPonderado grafo) {
        ArrayList<AristaId> unicas = new ArrayList<>();
        for (GrafoPonderado.AristaPonderada a : grafo.getAristas()) {
            AristaId aid = new AristaId(a.origen, a.destino, a.peso);
            if (!unicas.contains(aid)) {
                unicas.add(aid);
            }
        }
        return unicas;
    }
}
