package com.appciencias.views;

import com.appciencias.algorithms.Secuencial;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/**
 * Panel interactivo para búsqueda binaria.
 * Usa Secuencial internamente para almacenar datos en orden de ingreso,
 * y permite ordenarlos para realizar búsqueda binaria.
 */
public class BinarioPanel extends JPanel {

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
    private final Color HIGHLIGHT_DELETE_COLOR = new Color(255, 220, 220);

    private Secuencial secuencial;
    private boolean isTableOrdered = false;
    private String[] orderedData = null;

    private JPanel setupPanel;
    private JPanel actionsPanel;
    private JLabel tableInfoLabel;
    private JTextField sizeField;
    private JTextField lengthField;
    private JTextField keyField;
    private JButton createButton;
    private JButton insertButton;
    private JButton searchButton;
    private JButton deleteButton;
    private JButton sortButton;
    private JButton restartButton;
    private JLabel statusLabel;
    private JLabel fullLabel;
    private JLabel sortWarningLabel;

    private JPanel tablePanel;
    private JScrollPane tableScroll;
    private final List<RowPanel> rowPanels = new ArrayList<>();
    private Timer animationTimer;
    private boolean animationRunning;

    public BinarioPanel() {
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

        JLabel titleLabel = new JLabel("Búsqueda Binaria");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(titleLabel);
        header.add(Box.createVerticalStrut(10));

        JTextArea descArea = new JTextArea(
                "Algoritmo eficiente que busca en estructuras ordenadas dividiendo el espacio de búsqueda a la mitad en cada iteración.");
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

        JLabel sizeLabel = new JLabel("Tamaño máximo de la tabla");
        sizeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sizeLabel.setForeground(TEXT_PRIMARY);

        JLabel lengthLabel = new JLabel("Cantidad de caracteres por clave");
        lengthLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lengthLabel.setForeground(TEXT_PRIMARY);

        sizeField = createTextField(8);
        lengthField = createTextField(8);

        createButton = createActionButton("Crear tabla");
        createButton.addActionListener(e -> handleCreate());

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(sizeLabel, gbc);

        gbc.gridx = 1;
        panel.add(sizeField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(lengthLabel, gbc);

        gbc.gridx = 1;
        panel.add(lengthField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
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
        sortButton = createActionButton("Ordenar");
        searchButton = createActionButton("Buscar");
        deleteButton = createActionButton("Eliminar");
        restartButton = createActionButton("Reiniciar tabla");

        insertButton.addActionListener(e -> handleInsert());
        sortButton.addActionListener(e -> handleSort());
        searchButton.addActionListener(e -> handleSearch());
        deleteButton.addActionListener(e -> handleDelete());
        restartButton.addActionListener(e -> handleRestart());

        buttonsPanel.add(insertButton);
        buttonsPanel.add(sortButton);
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

        sortWarningLabel = new JLabel("Para realizar búsqueda o eliminación, la tabla debe estar ordenada.");
        sortWarningLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        sortWarningLabel.setForeground(new Color(200, 120, 0));
        sortWarningLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sortWarningLabel.setVisible(false);
        panel.add(sortWarningLabel);
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

        JLabel title = new JLabel("Tabla");
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
        tableScroll.setPreferredSize(new Dimension(420, 220));
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
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(BUTTON_HOVER_COLOR);
                }
            }

            @Override
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
            setStatus("Espera a que termine la animación.", true);
            return;
        }

        int tamaño;
        int longitud;
        try {
            tamaño = Integer.parseInt(sizeField.getText().trim());
            longitud = Integer.parseInt(lengthField.getText().trim());
        } catch (NumberFormatException ex) {
            setStatus("Error: Ingresa números válidos.", true);
            return;
        }

        if (tamaño <= 0 || longitud <= 0) {
            setStatus("Error: El tamaño y longitud deben ser mayores que 0.", true);
            return;
        }

        try {
            secuencial = new Secuencial(tamaño, longitud, false);
            isTableOrdered = true;
            orderedData = null;
        } catch (IllegalArgumentException ex) {
            setStatus("Error: " + ex.getMessage(), true);
            return;
        }

        tableInfoLabel.setText("Tabla creada: tamaño " + tamaño + ", longitud " + longitud + ". (datos en orden de ingreso)");
        setupPanel.setVisible(false);
        actionsPanel.setVisible(true);
        setStatus("Tabla creada correctamente.", false);
        refreshTable();
        updateControlsState();
    }

    private void handleInsert() {
        if (!canRunAction()) {
            setStatus("Espera a que termine la animación.", true);
            return;
        }

        String clave = keyField.getText().trim();
        keyField.setText("");

        if (clave.isEmpty()) {
            setStatus("Error: Ingresa una clave.", true);
            return;
        }

        if (secuencial.estaLleno()) {
            setStatus("Error: La tabla está llena.", true);
            return;
        }

        if (clave.length() > secuencial.getLongClave()) {
            setStatus("Error: La clave es demasiado larga.", true);
            return;
        }

        if (clave.length() < secuencial.getLongClave()) {
            try {
                clave = String.format("%0" + secuencial.getLongClave() + "d", Long.parseLong(clave));
            } catch (NumberFormatException e) {
                setStatus("Error: La clave debe tener exactamente " + secuencial.getLongClave() + " carácter(es).", true);
                return;
            }
        }

        String finalClave = clave;
        try {
            secuencial.insertar(finalClave);
            isTableOrdered = false;
            orderedData = null;
            setStatus("Clave '" + finalClave + "' insertada.", false);
            refreshTable();
            updateControlsState();
        } catch (IllegalStateException ex) {
            setStatus("Error: " + ex.getMessage(), true);
        }
    }

    private void handleSort() {
        if (secuencial == null || animationRunning) {
            setStatus("Error: No hay tabla creada o hay una animación en progreso.", true);
            return;
        }

        if (secuencial.getContador() == 0) {
            setStatus("Error: La tabla está vacía.", true);
            return;
        }

        // Obtener datos, ordenarlos
        ArrayList<String> datos = secuencial.obtenerDatos();
        datos.sort(String::compareTo);
        orderedData = datos.toArray(new String[0]);

        // Recrear secuencial con datos ordenados
        int tamaño = secuencial.getTamaño();
        int longitud = secuencial.getLongClave();
        secuencial = new Secuencial(tamaño, longitud, false);

        // Reinsertar datos en orden en el nuevo secuencial
        for (String clave : datos) {
            try {
                secuencial.insertar(clave);
            } catch (IllegalStateException ex) {
                setStatus("Error al ordenar: " + ex.getMessage(), true);
                return;
            }
        }

        isTableOrdered = true;
        setStatus("Tabla ordenada correctamente.", false);
        refreshTable();
        updateControlsState();
    }

    private void handleSearch() {
        if (!canRunAction()) {
            setStatus("Espera a que termine la animación.", true);
            return;
        }

        if (!isCurrentlyOrdered()) {
            setStatus("Error: La tabla debe estar ordenada para buscar.", true);
            return;
        }

        String clave = keyField.getText().trim();
        keyField.setText("");

        if (clave.isEmpty()) {
            setStatus("Error: Ingresa una clave.", true);
            return;
        }

        if (secuencial.getContador() == 0) {
            setStatus("Error: La tabla está vacía.", true);
            return;
        }

        if (clave.length() > secuencial.getLongClave()) {
            setStatus("Error: La clave es demasiado larga.", true);
            return;
        }

        if (clave.length() < secuencial.getLongClave()) {
            try {
                clave = String.format("%0" + secuencial.getLongClave() + "d", Long.parseLong(clave));
            } catch (NumberFormatException e) {
                setStatus("Error: La clave debe tener exactamente " + secuencial.getLongClave() + " carácter(es).", true);
                return;
            }
        }

        String finalClave = clave;
        startBinarySearchAnimation(finalClave);
    }

    private void handleDelete() {
        if (!canRunAction()) {
            setStatus("Espera a que termine la animación.", true);
            return;
        }

        if (!isCurrentlyOrdered()) {
            setStatus("Error: La tabla debe estar ordenada para eliminar.", true);
            return;
        }

        String clave = keyField.getText().trim();
        keyField.setText("");

        if (clave.isEmpty()) {
            setStatus("Error: Ingresa una clave.", true);
            return;
        }

        if (secuencial.getContador() == 0) {
            setStatus("Error: La tabla está vacía.", true);
            return;
        }

        if (clave.length() > secuencial.getLongClave()) {
            setStatus("Error: La clave es demasiado larga.", true);
            return;
        }

        if (clave.length() < secuencial.getLongClave()) {
            try {
                clave = String.format("%0" + secuencial.getLongClave() + "d", Long.parseLong(clave));
            } catch (NumberFormatException e) {
                setStatus("Error: La clave debe tener exactamente " + secuencial.getLongClave() + " carácter(es).", true);
                return;
            }
        }

        String finalClave = clave;
        startBinarySearchAnimationDelete(finalClave);
    }

    private void handleRestart() {
        if (animationRunning) {
            setStatus("Espera a que termine la animación.", true);
            return;
        }

        secuencial = null;
        isTableOrdered = false;
        orderedData = null;
        sizeField.setText("");
        lengthField.setText("");
        keyField.setText("");
        setupPanel.setVisible(true);
        actionsPanel.setVisible(false);
        setStatus(" ", false);
        refreshTable();
    }

    private boolean canRunAction() {
        if (animationRunning) {
            return false;
        }
        if (secuencial == null) {
            setStatus("Error: Primero debes crear una tabla.", true);
            return false;
        }
        return true;
    }

    /**
     * Verifica si los datos actuales están ordenados alfabéticamente
     */
    private boolean isCurrentlyOrdered() {
        if (secuencial == null || secuencial.getContador() == 0) {
            return true;
        }
        ArrayList<String> datos = secuencial.obtenerDatos();
        for (int i = 0; i < datos.size() - 1; i++) {
            if (datos.get(i).compareTo(datos.get(i + 1)) > 0) {
                return false;
            }
        }
        return true;
    }

    private void startBinarySearchAnimation(String clave) {
        if (animationRunning) {
            return;
        }

        animationRunning = true;
        setActionButtonsEnabled(false);
        clearHighlights();

        // Obtener datos ordenados
        ArrayList<String> datos = secuencial.obtenerDatos();
        String[] sortedData = datos.toArray(new String[0]);

        // Crear mapeo de índices ordenados a índices originales
        java.util.List<Integer> originalIndices = new java.util.ArrayList<>();
        java.util.List<String> sortedDatos = new java.util.ArrayList<>(datos);
        sortedDatos.sort(String::compareTo);
        for (String valor : sortedDatos) {
            for (int i = 0; i < datos.size(); i++) {
                if (datos.get(i).equals(valor) && !originalIndices.contains(i)) {
                    originalIndices.add(i);
                    break;
                }
            }
        }

        // Almacenar los pasos de la búsqueda binaria
        java.util.List<BinarySearchStep> steps = new java.util.ArrayList<>();
        int left = 0, right = sortedData.length - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            int cmp = sortedData[mid].compareTo(clave);
            steps.add(new BinarySearchStep(left, right, mid, cmp == 0));

            if (cmp == 0) {
                break;
            } else if (cmp < 0) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }

        if (steps.isEmpty()) {
            setStatus("Clave '" + clave + "' no encontrada.", true);
            animationRunning = false;
            setActionButtonsEnabled(true);
            return;
        }

        // Reproducir pasos con animación
        final int[] currentStep = {0};
        animationTimer = new Timer(600, null);
        animationTimer.addActionListener(e -> {
            if (currentStep[0] >= steps.size()) {
                animationTimer.stop();
                BinarySearchStep lastStep = steps.get(steps.size() - 1);
                if (lastStep.found) {
                    // Encontrar índice en la tabla original
                    int originalIndex = -1;
                    for (int i = 0; i < datos.size(); i++) {
                        if (datos.get(i).equals(clave)) {
                            originalIndex = i;
                            break;
                        }
                    }
                    if (originalIndex != -1) {
                        clearHighlights();
                        highlightPosition(originalIndex, HIGHLIGHT_FOUND_COLOR);
                        setStatus("Clave '" + clave + "' encontrada en " + (currentStep[0]) + " pasos.", false);
                    }
                } else {
                    clearHighlights();
                    setStatus("Clave '" + clave + "' no encontrada despues de " + (currentStep[0]) + " pasos.", true);
                }
                animationRunning = false;
                setActionButtonsEnabled(true);
                return;
            }

            clearHighlights();
            BinarySearchStep step = steps.get(currentStep[0]);
            
            // Resaltar el rango de búsqueda (izquierda y derecha)
            for (int i = step.left; i <= step.right && i < originalIndices.size(); i++) {
                int originalIndex = originalIndices.get(i);
                if (originalIndex < rowPanels.size()) {
                    rowPanels.get(originalIndex).setHighlight(true, HIGHLIGHT_COLOR);
                }
            }
            
            // Resaltar el elemento del medio en color más enfocado
            if (step.mid < originalIndices.size()) {
                int originalMidIndex = originalIndices.get(step.mid);
                if (originalMidIndex < rowPanels.size()) {
                    rowPanels.get(originalMidIndex).setHighlight(true, new Color(100, 150, 255));
                }
            }

            setStatus("Paso " + (currentStep[0] + 1) + ": Comparando con '" + sortedData[step.mid] 
                    + "' (evaluando posiciones " + step.left + " a " + step.right + ")", false);
            
            currentStep[0]++;
        });
        animationTimer.start();
    }

    /**
     * Búsqueda binaria con animación que termina eliminando el elemento encontrado
     */
    private void startBinarySearchAnimationDelete(String clave) {
        if (animationRunning) {
            return;
        }

        animationRunning = true;
        setActionButtonsEnabled(false);
        clearHighlights();

        // Obtener datos ordenados
        ArrayList<String> datos = secuencial.obtenerDatos();
        String[] sortedData = datos.toArray(new String[0]);

        // Crear mapeo de índices ordenados a índices originales
        java.util.List<Integer> originalIndices = new java.util.ArrayList<>();
        java.util.List<String> sortedDatos = new java.util.ArrayList<>(datos);
        sortedDatos.sort(String::compareTo);
        for (String valor : sortedDatos) {
            for (int i = 0; i < datos.size(); i++) {
                if (datos.get(i).equals(valor) && !originalIndices.contains(i)) {
                    originalIndices.add(i);
                    break;
                }
            }
        }

        // Almacenar los pasos de la búsqueda binaria
        java.util.List<BinarySearchStep> steps = new java.util.ArrayList<>();
        int left = 0, right = sortedData.length - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            int cmp = sortedData[mid].compareTo(clave);
            steps.add(new BinarySearchStep(left, right, mid, cmp == 0));

            if (cmp == 0) {
                break;
            } else if (cmp < 0) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }

        if (steps.isEmpty() || !steps.get(steps.size() - 1).found) {
            setStatus("Clave '" + clave + "' no encontrada.", true);
            animationRunning = false;
            setActionButtonsEnabled(true);
            return;
        }

        // Reproducir pasos con animación
        final int[] currentStep = {0};
        animationTimer = new Timer(600, null);
        animationTimer.addActionListener(e -> {
            if (currentStep[0] >= steps.size()) {
                animationTimer.stop();
                
                // Mostrar resalte en rojo antes de eliminar
                clearHighlights();
                
                // Encontrar índice del elemento a eliminar
                int deleteIndex = -1;
                for (int i = 0; i < datos.size(); i++) {
                    if (datos.get(i).equals(clave)) {
                        deleteIndex = i;
                        break;
                    }
                }
                
                if (deleteIndex != -1) {
                    highlightPosition(deleteIndex, HIGHLIGHT_DELETE_COLOR);
                    setStatus("Elemento encontrado: '" + clave + "'. Eliminando...", false);
                    
                    // Esperar 800ms antes de eliminar
                    Timer deleteTimer = new Timer(800, null);
                    deleteTimer.addActionListener(deleteAction -> {
                        deleteTimer.stop();
                        try {
                            clearHighlights();
                            secuencial.eliminar(clave);
                            isTableOrdered = false;
                            orderedData = null;
                            refreshTable();
                            updateControlsState();
                            setStatus("Clave '" + clave + "' encontrada y eliminada en " + (currentStep[0]) + " pasos.", false);
                        } catch (IllegalArgumentException ex) {
                            setStatus("Error al eliminar: " + ex.getMessage(), true);
                        }
                        animationRunning = false;
                        setActionButtonsEnabled(true);
                    });
                    deleteTimer.setRepeats(false);
                    deleteTimer.start();
                } else {
                    setStatus("Error: No se pudo encontrar el elemento.", true);
                    animationRunning = false;
                    setActionButtonsEnabled(true);
                }
                return;
            }

            clearHighlights();
            BinarySearchStep step = steps.get(currentStep[0]);
            
            // Resaltar el rango de búsqueda (izquierda y derecha)
            for (int i = step.left; i <= step.right && i < originalIndices.size(); i++) {
                int originalIndex = originalIndices.get(i);
                if (originalIndex < rowPanels.size()) {
                    rowPanels.get(originalIndex).setHighlight(true, HIGHLIGHT_COLOR);
                }
            }
            
            // Resaltar el elemento del medio en color más enfocado
            if (step.mid < originalIndices.size()) {
                int originalMidIndex = originalIndices.get(step.mid);
                if (originalMidIndex < rowPanels.size()) {
                    rowPanels.get(originalMidIndex).setHighlight(true, new Color(100, 150, 255));
                }
            }

            setStatus("Paso " + (currentStep[0] + 1) + ": Comparando con '" + sortedData[step.mid] 
                    + "' (evaluando posiciones " + step.left + " a " + step.right + ")", false);
            
            currentStep[0]++;
        });
        animationTimer.start();
    }

    /**
     * Clase interna para almacenar información de cada paso de búsqueda binaria
     */
    private class BinarySearchStep {
        int left, right, mid;
        boolean found;

        BinarySearchStep(int left, int right, int mid, boolean found) {
            this.left = left;
            this.right = right;
            this.mid = mid;
            this.found = found;
        }
    }

    /**
     * Realiza búsqueda binaria en un arreglo ordenado
     * @return índice si está presente, -1 si no
     */
    private int binarySearch(String[] sortedData, String target) {
        if (sortedData == null) {
            return -1;
        }
        int left = 0, right = sortedData.length - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            int cmp = sortedData[mid].compareTo(target);
            if (cmp == 0) {
                return mid;
            } else if (cmp < 0) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return -1;
    }

    private void refreshTable() {
        tablePanel.removeAll();
        rowPanels.clear();

        if (secuencial == null || secuencial.getContador() == 0) {
            JLabel emptyLabel = new JLabel("Sin datos cargados.");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            emptyLabel.setForeground(TEXT_SECONDARY);
            emptyLabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            tablePanel.add(emptyLabel);
        } else {
            if (isTableOrdered && orderedData != null) {
                // Mostrar datos ordenados
                for (int i = 0; i < orderedData.length; i++) {
                    RowPanel row = new RowPanel(i + 1, orderedData[i]);
                    rowPanels.add(row);
                    tablePanel.add(row);
                    tablePanel.add(Box.createVerticalStrut(6));
                }
            } else {
                // Mostrar datos en orden de ingreso
                String[] tabla = secuencial.obtenerTabla();
                for (int i = 0; i < secuencial.getContador(); i++) {
                    if (tabla[i] == null) {
                        continue;
                    }
                    RowPanel row = new RowPanel(i + 1, tabla[i]);
                    rowPanels.add(row);
                    tablePanel.add(row);
                    tablePanel.add(Box.createVerticalStrut(6));
                }
            }
        }

        tablePanel.revalidate();
        tablePanel.repaint();
    }

    private void updateControlsState() {
        boolean full = secuencial != null && secuencial.estaLleno();
        insertButton.setEnabled(!full);
        
        boolean ordered = isCurrentlyOrdered();
        boolean searchDeleteDisabled = !ordered && secuencial != null && secuencial.getContador() > 0;
        searchButton.setEnabled(!searchDeleteDisabled);
        deleteButton.setEnabled(!searchDeleteDisabled);
        
        if (searchDeleteDisabled) {
            searchButton.setBackground(BUTTON_DISABLED_COLOR);
            deleteButton.setBackground(BUTTON_DISABLED_COLOR);
            sortWarningLabel.setVisible(true);
        } else {
            searchButton.setBackground(BUTTON_COLOR);
            deleteButton.setBackground(BUTTON_COLOR);
            sortWarningLabel.setVisible(false);
        }
        
        fullLabel.setVisible(full);
    }

    private void setActionButtonsEnabled(boolean enabled) {
        insertButton.setEnabled(enabled && (secuencial == null || !secuencial.estaLleno()));
        searchButton.setEnabled(enabled && isTableOrdered);
        deleteButton.setEnabled(enabled && isTableOrdered);
        sortButton.setEnabled(enabled && secuencial != null && secuencial.getContador() > 0);
        restartButton.setEnabled(enabled);
    }

    private void clearHighlights() {
        for (RowPanel row : rowPanels) {
            row.setHighlight(false, null);
        }
    }

    private void highlightPosition(int index, Color color) {
        if (index >= 0 && index < rowPanels.size()) {
            rowPanels.get(index).setHighlight(true, color);
        }
    }

    private void clearAndHighlightRow(int index, Color color) {
        clearHighlights();
        highlightPosition(index, color);
    }

    private void setStatus(String message, boolean error) {
        statusLabel.setText(message);
        statusLabel.setForeground(error ? new Color(200, 0, 0) : TEXT_PRIMARY);
    }

    private class RowPanel extends JPanel {
        private final String clave;
        private final Color baseColor;

        RowPanel(int posicion, String clave) {
            super(new BorderLayout(10, 0));
            this.clave = clave;
            this.baseColor = Color.WHITE;
            setBackground(baseColor);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)
            ));

            JLabel posLabel = new JLabel("Posición " + posicion);
            posLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            posLabel.setForeground(TEXT_PRIMARY);

            JLabel claveLabel = new JLabel(clave);
            claveLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            claveLabel.setForeground(TEXT_SECONDARY);

            add(posLabel, BorderLayout.WEST);
            add(claveLabel, BorderLayout.CENTER);
        }

        String getClave() {
            return clave;
        }

        void setHighlight(boolean highlight, Color color) {
            setBackground(highlight ? color : baseColor);
            repaint();
        }
    }
}
