package com.appciencias.algorithms;

/**
 * Clase que implementa diversos algoritmos de funciones hash para 
 * búsquedas en tablas hash. Incluye métodos para:
 * - Hash lineal (módulo)
 * - Hash cuadrado
 * - Hash por truncamiento
 * - Hash por plegamiento
 */
public class FuncionesHash {

    /**
     * Aplica la fórmula base: H(k) = (k mod n) + 1
     * 
     * @param valor Valor a transformar
     * @param n Tamaño de la tabla
     * @return Posición en la tabla
     * @throws IllegalArgumentException si n <= 0
     */
    private static int aplicarFormula(long valor, int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("El tamaño del arreglo debe ser mayor que 0.");
        }
        return (int) (valor % n) + 1;
    }

    /**
     * Hash cuadrado: H(k') = (k² mod n) + 1
     * 
     * @param clave Clave a hashear
     * @param n Tamaño de la tabla
     * @return Posición en la tabla
     */
    public static int hashCuadrado(long clave, int n) {
        long cuadrado = clave * clave;
        return aplicarFormula(cuadrado, n);
    }

    /**
     * Hash por truncamiento: El usuario elige posiciones específicas de los 
     * dígitos que desea tomar de la clave.
     * 
     * @param clave Clave como String
     * @param n Tamaño de la tabla
     * @param posiciones Array de posiciones (1-based) a extraer
     * @return Posición en la tabla
     * @throws IllegalArgumentException si la clave es vacía o posiciones inválidas
     */
    public static int hashTruncamiento(String clave, int n, int[] posiciones) {
        if (clave == null || clave.isEmpty()) {
            throw new IllegalArgumentException("Clave vacia.");
        }

        StringBuilder sb = new StringBuilder();

        for (int pos : posiciones) {
            if (pos < 1 || pos > clave.length()) {
                throw new IllegalArgumentException("Posicion invalida: " + pos);
            }
            sb.append(clave.charAt(pos - 1));
        }

        long valor = Long.parseLong(sb.toString());
        return aplicarFormula(valor, n);
    }

    /**
     * Hash por plegamiento: La clave se divide en grupos del tamaño indicado.
     * Luego esos grupos pueden combinarse por suma o producto.
     * 
     * @param clave Clave como String
     * @param n Tamaño de la tabla
     * @param tamañoGrupo Tamaño de cada grupo de dígitos
     * @param producto true para multiplicar grupos, false para sumarlos
     * @return Posición en la tabla
     * @throws IllegalArgumentException si la clave es vacía o tamañoGrupo <= 0
     */
    public static int hashPlegamiento(String clave, int n, int tamañoGrupo, boolean producto) {
        if (clave == null || clave.isEmpty()) {
            throw new IllegalArgumentException("Clave vacia.");
        }

        if (tamañoGrupo <= 0) {
            throw new IllegalArgumentException("El tamaño del grupo debe ser mayor que 0.");
        }

        long acumulado = producto ? 1 : 0;

        for (int i = 0; i < clave.length(); i += tamañoGrupo) {
            int fin = Math.min(i + tamañoGrupo, clave.length());
            long grupo = Long.parseLong(clave.substring(i, fin));

            if (producto) {
                acumulado *= grupo; 
            } else {
                acumulado += grupo;
            }
        }

        return aplicarFormula(acumulado, n);
    }
}
