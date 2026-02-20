package com.appciencias.algorithms;

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
        long k = ClaveUtil.aNumero(clave);
        long cuadrado = k * k;
        return (int) (cuadrado % n) + 1;
    }

    /**
     * Truncamiento: extrae los caracteres en las posiciones indicadas, suma sus
     * valores ASCII y aplica módulo.
     *
     * Si el total de posiciones es impar y hay un "centro exacto", el centro se
     * considera el de la izquierda del par central.
     */
    private int hashTruncamiento(String clave) {
        if (posicionesTrunc == null || posicionesTrunc.length == 0) {
            throw new IllegalStateException("No se definieron posiciones para truncamiento.");
        }
        long suma = 0;
        for (int pos : posicionesTrunc) {
            if (pos < 1 || pos > clave.length()) {
                throw new IllegalArgumentException(
                        "Posicion de truncamiento " + pos + " invalida para clave de " + clave.length() + " caracteres."
                );
            }
            suma += (long) clave.charAt(pos - 1); // ASCII del carácter
        }
        return (int) (suma % n) + 1;
    }

    /**
     * Plegamiento: divide la clave en grupos de 2 caracteres.
     *
     * Si la longitud de la clave es IMPAR: El grupo del centro tiene solo 1
     * carácter (se toma el de la izquierda). Ejemplo: "ABCDE" → grupos: ["AB",
     * "C", "DE"] (C es el centro izquierdo) se hace de esta forma para mejorar
     * la forma de agrupar y evitar grupos de 1 carácter al final, que podrian
     * generar más colisiones.
     */
    private int hashPlegamiento(String clave) {
        int len = clave.length();

        // Dividir en grupos de 2 (el central puede ser 1 si longitud impar)
        java.util.List<String> grupos = new java.util.ArrayList<>();
        if (len % 2 == 0) {
            // Par: grupos normales de 2
            for (int i = 0; i < len; i += 2) {
                grupos.add(clave.substring(i, i + 2));
            }
        } else {
            // Impar: mitad izquierda, carácter central, mitad derecha
            int centro = len / 2; // índice del carácter central
            // Grupos de la mitad izquierda
            for (int i = 0; i < centro; i += 2) {
                int fin = Math.min(i + 2, centro);
                grupos.add(clave.substring(i, fin));
            }
            // Carácter central (solo 1 — el de la izquierda del centro)
            grupos.add(String.valueOf(clave.charAt(centro)));
            // Grupos de la mitad derecha
            for (int i = centro + 1; i < len; i += 2) {
                int fin = Math.min(i + 2, len);
                grupos.add(clave.substring(i, fin));
            }
        }

        // Calcular valor ASCII de cada grupo
        long[] valoresGrupos = new long[grupos.size()];
        for (int i = 0; i < grupos.size(); i++) {
            long suma = 0;
            for (char c : grupos.get(i).toCharArray()) {
                suma += (long) c;
            }
            valoresGrupos[i] = suma;
        }

        // Combinar grupos: suma o multiplicacion
        long resultado;
        if (tipoPlegamiento == TipoPlegamiento.SUMA) {
            resultado = 0;
            for (long v : valoresGrupos) {
                resultado += v;
            }
        } else {
            resultado = 1;
            for (long v : valoresGrupos) {
                resultado *= v;
            }
        }

        // Solo digitos menos significativos
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
