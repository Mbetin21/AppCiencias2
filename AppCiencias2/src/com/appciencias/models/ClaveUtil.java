package com.appciencias.models;

public class ClaveUtil {

    /**
     * Convierte una clave a su valor numérico k.
     *
     * @param clave Clave (solo números, solo letras, o alfanumerica)
     * @return Valor numérico k (siempre >= 0)
     */
    public static long aNumero(String clave) {
        if (clave == null || clave.isEmpty()) {
            throw new IllegalArgumentException("La clave no puede ser vacia.");
        }

        if (esNumerica(clave)) {
            // Clave numérica pura, usa el valor directamente
            return Long.parseLong(clave);
        } else {
            // Letras o alfanumerico, suma simple de valores ASCII
            long suma = 0;
            for (char c : clave.toCharArray()) {
                suma += (long) c;
            }
            return suma;
        }
    }

    /**
     * Convierte una clave a su representación numerica como cadena de digitos.
     * Usada por Truncamiento y Plegamiento.
     *
     * @param clave Clave alfanumérica
     * @return String con los dígitos del número equivalente
     */
    public static String aDigitos(String clave) {
        return String.valueOf(aNumero(clave));
    }

    /**
     * Valida que la clave tenga exactamente la longitud esperada.
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

    /**
     * Verifica si una cadena es completamente numérica (solo degitos).
     */
    public static boolean esNumerica(String clave) {
        if (clave == null || clave.isEmpty()) {
            return false;
        }
        for (char c : clave.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }
}
