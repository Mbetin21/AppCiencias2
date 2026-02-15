## Estructura de Paquetes

El proyecto está organizado usando una estructura de paquetes profesional:

```
src/
└── com/
    └── appciencias/
        ├── models/
        │   └── ArregloNumeros.java       (Estructura de datos)
        ├── algorithms/
        │   ├── BusquedaSecuencial.java   (Algoritmo O(n))
        │   ├── BusquedaBinaria.java      (Algoritmo O(log n))
        │   └── FuncionesHash.java        (Algoritmos de hashing)
        └── views/
            ├── MainWindow.java           (Ventana principal)
            ├── HomePanel.java            (Panel de inicio)
            └── README.md                 (Guía de interfaz)
```

### Paquetes

#### `com.appciencias.models`
Contiene las estructuras de datos principales del proyecto.

- **ArregloNumeros**: Clase que maneja un arreglo dinámico de números con métodos para:
  - `insertar(int numero)` - Inserta un número único
  - `ordenar()` - Ordena el arreglo
  - `eliminar(int numero)` - Elimina un número
  - `obtenerDatos()` - Retorna el ArrayList
  - `obtenerFormateado()` - Retorna números formateados con ceros

#### `com.appciencias.algorithms`
Contiene los algoritmos de búsqueda e hashing.

- **BusquedaSecuencial**: Búsqueda lineal → O(n)
  - `buscar(ArrayList<Integer> datos, int valor)`
  
- **BusquedaBinaria**: Búsqueda divide y conquista → O(log n)
  - `buscar(ArrayList<Integer> datos, int valor)` (requiere arreglo ordenado)
  
- **FuncionesHash**: Diversos métodos de hashing
  - `hashCuadrado(long clave, int n)` - Hash por elevación al cuadrado
  - `hashTruncamiento(String clave, int n, int[] posiciones)` - Hash por selección de dígitos
  - `hashPlegamiento(String clave, int n, int tamañoGrupo, boolean producto)` - Hash por división de grupos

#### `com.appciencias.views`
Contiene la interfaz gráfica de la aplicación.

- **MainWindow**: Ventana principal de la aplicación
  - Sistema de navegación con CardLayout
  - `showPanel(String panelName)` - Cambia entre paneles
  - `display()` - Muestra la ventana
  - `main(String[] args)` - Punto de entrada de la aplicación
  
- **HomePanel**: Panel de inicio
  - Presenta las opciones principales de navegación
  - Botones para "Búsquedas Internas" y "Búsquedas Externas"
  - Diseño con colores pastel sobrios

### Ejemplo de Uso

```java
import com.appciencias.models.ArregloNumeros;
import com.appciencias.algorithms.BusquedaSecuencial;
import com.appciencias.algorithms.BusquedaBinaria;

public class Main {
    public static void main(String[] args) {
        // Crear arreglo
        ArregloNumeros arreglo = new ArregloNumeros(10, 3);
        
        // Insertar datos
        arreglo.insertar(42);
        arreglo.insertar(15);
        arreglo.insertar(88);
        
        // Búsqueda secuencial
        int pos1 = BusquedaSecuencial.buscar(arreglo.obtenerDatos(), 15);
        System.out.println("Encontrado en posición: " + pos1);
        
        // Ordenar y búsqueda binaria
        arreglo.ordenar();
        int pos2 = BusquedaBinaria.buscar(arreglo.obtenerDatos(), 42);
        System.out.println("Encontrado en posición: " + pos2);
    }
}
```

### Compilación

Para compilar todos los archivos:
```bash
javac -d bin src/com/appciencias/models/ArregloNumeros.java \
              src/com/appciencias/algorithms/BusquedaSecuencial.java \
              src/com/appciencias/algorithms/BusquedaBinaria.java \
              src/com/appciencias/algorithms/FuncionesHash.java
```

O simplemente desde la raíz del proyecto:
```bash
javac -d bin src/com/appciencias/*/*.java
```

### Ejecutar la Interfaz Gráfica

Para compilar y ejecutar la aplicación con interfaz gráfica:

```bash
# Compilar todo el proyecto
javac -d bin src/com/appciencias/*/*.java

# Ejecutar la aplicación
java -cp bin com.appciencias.views.MainWindow
```

La aplicación abrirá una ventana con título "Ciencias de la Computación 2" donde podrás navegar entre diferentes paneles sin abrir nuevas ventanas.
