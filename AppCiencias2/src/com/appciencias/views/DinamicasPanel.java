package com.appciencias.views;

import com.appciencias.algorithms.Dinamicas;
import com.appciencias.algorithms.Dinamicas.Tipo;
import com.appciencias.algorithms.Dinamicas.UltimoEvento;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.UIManager;

/**
 * Panel interactivo para busquedas dinamicas con expansion/reduccion por umbral.
 */
public class DinamicasPanel extends JPanel {

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

    private Dinamicas dinamicas;

    private JTextField cubetasField;
    private JTextField regPorCubetaField;
    private JTextField umbralExpField;
    private JTextField umbralRedField;
    private JComboBox<Tipo> tipoExpCombo;
    private JComboBox<Tipo> tipoRedCombo;
    private JTextField claveField;

    private JTextArea resultadosArea;
    private BloquesDrawPanel bloquesDrawPanel;

    private int highlightedBucket = -1;
    private int highlightedPos = -1;

    public DinamicasPanel() {
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

        JLabel titleLabel = new JLabel("Búsquedas Dinámicas");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(textPrimary);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(titleLabel);
        header.add(Box.createVerticalStrut(8));

        JTextArea description = new JTextArea(
                "Hash dinámico con expansión y reducción de cubetas según la densidad de ocupación. "
                + "Las colisiones se visualizan dentro de su cubeta y disparan reorganizaciones cuando se superan los umbrales.");
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

        JLabel cubetasLabel = createLabel("Cubetas iniciales");
        JLabel regPorCubetaLabel = createLabel("Registros/cubeta");
        JLabel umbralExpLabel = createLabel("Umbral Expansión");
        JLabel umbralRedLabel = createLabel("Umbral Reducción");
        JLabel tipoExpLabel = createLabel("Tipo Expansión");
        JLabel tipoRedLabel = createLabel("Tipo Reducción");

        cubetasField = createTextField(5);
        cubetasField.setText("2");

        regPorCubetaField = createTextField(5);
        regPorCubetaField.setText("3");

        umbralExpField = createTextField(6);
        umbralExpField.setText("0.82");

        umbralRedField = createTextField(6);
        umbralRedField.setText("1.25");

        tipoExpCombo = createTipoCombo();
        tipoRedCombo = createTipoCombo();

        JButton inicializarButton = createButton("Inicializar");
        inicializarButton.addActionListener(e -> handleInicializar());

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        panel.add(cubetasLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.6;
        panel.add(cubetasField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        panel.add(regPorCubetaLabel, gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.6;
        panel.add(regPorCubetaField, gbc);

        gbc.gridx = 4;
        gbc.weightx = 0.0;
        panel.add(umbralExpLabel, gbc);

        gbc.gridx = 5;
        gbc.weightx = 0.7;
        panel.add(umbralExpField, gbc);

        gbc.gridx = 6;
        gbc.weightx = 0.0;
        panel.add(umbralRedLabel, gbc);

        gbc.gridx = 7;
        gbc.weightx = 0.7;
        panel.add(umbralRedField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        panel.add(tipoExpLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.6;
        panel.add(tipoExpCombo, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        panel.add(tipoRedLabel, gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.6;
        panel.add(tipoRedCombo, gbc);

        gbc.gridx = 4;
        gbc.weightx = 0.0;
        panel.add(Box.createHorizontalStrut(8), gbc);

        gbc.gridx = 5;
        gbc.gridwidth = 3;
        gbc.weightx = 0.0;
        panel.add(inicializarButton, gbc);

        return panel;
    }

    private JPanel createActionsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(panelColor);

        JPanel keyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        keyPanel.setBackground(panelColor);

        JLabel claveLabel = createLabel("Clave");
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

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(textPrimary);
        return label;
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

    private JComboBox<Tipo> createTipoCombo() {
        JComboBox<Tipo> combo = new JComboBox<>(Tipo.values());
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setMaximumRowCount(2);
        return combo;
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return button;
    }

    private void handleInicializar() {
        int numCubetas;
        int regPorCubeta;
        double umbralExp;
        double umbralRed;

        try {
            numCubetas = Integer.parseInt(cubetasField.getText().trim());
            regPorCubeta = Integer.parseInt(regPorCubetaField.getText().trim());
            umbralExp = Double.parseDouble(umbralExpField.getText().trim());
            umbralRed = Double.parseDouble(umbralRedField.getText().trim());
        } catch (NumberFormatException ex) {
            showError("Los valores de configuración deben ser numéricos.");
            return;
        }

        if (numCubetas <= 0 || regPorCubeta <= 0 || umbralExp <= 0 || umbralRed <= 0) {
            showError("Todos los valores deben ser mayores que 0.");
            return;
        }

        try {
            Tipo tipoExp = (Tipo) tipoExpCombo.getSelectedItem();
            Tipo tipoRed = (Tipo) tipoRedCombo.getSelectedItem();

            dinamicas = new Dinamicas(numCubetas, regPorCubeta, tipoExp, tipoRed, umbralExp, umbralRed);
            highlightedBucket = -1;
            highlightedPos = -1;

            updateResultados("Estructura dinámica inicializada correctamente.\n" + dinamicas.obtenerInfo());
            showInfo("Estructura inicializada correctamente.");
            refreshDrawPanel();
        } catch (RuntimeException ex) {
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
            dinamicas.limpiarUltimoEvento();
            dinamicas.insertar(clave);

            highlightedBucket = -1;
            highlightedPos = -1;

            StringBuilder out = new StringBuilder();
            out.append("Clave insertada: ").append(clave).append("\n");
            out.append(dinamicas.obtenerInfo());
            updateResultados(out.toString());

            notifyReorganizacionIfNeeded();
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

        int indiceCubeta = dinamicas.buscar(clave);
        if (indiceCubeta >= 0) {
            highlightedBucket = indiceCubeta;
            highlightedPos = findKeyPosition(indiceCubeta, clave);
            showInfo("Clave encontrada en cubeta " + (indiceCubeta + 1) + ".");
            updateResultados("Clave encontrada: " + clave + "\n" + dinamicas.obtenerInfo());
        } else {
            highlightedBucket = -1;
            highlightedPos = -1;
            showInfo("La clave no fue encontrada.");
            updateResultados("Clave no encontrada: " + clave + "\n" + dinamicas.obtenerInfo());
        }

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
            dinamicas.limpiarUltimoEvento();
            dinamicas.eliminar(clave);

            highlightedBucket = -1;
            highlightedPos = -1;

            StringBuilder out = new StringBuilder();
            out.append("Clave eliminada: ").append(clave).append("\n");
            out.append(dinamicas.obtenerInfo());
            updateResultados(out.toString());

            notifyReorganizacionIfNeeded();
            claveField.setText("");
            refreshDrawPanel();
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private int findKeyPosition(int indiceCubeta, String clave) {
        ArrayList<ArrayList<String>> cubetas = dinamicas.obtenerCubetas();
        if (indiceCubeta < 0 || indiceCubeta >= cubetas.size()) {
            return -1;
        }

        ArrayList<String> claves = cubetas.get(indiceCubeta);
        for (int i = 0; i < claves.size(); i++) {
            if (clave.equals(claves.get(i))) {
                return i;
            }
        }
        return -1;
    }

    private void notifyReorganizacionIfNeeded() {
        if (!dinamicas.huboReorganizacion()) {
            return;
        }

        UltimoEvento evento = dinamicas.getUltimoEvento();
        if (evento == null) {
            return;
        }

        String tipo = "EXPANSION".equalsIgnoreCase(evento.tipo) ? "Expansión" : "Reducción";
        String mensaje = tipo + ": de " + evento.cubetasAntes + " a " + evento.cubetasDespues + " cubetas"
                + "\nClave detonante: " + evento.claveDetonante
                + "\nDO: " + String.format("%.2f%%", evento.doQueDetono * 100);

        JOptionPane.showMessageDialog(this, mensaje, "Reorganización dinámica", JOptionPane.INFORMATION_MESSAGE);
    }

    private boolean ensureInicializado() {
        if (dinamicas != null) {
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
        private static final int BUCKET_GAP_Y = 14;
        private static final int HEADER_HEIGHT = 28;
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

            ArrayList<ArrayList<String>> cubetas = dinamicas != null
                    ? dinamicas.obtenerCubetas()
                    : new ArrayList<>();

            if (cubetas.isEmpty()) {
                g2.setColor(textPrimary);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                g2.drawString("Sin cubetas para mostrar. Inicializa e inserta claves.", OUTER_PADDING, OUTER_PADDING + 20);
                g2.dispose();
                return;
            }

            int regPorCubeta = Math.max(1, dinamicas.getRegPorCubeta());
            int cellWidth = calculateCellWidth(g2, cubetas);
            int bucketWidth = Math.max(280, cellWidth + (INNER_PADDING * 2));

            int y = OUTER_PADDING;
            for (int i = 0; i < cubetas.size(); i++) {
                ArrayList<String> claves = cubetas.get(i);
                int visibleRows = Math.max(regPorCubeta, claves.size());
                int bucketHeight = HEADER_HEIGHT + INNER_PADDING + (visibleRows * CELL_HEIGHT)
                        + ((visibleRows - 1) * CELL_GAP) + INNER_PADDING;

                drawBucket(g2, i, OUTER_PADDING, y, bucketWidth, bucketHeight, claves, regPorCubeta, cellWidth);
                y += bucketHeight + BUCKET_GAP_Y;
            }

            g2.dispose();
        }

        private int calculateCellWidth(Graphics2D g2, ArrayList<ArrayList<String>> cubetas) {
            Font cellFont = new Font("Segoe UI", Font.PLAIN, 12);
            FontMetrics fm = g2.getFontMetrics(cellFont);
            int max = fm.stringWidth("(vacío)");

            for (ArrayList<String> cubeta : cubetas) {
                for (String clave : cubeta) {
                    if (clave != null) {
                        max = Math.max(max, fm.stringWidth(clave));
                    }
                }
            }

            return Math.max(180, max + 24);
        }

        private void drawBucket(Graphics2D g2, int bucketIndex, int x, int y, int width, int height,
                ArrayList<String> claves, int regPorCubeta, int cellWidth) {
            g2.setColor(new Color(248, 248, 252));
            g2.fillRoundRect(x, y, width, height, 12, 12);
            g2.setColor(borderColor);
            g2.drawRoundRect(x, y, width, height, 12, 12);

            g2.setColor(textPrimary);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g2.drawString("Cubeta " + (bucketIndex + 1), x + INNER_PADDING, y + 18);

            int visibleRows = Math.max(regPorCubeta, claves.size());
            int cellX = x + INNER_PADDING;
            int cellY = y + HEADER_HEIGHT;

            for (int pos = 0; pos < visibleRows; pos++) {
                int rowY = cellY + pos * (CELL_HEIGHT + CELL_GAP);
                boolean filled = pos < claves.size();
                boolean overflow = pos >= regPorCubeta;
                boolean highlighted = bucketIndex == highlightedBucket && pos == highlightedPos;

                if (highlighted) {
                    g2.setColor(new Color(210, 235, 210));
                } else if (overflow && filled) {
                    g2.setColor(new Color(255, 225, 225));
                } else if (filled) {
                    g2.setColor(new Color(236, 236, 243));
                } else {
                    g2.setColor(new Color(247, 247, 250));
                }

                g2.fillRoundRect(cellX, rowY, cellWidth, CELL_HEIGHT, 8, 8);
                g2.setColor(borderColor);
                g2.drawRoundRect(cellX, rowY, cellWidth, CELL_HEIGHT, 8, 8);

                String text = filled ? claves.get(pos) : "(vacío)";
                if (overflow && filled) {
                    text = text + "  [colisión]";
                }

                g2.setColor(textPrimary);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2.drawString(text, cellX + 8, rowY + 16);
            }
        }

        @Override
        public Dimension getPreferredSize() {
            if (dinamicas == null) {
                return new Dimension(900, 220);
            }

            ArrayList<ArrayList<String>> cubetas = dinamicas.obtenerCubetas();
            int regPorCubeta = Math.max(1, dinamicas.getRegPorCubeta());

            Font cellFont = new Font("Segoe UI", Font.PLAIN, 12);
            FontMetrics fm = getFontMetrics(cellFont);
            int max = fm.stringWidth("(vacío)");
            for (ArrayList<String> cubeta : cubetas) {
                for (String clave : cubeta) {
                    if (clave != null) {
                        max = Math.max(max, fm.stringWidth(clave + "  [colisión]"));
                    }
                }
            }

            int cellWidth = Math.max(180, max + 24);
            int contentWidth = OUTER_PADDING * 2 + Math.max(280, cellWidth + (INNER_PADDING * 2));

            int contentHeight = OUTER_PADDING * 2;
            for (ArrayList<String> cubeta : cubetas) {
                int visibleRows = Math.max(regPorCubeta, cubeta.size());
                int bucketHeight = HEADER_HEIGHT + INNER_PADDING + (visibleRows * CELL_HEIGHT)
                        + ((visibleRows - 1) * CELL_GAP) + INNER_PADDING;
                contentHeight += bucketHeight;
            }
            if (!cubetas.isEmpty()) {
                contentHeight += (cubetas.size() - 1) * BUCKET_GAP_Y;
            }

            int width = Math.max(contentWidth, getParentAvailableWidth());
            int height = Math.max(contentHeight, 220);

            return new Dimension(width, height);
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
    }
}
