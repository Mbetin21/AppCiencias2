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

    // ─── Métodos ASCII (para Árbol Digital) ─────────────────────────

    /**
     * Valida que la clave sea exactamente 1 carácter ASCII imprimible (32-126).
     */
    public static void validarASCII(String clave) {
        if (clave == null || clave.isEmpty()) {
            throw new IllegalArgumentException("La clave no puede ser vacia.");
        }
        if (clave.length() != 1) {
            throw new IllegalArgumentException(
                    "Se recibe un caracter a la vez. Se recibio: '" + clave + "'"
            );
        }
        char c = clave.charAt(0);
        if (c < 32 || c > 126) {
            throw new IllegalArgumentException(
                    "Caracter fuera del rango ASCII imprimible (32-126): '" + c + "'"
            );
        }
    }

    /**
     * Convierte un carácter ASCII a binario de 7 bits.
     */
    public static String charA8Bits(char c) {
        return String.format("%7s", Integer.toBinaryString((int) c)).replace(' ', '0');
    }

    /**
     * Convierte una clave de 1 carácter a su cadena binaria de 7 bits (ASCII).
     */
    public static String claveABinarioASCII(String clave) {
        validarASCII(clave);
        return charA8Bits(clave.charAt(0));
    }

    /**
     * Retorna info ASCII de la clave para mostrar en pantalla.
     */
    public static String obtenerInfoASCII(String clave) {
        validarASCII(clave);
        char c = clave.charAt(0);
        int ascii = (int) c;
        String bits = charA8Bits(c);
        return "'" + c + "' → ASCII " + ascii + " → " + bits;
    }

    // ─── Métodos 8 bits reales (para Residuos Múltiples) ─────────────

    /**
     * Valida que la clave sea exactamente 1 carácter con valor 0-255.
     */
    public static void validar8Bits(String clave) {
        if (clave == null || clave.isEmpty()) {
            throw new IllegalArgumentException("La clave no puede ser vacía.");
        }
        if (clave.length() != 1) {
            throw new IllegalArgumentException(
                    "Se recibe un carácter a la vez. Se recibió: '" + clave + "'"
            );
        }
        char c = clave.charAt(0);
        if (c > 255) {
            throw new IllegalArgumentException(
                    "Carácter fuera del rango (0-255): código " + (int) c
            );
        }
    }

    /**
     * Convierte un carácter a binario de 8 bits reales.
     */
    public static String charABinario8Bits(char c) {
        return String.format("%8s", Integer.toBinaryString((int) c)).replace(' ', '0');
    }

    /**
     * Convierte una clave de 1 carácter a su cadena binaria de 8 bits.
     */
    public static String claveABinario8Bits(String clave) {
        validar8Bits(clave);
        return charABinario8Bits(clave.charAt(0));
    }

    /**
     * Retorna info de la clave con 8 bits para mostrar en pantalla.
     */
    public static String obtenerInfo8Bits(String clave) {
        validar8Bits(clave);
        char c = clave.charAt(0);
        int ascii = (int) c;
        String bits = charABinario8Bits(c);
        return "'" + c + "' → ASCII " + ascii + " → " + bits;
    }
}
