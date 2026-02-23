package com.appciencias.views;

import com.appciencias.algorithms.Secuencial;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/**
 * Panel interactivo para búsqueda secuencial.
 */
public class SecuencialPanel extends JPanel {

    private final Color BACKGROUND_COLOR = new Color(245, 245, 250);
    private final Color PANEL_COLOR = new Color(235, 235, 245);
    private final Color BORDER_COLOR = new Color(200, 200, 210);
    private final Color TEXT_PRIMARY = new Color(70, 70, 80);
    private final Color TEXT_SECONDARY = new Color(100, 100, 110);
    private final Color BUTTON_COLOR = new Color(230, 230, 240);
    private final Color BUTTON_HOVER_COLOR = new Color(220, 220, 235);
    private final Color HIGHLIGHT_COLOR = new Color(210, 225, 245);
    private final Color HIGHLIGHT_FOUND_COLOR = new Color(205, 235, 210);

    private Secuencial secuencial;

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
    private JButton restartButton;
    private JLabel statusLabel;
    private JLabel fullLabel;

    private JPanel tablePanel;
    private JScrollPane tableScroll;
    private final List<RowPanel> rowPanels = new ArrayList<>();
    private Timer animationTimer;
    private boolean animationRunning;

    public SecuencialPanel() {
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

        JLabel titleLabel = new JLabel("Búsqueda Secuencial");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(titleLabel);
        header.add(Box.createVerticalStrut(10));

        JTextArea descArea = new JTextArea(
                "Algoritmo que busca un elemento recorriendo la tabla elemento por elemento hasta encontrarlo.");
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
        searchButton = createActionButton("Buscar");
        deleteButton = createActionButton("Eliminar");
        restartButton = createActionButton("Reiniciar tabla");

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

        int tamaño;
        int longitud;
        try {
            tamaño = Integer.parseInt(sizeField.getText().trim());
            longitud = Integer.parseInt(lengthField.getText().trim());
        } catch (NumberFormatException ex) {
            setStatus("Ingrese números válidos para el tamaño y la longitud.", true);
            return;
        }

        if (tamaño <= 0 || longitud <= 0) {
            setStatus("Los valores deben ser mayores que 0.", true);
            return;
        }

        try {
            secuencial = new Secuencial(tamaño, longitud, false);
        } catch (IllegalArgumentException ex) {
            setStatus(ex.getMessage(), true);
            return;
        }

        tableInfoLabel.setText("Tabla creada: tamaño " + tamaño + ", longitud " + longitud + ".");
        setupPanel.setVisible(false);
        actionsPanel.setVisible(true);
        setStatus("Tabla creada correctamente.", false);
        refreshTable();
        updateControlsState();
    }

    private void handleInsert() {
        if (!canRunAction()) {
            return;
        }

        String clave = keyField.getText().trim();
        keyField.setText("");

        if (clave.isEmpty()) {
            setStatus("Ingrese una clave.", true);
            return;
        }

        if (secuencial.estaLleno()) {
            setStatus("La tabla ya está llena.", true);
            updateControlsState();
            return;
        }

        // Validar longitud y rellenar con ceros a la izquierda si es necesario (solo para números)
        if (clave.length() > secuencial.getLongClave()) {
            setStatus("La clave no puede exceder " + secuencial.getLongClave() + " carácter(es).", true);
            return;
        }

        // Rellenar con ceros a la izquierda si es más corta (solo si es numérica)
        if (clave.length() < secuencial.getLongClave()) {
            try {
                clave = String.format("%0" + secuencial.getLongClave() + "d", Long.parseLong(clave));
            } catch (NumberFormatException e) {
                // Si no es numérica, requiere la longitud exacta
                setStatus("La clave debe tener exactamente " + secuencial.getLongClave() + " carácter(es).", true);
                return;
            }
        }

        String finalClave = clave;
        startSearchAnimation(finalClave, index -> {
            setStatus("La clave ya existe en la posición " + (index + 1) + ".", true);
        }, () -> {
            try {
                secuencial.insertar(finalClave);
                refreshTable();
                updateControlsState();
                highlightPosition(secuencial.buscar(finalClave), HIGHLIGHT_FOUND_COLOR);
                setStatus("Clave insertada.", false);
            } catch (RuntimeException ex) {
                setStatus(ex.getMessage(), true);
            }
        }, false);
    }

    private void handleSearch() {
        if (!canRunAction()) {
            return;
        }

        String clave = keyField.getText().trim();
        keyField.setText("");

        if (clave.isEmpty()) {
            setStatus("Ingrese una clave.", true);
            return;
        }

        if (secuencial.getContador() == 0) {
            setStatus("La tabla está vacía.", true);
            return;
        }

        // Validar longitud y rellenar con ceros si es necesario (solo para números)
        if (clave.length() > secuencial.getLongClave()) {
            setStatus("La clave no puede exceder " + secuencial.getLongClave() + " carácter(es).", true);
            return;
        }

        if (clave.length() < secuencial.getLongClave()) {
            try {
                clave = String.format("%0" + secuencial.getLongClave() + "d", Long.parseLong(clave));
            } catch (NumberFormatException e) {
                // Si no es numérica, requiere la longitud exacta
                setStatus("La clave debe tener exactamente " + secuencial.getLongClave() + " carácter(es).", true);
                return;
            }
        }

        String finalClave = clave;
        startSearchAnimation(finalClave, index -> {
            setStatus("Clave encontrada en la posición " + (index + 1) + ".", false);
        }, () -> {
            setStatus("No se encontró la clave.", true);
        }, true);
    }

    private void handleDelete() {
        if (!canRunAction()) {
            return;
        }

        String clave = keyField.getText().trim();
        keyField.setText("");

        if (clave.isEmpty()) {
            setStatus("Ingrese una clave.", true);
            return;
        }

        if (secuencial.getContador() == 0) {
            setStatus("La tabla está vacía.", true);
            return;
        }

        // Validar longitud y rellenar con ceros si es necesario (solo para números)
        if (clave.length() > secuencial.getLongClave()) {
            setStatus("La clave no puede exceder " + secuencial.getLongClave() + " carácter(es).", true);
            return;
        }

        if (clave.length() < secuencial.getLongClave()) {
            try {
                clave = String.format("%0" + secuencial.getLongClave() + "d", Long.parseLong(clave));
            } catch (NumberFormatException e) {
                // Si no es numérica, requiere la longitud exacta
                setStatus("La clave debe tener exactamente " + secuencial.getLongClave() + " carácter(es).", true);
                return;
            }
        }

        String finalClave = clave;
        startSearchAnimation(finalClave, index -> {
            try {
                secuencial.eliminar(finalClave);
                refreshTable();
                updateControlsState();
                setStatus("Clave eliminada.", false);
            } catch (RuntimeException ex) {
                setStatus(ex.getMessage(), true);
            }
        }, () -> {
            setStatus("No se encontró la clave para eliminar.", true);
        }, true);
    }

    private void handleRestart() {
        if (animationRunning) {
            return;
        }

        secuencial = null;
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
            setStatus("Primero cree la tabla.", true);
            return false;
        }
        return true;
    }

    private void startSearchAnimation(String clave, java.util.function.IntConsumer onFound,
            Runnable onNotFound, boolean showNotFoundMessage) {
        if (animationRunning) {
            return;
        }

        int count = secuencial.getContador();
        if (count == 0) {
            if (showNotFoundMessage) {
                setStatus("No se encontró la clave.", true);
            }
            onNotFound.run();
            return;
        }

        animationRunning = true;
        setActionButtonsEnabled(false);
        clearHighlights();

        final int[] index = {0};
        animationTimer = new Timer(320, null);
        animationTimer.addActionListener(e -> {
            if (index[0] >= count) {
                animationTimer.stop();
                animationRunning = false;
                setActionButtonsEnabled(true);
                if (showNotFoundMessage) {
                    setStatus("No se encontró la clave.", true);
                }
                onNotFound.run();
                return;
            }

            clearHighlights();
            RowPanel row = rowPanels.get(index[0]);
            row.setHighlighted(HIGHLIGHT_COLOR);
            if (row.getClave().equals(clave)) {
                row.setHighlighted(HIGHLIGHT_FOUND_COLOR);
                animationTimer.stop();
                animationRunning = false;
                setActionButtonsEnabled(true);
                onFound.accept(index[0]);
                return;
            }
            index[0]++;
        });
        animationTimer.start();
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

        tablePanel.revalidate();
        tablePanel.repaint();
    }

    private void updateControlsState() {
        boolean full = secuencial != null && secuencial.estaLleno();
        insertButton.setEnabled(!full);
        fullLabel.setVisible(full);
        insertButton.setBackground(full ? BUTTON_COLOR : BUTTON_COLOR);
    }

    private void setActionButtonsEnabled(boolean enabled) {
        insertButton.setEnabled(enabled && (secuencial == null || !secuencial.estaLleno()));
        searchButton.setEnabled(enabled);
        deleteButton.setEnabled(enabled);
        restartButton.setEnabled(enabled);
    }

    private void clearHighlights() {
        for (RowPanel row : rowPanels) {
            row.setHighlighted(null);
        }
    }

    private void highlightPosition(int index, Color color) {
        if (index < 0 || index >= rowPanels.size()) {
            return;
        }
        rowPanels.get(index).setHighlighted(color);
    }

    private void setStatus(String message, boolean error) {
        statusLabel.setText(message);
        statusLabel.setForeground(error ? new Color(150, 70, 70) : TEXT_PRIMARY);
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

        void setHighlighted(Color color) {
            setBackground(color == null ? baseColor : color);
            repaint();
        }
    }
}
