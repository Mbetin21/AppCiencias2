package com.appciencias.algorithms;

import com.appciencias.models.ClaveUtil;
import java.util.ArrayList;

/**
 * Tabla Hash con direccionamiento abierto.
 * 
 * Funciones Hash disponibles (delegadas a FuncionHash):
 * - MOD: H(k) = (k mod n) + 1
 * - CUADRADO: H(k) = (k² mod n) + 1
 * - TRUNCAMIENTO: Extrae posiciones específicas
 * - PLEGAMIENTO: Divide en grupos y los combina
 *
 * Solución de colisiones:
 * - LINEAL: D' = D + i (donde i = 1, 2, 3...)
 * - CUADRATICA: D' = D + i² (i = 1, 2, 3...)
 * - DOBLE_HASH: D' = D + i * H2(k) con H2(k) = 1 + (k mod (n-1))
 */
public class TablaHash {

    // Enums mantenidos por retrocompatibilidad con código existente
    @Deprecated
    public enum FuncionHash {
        MOD, CUADRADO, TRUNCAMIENTO, PLEGAMIENTO
    }

    public enum TipoColision {
        LINEAL, CUADRATICA, DOBLE_HASH
    }

    @Deprecated
    public enum TipoPlegamiento {
        SUMA, MULTIPLICACION
    }

    // Marcador para posiciones eliminadas
    private static final String ELIMINADO = "__DELETED__";

    private String[] tabla;
    private int n;            // tamaño de la tabla
    private int longClave;    // caracteres por clave
    private int contador;     // elementos activos

    private com.appciencias.algorithms.FuncionHash funcionHashObj; // Nueva clase centralizada
    private TipoColision tipoColision;

    /**
     * Constructor principal que acepta una instancia de FuncionHash.
     * Permite cualquier tipo de función hash configurada externamente.
     */
    public TablaHash(int tamaño, int longClave, com.appciencias.algorithms.FuncionHash funcionHash, TipoColision tipoColision) {
        validarBase(tamaño, longClave);
        this.n = tamaño;
        this.longClave = longClave;
        this.funcionHashObj = funcionHash;
        this.tipoColision = tipoColision;
        this.tabla = new String[n];
    }

    /**
     * Constructor para MOD o CUADRADO (retrocompatibilidad).
     * @deprecated Use el constructor con FuncionHash object
     */
    @Deprecated
    public TablaHash(int tamaño, int longClave, FuncionHash funcionHash, TipoColision tipoColision) {
        validarBase(tamaño, longClave);
        if (funcionHash == FuncionHash.TRUNCAMIENTO || funcionHash == FuncionHash.PLEGAMIENTO) {
            throw new IllegalArgumentException("Use el constructor especifico para TRUNCAMIENTO o PLEGAMIENTO.");
        }
        this.n = tamaño;
        this.longClave = longClave;
        // Convertir enum antiguo a nueva clase FuncionHash
        com.appciencias.algorithms.FuncionHash.Tipo tipo = 
            (funcionHash == FuncionHash.MOD) ? com.appciencias.algorithms.FuncionHash.Tipo.MOD 
                                             : com.appciencias.algorithms.FuncionHash.Tipo.CUADRADO;
        this.funcionHashObj = new com.appciencias.algorithms.FuncionHash(tipo, tamaño);
        this.tipoColision = tipoColision;
        this.tabla = new String[n];
    }

    /**
     * Constructor para TRUNCAMIENTO (retrocompatibilidad).
     *
     * @param posicionesTrunc Posiciones (1-based) de la clave a extraer.
     * @deprecated Use el constructor con FuncionHash object
     */
    @Deprecated
    public TablaHash(int tamaño, int longClave, TipoColision tipoColision, int[] posicionesTrunc) {
        validarBase(tamaño, longClave);
        this.n = tamaño;
        this.longClave = longClave;
        this.funcionHashObj = new com.appciencias.algorithms.FuncionHash(tamaño, posicionesTrunc);
        this.tipoColision = tipoColision;
        this.tabla = new String[n];
    }

    /**
     * Constructor para PLEGAMIENTO. Divide la clave en grupos de 2 caracteres (retrocompatibilidad).
     *
     * @param tipoPlegamiento SUMA o MULTIPLICACION entre grupos
     * @deprecated Use el constructor con FuncionHash object
     */
    @Deprecated
    public TablaHash(int tamaño, int longClave, TipoColision tipoColision, TipoPlegamiento tipoPlegamiento) {
        validarBase(tamaño, longClave);
        this.n = tamaño;
        this.longClave = longClave;
        // Convertir enum antiguo a nuevo
        com.appciencias.algorithms.FuncionHash.TipoPlegamiento tipo = 
            (tipoPlegamiento == TipoPlegamiento.SUMA) ? com.appciencias.algorithms.FuncionHash.TipoPlegamiento.SUMA
                                                       : com.appciencias.algorithms.FuncionHash.TipoPlegamiento.MULTIPLICACION;
        this.funcionHashObj = new com.appciencias.algorithms.FuncionHash(tamaño, tipo);
        this.tipoColision = tipoColision;
        this.tabla = new String[n];
    }

    /**
     * Calcula el hash de una clave usando la función hash configurada.
     */
    private int calcularHash(String clave) {
        return funcionHashObj.calcular(clave);
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

    public com.appciencias.algorithms.FuncionHash getFuncionHashObj() {
        return funcionHashObj;
    }

    /**
     * @deprecated Use getFuncionHashObj()
     */
    @Deprecated
    public FuncionHash getFuncionHash() {
        // Intentar mantener compatibilidad
        com.appciencias.algorithms.FuncionHash.Tipo tipo = funcionHashObj.getTipo();
        switch (tipo) {
            case MOD: return FuncionHash.MOD;
            case CUADRADO: return FuncionHash.CUADRADO;
            case TRUNCAMIENTO: return FuncionHash.TRUNCAMIENTO;
            case PLEGAMIENTO: return FuncionHash.PLEGAMIENTO;
            default: return FuncionHash.MOD;
        }
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
