package com.appciencias.algorithms;

/**
 * Utilidad para convertir claves alfanuméricas a número. Usada para: Hash,
 * Secuencial, Binario.
 *
 * Suma ponderada de valores ASCII de cada carácter. Ejemplo: "AB3" -> (65 *
 * 1000²) + (66 * 1000¹) + (51 * 1000⁰)
 */
public class ClaveUtil {

    /**
     * Convierte una clave alfanumérica a long usando valores ASCII.
     *
     * @param clave Clave (letras, números o mezcla)
     * @return Valor numérico largo equivalente
     */
    public static long aNumero(String clave) {
        if (clave == null || clave.isEmpty()) {
            throw new IllegalArgumentException("La clave no puede ser vacia.");
        }
        long resultado = 0;
        for (char c : clave.toCharArray()) {
            resultado = resultado * 1000L + (long) c;
        }
        return resultado;
    }

    /**
     * Valida longitud de la clave.
     *
     * @param clave Clave a validar
     * @param longitud Longitud esperada
     */
    public static void validar(String clave, int longitud) {
        if (clave == null || clave.isEmpty()) {
            throw new IllegalArgumentException("La clave no puede ser vacia.");
        }
        if (clave.length() != longitud) {
            throw new IllegalArgumentException(
                    "La clave '" + clave + "' debe tener exactamente " + longitud
                    + " caracter(es). Tiene " + clave.length() + "."
            );
        }
    }
}
