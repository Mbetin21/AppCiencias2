package com.appciencias.views;

import com.appciencias.algorithms.ArreglosAnidados;
import com.appciencias.algorithms.FuncionHash;
import com.appciencias.algorithms.ListasEnlazadas;
import com.appciencias.algorithms.TablaHash;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/**
 * Panel interactivo para tabla hash con direccionamiento abierto, arreglos anidados y listas enlazadas.
 * Soporta múltiples funciones hash y métodos de resolución de colisiones.
 */
public class HashPanel extends JPanel {

    private final Color BACKGROUND_COLOR = new Color(245, 245, 250);
    private final Color PANEL_COLOR = new Color(235, 235, 245);
    private final Color BORDER_COLOR = new Color(200, 200, 210);
    private final Color TEXT_PRIMARY = new Color(70, 70, 80);
    private final Color TEXT_SECONDARY = new Color(100, 100, 110);
    private final Color BUTTON_COLOR = new Color(230, 230, 240);
    private final Color BUTTON_HOVER_COLOR = new Color(220, 220, 235);
    private final Color BUTTON_DISABLED_COLOR = new Color(220, 220, 220);
    private final Color HIGHLIGHT_COLOR = new Color(210, 225, 245);
    private final Color HIGHLIGHT_FOUND_COLOR = new Color(205, 235, 210);
    private final Color HIGHLIGHT_COLLISION_COLOR = new Color(255, 235, 200);
    private final Color HIGHLIGHT_DELETE_COLOR = new Color(255, 220, 220);

    // Estructuras de datos
    private TablaHash tablaHash;
    private ArreglosAnidados arreglosAnidados;
    private ListasEnlazadas listasEnlazadas;
    private int estructuraActual = 0; // 0=TablaHash, 1=ArreglosAnidados, 2=ListasEnlazadas
    private int longitudClave;

    private JPanel setupPanel;
    private JPanel actionsPanel;
    private JLabel tableInfoLabel;
    private JSpinner lengthSpinner;
    private JComboBox<String> hashFunctionCombo;
    private JComboBox<String> collisionMethodCombo;
    private JButton createButton;
    private JTextField keyField;
    private JButton insertButton;
    private JButton searchButton;
    private JButton deleteButton;
    private JButton restartButton;
    private JLabel statusLabel;
    private JLabel fullLabel;

    private JPanel tablePanel;
    private JScrollPane tableScroll;
    private final List<RowPanel> rowPanels = new ArrayList<>();
    private Timer animationTimer;
    private boolean animationRunning;

    // Constantes para visualización
    private static final int ROWS_PER_PAGE = 10;
    private static final int EMPTY_ROWS_AFTER_LAST = 3;

    public HashPanel() {
        initializePanel();
    }

    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BACKGROUND_COLOR);
        content.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        content.add(createHeader());
        content.add(Box.createVerticalStrut(20));
        content.add(createInteractivePanel());

        add(content, BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(BACKGROUND_COLOR);

        JLabel titleLabel = new JLabel("Tabla Hash");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(titleLabel);
        header.add(Box.createVerticalStrut(10));

        JTextArea descArea = new JTextArea(
                "Estructura de datos que almacena información usando funciones hash para acceso rápido.");
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        descArea.setForeground(TEXT_SECONDARY);
        descArea.setBackground(BACKGROUND_COLOR);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(descArea);

        return header;
    }

    private JPanel createInteractivePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        setupPanel = createSetupPanel();
        actionsPanel = createActionsPanel();
        actionsPanel.setVisible(false);

        panel.add(setupPanel);
        panel.add(Box.createVerticalStrut(12));
        panel.add(actionsPanel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(createTablePanel());

        return panel;
    }

    private JPanel createSetupPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(PANEL_COLOR);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Longitud de clave
        JLabel lengthLabel = new JLabel("Cantidad de caracteres por clave");
        lengthLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lengthLabel.setForeground(TEXT_PRIMARY);

        lengthSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        lengthSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Función Hash
        JLabel hashLabel = new JLabel("Función Hash");
        hashLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        hashLabel.setForeground(TEXT_PRIMARY);

        hashFunctionCombo = new JComboBox<>(new String[]{
            "Módulo (k mod n)",
            "Cuadrado (k² mod n)",
            "Truncamiento",
            "Plegamiento (Suma)",
            "Plegamiento (Multiplicación)"
        });
        hashFunctionCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Método de Colisión / Estructura de Datos
        JLabel collisionLabel = new JLabel("Solución de Colisiones");
        collisionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        collisionLabel.setForeground(TEXT_PRIMARY);

        collisionMethodCombo = new JComboBox<>(new String[]{
            "Prueba Lineal (D + i)",
            "Prueba Cuadrática (D + i²)",
            "Doble Hash (D + i*H2)",
            "Arreglos Anidados",
            "Listas Enlazadas"
        });
        collisionMethodCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        createButton = createActionButton("Crear tabla");
        createButton.addActionListener(e -> handleCreate());

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(lengthLabel, gbc);

        gbc.gridx = 1;
        panel.add(lengthSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(hashLabel, gbc);

        gbc.gridx = 1;
        panel.add(hashFunctionCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(collisionLabel, gbc);

        gbc.gridx = 1;
        panel.add(collisionMethodCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(createButton, gbc);

        return panel;
    }

    private JPanel createActionsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PANEL_COLOR);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        tableInfoLabel = new JLabel("Tabla creada.");
        tableInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tableInfoLabel.setForeground(TEXT_SECONDARY);
        tableInfoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(tableInfoLabel);
        panel.add(Box.createVerticalStrut(10));

        JPanel keyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        keyPanel.setBackground(PANEL_COLOR);
        JLabel keyLabel = new JLabel("Clave");
        keyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        keyLabel.setForeground(TEXT_PRIMARY);
        keyField = createTextField(16);
        keyPanel.add(keyLabel);
        keyPanel.add(keyField);
        panel.add(keyPanel);
        panel.add(Box.createVerticalStrut(10));

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonsPanel.setBackground(PANEL_COLOR);
        insertButton = createActionButton("Insertar");
        searchButton = createActionButton("Buscar");
        deleteButton = createActionButton("Eliminar");
        restartButton = createActionButton("Reiniciar búsqueda");

        insertButton.addActionListener(e -> handleInsert());
        searchButton.addActionListener(e -> handleSearch());
        deleteButton.addActionListener(e -> handleDelete());
        restartButton.addActionListener(e -> handleRestart());

        buttonsPanel.add(insertButton);
        buttonsPanel.add(searchButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(restartButton);
        panel.add(buttonsPanel);
        panel.add(Box.createVerticalStrut(10));

        fullLabel = new JLabel("La tabla está llena.");
        fullLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        fullLabel.setForeground(TEXT_SECONDARY);
        fullLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        fullLabel.setVisible(false);
        panel.add(fullLabel);
        panel.add(Box.createVerticalStrut(8));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusLabel.setForeground(TEXT_PRIMARY);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(statusLabel);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_COLOR);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Contenido de la tabla");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        panel.add(title, BorderLayout.NORTH);

        tablePanel = new JPanel();
        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
        tablePanel.setBackground(PANEL_COLOR);

        tableScroll = new JScrollPane(tablePanel);
        tableScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        tableScroll.setBackground(PANEL_COLOR);
        tableScroll.setPreferredSize(new Dimension(500, 250));
        tableScroll.getViewport().setBackground(PANEL_COLOR);

        panel.add(tableScroll, BorderLayout.CENTER);

        refreshTable();

        return panel;
    }

    private JTextField createTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        return field;
    }

    private JButton createActionButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(TEXT_PRIMARY);
        button.setBackground(BUTTON_COLOR);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(BUTTON_HOVER_COLOR);
                }
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(BUTTON_COLOR);
                }
            }
        });

        return button;
    }

    private void handleCreate() {
        if (animationRunning) {
            return;
        }

        int longitud;
        try {
            longitud = (Integer) lengthSpinner.getValue();
        } catch (NumberFormatException ex) {
            setStatus("Longitud de clave inválida.", true);
            return;
        }

        // Calcular tamaño máximo de la tabla
        int tamaño = (int) Math.pow(10, longitud);

        // Crear función hash
        FuncionHash.Tipo tipoHash = obtenerTipoHash();
        FuncionHash funcionHash = crearFuncionHash(tipoHash, tamaño, longitud);

        // Obtener opción seleccionada del comboBox unificado
        int opcionSeleccionada = collisionMethodCombo.getSelectedIndex();

        // Limpiar referencias previas
        tablaHash = null;
        arreglosAnidados = null;
        listasEnlazadas = null;

        // Crear la estructura correspondiente
        if (opcionSeleccionada <= 2) {
            // Opciones 0-2: Direccionamiento Abierto con diferentes métodos
            estructuraActual = 0;
            TablaHash.TipoColision tipoColision = obtenerTipoColisionPorIndice(opcionSeleccionada);
            tablaHash = new TablaHash(tamaño, longitud, funcionHash, tipoColision);
        } else if (opcionSeleccionada == 3) {
            // Opción 3: Arreglos Anidados
            estructuraActual = 1;
            arreglosAnidados = new ArreglosAnidados(tamaño, longitud, funcionHash);
        } else if (opcionSeleccionada == 4) {
            // Opción 4: Listas Enlazadas
            estructuraActual = 2;
            listasEnlazadas = new ListasEnlazadas(tamaño, longitud, funcionHash);
        }

        longitudClave = longitud;

        setupPanel.setVisible(false);
        actionsPanel.setVisible(true);

        updateTableInfo();
        refreshTable();

        setStatus("Estructura creada correctamente.", false);
        keyField.setText("");
    }

    private TablaHash.TipoColision obtenerTipoColisionPorIndice(int indice) {
        switch (indice) {
            case 0:
                return TablaHash.TipoColision.LINEAL;
            case 1:
                return TablaHash.TipoColision.CUADRATICA;
            case 2:
                return TablaHash.TipoColision.DOBLE_HASH;
            default:
                return TablaHash.TipoColision.LINEAL;
        }
    }

    private FuncionHash.Tipo obtenerTipoHash() {
        int seleccion = hashFunctionCombo.getSelectedIndex();
        switch (seleccion) {
            case 0:
                return FuncionHash.Tipo.MOD;
            case 1:
                return FuncionHash.Tipo.CUADRADO;
            case 2:
                return FuncionHash.Tipo.TRUNCAMIENTO;
            case 3:
            case 4:
                return FuncionHash.Tipo.PLEGAMIENTO;
            default:
                return FuncionHash.Tipo.MOD;
        }
    }

    private FuncionHash crearFuncionHash(FuncionHash.Tipo tipo, int tamaño, int longitud) {
        switch (tipo) {
            case MOD:
                return new FuncionHash(tipo, tamaño);
            case CUADRADO:
                return new FuncionHash(tipo, tamaño);
            case TRUNCAMIENTO:
                // Usar posiciones seguras: 1, 3, 5, 7, ... (impares)
                // Máximo 3 posiciones para evitar problemas con claves que se convierten a pocos dígitos
                int cantidad = Math.min(3, (longitud + 1) / 2);
                int[] posiciones = new int[cantidad];
                for (int i = 0; i < posiciones.length; i++) {
                    posiciones[i] = (i * 2) + 1;  // 1, 3, 5, 7...
                }
                return new FuncionHash(tamaño, posiciones);
            case PLEGAMIENTO:
                int seleccion = hashFunctionCombo.getSelectedIndex();
                FuncionHash.TipoPlegamiento tipoPlegamiento = 
                    (seleccion == 3) ? FuncionHash.TipoPlegamiento.SUMA 
                                    : FuncionHash.TipoPlegamiento.MULTIPLICACION;
                return new FuncionHash(tamaño, tipoPlegamiento);
            default:
                return new FuncionHash(FuncionHash.Tipo.MOD, tamaño);
        }
    }

    private void handleInsert() {
        if (animationRunning) {
            return;
        }

        String clave = keyField.getText().trim();

        if (!validarClave(clave)) {
            return;
        }

        // Normalizar la clave agregando ceros a la izquierda si es necesario
        String claveNormalizada = normalizarClave(clave);

        try {
            switch (estructuraActual) {
                case 0: // Direccionamiento Abierto
                    if (tablaHash == null) return;
                    tablaHash.insertar(claveNormalizada);
                    int posicion = tablaHash.buscar(claveNormalizada);
                    int posicionBase = tablaHash.obtenerPosicionBase(claveNormalizada) - 1;
                    boolean huboColision = (posicion != posicionBase);
                    
                    if (huboColision) {
                        animateInsertion(posicion, claveNormalizada, true);
                        setStatus("Colisión encontrada. Clave asignada en posición " + (posicion + 1) + ".", false);
                    } else {
                        animateInsertion(posicion, claveNormalizada, false);
                        setStatus("Clave asignada en posición " + (posicion + 1) + ".", false);
                    }
                    break;

                case 1: // Arreglos Anidados
                    if (arreglosAnidados == null) return;
                    arreglosAnidados.insertar(claveNormalizada);
                    ArreglosAnidados.ResultadoBusqueda resulArrAnidados = arreglosAnidados.buscar(claveNormalizada);
                    boolean esArrAnidadoColision = (resulArrAnidados.numeroArreglo > 0);
                    
                    if (esArrAnidadoColision) {
                        animateInsertion(resulArrAnidados.numeroArreglo, clave, true);
                        setStatus("Clave en arreglo " + resulArrAnidados.numeroArreglo + ", posición " + resulArrAnidados.posicion + ".", false);
                    } else {
                        animateInsertion(0, clave, false);
                        setStatus("Clave en arreglo principal, posición " + resulArrAnidados.posicion + ".", false);
                    }
                    break;

                case 2: // Listas Enlazadas
                    if (listasEnlazadas == null) return;
                    listasEnlazadas.insertar(claveNormalizada);
                    ListasEnlazadas.ResultadoBusqueda resulListasEnl = listasEnlazadas.buscar(claveNormalizada);
                    boolean esListasEnlColision = (!resulListasEnl.enTabla);
                    
                    if (esListasEnlColision) {
                        animateInsertion(resulListasEnl.saltosEnCadena, claveNormalizada, true);
                        setStatus("Clave en cadena de posición " + resulListasEnl.posicionBase + ", nodo " + resulListasEnl.saltosEnCadena + ".", false);
                    } else {
                        animateInsertion(0, claveNormalizada, false);
                        setStatus("Clave en posición base " + resulListasEnl.posicionBase + ".", false);
                    }
                    break;
            }

            keyField.setText("");
            updateTableInfo();
        } catch (IllegalStateException ex) {
            if (ex.getMessage().contains("llena")) {
                fullLabel.setVisible(true);
                setStatus("No hay espacio disponible.", true);
            } else {
                setStatus("La clave ya existe.", true);
            }
        } catch (Exception ex) {
            setStatus("Error: " + ex.getMessage(), true);
        }
    }

    private void handleSearch() {
        if (animationRunning) {
            return;
        }

        clearHighlights();

        String clave = keyField.getText().trim();

        if (!validarClave(clave)) {
            return;
        }

        // Normalizar la clave agregando ceros a la izquierda si es necesario
        String claveNormalizada = normalizarClave(clave);

        try {
            switch (estructuraActual) {
                case 0: // Direccionamiento Abierto
                    if (tablaHash == null) return;
                    int posicion = tablaHash.buscar(claveNormalizada);
                    if (posicion != -1) {
                        animateSearch(posicion);
                        setStatus("Clave encontrada en posición " + (posicion + 1) + ".", false);
                    } else {
                        setStatus("Clave no encontrada.", true);
                    }
                    break;

                case 1: // Arreglos Anidados
                    if (arreglosAnidados == null) return;
                    ArreglosAnidados.ResultadoBusqueda resulAnn = arreglosAnidados.buscar(claveNormalizada);
                    if (resulAnn != null) {
                        animateSearchMultipleArrays(resulAnn.numeroArreglo);
                        setStatus(resulAnn.toString(), false);
                    } else {
                        setStatus("Clave no encontrada.", true);
                    }
                    break;

                case 2: // Listas Enlazadas
                    if (listasEnlazadas == null) return;
                    ListasEnlazadas.ResultadoBusqueda resulListas = listasEnlazadas.buscar(claveNormalizada);
                    if (resulListas != null) {
                        animateSearch(resulListas.posicionBase - 1);
                        setStatus(resulListas.toString(), false);
                    } else {
                        setStatus("Clave no encontrada.", true);
                    }
                    break;
            }
        } catch (Exception ex) {
            setStatus("Error: " + ex.getMessage(), true);
        }
    }

    private void handleDelete() {
        if (animationRunning) {
            return;
        }

        String clave = keyField.getText().trim();

        if (!validarClave(clave)) {
            return;
        }

        // Normalizar la clave agregando ceros a la izquierda si es necesario
        String claveNormalizada = normalizarClave(clave);

        try {
            switch (estructuraActual) {
                case 0: // Direccionamiento Abierto
                    if (tablaHash == null) return;
                    int posicion = tablaHash.buscar(claveNormalizada);
                    if (posicion == -1) {
                        setStatus("Clave no encontrada.", true);
                        return;
                    }
                    tablaHash.eliminar(claveNormalizada);
                    animateDeletion(posicion);
                    setStatus("Clave eliminada de posición " + (posicion + 1) + ".", false);
                    break;

                case 1: // Arreglos Anidados
                    if (arreglosAnidados == null) return;
                    ArreglosAnidados.ResultadoBusqueda resulAnn = arreglosAnidados.buscar(claveNormalizada);
                    if (resulAnn == null) {
                        setStatus("Clave no encontrada.", true);
                        return;
                    }
                    arreglosAnidados.eliminar(claveNormalizada);
                    animateDeletion(resulAnn.numeroArreglo);
                    setStatus("Clave eliminada del arreglo " + resulAnn.numeroArreglo + ".", false);
                    break;

                case 2: // Listas Enlazadas
                    if (listasEnlazadas == null) return;
                    ListasEnlazadas.ResultadoBusqueda resulListas = listasEnlazadas.buscar(claveNormalizada);
                    if (resulListas == null) {
                        setStatus("Clave no encontrada.", true);
                        return;
                    }
                    listasEnlazadas.eliminar(claveNormalizada);
                    animateDeletion(resulListas.posicionBase - 1);
                    setStatus("Clave eliminada de posición " + resulListas.posicionBase + ".", false);
                    break;
            }

            keyField.setText("");
            updateTableInfo();
            fullLabel.setVisible(false);
        } catch (Exception ex) {
            statusLabel.setText("Error: " + ex.getMessage());
            statusLabel.setForeground(new Color(220, 70, 70));
        }
    }

    private void handleRestart() {
        if (animationRunning) {
            return;
        }

        tablaHash = null;
        arreglosAnidados = null;
        listasEnlazadas = null;
        lengthSpinner.setValue(3);
        hashFunctionCombo.setSelectedIndex(0);
        collisionMethodCombo.setSelectedIndex(0);
        keyField.setText("");
        setupPanel.setVisible(true);
        actionsPanel.setVisible(false);
        fullLabel.setVisible(false);
        setStatus(" ", false);
        refreshTable();
    }

    private boolean validarClave(String clave) {
        if (clave.isEmpty()) {
            setStatus("Ingrese una clave.", true);
            return false;
        }

        // Validar que solo contenga dígitos
        if (!clave.matches("\\d+")) {
            setStatus("La clave debe contener sólo dígitos.", true);
            return false;
        }

        // Si la clave es más larga que lo permitido, error
        if (clave.length() > longitudClave) {
            setStatus("La clave no puede tener más de " + longitudClave + " caracteres.", true);
            return false;
        }

        return true;
    }

    private String normalizarClave(String clave) {
        // Agregar ceros a la izquierda si es necesario
        if (clave.length() < longitudClave) {
            StringBuilder claveNormalizada = new StringBuilder(clave);
            while (claveNormalizada.length() < longitudClave) {
                claveNormalizada.insert(0, '0');
            }
            return claveNormalizada.toString();
        }
        return clave;
    }

    private void updateTableInfo() {
        switch (estructuraActual) {
            case 0: // Direccionamiento Abierto
                if (tablaHash != null) {
                    int usadas = tablaHash.getContador();
                    int total = tablaHash.getTamaño();
                    tableInfoLabel.setText("Tabla: " + usadas + " elemento(s) de " + total + " posiciones.");
                    fullLabel.setVisible(tablaHash.estaLlena());
                }
                break;

            case 1: // Arreglos Anidados
                if (arreglosAnidados != null) {
                    int usadas = arreglosAnidados.getContador();
                    int numArreglos = arreglosAnidados.getNumeroArreglos();
                    tableInfoLabel.setText("Arreglos: " + usadas + " elemento(s) en " + numArreglos + " arreglo(s).");
                    fullLabel.setVisible(false);
                }
                break;

            case 2: // Listas Enlazadas
                if (listasEnlazadas != null) {
                    int usadas = listasEnlazadas.getContador();
                    int total = listasEnlazadas.getTamaño();
                    tableInfoLabel.setText("Tabla: " + usadas + " elemento(s) de " + total + " posiciones.");
                    fullLabel.setVisible(false);
                }
                break;
        }
    }

    private void refreshTable() {
        tablePanel.removeAll();
        rowPanels.clear();

        switch (estructuraActual) {
            case 0: // Direccionamiento Abierto (TablaHash)
                refreshTableOpenAddressing();
                break;
            case 1: // Arreglos Anidados
                refreshTableNestedArrays();
                break;
            case 2: // Listas Enlazadas
                refreshTableLinkedLists();
                break;
        }

        tablePanel.add(Box.createVerticalGlue());
        tablePanel.revalidate();
        tablePanel.repaint();
    }

    private void refreshTableOpenAddressing() {
        if (tablaHash == null) {
            tablePanel.revalidate();
            tablePanel.repaint();
            return;
        }

        String[] tabla = tablaHash.obtenerTabla();
        
        // Encontrar la última posición con datos
        int ultimaPosicionConDatos = -1;
        for (int i = tabla.length - 1; i >= 0; i--) {
            if (tabla[i] != null && !tabla[i].equals(tablaHash.getMarcadorEliminado())) {
                ultimaPosicionConDatos = i;
                break;
            }
        }

        if (ultimaPosicionConDatos == -1) {
            // Tabla vacía
            JLabel emptyLabel = new JLabel("Tabla vacía");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            emptyLabel.setForeground(TEXT_SECONDARY);
            emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            tablePanel.add(emptyLabel);
            return;
        }

        int mostrarHasta = Math.min(ultimaPosicionConDatos + EMPTY_ROWS_AFTER_LAST, tabla.length - 1);
        
        for (int i = 0; i <= mostrarHasta; i++) {
            String celda = tabla[i];
            boolean esVacio = (celda == null);
            
            if (esVacio && i < ultimaPosicionConDatos) {
                int contadorVacios = 0;
                int j = i;
                while (j <= mostrarHasta && tabla[j] == null) {
                    contadorVacios++;
                    j++;
                }
                
                if (contadorVacios > 2) {
                    JLabel ellipsisLabel = new JLabel("     ...");
                    ellipsisLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                    ellipsisLabel.setForeground(TEXT_SECONDARY);
                    ellipsisLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    tablePanel.add(ellipsisLabel);
                    
                    int proximoConDatos = j;
                    while (proximoConDatos <= mostrarHasta && tabla[proximoConDatos] == null) {
                        proximoConDatos++;
                    }
                    
                    if (proximoConDatos <= mostrarHasta) {
                        i = proximoConDatos - 1;
                    } else {
                        i = mostrarHasta;
                    }
                    continue;
                }
            }
            
            RowPanel rowPanel = new RowPanel(i + 1, celda, tablaHash.getMarcadorEliminado());
            rowPanels.add(rowPanel);
            tablePanel.add(rowPanel);
        }
    }

    private void refreshTableNestedArrays() {
        if (arreglosAnidados == null) {
            tablePanel.revalidate();
            tablePanel.repaint();
            return;
        }

        ArrayList<String[]> arreglos = arreglosAnidados.obtenerArreglos();
        
        if (arreglos.isEmpty() || arreglos.get(0) == null) {
            JLabel emptyLabel = new JLabel("Sin elementos");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            emptyLabel.setForeground(TEXT_SECONDARY);
            emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            tablePanel.add(emptyLabel);
            return;
        }

        int tamaño = arreglos.get(0).length;

        // Para cada posición, mostrar la pila de valores
        for (int pos = 0; pos < tamaño; pos++) {
            List<String> pila = new ArrayList<>();
            
            // Recopilar todos los valores en esta posición de todos los arreglos
            for (String[] arreglo : arreglos) {
                if (arreglo[pos] != null) {
                    pila.add(arreglo[pos]);
                }
            }

            // Si hay algo en esa posición, mostrar la pila
            if (!pila.isEmpty()) {
                // Mostrar encabezado de posición
                JLabel posLabel = new JLabel("Posición [" + (pos + 1) + "]");
                posLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
                posLabel.setForeground(TEXT_PRIMARY);
                posLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 4, 0));
                posLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                tablePanel.add(posLabel);

                // Mostrar cada valor en la pila
                for (int i = 0; i < pila.size(); i++) {
                    JPanel stackItemPanel = new JPanel(new BorderLayout(10, 0));
                    stackItemPanel.setBackground(HIGHLIGHT_COLLISION_COLOR);
                    stackItemPanel.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(BORDER_COLOR, 1),
                            BorderFactory.createEmptyBorder(6, 20, 6, 10)
                    ));

                    String arrLabel = (i == 0) ? "Principal" : "Desbord. " + i;
                    JLabel arrLabelComp = new JLabel("↓ " + arrLabel + ":");
                    arrLabelComp.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    arrLabelComp.setForeground(TEXT_PRIMARY);

                    JLabel valueLabel = new JLabel(pila.get(i));
                    valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    valueLabel.setForeground(TEXT_PRIMARY);

                    stackItemPanel.add(arrLabelComp, BorderLayout.WEST);
                    stackItemPanel.add(valueLabel, BorderLayout.CENTER);
                    tablePanel.add(stackItemPanel);
                }

                tablePanel.add(Box.createVerticalStrut(8));
            }
        }
    }

    private void refreshTableLinkedLists() {
        if (listasEnlazadas == null) {
            tablePanel.revalidate();
            tablePanel.repaint();
            return;
        }

        ArrayList<ListasEnlazadas.FilaTabla> filas = listasEnlazadas.obtenerTabla();
        
        boolean hayDatos = false;
        for (ListasEnlazadas.FilaTabla fila : filas) {
            if (!fila.estaVacia() || fila.tieneCadena()) {
                hayDatos = true;
                break;
            }
        }

        if (!hayDatos) {
            JLabel emptyLabel = new JLabel("Tabla vacía");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            emptyLabel.setForeground(TEXT_SECONDARY);
            emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            tablePanel.add(emptyLabel);
            return;
        }

        // Mostrar solo posiciones que tengan datos
        for (ListasEnlazadas.FilaTabla fila : filas) {
            if (fila.estaVacia() && !fila.tieneCadena()) {
                continue; // Saltar posiciones vacías
            }

            // Panel para la posición
            JPanel posPanel = new JPanel(new BorderLayout());
            posPanel.setBackground(PANEL_COLOR);
            posPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            // Encabezado de posición
            JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            headerPanel.setBackground(PANEL_COLOR);

            JLabel posLabel = new JLabel("[Posición " + fila.posicion + "]");
            posLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            posLabel.setForeground(TEXT_PRIMARY);
            posLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
            headerPanel.add(posLabel);
            posPanel.add(headerPanel, BorderLayout.NORTH);

            // Panel de contenido (tabla + cadena)
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            contentPanel.setBackground(PANEL_COLOR);

            // Elemento principal de la tabla
            if (!fila.estaVacia()) {
                JPanel cellPanel = new JPanel(new BorderLayout(10, 0));
                cellPanel.setBackground(Color.WHITE);
                cellPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 1),
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)
                ));

                JLabel cellLabel = new JLabel("Tabla:");
                cellLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                cellLabel.setForeground(TEXT_SECONDARY);

                JLabel valueLabel = new JLabel(fila.claveBase);
                valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
                valueLabel.setForeground(TEXT_PRIMARY);

                cellPanel.add(cellLabel, BorderLayout.WEST);
                cellPanel.add(valueLabel, BorderLayout.CENTER);
                contentPanel.add(cellPanel);
            }

            // Cadena enlazada
            if (fila.tieneCadena()) {
                contentPanel.add(Box.createVerticalStrut(4));
                
                for (int i = 0; i < fila.cadena.size(); i++) {
                    JPanel chainPanel = new JPanel(new BorderLayout(10, 0));
                    chainPanel.setBackground(HIGHLIGHT_COLLISION_COLOR);
                    chainPanel.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(BORDER_COLOR, 1),
                            BorderFactory.createEmptyBorder(6, 20, 6, 10)
                    ));

                    JLabel chainLabel = new JLabel("→ Nodo " + (i + 1) + ":");
                    chainLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    chainLabel.setForeground(TEXT_PRIMARY);

                    JLabel chainValue = new JLabel(fila.cadena.get(i));
                    chainValue.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    chainValue.setForeground(TEXT_PRIMARY);

                    chainPanel.add(chainLabel, BorderLayout.WEST);
                    chainPanel.add(chainValue, BorderLayout.CENTER);
                    contentPanel.add(chainPanel);
                }
            }

            posPanel.add(contentPanel, BorderLayout.CENTER);
            tablePanel.add(posPanel);
            tablePanel.add(Box.createVerticalStrut(8));
        }
    }

    private void clearHighlights() {
        for (RowPanel row : rowPanels) {
            row.setHighlighted(null);
        }
    }

    private void animateInsertion(int posicion, String clave, boolean wasCollision) {
        startAnimation();

        new Timer(100, e -> {
            if (rowPanels.size() > posicion && rowPanels.get(posicion) != null) {
                Color color = wasCollision ? HIGHLIGHT_COLLISION_COLOR : HIGHLIGHT_FOUND_COLOR;
                rowPanels.get(posicion).setHighlighted(color);
            }
            updateTableInfo();
            refreshTable();
            stopAnimation();
        }).start();
    }

    private void animateSearch(int posicion) {
        startAnimation();

        new Timer(100, e -> {
            refreshTable();
            if (rowPanels.size() > posicion && rowPanels.get(posicion) != null) {
                rowPanels.get(posicion).setHighlighted(HIGHLIGHT_FOUND_COLOR);
            }
            stopAnimation();
        }).start();
    }

    private void animateSearchMultipleArrays(int arrayIndex) {
        startAnimation();

        new Timer(100, e -> {
            refreshTable();
            // Para arreglos anidados, podría resaltar visualmente la posición
            stopAnimation();
        }).start();
    }

    private void animateDeletion(int posicion) {
        startAnimation();

        new Timer(100, e -> {
            if (rowPanels.size() > posicion && rowPanels.get(posicion) != null) {
                rowPanels.get(posicion).setHighlighted(HIGHLIGHT_DELETE_COLOR);
            }
            refreshTable();
            stopAnimation();
        }).start();
    }

    private void startAnimation() {
        animationRunning = true;
        insertButton.setEnabled(false);
        searchButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }

    private void stopAnimation() {
        animationRunning = false;
        insertButton.setEnabled(true);
        searchButton.setEnabled(true);
        deleteButton.setEnabled(true);
    }

    private void setStatus(String message, boolean error) {
        statusLabel.setText(message);
        statusLabel.setForeground(error ? new Color(200, 0, 0) : TEXT_PRIMARY);
    }

    private class RowPanel extends JPanel {
        private final String clave;
        private final String marcadorEliminado;
        private final Color baseColor;

        RowPanel(int posicion, String clave, String marcadorEliminado) {
            super(new BorderLayout(10, 0));
            this.clave = clave;
            this.marcadorEliminado = marcadorEliminado;
            this.baseColor = Color.WHITE;
            setBackground(baseColor);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)
            ));

            JLabel posLabel = new JLabel("[" + posicion + "]");
            posLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            posLabel.setForeground(TEXT_PRIMARY);

            String displayText;
            Color textColor = TEXT_SECONDARY;

            if (clave == null) {
                displayText = "(vacío)";
            } else if (clave.equals(marcadorEliminado)) {
                displayText = "(eliminado)";
                textColor = new Color(150, 150, 150);
            } else {
                displayText = clave;
            }

            JLabel claveLabel = new JLabel(displayText);
            claveLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            claveLabel.setForeground(textColor);

            add(posLabel, BorderLayout.WEST);
            add(claveLabel, BorderLayout.CENTER);
        }

        String getClave() {
            return clave;
        }

        void setHighlighted(Color color) {
            setBackground(color == null ? baseColor : color);
            repaint();
        }
    }
}
