package com.appciencias.views;

import com.appciencias.algorithms.ArbolTries;
import com.appciencias.algorithms.ArbolTries.Nodo;
import com.appciencias.models.ClaveArbol;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;

/**
 * Panel interactivo para Árbol Tries por Residuos (simple).
 * Permite insertar, buscar y eliminar claves,
 * visualizando el árbol con animaciones.
 *
 * La raíz siempre queda vacía como nodo de enlace.
 * Los nodos de enlace (clave = null) se muestran como círculos punteados.
 */
public class ArbolTriesPanel extends JPanel {

    // Colores consistentes con el resto de la aplicación
    private final Color BACKGROUND_COLOR = new Color(245, 245, 250);
    private final Color PANEL_COLOR = new Color(235, 235, 245);
    private final Color BORDER_COLOR = new Color(200, 200, 210);
    private final Color TEXT_PRIMARY = new Color(70, 70, 80);
    private final Color TEXT_SECONDARY = new Color(100, 100, 110);
    private final Color BUTTON_COLOR = new Color(230, 230, 240);
    private final Color BUTTON_HOVER_COLOR = new Color(220, 220, 235);

    private final Color NODE_COLOR = new Color(220, 230, 250);
    private final Color NODE_BORDER_COLOR = new Color(150, 160, 190);
    private final Color NODE_HIGHLIGHT_COLOR = new Color(210, 225, 245);
    private final Color NODE_FOUND_COLOR = new Color(180, 230, 190);
    private final Color NODE_INSERT_COLOR = new Color(180, 210, 255);
    private final Color NODE_DELETE_COLOR = new Color(255, 200, 200);
    private final Color EDGE_COLOR = new Color(160, 170, 195);
    private final Color LINK_NODE_COLOR = new Color(240, 240, 250);
    private final Color LINK_NODE_BORDER_COLOR = new Color(180, 180, 200);

    private ArbolTries arbol;
    private JTextField keyField;
    private JButton insertButton;
    private JButton searchButton;
    private JButton deleteButton;
    private JButton restartButton;
    private JLabel statusLabel;
    private JLabel infoLabel;
    private TreeCanvas treeCanvas;

    // Estado de animación — se rastrean nodos por identidad (no por clave)
    private Timer animationTimer;
    private boolean animationRunning = false;
    private List<Nodo> animPath = null;
    private int animStep = 0;
    private String animTargetKey = null;
    private AnimationType animType = AnimationType.NONE;
    private Runnable animFinishCallback = null;

    private enum AnimationType {
        NONE, INSERT, SEARCH, DELETE
    }

    public ArbolTriesPanel() {
        arbol = new ArbolTries();
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

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(BACKGROUND_COLOR);
        scrollPane.getViewport().setBackground(BACKGROUND_COLOR);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(BACKGROUND_COLOR);

        JLabel titleLabel = new JLabel("Tries por Residuos");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(titleLabel);

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

        panel.add(createActionsPanel());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createTreePanel());

        return panel;
    }

    private JPanel createActionsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PANEL_COLOR);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Campo de clave
        JPanel keyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        keyPanel.setBackground(PANEL_COLOR);
        JLabel keyLabel = new JLabel("Clave");
        keyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        keyLabel.setForeground(TEXT_PRIMARY);
        keyField = createTextField(8);
        keyPanel.add(keyLabel);
        keyPanel.add(keyField);
        keyPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(keyPanel);
        panel.add(Box.createVerticalStrut(10));

        // Botones
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonsPanel.setBackground(PANEL_COLOR);
        buttonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        insertButton = createActionButton("Insertar");
        searchButton = createActionButton("Buscar");
        deleteButton = createActionButton("Eliminar");
        restartButton = createActionButton("Reiniciar árbol");

        insertButton.addActionListener(e -> handleInsert());
        searchButton.addActionListener(e -> handleSearch());
        deleteButton.addActionListener(e -> handleDelete());
        restartButton.addActionListener(e -> handleRestart());

        keyField.addActionListener(e -> handleInsert());

        buttonsPanel.add(insertButton);
        buttonsPanel.add(searchButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(restartButton);
        panel.add(buttonsPanel);
        panel.add(Box.createVerticalStrut(10));

        // Info label (muestra conversión binaria)
        infoLabel = new JLabel(" ");
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        infoLabel.setForeground(TEXT_SECONDARY);
        infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(infoLabel);
        panel.add(Box.createVerticalStrut(4));

        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusLabel.setForeground(TEXT_PRIMARY);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(statusLabel);

        return panel;
    }

    private JPanel createTreePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_COLOR);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Visualización del Árbol");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        panel.add(title, BorderLayout.NORTH);

        treeCanvas = new TreeCanvas();
        JScrollPane scrollPane = new JScrollPane(treeCanvas);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setPreferredSize(new Dimension(600, 420));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // ─── Utility creators ────────────────────────────────────────────

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

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(BUTTON_HOVER_COLOR);
                }
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(BUTTON_COLOR);
                }
            }
        });

        return button;
    }

    // ─── Handlers ────────────────────────────────────────────────────

    private void handleInsert() {
        if (animationRunning) {
            setStatus("Espera a que termine la animación.", true);
            return;
        }

        String clave = keyField.getText().trim();
        keyField.setText("");

        if (clave.isEmpty()) {
            setStatus("Ingresa una clave.", true);
            return;
        }

        try {
            ClaveArbol.validarASCII(clave);
        } catch (IllegalArgumentException ex) {
            setStatus("Error: " + ex.getMessage(), true);
            return;
        }

        String info = ClaveArbol.obtenerInfoASCII(clave);
        infoLabel.setText(info);

        if (arbol.buscar(clave)) {
            setStatus("La clave '" + clave + "' ya existe en el árbol.", true);
            return;
        }

        // Calcular ruta de animación antes de insertar
        List<Nodo> path = computeInsertPath(clave);

        try {
            arbol.insertar(clave);
        } catch (Exception ex) {
            setStatus("Error: " + ex.getMessage(), true);
            return;
        }

        final String claveF = clave;
        runAnimation(path, claveF, AnimationType.INSERT, () -> {
            setStatus("Clave '" + claveF + "' insertada. Claves en el árbol: " + arbol.getContador(), false);
            treeCanvas.repaint();
        });
    }

    private void handleSearch() {
        if (animationRunning) {
            setStatus("Espera a que termine la animación.", true);
            return;
        }

        String clave = keyField.getText().trim();
        keyField.setText("");

        if (clave.isEmpty()) {
            setStatus("Ingresa una clave.", true);
            return;
        }

        try {
            ClaveArbol.validarASCII(clave);
        } catch (IllegalArgumentException ex) {
            setStatus("Error: " + ex.getMessage(), true);
            return;
        }

        String info = ClaveArbol.obtenerInfoASCII(clave);
        infoLabel.setText(info);

        List<Nodo> path = computeSearchPath(clave);
        boolean found = arbol.buscar(clave);

        final String claveF = clave;
        runAnimation(path, claveF, AnimationType.SEARCH, () -> {
            if (found) {
                setStatus("Clave '" + claveF + "' encontrada en el árbol.", false);
            } else {
                setStatus("Clave '" + claveF + "' NO encontrada en el árbol.", true);
            }
        });
    }

    private void handleDelete() {
        if (animationRunning) {
            setStatus("Espera a que termine la animación.", true);
            return;
        }

        String clave = keyField.getText().trim();
        keyField.setText("");

        if (clave.isEmpty()) {
            setStatus("Ingresa una clave.", true);
            return;
        }

        try {
            ClaveArbol.validarASCII(clave);
        } catch (IllegalArgumentException ex) {
            setStatus("Error: " + ex.getMessage(), true);
            return;
        }

        String info = ClaveArbol.obtenerInfoASCII(clave);
        infoLabel.setText(info);

        if (!arbol.buscar(clave)) {
            setStatus("La clave '" + clave + "' no existe en el árbol.", true);
            return;
        }

        List<Nodo> path = computeSearchPath(clave);

        final String claveF = clave;
        runAnimation(path, claveF, AnimationType.DELETE, () -> {
            try {
                arbol.eliminar(claveF);
                setStatus("Clave '" + claveF + "' eliminada. Claves en el árbol: " + arbol.getContador(), false);
            } catch (Exception ex) {
                setStatus("Error: " + ex.getMessage(), true);
            }
            treeCanvas.repaint();
        });
    }

    private void handleRestart() {
        if (animationRunning) {
            stopAnimation();
        }
        arbol = new ArbolTries();
        infoLabel.setText(" ");
        setStatus("Árbol reiniciado.", false);
        treeCanvas.repaint();
    }

    // ─── Ruta de animación ───────────────────────────────────────────

    /**
     * Calcula la lista de nodos visitados al insertar (antes de la inserción real).
     * Se rastrean los nodos por identidad ya que los nodos de enlace tienen clave=null.
     */
    private List<Nodo> computeInsertPath(String clave) {
        List<Nodo> path = new ArrayList<>();
        Nodo raiz = arbol.getRaiz();
        if (raiz == null) {
            return path;
        }
        path.add(raiz);

        String bits = ClaveArbol.claveABinarioASCII(clave);
        Nodo actual = raiz;
        for (int i = 0; i < bits.length(); i++) {
            char bit = bits.charAt(i);
            Nodo siguiente = (bit == '1') ? actual.derecha : actual.izquierda;
            if (siguiente == null) {
                break;
            }
            path.add(siguiente);
            if (siguiente.clave != null) {
                // Es un nodo con dato; si hay colisión ambos bajarán
                break;
            }
            actual = siguiente;
        }
        return path;
    }

    /**
     * Calcula la lista de nodos visitados al buscar una clave.
     */
    private List<Nodo> computeSearchPath(String clave) {
        List<Nodo> path = new ArrayList<>();
        Nodo raiz = arbol.getRaiz();
        if (raiz == null) {
            return path;
        }
        path.add(raiz);

        String bits = ClaveArbol.claveABinarioASCII(clave);
        Nodo actual = raiz;
        for (int i = 0; i < bits.length(); i++) {
            char bit = bits.charAt(i);
            Nodo siguiente = (bit == '1') ? actual.derecha : actual.izquierda;
            if (siguiente == null) {
                break;
            }
            path.add(siguiente);
            if (siguiente.clave != null && siguiente.clave.equals(clave)) {
                break;
            }
            if (siguiente.clave != null && !siguiente.clave.equals(clave)) {
                // Llegamos a un nodo con otra clave, no encontrado
                break;
            }
            actual = siguiente;
        }
        return path;
    }

    // ─── Animación ───────────────────────────────────────────────────

    private void runAnimation(List<Nodo> path, String targetKey, AnimationType type, Runnable onFinish) {
        animPath = path;
        animStep = -1;
        animTargetKey = targetKey;
        animType = type;
        animFinishCallback = onFinish;
        animationRunning = true;
        setButtonsEnabled(false);

        if (path.isEmpty()) {
            animStep = 0;
            finishAnimation();
            return;
        }

        animationTimer = new Timer(500, e -> {
            animStep++;
            if (animStep >= animPath.size()) {
                finishAnimation();
            } else {
                treeCanvas.repaint();
            }
        });
        animationTimer.setInitialDelay(300);
        animationTimer.start();
    }

    private void finishAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        treeCanvas.repaint();
        Timer delayTimer = new Timer(600, e -> {
            animationRunning = false;
            animPath = null;
            animTargetKey = null;
            animType = AnimationType.NONE;
            setButtonsEnabled(true);
            treeCanvas.repaint();
            if (animFinishCallback != null) {
                animFinishCallback.run();
            }
        });
        delayTimer.setRepeats(false);
        delayTimer.start();
    }

    private void stopAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        animationRunning = false;
        animPath = null;
        animTargetKey = null;
        animType = AnimationType.NONE;
        setButtonsEnabled(true);
        treeCanvas.repaint();
    }

    private void setButtonsEnabled(boolean enabled) {
        insertButton.setEnabled(enabled);
        searchButton.setEnabled(enabled);
        deleteButton.setEnabled(enabled);
        restartButton.setEnabled(enabled);
        keyField.setEnabled(enabled);
    }

    private void setStatus(String msg, boolean isError) {
        statusLabel.setText(msg);
        statusLabel.setForeground(isError ? new Color(180, 60, 60) : TEXT_PRIMARY);
    }

    // ─── Canvas de visualización del árbol ───────────────────────────

    /**
     * Canvas que dibuja el árbol Tries con nodos circulares.
     * Los nodos de enlace (clave=null) se dibujan con borde punteado y color tenue.
     * Los nodos con dato se dibujan como en ArbolDigitalPanel.
     */
    private class TreeCanvas extends JPanel {

        private static final int NODE_RADIUS = 22;
        private static final int VERTICAL_GAP = 70;
        private static final int MIN_HORIZONTAL_GAP = 50;

        private Map<String, Point> nodePositions = new HashMap<>();

        public TreeCanvas() {
            setBackground(Color.WHITE);
        }

        @Override
        public Dimension getPreferredSize() {
            Nodo raiz = arbol.getRaiz();
            if (raiz == null) {
                return new Dimension(600, 400);
            }
            int depth = getDepth(raiz);
            int leaves = getLeafCount(raiz);
            int width = Math.max(600, leaves * (NODE_RADIUS * 2 + MIN_HORIZONTAL_GAP));
            int height = Math.max(400, depth * VERTICAL_GAP + 80);
            return new Dimension(width, height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            Nodo raiz = arbol.getRaiz();
            if (raiz == null) {
                drawEmptyMessage(g2);
                return;
            }

            // Si la raíz no tiene hijos, mostrar solo la raíz vacía con mensaje
            if (raiz.izquierda == null && raiz.derecha == null) {
                drawEmptyMessage(g2);
                return;
            }

            nodePositions.clear();

            int treeWidth = computeSubtreeWidth(raiz);
            int startX = getWidth() / 2;
            int startY = 40;

            computePositions(raiz, startX, startY, treeWidth / 2);

            drawEdges(g2, raiz);
            drawNodes(g2, raiz);
            drawLegend(g2);
        }

        private void drawEmptyMessage(Graphics2D g2) {
            g2.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            g2.setColor(TEXT_SECONDARY);
            String msg = "Árbol vacío. Inserta una clave para comenzar.";
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(msg)) / 2;
            int y = getHeight() / 2;
            g2.drawString(msg, x, y);
        }

        private int computeSubtreeWidth(Nodo nodo) {
            if (nodo == null) {
                return 0;
            }
            if (nodo.izquierda == null && nodo.derecha == null) {
                return NODE_RADIUS * 2 + MIN_HORIZONTAL_GAP;
            }

            int leftWidth = computeSubtreeWidth(nodo.izquierda);
            int rightWidth = computeSubtreeWidth(nodo.derecha);

            if (nodo.izquierda == null) {
                leftWidth = Math.max(leftWidth, NODE_RADIUS * 2 + MIN_HORIZONTAL_GAP / 2);
            }
            if (nodo.derecha == null) {
                rightWidth = Math.max(rightWidth, NODE_RADIUS * 2 + MIN_HORIZONTAL_GAP / 2);
            }

            return leftWidth + rightWidth;
        }

        private void computePositions(Nodo nodo, int x, int y, int halfSpan) {
            if (nodo == null) {
                return;
            }

            nodePositions.put(getNodeId(nodo), new Point(x, y));

            int childY = y + VERTICAL_GAP;
            int minSpan = NODE_RADIUS + MIN_HORIZONTAL_GAP / 2;

            int leftHalf = halfSpan;
            int rightHalf = halfSpan;

            if (nodo.izquierda != null && nodo.derecha != null) {
                int leftW = computeSubtreeWidth(nodo.izquierda);
                int rightW = computeSubtreeWidth(nodo.derecha);
                int total = leftW + rightW;
                if (total > 0) {
                    leftHalf = Math.max(minSpan, (int) ((double) leftW / total * halfSpan * 2) / 2);
                    rightHalf = Math.max(minSpan, (int) ((double) rightW / total * halfSpan * 2) / 2);
                }
            }

            if (nodo.izquierda != null) {
                int childX = x - Math.max(minSpan, leftHalf);
                computePositions(nodo.izquierda, childX, childY, Math.max(minSpan, leftHalf / 2));
            }

            if (nodo.derecha != null) {
                int childX = x + Math.max(minSpan, rightHalf);
                computePositions(nodo.derecha, childX, childY, Math.max(minSpan, rightHalf / 2));
            }
        }

        private void drawEdges(Graphics2D g2, Nodo nodo) {
            if (nodo == null) {
                return;
            }

            Point parentPos = nodePositions.get(getNodeId(nodo));
            if (parentPos == null) {
                return;
            }

            Stroke defaultStroke = g2.getStroke();

            if (nodo.izquierda != null) {
                Point childPos = nodePositions.get(getNodeId(nodo.izquierda));
                if (childPos != null) {
                    boolean highlighted = isEdgeHighlighted(nodo, nodo.izquierda);
                    g2.setColor(highlighted ? getAnimColor() : EDGE_COLOR);
                    g2.setStroke(new BasicStroke(highlighted ? 3f : 2f));
                    g2.drawLine(parentPos.x, parentPos.y + NODE_RADIUS,
                            childPos.x, childPos.y - NODE_RADIUS);

                    int midX = (parentPos.x + childPos.x) / 2 - 12;
                    int midY = (parentPos.y + NODE_RADIUS + childPos.y - NODE_RADIUS) / 2;
                    g2.setColor(TEXT_SECONDARY);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    g2.drawString("0", midX, midY);
                }
            }

            if (nodo.derecha != null) {
                Point childPos = nodePositions.get(getNodeId(nodo.derecha));
                if (childPos != null) {
                    boolean highlighted = isEdgeHighlighted(nodo, nodo.derecha);
                    g2.setColor(highlighted ? getAnimColor() : EDGE_COLOR);
                    g2.setStroke(new BasicStroke(highlighted ? 3f : 2f));
                    g2.drawLine(parentPos.x, parentPos.y + NODE_RADIUS,
                            childPos.x, childPos.y - NODE_RADIUS);

                    int midX = (parentPos.x + childPos.x) / 2 + 5;
                    int midY = (parentPos.y + NODE_RADIUS + childPos.y - NODE_RADIUS) / 2;
                    g2.setColor(TEXT_SECONDARY);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    g2.drawString("1", midX, midY);
                }
            }

            g2.setStroke(defaultStroke);

            drawEdges(g2, nodo.izquierda);
            drawEdges(g2, nodo.derecha);
        }

        private void drawNodes(Graphics2D g2, Nodo nodo) {
            if (nodo == null) {
                return;
            }

            Point pos = nodePositions.get(getNodeId(nodo));
            if (pos == null) {
                return;
            }

            Color fillColor = getNodeFillColor(nodo);
            boolean highlighted = isNodeHighlighted(nodo);

            // Círculo del nodo
            g2.setColor(fillColor);
            g2.fillOval(pos.x - NODE_RADIUS, pos.y - NODE_RADIUS,
                    NODE_RADIUS * 2, NODE_RADIUS * 2);

            // Borde — punteado para nodos de enlace, sólido para nodos con dato
            if (nodo.esEnlace()) {
                float[] dashPattern = {5f, 4f};
                g2.setColor(highlighted ? getAnimColor().darker() : LINK_NODE_BORDER_COLOR);
                g2.setStroke(new BasicStroke(highlighted ? 3f : 2f,
                        BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, dashPattern, 0f));
            } else {
                g2.setColor(highlighted ? getAnimColor().darker() : NODE_BORDER_COLOR);
                g2.setStroke(new BasicStroke(highlighted ? 3f : 2f));
            }
            g2.drawOval(pos.x - NODE_RADIUS, pos.y - NODE_RADIUS,
                    NODE_RADIUS * 2, NODE_RADIUS * 2);

            // Texto
            if (nodo.clave != null) {
                // Nodo con dato: mostrar la letra
                g2.setColor(TEXT_PRIMARY);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
                FontMetrics fm = g2.getFontMetrics();
                int textX = pos.x - fm.stringWidth(nodo.clave) / 2;
                int textY = pos.y + fm.getAscent() / 2 - 1;
                g2.drawString(nodo.clave, textX, textY);

                // Mostrar binario debajo del nodo
                String bits = ClaveArbol.claveABinarioASCII(nodo.clave);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g2.setColor(TEXT_SECONDARY);
                FontMetrics fm2 = g2.getFontMetrics();
                int bitsX = pos.x - fm2.stringWidth(bits) / 2;
                int bitsY = pos.y + NODE_RADIUS + 13;
                g2.drawString(bits, bitsX, bitsY);
            } else {
                // Nodo de enlace: mostrar "·" como indicador
                g2.setColor(LINK_NODE_BORDER_COLOR);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                FontMetrics fm = g2.getFontMetrics();
                String dot = "·";
                int textX = pos.x - fm.stringWidth(dot) / 2;
                int textY = pos.y + fm.getAscent() / 2 - 1;
                g2.drawString(dot, textX, textY);
            }

            drawNodes(g2, nodo.izquierda);
            drawNodes(g2, nodo.derecha);
        }

        private void drawLegend(Graphics2D g2) {
            int lx = 10;
            int ly = getHeight() - 55;

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2.setColor(TEXT_SECONDARY);
            g2.drawString("0 = Izquierda    1 = Derecha", lx, ly);
            g2.drawString("Claves insertadas: " + arbol.getContador(), lx, ly + 15);

            // Leyenda de tipos de nodo
            int legendY = ly + 35;
            // Nodo con dato
            g2.setColor(NODE_COLOR);
            g2.fillOval(lx, legendY - 10, 12, 12);
            g2.setColor(NODE_BORDER_COLOR);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(lx, legendY - 10, 12, 12);
            g2.setColor(TEXT_SECONDARY);
            g2.drawString("Nodo con dato", lx + 18, legendY);

            // Nodo de enlace
            g2.setColor(LINK_NODE_COLOR);
            g2.fillOval(lx + 130, legendY - 10, 12, 12);
            g2.setColor(LINK_NODE_BORDER_COLOR);
            float[] dash = {3f, 3f};
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, dash, 0f));
            g2.drawOval(lx + 130, legendY - 10, 12, 12);
            g2.setColor(TEXT_SECONDARY);
            g2.setStroke(new BasicStroke(1f));
            g2.drawString("Nodo de enlace", lx + 148, legendY);
        }

        // ─── Colores y resaltado por identidad de nodo ───────────────

        private Color getNodeFillColor(Nodo nodo) {
            if (!animationRunning || animPath == null) {
                return nodo.esEnlace() ? LINK_NODE_COLOR : NODE_COLOR;
            }

            int nodoId = System.identityHashCode(nodo);

            // Nodo actual en la animación
            if (animStep >= 0 && animStep < animPath.size()) {
                if (System.identityHashCode(animPath.get(animStep)) == nodoId) {
                    return getAnimColor();
                }
            }

            // Nodo ya visitado
            for (int i = 0; i < animStep && i < animPath.size(); i++) {
                if (System.identityHashCode(animPath.get(i)) == nodoId) {
                    return NODE_HIGHLIGHT_COLOR;
                }
            }

            // Animación terminó y es el nodo target
            if (animStep >= animPath.size() && nodo.clave != null && nodo.clave.equals(animTargetKey)) {
                switch (animType) {
                    case INSERT:
                        return NODE_INSERT_COLOR;
                    case SEARCH:
                        return NODE_FOUND_COLOR;
                    case DELETE:
                        return NODE_DELETE_COLOR;
                    default:
                        return nodo.esEnlace() ? LINK_NODE_COLOR : NODE_COLOR;
                }
            }

            return nodo.esEnlace() ? LINK_NODE_COLOR : NODE_COLOR;
        }

        private Color getAnimColor() {
            switch (animType) {
                case INSERT:
                    return NODE_INSERT_COLOR;
                case SEARCH:
                    return NODE_FOUND_COLOR;
                case DELETE:
                    return NODE_DELETE_COLOR;
                default:
                    return NODE_HIGHLIGHT_COLOR;
            }
        }

        private boolean isNodeHighlighted(Nodo nodo) {
            if (!animationRunning || animPath == null) {
                return false;
            }
            int nodoId = System.identityHashCode(nodo);
            if (animStep >= 0 && animStep < animPath.size()) {
                return System.identityHashCode(animPath.get(animStep)) == nodoId;
            }
            if (animStep >= animPath.size() && nodo.clave != null) {
                return nodo.clave.equals(animTargetKey);
            }
            return false;
        }

        private boolean isEdgeHighlighted(Nodo parent, Nodo child) {
            if (!animationRunning || animPath == null) {
                return false;
            }
            int parentId = System.identityHashCode(parent);
            int childId = System.identityHashCode(child);
            for (int i = 0; i < animStep && i + 1 < animPath.size(); i++) {
                if (System.identityHashCode(animPath.get(i)) == parentId
                        && System.identityHashCode(animPath.get(i + 1)) == childId) {
                    return true;
                }
            }
            if (animStep > 0 && animStep < animPath.size()) {
                if (System.identityHashCode(animPath.get(animStep - 1)) == parentId
                        && System.identityHashCode(animPath.get(animStep)) == childId) {
                    return true;
                }
            }
            return false;
        }

        private String getNodeId(Nodo nodo) {
            return String.valueOf(System.identityHashCode(nodo));
        }

        private int getDepth(Nodo nodo) {
            if (nodo == null) {
                return 0;
            }
            return 1 + Math.max(getDepth(nodo.izquierda), getDepth(nodo.derecha));
        }

        private int getLeafCount(Nodo nodo) {
            if (nodo == null) {
                return 0;
            }
            if (nodo.izquierda == null && nodo.derecha == null) {
                return 1;
            }
            int left = getLeafCount(nodo.izquierda);
            int right = getLeafCount(nodo.derecha);
            if (nodo.izquierda == null) {
                left = 1;
            }
            if (nodo.derecha == null) {
                right = 1;
            }
            return left + right;
        }
    }
}
