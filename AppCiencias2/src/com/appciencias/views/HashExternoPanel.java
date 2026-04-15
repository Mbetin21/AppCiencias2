package com.appciencias.views;

import com.appciencias.algorithms.HashExterno;
import com.appciencias.algorithms.HashExterno.Bloque;
import com.appciencias.algorithms.HashExterno.ResultadoBusqueda;
import java.awt.BasicStroke;
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
import java.awt.Stroke;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JScrollBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.JViewport;

/**
 * Panel interactivo para búsqueda por hash externo con encadenamiento de desbordamiento.
 */
public class HashExternoPanel extends JPanel {

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

    private HashExterno hashExterno;

    private JComboBox<HashExterno.Tipo> tipoCombo;
    private JScrollBar metodoHashScrollBar;
    private JTextField nField;
    private JTextField cField;
    private JTextField longClaveField;
    private JTextField baseField;
    private JLabel baseLabel;
    private JTextField claveField;

    private JTextArea resultadosArea;
    private BloquesDrawPanel bloquesDrawPanel;

    private ResultadoBusqueda ultimaBusqueda;
    private int[] posicionesTruncamiento;

    public HashExternoPanel() {
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

        JLabel titleLabel = new JLabel("Búsqueda por Hash Externo");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(textPrimary);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(titleLabel);
        header.add(Box.createVerticalStrut(8));

        JTextArea description = new JTextArea(
                "Las claves se transforman en una dirección base (cubeta). "
                + "Si el bloque de disco se llena, se encadenan bloques de desbordamiento.");
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

        JLabel tipoLabel = new JLabel("Método Hash");
        tipoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tipoLabel.setForeground(textPrimary);

        JLabel nLabel = new JLabel("N");
        nLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        nLabel.setForeground(textPrimary);

        JLabel cLabel = new JLabel("Capacidad c");
        cLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cLabel.setForeground(textPrimary);

        JLabel longClaveLabel = new JLabel("Longitud clave");
        longClaveLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        longClaveLabel.setForeground(textPrimary);

        baseLabel = new JLabel("Base");
        baseLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        baseLabel.setForeground(textPrimary);

        tipoCombo = new JComboBox<>(HashExterno.Tipo.values());
        tipoCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tipoCombo.setMaximumRowCount(4);

        metodoHashScrollBar = new JScrollBar(
                JScrollBar.HORIZONTAL,
                tipoCombo.getSelectedIndex(),
                1,
                0,
                tipoCombo.getItemCount()
        );
        metodoHashScrollBar.setUnitIncrement(1);
        metodoHashScrollBar.setBlockIncrement(1);
        metodoHashScrollBar.setPreferredSize(new Dimension(210, 14));
        metodoHashScrollBar.addAdjustmentListener(e -> {
            int index = Math.max(0, Math.min(tipoCombo.getItemCount() - 1, e.getValue()));
            if (tipoCombo.getSelectedIndex() != index) {
                tipoCombo.setSelectedIndex(index);
            }
        });

        JPanel tipoSelectorPanel = new JPanel();
        tipoSelectorPanel.setLayout(new BoxLayout(tipoSelectorPanel, BoxLayout.Y_AXIS));
        tipoSelectorPanel.setBackground(panelColor);
        tipoSelectorPanel.add(tipoCombo);
        tipoSelectorPanel.add(Box.createVerticalStrut(4));
        tipoSelectorPanel.add(metodoHashScrollBar);

        nField = createTextField(6);
        cField = createTextField(6);
        longClaveField = createTextField(6);
        baseField = createTextField(6);
        baseField.setText("8");

        tipoCombo.addActionListener(e -> {
            int idx = tipoCombo.getSelectedIndex();
            if (metodoHashScrollBar.getValue() != idx) {
                metodoHashScrollBar.setValue(idx);
            }
            updateBaseInputState();
        });

        JButton inicializarButton = createButton("Inicializar");
        inicializarButton.addActionListener(e -> handleInicializar());

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        panel.add(tipoLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(tipoSelectorPanel, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        panel.add(Box.createHorizontalStrut(8), gbc);

        gbc.gridx = 3;
        panel.add(nLabel, gbc);

        gbc.gridx = 4;
        gbc.weightx = 0.7;
        panel.add(nField, gbc);

        gbc.gridx = 5;
        gbc.weightx = 0.0;
        panel.add(cLabel, gbc);

        gbc.gridx = 6;
        gbc.weightx = 0.7;
        panel.add(cField, gbc);

        gbc.gridx = 7;
        gbc.weightx = 0.0;
        panel.add(longClaveLabel, gbc);

        gbc.gridx = 8;
        gbc.weightx = 0.7;
        panel.add(longClaveField, gbc);

        gbc.gridx = 9;
        gbc.weightx = 0.0;
        panel.add(baseLabel, gbc);

        gbc.gridx = 10;
        gbc.weightx = 0.7;
        panel.add(baseField, gbc);

        gbc.gridx = 11;
        gbc.weightx = 0.0;
        panel.add(Box.createHorizontalStrut(8), gbc);

        gbc.gridx = 12;
        panel.add(inicializarButton, gbc);

        updateBaseInputState();

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

    private void updateBaseInputState() {
        HashExterno.Tipo tipo = (HashExterno.Tipo) tipoCombo.getSelectedItem();
        boolean usarBase = tipo == HashExterno.Tipo.CONVERSION_BASE;

        baseField.setEnabled(usarBase);
        baseLabel.setEnabled(usarBase);
        baseField.setBackground(usarBase ? Color.WHITE : new Color(240, 240, 240));
    }

    private void handleInicializar() {
        int n;
        int c;
        int longClave;

        try {
            n = Integer.parseInt(nField.getText().trim());
            c = Integer.parseInt(cField.getText().trim());
            longClave = Integer.parseInt(longClaveField.getText().trim());
        } catch (NumberFormatException ex) {
            showError("N, c y Longitud de Clave deben ser números enteros.");
            return;
        }

        if (n <= 0 || c <= 0 || longClave <= 0) {
            showError("N, c y Longitud de Clave deben ser mayores que 0.");
            return;
        }

        HashExterno.Tipo tipo = (HashExterno.Tipo) tipoCombo.getSelectedItem();

        try {
            posicionesTruncamiento = null;

            if (tipo == HashExterno.Tipo.CONVERSION_BASE) {
                int base;
                try {
                    base = Integer.parseInt(baseField.getText().trim());
                } catch (NumberFormatException ex) {
                    showError("La base debe ser un número entero.");
                    return;
                }

                if (base < 2) {
                    showError("La base debe ser mayor o igual a 2.");
                    return;
                }

                hashExterno = new HashExterno(n, c, longClave, base);
            } else if (tipo == HashExterno.Tipo.TRUNCAMIENTO) {
                posicionesTruncamiento = buildDefaultTruncamientoPositions(longClave);
                hashExterno = new HashExterno(n, c, longClave, posicionesTruncamiento);
            } else {
                hashExterno = new HashExterno(n, c, longClave, tipo);
            }

            ultimaBusqueda = null;
            updateResultados(buildInitMessage());
            showInfo("Estructura inicializada correctamente.");
            refreshDrawPanel();
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private int[] buildDefaultTruncamientoPositions(int longClave) {
        if (longClave <= 1) {
            return new int[]{1};
        }
        if (longClave == 2) {
            return new int[]{1, 2};
        }
        return new int[]{1, (longClave + 1) / 2, longClave};
    }

    private String buildInitMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("Estructura inicializada correctamente.\n");
        sb.append(hashExterno.obtenerInfo());

        if (hashExterno.getTipo() == HashExterno.Tipo.TRUNCAMIENTO && posicionesTruncamiento != null) {
            sb.append("\nPosiciones de truncamiento usadas: ");
            for (int i = 0; i < posicionesTruncamiento.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(posicionesTruncamiento[i]);
            }
        }

        return sb.toString();
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
            hashExterno.insertar(clave);
            ultimaBusqueda = null;
            updateResultados("Clave insertada: " + clave + "\n" + hashExterno.obtenerInfo());
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

        ultimaBusqueda = hashExterno.buscar(clave);

        if (ultimaBusqueda.encontrada) {
            if (ultimaBusqueda.enDesbordamiento) {
                showInfo("Clave encontrada en desbordamiento del bloque base "
                        + ultimaBusqueda.bloqueBase + ".");
            } else {
                showInfo("Clave encontrada en bloque primario " + ultimaBusqueda.bloqueEncontrado + ".");
            }
        } else {
            showInfo("La clave no fue encontrada.");
        }

        int primariosVisitados = ultimaBusqueda.bloquesVisitados > 0 ? 1 : 0;
        int desbordamientosVisitados = Math.max(0, ultimaBusqueda.bloquesVisitados - primariosVisitados);

        String detalleVisita = "Bloques visitados en la última búsqueda: "
                + "primarios=" + primariosVisitados
                + ", desbordamientos=" + desbordamientosVisitados
                + ", total=" + ultimaBusqueda.bloquesVisitados + ".";

        updateResultados(hashExterno.obtenerInfo() + "\n" + ultimaBusqueda + "\n" + detalleVisita);
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
            hashExterno.eliminar(clave);
            ultimaBusqueda = null;
            updateResultados("Clave eliminada: " + clave + "\n" + hashExterno.obtenerInfo());
            showInfo("Clave eliminada correctamente.");
            claveField.setText("");
            refreshDrawPanel();
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private boolean ensureInicializado() {
        if (hashExterno != null) {
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
        private static final int ROW_GAP = 18;
        private static final int LABEL_WIDTH = 110;
        private static final int BLOCK_WIDTH = 220;
        private static final int BLOCK_HEADER_HEIGHT = 28;
        private static final int CELL_HEIGHT = 24;
        private static final int CELL_GAP = 4;
        private static final int INNER_PADDING = 8;
        private static final int CHAIN_GAP = 38;

        BloquesDrawPanel() {
            setOpaque(true);
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            ArrayList<Bloque> primarios = hashExterno != null
                    ? hashExterno.obtenerBloquesPrimarios()
                    : new ArrayList<>();

            if (primarios.isEmpty()) {
                g2.setColor(textPrimary);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                g2.drawString("Sin cubetas para mostrar. Inicializa e inserta claves.", OUTER_PADDING, OUTER_PADDING + 20);
                g2.dispose();
                return;
            }

            int slotsPorBloque = Math.max(1, hashExterno.getC());
            int blockHeight = calculateBlockHeight(slotsPorBloque);

            for (int i = 0; i < primarios.size(); i++) {
                int y = OUTER_PADDING + i * (blockHeight + ROW_GAP);

                drawPrimaryLabel(g2, i + 1, OUTER_PADDING, y, blockHeight);

                int x = OUTER_PADDING + LABEL_WIDTH;
                Bloque current = primarios.get(i);
                Bloque previous = null;

                while (current != null) {
                    if (previous != null) {
                        drawArrow(g2, x - CHAIN_GAP + 6, y + (blockHeight / 2), x - 10, y + (blockHeight / 2));
                    }

                    drawBlock(g2, current, x, y, BLOCK_WIDTH, blockHeight, slotsPorBloque, i + 1);

                    previous = current;
                    current = current.desbordamiento;
                    x += BLOCK_WIDTH + CHAIN_GAP;
                }
            }

            g2.dispose();
        }

        private void drawPrimaryLabel(Graphics2D g2, int bucket, int x, int y, int blockHeight) {
            g2.setColor(textPrimary);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g2.drawString("H(k) = " + bucket, x, y + (blockHeight / 2) + 5);
        }

        private void drawArrow(Graphics2D g2, int x1, int y1, int x2, int y2) {
            Stroke oldStroke = g2.getStroke();
            g2.setColor(new Color(130, 130, 145));
            g2.setStroke(new BasicStroke(1.5f));

            g2.drawLine(x1, y1, x2, y2);
            g2.drawLine(x2 - 7, y2 - 5, x2, y2);
            g2.drawLine(x2 - 7, y2 + 5, x2, y2);

            g2.setStroke(oldStroke);
        }

        private void drawBlock(Graphics2D g2, Bloque bloque, int x, int y, int width, int height,
                int slotsPorBloque, int bucketIndex) {
            boolean isBase = ultimaBusqueda != null && ultimaBusqueda.bloqueBase == bucketIndex;
            boolean isFound = ultimaBusqueda != null
                    && ultimaBusqueda.encontrada
                    && ultimaBusqueda.bloqueEncontrado == bloque.numero;

            Color blockFill;
            if (isFound) {
                blockFill = new Color(214, 236, 214);
            } else if (isBase) {
                blockFill = new Color(228, 238, 252);
            } else if (!bloque.esPrimario) {
                blockFill = new Color(248, 244, 233);
            } else {
                blockFill = new Color(248, 248, 252);
            }

            g2.setColor(blockFill);
            g2.fillRoundRect(x, y, width, height, 12, 12);
            g2.setColor(borderColor);
            g2.drawRoundRect(x, y, width, height, 12, 12);

            g2.setColor(textPrimary);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            String title = bloque.esPrimario ? "Bloque " + bloque.numero : "Desb " + bloque.numero;
            g2.drawString(title, x + INNER_PADDING, y + 18);

            int cellY = y + BLOCK_HEADER_HEIGHT;
            for (int pos = 0; pos < slotsPorBloque; pos++) {
                int currentY = cellY + pos * (CELL_HEIGHT + CELL_GAP);

                boolean filled = pos < bloque.claves.size();

                if (isFound && filled && ultimaBusqueda.posEnBloque == pos + 1) {
                    g2.setColor(new Color(192, 226, 192));
                } else if (filled) {
                    g2.setColor(new Color(236, 236, 243));
                } else {
                    g2.setColor(new Color(247, 247, 250));
                }

                g2.fillRoundRect(x + INNER_PADDING, currentY, width - (INNER_PADDING * 2), CELL_HEIGHT, 8, 8);
                g2.setColor(borderColor);
                g2.drawRoundRect(x + INNER_PADDING, currentY, width - (INNER_PADDING * 2), CELL_HEIGHT, 8, 8);

                g2.setColor(textPrimary);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                String text = filled ? bloque.claves.get(pos) : "(vacío)";
                g2.drawString(text, x + INNER_PADDING + 8, currentY + 16);
            }
        }

        @Override
        public Dimension getPreferredSize() {
            if (hashExterno == null) {
                return new Dimension(900, 260);
            }

            ArrayList<Bloque> primarios = hashExterno.obtenerBloquesPrimarios();
            int slotsPorBloque = Math.max(1, hashExterno.getC());
            int blockHeight = calculateBlockHeight(slotsPorBloque);

            int maxChainLength = 1;
            for (Bloque primario : primarios) {
                int chainLength = 0;
                Bloque current = primario;
                while (current != null) {
                    chainLength++;
                    current = current.desbordamiento;
                }
                maxChainLength = Math.max(maxChainLength, chainLength);
            }

            int contentWidth = OUTER_PADDING * 2
                    + LABEL_WIDTH
                    + (maxChainLength * BLOCK_WIDTH)
                    + ((maxChainLength - 1) * CHAIN_GAP)
                    + 20;

            int rows = Math.max(1, primarios.size());
            int contentHeight = OUTER_PADDING * 2
                    + (rows * blockHeight)
                    + ((rows - 1) * ROW_GAP);

            int totalWidth = Math.max(contentWidth, getParentAvailableWidth());
            int totalHeight = Math.max(contentHeight, getParentAvailableHeight());

            return new Dimension(totalWidth, totalHeight);
        }

        private int getParentAvailableWidth() {
            if (getParent() instanceof JViewport viewport) {
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

        private int getParentAvailableHeight() {
            if (getParent() instanceof JViewport viewport) {
                int extent = viewport.getExtentSize().height;
                if (extent > 0) {
                    return extent;
                }
            }

            int height = getHeight();
            if (height > 0) {
                return height;
            }

            return 320;
        }

        private int calculateBlockHeight(int slotsPorBloque) {
            return BLOCK_HEADER_HEIGHT + (slotsPorBloque * CELL_HEIGHT) + ((slotsPorBloque - 1) * CELL_GAP) + INNER_PADDING + 10;
        }
    }
}
