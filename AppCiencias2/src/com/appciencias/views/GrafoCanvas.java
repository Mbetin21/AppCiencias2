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

    private Grafo grafo;
    private final LinkedHashMap<String, Point2D.Double> positions = new LinkedHashMap<>();
    private String draggedVertex = null;
    private final Point dragOffset = new Point();
    private boolean needsLayout = true;

    private static final int VERTEX_RADIUS = 20;
    private static final Color CANVAS_BACKGROUND = new Color(252, 252, 254);
    private static final Color VERTEX_FILL = new Color(225, 232, 245);
    private static final Color VERTEX_BORDER = new Color(120, 135, 165);
    private static final Color VERTEX_LABEL = new Color(50, 55, 70);
    private static final Color EDGE_COLOR = new Color(140, 150, 175);
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
        this.needsLayout = true;
        this.positions.clear();
        repaint();
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

        if (needsLayout) {
            circularLayout();
            needsLayout = false;
        }

        // Aristas primero (debajo de los vértices)
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(EDGE_COLOR);
        for (Grafo.Arista a : grafo.getAristas()) {
            Point2D.Double p1 = positions.get(a.v1);
            Point2D.Double p2 = positions.get(a.v2);
            if (p1 != null && p2 != null) {
                g2.drawLine((int) p1.x, (int) p1.y, (int) p2.x, (int) p2.y);
            }
        }

        // Vértices arriba
        for (Map.Entry<String, Point2D.Double> e : positions.entrySet()) {
            String v = e.getKey();
            Point2D.Double p = e.getValue();
            int d = VERTEX_RADIUS * 2;
            int x = (int) (p.x - VERTEX_RADIUS);
            int y = (int) (p.y - VERTEX_RADIUS);

            g2.setColor(VERTEX_FILL);
            g2.fillOval(x, y, d, d);
            g2.setStroke(new BasicStroke(1.5f));
            g2.setColor(VERTEX_BORDER);
            g2.drawOval(x, y, d, d);

            g2.setColor(VERTEX_LABEL);
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
