package com.appciencias.algorithms;

import com.appciencias.models.ClaveUtil;
import java.util.ArrayList;

/**
 * Clase centralizada para todas las funciones hash.
 * Permite que cualquier estructura (TablaHash, ListasEnlazadas, ArreglosAnidados)
 * use cualquier función hash disponible.
 * 
 * Funciones disponibles:
 * - MOD: H(k) = (k mod n) + 1
 * - CUADRADO: H(k) = (k² mod n) + 1
 * - TRUNCAMIENTO: Extrae posiciones específicas de los dígitos
 * - PLEGAMIENTO: Divide en grupos de 2 dígitos y los combina
 */
public class FuncionHash {

    public enum Tipo {
        MOD, CUADRADO, TRUNCAMIENTO, PLEGAMIENTO
    }

    public enum TipoPlegamiento {
        SUMA, MULTIPLICACION
    }

    private Tipo tipo;
    private int n;  // tamaño de la tabla
    
    // Para TRUNCAMIENTO
    private int[] posicionesTrunc;
    
    // Para PLEGAMIENTO
    private TipoPlegamiento tipoPlegamiento;

    /**
     * Constructor para MOD o CUADRADO.
     */
    public FuncionHash(Tipo tipo, int tamaño) {
        if (tipo == Tipo.TRUNCAMIENTO || tipo == Tipo.PLEGAMIENTO) {
            throw new IllegalArgumentException("Use el constructor específico para TRUNCAMIENTO o PLEGAMIENTO.");
        }
        this.tipo = tipo;
        this.n = tamaño;
    }

    /**
     * Constructor para TRUNCAMIENTO.
     * 
     * @param tamaño Tamaño de la tabla
     * @param posicionesTrunc Posiciones (1-based) de la clave a extraer
     */
    public FuncionHash(int tamaño, int[] posicionesTrunc) {
        this.tipo = Tipo.TRUNCAMIENTO;
        this.n = tamaño;
        this.posicionesTrunc = posicionesTrunc;
    }

    /**
     * Constructor para PLEGAMIENTO.
     * 
     * @param tamaño Tamaño de la tabla
     * @param tipoPlegamiento SUMA o MULTIPLICACION entre grupos
     */
    public FuncionHash(int tamaño, TipoPlegamiento tipoPlegamiento) {
        this.tipo = Tipo.PLEGAMIENTO;
        this.n = tamaño;
        this.tipoPlegamiento = tipoPlegamiento;
    }

    /**
     * Calcula el hash de una clave según la función configurada.
     * 
     * @param clave Clave a hashear
     * @return Posición 1-based en la tabla
     */
    public int calcular(String clave) {
        switch (tipo) {
            case MOD:
                return hashMod(clave);
            case CUADRADO:
                return hashCuadrado(clave);
            case TRUNCAMIENTO:
                return hashTruncamiento(clave);
            case PLEGAMIENTO:
                return hashPlegamiento(clave);
            default:
                throw new IllegalStateException("Función hash no reconocida.");
        }
    }

    /**
     * H(k) = (k mod n) + 1
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
     * Extrae dígitos en posiciones específicas del número k.
     * Se trabaja sobre los dígitos del NÚMERO k obtenido de la clave.
     */
    private int hashTruncamiento(String clave) {
        String digitos = ClaveUtil.aDigitos(clave);
        StringBuilder extraido = new StringBuilder();

        for (int pos : posicionesTrunc) {
            if (pos >= 1 && pos <= digitos.length()) {
                extraido.append(digitos.charAt(pos - 1));
            }
        }

        if (extraido.length() == 0) {
            throw new IllegalStateException(
                    "Ninguna posición de truncamiento es válida para la clave '" + clave
                    + "'. Número k tiene " + digitos.length() + " dígito(s).");
        }

        long valor = Long.parseLong(extraido.toString());
        return (int) (valor % n) + 1;
    }

    /**
     * Plegamiento: divide la clave en grupos de 2 caracteres.
     * Si la longitud es impar, el último grupo tiene solo 1 carácter.
     */
    private int hashPlegamiento(String clave) {
        String digitos = ClaveUtil.aDigitos(clave);
        int len = digitos.length();

        // Dividir en grupos de 2 dígitos
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

    // Getters
    public Tipo getTipo() {
        return tipo;
    }

    public int getTamaño() {
        return n;
    }

    public int[] getPosicionesTrunc() {
        return posicionesTrunc;
    }

    public TipoPlegamiento getTipoPlegamiento() {
        return tipoPlegamiento;
    }

    /**
     * Actualiza el tamaño de la tabla (útil si se redimensiona).
     */
    public void setTamaño(int nuevoTamaño) {
        this.n = nuevoTamaño;
    }
}
