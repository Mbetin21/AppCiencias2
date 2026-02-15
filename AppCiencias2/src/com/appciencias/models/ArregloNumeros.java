package com.appciencias.models;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Clase para manejar el arreglo de números, con métodos para insertar, ordenar,
 * eliminar y obtener los datos, respetando las restricciones de tamaño y
 * dígitos
 */
public class ArregloNumeros {

    private ArrayList<Integer> datos;
    private int tamaño;
    private int digitos;

    public ArregloNumeros(int tamaño, int digitos) {
        this.tamaño = tamaño; // Tamaño máximo del arreglo, indicado por el usuario
        this.digitos = digitos; // Cantidad de dígitos, indicado por el usuario
        this.datos = new ArrayList<>();
    }

    /**
     * Inserta un número si: - hay espacio - no está repetido
     */
    public void insertar(int numero) {
        if (datos.size() >= tamaño) {
            throw new RuntimeException("Arreglo lleno");
        }
        if (datos.contains(numero)) {
            throw new RuntimeException("No se permiten repetidos");
        }
        datos.add(numero);
    }

    /**
     * Ordena el arreglo (deberia ser cuando se presione el botón guardar)
     */
    public void ordenar() {
        Collections.sort(datos);
    }

    /**
     * Elimina un número del arreglo y corre todo el arreglo cerrando el espacio
     * dejado por el número eliminado
     */
    public void eliminar(int numero) {
        datos.remove(Integer.valueOf(numero));
    }

    /**
     * Devuelve el arreglo (para algoritmos)
     */
    public ArrayList<Integer> obtenerDatos() {
        return datos;
    }

    /**
     * Devuelve el arreglo modificado añadiendo los 0 a la izquierda según la
     * cantidad de dígitos indicada
     */
    public ArrayList<String> obtenerFormateado() {
        ArrayList<String> salida = new ArrayList<>();
        for (int n : datos) {
            salida.add(String.format("%0" + digitos + "d", n));
        }
        return salida;
    }
    
    public int getDigitos() {
        return digitos;
    }
    
    public int getTamaño() {
        return tamaño;
    }
}
