package com.appciencias.algorithms;

import java.util.ArrayList;

/**
 * Grafo DIRIGIDO con pesos positivos.
 *
 * G = (S, A) donde cada arista tiene un peso > 0.
 */
public class GrafoPonderado {

    /**
     * Arista DIRIGIDA con peso positivo. "1->2 con peso 5" la dirección
     * contraria es otra cosa.
     */
    public static class AristaPonderada {

        public final String origen;
        public final String destino;
        public final double peso;

        public AristaPonderada(String origen, String destino, double peso) {
            this.origen = origen.trim();
            this.destino = destino.trim();
            this.peso = peso;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof AristaPonderada)) {
                return false;
            }
            AristaPonderada a = (AristaPonderada) o;
            // Dirigido: origen y destino importan en orden
            return origen.equals(a.origen) && destino.equals(a.destino);
        }

        @Override
        public int hashCode() {
            return origen.hashCode() * 31 + destino.hashCode();
        }

        @Override
        public String toString() {
            return origen + " -> " + destino + " (peso: " + peso + ")";
        }
    }

    private final String nombre;
    private final ArrayList<String> vertices;
    private final ArrayList<AristaPonderada> aristas;

    public GrafoPonderado(String nombre) {
        this.nombre = nombre;
        this.vertices = new ArrayList<>();
        this.aristas = new ArrayList<>();
    }

    /**
     * Agrega vertices multiples.
     *
     * @throws IllegalArgumentException si alguno ya existe o el nombre es
     * vacio.
     */
    public void agregarVertices(String entrada) {
        if (entrada == null || entrada.trim().isEmpty()) {
            throw new IllegalArgumentException("La entrada no puede estar vacía.");
        }
        String[] partes = entrada.split(",");
        for (String parte : partes) {
            agregarVertice(parte.trim());
        }
    }

    /**
     * Agrega un solo vertice.
     */
    public void agregarVertice(String v) {
        if (v == null || v.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del vértice no puede estar vacío.");
        }
        v = v.trim().toLowerCase();
        if (vertices.contains(v)) {
            throw new IllegalArgumentException("El vértice '" + v + "' ya existe.");
        }
        vertices.add(v);
    }

    /**
     * Elimina un vertice y todas sus aristas asociadas.
     *
     * @throws IllegalArgumentException si el vertice no existe.
     */
    public void eliminarVertice(String v) {
        v = v.trim().toLowerCase();
        if (!vertices.contains(v)) {
            throw new IllegalArgumentException("El vértice '" + v + "' no existe.");
        }
        vertices.remove(v);
        final String vFinal = v;
        aristas.removeIf(a -> a.origen.equals(vFinal) || a.destino.equals(vFinal));
    }

    /**
     * Agrega una arista dirigida con peso.
     *
     * @param origen vertice de origen (debe existir)
     * @param destino vertice de destino (debe existir)
     * @param peso peso de la arista (debe ser positivo)
     * @throws IllegalArgumentException si algun vertice no existe, si la arista
     * ya existe, si es un bucle, o si el peso <= 0.
     */
    public void agregarArista(String origen, String destino, double peso) {
        origen = origen.trim().toLowerCase();
        destino = destino.trim().toLowerCase();

        if (!vertices.contains(origen)) {
            throw new IllegalArgumentException("El vértice origen '" + origen + "' no existe.");
        }
        if (!vertices.contains(destino)) {
            throw new IllegalArgumentException("El vértice destino '" + destino + "' no existe.");
        }
        if (origen.equals(destino)) {
            throw new IllegalArgumentException("Una arista no puede conectar un vértice consigo mismo.");
        }
        if (peso <= 0) {
            throw new IllegalArgumentException("El peso debe ser un número positivo mayor que 0.");
        }

        AristaPonderada nueva = new AristaPonderada(origen, destino, peso);
        if (aristas.contains(nueva)) {
            throw new IllegalArgumentException(
                    "La arista '" + origen + " -> " + destino + "' ya existe.");
        }
        aristas.add(nueva);
    }

    /**
     * Elimina una arista dirigida.
     *
     * @throws IllegalArgumentException si la arista no existe.
     */
    public void eliminarArista(String origen, String destino) {
        origen = origen.trim().toLowerCase();
        destino = destino.trim().toLowerCase();
        AristaPonderada a = new AristaPonderada(origen, destino, 1);
        if (!aristas.contains(a)) {
            throw new IllegalArgumentException(
                    "La arista '" + origen + " -> " + destino + "' no existe.");
        }
        aristas.remove(a);
    }

    /**
     * Limpia el grafo completamente.
     */
    public void limpiar() {
        vertices.clear();
        aristas.clear();
    }

    public String getNombre() {
        return nombre;
    }

    public ArrayList<String> getVertices() {
        return new ArrayList<>(vertices);
    }

    public ArrayList<AristaPonderada> getAristas() {
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

    public boolean contieneArista(String origen, String destino) {
        return aristas.contains(new AristaPonderada(origen, destino, 1));
    }

    /**
     * Vecinos de salida de un vertice (aristas que salen de v).
     */
    public ArrayList<String> getVecinosSalida(String v) {
        ArrayList<String> vecinos = new ArrayList<>();
        for (AristaPonderada a : aristas) {
            if (a.origen.equals(v)) {
                vecinos.add(a.destino);
            }
        }
        return vecinos;
    }

    /**
     * Peso de la arista origen->destino. Retorna -1 si no existe.
     */
    public double getPeso(String origen, String destino) {
        for (AristaPonderada a : aristas) {
            if (a.origen.equals(origen) && a.destino.equals(destino)) {
                return a.peso;
            }
        }
        return -1;
    }

    public String getVerticesStr() {
        if (vertices.isEmpty()) {
            return "S = {}";
        }
        return "S = {" + String.join(", ", vertices) + "}";
    }

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
