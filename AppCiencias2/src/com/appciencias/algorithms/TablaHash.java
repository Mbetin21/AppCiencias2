package com.appciencias.algorithms;

import com.appciencias.models.ClaveUtil;
import java.util.ArrayList;

/**
 * MOD -> H(k) = (k mod n) + 1 CUADRADO -> H(k) = (k² mod n) + 1 TRUNCAMIENTO ->
 * extrae posiciones específicas de la clave, suma sus ASCII, aplica mod
 * PLEGAMIENTO -> divide en grupos de 2, suma o multiplica grupos, aplica mod
 *
 * Solucion de las colisiones LINEAL -> D' = D + i (donde i = 1, 2, 3...)
 * CUADRATICA -> D' = D + i² (i = 1, 2, 3...) DOBLE_HASH -> D' = D + i * H2(k)
 * con H2(k) = 1 + (k mod (n-1)) (No la entendi bien se lo pedi a la IA)
 */
public class TablaHash {

    public enum FuncionHash {
        MOD, CUADRADO, TRUNCAMIENTO, PLEGAMIENTO
    }

    public enum TipoColision {
        LINEAL, CUADRATICA, DOBLE_HASH
    }

    public enum TipoPlegamiento {
        SUMA, MULTIPLICACION
    }

    // Marcador para posiciones eliminadas
    private static final String ELIMINADO = "__DELETED__";

    private String[] tabla;
    private int n;            // tamaño de la tabla
    private int longClave;    // caracteres por clave
    private int contador;     // elementos activos

    private FuncionHash funcionHash;
    private TipoColision tipoColision;

    // Para TRUNCAMIENTO
    private int[] posicionesTrunc;   // posiciones (1-based) a extraer

    // Para PLEGAMIENTO
    private TipoPlegamiento tipoPlegamiento; // SUMA o MULTIPLICACION

    /**
     * Constructor para MOD o CUADRADO.
     */
    public TablaHash(int tamaño, int longClave, FuncionHash funcionHash, TipoColision tipoColision) {
        validarBase(tamaño, longClave);
        if (funcionHash == FuncionHash.TRUNCAMIENTO || funcionHash == FuncionHash.PLEGAMIENTO) {
            throw new IllegalArgumentException("Use el constructor especifico para TRUNCAMIENTO o PLEGAMIENTO.");
        }
        this.n = tamaño;
        this.longClave = longClave;
        this.funcionHash = funcionHash;
        this.tipoColision = tipoColision;
        this.tabla = new String[n];
    }

    /**
     * Constructor para TRUNCAMIENTO.
     *
     * @param posicionesTrunc Posiciones (1-based) de la clave a extraer.
     */
    public TablaHash(int tamaño, int longClave, TipoColision tipoColision, int[] posicionesTrunc) {
        validarBase(tamaño, longClave);
        this.n = tamaño;
        this.longClave = longClave;
        this.funcionHash = FuncionHash.TRUNCAMIENTO;
        this.tipoColision = tipoColision;
        this.posicionesTrunc = posicionesTrunc;
        this.tabla = new String[n];
    }

    /**
     * Constructor para PLEGAMIENTO. Divide la clave en grupos de 2 caracteres.
     *
     * @param tipoPlegamiento SUMA o MULTIPLICACION entre grupos
     */
    public TablaHash(int tamaño, int longClave, TipoColision tipoColision, TipoPlegamiento tipoPlegamiento) {
        validarBase(tamaño, longClave);
        this.n = tamaño;
        this.longClave = longClave;
        this.funcionHash = FuncionHash.PLEGAMIENTO;
        this.tipoColision = tipoColision;
        this.tipoPlegamiento = tipoPlegamiento;
        this.tabla = new String[n];
    }

    private int calcularHash(String clave) {
        switch (funcionHash) {
            case MOD:
                return hashMod(clave);
            case CUADRADO:
                return hashCuadrado(clave);
            case TRUNCAMIENTO:
                return hashTruncamiento(clave);
            case PLEGAMIENTO:
                return hashPlegamiento(clave);
            default:
                throw new IllegalStateException("Funcion hash no reconocida.");
        }
    }

    /**
     * H(k) = (k mod n) + 1 Los dos dígitos menos significativos del resultado
     * son el residuo.
     */
    private int hashMod(String clave) {
        long k = ClaveUtil.aNumero(clave);
        return (int) (k % n) + 1;
    }

    /**
     * H(k) = (k² mod n) + 1
     */
    private int hashCuadrado(String clave) {
        long k = ClaveUtil.aNumero(clave) % 1_000_000L;
        long cuadrado = k * k;
        return (int) (cuadrado % n) + 1;
    }

    /**
     * Se trabaja sobre los dígitos del NÚMERO k obtenido de la clave. Se
     * extraen los dígitos en las posiciones indicadas.
     */
    private int hashTruncamiento(String clave) {
        String digitos = ClaveUtil.aDigitos(clave); // dígitos del número k
        StringBuilder extraido = new StringBuilder();

        for (int pos : posicionesTrunc) {
            if (pos >= 1 && pos <= digitos.length()) {
                extraido.append(digitos.charAt(pos - 1));
            }
            // Si la posición está fuera del rango, se omite silenciosamente
        }

        if (extraido.length() == 0) {
            throw new IllegalStateException(
                    "Ninguna posicion de truncamiento es valida para la clave '" + clave
                    + "'. Numero k tiene " + digitos.length() + " digito(s).");
        }

        long valor = Long.parseLong(extraido.toString());
        return (int) (valor % n) + 1;
    }

    /**
     * Plegamiento: divide la clave en grupos de 2 caracteres.
     *
     * Si la longitud de la clave es IMPAR: El grupo del centro tiene solo 1
     * carácter (se toma el de la izquierda).
     */
    private int hashPlegamiento(String clave) {
        String digitos = ClaveUtil.aDigitos(clave);
        int len = digitos.length();

        // Dividir en grupos de 2 digitos (de izquierda a derecha)
        ArrayList<Long> grupos = new ArrayList<>();
        for (int i = 0; i < len; i += 2) {
            int fin = Math.min(i + 2, len);
            grupos.add(Long.parseLong(digitos.substring(i, fin)));
        }

        // Combinar grupos
        long resultado;
        if (tipoPlegamiento == TipoPlegamiento.SUMA) {
            resultado = 0;
            for (long g : grupos) {
                resultado += g;
            }
        } else { // MULTIPLICACION
            resultado = 1;
            for (long g : grupos) {
                // Evitar multiplicar por 0 si un grupo es "00"
                resultado *= (g == 0 ? 1 : g);
            }
        }

        return (int) (resultado % n) + 1;
    }

    /**
     * Segunda función hash: H2(k) = 1 + (k mod (n-1)) Nunca retorna 0,
     * garantizando que siempre haya desplazamiento.
     */
    private int h2(String clave) {
        long k = ClaveUtil.aNumero(clave);
        int divisor = (n - 1 <= 0) ? 1 : n - 1;
        return 1 + (int) (k % divisor);
    }

    /**
     * Calcula la posicion real (0-based) para la i-esima prueba.
     *
     * i=0 -> posición base D i=1 -> primera colision i=2 -> segunda colision
     * ...
     *
     * El % n garantiza circularidad (si lllega al final)
     *
     * @param D Posicion base (1-based, resultado de la funcion hash)
     * @param i Numero de intento (0 = posicion base)
     * @param paso Paso para doble hash (H2)
     */
    private int calcularPosicion(int D, int i, int paso) {
        int desplazamiento;
        switch (tipoColision) {
            case LINEAL:
                desplazamiento = i;
                break;
            case CUADRATICA:
                desplazamiento = i * i;
                break;
            case DOBLE_HASH:
                desplazamiento = i * paso;
                break;
            default:
                desplazamiento = i;
        }
        // (D-1) convierte a 0-based, + desplazamiento, % n para circular
        return ((D - 1) + desplazamiento) % n;
    }

    /**
     * Inserta una clave en la tabla. Calcula la posicion base con la funcion
     * hash configurada. Si hay colision, aplica el método de resolucion
     * configurado.
     *
     * @throws IllegalStateException si la tabla está llena o la clave ya existe
     * @throws IllegalArgumentException si la clave tiene longitud incorrecta
     */
    public void insertar(String clave) {
        ClaveUtil.validar(clave, longClave);
        if (contador >= n) {
            throw new IllegalStateException("La tabla está llena. Capacidad maxima: " + n + ".");
        }
        if (buscar(clave) != -1) {
            throw new IllegalStateException("La clave '" + clave + "' ya existe en la tabla.");
        }

        int D = calcularHash(clave);
        int paso = (tipoColision == TipoColision.DOBLE_HASH) ? h2(clave) : 0;

        for (int i = 0; i < n; i++) {
            int pos = calcularPosicion(D, i, paso);
            if (tabla[pos] == null || tabla[pos].equals(ELIMINADO)) {
                tabla[pos] = clave;
                contador++;
                return;
            }
        }
        throw new IllegalStateException("No se encontro posicion libre.");
    }

    /**
     * Busca una clave siguiendo el MISMO camino que insertar. Si la clave tuvo
     * colision al insertar, la busqueda repite el mismo proceso de resolucion
     * para encontrarla.
     *
     * null -> cadena cortada, la clave no esta. ELIMINADO -> saltar y continuar
     * buscando.
     *
     * @return Índice (0-based) donde está, o -1 si no existe
     */
    public int buscar(String clave) {
        if (clave == null || clave.isEmpty()) {
            return -1;
        }

        int D = calcularHash(clave);
        int paso = (tipoColision == TipoColision.DOBLE_HASH) ? h2(clave) : 0;

        for (int i = 0; i < n; i++) {
            int pos = calcularPosicion(D, i, paso);
            if (tabla[pos] == null) {
                return -1;
            }
            if (tabla[pos].equals(ELIMINADO)) {
                continue;
            }
            if (tabla[pos].equals(clave)) {
                return pos;
            }
        }
        return -1;
    }

    /**
     * Elimina una clave de la tabla. Pone el marcador ELIMINADO (no null) para
     * no romper la cadena de búsqueda de otras claves que hayan tenido
     * colision.
     *
     * @throws IllegalArgumentException si la clave no existe
     */
    public void eliminar(String clave) {
        int pos = buscar(clave);
        if (pos == -1) {
            throw new IllegalArgumentException("La clave '" + clave + "' no existe en la tabla.");
        }
        tabla[pos] = ELIMINADO;
        contador--;
    }

    /**
     * Tabla completa para mostrar en front. null = vacio, ELIMINADO = marcador
     * de eliminado, otro = clave activa.
     */
    public String[] obtenerTabla() {
        return tabla.clone();
    }

    /**
     * Solo las claves activas (sin vacios ni eliminadas).
     */
    public ArrayList<String> obtenerClavesActivas() {
        ArrayList<String> lista = new ArrayList<>();
        for (String s : tabla) {
            if (s != null && !s.equals(ELIMINADO)) {
                lista.add(s);
            }
        }
        return lista;
    }

    /**
     * Retorna la posición base H(k) para una clave (1-based).
     */
    public int obtenerPosicionBase(String clave) {
        return calcularHash(clave);
    }

    public int getTamaño() {
        return n;
    }

    public int getLongClave() {
        return longClave;
    }

    public int getContador() {
        return contador;
    }

    public boolean estaLlena() {
        return contador >= n;
    }

    public FuncionHash getFuncionHash() {
        return funcionHash;
    }

    public TipoColision getTipoColision() {
        return tipoColision;
    }

    public String getMarcadorEliminado() {
        return ELIMINADO;
    }

    private void validarBase(int tamaño, int longClave) {
        if (tamaño <= 0) {
            throw new IllegalArgumentException("El tamaño debe ser mayor que 0.");
        }
        if (longClave <= 0) {
            throw new IllegalArgumentException("La longitud de clave debe ser mayor que 0.");
        }
    }
}
