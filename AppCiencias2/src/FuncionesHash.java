
public class FuncionesHash {

    /**
     * H(k) = (k mod n) + 1
     *
     * Returns: Un entero que representa la posición en la tabla.
     */
    private static int aplicarFormula(long valor, int n) {

        if (n <= 0) {
            throw new IllegalArgumentException("El tamaño del arreglo debe ser mayor que 0.");
        }

        return (int) (valor % n) + 1;
    }

    /**
     * H(k') = (k² mod n) + 1
     *
     * Returns: Un entero que representa la posición en la tabla.
     */
    public static int hashCuadrado(long clave, int n) {

        long cuadrado = clave * clave;

        return aplicarFormula(cuadrado, n);
    }

    /**
     * El usuario elige las posiciones específicas de los dígitos que desea
     * tomar de la clave.
     *
     * H(k) = (k mod n) + 1
     *
     * Returns: Un entero que representa la posición final en la tabla.
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
     * La clave se divide en grupos del tamaño indicado por el usuario. Luego
     * esos grupos pueden combinarse por: - Suma: Se suman los valores de cada
     * grupo. - Producto: Se multiplican los valores de cada grupo.
     *
     * H(k) = (k mod n) + 1
     *
     * Siempre se solicita si es por suma (false) o producto (true).
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
            }else {
                acumulado += grupo;
            }
        }

        return aplicarFormula(acumulado, n);
    }
}
