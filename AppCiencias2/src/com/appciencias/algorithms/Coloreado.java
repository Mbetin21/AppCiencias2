package com.appciencias.algorithms;

import com.appciencias.models.Grafo;

import java.util.*;

/**
 * ColoracionGrafo.java
 * Coloreado de vértices (χ(G)) y aristas (χ'(G)) + Polinomio Cromático.
 * Opera sobre Grafo (no dirigido, sin pesos).
 */
public class ColoracionGrafo {

    // =========================================================
    //  LÍMITE INTERNO DE FORMAS
    // =========================================================
    private static final int MAX_FORMAS = 100;

    // =========================================================
    //  TIPOS DE GRAFO DETECTADOS
    // =========================================================
    public enum TipoGrafo {
        ARBOL, CICLO_PAR, CICLO_IMPAR, COMPLETO, BIPARTITO, GENERAL
    }

    // =========================================================
    //  RESULTADO — POLINOMIO CROMÁTICO
    // =========================================================
    public static class ResultadoPolinomio {
        /** Tipo detectado */
        public TipoGrafo tipo;
        /** Expresión simbólica del polinomio, ej: "λ(λ-1)^4" */
        public String expresion;
        /** Pasos/explicación de cómo se obtuvo */
        public List<String> pasos = new ArrayList<>();
        /** Evaluaciones P(G, λ) para λ = 1 .. n+2 */
        public Map<Integer, Long> evaluaciones = new LinkedHashMap<>();
        /** Número cromático (menor λ > 0 con evaluación > 0) */
        public int numeroCromatico;
    }

    // =========================================================
    //  RESULTADO — COLORACIÓN DE VÉRTICES
    // =========================================================
    public static class ResultadoColoracion {
        /** vertice → índice de color (0-based) */
        public Map<String, Integer> asignacion = new LinkedHashMap<>();
        /** Número cromático χ(G) */
        public int numeroCromatico;
        /** color → lista de vértices (clases cromáticas) */
        public Map<Integer, List<String>> clasesCromaticas = new LinkedHashMap<>();
        /** Todas las formas válidas (máx MAX_FORMAS) */
        public List<Map<String, Integer>> todasLasFormas = new ArrayList<>();
    }

    // =========================================================
    //  RESULTADO — COLORACIÓN DE ARISTAS
    // =========================================================
    public static class ResultadoColoracionAristas {
        /** "v1-v2" → índice de color (0-based) */
        public Map<String, Integer> asignacionAristas = new LinkedHashMap<>();
        /** Índice cromático χ'(G) */
        public int indiceCromatico;
        /** color → lista de aristas */
        public Map<Integer, List<String>> clasesCromaticasAristas = new LinkedHashMap<>();
        /** Todas las formas válidas (máx MAX_FORMAS) */
        public List<Map<String, Integer>> todasLasFormasAristas = new ArrayList<>();
    }

    // =========================================================
    //  API PÚBLICA
    // =========================================================

    /** Calcula el polinomio cromático del grafo. */
    public static ResultadoPolinomio calcularPolinomio(Grafo grafo) {
        List<String> vertices = new ArrayList<>(grafo.getVertices());
        Map<String, Set<String>> ady = construirAdyacencia(grafo, vertices);
        int n = vertices.size();
        int m = grafo.getNumAristas();

        ResultadoPolinomio res = new ResultadoPolinomio();

        if (n == 0) {
            res.tipo = TipoGrafo.GENERAL;
            res.expresion = "1";
            res.pasos.add("Grafo vacío.");
            res.numeroCromatico = 0;
            return res;
        }

        // --- Detectar tipo ---
        if (esArbol(n, m, ady, vertices)) {
            res.tipo = TipoGrafo.ARBOL;
            res.expresion = "λ(λ-1)^" + (n - 1);
            res.pasos.add("Grafo detectado: ÁRBOL (conexo, |A| = |V|-1 = " + m + ").");
            res.pasos.add("Fórmula directa: P(G,λ) = λ·(λ-1)^(n-1) = λ·(λ-1)^" + (n - 1));
            for (int lam = 1; lam <= n + 2; lam++) {
                long val = (long) lam * potenciaLong(lam - 1, n - 1);
                res.evaluaciones.put(lam, val);
            }
        } else if (esCiclo(n, m, ady)) {
            boolean par = (n % 2 == 0);
            res.tipo = par ? TipoGrafo.CICLO_PAR : TipoGrafo.CICLO_IMPAR;
            res.expresion = "(λ-1)^" + n + (par ? " + (λ-1)" : " - (λ-1)");
            res.pasos.add("Grafo detectado: CICLO " + (par ? "PAR" : "IMPAR") + " (C" + n + ").");
            res.pasos.add("Fórmula directa: P(G,λ) = (λ-1)^n + (-1)^n·(λ-1)");
            res.pasos.add("Con n=" + n + ": P(G,λ) = (λ-1)^" + n + " + " + (par ? "+" : "-") + "(λ-1)");
            for (int lam = 1; lam <= n + 2; lam++) {
                long signo = (par) ? 1L : -1L;
                long val = potenciaLong(lam - 1, n) + signo * (lam - 1);
                res.evaluaciones.put(lam, val);
            }
        } else if (esCompleto(n, m)) {
            res.tipo = TipoGrafo.COMPLETO;
            StringBuilder sb = new StringBuilder("λ");
            for (int i = 1; i < n; i++) sb.append("(λ-").append(i).append(")");
            res.expresion = sb.toString();
            res.pasos.add("Grafo detectado: COMPLETO (K" + n + ", |A| = n(n-1)/2 = " + m + ").");
            res.pasos.add("Fórmula directa: P(G,λ) = λ(λ-1)(λ-2)···(λ-" + (n - 1) + ")");
            for (int lam = 1; lam <= n + 2; lam++) {
                long val = 1;
                for (int i = 0; i < n; i++) val *= (lam - i);
                res.evaluaciones.put(lam, val);
            }
        } else {
            // General: contracción-eliminación
            res.tipo = TipoGrafo.GENERAL;
            res.pasos.add("Grafo general: se aplica contracción-eliminación recursiva.");
            res.pasos.add("P(G,λ) = P(G-e, λ) - P(G/e, λ)  para cada arista e.");
            // Representamos el grafo como mapa de adyacencia para la recursión
            Map<String, Set<String>> adyCopia = copiarAdy(ady);
            // Calculamos para λ = 1..n+2
            // La expresión simbólica se construye evaluando en varios puntos
            // y mostrando los coeficientes (polinomio de grado n)
            long[] coefs = calcularCoeficientesGeneral(adyCopia, vertices);
            res.expresion = coeficientesAExpresion(coefs);
            res.pasos.add("Polinomio resultante: " + res.expresion);
            for (int lam = 1; lam <= n + 2; lam++) {
                long val = evaluarPolinomio(coefs, lam);
                res.evaluaciones.put(lam, val);
            }
        }

        // Número cromático: menor λ >= 1 con evaluación > 0
        res.numeroCromatico = calcularNumeroCromatico(res.evaluaciones);
        res.pasos.add("Número cromático χ(G) = " + res.numeroCromatico +
                " (menor λ con P(G,λ) > 0).");
        return res;
    }

    /** Coloración greedy de vértices + todas las formas válidas. */
    public static ResultadoColoracion colorarVertices(Grafo grafo) {
        List<String> vertices = new ArrayList<>(grafo.getVertices());
        Map<String, Set<String>> ady = construirAdyacencia(grafo, vertices);
        int n = vertices.size();

        ResultadoColoracion res = new ResultadoColoracion();
        if (n == 0) return res;

        // Greedy para obtener número cromático mínimo
        int[] asignGreedy = greedy(vertices, ady);
        int numColores = Arrays.stream(asignGreedy).max().getAsInt() + 1;
        res.numeroCromatico = numColores;

        // Convertir a mapa
        res.asignacion = arrayAMapa(vertices, asignGreedy);
        res.clasesCromaticas = calcularClases(res.asignacion);

        // Todas las formas válidas con exactamente numColores colores
        List<int[]> formas = new ArrayList<>();
        int[] actual = new int[n];
        Arrays.fill(actual, -1);
        generarFormasVertices(vertices, ady, actual, 0, numColores, formas);

        for (int[] f : formas) {
            res.todasLasFormas.add(arrayAMapa(vertices, f));
        }

        return res;
    }

    /** Coloración greedy de aristas + todas las formas válidas. */
    public static ResultadoColoracionAristas colorarAristas(Grafo grafo) {
        List<String> vertices = new ArrayList<>(grafo.getVertices());
        Map<String, Set<String>> ady = construirAdyacencia(grafo, vertices);
        List<String[]> aristas = obtenerAristas(ady, vertices);

        ResultadoColoracionAristas res = new ResultadoColoracionAristas();
        if (aristas.isEmpty()) return res;

        // Greedy sobre aristas
        int[] asignGreedy = greedyAristas(aristas, vertices, ady);
        int numColores = Arrays.stream(asignGreedy).max().getAsInt() + 1;
        res.indiceCromatico = numColores;

        res.asignacionAristas = arrayAMapaAristas(aristas, asignGreedy);
        res.clasesCromaticasAristas = calcularClasesAristas(res.asignacionAristas);

        // Todas las formas válidas
        List<int[]> formas = new ArrayList<>();
        int[] actual = new int[aristas.size()];
        Arrays.fill(actual, -1);
        generarFormasAristas(aristas, vertices, ady, actual, 0, numColores, formas);

        for (int[] f : formas) {
            res.todasLasFormasAristas.add(arrayAMapaAristas(aristas, f));
        }

        return res;
    }

    // =========================================================
    //  DETECCIÓN DE TIPO DE GRAFO
    // =========================================================

    private static boolean esArbol(int n, int m,
                                    Map<String, Set<String>> ady,
                                    List<String> vertices) {
        if (m != n - 1) return false;
        return esConexo(ady, vertices);
    }

    private static boolean esCiclo(int n, int m, Map<String, Set<String>> ady) {
        if (m != n) return false;
        // Todo vértice debe tener grado exactamente 2
        for (Set<String> vec : ady.values()) {
            if (vec.size() != 2) return false;
        }
        return true;
    }

    private static boolean esCompleto(int n, int m) {
        return m == n * (n - 1) / 2;
    }

    private static boolean esConexo(Map<String, Set<String>> ady, List<String> vertices) {
        if (vertices.isEmpty()) return true;
        Set<String> visitados = new HashSet<>();
        Queue<String> cola = new LinkedList<>();
        cola.add(vertices.get(0));
        visitados.add(vertices.get(0));
        while (!cola.isEmpty()) {
            String v = cola.poll();
            for (String vec : ady.getOrDefault(v, Collections.emptySet())) {
                if (!visitados.contains(vec)) {
                    visitados.add(vec);
                    cola.add(vec);
                }
            }
        }
        return visitados.size() == vertices.size();
    }

    // =========================================================
    //  POLINOMIO GENERAL — CONTRACCIÓN/ELIMINACIÓN
    // =========================================================

    /**
     * Calcula los coeficientes del polinomio cromático por
     * contracción-eliminación recursiva.
     * Representamos el grafo como mapa String→Set<String>.
     * Devuelve array de coeficientes: coefs[i] es el coef. de λ^i.
     */
    private static long[] calcularCoeficientesGeneral(
            Map<String, Set<String>> ady, List<String> vertices) {
        int n = vertices.size();
        // Evaluamos el polinomio en n+1 puntos distintos (0..n)
        // y luego interpolamos con diferencias finitas para obtener coeficientes
        long[] valores = new long[n + 1];
        for (int lam = 0; lam <= n; lam++) {
            valores[lam] = evaluarContraccionEliminacion(copiarAdy(ady), lam);
        }
        // Interpolación de Newton
        return interpolacionNewton(valores);
    }

    /**
     * Evalúa P(G, lambda) directamente por contracción-eliminación.
     * Si lambda = 0 → retorna 0.
     */
    private static long evaluarContraccionEliminacion(
            Map<String, Set<String>> ady, int lambda) {
        // Caso base: sin aristas → λ^n
        List<String[]> aristas = obtenerAristasDeAdy(ady);
        if (aristas.isEmpty()) {
            return potenciaLong(lambda, ady.size());
        }
        // Tomar primera arista
        String u = aristas.get(0)[0];
        String v = aristas.get(0)[1];

        // G - e (eliminar arista)
        Map<String, Set<String>> gMenosE = copiarAdy(ady);
        gMenosE.get(u).remove(v);
        gMenosE.get(v).remove(u);

        // G / e (contraer arista: fusionar v en u)
        Map<String, Set<String>> gContraido = copiarAdy(ady);
        contraerArista(gContraido, u, v);

        return evaluarContraccionEliminacion(gMenosE, lambda)
                - evaluarContraccionEliminacion(gContraido, lambda);
    }

    /** Fusiona vértice v en u y elimina v del grafo. */
    private static void contraerArista(Map<String, Set<String>> ady,
                                        String u, String v) {
        // Los vecinos de v pasan a ser vecinos de u (excepto u mismo)
        Set<String> vecinosV = ady.getOrDefault(v, new HashSet<>());
        for (String w : vecinosV) {
            if (!w.equals(u)) {
                ady.get(u).add(w);
                ady.get(w).remove(v);
                ady.get(w).add(u);
            }
        }
        ady.get(u).remove(v);
        ady.remove(v);
    }

    /** Interpolación de Newton para obtener coeficientes a partir de valores en 0,1,...,n */
    private static long[] interpolacionNewton(long[] y) {
        int n = y.length;
        long[] delta = Arrays.copyOf(y, n);
        long[] coefs = new long[n]; // coefs[i] = coef de C(x,i) * i!
        // Tabla de diferencias finitas hacia adelante
        long[][] tabla = new long[n][n];
        tabla[0] = Arrays.copyOf(y, n);
        for (int j = 1; j < n; j++) {
            for (int i = 0; i < n - j; i++) {
                tabla[j][i] = tabla[j - 1][i + 1] - tabla[j - 1][i];
            }
        }
        // Los coeficientes del polinomio en base estándar
        // P(x) = sum_{k=0}^{n-1} C(x,k) * delta^k[0]
        // donde C(x,k) = x(x-1)...(x-k+1)/k!
        // Expandimos a coeficientes estándar
        long[] resultado = new long[n];
        for (int k = 0; k < n; k++) {
            long dk = tabla[k][0];
            if (dk == 0) continue;
            // C(x,k) expandido: coeficientes del polinomio x*(x-1)*...*(x-k+1)/k!
            long[] stirling = stirlingAscendente(k);
            long fact = factorial(k);
            for (int i = 0; i < stirling.length; i++) {
                resultado[i] += dk * stirling[i] / fact;
            }
        }
        return resultado;
    }

    /** Polinomio x(x-1)(x-2)···(x-k+1) como array de coeficientes */
    private static long[] stirlingAscendente(int k) {
        long[] poly = new long[k + 1];
        poly[0] = 1;
        for (int i = 0; i < k; i++) {
            // Multiplica por (x - i)
            long[] nuevo = new long[k + 1];
            for (int j = 0; j <= i; j++) {
                nuevo[j + 1] += poly[j];   // *x
                nuevo[j] -= (long) i * poly[j]; // *(-i)
            }
            poly = nuevo;
        }
        return poly;
    }

    private static long factorial(int n) {
        long r = 1;
        for (int i = 2; i <= n; i++) r *= i;
        return r;
    }

    private static long evaluarPolinomio(long[] coefs, long x) {
        long val = 0;
        long xp = 1;
        for (long c : coefs) {
            val += c * xp;
            xp *= x;
        }
        return val;
    }

    private static String coeficientesAExpresion(long[] coefs) {
        // coefs[i] es el coef de λ^i
        StringBuilder sb = new StringBuilder();
        boolean primero = true;
        for (int i = coefs.length - 1; i >= 0; i--) {
            long c = coefs[i];
            if (c == 0) continue;
            if (!primero) {
                sb.append(c > 0 ? " + " : " - ");
                c = Math.abs(c);
            } else if (c < 0) {
                sb.append("-");
                c = Math.abs(c);
            }
            if (i == 0) {
                sb.append(c);
            } else if (i == 1) {
                sb.append(c == 1 ? "" : c).append("λ");
            } else {
                sb.append(c == 1 ? "" : c).append("λ^").append(i);
            }
            primero = false;
        }
        return sb.length() == 0 ? "0" : sb.toString();
    }

    // =========================================================
    //  GREEDY — VÉRTICES
    // =========================================================

    private static int[] greedy(List<String> vertices,
                                 Map<String, Set<String>> ady) {
        int n = vertices.size();
        int[] resultado = new int[n];
        Arrays.fill(resultado, -1);
        resultado[0] = 0;

        boolean[] disponible = new boolean[n];

        for (int u = 1; u < n; u++) {
            Arrays.fill(disponible, true);
            String vU = vertices.get(u);
            for (String vecino : ady.getOrDefault(vU, Collections.emptySet())) {
                int idx = vertices.indexOf(vecino);
                if (idx >= 0 && resultado[idx] != -1) {
                    disponible[resultado[idx]] = false;
                }
            }
            for (int c = 0; c < n; c++) {
                if (disponible[c]) {
                    resultado[u] = c;
                    break;
                }
            }
        }
        return resultado;
    }

    /** Genera TODAS las coloraciones válidas con exactamente k colores. */
    private static void generarFormasVertices(List<String> vertices,
                                               Map<String, Set<String>> ady,
                                               int[] actual, int pos, int k,
                                               List<int[]> formas) {
        if (formas.size() >= MAX_FORMAS) return;
        if (pos == vertices.size()) {
            // Verificar que usa exactamente k colores
            Set<Integer> usados = new HashSet<>();
            for (int c : actual) usados.add(c);
            if (usados.size() == k) {
                formas.add(Arrays.copyOf(actual, actual.length));
            }
            return;
        }
        String v = vertices.get(pos);
        Set<Integer> prohibidos = new HashSet<>();
        for (String vec : ady.getOrDefault(v, Collections.emptySet())) {
            int idx = vertices.indexOf(vec);
            if (idx >= 0 && idx < pos && actual[idx] != -1) {
                prohibidos.add(actual[idx]);
            }
        }
        for (int c = 0; c < k; c++) {
            if (!prohibidos.contains(c)) {
                actual[pos] = c;
                generarFormasVertices(vertices, ady, actual, pos + 1, k, formas);
                if (formas.size() >= MAX_FORMAS) return;
            }
        }
        actual[pos] = -1;
    }

    // =========================================================
    //  GREEDY — ARISTAS
    // =========================================================

    private static int[] greedyAristas(List<String[]> aristas,
                                        List<String> vertices,
                                        Map<String, Set<String>> ady) {
        int m = aristas.size();
        int[] resultado = new int[m];
        Arrays.fill(resultado, -1);

        // Para cada arista, colores usados en aristas adyacentes ya coloreadas
        for (int i = 0; i < m; i++) {
            String u = aristas.get(i)[0];
            String v = aristas.get(i)[1];
            Set<Integer> prohibidos = new HashSet<>();
            // Aristas adyacentes = aristas que comparten vértice con (u,v)
            for (int j = 0; j < i; j++) {
                if (resultado[j] == -1) continue;
                String a = aristas.get(j)[0], b = aristas.get(j)[1];
                if (a.equals(u) || b.equals(u) || a.equals(v) || b.equals(v)) {
                    prohibidos.add(resultado[j]);
                }
            }
            for (int c = 0; c < m; c++) {
                if (!prohibidos.contains(c)) {
                    resultado[i] = c;
                    break;
                }
            }
        }
        return resultado;
    }

    /** Genera TODAS las coloraciones de aristas válidas con exactamente k colores. */
    private static void generarFormasAristas(List<String[]> aristas,
                                              List<String> vertices,
                                              Map<String, Set<String>> ady,
                                              int[] actual, int pos, int k,
                                              List<int[]> formas) {
        if (formas.size() >= MAX_FORMAS) return;
        if (pos == aristas.size()) {
            Set<Integer> usados = new HashSet<>();
            for (int c : actual) usados.add(c);
            if (usados.size() == k) {
                formas.add(Arrays.copyOf(actual, actual.length));
            }
            return;
        }
        String u = aristas.get(pos)[0];
        String v = aristas.get(pos)[1];
        Set<Integer> prohibidos = new HashSet<>();
        for (int j = 0; j < pos; j++) {
            if (actual[j] == -1) continue;
            String a = aristas.get(j)[0], b = aristas.get(j)[1];
            if (a.equals(u) || b.equals(u) || a.equals(v) || b.equals(v)) {
                prohibidos.add(actual[j]);
            }
        }
        for (int c = 0; c < k; c++) {
            if (!prohibidos.contains(c)) {
                actual[pos] = c;
                generarFormasAristas(aristas, vertices, ady, actual, pos + 1, k, formas);
                if (formas.size() >= MAX_FORMAS) return;
            }
        }
        actual[pos] = -1;
    }

    // =========================================================
    //  UTILIDADES INTERNAS
    // =========================================================

    /** Construye mapa de adyacencia desde el Grafo. */
    private static Map<String, Set<String>> construirAdyacencia(
            Grafo grafo, List<String> vertices) {
        Map<String, Set<String>> ady = new LinkedHashMap<>();
        for (String v : vertices) ady.put(v, new LinkedHashSet<>());
        for (String v : vertices) {
            List<String> vecinos = grafo.getVecinos(v);
            if (vecinos != null) {
                for (String w : vecinos) {
                    ady.get(v).add(w);
                }
            }
        }
        return ady;
    }

    /** Obtiene lista de aristas sin duplicados (u < v lexicográfico). */
    private static List<String[]> obtenerAristas(Map<String, Set<String>> ady,
                                                   List<String> vertices) {
        Set<String> vistas = new LinkedHashSet<>();
        List<String[]> aristas = new ArrayList<>();
        for (String u : vertices) {
            for (String v : ady.getOrDefault(u, Collections.emptySet())) {
                String key = u.compareTo(v) < 0 ? u + "-" + v : v + "-" + u;
                if (vistas.add(key)) aristas.add(new String[]{u, v});
            }
        }
        return aristas;
    }

    private static List<String[]> obtenerAristasDeAdy(Map<String, Set<String>> ady) {
        Set<String> vistas = new HashSet<>();
        List<String[]> aristas = new ArrayList<>();
        for (String u : ady.keySet()) {
            for (String v : ady.getOrDefault(u, Collections.emptySet())) {
                String key = u.compareTo(v) < 0 ? u + "|" + v : v + "|" + u;
                if (vistas.add(key)) aristas.add(new String[]{u, v});
            }
        }
        return aristas;
    }

    private static Map<String, Set<String>> copiarAdy(Map<String, Set<String>> ady) {
        Map<String, Set<String>> copia = new LinkedHashMap<>();
        for (Map.Entry<String, Set<String>> e : ady.entrySet()) {
            copia.put(e.getKey(), new LinkedHashSet<>(e.getValue()));
        }
        return copia;
    }

    private static int calcularNumeroCromatico(Map<Integer, Long> evaluaciones) {
        for (Map.Entry<Integer, Long> e : evaluaciones.entrySet()) {
            if (e.getValue() > 0) return e.getKey();
        }
        return evaluaciones.keySet().stream().mapToInt(i -> i).min().orElse(1);
    }

    private static long potenciaLong(long base, int exp) {
        long r = 1;
        for (int i = 0; i < exp; i++) r *= base;
        return r;
    }

    private static Map<String, Integer> arrayAMapa(List<String> vertices, int[] arr) {
        Map<String, Integer> m = new LinkedHashMap<>();
        for (int i = 0; i < vertices.size(); i++) m.put(vertices.get(i), arr[i]);
        return m;
    }

    private static Map<String, Integer> arrayAMapaAristas(List<String[]> aristas, int[] arr) {
        Map<String, Integer> m = new LinkedHashMap<>();
        for (int i = 0; i < aristas.size(); i++) {
            String[] ar = aristas.get(i);
            String key = ar[0].compareTo(ar[1]) < 0
                    ? ar[0] + "-" + ar[1] : ar[1] + "-" + ar[0];
            m.put(key, arr[i]);
        }
        return m;
    }

    private static Map<Integer, List<String>> calcularClases(Map<String, Integer> asig) {
        Map<Integer, List<String>> clases = new TreeMap<>();
        for (Map.Entry<String, Integer> e : asig.entrySet()) {
            clases.computeIfAbsent(e.getValue(), k -> new ArrayList<>()).add(e.getKey());
        }
        return clases;
    }

    private static Map<Integer, List<String>> calcularClasesAristas(
            Map<String, Integer> asig) {
        Map<Integer, List<String>> clases = new TreeMap<>();
        for (Map.Entry<String, Integer> e : asig.entrySet()) {
            clases.computeIfAbsent(e.getValue(), k -> new ArrayList<>()).add(e.getKey());
        }
        return clases;
    }
}