package com.appciencias.algorithms;

import com.appciencias.models.ClaveUtil;
import java.util.ArrayList;

/**
 * DInamicas (expansion y reduccion partiales y totaes)
 *
 * Colisiones -> fila extra en la misma cubeta. Las primeras posiciones son
 * registros normales, las siguientes son colisiones (Colisiones 1, 2, ...).
 *
 * DO expansion = registros ocupados / (numCubetas × regPorCubeta) Si DO > 75%
 * despues de insertar → expandir y reorganizar.
 *
 * DO reduccion = registros ocupados / numCubetas Si DO < 75% despues de
 * eliminar → reducir y reorganizar.
 *
 * Expansion total : numCubetas × 2 Expansion parcial : ceil(numCubetas × 1.5)
 * Reduccion total : numCubetas / 2 Reduccion parcial : floor(numCubetas / 1.5)
 */
public class Dinamicas {

    public enum Tipo {
        TOTAL, PARCIAL
    }

    public static class UltimoEvento {

        public final String tipo;           // "EXPANSION" o "REDUCCION"
        public final int cubetasAntes;
        public final int cubetasDespues;
        public final String claveDetonante;
        public final double doQueDetono;    // DO que supero el umbral

        public UltimoEvento(String tipo, int cubetasAntes, int cubetasDespues,
                String claveDetonante, double doQueDetono) {
            this.tipo = tipo;
            this.cubetasAntes = cubetasAntes;
            this.cubetasDespues = cubetasDespues;
            this.claveDetonante = claveDetonante;
            this.doQueDetono = doQueDetono;
        }

        @Override
        public String toString() {
            return tipo + ": " + cubetasAntes + " cubetas → " + cubetasDespues
                    + " cubetas (clave '" + claveDetonante + "', DO="
                    + String.format("%.2f%%", doQueDetono * 100) + ")";
        }
    }

    private int numCubetas;
    private final int regPorCubeta;
    private final int longClave;
    private final Tipo tipo;

    /**
     * cubetas.get(i) = lista de claves en la cubeta i. Las primeras
     * regPorCubeta son registros normales, las siguientes son colisiones.
     */
    private ArrayList<ArrayList<String>> cubetas;

    /**
     * Orden en que fueron ingresados los datos (para reorganizar).
     */
    private ArrayList<String> historialIngreso;

    /**
     * Ultimo evento de expansion o reduccion. null si no hubo ninguno aun.
     */
    private UltimoEvento ultimoEvento;

    private int contador;

    private static final double UMBRAL_EXPANSION = 0.75;
    private static final double UMBRAL_REDUCCION = 0.75;

    /**
     * @param numCubetas Numero inicial de cubetas.
     * @param regPorCubeta Registros por cubeta (capacidad normal).
     * @param longClave Caracteres por clave.
     * @param tipo TOTAL o PARCIAL.
     */
    public Dinamicas(int numCubetas, int regPorCubeta,
            int longClave, Tipo tipo) {
        if (numCubetas <= 0) {
            throw new IllegalArgumentException("Numero deCubetas debe ser mayor que 0.");
        }
        if (regPorCubeta <= 0) {
            throw new IllegalArgumentException("Registros por cubeta debe ser mayor que 0.");
        }
        if (longClave <= 0) {
            throw new IllegalArgumentException("Longitud de clave debe ser mayor que 0.");
        }

        this.numCubetas = numCubetas;
        this.regPorCubeta = regPorCubeta;
        this.longClave = longClave;
        this.tipo = tipo;
        this.contador = 0;
        this.historialIngreso = new ArrayList<>();
        this.ultimoEvento = null;
        this.cubetas = crearCubetasVacias(numCubetas);
    }

    /**
     * Inserta una clave en la cubeta H(k). Si la cubeta ya tiene regPorCubeta
     * registros, la clave va como colision (fila extra en la misma cubeta).
     * Despues de insertar verifica DO y expande si supera el umbral.
     *
     * @throws IllegalStateException si la clave ya existe.
     * @throws IllegalArgumentException si la longitud de clave es incorrecta.
     */
    public void insertar(String clave) {
        ClaveUtil.validar(clave, longClave);

        if (buscar(clave) != -1) {
            throw new IllegalStateException("La clave '" + clave + "' ya existe.");
        }

        int idx = hash(clave);
        cubetas.get(idx).add(clave);
        historialIngreso.add(clave);
        contador++;

        // Verificar DO de expansion
        double do_ = calcularDOExpansion();
        if (do_ > UMBRAL_EXPANSION) {
            expandir(clave, do_);
        }
    }

    /**
     * Busca una clave en su cubeta H(k).
     *
     * @return indice de cubeta (0-based) donde esta, o -1 si no existe.
     */
    public int buscar(String clave) {
        if (clave == null || clave.isEmpty()) {
            return -1;
        }

        int idx = hash(clave);
        for (String c : cubetas.get(idx)) {
            if (c.equals(clave)) {
                return idx;
            }
        }
        return -1;
    }

    /**
     * Elimina una clave de su cubeta. Despues de eliminar verifica DO y reduce
     * si baja del umbral.
     *
     * @throws IllegalArgumentException si la clave no existe.
     */
    public void eliminar(String clave) {
        ClaveUtil.validar(clave, longClave);

        int idx = buscar(clave);
        if (idx == -1) {
            throw new IllegalArgumentException("La clave '" + clave + "' no existe.");
        }

        cubetas.get(idx).remove(clave);
        historialIngreso.remove(clave);
        contador--;

        // Verificar DO de reduccion
        if (numCubetas > 1) {
            double do_ = calcularDOReduccion();
            if (do_ < UMBRAL_REDUCCION) {
                reducir(clave, do_);
            }
        }
    }

    private void expandir(String claveDetonante, double doAntes) {
        int cubetasAntes = numCubetas;

        if (tipo == Tipo.TOTAL) {
            numCubetas = numCubetas * 2;
        } else {
            numCubetas = (int) Math.ceil(numCubetas * 1.5);
        }

        reorganizar();

        ultimoEvento = new UltimoEvento(
                "EXPANSION", cubetasAntes, numCubetas, claveDetonante, doAntes);
    }

    private void reducir(String claveDetonante, double doAntes) {
        int cubetasAntes = numCubetas;

        if (tipo == Tipo.TOTAL) {
            numCubetas = numCubetas / 2;
        } else {
            numCubetas = (int) Math.floor(numCubetas / 1.5);
        }

        if (numCubetas < 1) {
            numCubetas = 1;
        }

        reorganizar();

        ultimoEvento = new UltimoEvento(
                "REDUCCION", cubetasAntes, numCubetas, claveDetonante, doAntes);
    }

    /**
     * Reorganizatodos los datos en el nuevo numero de cubetas, respetando el
     * orden original de ingreso.
     */
    private void reorganizar() {
        cubetas = crearCubetasVacias(numCubetas);
        for (String c : historialIngreso) {
            int idx = hash(c);
            cubetas.get(idx).add(c);
        }
    }

    /**
     * H(k) = k mod numCubetas (0-based).
     */
    private int hash(String clave) {
        long k = ClaveUtil.aNumero(clave);
        return (int) (k % numCubetas);
    }

    /**
     * DO expansion = registros ocupados / (numCubetas × regPorCubeta).
     */
    public double calcularDOExpansion() {
        return (double) contador / ((double) numCubetas * regPorCubeta);
    }

    /**
     * DO reduccion = registros ocupados / numCubetas.
     */
    public double calcularDOReduccion() {
        if (numCubetas == 0) {
            return 0;
        }
        return (double) contador / numCubetas;
    }

    private ArrayList<ArrayList<String>> crearCubetasVacias(int n) {
        ArrayList<ArrayList<String>> lista = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            lista.add(new ArrayList<>());
        }
        return lista;
    }

    /**
     * Estructura actual. Las primeras regPorCubeta son normales, las extra son
     * colisiones.
     */
    public ArrayList<ArrayList<String>> obtenerEstructura() {
        ArrayList<ArrayList<String>> copia = new ArrayList<>();
        for (ArrayList<String> cubeta : cubetas) {
            copia.add(new ArrayList<>(cubeta));
        }
        return copia;
    }

    /**
     * Cuantas filas de colision tiene una cubeta. Si tiene mas de regPorCubeta
     * elementos, las extra son colisiones.
     */
    public int obtenerFilasColision(int indiceCubeta) {
        int total = cubetas.get(indiceCubeta).size();
        return Math.max(0, total - regPorCubeta);
    }

    /**
     * Numero de cubeta (0-based) donde hashea una clave.
     */
    public int obtenerCubetaBase(String clave) {
        return hash(clave);
    }

    /**
     * ultimo evento de expansion o reduccion. null si no hubo ninguno.
     */
    public UltimoEvento getUltimoEvento() {
        return ultimoEvento;
    }

    /**
     * true si el ultimo insertar o eliminar disparo una reorganizacion.
     */
    public boolean huboReorganizacion() {
        return ultimoEvento != null;
    }

    public void limpiarUltimoEvento() {
        ultimoEvento = null;
    }

    /**
     * Resumen
     */
    public String obtenerInfo() {
        return "Cubetas=" + numCubetas
                + "  Reg/cubeta=" + regPorCubeta
                + "  Registros=" + contador
                + "  DO exp=" + String.format("%.2f%%", calcularDOExpansion() * 100)
                + "  DO red=" + String.format("%.2f%%", calcularDOReduccion() * 100);
    }

    public int getNumCubetas() {
        return numCubetas;
    }

    public int getRegPorCubeta() {
        return regPorCubeta;
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

    public ArrayList<String> getHistorialIngreso() {
        return new ArrayList<>(historialIngreso);
    }
}
