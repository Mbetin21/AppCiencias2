package com.appciencias.algorithms;

import java.util.ArrayList;

/**
 * Matrices de estructura de un grafo.
 *
 * Contiene: - Matriz de Incidencia (vertices x aristas) - Matriz de Adyacencia
 * de Vertices (vertices x vertices) - Matriz de Adyacencia de Aristas (aristas
 * x aristas) - Matriz de Circuitos dirigidos (circuitos x aristas)
 *
 * Funciona para grafos ponderados y no ponderados, dirigidos y no dirigidos. Si
 * el grafo no es ponderado (peso=1), la matriz usa 0/1. Si es ponderado, usa
 * 0/peso. Si es dirigido, usa +/- segun la direccion.
 *
 * Recibe GrafoPonderado para ambos casos. Para no ponderado: crear con pesos 1.
 * Para no dirigido: crear con dirigido=false.
 */
public class MatricesAdy_Inc {

    /**
     * Resultado generico de una matriz. Contiene la matriz numerica, etiquetas
     * de filas y columnas.
     */
    public static class Matriz {

        public final String titulo;
        public final ArrayList<String> filas;   // etiquetas de filas
        public final ArrayList<String> columnas; // etiquetas de columnas
        public final double[][] valores;

        public Matriz(String titulo, ArrayList<String> filas,
                ArrayList<String> columnas, double[][] valores) {
            this.titulo = titulo;
            this.filas = new ArrayList<>(filas);
            this.columnas = new ArrayList<>(columnas);
            this.valores = valores;
        }

        /**
         * Determina si todos los pesos son 1 (grafo no ponderado). En ese caso
         * muestra 0/1 en vez de 0/peso.
         */
        private boolean esNoPonderada() {
            for (double[] fila : valores) {
                for (double v : fila) {
                    if (v != 0 && v != 1 && v != -1) {
                        return false;
                    }
                }
            }
            return true;
        }

        public String formatear() {
            int nf = filas.size();
            int nc = columnas.size();
            StringBuilder sb = new StringBuilder();
            sb.append("--- ").append(titulo).append(" ---\n");

            // Encabezado
            sb.append(String.format("%-8s", ""));
            for (String col : columnas) {
                sb.append(String.format("%-8s", col));
            }
            sb.append("\n");

            // Filas
            for (int i = 0; i < nf; i++) {
                sb.append(String.format("%-8s", filas.get(i)));
                for (int j = 0; j < nc; j++) {
                    double v = valores[i][j];
                    String val;
                    if (v == 0) {
                        val = "0";
                    } else if (v == 1) {
                        val = "1";
                    } else if (v == -1) {
                        val = "-1";
                    } else {
                        val = String.valueOf(v);
                    }
                    sb.append(String.format("%-8s", val));
                }
                sb.append("\n");
            }
            return sb.toString();
        }

        @Override
        public String toString() {
            return formatear();
        }
    }

    // =========================================================================
    //  ARISTA IDENTIFICADA (para columnas de matrices)
    // =========================================================================
    /**
     * Identifica una arista por nombre para usarla como columna. Dirigido:
     * "origen->destino" No dirigido: "v1-v2" en orden lexicografico
     */
    public static class AristaId {

        public final String nombre;
        public final String origen;
        public final String destino;
        public final double peso;
        public final boolean dirigida;

        public AristaId(String origen, String destino, double peso, boolean dirigida) {
            this.origen = origen;
            this.destino = destino;
            this.peso = peso;
            this.dirigida = dirigida;
            if (dirigida) {
                this.nombre = origen + "->" + destino;
            } else {
                if (origen.compareTo(destino) <= 0) {
                    this.nombre = origen + "-" + destino;
                } else {
                    this.nombre = destino + "-" + origen;
                }
            }
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

    // =========================================================================
    //  UTILIDADES COMUNES
    // =========================================================================
    /**
     * Obtiene aristas unicas del grafo como AristaId. Dirigido: una por cada
     * arista original. No dirigido: sin duplicar a-b y b-a.
     */
    private static ArrayList<AristaId> getAristasUnicas(GrafoPonderado grafo) {
        ArrayList<AristaId> unicas = new ArrayList<>();
        for (GrafoPonderado.AristaPonderada a : grafo.getAristas()) {
            AristaId aid = new AristaId(a.origen, a.destino, a.peso, grafo.isDirigido());
            if (!unicas.contains(aid)) {
                unicas.add(aid);
            }
        }
        return unicas;
    }

    private static boolean esPonderado(GrafoPonderado grafo) {
        for (GrafoPonderado.AristaPonderada a : grafo.getAristas()) {
            if (a.peso != 1.0) {
                return true;
            }
        }
        return false;
    }

    // =========================================================================
    //  1. MATRIZ DE INCIDENCIA (vertices x aristas)
    // =========================================================================
    /**
     * Calcula la Matriz de Incidencia del grafo.
     *
     * No dirigido: valor = peso si el vertice toca la arista, 0 si no. Si no
     * ponderado, valor = 1. Dirigido: +peso si la arista SALE del vertice,
     * -peso si la arista LLEGA al vertice, 0 si no toca. Si no ponderado,
     * +1/-1.
     *
     * @param grafo Grafo ponderado dirigido o no dirigido.
     * @return Matriz de incidencia.
     */
    public static Matriz incidencia(GrafoPonderado grafo) {
        ArrayList<String> vertices = grafo.getVertices();
        ArrayList<AristaId> aristas = getAristasUnicas(grafo);
        boolean dirigido = grafo.isDirigido();
        boolean ponderado = esPonderado(grafo);

        int nv = vertices.size();
        int na = aristas.size();
        double[][] mat = new double[nv][na];

        for (int i = 0; i < nv; i++) {
            String v = vertices.get(i);
            for (int j = 0; j < na; j++) {
                AristaId a = aristas.get(j);
                if (dirigido) {
                    if (a.origen.equals(v) && a.destino.equals(v)) {
                        mat[i][j] = 0; // bucle dirigido: +1 sale -1 llega = 0
                    } else if (a.origen.equals(v)) {
                        mat[i][j] = ponderado ? a.peso : 1;
                    } else if (a.destino.equals(v)) {
                        mat[i][j] = ponderado ? -a.peso : -1;
                    } else {
                        mat[i][j] = 0;
                    }
                } else {
                    // No dirigido: toca la arista?
                    if (a.origen.equals(v) && a.destino.equals(v)) {
                        // Bucle: toca el vertice dos veces
                        mat[i][j] = ponderado ? a.peso * 2 : 2;
                    } else if (a.origen.equals(v) || a.destino.equals(v)) {
                        mat[i][j] = ponderado ? a.peso : 1;
                    } else {
                        mat[i][j] = 0;
                    }
                }
            }
        }

        // Etiquetas de columnas = nombres de aristas
        ArrayList<String> colNames = new ArrayList<>();
        for (AristaId a : aristas) {
            colNames.add(a.nombre);
        }

        return new Matriz("Matriz de Incidencia (vertices x aristas)",
                vertices, colNames, mat);
    }

    // =========================================================================
    //  2. MATRIZ DE ADYACENCIA DE VERTICES (vertices x vertices)
    // =========================================================================
    /**
     * Calcula la Matriz de Adyacencia de Vertices.
     *
     * No dirigido: valor = peso si hay arista entre vi y vj, 0 si no. Si no
     * ponderado, valor = 1. Dirigido: valor = peso si hay arista de vi a vj, 0
     * si no hay (solo en la direccion de la flecha).
     *
     * @param grafo Grafo ponderado dirigido o no dirigido.
     * @return Matriz de adyacencia de vertices.
     */
    public static Matriz adyacenciaVertices(GrafoPonderado grafo) {
        ArrayList<String> vertices = grafo.getVertices();
        boolean ponderado = esPonderado(grafo);
        int n = vertices.size();
        double[][] mat = new double[n][n];

        for (GrafoPonderado.AristaPonderada a : grafo.getAristas()) {
            int i = vertices.indexOf(a.origen);
            int j = vertices.indexOf(a.destino);
            if (i == -1 || j == -1) {
                continue;
            }
            double val = ponderado ? a.peso : 1;
            if (i == j) {
                // Bucle: se cuenta dos veces (ida y vuelta por la misma arista)
                mat[i][j] = ponderado ? a.peso * 2 : 2;
            } else {
                mat[i][j] = val;
                if (!grafo.isDirigido()) {
                    mat[j][i] = val;
                }
            }

        }

        return new Matriz("Matriz de Adyacencia de Vertices (vertices x vertices)",
                vertices, vertices, mat);
    }

    // =========================================================================
    //  3. MATRIZ DE ADYACENCIA DE ARISTAS (aristas x aristas)
    // =========================================================================
    /**
     * Calcula la Matriz de Adyacencia de Aristas.
     *
     * Dos aristas son adyacentes si comparten un vertice.
     *
     * No dirigido: 1 si comparten vertice, 0 si no. Dirigido: +1 si el destino
     * de ai es el origen de aj (ai "entra" y aj "sale" del mismo nodo), -1 si
     * el origen de ai es el destino de aj (aj "entra" y ai "sale" del mismo
     * nodo), 0 si no son adyacentes.
     *
     * @param grafo Grafo ponderado dirigido o no dirigido.
     * @return Matriz de adyacencia de aristas.
     */
    public static Matriz adyacenciaAristas(GrafoPonderado grafo) {
        ArrayList<AristaId> aristas = getAristasUnicas(grafo);
        boolean dirigido = grafo.isDirigido();
        int na = aristas.size();
        double[][] mat = new double[na][na];

        for (int i = 0; i < na; i++) {
            for (int j = 0; j < na; j++) {
                if (i == j) {
                    mat[i][j] = 0;
                    continue;
                }
                AristaId ai = aristas.get(i);
                AristaId aj = aristas.get(j);

                if (dirigido) {
                    // +1: destino de ai == origen de aj (flujo continuo)
                    if (ai.destino.equals(aj.origen)) {
                        mat[i][j] = 1;
                    } // -1: origen de ai == destino de aj (flujo invertido)
                    else if (ai.origen.equals(aj.destino)) {
                        mat[i][j] = -1;
                    } else {
                        mat[i][j] = 0;
                    }
                } else {
                    // No dirigido: comparten algun vertice?
                    boolean comparten = ai.origen.equals(aj.origen)
                            || ai.origen.equals(aj.destino)
                            || ai.destino.equals(aj.origen)
                            || ai.destino.equals(aj.destino);
                    mat[i][j] = comparten ? 1 : 0;
                }
            }
        }

        ArrayList<String> nombres = new ArrayList<>();
        for (AristaId a : aristas) {
            nombres.add(a.nombre);
        }

        return new Matriz("Matriz de Adyacencia de Aristas (aristas x aristas)",
                nombres, nombres, mat);
    }

    // =========================================================================
    //  4. MATRIZ DE CIRCUITOS DIRIGIDOS (circuitos x aristas)
    // =========================================================================
    /**
     * Calcula la Matriz de Circuitos para un grafo DIRIGIDO.
     *
     * Para cada circuito: 1. Se ignora la direccion de las aristas para
     * encontrar el circuito. 2. Se elige el nodo de inicio: el mas
     * "arriba-izquierda" (primero en orden de insercion del grafo). 3. Se
     * recorre hacia la derecha (orden de los vertices del grafo). 4. Por cada
     * arista del circuito: +1 si la arista va en el sentido del recorrido, -1
     * si va en sentido contrario, 0 si no pertenece al circuito.
     *
     * Los circuitos se reciben como lista de aristas (de MatrizCircuitos). Esta
     * matriz NO recalcula los circuitos, los recibe ya calculados.
     *
     * @param grafo Grafo DIRIGIDO ponderado.
     * @param circuitos Lista de circuitos (cada uno como lista de pares
     * origen-destino). Se puede obtener de MatrizCircuitos con el grafo no
     * dirigido base.
     * @return Matriz de circuitos dirigidos.
     */
    public static Matriz circuitosDirigidos(GrafoPonderado grafo,
            ArrayList<ArrayList<String[]>> circuitos) {
        if (!grafo.isDirigido()) {
            throw new IllegalArgumentException(
                    "circuitosDirigidos requiere un grafo DIRIGIDO.");
        }

        ArrayList<AristaId> aristasGrafo = getAristasUnicas(grafo);
        ArrayList<String> vertices = grafo.getVertices();
        int nc = circuitos.size();
        int na = aristasGrafo.size();
        double[][] mat = new double[nc][na];

        ArrayList<String> nombresC = new ArrayList<>();
        for (int c = 0; c < nc; c++) {
            nombresC.add("C" + (c + 1));
            ArrayList<String[]> circ = circuitos.get(c);

            // Encontrar el nodo de inicio: el primero en orden de insercion del grafo
            // que aparezca en el circuito
            String nodoInicio = null;
            for (String v : vertices) {
                for (String[] par : circ) {
                    if (par[0].equals(v) || par[1].equals(v)) {
                        nodoInicio = v;
                        break;
                    }
                }
                if (nodoInicio != null) {
                    break;
                }
            }

            // Reconstruir el recorrido del circuito desde nodoInicio
            // Los pares representan aristas sin direccion: {v1, v2}
            ArrayList<String[]> recorrido = reconstruirRecorrido(circ, nodoInicio);

            // Asignar +1/-1 segun si la arista del grafo va en el sentido del recorrido
            for (int j = 0; j < na; j++) {
                AristaId a = aristasGrafo.get(j);
                int signo = obtenerSigno(a, recorrido);
                mat[c][j] = signo;
            }
        }

        ArrayList<String> nombresA = new ArrayList<>();
        for (AristaId a : aristasGrafo) {
            nombresA.add(a.nombre);
        }

        return new Matriz("Matriz de Circuitos Dirigidos (circuitos x aristas)",
                nombresC, nombresA, mat);
    }

    /**
     * Reconstruye el recorrido del circuito como lista ordenada de pares
     * (origen, destino) empezando desde nodoInicio y yendo hacia la derecha.
     */
    private static ArrayList<String[]> reconstruirRecorrido(
            ArrayList<String[]> aristas, String inicio) {
        ArrayList<String[]> recorrido = new ArrayList<>();
        ArrayList<String[]> restantes = new ArrayList<>(aristas);
        String actual = inicio;

        while (!restantes.isEmpty()) {
            boolean encontrado = false;
            for (int i = 0; i < restantes.size(); i++) {
                String[] par = restantes.get(i);
                if (par[0].equals(actual)) {
                    recorrido.add(new String[]{par[0], par[1]});
                    actual = par[1];
                    restantes.remove(i);
                    encontrado = true;
                    break;
                } else if (par[1].equals(actual)) {
                    recorrido.add(new String[]{par[1], par[0]});
                    actual = par[0];
                    restantes.remove(i);
                    encontrado = true;
                    break;
                }
            }
            if (!encontrado) {
                break;
            }
        }
        return recorrido;
    }

    /**
     * Determina el signo de una arista del grafo respecto al recorrido del
     * circuito. +1 si la arista va en el mismo sentido del recorrido. -1 si va
     * en sentido contrario. 0 si no pertenece al circuito.
     */
    private static int obtenerSigno(AristaId arista, ArrayList<String[]> recorrido) {
        for (String[] paso : recorrido) {
            // Mismo sentido: origen->destino coincide con el paso del recorrido
            if (arista.origen.equals(paso[0]) && arista.destino.equals(paso[1])) {
                return 1;
            }
            // Sentido contrario: la arista va al reves del recorrido
            if (arista.origen.equals(paso[1]) && arista.destino.equals(paso[0])) {
                return -1;
            }
        }
        return 0; // no pertenece al circuito
    }

    // =========================================================================
    //  METODO CONVENIENTE: calcular todas las matrices de una vez
    // =========================================================================
    /**
     * Calcula incidencia, adyacencia de vertices y adyacencia de aristas de una
     * sola vez.
     *
     * @param grafo Grafo ponderado dirigido o no dirigido.
     * @return Arreglo con las 3 matrices: [incidencia, adyVertices, adyAristas]
     */
    public static Matriz[] calcularTodas(GrafoPonderado grafo) {
        return new Matriz[]{
            incidencia(grafo),
            adyacenciaVertices(grafo),
            adyacenciaAristas(grafo)
        };
    }
}
