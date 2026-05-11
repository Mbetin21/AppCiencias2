package com.appciencias.views;

import com.appciencias.algorithms.Grafo;
import java.awt.*;
import javax.swing.*;

/**
 * Panel interactivo de operaciones sobre grafos no dirigidos sin pesos.
 *
 * Permite al usuario construir dos grafos (G1 y G2) agregando vértices y
 * aristas, y aplicar sobre ellos:
 *   Unarias  → complemento, fusión de vértices, contracción de arista, limpiar
 *   Binarias → unión (∪), intersección (∩), suma anillo (⊕), suma (+)
 *
 * Todas las operaciones se delegan a com.appciencias.algorithms.Grafo.
 */
public class OperacionesPanel extends JPanel {

    // Paleta consistente con el resto de la app
    private final Color BACKGROUND_COLOR = new Color(245, 245, 250);
    private final Color PANEL_COLOR = new Color(235, 235, 245);
    private final Color BORDER_COLOR = new Color(200, 200, 210);
    private final Color BUTTON_COLOR = new Color(230, 230, 240);
    private final Color BUTTON_HOVER_COLOR = new Color(220, 220, 235);
    private final Color BUTTON_PRIMARY = new Color(210, 222, 240);
    private final Color BUTTON_PRIMARY_HOVER = new Color(195, 210, 232);
    private final Color TEXT_PRIMARY = new Color(70, 70, 80);
    private final Color TEXT_SECONDARY = new Color(100, 100, 110);
    private final Color RESULT_PANEL = new Color(232, 238, 248);
    private final Color STATUS_OK = new Color(60, 110, 80);
    private final Color STATUS_ERROR = new Color(170, 60, 60);

    // Modelo
    private final Grafo g1 = new Grafo("G1");
    private final Grafo g2 = new Grafo("G2");
    private Grafo resultado = null;

    // Componentes
    private GrafoCanvas canvasG1, canvasG2, canvasResultado;
    private JTextField vertFieldG1, vertFieldG2;
    private JTextField edgeFromG1, edgeToG1, edgeFromG2, edgeToG2;
    private JLabel verticesG1Label, aristasG1Label;
    private JLabel verticesG2Label, aristasG2Label;
    private JLabel resultadoTituloLabel;
    private JLabel verticesResultadoLabel, aristasResultadoLabel;
    private JLabel statusLabel;
    private JComboBox<String> unaryTargetCombo;
    private JTextField unaryV1Field, unaryV2Field;

    public OperacionesPanel() {
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
        content.add(createGraphsSection());
        content.add(Box.createVerticalStrut(15));
        content.add(createUnarySection());
        content.add(Box.createVerticalStrut(10));
        content.add(createBinarySection());
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

        JLabel title = new JLabel("Operaciones");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(8));

        JTextArea desc = new JTextArea(
                "Construya dos grafos no dirigidos y aplique sobre ellos las "
                + "operaciones unarias (complemento, fusión de vértices, "
                + "contracción de arista) y binarias (unión, intersección, "
                + "suma anillo y suma).");
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

    // -------------------- GRAFOS G1 / G2 --------------------

    private JPanel createGraphsSection() {
        JPanel section = new JPanel(new GridLayout(1, 2, 15, 0));
        section.setBackground(BACKGROUND_COLOR);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 420));

        section.add(createGraphEditor(true));
        section.add(createGraphEditor(false));

        return section;
    }

    private JPanel createGraphEditor(boolean isG1) {
        final Grafo grafo = isG1 ? g1 : g2;
        final String etiqueta = isG1 ? "G1" : "G2";

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JLabel title = new JLabel(etiqueta);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(8));

        // Canvas
        GrafoCanvas canvas = new GrafoCanvas();
        canvas.setAlignmentX(Component.LEFT_ALIGNMENT);
        canvas.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        canvas.setGrafo(grafo);
        if (isG1) {
            canvasG1 = canvas;
        } else {
            canvasG2 = canvas;
        }
        panel.add(canvas);
        panel.add(Box.createVerticalStrut(10));

        // Fila vértice (Agregar / Eliminar comparten campo)
        JPanel vRow = createInputRow();
        vRow.add(createInlineLabel("Vértices:"));
        JTextField vField = new JTextField(8);
        vField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        vRow.add(vField);
        JButton addVBtn = createSmallButton("Agregar", false);
        JButton delVBtn = createSmallButton("Eliminar", false);
        vRow.add(addVBtn);
        vRow.add(delVBtn);
        if (isG1) {
            vertFieldG1 = vField;
        } else {
            vertFieldG2 = vField;
        }
        panel.add(vRow);
        panel.add(Box.createVerticalStrut(5));

        // Fila arista
        JPanel eRow = createInputRow();
        eRow.add(createInlineLabel("Arista:"));
        JTextField eFrom = new JTextField(3);
        eFrom.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        eRow.add(eFrom);
        eRow.add(createInlineLabel("—"));
        JTextField eTo = new JTextField(3);
        eTo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        eRow.add(eTo);
        JButton addEBtn = createSmallButton("Agregar", false);
        JButton delEBtn = createSmallButton("Eliminar", false);
        eRow.add(addEBtn);
        eRow.add(delEBtn);
        if (isG1) {
            edgeFromG1 = eFrom;
            edgeToG1 = eTo;
        } else {
            edgeFromG2 = eFrom;
            edgeToG2 = eTo;
        }
        panel.add(eRow);
        panel.add(Box.createVerticalStrut(8));

        // Etiquetas S / A
        JLabel vCount = new JLabel(grafo.getVerticesStr());
        vCount.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        vCount.setForeground(TEXT_SECONDARY);
        vCount.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel aCount = new JLabel(grafo.getAristasStr());
        aCount.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        aCount.setForeground(TEXT_SECONDARY);
        aCount.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (isG1) {
            verticesG1Label = vCount;
            aristasG1Label = aCount;
        } else {
            verticesG2Label = vCount;
            aristasG2Label = aCount;
        }
        panel.add(vCount);
        panel.add(Box.createVerticalStrut(2));
        panel.add(aCount);
        panel.add(Box.createVerticalStrut(10));

        // Botones por-grafo
        JPanel btnRow = createInputRow();
        JButton complBtn = createSmallButton("Complemento", true);
        JButton clearBtn = createSmallButton("Limpiar", false);
        btnRow.add(complBtn);
        btnRow.add(clearBtn);
        panel.add(btnRow);

        // Acciones
        addVBtn.addActionListener(e -> handleAddVertex(grafo, vField, canvas,
                vCount, aCount, etiqueta));
        vField.addActionListener(e -> addVBtn.doClick());
        delVBtn.addActionListener(e -> handleDeleteVertex(grafo, vField, canvas,
                vCount, aCount, etiqueta));

        addEBtn.addActionListener(e -> handleAddEdge(grafo, eFrom, eTo, canvas,
                vCount, aCount, etiqueta));
        eTo.addActionListener(e -> addEBtn.doClick());
        delEBtn.addActionListener(e -> handleDeleteEdge(grafo, eFrom, eTo, canvas,
                vCount, aCount, etiqueta));

        complBtn.addActionListener(e -> handleComplemento(grafo, etiqueta));
        clearBtn.addActionListener(e -> handleLimpiar(grafo, canvas, vCount,
                aCount, etiqueta));

        return panel;
    }

    private JPanel createInputRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        row.setBackground(PANEL_COLOR);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        return row;
    }

    private JLabel createInlineLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(TEXT_SECONDARY);
        return l;
    }

    // -------------------- MODIFICACIONES UNARIAS --------------------

    private JPanel createUnarySection() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JLabel title = new JLabel("Modificaciones unarias (modifican el grafo seleccionado)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(6));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        row.setBackground(PANEL_COLOR);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        row.add(createInlineLabel("Sobre:"));
        unaryTargetCombo = new JComboBox<>(new String[]{"G1", "G2"});
        unaryTargetCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        row.add(unaryTargetCombo);

        row.add(createInlineLabel("v1:"));
        unaryV1Field = new JTextField(4);
        unaryV1Field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        row.add(unaryV1Field);

        row.add(createInlineLabel("v2:"));
        unaryV2Field = new JTextField(4);
        unaryV2Field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        row.add(unaryV2Field);

        JButton fusionBtn = createSmallButton("Fusionar vértices", true);
        JButton contraerBtn = createSmallButton("Contraer arista", true);
        row.add(fusionBtn);
        row.add(contraerBtn);
        panel.add(row);

        fusionBtn.addActionListener(e -> handleFusionar());
        contraerBtn.addActionListener(e -> handleContraer());

        return panel;
    }

    // -------------------- OPERACIONES BINARIAS --------------------

    private JPanel createBinarySection() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JLabel title = new JLabel("Operaciones binarias (resultado en la sección inferior)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(6));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        row.setBackground(PANEL_COLOR);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton unionBtn = createSmallButton("Union", true);
        JButton interBtn = createSmallButton("Interseccion", true);
        JButton anilloBtn = createSmallButton("Suma anillo", true);
        JButton sumaBtn = createSmallButton("Suma", true);
        row.add(unionBtn);
        row.add(interBtn);
        row.add(anilloBtn);
        row.add(sumaBtn);
        panel.add(row);

        unionBtn.addActionListener(e -> aplicarBinaria("G1 ∪ G2", Grafo.union(g1, g2)));
        interBtn.addActionListener(e -> aplicarBinaria("G1 ∩ G2", Grafo.interseccion(g1, g2)));
        anilloBtn.addActionListener(e -> aplicarBinaria("G1 ⊕ G2", Grafo.sumaAnillo(g1, g2)));
        sumaBtn.addActionListener(e -> aplicarBinaria("G1 + G2", Grafo.suma(g1, g2)));

        return panel;
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
        panel.add(Box.createVerticalStrut(8));

        canvasResultado = new GrafoCanvas();
        canvasResultado.setAlignmentX(Component.LEFT_ALIGNMENT);
        canvasResultado.setPreferredSize(new Dimension(600, 260));
        canvasResultado.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        panel.add(canvasResultado);
        panel.add(Box.createVerticalStrut(8));

        verticesResultadoLabel = new JLabel("S = {}");
        verticesResultadoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        verticesResultadoLabel.setForeground(TEXT_SECONDARY);
        verticesResultadoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        aristasResultadoLabel = new JLabel("A = {}");
        aristasResultadoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        aristasResultadoLabel.setForeground(TEXT_SECONDARY);
        aristasResultadoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(verticesResultadoLabel);
        panel.add(Box.createVerticalStrut(2));
        panel.add(aristasResultadoLabel);

        return panel;
    }

    // -------------------- STATUS BAR --------------------

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

    // -------------------- HANDLERS --------------------

    private void handleAddVertex(Grafo grafo, JTextField field, GrafoCanvas canvas,
                                 JLabel vLabel, JLabel aLabel, String etiqueta) {
        String entrada = field.getText().trim();
        if (entrada.isEmpty()) {
            setStatus("Escriba uno o más vértices separados por coma.", true);
            return;
        }
        try {
            grafo.agregarVertices(entrada);
            field.setText("");
            canvas.refresh();
            vLabel.setText(grafo.getVerticesStr());
            aLabel.setText(grafo.getAristasStr());
            invalidateResult();
            setStatus("Vértices agregados a " + etiqueta + ".", false);
        } catch (IllegalArgumentException ex) {
            setStatus(ex.getMessage(), true);
        }
    }

    private void handleDeleteVertex(Grafo grafo, JTextField field, GrafoCanvas canvas,
                                    JLabel vLabel, JLabel aLabel, String etiqueta) {
        String entrada = field.getText().trim();
        if (entrada.isEmpty()) {
            setStatus("Escriba el o los vértices a eliminar (separados por coma).", true);
            return;
        }
        try {
            for (String parte : entrada.split(",")) {
                String v = parte.trim();
                if (!v.isEmpty()) {
                    grafo.eliminarVertice(v);
                }
            }
            field.setText("");
            canvas.refresh();
            vLabel.setText(grafo.getVerticesStr());
            aLabel.setText(grafo.getAristasStr());
            invalidateResult();
            setStatus("Vértice(s) eliminado(s) de " + etiqueta + ".", false);
        } catch (IllegalArgumentException ex) {
            setStatus(ex.getMessage(), true);
        }
    }

    private void handleAddEdge(Grafo grafo, JTextField from, JTextField to,
                               GrafoCanvas canvas, JLabel vLabel, JLabel aLabel,
                               String etiqueta) {
        String v1 = from.getText().trim();
        String v2 = to.getText().trim();
        if (v1.isEmpty() || v2.isEmpty()) {
            setStatus("Indique los dos extremos de la arista.", true);
            return;
        }
        try {
            grafo.agregarArista(v1, v2);
            from.setText("");
            to.setText("");
            canvas.refresh();
            vLabel.setText(grafo.getVerticesStr());
            aLabel.setText(grafo.getAristasStr());
            invalidateResult();
            setStatus("Arista " + v1 + "—" + v2 + " agregada a " + etiqueta + ".", false);
        } catch (IllegalArgumentException ex) {
            setStatus(ex.getMessage(), true);
        }
    }

    private void handleDeleteEdge(Grafo grafo, JTextField from, JTextField to,
                                  GrafoCanvas canvas, JLabel vLabel, JLabel aLabel,
                                  String etiqueta) {
        String v1 = from.getText().trim();
        String v2 = to.getText().trim();
        if (v1.isEmpty() || v2.isEmpty()) {
            setStatus("Indique los dos extremos de la arista a eliminar.", true);
            return;
        }
        try {
            grafo.eliminarArista(v1, v2);
            from.setText("");
            to.setText("");
            canvas.refresh();
            vLabel.setText(grafo.getVerticesStr());
            aLabel.setText(grafo.getAristasStr());
            invalidateResult();
            setStatus("Arista " + v1 + "—" + v2 + " eliminada de " + etiqueta + ".", false);
        } catch (IllegalArgumentException ex) {
            setStatus(ex.getMessage(), true);
        }
    }

    private void handleComplemento(Grafo grafo, String etiqueta) {
        if (grafo.getNumVertices() == 0) {
            setStatus("Agregue vértices a " + etiqueta + " antes de calcular su complemento.", true);
            return;
        }
        Grafo complemento = grafo.complemento();
        mostrarResultado("Complemento de " + etiqueta, complemento);

        // Heredar el layout del editor fuente (mismos vértices)
        GrafoCanvas fuente = (grafo == g1) ? canvasG1 : canvasG2;
        java.util.LinkedHashMap<String, double[]> pos = fuente.getNormalizedPositions();
        if (!pos.isEmpty()) {
            canvasResultado.setNormalizedPositions(pos);
        }

        setStatus("Complemento de " + etiqueta + " calculado.", false);
    }

    private void handleLimpiar(Grafo grafo, GrafoCanvas canvas, JLabel vLabel,
                               JLabel aLabel, String etiqueta) {
        grafo.limpiar();
        canvas.setGrafo(grafo);
        vLabel.setText(grafo.getVerticesStr());
        aLabel.setText(grafo.getAristasStr());
        invalidateResult();
        setStatus(etiqueta + " vaciado.", false);
    }

    private void handleFusionar() {
        Grafo objetivo = "G1".equals(unaryTargetCombo.getSelectedItem()) ? g1 : g2;
        String etiqueta = (String) unaryTargetCombo.getSelectedItem();
        String v1 = unaryV1Field.getText().trim();
        String v2 = unaryV2Field.getText().trim();
        if (v1.isEmpty() || v2.isEmpty()) {
            setStatus("Indique los dos vértices a fusionar.", true);
            return;
        }
        try {
            objetivo.fusionarVertices(v1, v2);
            actualizarCanvasYLabels(objetivo);
            unaryV1Field.setText("");
            unaryV2Field.setText("");
            invalidateResult();
            setStatus("Vértices " + v1 + " y " + v2 + " fusionados en " + etiqueta + ".", false);
        } catch (IllegalArgumentException ex) {
            setStatus(ex.getMessage(), true);
        }
    }

    private void handleContraer() {
        Grafo objetivo = "G1".equals(unaryTargetCombo.getSelectedItem()) ? g1 : g2;
        String etiqueta = (String) unaryTargetCombo.getSelectedItem();
        String v1 = unaryV1Field.getText().trim();
        String v2 = unaryV2Field.getText().trim();
        if (v1.isEmpty() || v2.isEmpty()) {
            setStatus("Indique los dos extremos de la arista a contraer.", true);
            return;
        }
        try {
            objetivo.contraerArista(v1, v2);
            actualizarCanvasYLabels(objetivo);
            unaryV1Field.setText("");
            unaryV2Field.setText("");
            invalidateResult();
            setStatus("Arista " + v1 + "—" + v2 + " contraída en " + etiqueta + ".", false);
        } catch (IllegalArgumentException ex) {
            setStatus(ex.getMessage(), true);
        }
    }

    private void aplicarBinaria(String nombre, Grafo res) {
        mostrarResultado(nombre, res);
        setStatus("Operación " + nombre + " calculada.", false);
    }

    // -------------------- HELPERS --------------------

    private void mostrarResultado(String titulo, Grafo res) {
        resultado = res;
        resultadoTituloLabel.setText("Resultado · " + titulo);
        canvasResultado.setGrafo(res);
        verticesResultadoLabel.setText(res.getVerticesStr());
        aristasResultadoLabel.setText(res.getAristasStr());
    }

    /**
     * Restablece la sección de resultado a su estado vacío. Se llama
     * después de cualquier edición sobre G1 o G2 para evitar mostrar un
     * resultado obsoleto.
     */
    private void invalidateResult() {
        if (resultado == null) {
            return;
        }
        resultado = null;
        canvasResultado.setGrafo(null);
        resultadoTituloLabel.setText("Resultado");
        verticesResultadoLabel.setText("S = {}");
        aristasResultadoLabel.setText("A = {}");
    }

    private void actualizarCanvasYLabels(Grafo grafo) {
        // refresh() preserva las posiciones que el usuario ya arrastró y
        // sólo agrega/quita según el nuevo conjunto de vértices.
        if (grafo == g1) {
            canvasG1.refresh();
            verticesG1Label.setText(g1.getVerticesStr());
            aristasG1Label.setText(g1.getAristasStr());
        } else {
            canvasG2.refresh();
            verticesG2Label.setText(g2.getVerticesStr());
            aristasG2Label.setText(g2.getAristasStr());
        }
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
                b.setBackground(hover);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                b.setBackground(base);
            }
        });
        return b;
    }
}
