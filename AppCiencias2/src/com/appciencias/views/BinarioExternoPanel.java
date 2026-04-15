package com.appciencias.views;

import com.appciencias.algorithms.BinarioExterno;
import com.appciencias.algorithms.BinarioExterno.ResultadoBusqueda;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.UIManager;

/**
 * Panel interactivo para búsqueda binaria externa con visualización por bloques.
 */
public class BinarioExternoPanel extends JPanel {

    private final Color backgroundColor = UIManager.getColor("Panel.background") != null
            ? UIManager.getColor("Panel.background")
            : new Color(245, 245, 250);
    private final Color panelColor = UIManager.getColor("Panel.background") != null
            ? UIManager.getColor("Panel.background")
            : new Color(235, 235, 245);
    private final Color textPrimary = UIManager.getColor("Label.foreground") != null
            ? UIManager.getColor("Label.foreground")
            : new Color(70, 70, 80);
    private final Color borderColor = UIManager.getColor("Separator.foreground") != null
            ? UIManager.getColor("Separator.foreground")
            : new Color(200, 200, 210);

    private BinarioExterno binarioExterno;

    private JTextField nField;
    private JTextField longClaveField;
    private JTextField claveField;

    private JTextArea resultadosArea;
    private BloquesDrawPanel bloquesDrawPanel;

    private int highlightedBlock = -1;
    private int highlightedPos = -1;

    public BinarioExternoPanel() {
        initializePanel();
    }

    private void initializePanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(backgroundColor);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createResultsPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(backgroundColor);

        JLabel titleLabel = new JLabel("Búsqueda Binaria Externa");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(textPrimary);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(titleLabel);
        header.add(Box.createVerticalStrut(8));

        JTextArea description = new JTextArea(
                "Simula búsqueda binaria sobre almacenamiento externo. "
                + "Los registros se distribuyen en bloques ordenados y se usa el pivote "
                + "(último elemento del bloque) para decidir si se entra al bloque o se salta.");
        description.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        description.setForeground(textPrimary);
        description.setBackground(backgroundColor);
        description.setEditable(false);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(description);

        return header;
    }

    private JPanel createCenterPanel() {
        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setBackground(backgroundColor);

        JPanel fixedTop = new JPanel();
        fixedTop.setLayout(new BoxLayout(fixedTop, BoxLayout.Y_AXIS));
        fixedTop.setBackground(panelColor);
        fixedTop.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JPanel configPanel = createConfigPanel();
        configPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        fixedTop.add(configPanel);
        fixedTop.add(Box.createVerticalStrut(10));

        JPanel actionsPanel = createActionsPanel();
        actionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        fixedTop.add(actionsPanel);

        Dimension topPref = fixedTop.getPreferredSize();
        fixedTop.setMaximumSize(new Dimension(Integer.MAX_VALUE, topPref.height));

        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setBackground(backgroundColor);
        topWrapper.add(fixedTop, BorderLayout.NORTH);

        center.add(topWrapper, BorderLayout.NORTH);

        bloquesDrawPanel = new BloquesDrawPanel();
        JScrollPane drawScroll = new JScrollPane(bloquesDrawPanel);
        drawScroll.setBorder(BorderFactory.createLineBorder(borderColor, 1));
        drawScroll.getViewport().setBackground(Color.WHITE);
        drawScroll.setBackground(Color.WHITE);
        drawScroll.getVerticalScrollBar().setUnitIncrement(16);
        drawScroll.getHorizontalScrollBar().setUnitIncrement(16);

        center.add(drawScroll, BorderLayout.CENTER);

        return center;
    }

    private JPanel createConfigPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(panelColor);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel nLabel = new JLabel("N (capacidad total)");
        nLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        nLabel.setForeground(textPrimary);

        JLabel longClaveLabel = new JLabel("Longitud de clave");
        longClaveLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        longClaveLabel.setForeground(textPrimary);

        nField = createTextField(8);
        longClaveField = createTextField(8);

        JButton inicializarButton = createButton("Inicializar");
        inicializarButton.addActionListener(e -> handleInicializar());

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(nLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(nField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        panel.add(Box.createHorizontalStrut(8), gbc);

        gbc.gridx = 3;
        panel.add(longClaveLabel, gbc);

        gbc.gridx = 4;
        gbc.weightx = 1.0;
        panel.add(longClaveField, gbc);

        gbc.gridx = 5;
        gbc.weightx = 0.0;
        panel.add(Box.createHorizontalStrut(8), gbc);

        gbc.gridx = 6;
        panel.add(inicializarButton, gbc);

        return panel;
    }

    private JPanel createActionsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(panelColor);

        JPanel keyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        keyPanel.setBackground(panelColor);

        JLabel claveLabel = new JLabel("Clave");
        claveLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        claveLabel.setForeground(textPrimary);

        claveField = createTextField(16);

        keyPanel.add(claveLabel);
        keyPanel.add(claveField);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonsPanel.setBackground(panelColor);

        JButton insertarButton = createButton("Insertar");
        JButton buscarButton = createButton("Buscar");
        JButton eliminarButton = createButton("Eliminar");

        insertarButton.addActionListener(e -> handleInsertar());
        buscarButton.addActionListener(e -> handleBuscar());
        eliminarButton.addActionListener(e -> handleEliminar());

        buttonsPanel.add(insertarButton);
        buttonsPanel.add(buscarButton);
        buttonsPanel.add(eliminarButton);

        panel.add(keyPanel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(buttonsPanel);

        return panel;
    }

    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(backgroundColor);

        JLabel title = new JLabel("Resultados");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(textPrimary);
        panel.add(title, BorderLayout.NORTH);

        resultadosArea = new JTextArea(4, 30);
        resultadosArea.setEditable(false);
        resultadosArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        resultadosArea.setLineWrap(true);
        resultadosArea.setWrapStyleWord(true);
        resultadosArea.setBackground(UIManager.getColor("TextArea.background") != null
                ? UIManager.getColor("TextArea.background")
                : Color.WHITE);
        resultadosArea.setForeground(textPrimary);
        resultadosArea.setText("Inicializa la estructura para comenzar.");

        JScrollPane resultScroll = new JScrollPane(resultadosArea);
        resultScroll.setBorder(BorderFactory.createLineBorder(borderColor, 1));
        resultScroll.setPreferredSize(new Dimension(100, 110));
        resultScroll.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(resultScroll, BorderLayout.CENTER);

        return panel;
    }

    private JTextField createTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        Dimension preferred = field.getPreferredSize();
        field.setMaximumSize(new Dimension(260, preferred.height));
        return field;
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return button;
    }

    private void handleInicializar() {
        int n;
        int longClave;

        try {
            n = Integer.parseInt(nField.getText().trim());
            longClave = Integer.parseInt(longClaveField.getText().trim());
        } catch (NumberFormatException ex) {
            showError("N y Longitud de Clave deben ser números enteros.");
            return;
        }

        if (n <= 0 || longClave <= 0) {
            showError("N y Longitud de Clave deben ser mayores que 0.");
            return;
        }

        try {
            binarioExterno = new BinarioExterno(n, longClave);
            highlightedBlock = -1;
            highlightedPos = -1;
            updateResultados("Estructura inicializada correctamente.\n" + binarioExterno.obtenerInfo());
            showInfo("Estructura inicializada correctamente.");
            refreshDrawPanel();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void handleInsertar() {
        if (!ensureInicializado()) {
            return;
        }

        String clave = claveField.getText().trim();
        if (clave.isEmpty()) {
            showError("Ingresa una clave para insertar.");
            return;
        }

        try {
            binarioExterno.insertar(clave);
            highlightedBlock = -1;
            highlightedPos = -1;
            updateResultados("Clave insertada: " + clave + "\n" + binarioExterno.obtenerInfo());
            showInfo("Clave insertada correctamente.");
            claveField.setText("");
            refreshDrawPanel();
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void handleBuscar() {
        if (!ensureInicializado()) {
            return;
        }

        String clave = claveField.getText().trim();
        if (clave.isEmpty()) {
            showError("Ingresa una clave para buscar.");
            return;
        }

        ResultadoBusqueda resultado = binarioExterno.buscar(clave);

        if (resultado.encontrada) {
            highlightedBlock = resultado.numBloque - 1;
            highlightedPos = resultado.posEnBloque - 1;
            showInfo("Clave encontrada en bloque " + resultado.numBloque + ", posición " + resultado.posEnBloque + ".");
        } else {
            highlightedBlock = -1;
            highlightedPos = -1;
            showInfo("La clave no fue encontrada.");
        }

        int bloquesSaltados = Math.max(0, binarioExterno.getNumBloques() - resultado.bloquesVisitados);
        String detalle = "Bloques saltados por orden: " + bloquesSaltados
                + " de " + binarioExterno.getNumBloques() + ".";

        updateResultados(binarioExterno.obtenerInfo() + "\n" + resultado + "\n" + detalle);
        claveField.setText("");
        refreshDrawPanel();
    }

    private void handleEliminar() {
        if (!ensureInicializado()) {
            return;
        }

        String clave = claveField.getText().trim();
        if (clave.isEmpty()) {
            showError("Ingresa una clave para eliminar.");
            return;
        }

        try {
            binarioExterno.eliminar(clave);
            highlightedBlock = -1;
            highlightedPos = -1;
            updateResultados("Clave eliminada: " + clave + "\n" + binarioExterno.obtenerInfo());
            showInfo("Clave eliminada correctamente.");
            claveField.setText("");
            refreshDrawPanel();
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private boolean ensureInicializado() {
        if (binarioExterno != null) {
            return true;
        }
        showError("Primero inicializa la estructura.");
        return false;
    }

    private void refreshDrawPanel() {
        bloquesDrawPanel.revalidate();
        bloquesDrawPanel.repaint();
        revalidate();
        repaint();
    }

    private void updateResultados(String texto) {
        resultadosArea.setText(texto);
        resultadosArea.setCaretPosition(0);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    private class BloquesDrawPanel extends JPanel {

        private static final int OUTER_PADDING = 18;
        private static final int BLOCK_GAP_X = 16;
        private static final int BLOCK_GAP_Y = 18;
        private static final int BLOCK_WIDTH = 220;
        private static final int BLOCK_HEADER_HEIGHT = 28;
        private static final int CELL_HEIGHT = 24;
        private static final int CELL_GAP = 4;
        private static final int INNER_PADDING = 8;

        BloquesDrawPanel() {
            setOpaque(true);
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            ArrayList<ArrayList<String>> bloques = binarioExterno != null
                    ? binarioExterno.obtenerBloques()
                    : new ArrayList<>();

            if (bloques.isEmpty()) {
                g2.setColor(textPrimary);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                g2.drawString("Sin bloques para mostrar. Inicializa e inserta claves.", OUTER_PADDING, OUTER_PADDING + 20);
                g2.dispose();
                return;
            }

            int slotsPorBloque = Math.max(1, binarioExterno.getTB());
            int blockHeight = calculateBlockHeight(slotsPorBloque);
            LayoutInfo layout = calculateLayout(getWidth(), bloques.size(), blockHeight);

            for (int i = 0; i < bloques.size(); i++) {
                int row = i / layout.columns;
                int col = i % layout.columns;

                int x = OUTER_PADDING + col * (BLOCK_WIDTH + BLOCK_GAP_X);
                int y = OUTER_PADDING + row * (blockHeight + BLOCK_GAP_Y);

                drawBlock(g2, i, x, y, BLOCK_WIDTH, blockHeight, bloques.get(i), slotsPorBloque);
            }

            g2.dispose();
        }

        private void drawBlock(Graphics2D g2, int blockIndex, int x, int y, int width, int height,
                ArrayList<String> claves, int slotsPorBloque) {
            Color blockFill = new Color(248, 248, 252);
            Color blockStroke = borderColor;

            g2.setColor(blockFill);
            g2.fillRoundRect(x, y, width, height, 12, 12);
            g2.setColor(blockStroke);
            g2.drawRoundRect(x, y, width, height, 12, 12);

            g2.setColor(textPrimary);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g2.drawString("Bloque " + (blockIndex + 1), x + INNER_PADDING, y + 18);

            int cellY = y + BLOCK_HEADER_HEIGHT;
            for (int pos = 0; pos < slotsPorBloque; pos++) {
                int currentY = cellY + pos * (CELL_HEIGHT + CELL_GAP);

                boolean filled = pos < claves.size();
                boolean isPivot = filled && pos == claves.size() - 1;
                boolean highlighted = blockIndex == highlightedBlock && pos == highlightedPos;

                if (highlighted) {
                    g2.setColor(new Color(210, 235, 210));
                } else if (isPivot) {
                    g2.setColor(new Color(224, 238, 255));
                } else if (filled) {
                    g2.setColor(new Color(236, 236, 243));
                } else {
                    g2.setColor(new Color(247, 247, 250));
                }

                g2.fillRoundRect(x + INNER_PADDING, currentY, width - (INNER_PADDING * 2), CELL_HEIGHT, 8, 8);

                if (isPivot && !highlighted) {
                    g2.setColor(new Color(90, 125, 180));
                } else {
                    g2.setColor(borderColor);
                }
                g2.drawRoundRect(x + INNER_PADDING, currentY, width - (INNER_PADDING * 2), CELL_HEIGHT, 8, 8);

                g2.setColor(textPrimary);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                String text = filled ? claves.get(pos) : "(vacío)";
                g2.drawString(text, x + INNER_PADDING + 8, currentY + 16);
            }
        }

        @Override
        public Dimension getPreferredSize() {
            int bloques = binarioExterno != null ? binarioExterno.getNumBloques() : 1;
            int slotsPorBloque = binarioExterno != null ? Math.max(1, binarioExterno.getTB()) : 1;
            int blockHeight = calculateBlockHeight(slotsPorBloque);

            int availableWidth = getParentAvailableWidth();
            LayoutInfo layout = calculateLayout(availableWidth, bloques, blockHeight);

            return new Dimension(layout.totalWidth, layout.totalHeight);
        }

        private int getParentAvailableWidth() {
            if (getParent() instanceof JViewport) {
                JViewport viewport = (JViewport) getParent();
                int extent = viewport.getExtentSize().width;
                if (extent > 0) {
                    return extent;
                }
            }

            int width = getWidth();
            if (width > 0) {
                return width;
            }

            return 900;
        }

        private int calculateBlockHeight(int slotsPorBloque) {
            return BLOCK_HEADER_HEIGHT + (slotsPorBloque * CELL_HEIGHT) + ((slotsPorBloque - 1) * CELL_GAP) + INNER_PADDING + 10;
        }

        private LayoutInfo calculateLayout(int availableWidth, int blockCount, int blockHeight) {
            int usableWidth = Math.max(BLOCK_WIDTH, availableWidth - (OUTER_PADDING * 2));
            int stepWidth = BLOCK_WIDTH + BLOCK_GAP_X;
            int columns = Math.max(1, (usableWidth + BLOCK_GAP_X) / stepWidth);
            int rows = (int) Math.ceil(blockCount / (double) columns);

            int contentWidth = OUTER_PADDING * 2 + (columns * BLOCK_WIDTH) + ((columns - 1) * BLOCK_GAP_X);
            int contentHeight = OUTER_PADDING * 2 + (rows * blockHeight) + ((rows - 1) * BLOCK_GAP_Y);

            int totalWidth = Math.max(contentWidth, availableWidth);
            int totalHeight = Math.max(contentHeight, 220);

            return new LayoutInfo(columns, totalWidth, totalHeight);
        }

        private class LayoutInfo {

            private final int columns;
            private final int totalWidth;
            private final int totalHeight;

            private LayoutInfo(int columns, int totalWidth, int totalHeight) {
                this.columns = columns;
                this.totalWidth = totalWidth;
                this.totalHeight = totalHeight;
            }
        }
    }
}
