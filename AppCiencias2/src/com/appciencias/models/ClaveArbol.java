package com.appciencias.models;

/**
 * Conversión de letras para los árboles (Digital, Trie, Múltiple).
 *
 * Cada letra -> posición en el alfabeto (a=1 ... z=26) -> binario de 5 bits.
 */
public class ClaveArbol {

    /**
     * Convierte una letra a su posición en el alfabeto (a=1 ... z=26).
     */
    public static int letraAPosicion(char c) {
        c = Character.toLowerCase(c);
        if (c < 'a' || c > 'z') {
            throw new IllegalArgumentException(
                    "Los arboles solo aceptan letras (a-z). Caracter inválido: '" + c + "'"
            );
        }
        return c - 'a' + 1;
    }

    /**
     * Convierte una letra a binario de 5 bits.
     */
    public static String letraA5Bits(char c) {
        int pos = letraAPosicion(c);
        return String.format("%5s", Integer.toBinaryString(pos)).replace(' ', '0');
    }

    /**
     * Convierte una letra (clave de 1 carácter) a su cadena binaria de 5 bits.
     * Valida que sea exactamente 1 letra.
     */
    public static String claveABinario(String clave) {
        validar(clave);
        return letraA5Bits(clave.charAt(0));
    }

    /**
     * Valida que la clave sea exactamente 1 letra (a-z / A-Z).
     */
    public static void validar(String clave) {
        if (clave == null || clave.isEmpty()) {
            throw new IllegalArgumentException("La clave no puede ser vacia.");
        }
        if (clave.length() != 1) {
            throw new IllegalArgumentException(
                    "Los arboles reciben una letra a la vez. Se recibio: '" + clave + "'"
            );
        }
        char c = clave.charAt(0);
        if (!Character.isLetter(c)) {
            throw new IllegalArgumentException(
                    "Los arboles solo aceptan letras (a-z). Caracter invalido: '" + c + "'"
            );
        }
    }

    /**
     * Retorna info de la letra para mostrar en pantalla. (Por si se necesita
     * mostrar en el front)
     */
    public static String obtenerInfo(String clave) {
        validar(clave);
        char c = clave.toLowerCase().charAt(0);
        int pos = letraAPosicion(c);
        String bits = letraA5Bits(c);
        return Character.toUpperCase(c) + " → posicion " + pos + " → " + bits;
    }
}
