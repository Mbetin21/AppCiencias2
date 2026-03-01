package com.appciencias.algorithms;

import java.util.ArrayList;

/**
 * Arbol huffman
 * Mensaje encriptado usando las frecuencas de las letras en el mensaje.
 */ 
public class ArbolHuffman {

    public static class Nodo {
        public String  etiqueta;
        public int     frecuencia;
        public int     orden;       // orden de entrada (primero = más abajo)
        public Nodo    izquierda;   // bit 0
        public Nodo    derecha;     // bit 1
        public boolean esHoja;

        public Nodo(String etiqueta, int frecuencia, int orden) {
            this.etiqueta   = etiqueta;
            this.frecuencia = frecuencia;
            this.orden      = orden;
            this.esHoja     = true;
        }

        // Nodo union: el primero (mas abajo) va a izquierda, el segundo a derecha...
        public Nodo(Nodo abajo, Nodo segundo, int ordenNuevo) {
            this.etiqueta   = abajo.etiqueta + "+" + segundo.etiqueta;
            this.frecuencia = abajo.frecuencia + segundo.frecuencia;
            this.orden      = ordenNuevo;
            this.izquierda  = abajo;
            this.derecha    = segundo;
            this.esHoja     = false;
        }
    }

    /**
     * Tablas por cada paso.
     * nodos: index 0 = arriba, index n-1 = abajo (primero en entrar).
     */
    public static class TablaIntermedia {
        public final int             numeroPaso;
        public final ArrayList<Nodo> nodos;
        public final String          descripcion;

        public TablaIntermedia(int numeroPaso, ArrayList<Nodo> nodos, String descripcion) {
            this.numeroPaso  = numeroPaso;
            this.nodos       = new ArrayList<>(nodos);
            this.descripcion = descripcion;
        }
    }

    //Calculadora para frecuencia: letra, frecuencia 
    public static class CodigoHuffman {
        public final String letra;
        public final int    frecuencia;
        public final String codigo;

        public CodigoHuffman(String letra, int frecuencia, String codigo) {
            this.letra      = letra;
            this.frecuencia = frecuencia;
            this.codigo     = codigo;
        }
    }

    private String                     textoOriginal;
    private ArrayList<TablaIntermedia> tablas;
    private ArrayList<CodigoHuffman>   codigos;
    private Nodo                       raiz;
    private boolean                    construido;

    public ArbolHuffman() {
        this.tablas     = new ArrayList<>();
        this.codigos    = new ArrayList<>();
        this.construido = false;
    }

    public void construir(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("El texto no puede estar vacio.");
        }

        texto = texto.toUpperCase().replaceAll("[^A-Z]", "");

        if (texto.isEmpty()) {
            throw new IllegalArgumentException("El texto debe contener al menos una letra.");
        }

        this.textoOriginal = texto;
        this.tablas        = new ArrayList<>();
        this.codigos       = new ArrayList<>();
        this.construido    = false;

        // Contar frecuencias en orden de aparicion
        ArrayList<Nodo> lista = contarFrecuencias(texto);

        // Ordenar: abajo los que entraron primero
        ordenar(lista);

        // Tabla inicial
        tablas.add(new TablaIntermedia(0, lista,
                "Frecuencias iniciales (abajo = primero en entrar)"));

        // Caso especial: una sola letra
        if (lista.size() == 1) {
            raiz = lista.get(0);
            codigos.add(new CodigoHuffman(raiz.etiqueta, raiz.frecuencia, "0"));
            construido = true;
            return;
        }

        // Proceso iterativo: uniendo los dos de abajo
        int contadorOrden = lista.size();

        while (lista.size() > 1) {
            // Tomar los DOS DE ABAJO
            Nodo primero = lista.remove(lista.size() - 1); // mas abajo (primer caracter)
            Nodo segundo = lista.remove(lista.size() - 1); // segundo mas abajo

            // Crear nodo de union
            Nodo union = new Nodo(primero, segundo, contadorOrden++);

            // Reinsertar y reordenar
            lista.add(union);
            ordenar(lista);

            String desc = "Paso " + tablas.size() + ": unir '"
                    + primero.etiqueta + "' (" + primero.frecuencia + ") + '"
                    + segundo.etiqueta + "' (" + segundo.frecuencia + ") = "
                    + union.frecuencia;
            tablas.add(new TablaIntermedia(tablas.size(), lista, desc));
        }

        // Raiz
        raiz = lista.get(0);

        generarCodigos(raiz, "");

        construido = true;
    }

    // Manejo de frecuencias
    private ArrayList<Nodo> contarFrecuencias(String texto) {
        ArrayList<String>  letras = new ArrayList<>();
        ArrayList<Integer> freqs  = new ArrayList<>();

        for (char c : texto.toCharArray()) {
            String letra = String.valueOf(c);
            int idx = letras.indexOf(letra);
            if (idx == -1) { letras.add(letra); freqs.add(1); }
            else freqs.set(idx, freqs.get(idx) + 1);
        }

        ArrayList<Nodo> nodos = new ArrayList<>();
        for (int i = 0; i < letras.size(); i++) {
            nodos.add(new Nodo(letras.get(i), freqs.get(i), i));
        }
        return nodos;
    }

    // ── ORDENAR ───────────────────────────────────────────────────────────────

    /**
     * Ordena: index 0 = arriba, index n-1 = abajo.
     * Abajo = menor frecuencia. Empate = menor orden (entro primero) va mas abajo.
     */
    private void ordenar(ArrayList<Nodo> lista) {
        for (int i = 1; i < lista.size(); i++) {
            Nodo key = lista.get(i);
            int j = i - 1;
            while (j >= 0 && debeIrMasAbajo(lista.get(j), key)) {
                lista.set(j + 1, lista.get(j));
                j--;
            }
            lista.set(j + 1, key);
        }
    }

    /**
     * true si 'a' debe ir más abajo que 'b'.
     * Mas abajo = menor frecuencia, o en empate, menor orden (entro primero).
     */
    private boolean debeIrMasAbajo(Nodo a, Nodo b) {
        if (a.frecuencia != b.frecuencia) return a.frecuencia < b.frecuencia;
        return a.orden < b.orden;
    }

    private void generarCodigos(Nodo nodo, String actual) {
        if (nodo == null) return;
        if (nodo.esHoja) {
            codigos.add(new CodigoHuffman(
                    nodo.etiqueta, nodo.frecuencia,
                    actual.isEmpty() ? "0" : actual));
            return;
        }
        generarCodigos(nodo.izquierda, actual + "0");
        generarCodigos(nodo.derecha,   actual + "1");
    }

    /**
     * Codifica el texto original con los codigos generados.
     */
    public String codificarTexto() {
        validarConstruido();
        StringBuilder sb = new StringBuilder();
        for (char c : textoOriginal.toCharArray()) {
            String letra = String.valueOf(c);
            for (CodigoHuffman ch : codigos) {
                if (ch.letra.equals(letra)) {
                    sb.append(ch.codigo).append(" ");
                    break;
                }
            }
        }
        return sb.toString().trim();
    }

    /**
     * Todas las tablas intermedias para mostrar paso a paso.
     * Cada TablaIntermedia.nodos esta ordenada: index 0=arriba, index n-1=abajo.
     */
    public ArrayList<TablaIntermedia> getTablasIntermedias() {
        validarConstruido();
        return new ArrayList<>(tablas);
    }

    /**
     * Tabla finales: Letra, Frecuencia
     */
    public ArrayList<CodigoHuffman> getCodigos() {
        validarConstruido();
        return new ArrayList<>(codigos);
    }

    /**
     * Raiz del arbol final.
     * nodo.izquierda = bit 0, nodo.derecha = bit 1, nodo.esHoja = es letra real
     */
    public Nodo getRaiz() {
        validarConstruido();
        return raiz;
    }

    public String  getTextoOriginal()   { return textoOriginal; }
    public int     getNumeroDistintas() { validarConstruido(); return codigos.size(); }
    public boolean estaConstruido()     { return construido; }

    private void validarConstruido() {
        if (!construido) throw new IllegalStateException(
                "Primero llama a construir(texto) antes de consultar resultados.");
    }
}