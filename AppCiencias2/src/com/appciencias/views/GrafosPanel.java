package com.appciencias.views;

import java.awt.*;
import java.io.File;
import javax.swing.*;

/**
 * Panel de grafos con menú lateral desplegable.
 * Sigue el mismo patrón visual que BusquedasPanel.
 */
public class GrafosPanel extends JPanel {

    private MainWindow mainWindow;
    private JPanel sidebarPanel;
    private JPanel contentPanel;
    private boolean sidebarExpanded = true;

    // Colores consistentes con el resto de la aplicación
    private final Color BACKGROUND_COLOR = new Color(245, 245, 250);
    private final Color SIDEBAR_COLOR = new Color(240, 240, 245);
    private final Color BUTTON_COLOR = new Color(230, 230, 240);
    private final Color BUTTON_HOVER_COLOR = new Color(220, 220, 235);
    private final Color BUTTON_SELECTED_COLOR = new Color(210, 210, 230);
    private final Color BORDER_COLOR = new Color(200, 200, 210);
    private final Color TEXT_PRIMARY = new Color(70, 70, 80);
    private final Color TEXT_SECONDARY = new Color(100, 100, 110);

    private JButton currentSelectedButton = null;

    public GrafosPanel(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        initializePanel();
    }

    /**
     * Inicializa el panel con su estructura de menú lateral
     */
    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);

        sidebarPanel = createSidebarPanel();
        add(sidebarPanel, BorderLayout.WEST);

        contentPanel = createContentPanel();
        add(contentPanel, BorderLayout.CENTER);
    }

    /**
     * Crea el panel lateral con navegación
     */
    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SIDEBAR_COLOR);
        sidebar.setPreferredSize(new Dimension(280, getHeight()));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JPanel topPanel = createTopPanel();
        sidebar.add(topPanel, BorderLayout.NORTH);

        JPanel menuPanel = createMenuPanel();
        JScrollPane scrollPane = new JScrollPane(menuPanel);
        scrollPane.setBackground(SIDEBAR_COLOR);
        scrollPane.getViewport().setBackground(SIDEBAR_COLOR);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
        scrollBar.setBackground(SIDEBAR_COLOR);
        scrollBar.setUnitIncrement(16);

        sidebar.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = createBottomPanel();
        sidebar.add(bottomPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    /**
     * Crea el panel superior con el botón hamburger
     */
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(SIDEBAR_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 15, 5));

        JButton hamburgerBtn = createHamburgerButton();
        hamburgerBtn.setForeground(TEXT_PRIMARY);
        hamburgerBtn.setBackground(BUTTON_COLOR);
        hamburgerBtn.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        hamburgerBtn.setFocusPainted(false);
        hamburgerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        hamburgerBtn.setToolTipText("Contraer/Expandir menú");
        hamburgerBtn.setPreferredSize(new Dimension(38, 35));

        hamburgerBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                hamburgerBtn.setBackground(BUTTON_HOVER_COLOR);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                hamburgerBtn.setBackground(BUTTON_COLOR);
            }
        });

        hamburgerBtn.addActionListener(e -> toggleSidebar());

        JLabel titleLabel = new JLabel("Grafos");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));

        panel.add(hamburgerBtn, BorderLayout.WEST);
        panel.add(titleLabel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Crea el botón hamburger intentando cargar una imagen
     */
    private JButton createHamburgerButton() {
        JButton button = null;

        String[] extensions = {"jpg", "jpeg", "png", "gif"};
        String basePath = "C:\\Users\\david\\OneDrive\\Escritorio\\AppCiencias2\\AppCiencias2\\images\\hamburger";

        try {
            for (String ext : extensions) {
                File imageFile = new File(basePath + "." + ext);
                if (imageFile.exists()) {
                    ImageIcon icon = new ImageIcon(imageFile.getAbsolutePath());
                    Image image = icon.getImage().getScaledInstance(26, 26, Image.SCALE_SMOOTH);
                    button = new JButton(new ImageIcon(image));
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("No se pudo cargar la imagen del botón hamburger: " + e.getMessage());
        }

        if (button == null) {
            button = new JButton("☰");
            button.setFont(new Font("Segoe UI", Font.BOLD, 18));
        }

        return button;
    }

    /**
     * Crea el panel de menú con las opciones de navegación
     */
    private JPanel createMenuPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(SIDEBAR_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // Sección: Operaciones
        panel.add(createSectionLabel("Operaciones"));
        panel.add(Box.createVerticalStrut(8));

        JButton btnOperaciones = createMenuButton("Operaciones unarias y binarias", "operaciones");
        panel.add(btnOperaciones);
        panel.add(Box.createVerticalStrut(5));

        JButton btnProductos = createMenuButton("Productos", "productos");
        panel.add(btnProductos);
        panel.add(Box.createVerticalStrut(5));

        JButton btnCentro = createMenuButton("Centro y Bicentro", "centro");
        panel.add(btnCentro);
        panel.add(Box.createVerticalStrut(20));

        // Sección: Numeración
        panel.add(createSectionLabel("Numeración"));
        panel.add(Box.createVerticalStrut(8));

        JButton btnOrdinal = createMenuButton("Ordinal", "ordinal");
        panel.add(btnOrdinal);
        panel.add(Box.createVerticalStrut(20));

        // Sección: Caminos Mínimos
        panel.add(createSectionLabel("Caminos Mínimos"));
        panel.add(Box.createVerticalStrut(8));

        JButton btnDijkstra = createMenuButton("Dijkstra", "dijkstra");
        panel.add(btnDijkstra);
        panel.add(Box.createVerticalStrut(5));

        JButton btnBellman = createMenuButton("Bellman", "bellman");
        panel.add(btnBellman);
        panel.add(Box.createVerticalStrut(5));

        JButton btnFloyd = createMenuButton("Floyd", "floyd");
        panel.add(btnFloyd);
        panel.add(Box.createVerticalStrut(20));

        // Sección: Árboles Generadores
        panel.add(createSectionLabel("Árboles Generadores"));
        panel.add(Box.createVerticalStrut(8));

        JButton btnExpansion = createMenuButton("Expansión Mínima / Máxima", "expansion");
        panel.add(btnExpansion);
        panel.add(Box.createVerticalStrut(5));

        JButton btnDistanciaGrafos = createMenuButton("Distancia entre Grafos", "distancia_grafos");
        panel.add(btnDistanciaGrafos);
        panel.add(Box.createVerticalStrut(20));

        // Sección: Propiedades del Grafo
        panel.add(createSectionLabel("Propiedades del Grafo"));
        panel.add(Box.createVerticalStrut(8));

        JButton btnPropiedades = createMenuButton("Propiedades del Grafo", "propiedades");
        panel.add(btnPropiedades);

        // Selección por defecto
        selectMenuButton(btnOperaciones);

        return panel;
    }

    /**
     * Crea una etiqueta de sección para el menú
     */
    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(TEXT_SECONDARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        return label;
    }

    /**
     * Crea un botón de menú con estilo consistente
     */
    private JButton createMenuButton(String text, String action) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(TEXT_PRIMARY);
        button.setBackground(SIDEBAR_COLOR);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setPreferredSize(new Dimension(0, 50));
        button.setMinimumSize(new Dimension(0, 50));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button != currentSelectedButton) {
                    button.setBackground(BUTTON_HOVER_COLOR);
                }
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (button != currentSelectedButton) {
                    button.setBackground(SIDEBAR_COLOR);
                }
            }
        });

        button.addActionListener(e -> {
            selectMenuButton(button);
            loadContent(action);
            if (sidebarExpanded) {
                toggleSidebar();
            }
        });

        return button;
    }

    /**
     * Selecciona un botón del menú visualmente
     */
    private void selectMenuButton(JButton button) {
        if (currentSelectedButton != null) {
            currentSelectedButton.setBackground(SIDEBAR_COLOR);
            currentSelectedButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        }
        currentSelectedButton = button;
        button.setBackground(BUTTON_SELECTED_COLOR);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
    }

    /**
     * Crea el panel inferior con botón de volver
     */
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(SIDEBAR_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        JButton backButton = new JButton("← Volver al Inicio");
        backButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        backButton.setForeground(TEXT_PRIMARY);
        backButton.setBackground(BUTTON_COLOR);
        backButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        backButton.setFocusPainted(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        backButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                backButton.setBackground(BUTTON_HOVER_COLOR);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                backButton.setBackground(BUTTON_COLOR);
            }
        });

        backButton.addActionListener(e -> mainWindow.showPanel("HOME"));

        panel.add(backButton);

        return panel;
    }

    /**
     * Crea el panel de contenido principal
     */
    private JPanel createContentPanel() {
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        loadContent("operaciones");

        return contentPanel;
    }

    /**
     * Carga el contenido según la opción seleccionada
     */
    private void loadContent(String contentType) {
        contentPanel.removeAll();

        JPanel content;

        switch (contentType) {
            case "operaciones":
                content = createPlaceholderPanel(
                        "Operaciones unarias y binarias",
                        "Operaciones sobre grafos no dirigidos: unión (∪), intersección (∩), suma anillo (⊕), suma, complemento, fusión y contracción de aristas."
                );
                break;
            case "productos":
                content = createPlaceholderPanel(
                        "Productos de Grafos",
                        "Producto cartesiano (×), producto tensorial (⊗) y composición G1[G2] entre dos grafos no dirigidos."
                );
                break;
            case "centro":
                content = createPlaceholderPanel(
                        "Centro y Bicentro",
                        "Identificación del centro o bicentro de un grafo no dirigido eliminando hojas iterativamente."
                );
                break;
            case "ordinal":
                content = createPlaceholderPanel(
                        "Numeración Ordinal",
                        "Numeración de los vértices del grafo siguiendo un orden topológico válido."
                );
                break;
            case "dijkstra":
                content = createPlaceholderPanel(
                        "Algoritmo de Dijkstra",
                        "Camino mínimo desde un nodo origen hacia los demás vértices con etiquetas {d, N} y estados temporal/permanente."
                );
                break;
            case "bellman":
                content = createPlaceholderPanel(
                        "Algoritmo de Bellman",
                        "Camino mínimo entre origen y destino siguiendo la numeración ordinal del grafo dirigido con pesos."
                );
                break;
            case "floyd":
                content = createPlaceholderPanel(
                        "Algoritmo de Floyd",
                        "Caminos mínimos entre todos los pares de vértices con matrices intermedias por iteración."
                );
                break;
            case "expansion":
                content = createPlaceholderPanel(
                        "Expansión Mínima / Máxima",
                        "Árbol de expansión mínima y máxima por Kruskal, mostrando ramas y cuerdas resultantes."
                );
                break;
            case "distancia_grafos":
                content = createPlaceholderPanel(
                        "Distancia entre Grafos",
                        "Distancia entre dos grafos calculada a partir de las expansiones mínimas T1 y T2 de cada uno."
                );
                break;
            case "propiedades":
                content = createPlaceholderPanel(
                        "Propiedades del Grafo",
                        "Excentricidad, diámetro, radio, mediana y cintura de un grafo no dirigido con pesos."
                );
                break;
            default:
                content = createDefaultContent();
        }

        contentPanel.add(content, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /**
     * Plantilla de contenido en desarrollo, idéntica en estilo a la del módulo de búsquedas
     */
    private JPanel createPlaceholderPanel(String title, String description) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND_COLOR);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(15));

        JTextArea descArea = new JTextArea(description);
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        descArea.setForeground(TEXT_SECONDARY);
        descArea.setBackground(BACKGROUND_COLOR);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(descArea);
        panel.add(Box.createVerticalStrut(25));

        JPanel placeholderBox = new JPanel();
        placeholderBox.setLayout(new BoxLayout(placeholderBox, BoxLayout.Y_AXIS));
        placeholderBox.setBackground(new Color(235, 235, 245));
        placeholderBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        placeholderBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel statusLabel = new JLabel("En desarrollo");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statusLabel.setForeground(TEXT_PRIMARY);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        placeholderBox.add(statusLabel);
        placeholderBox.add(Box.createVerticalStrut(10));

        JLabel placeholderLabel = new JLabel("Esta pantalla se construirá próximamente.");
        placeholderLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        placeholderLabel.setForeground(TEXT_SECONDARY);
        placeholderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        placeholderBox.add(placeholderLabel);

        panel.add(placeholderBox);

        return panel;
    }

    /**
     * Contenido por defecto cuando no hay opción seleccionada
     */
    private JPanel createDefaultContent() {
        return createPlaceholderPanel(
                "Bienvenido",
                "Seleccione una opción del menú lateral para explorar los algoritmos de grafos."
        );
    }

    /**
     * Alterna la visibilidad del sidebar
     */
    private void toggleSidebar() {
        sidebarExpanded = !sidebarExpanded;

        if (sidebarExpanded) {
            sidebarPanel.setPreferredSize(new Dimension(280, getHeight()));
        } else {
            sidebarPanel.setPreferredSize(new Dimension(70, getHeight()));
        }

        Component[] components = sidebarPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JScrollPane) {
                comp.setVisible(sidebarExpanded);
            } else if (comp instanceof JPanel) {
                JPanel p = (JPanel) comp;
                if (p.getLayout() instanceof BorderLayout) {
                    Component center = ((BorderLayout) p.getLayout()).getLayoutComponent(BorderLayout.CENTER);
                    Component south = ((BorderLayout) p.getLayout()).getLayoutComponent(BorderLayout.SOUTH);
                    if (center != null) center.setVisible(sidebarExpanded);
                    if (south != null) south.setVisible(sidebarExpanded);
                } else {
                    p.setVisible(sidebarExpanded);
                }
            }
        }

        sidebarPanel.revalidate();
        sidebarPanel.repaint();
    }
}
