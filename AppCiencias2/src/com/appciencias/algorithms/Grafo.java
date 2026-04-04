package com.appciencias.algorithms;

import java.util.ArrayList;

/**
 * Grafo G = (S, A). Se contruye paso a paso, crei que seria la mejor forma de
 * hacerlo.
 *
 * S = conjunto de vertices (strings) A = conjunto de aristas (pares de
 * vertices)
 *
 * Una arista solo puede existir si ambos vertices ya fueron agregados.
 *
 * Operaciones (reciben dos grafos y retornan el otro segun la operacion): -
 * union -> G1 ∪ G2 - interseccion -> G1 ∩ G2 - sumaAnillo -> G1 ⊕ G2
 */
public class Grafo {

    /**
     * Par no ordenado de vertices. "1-2" y "2-1" son la misma arista.
     */
    public static class Arista {

        public final String v1;
        public final String v2;

        public Arista(String v1, String v2) {
            this.v1 = v1.trim();
            this.v2 = v2.trim();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Arista)) {
                return false;
            }
            Arista a = (Arista) o;
            return (v1.equals(a.v1) && v2.equals(a.v2))
                    || (v1.equals(a.v2) && v2.equals(a.v1));
        }

        @Override
        public int hashCode() {
            return v1.hashCode() + v2.hashCode();
        }

        @Override
        public String toString() {
            return v1 + "-" + v2;
        }
    }

    private final String nombre;
    private final ArrayList<String> vertices;
    private final ArrayList<Arista> aristas;

    public Grafo(String nombre) {
        this.nombre = nombre;
        this.vertices = new ArrayList<>();
        this.aristas = new ArrayList<>();
    }

    /**
     * Agrega un vertice al grafo.
     *
     * @throws IllegalArgumentException si el vertice ya existe o el nombre es
     * vacio.
     */
    public void agregarVertice(String v) {
        if (v == null || v.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del vertice no puede estar vacio.");
        }
        v = v.trim();
        if (vertices.contains(v)) {
            throw new IllegalArgumentException("El vertice '" + v + "' ya existe en el grafo.");
        }
        vertices.add(v);
    }

    /**
     * Agrega una arista entre dos vertices que ya deben existir en el grafo.
     *
     * @throws IllegalArgumentException si alguno de los vertices no existe, si
     * la arista ya existe, o si v1 == v2 (bucle).
     */
    public void agregarArista(String v1, String v2) {
        v1 = v1.trim();
        v2 = v2.trim();

        if (!vertices.contains(v1)) {
            throw new IllegalArgumentException(
                    "El vertice '" + v1 + "' no existe.");
        }
        if (!vertices.contains(v2)) {
            throw new IllegalArgumentException(
                    "El vertice '" + v2 + "' no existe.");
        }
        if (v1.equals(v2)) {
            throw new IllegalArgumentException(
                    "Una arista no puede conectar un vertice consigo mismo ('" + v1 + "').");
        }

        Arista nueva = new Arista(v1, v2);
        if (aristas.contains(nueva)) {
            throw new IllegalArgumentException(
                    "La arista '" + v1 + "-" + v2 + "' ya existe en el grafo.");
        }
        aristas.add(nueva);
    }

    /**
     * Elimina un vertice y todas sus aristas asociadas.
     *
     * @throws IllegalArgumentException si el vertice no existe.
     */
    public void eliminarVertice(String v) {
        v = v.trim();
        if (!vertices.contains(v)) {
            throw new IllegalArgumentException("El vertice '" + v + "' no existe.");
        }
        vertices.remove(v);
        // Eliminar todas las aristas que incluyan este vertice
        aristas.removeIf(a -> a.v1.equals(v) || a.v2.equals(v));
    }

    /**
     * Elimina una arista.
     *
     * @throws IllegalArgumentException si la arista no existe.
     */
    public void eliminarArista(String v1, String v2) {
        v1 = v1.trim();
        v2 = v2.trim();
        Arista a = new Arista(v1, v2);
        if (!aristas.contains(a)) {
            throw new IllegalArgumentException(
                    "La arista '" + v1 + "-" + v2 + "' no existe.");
        }
        aristas.remove(a);
    }

    /**
     * Limpia completamente el grafo (quita todos los vertices y aristas).
     */
    public void limpiar() {
        vertices.clear();
        aristas.clear();
    }

    // ================================== O P E R A C I O N E S ==================================
    /**
     * Union: G1 ∪ G2 = G3 S3 = S1 ∪ S2 A3 = A1 ∪ A2
     */
    public static Grafo union(Grafo g1, Grafo g2) {
        Grafo g3 = new Grafo("G1 ∪ G2");

        for (String v : g1.vertices) {
            if (!g3.vertices.contains(v)) {
                g3.vertices.add(v);
            }
        }
        for (String v : g2.vertices) {
            if (!g3.vertices.contains(v)) {
                g3.vertices.add(v);
            }
        }

        for (Arista a : g1.aristas) {
            if (!g3.aristas.contains(a)) {
                g3.aristas.add(a);
            }
        }
        for (Arista a : g2.aristas) {
            if (!g3.aristas.contains(a)) {
                g3.aristas.add(a);
            }
        }

        return g3;
    }

    /**
     * Interseccion: G1 ∩ G2 = G3 S3 = S1 ∩ S2 A3 = A1 ∩ A2
     */
    public static Grafo interseccion(Grafo g1, Grafo g2) {
        Grafo g3 = new Grafo("G1 ∩ G2");

        for (String v : g1.vertices) {
            if (g2.vertices.contains(v)) {
                g3.vertices.add(v);
            }
        }

        for (Arista a : g1.aristas) {
            if (g2.aristas.contains(a)) {
                g3.aristas.add(a);
            }
        }

        return g3;
    }

    /**
     * Suma anillo (diferencia simetrica): G1 ⊕ G2 = G3 S3 = S1 ∪ S2 A3 = (A1 ∪
     * A2) - (A1 ∩ A2)
     */
    public static Grafo sumaAnillo(Grafo g1, Grafo g2) {
        Grafo g3 = new Grafo("G1 ⊕ G2");

        for (String v : g1.vertices) {
            if (!g3.vertices.contains(v)) {
                g3.vertices.add(v);
            }
        }
        for (String v : g2.vertices) {
            if (!g3.vertices.contains(v)) {
                g3.vertices.add(v);
            }
        }

        // Aristas de G1 que NO estan en G2
        for (Arista a : g1.aristas) {
            if (!g2.aristas.contains(a)) {
                g3.aristas.add(a);
            }
        }

        // Aristas de G2 que NO estan en G1
        for (Arista a : g2.aristas) {
            if (!g1.aristas.contains(a)) {
                g3.aristas.add(a);
            }
        }

        return g3;
    }

    /**
     * Suma normal: G1 + G2 = G3 Todos los vertices de G1 se conectan con todos
     * los de G2 y viceversa.
     */
    public static Grafo suma(Grafo g1, Grafo g2) {
        Grafo g3 = new Grafo("G1 + G2");

        // S3
        for (String v : g1.vertices) {
            if (!g3.vertices.contains(v)) {
                g3.vertices.add(v);
            }
        }
        for (String v : g2.vertices) {
            if (!g3.vertices.contains(v)) {
                g3.vertices.add(v);
            }
        }

        // A3
        for (Arista a : g1.aristas) {
            if (!g3.aristas.contains(a)) {
                g3.aristas.add(a);
            }
        }
        for (Arista a : g2.aristas) {
            if (!g3.aristas.contains(a)) {
                g3.aristas.add(a);
            }
        }

        // Agregar todas las aristas cruzadas
        for (String v1 : g1.vertices) {
            for (String v2 : g2.vertices) {
                Arista cruzada = new Arista(v1, v2);
                if (!g3.aristas.contains(cruzada)) {
                    g3.aristas.add(cruzada);
                }
            }
        }

        return g3;
    }

    /**
     * Fusion de vertices: fusiona v1 y v2 en un solo vertice dentro del grafo.
     *
     * El vertice fusionado se llama "v1,v2" (con barrota encima solo por
     * representacion). Todas las aristas que apuntaban a v1 o v2 ahora apuntan
     * al fusionado. La arista entre v1 y v2 (si existía) desaparece.
     *
     * Modifica el grafo actual.
     *
     * @throws IllegalArgumentException si alguno de los vertices no existe o si
     * son el mismo.
     */
    public void fusionarVertices(String v1, String v2) {
        v1 = v1.trim();
        v2 = v2.trim();

        if (!vertices.contains(v1)) {
            throw new IllegalArgumentException(
                    "El vértice '" + v1 + "' no existe.");
        }
        if (!vertices.contains(v2)) {
            throw new IllegalArgumentException(
                    "El vértice '" + v2 + "' no existe.");
        }
        if (v1.equals(v2)) {
            throw new IllegalArgumentException(
                    "No se puede fusionar un vértice consigo mismo.");
        }

        String fusionado = v1 + "," + v2; // nombre del vertice fusionado

        // Reemplazar v1 y v2 por el fusionado en la lista de vertices
        int posV1 = vertices.indexOf(v1);
        vertices.set(posV1, fusionado);
        vertices.remove(v2);

        // Actualizar aristas:
        ArrayList<Arista> nuevasAristas = new ArrayList<>();
        for (Arista a : aristas) {
            // Saltar la arista entre v1 y v2
            if ((a.v1.equals(v1) && a.v2.equals(v2))
                    || (a.v1.equals(v2) && a.v2.equals(v1))) {
                continue;
            }

            String nuevoV1 = a.v1;
            String nuevoV2 = a.v2;

            if (nuevoV1.equals(v1) || nuevoV1.equals(v2)) {
                nuevoV1 = fusionado;
            }
            if (nuevoV2.equals(v1) || nuevoV2.equals(v2)) {
                nuevoV2 = fusionado;
            }

            Arista nueva = new Arista(nuevoV1, nuevoV2);

            // Evitar aristas duplicadas o bucles que puedan surgir
            if (!nuevoV1.equals(nuevoV2) && !nuevasAristas.contains(nueva)) {
                nuevasAristas.add(nueva);
            }
        }

        aristas.clear();
        aristas.addAll(nuevasAristas);
    }

    public String getNombre() {
        return nombre;
    }

    public ArrayList<String> getVertices() {
        return new ArrayList<>(vertices);
    }

    public ArrayList<Arista> getAristas() {
        return new ArrayList<>(aristas);
    }

    public int getNumVertices() {
        return vertices.size();
    }

    public int getNumAristas() {
        return aristas.size();
    }

    public boolean contieneVertice(String v) {
        return vertices.contains(v.trim());
    }

    public boolean contieneArista(String v1, String v2) {
        return aristas.contains(new Arista(v1, v2));
    }

    public ArrayList<String> getVecinos(String v) {
        ArrayList<String> vecinos = new ArrayList<>();
        for (Arista a : aristas) {
            if (a.v1.equals(v)) {
                vecinos.add(a.v2);
            } else if (a.v2.equals(v)) {
                vecinos.add(a.v1);
            }
        }
        return vecinos;
    }

    /**
     * String de vertices S = {1, 2, 3}
     */
    public String getVerticesStr() {
        if (vertices.isEmpty()) {
            return "S = {}";
        }
        StringBuilder sb = new StringBuilder("S = {");
        for (int i = 0; i < vertices.size(); i++) {
            sb.append(vertices.get(i));
            if (i < vertices.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.append("}").toString();
    }

    /**
     * String de aristas A = {1-2, 2-3}
     */
    public String getAristasStr() {
        if (aristas.isEmpty()) {
            return "A = {}";
        }
        StringBuilder sb = new StringBuilder("A = {");
        for (int i = 0; i < aristas.size(); i++) {
            sb.append(aristas.get(i).toString());
            if (i < aristas.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.append("}").toString();
    }

    @Override
    public String toString() {
        return nombre + "\n" + getVerticesStr() + "\n" + getAristasStr();
    }
}
