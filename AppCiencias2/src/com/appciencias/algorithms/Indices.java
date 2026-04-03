package com.appciencias.algorithms;

/**
 * Cálculo de índices para acceso a disco.
 *
 * Parámetros del usuario:
 *   r   = número de registros del archivo
 *   B   = tamaño del bloque en bytes
 *   R   = longitud del registro dato en bytes
 *   Ri  = longitud del registro índice en bytes (V + P)
 *
 * Calcula:
 *   bfr  = floor(B / R)         → registros dato por bloque
 *   b    = ceil(r / bfr)        → bloques para registros dato
 *   bfri = floor(B / Ri)        → registros índice por bloque (fanout)
 *
 *   ÍNDICE PRIMARIO:
 *     bi       = ceil(b / bfri)
 *     accesos  = ceil(log2(bi)) + 1
 *
 *   ÍNDICE SECUNDARIO:
 *     bi       = ceil(r / bfri)
 *     accesos  = ceil(log2(bi)) + 1
 *
 *   MULTINIVEL SOBRE PRIMARIO:
 *     niveles  = ceil(log_bfri(b))
 *     accesos  = niveles + 1
 *     bloquesPorNivel[] calculado iterativamente
 *
 *   MULTINIVEL SOBRE SECUNDARIO:
 *     niveles  = ceil(log_bfri(r))
 *     accesos  = niveles + 1
 *     bloquesPorNivel[] calculado iterativamente
 */
public class Indices {

    // ── Resultado completo ───────────────────────────────────────────────────

    public static class Resultado {

        // Parámetros ingresados
        public final long r;
        public final int  B;
        public final int  R;
        public final int  Ri;

        // Calculados base
        public final int  bfr;   // floor(B/R)
        public final long b;     // ceil(r/bfr)
        public final int  bfri;  // floor(B/Ri) = fanout

        // Índice Primario
        public final long bi_primario;
        public final int  accesos_primario;

        // Índice Secundario
        public final long bi_secundario;
        public final int  accesos_secundario;

        // Multinivel sobre Primario
        public final int    niveles_multi_primario;
        public final int    accesos_multi_primario;
        public final long[] bloquesPorNivel_primario; // índice 0 = nivel 1

        // Multinivel sobre Secundario
        public final int    niveles_multi_secundario;
        public final int    accesos_multi_secundario;
        public final long[] bloquesPorNivel_secundario;

        public Resultado(long r, int B, int R, int Ri,
                         int bfr, long b, int bfri,
                         long bi_primario, int accesos_primario,
                         long bi_secundario, int accesos_secundario,
                         int niveles_multi_primario, int accesos_multi_primario,
                         long[] bloquesPorNivel_primario,
                         int niveles_multi_secundario, int accesos_multi_secundario,
                         long[] bloquesPorNivel_secundario) {
            this.r = r;
            this.B = B;
            this.R = R;
            this.Ri = Ri;
            this.bfr = bfr;
            this.b = b;
            this.bfri = bfri;
            this.bi_primario = bi_primario;
            this.accesos_primario = accesos_primario;
            this.bi_secundario = bi_secundario;
            this.accesos_secundario = accesos_secundario;
            this.niveles_multi_primario = niveles_multi_primario;
            this.accesos_multi_primario = accesos_multi_primario;
            this.bloquesPorNivel_primario = bloquesPorNivel_primario;
            this.niveles_multi_secundario = niveles_multi_secundario;
            this.accesos_multi_secundario = accesos_multi_secundario;
            this.bloquesPorNivel_secundario = bloquesPorNivel_secundario;
        }
    }

    // ── Calcular ─────────────────────────────────────────────────────────────

    /**
     * Realiza todos los cálculos de índices con los parámetros dados.
     *
     * @param r  Número de registros del archivo.
     * @param B  Tamaño del bloque en bytes.
     * @param R  Longitud del registro dato en bytes.
     * @param Ri Longitud del registro índice en bytes.
     * @return   Resultado con todos los valores calculados.
     */
    public static Resultado calcular(long r, int B, int R, int Ri) {
        validar(r, B, R, Ri);

        // ── Base ─────────────────────────────────────────────────────────────
        int  bfr  = (int) Math.floor((double) B / R);
        long b    = (long) Math.ceil((double) r / bfr);
        int  bfri = (int) Math.floor((double) B / Ri);

        // ── Índice Primario ───────────────────────────────────────────────────
        long bi_p     = (long) Math.ceil((double) b / bfri);
        int  acc_p    = (int) Math.ceil(log2(bi_p)) + 1;

        // ── Índice Secundario ─────────────────────────────────────────────────
        long bi_s     = (long) Math.ceil((double) r / bfri);
        int  acc_s    = (int) Math.ceil(log2(bi_s)) + 1;

        // ── Multinivel sobre Primario ─────────────────────────────────────────
        long[] nivP   = calcularNiveles(b, bfri);
        int    niv_p  = nivP.length;
        int    acc_mp = niv_p + 1;

        // ── Multinivel sobre Secundario ───────────────────────────────────────
        long[] nivS   = calcularNiveles(r, bfri);
        int    niv_s  = nivS.length;
        int    acc_ms = niv_s + 1;

        return new Resultado(
                r, B, R, Ri,
                bfr, b, bfri,
                bi_p, acc_p,
                bi_s, acc_s,
                niv_p, acc_mp, nivP,
                niv_s, acc_ms, nivS);
    }

    /**
     * Calcula los bloques por nivel del multinivel iterativamente.
     * Nivel 1: ceil(base / bfri)
     * Nivel 2: ceil(nivel1 / bfri)
     * ... hasta que bi <= 1
     *
     * @param base  b para primario, r para secundario.
     * @param bfri  Fanout (registros índice por bloque).
     * @return Array con bloques por nivel (índice 0 = nivel 1).
     */
    private static long[] calcularNiveles(long base, int bfri) {
        java.util.ArrayList<Long> niveles = new java.util.ArrayList<>();
        long actual = (long) Math.ceil((double) base / bfri);
        niveles.add(actual);

        while (actual > 1) {
            actual = (long) Math.ceil((double) actual / bfri);
            niveles.add(actual);
            if (actual <= 1) break;
        }

        long[] result = new long[niveles.size()];
        for (int i = 0; i < niveles.size(); i++) {
            result[i] = niveles.get(i);
        }
        return result;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static double log2(long n) {
        return Math.log(n) / Math.log(2);
    }

    private static void validar(long r, int B, int R, int Ri) {
        if (r <= 0)  throw new IllegalArgumentException("r debe ser mayor que 0.");
        if (B <= 0)  throw new IllegalArgumentException("B debe ser mayor que 0.");
        if (R <= 0)  throw new IllegalArgumentException("R debe ser mayor que 0.");
        if (Ri <= 0) throw new IllegalArgumentException("Ri debe ser mayor que 0.");
        if (R >= B)  throw new IllegalArgumentException(
                "El registro dato (R=" + R + ") debe ser menor que el bloque (B=" + B + ").");
        if (Ri >= B) throw new IllegalArgumentException(
                "El registro índice (Ri=" + Ri + ") debe ser menor que el bloque (B=" + B + ").");
    }
}