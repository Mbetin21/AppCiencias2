package com.appciencias.algorithms;

import com.appciencias.models.ClaveUtil;
import java.util.ArrayList;

/**
 * Solución de colisiones por arreglos anidados. 
 * Existe un arreglo principal. Cuando hay colisión en la posición X, se crea 
 * un arreglo nuevo completo (del mismo tamaño n) y el dato que colisionó va 
 * en la posición X de ese nuevo arreglo.
 * 
 * Ahora soporta cualquier función hash (MOD, CUADRADO, TRUNCAMIENTO, PLEGAMIENTO).
 */
public class ArreglosAnidados {

    private ArrayList<String[]> arreglos; // arreglos apilados (indice 0 = principal)
    private int n;
    private int longClave;
    private int contador;
    private FuncionHash funcionHashObj; // Función hash configurable

    /**
     * Constructor principal con función hash configurable.
     * Permite usar cualquier tipo de función hash.
     * 
     * @param tamaño Tamaño de cada arreglo
     * @param longClave Caracteres por clave
     * @param funcionHash Instancia de FuncionHash configurada
     */
    public ArreglosAnidados(int tamaño, int longClave, FuncionHash funcionHash) {
        if (tamaño <= 0) {
            throw new IllegalArgumentException("El tamaño debe ser mayor que 0.");
        }
        if (longClave <= 0) {
            throw new IllegalArgumentException("La longitud de clave debe ser mayor que 0.");
        }
        this.n = tamaño;
        this.longClave = longClave;
        this.funcionHashObj = funcionHash;
        this.contador = 0;
        this.arreglos = new ArrayList<>();
        arreglos.add(new String[n]); // arreglo principal
    }

    /**
     * Constructor con función hash por defecto (MOD) - retrocompatibilidad.
     * 
     * @param tamaño Tamaño de cada arreglo
     * @param longClave Caracteres por clave
     */
    public ArreglosAnidados(int tamaño, int longClave) {
        this(tamaño, longClave, new FuncionHash(FuncionHash.Tipo.MOD, tamaño));
    }

    /**
     * Calcula el hash usando la función configurada.
     * Retorna índice 0-based.
     */
    private int hash(String clave) {
        int posicion1Based = funcionHashObj.calcular(clave);
        return posicion1Based - 1; // Convertir a 0-based
    }

    /**
     * Inserta una clave. Busca el primer arreglo que tenga la posicion X libre.
     * Si todos la tienen ocupada, crea un arreglo nuevo.
     */
    public void insertar(String clave) {
        ClaveUtil.validar(clave, longClave);
        if (buscar(clave) != null) {
            throw new IllegalStateException("La clave '" + clave + "' ya existe.");
        }

        int pos = hash(clave);

        for (String[] arreglo : arreglos) {
            if (arreglo[pos] == null) {
                arreglo[pos] = clave;
                contador++;
                return;
            }
        }

        // Todos ocupados en esa posicion, nuevo arreglo
        String[] nuevo = new String[n];
        nuevo[pos] = clave;
        arreglos.add(nuevo);
        contador++;
    }

    /**
     * Busca revisando la posicion X en cada arreglo, de arriba hacia abajo. Se
     * detiene si encuentra una posicion vacia (la clave no puede estar mas
     * abajo).
     *
     * @return ResultadoBusqueda con número de arreglo y posicion, o null si no
     * existe
     */
    public ResultadoBusqueda buscar(String clave) {
        if (clave == null || clave.isEmpty()) {
            return null;
        }

        int pos = hash(clave);

        for (int i = 0; i < arreglos.size(); i++) {
            String[] arreglo = arreglos.get(i);
            if (arreglo[pos] == null) {
                return null; // vacio, no existe más abajo

            }
            if (arreglo[pos].equals(clave)) {
                return new ResultadoBusqueda(i, pos + 1);
            }
        }
        return null;
    }

    /**
     * Resultado de busqueda para mostrar en Frotn si la vez necesaria.
     */
    public static class ResultadoBusqueda {

        public final int numeroArreglo; // 0 = principal, 1,2,3... = desbordamientos
        public final int posicion;      // posicion 1-based

        public ResultadoBusqueda(int numeroArreglo, int posicion) {
            this.numeroArreglo = numeroArreglo;
            this.posicion = posicion;
        }

        @Override
        public String toString() {
            return "Encontrada en "
                    + (numeroArreglo == 0 ? "arreglo principal" : "arreglo de desbordamiento " + numeroArreglo)
                    + ", posición " + posicion;
        }
    }

    /**
     * Elimina una clave. Despues de eliminar, sube los datos de los arreglos
     * inferiores en esa posicion para no dejar huecos en la cadena de busqueda.
     */
    public void eliminar(String clave) {
        ClaveUtil.validar(clave, longClave);
        ResultadoBusqueda r = buscar(clave);
        if (r == null) {
            throw new IllegalArgumentException("La clave '" + clave + "' no existe.");
        }

        int pos = hash(clave);
        int desde = r.numeroArreglo;

        // Vaciar la posicion y subir todo lo de abajo
        for (int i = desde; i < arreglos.size() - 1; i++) {
            arreglos.get(i)[pos] = arreglos.get(i + 1)[pos];
        }
        arreglos.get(arreglos.size() - 1)[pos] = null;
        contador--;

        // Limpiar arreglos del final que quedaron completamente vacios
        while (arreglos.size() > 1) {
            String[] ultimo = arreglos.get(arreglos.size() - 1);
            boolean vacio = true;
            for (String s : ultimo) {
                if (s != null) {
                    vacio = false;
                    break;
                }
            }
            if (vacio) {
                arreglos.remove(arreglos.size() - 1);
            } else {
                break;
            }
        }
    }

    /**
     * Retorna todos los arreglos para que el front los muestre apilados. índice
     * 0 = arreglo principal, 1,2... = desbordamientos.
     */
    public ArrayList<String[]> obtenerArreglos() {
        ArrayList<String[]> copia = new ArrayList<>();
        for (String[] arr : arreglos) {
            copia.add(arr.clone());
        }
        return copia;
    }

    /**
     * Cuántos arreglos hay actualmente (1 = sin colisiones).
     */
    public int getNumeroArreglos() {
        return arreglos.size();
    }

    /**
     * Lista plana de todas las claves activas.
     */
    public ArrayList<String> obtenerClavesActivas() {
        ArrayList<String> lista = new ArrayList<>();
        for (String[] arr : arreglos) {
            for (String s : arr) {
                if (s != null) {
                    lista.add(s);
                }
            }
        }
        return lista;
    }

    /**
     * Posicion base H(k) para una clave (1-based).
     */
    public int obtenerPosicionBase(String clave) {
        return hash(clave) + 1;
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

    public FuncionHash getFuncionHash() {
        return funcionHashObj;
    }
}
