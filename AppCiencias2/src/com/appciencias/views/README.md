# Paquete Views - Interfaz Gráfica

## Estructura Actual

```
views/
├── MainWindow.java          - Ventana principal con sistema de navegación
├── HomePanel.java           - Panel inicial con opciones principales
└── README.md               - Este archivo
```

## Arquitectura

### Sistema de Navegación
La aplicación utiliza un **CardLayout** para gestionar múltiples paneles en una sola ventana:
- Los paneles se muestran/ocultan sin abrir nuevas ventanas
- Navegación fluida entre secciones
- Fácil expansión agregando nuevos paneles

### MainWindow
Ventana principal que:
- Gestiona todos los paneles de la aplicación
- Proporciona método `showPanel(String)` para navegación
- Mantiene referencias a todos los paneles
- Título: "Ciencias de la Computación 2"

### HomePanel
Panel de inicio que presenta:
- Título y descripción de la aplicación
- Botones para "Búsquedas Internas" y "Búsquedas Externas"
- Diseño intuitivo y profesional

## Guía de Estilo

### Colores
- **Fondo principal**: `#F5F5FA` (gris muy claro)
- **Fondo de botones**: `#E6E6F0` (gris pastel)
- **Bordes**: `#C8C8D2` (gris medio)
- **Texto principal**: `#3C3C46` (gris oscuro)
- **Texto secundario**: `#646469` (gris medio)
- **Hover**: `#DCDCEB` (gris ligeramente más oscuro)

### Tipografía
- **Fuente**: Segoe UI (estándar de Windows)
- **Títulos principales**: 32px, Bold
- **Subtítulos**: 20px, Bold
- **Texto normal**: 14-16px, Regular
- **Texto pequeño**: 12px, Italic

### Espaciado
- Márgenes externos: 40-60px
- Espaciado entre componentes: 15-20px
- Padding en botones: 25-30px

## Cómo Agregar Nuevos Paneles

### 1. Crear la Clase del Panel
```java
package com.appciencias.views;

import javax.swing.*;
import java.awt.*;

public class NuevoPanelPanel extends JPanel {
    private MainWindow mainWindow;
    
    public NuevoPanelPanel(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        initializePanel();
    }
    
    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 250));
        
        // Agregar componentes...
        
        // Botón para volver (opcional)
        JButton backButton = new JButton("Volver");
        backButton.addActionListener(e -> mainWindow.showPanel("HOME"));
    }
}
```

### 2. Registrar en MainWindow
En el método `initializePanels()`:
```java
NuevoPanelPanel nuevoPanel = new NuevoPanelPanel(this);
panelContainer.add(nuevoPanel, "NOMBRE_PANEL");
```

### 3. Navegar al Panel
Desde cualquier botón:
```java
button.addActionListener(e -> mainWindow.showPanel("NOMBRE_PANEL"));
```

## Paneles Planificados

### ✅ Implementados
- [x] HomePanel - Panel de inicio

### 📋 Pendientes
- [ ] **BusquedasInternasPanel** - Búsquedas en estructuras de datos
  - Búsqueda secuencial
  - Búsqueda binaria
  - Funciones hash
  
- [ ] **BusquedasExternasPanel** - Búsquedas en archivos
  - Archivos secuenciales
  - Archivos indexados
  - Árboles B

## Ejecución

### Compilar
```bash
javac -d bin src\com\appciencias\views\*.java
```

### Ejecutar
```bash
java -cp bin com.appciencias.views.MainWindow
```

## Principios de Diseño

1. **Consistencia**: Todos los paneles deben seguir el mismo estilo visual
2. **Claridad**: Textos descriptivos y fáciles de entender
3. **Accesibilidad**: Tamaños de fuente legibles, contraste adecuado
4. **Escalabilidad**: Código modular y fácil de extender
5. **Respeto al usuario**: Lenguaje cortés y profesional
