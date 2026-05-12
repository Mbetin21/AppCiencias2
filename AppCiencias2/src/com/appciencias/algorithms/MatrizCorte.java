package com.appciencias.algorithms;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Matriz de Conjuntos de Corte y Matriz de Conjuntos de Corte Fundamentales.
 *
 * Recibe un GrafoPonderado NO DIRIGIDO (creado con dirigido=false).
 *
 * CONJUNTOS DE CORTE: Un conjunto de aristas cuya remocion desconecta el grafo.
 * Debe ser MINIMAL: ningun subconjunto propio tambien desconecta. Matriz: filas
 * = conjuntos (Cc1, Cc2...), columnas = aristas del grafo, valor = peso si
 * pertenece al conjunto, 0 si no.
 *
 * CONJUNTOS DE CORTE FUNDAMENTALES: Se obtiene el arbol de expansion minima T y
 * sus ramas. Por cada rama del arbol T se busca el conjunto de corte
 * fundamental: el conjunto minimal que contiene EXACTAMENTE esa rama del arbol
 * y ninguna otra rama (puede contener cuerdas). Numero de conjuntos
 * fundamentales = numero de ramas = |V| - 1.
 */
public class MatrizCorte {

    // Reutilizamos AristaId de MatrizCircuitos para identificar aristas
    // pero la redefinimos aqui para no crear dependencia innecesaria
    /**
     * Arista identificada por nombre para las columnas de la matriz. En grafo
     * no dirigido "a-b" == "b-a", se usa orden lexicografico.
     */
    public static class AristaId {

        public final String nombre;
        public final double peso;

        public AristaId(String v1, String v2, double peso) {
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
     * Resultado de una matriz (corte o corte fundamental).
     */
    public static class MatrizResultado {

        public final ArrayList<String> nombresConjuntos;
        public final ArrayList<AristaId> aristas;
        public final ArrayList<ArrayList<AristaId>> conjuntos;
        public final double[][] matriz;
        public final int cantidad; // numero de conjuntos

        public MatrizResultado(ArrayList<String> nombresConjuntos,
                ArrayList<AristaId> aristas,
                ArrayList<ArrayList<AristaId>> conjuntos) {
            this.nombresConjuntos = new ArrayList<>(nombresConjuntos);
            this.aristas = new ArrayList<>(aristas);
            this.conjuntos = conjuntos;
            this.cantidad = conjuntos.size();

            int filas = conjuntos.size();
            int cols = aristas.size();
            this.matriz = new double[filas][cols];

            for (int i = 0; i < filas; i++) {
                for (int j = 0; j < cols; j++) {
                    AristaId a = aristas.get(j);
                    boolean encontrada = false;
                    for (AristaId ca : conjuntos.get(i)) {
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

        public String formatear(String titulo) {
            int filas = nombresConjuntos.size();
            int cols = aristas.size();
            StringBuilder sb = new StringBuilder();
            sb.append(titulo).append("\n");

            // Encabezado
            sb.append(String.format("%-8s", ""));
            for (AristaId a : aristas) {
                sb.append(String.format("%-8s", a.nombre));
            }
            sb.append("\n");

            // Filas
            for (int i = 0; i < filas; i++) {
                sb.append(String.format("%-8s", nombresConjuntos.get(i)));
                for (int j = 0; j < cols; j++) {
                    String val = (matriz[i][j] == 0) ? "0"
                            : (matriz[i][j] == 1.0) ? "1"
                                    : String.valueOf(matriz[i][j]);
                    sb.append(String.format("%-8s", val));
                }
                sb.append("\n");
            }
            return sb.toString();
        }

        @Override
        public String toString() {
            return formatear("Matriz");
        }
    }

    /**
     * Resultado completo: ambas matrices.
     */
    public static class Resultado {

        public final MatrizResultado matrizCorte;
        public final MatrizResultado matrizCorteFundamental;
        public final ArrayList<AristaId> ramas;   // ramas del arbol T
        public final ArrayList<AristaId> cuerdas; // cuerdas (complemento de T)

        public Resultado(MatrizResultado matrizCorte,
                MatrizResultado matrizCorteFundamental,
                ArrayList<AristaId> ramas,
                ArrayList<AristaId> cuerdas) {
            this.matrizCorte = matrizCorte;
            this.matrizCorteFundamental = matrizCorteFundamental;
            this.ramas = ramas;
            this.cuerdas = cuerdas;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append("=== CONJUNTOS DE CORTE ===\n");
            sb.append(matrizCorte.formatear("Matriz de Conjuntos de Corte")).append("\n");

            sb.append("Ramas (aristas de T): {");
            ArrayList<String> nr = new ArrayList<>();
            for (AristaId r : ramas) {
                nr.add(r.nombre);
            }
            sb.append(String.join(", ", nr)).append("}\n\n");

            sb.append("=== CONJUNTOS DE CORTE FUNDAMENTALES ===\n");
            sb.append("Cantidad = ").append(matrizCorteFundamental.cantidad)
                    .append(" (= |V| - 1)\n");
            sb.append(matrizCorteFundamental.formatear(
                    "Matriz de Conjuntos de Corte Fundamentales"));

            return sb.toString();
        }
    }

    // =====================================================================
    //  ALGORITMO PRINCIPAL
    // =====================================================================
    /**
     * Calcula las dos matrices: conjuntos de corte y conjuntos de corte
     * fundamentales.
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
                    "MatrizCorte requiere un grafo NO dirigido (dirigido=false).");
        }

        ArrayList<String> vertices = grafo.getVertices();
        ArrayList<AristaId> aristasAll = getAristasUnicas(grafo);

        // ── Paso 1: Arbol de expansion minima (ramas y cuerdas) ─────────────
        ArbolGenerador.Resultado arbol = ArbolGenerador.calcular(grafo, ArbolGenerador.Tipo.MINIMA);

        ArrayList<AristaId> ramas = new ArrayList<>();
        ArrayList<AristaId> cuerdas = new ArrayList<>();

        for (GrafoPonderado.AristaPonderada a : arbol.ramas) {
            AristaId aid = new AristaId(a.origen, a.destino, a.peso);
            if (!ramas.contains(aid)) {
                ramas.add(aid);
            }
        }
        for (GrafoPonderado.AristaPonderada a : arbol.cuerdas) {
            AristaId aid = new AristaId(a.origen, a.destino, a.peso);
            if (!cuerdas.contains(aid)) {
                cuerdas.add(aid);
            }
        }

        // ── Paso 2: Todos los conjuntos de corte minimales ──────────────────
        ArrayList<ArrayList<AristaId>> conjuntosCorte
                = encontrarConjuntosCorteMinimales(grafo, vertices, aristasAll);

        ArrayList<String> nombresCC = new ArrayList<>();
        for (int i = 0; i < conjuntosCorte.size(); i++) {
            nombresCC.add("Cc" + (i + 1));
        }

        MatrizResultado matrizCC = new MatrizResultado(nombresCC, aristasAll, conjuntosCorte);

        // ── Paso 3: Conjuntos de corte fundamentales (uno por rama) ─────────
        ArrayList<ArrayList<AristaId>> conjuntosFund = new ArrayList<>();

        for (AristaId rama : ramas) {
            ArrayList<AristaId> ccf = encontrarCorteFundamental(
                    rama, ramas, cuerdas, vertices, grafo);
            if (ccf != null) {
                conjuntosFund.add(ccf);
            }
        }

        ArrayList<String> nombresCCF = new ArrayList<>();
        for (int i = 0; i < conjuntosFund.size(); i++) {
            nombresCCF.add("Ccf" + (i + 1));
        }

        MatrizResultado matrizCCF = new MatrizResultado(nombresCCF, aristasAll, conjuntosFund);

        return new Resultado(matrizCC, matrizCCF, ramas, cuerdas);
    }

    // =====================================================================
    //  CONJUNTOS DE CORTE MINIMALES
    // =====================================================================
    /**
     * Encuentra todos los conjuntos de corte minimales del grafo. Genera
     * subconjuntos de aristas de menor a mayor tamano, verifica si desconectan
     * el grafo, y descarta los no minimales.
     */
    private static ArrayList<ArrayList<AristaId>> encontrarConjuntosCorteMinimales(
            GrafoPonderado grafo, ArrayList<String> vertices,
            ArrayList<AristaId> aristas) {

        ArrayList<ArrayList<AristaId>> resultado = new ArrayList<>();
        int n = aristas.size();

        // Generar subconjuntos de menor a mayor tamano
        for (int tam = 1; tam <= n; tam++) {
            ArrayList<ArrayList<AristaId>> subconjuntos = generarSubconjuntos(aristas, tam);

            for (ArrayList<AristaId> sub : subconjuntos) {
                // Verificar si este subconjunto desconecta el grafo
                if (!esConexo(grafo, vertices, sub)) {
                    // Verificar que sea minimal: ningun subconjunto propio tambien desconecta
                    if (esMinimal(grafo, vertices, sub, resultado)) {
                        resultado.add(sub);
                    }
                }
            }
        }

        return resultado;
    }

    /**
     * Verifica si un conjunto de corte es minimal: ningun subconjunto propio
     * suyo ya esta en la lista de conjuntos de corte.
     */
    private static boolean esMinimal(GrafoPonderado grafo, ArrayList<String> vertices,
            ArrayList<ArrayList<AristaId>> yaEncontrados,
            ArrayList<AristaId> candidato,
            ArrayList<ArrayList<AristaId>> existentes) {
        for (ArrayList<AristaId> ex : existentes) {
            if (candidato.containsAll(ex)) {
                return false; // ex es subconjunto de candidato

                    }}
        return true;
    }

    // Sobrecarga para llamada limpia
    private static boolean esMinimal(GrafoPonderado grafo, ArrayList<String> vertices,
            ArrayList<AristaId> candidato,
            ArrayList<ArrayList<AristaId>> existentes) {
        return esMinimal(grafo, vertices, existentes, candidato, existentes);
    }

    /**
     * Verifica si el grafo sigue siendo conexo al quitar un conjunto de
     * aristas. Usa BFS ignorando las aristas del conjunto.
     */
    private static boolean esConexo(GrafoPonderado grafo, ArrayList<String> vertices,
            ArrayList<AristaId> quitadas) {
        if (vertices.isEmpty()) {
            return true;
        }

        ArrayList<String> visitados = new ArrayList<>();
        ArrayList<String> cola = new ArrayList<>();
        cola.add(vertices.get(0));
        visitados.add(vertices.get(0));

        while (!cola.isEmpty()) {
            String actual = cola.remove(0);
            for (GrafoPonderado.AristaPonderada a : grafo.getAristas()) {
                if (!a.origen.equals(actual)) {
                    continue;
                }
                AristaId aid = new AristaId(a.origen, a.destino, a.peso);
                if (quitadas.contains(aid)) {
                    continue; // arista quitada

                                }if (!visitados.contains(a.destino)) {
                    visitados.add(a.destino);
                    cola.add(a.destino);
                }
            }
        }

        return visitados.size() == vertices.size();
    }

    /**
     * Genera todos los subconjuntos de tamano exacto k de una lista de aristas.
     */
    private static ArrayList<ArrayList<AristaId>> generarSubconjuntos(
            ArrayList<AristaId> aristas, int k) {
        ArrayList<ArrayList<AristaId>> resultado = new ArrayList<>();
        generarSubconjuntosRec(aristas, k, 0, new ArrayList<>(), resultado);
        return resultado;
    }

    private static void generarSubconjuntosRec(ArrayList<AristaId> aristas, int k,
            int inicio, ArrayList<AristaId> actual,
            ArrayList<ArrayList<AristaId>> resultado) {
        if (actual.size() == k) {
            resultado.add(new ArrayList<>(actual));
            return;
        }
        for (int i = inicio; i < aristas.size(); i++) {
            actual.add(aristas.get(i));
            generarSubconjuntosRec(aristas, k, i + 1, actual, resultado);
            actual.remove(actual.size() - 1);
        }
    }

    // =====================================================================
    //  CONJUNTO DE CORTE FUNDAMENTAL POR RAMA
    // =====================================================================
    /**
     * Encuentra el conjunto de corte fundamental para una rama dada.
     *
     * Al quitar una rama del arbol T, el arbol se parte en dos componentes. El
     * conjunto de corte fundamental es el conjunto de todas las aristas del
     * grafo original que van de una componente a la otra: exactamente la rama +
     * las cuerdas que cruzan esa particion.
     *
     * @param rama La arista rama del arbol T.
     * @param ramas Todas las ramas del arbol T.
     * @param cuerdas Todas las cuerdas (complemento de T).
     * @param vertices Lista de vertices.
     * @param grafo Grafo completo.
     * @param aristasAll Todas las aristas unicas del grafo.
     * @return Lista de aristas del conjunto de corte fundamental.
     */
    private static ArrayList<AristaId> encontrarCorteFundamental(
            AristaId rama, ArrayList<AristaId> ramas,
            ArrayList<AristaId> cuerdas, ArrayList<String> vertices,
            GrafoPonderado grafo) {

        // Quitar la rama del arbol -> dos componentes en T
        ArrayList<AristaId> ramasSinEsta = new ArrayList<>(ramas);
        ramasSinEsta.remove(rama);

        // BFS en T sin la rama para encontrar la componente que contiene v1
        String[] partes = rama.nombre.split("-");
        String v1 = partes[0];

        ArrayList<String> componenteV1 = bfsEnArbol(v1, ramasSinEsta, vertices);

        // El conjunto de corte fundamental son todas las aristas del grafo
        // que tienen un extremo en componenteV1 y el otro fuera
        ArrayList<AristaId> ccf = new ArrayList<>();

        for (GrafoPonderado.AristaPonderada a : grafo.getAristas()) {
            AristaId aid = new AristaId(a.origen, a.destino, a.peso);
            boolean origenEnC1 = componenteV1.contains(a.origen);
            boolean destinoEnC1 = componenteV1.contains(a.destino);

            // Arista que cruza la particion (un extremo en C1, otro fuera)
            if (origenEnC1 != destinoEnC1) {
                if (!ccf.contains(aid)) {
                    ccf.add(aid);
                }
            }
        }

        return ccf.isEmpty() ? null : ccf;
    }

    /**
     * BFS en el arbol T (usando solo esas aristas) desde un vertice inicio.
     * Retorna todos los vertices alcanzables.
     */
    private static ArrayList<String> bfsEnArbol(String inicio,
            ArrayList<AristaId> aristasT,
            ArrayList<String> vertices) {
        ArrayList<String> visitados = new ArrayList<>();
        ArrayList<String> cola = new ArrayList<>();
        cola.add(inicio);
        visitados.add(inicio);

        while (!cola.isEmpty()) {
            String actual = cola.remove(0);
            for (AristaId a : aristasT) {
                String[] p = a.nombre.split("-");
                String va  = p[0], vb = p[1];
                String vecino = null;
                if (va.equals(actual) && !visitados.contains(vb)) {
                    vecino = vb; 
                }else if (vb.equals(actual) && !visitados.contains(va)) {
                    vecino = va;
                }
                if (vecino != null) {
                    visitados.add(vecino);
                    cola.add(vecino);
                }
            }
        }
        return visitados;
    }

    // =====================================================================
    //  UTILIDADES
    // =====================================================================
    /**
     * Obtiene aristas unicas del grafo (sin duplicar a-b y b-a).
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
