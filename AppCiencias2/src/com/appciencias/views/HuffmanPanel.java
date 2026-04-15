package com.appciencias.views;

import com.appciencias.algorithms.ArbolHuffman;
import com.appciencias.algorithms.ArbolHuffman.CodigoHuffman;
import com.appciencias.algorithms.ArbolHuffman.Nodo;
import com.appciencias.algorithms.ArbolHuffman.TablaIntermedia;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

/**
 * Panel interactivo para construir y visualizar un Árbol de Huffman.
 */
public class HuffmanPanel extends JPanel {

    private final Color backgroundColor = getUIOrDefault("Panel.background", new Color(245, 245, 250));
    private final Color panelColor = getUIOrDefault("Panel.background", new Color(235, 235, 245));
    private final Color borderColor = getUIOrDefault("Separator.foreground", new Color(200, 200, 210));
    private final Color textPrimary = getUIOrDefault("Label.foreground", new Color(70, 70, 80));
    private final Color textSecondary = getUIOrDefault("Label.disabledForeground", new Color(100, 100, 110));
    private final Color buttonColor = getUIOrDefault("Button.background", new Color(230, 230, 240));

    private ArbolHuffman arbol;

    private JTextField inputField;
    private JButton construirButton;
    private JButton reiniciarButton;

    private DefaultTableModel procesoModel;
    private DefaultTableModel resultadosModel;
    private JTable procesoTable;
    private JTable resultadosTable;

    private JLabel encodedLabel;
    private JLabel pesoLabel;

    private TreeDrawPanel treeDrawPanel;

    public HuffmanPanel() {
        this.arbol = new ArbolHuffman();
        initializePanel();
    }

    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(backgroundColor);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(backgroundColor);
        content.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        content.add(createHeader());
        content.add(Box.createVerticalStrut(16));
        content.add(createConfigPanel());
        content.add(Box.createVerticalStrut(14));
        content.add(createVisualizationPanel());

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(backgroundColor);
        scrollPane.getViewport().setBackground(backgroundColor);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(backgroundColor);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel("Árbol de Huffman");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(textPrimary);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea description = new JTextArea(
                "Construye el árbol a partir de un texto, visualiza cada unión de frecuencias "
                + "y consulta la codificación binaria final.");
        description.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        description.setForeground(textSecondary);
        description.setBackground(backgroundColor);
        description.setEditable(false);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(titleLabel);
        header.add(Box.createVerticalStrut(8));
        header.add(description);

        return header;
    }

    private JPanel createConfigPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(panelColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setBackground(panelColor);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel inputLabel = new JLabel("Texto de entrada");
        inputLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputLabel.setForeground(textPrimary);

        inputField = new JTextField();
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        inputField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        inputField.addActionListener(e -> handleConstruir());

        construirButton = createActionButton("Construir");
        construirButton.addActionListener(e -> handleConstruir());

        reiniciarButton = createActionButton("Reiniciar");
        reiniciarButton.addActionListener(e -> handleReiniciar());

        row.add(inputLabel);
        row.add(Box.createHorizontalStrut(10));
        row.add(inputField);
        row.add(Box.createHorizontalStrut(10));
        row.add(construirButton);
        row.add(Box.createHorizontalStrut(8));
        row.add(reiniciarButton);

        panel.add(row);

        return panel;
    }

    private JPanel createVisualizationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(panelColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setPreferredSize(new Dimension(980, 600));
        panel.setMinimumSize(new Dimension(800, 520));

        treeDrawPanel = new TreeDrawPanel();
        JScrollPane treeScroll = new JScrollPane(treeDrawPanel);
        treeScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                "Árbol Gráfico"
        ));
        treeScroll.getVerticalScrollBar().setUnitIncrement(16);
        treeScroll.getHorizontalScrollBar().setUnitIncrement(16);
        treeScroll.setPreferredSize(new Dimension(540, 560));

        procesoModel = new DefaultTableModel(new Object[]{"Paso", "Descripción", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        procesoTable = new JTable(procesoModel);
        procesoTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        procesoTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        procesoTable.setRowHeight(24);
        procesoTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        procesoTable.getColumnModel().getColumn(1).setPreferredWidth(260);
        procesoTable.getColumnModel().getColumn(2).setPreferredWidth(280);

        JScrollPane procesoScroll = new JScrollPane(procesoTable);
        procesoScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                "Proceso"
        ));

        resultadosModel = new DefaultTableModel(new Object[]{"Letra", "Frecuencia", "Código"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        resultadosTable = new JTable(resultadosModel);
        resultadosTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        resultadosTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        resultadosTable.setRowHeight(24);
        resultadosTable.getColumnModel().getColumn(0).setPreferredWidth(70);
        resultadosTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        resultadosTable.getColumnModel().getColumn(2).setPreferredWidth(170);

        JScrollPane resultadosScroll = new JScrollPane(resultadosTable);
        resultadosScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                "Resultados Finales"
        ));

        encodedLabel = new JLabel("Cadena codificada: -");
        encodedLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        encodedLabel.setForeground(textPrimary);
        encodedLabel.setHorizontalAlignment(SwingConstants.LEFT);

        pesoLabel = new JLabel("Peso total: -");
        pesoLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pesoLabel.setForeground(textPrimary);
        pesoLabel.setHorizontalAlignment(SwingConstants.LEFT);

        JPanel resumenPanel = new JPanel();
        resumenPanel.setLayout(new BoxLayout(resumenPanel, BoxLayout.Y_AXIS));
        resumenPanel.setBackground(panelColor);
        resumenPanel.setBorder(BorderFactory.createEmptyBorder(8, 2, 2, 2));
        resumenPanel.add(encodedLabel);
        resumenPanel.add(Box.createVerticalStrut(5));
        resumenPanel.add(pesoLabel);

        JPanel resultadosContainer = new JPanel(new BorderLayout());
        resultadosContainer.setBackground(panelColor);
        resultadosContainer.add(resultadosScroll, BorderLayout.CENTER);
        resultadosContainer.add(resumenPanel, BorderLayout.SOUTH);

        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, procesoScroll, resultadosContainer);
        rightSplit.setResizeWeight(0.52);
        rightSplit.setDividerSize(8);
        rightSplit.setContinuousLayout(true);
        rightSplit.setBorder(BorderFactory.createEmptyBorder());

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScroll, rightSplit);
        mainSplit.setResizeWeight(0.55);
        mainSplit.setDividerSize(10);
        mainSplit.setContinuousLayout(true);
        mainSplit.setBorder(BorderFactory.createEmptyBorder());

        panel.add(mainSplit, BorderLayout.CENTER);

        return panel;
    }

    private JButton createActionButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(textPrimary);
        button.setBackground(buttonColor);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));
        return button;
    }

    private void handleConstruir() {
        String texto = inputField.getText();

        if (texto == null || texto.trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Ingresa un texto antes de construir el árbol.",
                    "Validación",
                    JOptionPane.WARNING_MESSAGE
            );
            inputField.requestFocusInWindow();
            return;
        }

        try {
            arbol.construir(texto);
            refreshProcessTable(arbol.getTablasIntermedias());
            refreshResultsTable(arbol.getCodigos());
            refreshSummary();
            treeDrawPanel.updateTree(arbol.getRaiz());
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Texto inválido",
                    JOptionPane.WARNING_MESSAGE
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Ocurrió un error al construir el árbol: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void handleReiniciar() {
        inputField.setText("");
        arbol = new ArbolHuffman();

        procesoModel.setRowCount(0);
        resultadosModel.setRowCount(0);
        encodedLabel.setText("Cadena codificada: -");
        pesoLabel.setText("Peso total: -");

        treeDrawPanel.updateTree(null);
        inputField.requestFocusInWindow();
    }

    private void refreshProcessTable(ArrayList<TablaIntermedia> tablas) {
        procesoModel.setRowCount(0);

        for (TablaIntermedia tabla : tablas) {
            procesoModel.addRow(new Object[]{
                    tabla.numeroPaso,
                    tabla.descripcion,
                    formatNodeState(tabla.nodos)
            });
        }
    }

    private void refreshResultsTable(ArrayList<CodigoHuffman> codigos) {
        resultadosModel.setRowCount(0);

        for (CodigoHuffman codigo : codigos) {
            resultadosModel.addRow(new Object[]{
                    codigo.letra,
                    codigo.frecuencia,
                    codigo.codigo
            });
        }
    }

    private void refreshSummary() {
        String codificado = arbol.codificarTexto();
        encodedLabel.setText("Cadena codificada: " + codificado);
        pesoLabel.setText("Peso total: " + calculateTotalWeight(arbol.getCodigos()) + " bits");
    }

    private String formatNodeState(ArrayList<Nodo> nodos) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < nodos.size(); i++) {
            Nodo nodo = nodos.get(i);
            if (i > 0) {
                sb.append("  |  ");
            }
            sb.append(nodo.etiqueta).append("(").append(nodo.frecuencia).append(")");
        }

        return sb.toString();
    }

    private int calculateTotalWeight(ArrayList<CodigoHuffman> codigos) {
        int total = 0;
        for (CodigoHuffman codigo : codigos) {
            total += codigo.frecuencia * codigo.codigo.length();
        }
        return total;
    }

    private Color getUIOrDefault(String key, Color fallback) {
        Color color = UIManager.getColor(key);
        return color != null ? color : fallback;
    }

    private class TreeDrawPanel extends JPanel {

        private static final int NODE_RADIUS = 22;
        private static final int NODE_DIAMETER = NODE_RADIUS * 2;
        private static final int LEVEL_GAP = 88;
        private static final int SUBTREE_GAP = 36;
        private static final int LEAF_BASE_WIDTH = 76;
        private static final int PADDING_X = 40;
        private static final int PADDING_TOP = 36;
        private static final int PADDING_BOTTOM = 36;

        private final Map<Nodo, Integer> widthCache;
        private final Map<Nodo, Point> positions;
        private Nodo root;

        TreeDrawPanel() {
            this.widthCache = new HashMap<>();
            this.positions = new HashMap<>();
            this.root = null;
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(900, 500));
        }

        void updateTree(Nodo root) {
            this.root = root;
            recomputeLayout();
            revalidate();
            repaint();
        }

        private void recomputeLayout() {
            widthCache.clear();
            positions.clear();

            if (root == null) {
                setPreferredSize(new Dimension(900, 500));
                return;
            }

            int width = computeSubtreeWidth(root);
            int depth = computeDepth(root);
            int preferredWidth = Math.max(900, width + (PADDING_X * 2));
            int preferredHeight = Math.max(500, PADDING_TOP + (depth * LEVEL_GAP) + PADDING_BOTTOM + NODE_DIAMETER);
            setPreferredSize(new Dimension(preferredWidth, preferredHeight));

            int rootX = preferredWidth / 2;
            layoutRecursively(root, rootX, PADDING_TOP);
        }

        private int computeSubtreeWidth(Nodo node) {
            if (node == null) {
                return 0;
            }

            Integer cached = widthCache.get(node);
            if (cached != null) {
                return cached;
            }

            int width;
            boolean hasLeft = node.izquierda != null;
            boolean hasRight = node.derecha != null;

            if (!hasLeft && !hasRight) {
                width = LEAF_BASE_WIDTH;
            } else {
                int leftWidth = hasLeft ? computeSubtreeWidth(node.izquierda) : LEAF_BASE_WIDTH;
                int rightWidth = hasRight ? computeSubtreeWidth(node.derecha) : LEAF_BASE_WIDTH;
                width = leftWidth + rightWidth + SUBTREE_GAP;
                width = Math.max(width, NODE_DIAMETER + 18);
            }

            widthCache.put(node, width);
            return width;
        }

        private void layoutRecursively(Nodo node, int centerX, int y) {
            if (node == null) {
                return;
            }

            positions.put(node, new Point(centerX, y));

            int childY = y + LEVEL_GAP;
            boolean hasLeft = node.izquierda != null;
            boolean hasRight = node.derecha != null;

            if (hasLeft && hasRight) {
                int leftWidth = computeSubtreeWidth(node.izquierda);
                int rightWidth = computeSubtreeWidth(node.derecha);
                int totalWidth = leftWidth + SUBTREE_GAP + rightWidth;

                int leftCenter = centerX - (totalWidth / 2) + (leftWidth / 2);
                int rightCenter = centerX + (totalWidth / 2) - (rightWidth / 2);

                layoutRecursively(node.izquierda, leftCenter, childY);
                layoutRecursively(node.derecha, rightCenter, childY);
            } else if (hasLeft) {
                layoutRecursively(node.izquierda, centerX - (LEAF_BASE_WIDTH / 2), childY);
            } else if (hasRight) {
                layoutRecursively(node.derecha, centerX + (LEAF_BASE_WIDTH / 2), childY);
            }
        }

        private int computeDepth(Nodo node) {
            if (node == null) {
                return 0;
            }
            return 1 + Math.max(computeDepth(node.izquierda), computeDepth(node.derecha));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (root == null) {
                g2.setColor(textSecondary);
                g2.setFont(new Font("Segoe UI", Font.ITALIC, 15));
                g2.drawString("Construye un árbol para visualizar su estructura.", 28, 40);
                g2.dispose();
                return;
            }

            drawEdges(g2, root);
            drawNodes(g2, root);

            g2.dispose();
        }

        private void drawEdges(Graphics2D g2, Nodo node) {
            if (node == null) {
                return;
            }

            Point parent = positions.get(node);
            if (parent == null) {
                return;
            }

            if (node.izquierda != null) {
                Point left = positions.get(node.izquierda);
                if (left != null) {
                    drawSingleEdge(g2, parent, left, "0");
                }
                drawEdges(g2, node.izquierda);
            }

            if (node.derecha != null) {
                Point right = positions.get(node.derecha);
                if (right != null) {
                    drawSingleEdge(g2, parent, right, "1");
                }
                drawEdges(g2, node.derecha);
            }
        }

        private void drawSingleEdge(Graphics2D g2, Point from, Point to, String bit) {
            int x1 = from.x;
            int y1 = from.y + NODE_RADIUS;
            int x2 = to.x;
            int y2 = to.y - NODE_RADIUS;

            g2.setColor(getUIOrDefault("Separator.foreground", new Color(160, 170, 195)));
            g2.setStroke(new BasicStroke(1.6f));
            g2.drawLine(x1, y1, x2, y2);

            int labelX = (x1 + x2) / 2;
            int labelY = (y1 + y2) / 2;

            g2.setColor(Color.WHITE);
            g2.fillRoundRect(labelX - 10, labelY - 12, 20, 18, 10, 10);
            g2.setColor(getUIOrDefault("TextField.foreground", textPrimary));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.drawString(bit, labelX - 4, labelY + 1);
        }

        private void drawNodes(Graphics2D g2, Nodo node) {
            if (node == null) {
                return;
            }

            Point p = positions.get(node);
            if (p == null) {
                return;
            }

            Color fillColor;
            if (node.esHoja) {
                fillColor = getUIOrDefault("Table.selectionBackground", new Color(215, 228, 250));
            } else {
                fillColor = getUIOrDefault("Button.background", new Color(228, 232, 242));
            }

            g2.setColor(fillColor);
            g2.fillOval(p.x - NODE_RADIUS, p.y - NODE_RADIUS, NODE_DIAMETER, NODE_DIAMETER);

            g2.setColor(getUIOrDefault("Table.gridColor", borderColor));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(p.x - NODE_RADIUS, p.y - NODE_RADIUS, NODE_DIAMETER, NODE_DIAMETER);

            String text = node.esHoja
                    ? node.etiqueta + ":" + node.frecuencia
                    : String.valueOf(node.frecuencia);

            g2.setColor(textPrimary);
            g2.setFont(new Font("Segoe UI", Font.BOLD, node.esHoja ? 11 : 12));

            FontMetrics fm = g2.getFontMetrics();
            int textW = fm.stringWidth(text);
            int textX = p.x - (textW / 2);
            int textY = p.y + (fm.getAscent() / 2) - 2;
            g2.drawString(text, textX, textY);

            drawNodes(g2, node.izquierda);
            drawNodes(g2, node.derecha);
        }
    }
}