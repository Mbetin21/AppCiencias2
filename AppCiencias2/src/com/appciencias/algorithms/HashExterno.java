package com.appciencias.algorithms;

import com.appciencias.models.ClaveUtil;
import java.util.ArrayList;

/**
 * Busqueda externa por transformacion de clave
 *
 * El archivo se divide en numBloques bloques, cada uno con capacidad c. La
 * función hash determina en que bloque va cada clave. Cuando un bloque se llena
 * -> bloque de desbordamiento encadenado.
 *
 * Funciones hash: MOD : H(k) = (k mod numBloques) + 1 CUADRADO : H(k) = (k² mod
 * numBloques) + 1 PLEGAMIENTO_SUMA : divide k en grupos de 2 dígitos, los suma,
 * mod numBloques PLEGAMIENTO_MULT : divide k en grupos de 2 dígitos, los
 * multiplica, mod numBloques TRUNCAMIENTO : extrae digitos en posiciones
 * especificas, mod numBloques CONVERSION_BASE : convierte k a otra base, toma
 * sus digitos, mod numBloques
 *
 * Solución de colisiones: bloque de desbordamiento encadenado Cada bloque tiene
 * un puntero a un bloque de desbordamiento Cuando el bloque primario se llena,
 * las claves extra van al desbordamiento
 */
public class HashExterno {

    public enum Tipo {
        MOD,
        CUADRADO,
        PLEGAMIENTO_SUMA,
        PLEGAMIENTO_MULT,
        TRUNCAMIENTO,
        CONVERSION_BASE
    }

    /**
     * Cada bloque tiene capacidad c y puede tener un bloque de desbordamiento
     */
    public static class Bloque {

        public final int numero;       // numero de bloque (1-based)
        public final ArrayList<String> claves; // claves almacenadas
        public final int capacidad;    // maximo de claves
        public Bloque desbordamiento; // siguiente bloque encadenado
        public final boolean esPrimario;   // true = bloque primario, false = desbordamiento

        public Bloque(int numero, int capacidad, boolean esPrimario) {
            this.numero = numero;
            this.capacidad = capacidad;
            this.claves = new ArrayList<>();
            this.desbordamiento = null;
            this.esPrimario = esPrimario;
        }

        public boolean estaLleno() {
            return claves.size() >= capacidad;
        }

        public boolean estaVacio() {
            return claves.isEmpty();
        }
    }

    public static class ResultadoBusqueda {

        public final int bloqueBase;       // bloque donde deberia estar (1-based)
        public final int bloqueEncontrado; // bloque donde realmente esta (-1 si no existe)
        public final int posEnBloque;      // posicion dentro del bloque (1-based)
        public final int bloquesVisitados;
        public final boolean encontrada;
        public final boolean enDesbordamiento; // true si esta en un bloque de desbordamiento

        public ResultadoBusqueda(int bloqueBase, int bloqueEncontrado, int posEnBloque,
                int bloquesVisitados, boolean encontrada,
                boolean enDesbordamiento) {
            this.bloqueBase = bloqueBase;
            this.bloqueEncontrado = bloqueEncontrado;
            this.posEnBloque = posEnBloque;
            this.bloquesVisitados = bloquesVisitados;
            this.encontrada = encontrada;
            this.enDesbordamiento = enDesbordamiento;
        }

        @Override
        public String toString() {
            if (!encontrada) {
                return "Clave no encontrada. Bloque base: " + bloqueBase
                        + ", bloques visitados: " + bloquesVisitados + ".";
            }
            if (enDesbordamiento) {
                return "Encontrada en bloque de desbordamiento " + bloqueEncontrado
                        + " (base: " + bloqueBase + "), posición " + posEnBloque
                        + ". Bloques visitados: " + bloquesVisitados + ".";
            }
            return "Encontrada en bloque " + bloqueEncontrado
                    + ", posición " + posEnBloque
                    + ". Bloques visitados: " + bloquesVisitados + ".";
        }
    }

    private final int N;
    private final int c;           // capacidad por bloque
    private final int numBloques;
    private final int longClave;
    private final Tipo tipo;
    private final int base;        // para CONVERSION_BASE (ej. 8)
    private final int[] posicionesTrunc; // para TRUNCAMIENTO

    /**
     * Bloques primarios (indice 0 = bloque 1).
     */
    private final ArrayList<Bloque> bloquesPrimarios;

    private int contador;
    private int contadorDesbordamiento; // para numerar bloques de desbordamiento

    /**
     * Constructor para MOD, CUADRADO, PLEGAMIENTO_SUMA, PLEGAMIENTO_MULT.
     */
    public HashExterno(int N, int c, int longClave, Tipo tipo) {
        if (tipo == Tipo.TRUNCAMIENTO || tipo == Tipo.CONVERSION_BASE) {
            throw new IllegalArgumentException(
                    "Use el constructor especifico para TRUNCAMIENTO o CONVERSION_BASE.");
        }
        this.N = N;
        this.c = c;
        this.longClave = longClave;
        this.tipo = tipo;
        this.base = 8;
        this.posicionesTrunc = null;
        this.numBloques = (int) Math.ceil((double) N / c);
        this.contador = 0;
        this.contadorDesbordamiento = numBloques;
        this.bloquesPrimarios = crearBloquesPrimarios();
        validarParametros(N, c, longClave);
    }

    /**
     * Constructor para truncamiento
     *
     * @param posicionesTrunc Posiciones (1-based) de los digitos a extraer de k
     */
    public HashExterno(int N, int c, int longClave, int[] posicionesTrunc) {
        validarParametros(N, c, longClave);
        this.N = N;
        this.c = c;
        this.longClave = longClave;
        this.tipo = Tipo.TRUNCAMIENTO;
        this.base = 8;
        this.posicionesTrunc = posicionesTrunc;
        this.numBloques = (int) Math.ceil((double) N / c);
        this.contador = 0;
        this.contadorDesbordamiento = numBloques;
        this.bloquesPrimarios = crearBloquesPrimarios();
    }

    /**
     * Constructor para conversion de base.
     *
     * @param base Base destino (ej. 8 para octal, 16 para hexadecimal).
     */
    public HashExterno(int N, int c, int longClave, int base) {
        validarParametros(N, c, longClave);
        if (base < 2) {
            throw new IllegalArgumentException("La base debe ser al menos 2.");
        }
        this.N = N;
        this.c = c;
        this.longClave = longClave;
        this.tipo = Tipo.CONVERSION_BASE;
        this.base = base;
        this.posicionesTrunc = null;
        this.numBloques = (int) Math.ceil((double) N / c);
        this.contador = 0;
        this.contadorDesbordamiento = numBloques;
        this.bloquesPrimarios = crearBloquesPrimarios();
    }

    /**
     * Inserta una clave en el bloque que le corresponde segun la funcion. Si el
     * bloque esta lleno, va al bloque de desbordamiento encadenado.
     *
     * @throws IllegalStateException si la clave ya existe.
     * @throws IllegalArgumentException si la longitud de clave es incorrecta.
     */
    public void insertar(String clave) {
        ClaveUtil.validar(clave, longClave);

        if (buscar(clave).encontrada) {
            throw new IllegalStateException("La clave '" + clave + "' ya existe.");
        }

        int numBloque = calcularHash(clave); // 1-based
        Bloque bloque = bloquesPrimarios.get(numBloque - 1);

        // Buscar primer bloque con espacio (primario o desbordamiento)
        while (bloque.estaLleno()) {
            if (bloque.desbordamiento == null) {
                // Crear nuevo bloque de desbordamiento
                contadorDesbordamiento++;
                bloque.desbordamiento = new Bloque(contadorDesbordamiento, c, false);
            }
            bloque = bloque.desbordamiento;
        }

        bloque.claves.add(clave);
        contador++;
    }

    /**
     * Busca una clave empezando en el bloque H(k) y siguiendo la cadena de
     * desbordamiento si es necesario.
     */
    public ResultadoBusqueda buscar(String clave) {
        if (clave == null || clave.isEmpty()) {
            return new ResultadoBusqueda(-1, -1, -1, 0, false, false);
        }

        int bloqueBase = calcularHash(clave);
        int bloquesVisit = 0;
        Bloque actual = bloquesPrimarios.get(bloqueBase - 1);

        while (actual != null) {
            bloquesVisit++;
            for (int j = 0; j < actual.claves.size(); j++) {
                if (actual.claves.get(j).equals(clave)) {
                    return new ResultadoBusqueda(
                            bloqueBase, actual.numero, j + 1,
                            bloquesVisit, true, !actual.esPrimario);
                }
            }
            // Si el bloque no esta lleno, la clave no puede estar más adelante
            if (!actual.estaLleno() && actual.desbordamiento == null) {
                break;
            }
            actual = actual.desbordamiento;
        }

        return new ResultadoBusqueda(bloqueBase, -1, -1, bloquesVisit, false, false);
    }

    /**
     * Elimina una clave de su bloque. Si la clave estaba en un bloque de
     * desbordamiento, compacta la cadena: mueve el ultimo elemento de la cadena
     * a la posición eliminada para no dejar huecos que rompan la búsqueda.
     *
     * @throws IllegalArgumentException si la clave no existe.
     */
    public void eliminar(String clave) {
        ClaveUtil.validar(clave, longClave);

        ResultadoBusqueda r = buscar(clave);
        if (!r.encontrada) {
            throw new IllegalArgumentException("La clave '" + clave + "' no existe.");
        }

        int bloqueBase = r.bloqueBase;
        Bloque actual = bloquesPrimarios.get(bloqueBase - 1);

        // Encontrar el bloque que contiene la clave y el bloque anterior
        Bloque anterior = null;
        Bloque bloqueTarget = null;
        int posTarget = -1;

        Bloque b = actual;
        while (b != null) {
            int idx = b.claves.indexOf(clave);
            if (idx != -1) {
                bloqueTarget = b;
                posTarget = idx;
                break;
            }
            anterior = b;
            b = b.desbordamiento;
        }

        if (bloqueTarget == null) {
            return; // no deberia pasar
        }
        // Encontrar el ultimo bloque de la cadena y su ultimo elemento
        Bloque ultimo = bloqueTarget;
        Bloque penultimo = anterior;
        while (ultimo.desbordamiento != null) {
            penultimo = ultimo;
            ultimo = ultimo.desbordamiento;
        }

        if (ultimo == bloqueTarget) {
            // La clave esta en el último bloque: solo eliminar
            bloqueTarget.claves.remove(posTarget);
        } else {
            // Mover el ultimo elemento al hueco
            String ultimaClave = ultimo.claves.remove(ultimo.claves.size() - 1);
            bloqueTarget.claves.set(posTarget, ultimaClave);
        }

        // Limpiar bloques de desbordamiento vacios al final de la cadena
        limpiarDesbordes(bloquesPrimarios.get(bloqueBase - 1));

        contador--;
    }

    /**
     * Elimina bloques de desbordamiento vacios al final de la cadena.
     */
    private void limpiarDesbordes(Bloque primario) {
        if (primario.desbordamiento == null) {
            return;
        }
        if (primario.desbordamiento.estaVacio()) {
            primario.desbordamiento = null;
            return;
        }
        Bloque anterior = primario;
        Bloque actual = primario.desbordamiento;
        while (actual.desbordamiento != null) {
            anterior = actual;
            actual = actual.desbordamiento;
        }
        if (actual.estaVacio()) {
            anterior.desbordamiento = null;
        }
    }

    /**
     * Crea los bloques primarios vacios
     */
    private ArrayList<Bloque> crearBloquesPrimarios() {
        ArrayList<Bloque> lista = new ArrayList<>();
        for (int i = 1; i <= numBloques; i++) {
            lista.add(new Bloque(i, c, true));
        }
        return lista;
    }

    /**
     * Calcula el numero de bloque (1-based) para una clave
     */
    private int calcularHash(String clave) {
        switch (tipo) {
            case MOD:
                return hashMod(clave);
            case CUADRADO:
                return hashCuadrado(clave);
            case PLEGAMIENTO_SUMA:
                return hashPlegamiento(clave, true);
            case PLEGAMIENTO_MULT:
                return hashPlegamiento(clave, false);
            case TRUNCAMIENTO:
                return hashTruncamiento(clave);
            case CONVERSION_BASE:
                return hashConversionBase(clave);
            default:
                throw new IllegalStateException("Función hash no reconocida.");
        }
    }

    /**
     * H(k) = (k mod numBloques) + 1
     */
    private int hashMod(String clave) {
        long k = ClaveUtil.aNumero(clave);
        return (int) (k % numBloques) + 1;
    }

    /**
     * H(k) = (k² mod numBloques) + 1
     */
    private int hashCuadrado(String clave) {
        long k = ClaveUtil.aNumero(clave) % 1_000_000L;
        long cuadrado = k * k;
        return (int) (cuadrado % numBloques) + 1;
    }

    /**
     * Divide los digitos de k en grupos de 2, los suma o multiplica, mod
     * numBloques.
     */
    private int hashPlegamiento(String clave, boolean suma) {
        String digitos = ClaveUtil.aDigitos(clave);
        ArrayList<Long> grupos = new ArrayList<>();
        for (int i = 0; i < digitos.length(); i += 2) {
            int fin = Math.min(i + 2, digitos.length());
            grupos.add(Long.parseLong(digitos.substring(i, fin)));
        }
        long resultado;
        if (suma) {
            resultado = 0;
            for (long g : grupos) {
                resultado += g;
            }
        } else {
            resultado = 1;
            for (long g : grupos) {
                resultado *= (g == 0 ? 1 : g);
            }
        }
        return (int) (resultado % numBloques) + 1;
    }

    /**
     * Extrae digitos en posiciones especificas de k, mod numBloques
     */
    private int hashTruncamiento(String clave) {
        String digitos = ClaveUtil.aDigitos(clave);
        StringBuilder extraido = new StringBuilder();
        for (int pos : posicionesTrunc) {
            if (pos >= 1 && pos <= digitos.length()) {
                extraido.append(digitos.charAt(pos - 1));
            }
        }
        if (extraido.length() == 0) {
            throw new IllegalStateException(
                    "Ninguna posición de truncamiento es válida para la clave '" + clave + "'.");
        }
        long valor = Long.parseLong(extraido.toString());
        return (int) (valor % numBloques) + 1;
    }

    /**
     * Convierte k a la base configurada, toma los digitos resultantes, los suma
     * y aplica mod numBloques.
     */
    private int hashConversionBase(String clave) {
        String digitos = ClaveUtil.aDigitos(clave); // digitos de k
        int n = digitos.length();
        long resultado = 0;
        for (int i = 0; i < n; i++) {
            int digito = Character.getNumericValue(digitos.charAt(i));
            long potencia = (long) Math.pow(base, n - 1 - i);
            resultado += digito * potencia;
        }
        return (int) (resultado % numBloques) + 1;
    }

    /**
     * Todos los bloques primarios con sus cadenas de desbordamiento.
     */
    public ArrayList<Bloque> obtenerBloquesPrimarios() {
        return new ArrayList<>(bloquesPrimarios);
    }

    /**
     * Numero de bloque base para una clave (1-based)
     */
    public int obtenerBloqueBase(String clave) {
        return calcularHash(clave);
    }

    /**
     * Resumen
     */
    public String obtenerInfo() {
        return "N=" + N + "  c=" + c + "  Bloques=" + numBloques
                + "  Registros actuales=" + contador;
    }

    public int getN() {
        return N;
    }

    public int getC() {
        return c;
    }

    public int getNumBloques() {
        return numBloques;
    }

    public int getLongClave() {
        return longClave;
    }

    public int getContador() {
        return contador;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public int getBase() {
        return base;
    }

    private void validarParametros(int N, int c, int longClave) {
        if (N <= 0) {
            throw new IllegalArgumentException("N debe ser mayor que 0.");
        }
        if (c <= 0) {
            throw new IllegalArgumentException("c debe ser mayor que 0.");
        }
        if (longClave <= 0) {
            throw new IllegalArgumentException("longClave debe ser mayor que 0.");
        }
    }
}
