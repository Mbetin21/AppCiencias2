package com.appciencias.views;

import com.appciencias.algorithms.CentroGrafo;
import com.appciencias.algorithms.Grafo;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;

/**
 * Panel interactivo para el cálculo del centro y bicentro de un grafo no
 * dirigido sin pesos.
 *
 * El usuario construye un grafo G (vértices y aristas), presiona "Calcular" y
 * la sección de resultado muestra la ejecución del algoritmo paso a paso:
 * cada iteración resalta las hojas eliminadas (naranja), los vértices vivos
 * (azul), los ya eliminados en pasos anteriores (gris) y, en la iteración
 * final, el centro o bicentro encontrado (verde). Un trace textual completo
 * acompaña al diagrama.
 *
 * El cálculo se delega a {@link com.appciencias.algorithms.CentroGrafo}.
 */
public class CentroPanel extends JPanel {

    // Paleta consistente con el resto de la app
    private final Color BACKGROUND_COLOR = new Color(245, 245, 250);
    private final Color PANEL_COLOR = new Color(235, 235, 245);
    private final Color BORDER_COLOR = new Color(200, 200, 210);
    private final Color BUTTON_COLOR = new Color(230, 230, 240);
    private final Color BUTTON_HOVER_COLOR = new Color(220, 220, 235);
    private final Color BUTTON_PRIMARY = new Color(210, 222, 240);
    private final Color BUTTON_PRIMARY_HOVER = new Color(195, 210, 232);
    private final Color BUTTON_DISABLED_BG = new Color(228, 228, 232);
    private final Color BUTTON_DISABLED_FG = new Color(180, 180, 190);
    private final Color TEXT_PRIMARY = new Color(70, 70, 80);
    private final Color TEXT_SECONDARY = new Color(100, 100, 110);
    private final Color RESULT_PANEL = new Color(232, 238, 248);
    private final Color STATUS_OK = new Color(60, 110, 80);
    private final Color STATUS_ERROR = new Color(170, 60, 60);

    // Modelo
    private final Grafo g = new Grafo("G");
    private CentroGrafo.Resultado resultado = null;
    private int currentIter = 0;

    // UI del editor
    private GrafoCanvas canvasG;
    private JTextField vertField, edgeFrom, edgeTo;
    private JLabel verticesGLabel, aristasGLabel;

    // UI del resultado
    private GrafoCanvas canvasResultado;
    private JLabel resultadoTituloLabel;
    private JLabel iteracionLabel;
    private JButton prevBtn, nextBtn;
    private JLabel verticesIterLabel, hojasIterLabel;
    private JTextArea traceArea;
    private JLabel statusLabel;

    public CentroPanel() {
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BACKGROUND_COLOR);
        content.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        content.add(createHeader());
        content.add(Box.createVerticalStrut(15));
        content.add(createEditorSection());
        content.add(Box.createVerticalStrut(15));
        content.add(createCalcularSection());
        content.add(Box.createVerticalStrut(15));
        content.add(createResultSection());
        content.add(Box.createVerticalStrut(10));
        content.add(createStatusBar());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBackground(BACKGROUND_COLOR);
        scroll.getViewport().setBackground(BACKGROUND_COLOR);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(scroll, BorderLayout.CENTER);
    }

    // -------------------- HEADER --------------------

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(BACKGROUND_COLOR);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Centro y Bicentro");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(8));

        JTextArea desc = new JTextArea(
                "Construya un grafo no dirigido y presione Calcular para "
                + "identificar su centro (un vértice) o bicentro (dos vértices) "
                + "eliminando hojas iterativamente. El resultado se muestra paso "
                + "a paso: hojas eliminadas en cada iteración (naranja), vértices "
                + "vivos (azul), ya eliminados en pasos previos (gris) y centro "
                + "o bicentro final (verde).");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        desc.setForeground(TEXT_SECONDARY);
        desc.setBackground(BACKGROUND_COLOR);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setEditable(false);
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(desc);

        return header;
    }

    // -------------------- EDITOR DE G --------------------

    private JPanel createEditorSection() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("G");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(8));

        canvasG = new GrafoCanvas();
        canvasG.setAlignmentX(Component.LEFT_ALIGNMENT);
        canvasG.setPreferredSize(new Dimension(600, 220));
        canvasG.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        canvasG.setGrafo(g);
        panel.add(canvasG);
        panel.add(Box.createVerticalStrut(10));

        // Entrada de vértices y aristas (una sola fila, ancho completo)
        JPanel inputs = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        inputs.setBackground(PANEL_COLOR);
        inputs.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputs.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        inputs.add(createInlineLabel("Vértices:"));
        vertField = new JTextField(10);
        vertField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        inputs.add(vertField);
        JButton addVBtn = createSmallButton("Agregar", false);
        inputs.add(addVBtn);

        inputs.add(Box.createHorizontalStrut(15));
        inputs.add(createInlineLabel("Arista:"));
        edgeFrom = new JTextField(4);
        edgeFrom.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        inputs.add(edgeFrom);
        inputs.add(createInlineLabel("—"));
        edgeTo = new JTextField(4);
        edgeTo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        inputs.add(edgeTo);
        JButton addEBtn = createSmallButton("Agregar", false);
        inputs.add(addEBtn);

        inputs.add(Box.createHorizontalStrut(15));
        JButton clearBtn = createSmallButton("Limpiar", false);
        inputs.add(clearBtn);

        panel.add(inputs);
        panel.add(Box.createVerticalStrut(8));

        verticesGLabel = new JLabel(g.getVerticesStr());
        verticesGLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        verticesGLabel.setForeground(TEXT_SECONDARY);
        verticesGLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        aristasGLabel = new JLabel(g.getAristasStr());
        aristasGLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        aristasGLabel.setForeground(TEXT_SECONDARY);
        aristasGLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(verticesGLabel);
        panel.add(Box.createVerticalStrut(2));
        panel.add(aristasGLabel);

        // Acciones
        addVBtn.addActionListener(e -> handleAddVertex());
        vertField.addActionListener(e -> addVBtn.doClick());
        addEBtn.addActionListener(e -> handleAddEdge());
        edgeTo.addActionListener(e -> addEBtn.doClick());
        clearBtn.addActionListener(e -> handleLimpiar());

        return panel;
    }

    // -------------------- BOTÓN CALCULAR --------------------

    private JPanel createCalcularSection() {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setBackground(BACKGROUND_COLOR);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JButton calcularBtn = createSmallButton("Calcular Centro / Bicentro", true);
        calcularBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        calcularBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 24, 10, 24)
        ));
        calcularBtn.addActionListener(e -> handleCalcular());

        row.add(Box.createHorizontalGlue());
        row.add(calcularBtn);
        row.add(Box.createHorizontalGlue());
        return row;
    }

    // -------------------- RESULTADO --------------------

    private JPanel createResultSection() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(RESULT_PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        resultadoTituloLabel = new JLabel("Resultado");
        resultadoTituloLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        resultadoTituloLabel.setForeground(TEXT_PRIMARY);
        resultadoTituloLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(resultadoTituloLabel);
        panel.add(Box.createVerticalStrut(10));

        // Fila de navegación
        JPanel navRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        navRow.setBackground(RESULT_PANEL);
        navRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        navRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        iteracionLabel = new JLabel("Iteración —");
        iteracionLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        iteracionLabel.setForeground(TEXT_PRIMARY);
        navRow.add(iteracionLabel);

        prevBtn = createSmallButton("◀", false);
        nextBtn = createSmallButton("▶", false);
        prevBtn.setEnabled(false);
        nextBtn.setEnabled(false);
        navRow.add(prevBtn);
        navRow.add(nextBtn);

        prevBtn.addActionListener(e -> mostrarIteracion(currentIter - 1));
        nextBtn.addActionListener(e -> mostrarIteracion(currentIter + 1));

        panel.add(navRow);
        panel.add(Box.createVerticalStrut(8));

        // Canvas del resultado
        canvasResultado = new GrafoCanvas();
        canvasResultado.setAlignmentX(Component.LEFT_ALIGNMENT);
        canvasResultado.setPreferredSize(new Dimension(600, 280));
        canvasResultado.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
        panel.add(canvasResultado);
        panel.add(Box.createVerticalStrut(8));

        // Etiquetas de estado por iteración
        verticesIterLabel = new JLabel("S = {}");
        verticesIterLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        verticesIterLabel.setForeground(TEXT_SECONDARY);
        verticesIterLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        hojasIterLabel = new JLabel("Hojas eliminadas en este paso: —");
        hojasIterLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hojasIterLabel.setForeground(TEXT_SECONDARY);
        hojasIterLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(verticesIterLabel);
        panel.add(Box.createVerticalStrut(2));
        panel.add(hojasIterLabel);
        panel.add(Box.createVerticalStrut(12));

        // Trace completo
        JLabel traceTitle = new JLabel("Trace completo");
        traceTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        traceTitle.setForeground(TEXT_PRIMARY);
        traceTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(traceTitle);
        panel.add(Box.createVerticalStrut(4));

        traceArea = new JTextArea();
        traceArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        traceArea.setForeground(TEXT_PRIMARY);
        traceArea.setBackground(new Color(248, 250, 253));
        traceArea.setEditable(false);
        traceArea.setLineWrap(false);
        JScrollPane traceScroll = new JScrollPane(traceArea);
        traceScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        traceScroll.setPreferredSize(new Dimension(600, 110));
        traceScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        traceScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        panel.add(traceScroll);

        return panel;
    }

    // -------------------- STATUS --------------------

    private JPanel createStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bar.setBackground(BACKGROUND_COLOR);
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(TEXT_SECONDARY);
        bar.add(statusLabel);
        return bar;
    }

    // -------------------- HANDLERS DEL EDITOR --------------------

    private void handleAddVertex() {
        String entrada = vertField.getText().trim();
        if (entrada.isEmpty()) {
            setStatus("Escriba uno o más vértices separados por coma.", true);
            return;
        }
        try {
            g.agregarVertices(entrada);
            vertField.setText("");
            canvasG.refresh();
            verticesGLabel.setText(g.getVerticesStr());
            aristasGLabel.setText(g.getAristasStr());
            setStatus("Vértices agregados a G.", false);
        } catch (IllegalArgumentException ex) {
            setStatus(ex.getMessage(), true);
        }
    }

    private void handleAddEdge() {
        String v1 = edgeFrom.getText().trim();
        String v2 = edgeTo.getText().trim();
        if (v1.isEmpty() || v2.isEmpty()) {
            setStatus("Indique los dos extremos de la arista.", true);
            return;
        }
        try {
            g.agregarArista(v1, v2);
            edgeFrom.setText("");
            edgeTo.setText("");
            canvasG.refresh();
            verticesGLabel.setText(g.getVerticesStr());
            aristasGLabel.setText(g.getAristasStr());
            setStatus("Arista " + v1 + "—" + v2 + " agregada a G.", false);
        } catch (IllegalArgumentException ex) {
            setStatus(ex.getMessage(), true);
        }
    }

    private void handleLimpiar() {
        g.limpiar();
        canvasG.setGrafo(g);
        verticesGLabel.setText(g.getVerticesStr());
        aristasGLabel.setText(g.getAristasStr());
        setStatus("G vaciado.", false);
    }

    // -------------------- HANDLER DE CÁLCULO --------------------

    private void handleCalcular() {
        if (g.getNumVertices() == 0) {
            setStatus("Agregue al menos un vértice a G antes de calcular.", true);
            return;
        }
        try {
            CentroGrafo.Resultado res = CentroGrafo.calcular(g);
            resultado = res;
            currentIter = 0;

            // Snapshot del grafo para el canvas de resultado (independiente de G)
            Grafo snapshot = clonarGrafo(g);
            canvasResultado.setGrafo(snapshot);

            resultadoTituloLabel.setText("Resultado · " + tituloResultado(res));
            traceArea.setText(construirTrace(res));
            traceArea.setCaretPosition(0);

            mostrarIteracion(0);
            setStatus(tituloResultado(res) + " calculado en "
                    + res.iteraciones.size() + " iteración(es).", false);
        } catch (IllegalArgumentException ex) {
            setStatus(ex.getMessage(), true);
        }
    }

    /**
     * Muestra la iteración n en el canvas: colorea hojas eliminadas (LEAF),
     * vértices vivos (NORMAL), eliminados en pasos previos (FADED) y, si es
     * la última iteración con centro o bicentro válido, los marca como CENTER.
     */
    private void mostrarIteracion(int n) {
        if (resultado == null || n < 0 || n >= resultado.iteraciones.size()) {
            return;
        }
        currentIter = n;
        CentroGrafo.Iteracion iter = resultado.iteraciones.get(n);

        Map<String, GrafoCanvas.VertexState> states = new LinkedHashMap<>();

        // FADED: hojas eliminadas en iteraciones anteriores
        for (int k = 1; k < n; k++) {
            for (String v : resultado.iteraciones.get(k).hojasEliminadas) {
                states.put(v, GrafoCanvas.VertexState.FADED);
            }
        }
        // LEAF: hojas eliminadas en este paso
        for (String v : iter.hojasEliminadas) {
            states.put(v, GrafoCanvas.VertexState.LEAF);
        }
        // CENTER / NORMAL: vértices vivos en esta iteración
        boolean esFinal = (n == resultado.iteraciones.size() - 1);
        boolean centroValido = esFinal
                && (resultado.centro.size() == 1 || resultado.centro.size() == 2);
        for (String v : iter.vertices) {
            if (centroValido && resultado.centro.contains(v)) {
                states.put(v, GrafoCanvas.VertexState.CENTER);
            } else {
                states.put(v, GrafoCanvas.VertexState.NORMAL);
            }
        }

        canvasResultado.setVertexStates(states);

        iteracionLabel.setText("Iteración " + iter.numero
                + " / " + (resultado.iteraciones.size() - 1));
        verticesIterLabel.setText("S = {" + String.join(", ", iter.vertices) + "}");
        if (iter.hojasEliminadas.isEmpty()) {
            hojasIterLabel.setText("Hojas eliminadas en este paso: —");
        } else {
            hojasIterLabel.setText("Hojas eliminadas en este paso: {"
                    + String.join(", ", iter.hojasEliminadas) + "}");
        }

        actualizarNavBotones();
    }

    private void actualizarNavBotones() {
        boolean hayResultado = (resultado != null);
        int total = hayResultado ? resultado.iteraciones.size() : 0;
        setNavEnabled(prevBtn, hayResultado && currentIter > 0);
        setNavEnabled(nextBtn, hayResultado && currentIter < total - 1);
    }

    private void setNavEnabled(JButton b, boolean enabled) {
        b.setEnabled(enabled);
        if (enabled) {
            b.setBackground(BUTTON_COLOR);
            b.setForeground(TEXT_PRIMARY);
        } else {
            b.setBackground(BUTTON_DISABLED_BG);
            b.setForeground(BUTTON_DISABLED_FG);
        }
    }

    // -------------------- HELPERS --------------------

    private String tituloResultado(CentroGrafo.Resultado res) {
        int n = res.centro.size();
        if (n == 1) {
            return "Centro = {" + res.centro.get(0) + "}";
        } else if (n == 2) {
            return "Bicentro = {" + String.join(", ", res.centro) + "}";
        } else if (n == 0) {
            return "Sin vértices restantes";
        }
        return "Sin centro/bicentro (vértices restantes: {"
                + String.join(", ", res.centro) + "})";
    }

    private String construirTrace(CentroGrafo.Resultado res) {
        StringBuilder sb = new StringBuilder();
        for (CentroGrafo.Iteracion it : res.iteraciones) {
            sb.append(it.toString()).append("\n");
        }
        sb.append("\nResultado: ");
        int n = res.centro.size();
        if (n == 1) {
            sb.append("CENTRO = {").append(res.centro.get(0)).append("}");
        } else if (n == 2) {
            sb.append("BICENTRO = {").append(String.join(", ", res.centro)).append("}");
        } else if (n == 0) {
            sb.append("Sin vértices restantes.");
        } else {
            sb.append("El algoritmo terminó sin reducir a 1 ó 2 vértices "
                    + "(posible ciclo o desconexión). Vértices restantes: {")
                    .append(String.join(", ", res.centro)).append("}");
        }
        return sb.toString();
    }

    private Grafo clonarGrafo(Grafo orig) {
        Grafo nuevo = new Grafo(orig.getNombre());
        for (String v : orig.getVertices()) {
            nuevo.agregarVertice(v);
        }
        for (Grafo.Arista a : orig.getAristas()) {
            nuevo.agregarArista(a.v1, a.v2);
        }
        return nuevo;
    }

    private JLabel createInlineLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(TEXT_SECONDARY);
        return l;
    }

    private void setStatus(String msg, boolean error) {
        statusLabel.setText(msg);
        statusLabel.setForeground(error ? STATUS_ERROR : STATUS_OK);
    }

    private JButton createSmallButton(String text, boolean primary) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        b.setForeground(TEXT_PRIMARY);
        b.setBackground(primary ? BUTTON_PRIMARY : BUTTON_COLOR);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        final Color base = primary ? BUTTON_PRIMARY : BUTTON_COLOR;
        final Color hover = primary ? BUTTON_PRIMARY_HOVER : BUTTON_HOVER_COLOR;
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (b.isEnabled()) {
                    b.setBackground(hover);
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (b.isEnabled()) {
                    b.setBackground(base);
                }
            }
        });
        return b;
    }
}
