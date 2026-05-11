package com.appciencias.views;

import com.appciencias.algorithms.Grafo;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * Componente reutilizable para visualizar y editar (por arrastre) un Grafo no
 * dirigido sin pesos. Aplica una disposición circular automática y permite al
 * usuario arrastrar vértices con el mouse para reorganizarlos.
 */
public class GrafoCanvas extends JPanel {

    /**
     * Estado visual de un vértice. Usado por algoritmos (centro/bicentro,
     * Dijkstra, etc.) para resaltar vértices según su rol en la ejecución.
     */
    public enum VertexState {
        /** Vértice activo / por defecto */
        NORMAL,
        /** Vértice atenuado (ya no participa, en gris) */
        FADED,
        /** Vértice resaltado como "eliminado en este paso" (naranja) */
        LEAF,
        /** Vértice resaltado como centro / bicentro / resultado (verde) */
        CENTER
    }

    private Grafo grafo;
    private final LinkedHashMap<String, Point2D.Double> positions = new LinkedHashMap<>();
    private String draggedVertex = null;
    private final Point dragOffset = new Point();
    private boolean needsLayout = true;

    // Disposición opcional en cuadrícula (usada por productos, donde
    // los vértices son pares (u, v) con estructura natural fila/columna).
    private boolean useGrid = false;
    private LinkedHashMap<String, int[]> gridSpec = null;
    private int gridRows = 0;
    private int gridCols = 0;

    // Estados visuales por vértice (null = todos NORMAL)
    private Map<String, VertexState> vertexStates = null;

    // Posiciones pendientes de aplicarse cuando el canvas tenga tamaño
    private LinkedHashMap<String, double[]> pendingNormalized = null;

    private static final int VERTEX_RADIUS = 20;
    private static final Color CANVAS_BACKGROUND = new Color(252, 252, 254);
    private static final Color VERTEX_FILL = new Color(225, 232, 245);
    private static final Color VERTEX_BORDER = new Color(120, 135, 165);
    private static final Color VERTEX_LABEL = new Color(50, 55, 70);
    private static final Color VERTEX_LEAF_FILL = new Color(250, 222, 195);
    private static final Color VERTEX_LEAF_BORDER = new Color(200, 130, 70);
    private static final Color VERTEX_LEAF_LABEL = new Color(110, 60, 30);
    private static final Color VERTEX_FADED_FILL = new Color(235, 235, 238);
    private static final Color VERTEX_FADED_BORDER = new Color(205, 205, 210);
    private static final Color VERTEX_FADED_LABEL = new Color(170, 170, 180);
    private static final Color VERTEX_CENTER_FILL = new Color(205, 235, 210);
    private static final Color VERTEX_CENTER_BORDER = new Color(90, 150, 100);
    private static final Color VERTEX_CENTER_LABEL = new Color(40, 90, 50);
    private static final Color EDGE_COLOR = new Color(140, 150, 175);
    private static final Color EDGE_LEAF_COLOR = new Color(210, 150, 100);
    private static final Color EDGE_FADED_COLOR = new Color(218, 218, 222);
    private static final Color CANVAS_BORDER = new Color(200, 200, 210);
    private static final Color EMPTY_TEXT = new Color(180, 180, 195);
    private static final Font VERTEX_FONT = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font EMPTY_FONT = new Font("Segoe UI", Font.ITALIC, 13);

    public GrafoCanvas() {
        setBackground(CANVAS_BACKGROUND);
        setBorder(BorderFactory.createLineBorder(CANVAS_BORDER, 1));
        setPreferredSize(new Dimension(300, 220));

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                draggedVertex = findVertexAt(e.getX(), e.getY());
                if (draggedVertex != null) {
                    Point2D.Double p = positions.get(draggedVertex);
                    dragOffset.setLocation((int) (e.getX() - p.x), (int) (e.getY() - p.y));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                draggedVertex = null;
                setCursor(Cursor.getDefaultCursor());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggedVertex != null) {
                    double nx = e.getX() - dragOffset.x;
                    double ny = e.getY() - dragOffset.y;
                    nx = Math.max(VERTEX_RADIUS, Math.min(getWidth() - VERTEX_RADIUS, nx));
                    ny = Math.max(VERTEX_RADIUS, Math.min(getHeight() - VERTEX_RADIUS, ny));
                    positions.put(draggedVertex, new Point2D.Double(nx, ny));
                    repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                String v = findVertexAt(e.getX(), e.getY());
                setCursor(v != null
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor());
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    /**
     * Asigna un nuevo grafo y fuerza un relayout circular completo.
     */
    public void setGrafo(Grafo g) {
        this.grafo = g;
        this.useGrid = false;
        this.gridSpec = null;
        this.needsLayout = true;
        this.positions.clear();
        this.pendingNormalized = null;
        repaint();
    }

    /**
     * Asigna un grafo cuyos vértices se colocan en una cuadrícula de filas ×
     * columnas. El mapa {@code spec} asocia cada vértice con su par
     * {@code [fila, columna]}. Útil para grafos producto donde el orden
     * fila/columna refleja la estructura del producto y reduce cruces de
     * aristas frente a la disposición circular. El usuario puede arrastrar
     * los vértices después igual que en modo circular.
     */
    public void setGrafoEnCuadricula(Grafo g, LinkedHashMap<String, int[]> spec,
                                     int rows, int cols) {
        this.grafo = g;
        this.useGrid = true;
        this.gridSpec = spec;
        this.gridRows = rows;
        this.gridCols = cols;
        this.needsLayout = true;
        this.positions.clear();
        this.pendingNormalized = null;
        repaint();
    }

    /**
     * Devuelve las posiciones actuales como ratios [x/width, y/height] en el
     * rango [0, 1]. Útil para transferir el layout entre canvas de distinto
     * tamaño (por ejemplo, copiar la organización del editor al canvas de
     * resultado). Si el canvas aún no se pintó, retorna un mapa vacío.
     */
    public LinkedHashMap<String, double[]> getNormalizedPositions() {
        LinkedHashMap<String, double[]> result = new LinkedHashMap<>();
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) {
            return result;
        }
        for (Map.Entry<String, Point2D.Double> e : positions.entrySet()) {
            result.put(e.getKey(),
                    new double[]{e.getValue().x / w, e.getValue().y / h});
        }
        return result;
    }

    /**
     * Aplica posiciones normalizadas (ratios en [0, 1]) al canvas. Las
     * posiciones se escalan al tamaño actual del canvas. Si el canvas aún no
     * tiene tamaño conocido, se aplican en el próximo {@code paintComponent}.
     * Llamar después de {@link #setGrafo(Grafo)} para sobrescribir la
     * disposición circular por defecto con un layout heredado.
     */
    public void setNormalizedPositions(LinkedHashMap<String, double[]> norm) {
        if (norm == null || norm.isEmpty()) {
            this.pendingNormalized = null;
            return;
        }
        this.pendingNormalized = new LinkedHashMap<>(norm);
        this.useGrid = false;
        if (getWidth() > 0 && getHeight() > 0) {
            applyPendingNormalized();
        }
        repaint();
    }

    private void applyPendingNormalized() {
        if (pendingNormalized == null) {
            return;
        }
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        positions.clear();
        for (Map.Entry<String, double[]> e : pendingNormalized.entrySet()) {
            double[] xy = e.getValue();
            positions.put(e.getKey(), new Point2D.Double(xy[0] * w, xy[1] * h));
        }
        needsLayout = false;
        pendingNormalized = null;
    }

    /**
     * Define el estado visual de cada vértice. Los vértices ausentes del mapa
     * se dibujan como NORMAL. Pasar {@code null} (o llamar a
     * {@link #clearVertexStates()}) restaura el render por defecto.
     */
    public void setVertexStates(Map<String, VertexState> states) {
        this.vertexStates = (states == null) ? null : new LinkedHashMap<>(states);
        repaint();
    }

    public void clearVertexStates() {
        this.vertexStates = null;
        repaint();
    }

    private VertexState stateOf(String v) {
        if (vertexStates == null) {
            return VertexState.NORMAL;
        }
        VertexState s = vertexStates.get(v);
        return (s == null) ? VertexState.NORMAL : s;
    }

    /**
     * Llamar cuando el contenido del grafo cambia (se agregó/quitó vértice o
     * arista) sin reemplazar la instancia. Conserva las posiciones de los
     * vértices que el usuario ya arrastró y agrega posiciones para los nuevos.
     */
    public void refresh() {
        if (grafo == null) {
            return;
        }
        List<String> currentVertices = grafo.getVertices();
        positions.keySet().retainAll(currentVertices);

        if (positions.isEmpty() && !currentVertices.isEmpty()) {
            needsLayout = true;
            repaint();
            return;
        }

        List<String> nuevos = new ArrayList<>();
        for (String v : currentVertices) {
            if (!positions.containsKey(v)) {
                nuevos.add(v);
            }
        }

        if (!nuevos.isEmpty() && getWidth() > 0 && getHeight() > 0) {
            int w = getWidth();
            int h = getHeight();
            double cx = w / 2.0;
            double cy = h / 2.0;
            double r = Math.min(w, h) * 0.36;
            int total = currentVertices.size();
            for (String v : nuevos) {
                int idx = currentVertices.indexOf(v);
                double angle = 2 * Math.PI * idx / total - Math.PI / 2;
                positions.put(v, new Point2D.Double(cx + r * Math.cos(angle),
                        cy + r * Math.sin(angle)));
            }
        }
        repaint();
    }

    private void gridLayout() {
        if (gridSpec == null || gridRows <= 0 || gridCols <= 0) {
            return;
        }
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        int padX = Math.max(45, VERTEX_RADIUS * 2 + 8);
        int padY = Math.max(35, VERTEX_RADIUS * 2 + 8);

        double cellW = (gridCols <= 1) ? 0 : (double) (w - 2 * padX) / (gridCols - 1);
        double cellH = (gridRows <= 1) ? 0 : (double) (h - 2 * padY) / (gridRows - 1);

        double startX = (gridCols <= 1) ? w / 2.0 : padX;
        double startY = (gridRows <= 1) ? h / 2.0 : padY;

        positions.clear();
        for (Map.Entry<String, int[]> e : gridSpec.entrySet()) {
            int row = e.getValue()[0];
            int col = e.getValue()[1];
            double x = startX + cellW * col;
            double y = startY + cellH * row;
            positions.put(e.getKey(), new Point2D.Double(x, y));
        }
    }

    private void circularLayout() {
        if (grafo == null) {
            return;
        }
        int n = grafo.getNumVertices();
        if (n == 0) {
            return;
        }
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        double cx = w / 2.0;
        double cy = h / 2.0;
        double r = Math.min(w, h) * 0.36;
        if (n == 1) {
            r = 0;
        }

        positions.clear();
        int i = 0;
        for (String v : grafo.getVertices()) {
            double angle = (n == 1) ? 0 : 2 * Math.PI * i / n - Math.PI / 2;
            positions.put(v, new Point2D.Double(cx + r * Math.cos(angle),
                    cy + r * Math.sin(angle)));
            i++;
        }
    }

    private String findVertexAt(int x, int y) {
        List<String> keys = new ArrayList<>(positions.keySet());
        for (int i = keys.size() - 1; i >= 0; i--) {
            Point2D.Double p = positions.get(keys.get(i));
            double dx = x - p.x;
            double dy = y - p.y;
            if (dx * dx + dy * dy <= VERTEX_RADIUS * VERTEX_RADIUS) {
                return keys.get(i);
            }
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (grafo == null || grafo.getNumVertices() == 0) {
            g2.setColor(EMPTY_TEXT);
            g2.setFont(EMPTY_FONT);
            String msg = (grafo == null) ? "Sin grafo"
                    : "Agregue vértices para comenzar";
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(msg);
            g2.drawString(msg, (getWidth() - tw) / 2, getHeight() / 2);
            return;
        }

        if (pendingNormalized != null) {
            applyPendingNormalized();
        }

        if (needsLayout) {
            if (useGrid) {
                gridLayout();
            } else {
                circularLayout();
            }
            needsLayout = false;
        }

        // Aristas primero (debajo de los vértices)
        g2.setStroke(new BasicStroke(2f));
        for (Grafo.Arista a : grafo.getAristas()) {
            Point2D.Double p1 = positions.get(a.v1);
            Point2D.Double p2 = positions.get(a.v2);
            if (p1 == null || p2 == null) {
                continue;
            }
            VertexState s1 = stateOf(a.v1);
            VertexState s2 = stateOf(a.v2);
            if (s1 == VertexState.FADED || s2 == VertexState.FADED) {
                g2.setColor(EDGE_FADED_COLOR);
            } else if (s1 == VertexState.LEAF || s2 == VertexState.LEAF) {
                g2.setColor(EDGE_LEAF_COLOR);
            } else {
                g2.setColor(EDGE_COLOR);
            }
            g2.drawLine((int) p1.x, (int) p1.y, (int) p2.x, (int) p2.y);
        }

        // Vértices arriba
        for (Map.Entry<String, Point2D.Double> e : positions.entrySet()) {
            String v = e.getKey();
            Point2D.Double p = e.getValue();
            int d = VERTEX_RADIUS * 2;
            int x = (int) (p.x - VERTEX_RADIUS);
            int y = (int) (p.y - VERTEX_RADIUS);

            VertexState state = stateOf(v);
            Color fill, border, labelColor;
            switch (state) {
                case LEAF:
                    fill = VERTEX_LEAF_FILL;
                    border = VERTEX_LEAF_BORDER;
                    labelColor = VERTEX_LEAF_LABEL;
                    break;
                case FADED:
                    fill = VERTEX_FADED_FILL;
                    border = VERTEX_FADED_BORDER;
                    labelColor = VERTEX_FADED_LABEL;
                    break;
                case CENTER:
                    fill = VERTEX_CENTER_FILL;
                    border = VERTEX_CENTER_BORDER;
                    labelColor = VERTEX_CENTER_LABEL;
                    break;
                case NORMAL:
                default:
                    fill = VERTEX_FILL;
                    border = VERTEX_BORDER;
                    labelColor = VERTEX_LABEL;
            }

            g2.setColor(fill);
            g2.fillOval(x, y, d, d);
            g2.setStroke(new BasicStroke(1.5f));
            g2.setColor(border);
            g2.drawOval(x, y, d, d);

            g2.setColor(labelColor);
            g2.setFont(VERTEX_FONT);
            FontMetrics fm = g2.getFontMetrics();
            String label = v.length() > 6 ? v.substring(0, 5) + "…" : v;
            int tw = fm.stringWidth(label);
            g2.drawString(label,
                    (float) (p.x - tw / 2.0),
                    (float) (p.y + fm.getAscent() / 2.0 - 2));
        }
    }
}
