package com.appciencias.views;

import com.appciencias.algorithms.ArbolMultipleResiduo;
import com.appciencias.algorithms.ArbolMultipleResiduo.Nodo;
import com.appciencias.models.ClaveArbol;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;

/**
 * Panel interactivo para Árbol de Residuos Múltiples.
 * El usuario selecciona n (bits por nivel), cada nodo tiene M = 2^n hijos.
 * Los botones de insertar/buscar/eliminar se desbloquean tras configurar n.
 *
 * La raíz siempre queda vacía como nodo de enlace.
 * Los nodos de enlace (clave = null) se muestran como círculos punteados.
 */
public class ArbolMultiplePanel extends JPanel {

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

    private ArbolMultipleResiduo arbol;

    // Configuración
    private JComboBox<Integer> nComboBox;
    private JButton confirmButton;
    private JLabel configInfoLabel;

    // Acciones
    private JTextField keyField;
    private JButton insertButton;
    private JButton searchButton;
    private JButton deleteButton;
    private JButton restartButton;
    private JLabel statusLabel;
    private JLabel infoLabel;
    private TreeCanvas treeCanvas;

    // Estado de animación
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

    public ArbolMultiplePanel() {
        arbol = null;
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

        JLabel titleLabel = new JLabel("Residuos Múltiples");
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

        panel.add(createConfigPanel());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createActionsPanel());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createTreePanel());

        return panel;
    }

    // ─── Panel de configuración ──────────────────────────────────────

    private JPanel createConfigPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PANEL_COLOR);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Fila con selector de n y botón confirmar
        JPanel configRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        configRow.setBackground(PANEL_COLOR);
        configRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nLabel = new JLabel("Bits por nivel (n)");
        nLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        nLabel.setForeground(TEXT_PRIMARY);

        nComboBox = new JComboBox<>(new Integer[]{1, 2, 4, 8});
        nComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        nComboBox.setBackground(Color.WHITE);
        nComboBox.setSelectedIndex(1); // n=2 por defecto

        confirmButton = createActionButton("Crear árbol");
        confirmButton.addActionListener(e -> handleConfirm());

        configRow.add(nLabel);
        configRow.add(nComboBox);
        configRow.add(confirmButton);

        panel.add(configRow);
        panel.add(Box.createVerticalStrut(5));

        // Etiqueta informativa de configuración
        configInfoLabel = new JLabel("Selecciona n y presiona 'Crear árbol' para comenzar.");
        configInfoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        configInfoLabel.setForeground(TEXT_SECONDARY);
        configInfoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(configInfoLabel);

        return panel;
    }

    // ─── Panel de acciones ───────────────────────────────────────────

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
        keyField.setEnabled(false);
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
        restartButton = createActionButton("Reiniciar");

        insertButton.setEnabled(false);
        searchButton.setEnabled(false);
        deleteButton.setEnabled(false);
        restartButton.setEnabled(false);

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

        // Info label (muestra conversión binaria y grupos)
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

    private void handleConfirm() {
        int n = (Integer) nComboBox.getSelectedItem();
        arbol = new ArbolMultipleResiduo(n);

        int M = arbol.getM();
        int niveles = arbol.getNiveles();

        configInfoLabel.setText("n = " + n + " bits → M = " + M
                + " hijos por nodo → " + niveles + " niveles (8 bits)");

        nComboBox.setEnabled(false);
        confirmButton.setEnabled(false);
        keyField.setEnabled(true);
        insertButton.setEnabled(true);
        searchButton.setEnabled(true);
        deleteButton.setEnabled(true);
        restartButton.setEnabled(true);

        infoLabel.setText(" ");
        setStatus("Árbol creado. Puedes insertar claves.", false);
        treeCanvas.repaint();
    }

    private void handleInsert() {
        if (arbol == null) {
            setStatus("Primero configura el árbol.", true);
            return;
        }
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
            ClaveArbol.validar8Bits(clave);
        } catch (IllegalArgumentException ex) {
            setStatus("Error: " + ex.getMessage(), true);
            return;
        }

        String info = ClaveArbol.obtenerInfo8Bits(clave);
        List<String> grupos = arbol.obtenerGrupos(clave);
        infoLabel.setText(info + " → Grupos: " + grupos);

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
            setStatus("Clave '" + claveF + "' insertada. Claves en el árbol: "
                    + arbol.getContador(), false);
            treeCanvas.repaint();
        });
    }

    private void handleSearch() {
        if (arbol == null) {
            setStatus("Primero configura el árbol.", true);
            return;
        }
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
            ClaveArbol.validar8Bits(clave);
        } catch (IllegalArgumentException ex) {
            setStatus("Error: " + ex.getMessage(), true);
            return;
        }

        String info = ClaveArbol.obtenerInfo8Bits(clave);
        List<String> grupos = arbol.obtenerGrupos(clave);
        infoLabel.setText(info + " → Grupos: " + grupos);

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
        if (arbol == null) {
            setStatus("Primero configura el árbol.", true);
            return;
        }
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
            ClaveArbol.validar8Bits(clave);
        } catch (IllegalArgumentException ex) {
            setStatus("Error: " + ex.getMessage(), true);
            return;
        }

        String info = ClaveArbol.obtenerInfo8Bits(clave);
        List<String> grupos = arbol.obtenerGrupos(clave);
        infoLabel.setText(info + " → Grupos: " + grupos);

        if (!arbol.buscar(clave)) {
            setStatus("La clave '" + clave + "' no existe en el árbol.", true);
            return;
        }

        List<Nodo> path = computeSearchPath(clave);

        final String claveF = clave;
        runAnimation(path, claveF, AnimationType.DELETE, () -> {
            try {
                arbol.eliminar(claveF);
                setStatus("Clave '" + claveF + "' eliminada. Claves en el árbol: "
                        + arbol.getContador(), false);
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
        arbol = null;

        nComboBox.setEnabled(true);
        confirmButton.setEnabled(true);
        keyField.setEnabled(false);
        insertButton.setEnabled(false);
        searchButton.setEnabled(false);
        deleteButton.setEnabled(false);
        restartButton.setEnabled(false);

        infoLabel.setText(" ");
        configInfoLabel.setText("Selecciona n y presiona 'Crear árbol' para comenzar.");
        setStatus("Árbol reiniciado. Configura n para crear uno nuevo.", false);
        treeCanvas.repaint();
    }

    // ─── Ruta de animación ───────────────────────────────────────────

    /**
     * Calcula la lista de nodos visitados al insertar (antes de la inserción real).
     */
    private List<Nodo> computeInsertPath(String clave) {
        List<Nodo> path = new ArrayList<>();
        if (arbol == null) {
            return path;
        }
        Nodo raiz = arbol.getRaiz();
        if (raiz == null) {
            return path;
        }
        path.add(raiz);

        String bits = ClaveArbol.claveABinario8Bits(clave);
        Nodo actual = raiz;
        int n = arbol.getN();
        int niveles = arbol.getNiveles();

        for (int nivel = 0; nivel < niveles; nivel++) {
            int inicio = nivel * n;
            String grupo = bits.substring(inicio, inicio + n);
            int indice = Integer.parseInt(grupo, 2);

            Nodo siguiente = actual.hijos[indice];
            if (siguiente == null) {
                break;
            }
            path.add(siguiente);
            if (siguiente.clave != null) {
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
        if (arbol == null) {
            return path;
        }
        Nodo raiz = arbol.getRaiz();
        if (raiz == null) {
            return path;
        }
        path.add(raiz);

        String bits = ClaveArbol.claveABinario8Bits(clave);
        Nodo actual = raiz;
        int n = arbol.getN();
        int niveles = arbol.getNiveles();

        for (int nivel = 0; nivel < niveles; nivel++) {
            int inicio = nivel * n;
            String grupo = bits.substring(inicio, inicio + n);
            int indice = Integer.parseInt(grupo, 2);

            Nodo siguiente = actual.hijos[indice];
            if (siguiente == null) {
                break;
            }
            path.add(siguiente);
            if (siguiente.clave != null) {
                break;
            }
            actual = siguiente;
        }
        return path;
    }

    // ─── Animación ───────────────────────────────────────────────────

    private void runAnimation(List<Nodo> path, String targetKey,
            AnimationType type, Runnable onFinish) {
        animPath = path;
        animStep = -1;
        animTargetKey = targetKey;
        animType = type;
        animFinishCallback = onFinish;
        animationRunning = true;
        setActionsEnabled(false);

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
            setActionsEnabled(true);
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
        setActionsEnabled(true);
        treeCanvas.repaint();
    }

    private void setActionsEnabled(boolean enabled) {
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
     * Canvas que dibuja el árbol M-ario con nodos circulares.
     * Los nodos de enlace (clave=null) se dibujan con borde punteado y color tenue.
     * Los nodos con dato se dibujan con borde sólido.
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
            if (arbol == null) {
                return new Dimension(600, 400);
            }
            Nodo raiz = arbol.getRaiz();
            if (raiz == null) {
                return new Dimension(600, 400);
            }
            int depth = getDepth(raiz);
            int leaves = getLeafCount(raiz);
            int width = Math.max(600, leaves * (NODE_RADIUS * 2 + MIN_HORIZONTAL_GAP));
            int height = Math.max(400, depth * VERTICAL_GAP + 100);
            return new Dimension(width, height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            if (arbol == null) {
                drawMessage(g2, "Configura el número de bits (n) para crear el árbol.");
                return;
            }

            Nodo raiz = arbol.getRaiz();
            if (raiz == null || !tieneAlgunHijo(raiz)) {
                drawMessage(g2, "Árbol vacío. Inserta una clave para comenzar.");
                return;
            }

            nodePositions.clear();

            int treeWidth = computeSubtreeWidth(raiz);
            int startX = Math.max(getWidth() / 2, treeWidth / 2 + 20);
            int startY = 40;

            computePositions(raiz, startX, startY, treeWidth);

            drawEdges(g2, raiz);
            drawNodes(g2, raiz);
            drawLegend(g2);
        }

        private void drawMessage(Graphics2D g2, String msg) {
            g2.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            g2.setColor(TEXT_SECONDARY);
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(msg)) / 2;
            int y = getHeight() / 2;
            g2.drawString(msg, x, y);
        }

        private boolean tieneAlgunHijo(Nodo nodo) {
            if (nodo == null) {
                return false;
            }
            for (Nodo h : nodo.hijos) {
                if (h != null) {
                    return true;
                }
            }
            return false;
        }

        private int computeSubtreeWidth(Nodo nodo) {
            if (nodo == null) {
                return 0;
            }

            int totalChildWidth = 0;
            int childCount = 0;
            for (Nodo hijo : nodo.hijos) {
                if (hijo != null) {
                    totalChildWidth += computeSubtreeWidth(hijo);
                    childCount++;
                }
            }

            if (childCount == 0) {
                return NODE_RADIUS * 2 + MIN_HORIZONTAL_GAP;
            }

            return Math.max(NODE_RADIUS * 2 + MIN_HORIZONTAL_GAP, totalChildWidth);
        }

        private void computePositions(Nodo nodo, int x, int y, int span) {
            if (nodo == null) {
                return;
            }

            nodePositions.put(getNodeId(nodo), new Point(x, y));

            int childY = y + VERTICAL_GAP;
            int M = nodo.hijos.length;
            int minSlotWidth = NODE_RADIUS * 2 + MIN_HORIZONTAL_GAP;

            // Recolectar hijos no nulos con sus anchos de subárbol
            List<Integer> indices = new ArrayList<>();
            List<Integer> widths = new ArrayList<>();
            int totalWidth = 0;

            for (int i = 0; i < M; i++) {
                if (nodo.hijos[i] != null) {
                    int w = Math.max(minSlotWidth, computeSubtreeWidth(nodo.hijos[i]));
                    indices.add(i);
                    widths.add(w);
                    totalWidth += w;
                }
            }

            if (indices.isEmpty()) {
                return;
            }

            // Caso un solo hijo: desplazar según su posición relativa en M slots
            if (indices.size() == 1) {
                int idx = indices.get(0);
                double centerFrac = (double) (idx + 0.5) / M;
                int offset = (int) ((centerFrac - 0.5)
                        * Math.max(span, minSlotWidth * 2));
                computePositions(nodo.hijos[idx],
                        x + offset, childY, widths.get(0));
                return;
            }

            // Múltiples hijos: espaciado proporcional a distancia de índices
            int gapUnit = minSlotWidth / 2;
            int totalGaps = 0;
            for (int i = 1; i < indices.size(); i++) {
                totalGaps += (indices.get(i) - indices.get(i - 1)) * gapUnit;
            }

            int totalNeeded = totalWidth + totalGaps;
            int actualSpan = Math.max(span, totalNeeded);
            double scale = totalNeeded > 0
                    ? (double) actualSpan / totalNeeded : 1.0;

            int currentX = x - actualSpan / 2;

            for (int i = 0; i < indices.size(); i++) {
                int scaledW = (int) (widths.get(i) * scale);
                int childX = currentX + scaledW / 2;
                computePositions(nodo.hijos[indices.get(i)],
                        childX, childY, scaledW);
                currentX += scaledW;

                if (i < indices.size() - 1) {
                    int indexDist = indices.get(i + 1) - indices.get(i);
                    currentX += (int) (indexDist * gapUnit * scale);
                }
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
            int nBits = arbol.getN();

            for (int i = 0; i < nodo.hijos.length; i++) {
                if (nodo.hijos[i] != null) {
                    Point childPos = nodePositions.get(getNodeId(nodo.hijos[i]));
                    if (childPos != null) {
                        boolean highlighted = isEdgeHighlighted(nodo, nodo.hijos[i]);
                        g2.setColor(highlighted ? getAnimColor() : EDGE_COLOR);
                        g2.setStroke(new BasicStroke(highlighted ? 3f : 2f));
                        g2.drawLine(parentPos.x, parentPos.y + NODE_RADIUS,
                                childPos.x, childPos.y - NODE_RADIUS);

                        // Etiqueta del arco: grupo de bits
                        String label = String.format("%" + nBits + "s",
                                Integer.toBinaryString(i)).replace(' ', '0');
                        g2.setColor(TEXT_SECONDARY);
                        g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                        FontMetrics fm = g2.getFontMetrics();

                        int midX = (parentPos.x + childPos.x) / 2;
                        int midY = (parentPos.y + NODE_RADIUS
                                + childPos.y - NODE_RADIUS) / 2;

                        // Desplazar etiqueta al lado de la línea
                        int offsetX = childPos.x >= parentPos.x
                                ? 5 : -fm.stringWidth(label) - 5;
                        g2.drawString(label, midX + offsetX, midY);
                    }
                }
            }

            g2.setStroke(defaultStroke);

            // Recursar a hijos
            for (Nodo hijo : nodo.hijos) {
                if (hijo != null) {
                    drawEdges(g2, hijo);
                }
            }
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
                g2.setColor(highlighted ? getAnimColor().darker()
                        : LINK_NODE_BORDER_COLOR);
                g2.setStroke(new BasicStroke(highlighted ? 3f : 2f,
                        BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                        10f, dashPattern, 0f));
            } else {
                g2.setColor(highlighted ? getAnimColor().darker()
                        : NODE_BORDER_COLOR);
                g2.setStroke(new BasicStroke(highlighted ? 3f : 2f));
            }
            g2.drawOval(pos.x - NODE_RADIUS, pos.y - NODE_RADIUS,
                    NODE_RADIUS * 2, NODE_RADIUS * 2);

            // Texto
            if (nodo.clave != null) {
                // Nodo con dato: mostrar la clave
                g2.setColor(TEXT_PRIMARY);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
                FontMetrics fm = g2.getFontMetrics();
                int textX = pos.x - fm.stringWidth(nodo.clave) / 2;
                int textY = pos.y + fm.getAscent() / 2 - 1;
                g2.drawString(nodo.clave, textX, textY);

                // Mostrar binario (8 bits) debajo del nodo
                String bits = ClaveArbol.charABinario8Bits(nodo.clave.charAt(0));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
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

            // Recursar a hijos
            for (Nodo hijo : nodo.hijos) {
                drawNodes(g2, hijo);
            }
        }

        private void drawLegend(Graphics2D g2) {
            int lx = 10;
            int ly = getHeight() - 55;

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2.setColor(TEXT_SECONDARY);
            g2.drawString("n = " + arbol.getN() + " bits    M = " + arbol.getM()
                    + " hijos    Niveles = " + arbol.getNiveles(), lx, ly);
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
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND, 10f, dash, 0f));
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
            if (animStep >= animPath.size() && nodo.clave != null
                    && nodo.clave.equals(animTargetKey)) {
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
            int maxChildDepth = 0;
            for (Nodo hijo : nodo.hijos) {
                maxChildDepth = Math.max(maxChildDepth, getDepth(hijo));
            }
            return 1 + maxChildDepth;
        }

        private int getLeafCount(Nodo nodo) {
            if (nodo == null) {
                return 0;
            }
            boolean hasChild = false;
            int count = 0;
            for (Nodo hijo : nodo.hijos) {
                if (hijo != null) {
                    hasChild = true;
                    count += getLeafCount(hijo);
                }
            }
            return hasChild ? Math.max(1, count) : 1;
        }
    }
}
