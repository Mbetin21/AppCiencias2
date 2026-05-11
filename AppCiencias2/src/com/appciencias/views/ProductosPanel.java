package com.appciencias.views;

import com.appciencias.algorithms.Grafo;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import javax.swing.*;

/**
 * Panel interactivo de productos sobre grafos no dirigidos sin pesos.
 *
 * Permite al usuario construir dos grafos (G1 y G2) y aplicar sobre ellos:
 *   Producto Cartesiano  → G1 × G2
 *   Producto Tensorial   → G1 ⊗ G2
 *   Composición G1[G2]
 *   Composición G2[G1]
 *
 * Como los vértices del resultado son pares (u, v), el grafo resultante se
 * visualiza en una cuadrícula (fila = índice del primer factor, columna =
 * índice del segundo). Esto refleja la matemática del producto y reduce
 * cruces de aristas frente a la disposición circular. El usuario puede
 * arrastrar los vértices después.
 *
 * Todas las operaciones se delegan a {@link com.appciencias.algorithms.Grafo}.
 */
public class ProductosPanel extends JPanel {

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

    public ProductosPanel() {
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
        content.add(createOperationsSection());
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

        JLabel title = new JLabel("Productos");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(8));

        JTextArea desc = new JTextArea(
                "Construya dos grafos no dirigidos y calcule entre ellos los "
                + "productos cartesiano y tensorial, así como la composición "
                + "en ambas direcciones (G1[G2] y G2[G1]). Los vértices del "
                + "resultado son pares y se distribuyen en una cuadrícula.");
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
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));

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

        // Fila vértice
        JPanel vRow = createInputRow();
        vRow.add(createInlineLabel("Vértices:"));
        JTextField vField = new JTextField(10);
        vField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        vRow.add(vField);
        JButton addVBtn = createSmallButton("Agregar", false);
        vRow.add(addVBtn);
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
        JTextField eFrom = new JTextField(4);
        eFrom.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        eRow.add(eFrom);
        eRow.add(createInlineLabel("—"));
        JTextField eTo = new JTextField(4);
        eTo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        eRow.add(eTo);
        JButton addEBtn = createSmallButton("Agregar", false);
        eRow.add(addEBtn);
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

        // Botón por-grafo (solo Limpiar — los productos van en la sección común)
        JPanel btnRow = createInputRow();
        JButton clearBtn = createSmallButton("Limpiar", false);
        btnRow.add(clearBtn);
        panel.add(btnRow);

        // Acciones
        addVBtn.addActionListener(e -> handleAddVertex(grafo, vField, canvas,
                vCount, aCount, etiqueta));
        vField.addActionListener(e -> addVBtn.doClick());

        addEBtn.addActionListener(e -> handleAddEdge(grafo, eFrom, eTo, canvas,
                vCount, aCount, etiqueta));
        eTo.addActionListener(e -> addEBtn.doClick());

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

    // -------------------- OPERACIONES --------------------

    private JPanel createOperationsSection() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JLabel title = new JLabel("Operaciones (resultado en la sección inferior)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(6));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        row.setBackground(PANEL_COLOR);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton cartesianoBtn = createSmallButton("Producto Cartesiano", true);
        JButton tensorialBtn = createSmallButton("Producto Tensorial", true);
        JButton compG1G2Btn = createSmallButton("Composición G1[G2]", true);
        JButton compG2G1Btn = createSmallButton("Composición G2[G1]", true);
        row.add(cartesianoBtn);
        row.add(tensorialBtn);
        row.add(compG1G2Btn);
        row.add(compG2G1Btn);
        panel.add(row);

        cartesianoBtn.addActionListener(e -> aplicarProducto(
                "Producto Cartesiano", g1, g2, Grafo.productoCartesiano(g1, g2)));
        tensorialBtn.addActionListener(e -> aplicarProducto(
                "Producto Tensorial", g1, g2, Grafo.productoTensorial(g1, g2)));
        compG1G2Btn.addActionListener(e -> aplicarProducto(
                "Composición G1[G2]", g1, g2, Grafo.composicion(g1, g2)));
        compG2G1Btn.addActionListener(e -> aplicarProducto(
                "Composición G2[G1]", g2, g1, Grafo.composicion(g2, g1)));

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
        canvasResultado.setPreferredSize(new Dimension(600, 300));
        canvasResultado.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
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
            setStatus("Vértices agregados a " + etiqueta + ".", false);
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
            setStatus("Arista " + v1 + "—" + v2 + " agregada a " + etiqueta + ".", false);
        } catch (IllegalArgumentException ex) {
            setStatus(ex.getMessage(), true);
        }
    }

    private void handleLimpiar(Grafo grafo, GrafoCanvas canvas, JLabel vLabel,
                               JLabel aLabel, String etiqueta) {
        grafo.limpiar();
        canvas.setGrafo(grafo);
        vLabel.setText(grafo.getVerticesStr());
        aLabel.setText(grafo.getAristasStr());
        setStatus(etiqueta + " vaciado.", false);
    }

    /**
     * Aplica un producto / composición. {@code outer} es el grafo que aporta
     * el primer factor de los pares (filas de la cuadrícula); {@code inner}
     * aporta el segundo (columnas).
     */
    private void aplicarProducto(String nombre, Grafo outer, Grafo inner, Grafo res) {
        if (outer.getNumVertices() == 0 || inner.getNumVertices() == 0) {
            setStatus("Ambos grafos deben tener al menos un vértice para "
                    + "calcular " + nombre + ".", true);
            return;
        }
        resultado = res;
        resultadoTituloLabel.setText("Resultado · " + nombre);

        // Cuadrícula: filas = vértices de outer, columnas = vértices de inner
        List<String> filas = outer.getVertices();
        List<String> columnas = inner.getVertices();
        LinkedHashMap<String, int[]> spec = new LinkedHashMap<>();
        for (int r = 0; r < filas.size(); r++) {
            for (int c = 0; c < columnas.size(); c++) {
                spec.put(filas.get(r) + columnas.get(c), new int[]{r, c});
            }
        }
        canvasResultado.setGrafoEnCuadricula(res, spec, filas.size(), columnas.size());
        verticesResultadoLabel.setText(res.getVerticesStr());
        aristasResultadoLabel.setText(res.getAristasStr());
        setStatus(nombre + " calculado: " + res.getNumVertices()
                + " vértices, " + res.getNumAristas() + " aristas.", false);
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
