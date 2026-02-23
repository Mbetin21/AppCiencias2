package com.appciencias.views;

import java.awt.*;
import java.io.File;
import javax.swing.*;

/**
 * Panel de búsquedas con menú lateral desplegable
 * Permite navegar entre búsquedas internas y externas
 */
public class BusquedasPanel extends JPanel {
    
    private MainWindow mainWindow;
    private JPanel sidebarPanel;
    private JPanel contentPanel;
    private boolean sidebarExpanded = true;
    private SecuencialPanel secuencialPanel;
    
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
    
    public BusquedasPanel(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        initializePanel();
    }
    
    /**
     * Inicializa el panel con su estructura de menú lateral
     */
    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        
        // Panel lateral (sidebar)
        sidebarPanel = createSidebarPanel();
        add(sidebarPanel, BorderLayout.WEST);
        
        // Panel de contenido principal
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
        
        // Panel superior con botón hamburger
        JPanel topPanel = createTopPanel();
        sidebar.add(topPanel, BorderLayout.NORTH);
        
        // Panel central con opciones de navegación
        JPanel menuPanel = createMenuPanel();
        sidebar.add(menuPanel, BorderLayout.CENTER);
        
        // Panel inferior con botón de volver
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
        
        // Botón hamburger
        JButton hamburgerBtn = createHamburgerButton();
        hamburgerBtn.setForeground(TEXT_PRIMARY);
        hamburgerBtn.setBackground(BUTTON_COLOR);
        hamburgerBtn.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        hamburgerBtn.setFocusPainted(false);
        hamburgerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        hamburgerBtn.setToolTipText("Contraer/Expandir menú");
        hamburgerBtn.setPreferredSize(new Dimension(38, 35));
        
        // Efecto hover
        hamburgerBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                hamburgerBtn.setBackground(BUTTON_HOVER_COLOR);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                hamburgerBtn.setBackground(BUTTON_COLOR);
            }
        });
        
        hamburgerBtn.addActionListener(e -> toggleSidebar());
        
        JLabel titleLabel = new JLabel("Búsquedas");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        
        panel.add(hamburgerBtn, BorderLayout.WEST);
        panel.add(titleLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Crea el botón hamburger intentando cargar una imagen
     * Si no encuentra la imagen, usa el símbolo de texto
     */
    private JButton createHamburgerButton() {
        JButton button = null;
        
        // Intentar cargar la imagen (buscar múltiples formatos)
        String[] extensions = {"jpg", "jpeg", "png", "gif"};
        String basePath = "C:\\Users\\david\\OneDrive\\Escritorio\\AppCiencias2\\AppCiencias2\\images\\hamburger";
        
        try {
            for (String ext : extensions) {
                File imageFile = new File(basePath + "." + ext);
                if (imageFile.exists()) {
                    ImageIcon icon = new ImageIcon(imageFile.getAbsolutePath());
                    // Escalar la imagen con mejor calidad (26x26 para mayor nitidez)
                    Image image = icon.getImage().getScaledInstance(26, 26, Image.SCALE_SMOOTH);
                    button = new JButton(new ImageIcon(image));
                    break;
                }
            }
        } catch (Exception e) {
            // Si hay error al cargar la imagen, usar texto
            System.out.println("No se pudo cargar la imagen del botón hamburger: " + e.getMessage());
        }
        
        // Si no se cargó la imagen, usar texto
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
        
        // Sección: Búsquedas Internas
        JLabel internasLabel = createSectionLabel("Búsquedas Internas");
        panel.add(internasLabel);
        panel.add(Box.createVerticalStrut(8));
        
        JButton btnSecuencial = createMenuButton("Búsqueda Secuencial", "secuencial");
        panel.add(btnSecuencial);
        panel.add(Box.createVerticalStrut(5));
        
        JButton btnBinaria = createMenuButton("Búsqueda Binaria", "binaria");
        panel.add(btnBinaria);
        panel.add(Box.createVerticalStrut(5));
        
        JButton btnHash = createMenuButton("Búsqueda Hash", "hash");
        panel.add(btnHash);
        panel.add(Box.createVerticalStrut(20));
        
        // Sección: Búsquedas Externas
        JLabel externasLabel = createSectionLabel("Búsquedas Externas");
        panel.add(externasLabel);
        panel.add(Box.createVerticalStrut(8));
        
        JLabel placeholderLabel = new JLabel("(En desarrollo...)");
        placeholderLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        placeholderLabel.setForeground(TEXT_SECONDARY);
        placeholderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        placeholderLabel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 0));
        panel.add(placeholderLabel);
        
        // Seleccionar el primer botón por defecto
        selectMenuButton(btnSecuencial);
        
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
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        
        // Efecto hover
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
        
        // Acción del botón
        button.addActionListener(e -> {
            selectMenuButton(button);
            loadContent(action);
            // Contraer el menú automáticamente después de seleccionar una búsqueda
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
        // Deseleccionar el botón anterior
        if (currentSelectedButton != null) {
            currentSelectedButton.setBackground(SIDEBAR_COLOR);
            currentSelectedButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        }
        
        // Seleccionar el nuevo botón
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
        
        // Efecto hover
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

        secuencialPanel = new SecuencialPanel();
        
        // Cargar contenido por defecto
        loadContent("secuencial");
        
        return contentPanel;
    }
    
    /**
     * Carga el contenido según la opción seleccionada
     */
    private void loadContent(String contentType) {
        contentPanel.removeAll();
        
        JPanel content = null;
        
        switch (contentType) {
            case "secuencial":
                content = secuencialPanel;
                break;
            case "binaria":
                content = createBinariaContent();
                break;
            case "hash":
                content = createHashContent();
                break;
            default:
                content = createDefaultContent();
        }
        
        contentPanel.add(content, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    /**
     * Crea contenido para Búsqueda Secuencial
     */
    private JPanel createSecuencialContent() {
        return createContentTemplate(
            "Búsqueda Secuencial",
            "Algoritmo que busca un elemento recorriendo la estructura de datos elemento por elemento hasta encontrarlo o llegar al final.",
            new String[]{"Complejidad: O(n)", "Simple de implementar", "No requiere ordenamiento"}
        );
    }
    
    /**
     * Crea contenido para Búsqueda Binaria
     */
    private JPanel createBinariaContent() {
        return createContentTemplate(
            "Búsqueda Binaria",
            "Algoritmo eficiente para buscar en estructuras ordenadas, dividiendo el espacio de búsqueda a la mitad en cada iteración.",
            new String[]{"Complejidad: O(log n)", "Requiere datos ordenados", "Muy eficiente para grandes conjuntos"}
        );
    }
    
    /**
     * Crea contenido para Búsqueda Hash
     */
    private JPanel createHashContent() {
        return createContentTemplate(
            "Búsqueda Hash",
            "Algoritmo que utiliza funciones hash para mapear claves a posiciones en una tabla, permitiendo búsquedas en tiempo constante promedio.",
            new String[]{"Complejidad: O(1) promedio", "Usa funciones de dispersión", "Manejo de colisiones necesario", "Muy rápida para acceso directo"}
        );
    }
    
    /**
     * Crea contenido por defecto
     */
    private JPanel createDefaultContent() {
        return createContentTemplate(
            "Bienvenido",
            "Seleccione un algoritmo del menú lateral para ver su información y pruebas interactivas.",
            new String[]{}
        );
    }
    
    /**
     * Plantilla para crear contenido de forma consistente
     */
    private JPanel createContentTemplate(String title, String description, String[] points) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND_COLOR);
        
        // Título
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(15));
        
        // Descripción
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
        
        // Características
        if (points.length > 0) {
            JLabel featuresLabel = new JLabel("Características:");
            featuresLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            featuresLabel.setForeground(TEXT_PRIMARY);
            featuresLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(featuresLabel);
            panel.add(Box.createVerticalStrut(10));
            
            for (String point : points) {
                JLabel pointLabel = new JLabel("• " + point);
                pointLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                pointLabel.setForeground(TEXT_SECONDARY);
                pointLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                panel.add(pointLabel);
                panel.add(Box.createVerticalStrut(5));
            }
            
            panel.add(Box.createVerticalStrut(30));
        }
        
        // Panel para prueba interactiva (placeholder)
        JPanel interactivePanel = new JPanel();
        interactivePanel.setLayout(new BoxLayout(interactivePanel, BoxLayout.Y_AXIS));
        interactivePanel.setBackground(new Color(235, 235, 245));
        interactivePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        interactivePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel interactiveLabel = new JLabel("Prueba Interactiva");
        interactiveLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        interactiveLabel.setForeground(TEXT_PRIMARY);
        interactiveLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        interactivePanel.add(interactiveLabel);
        interactivePanel.add(Box.createVerticalStrut(10));
        
        JLabel placeholderLabel = new JLabel("(Implementación en desarrollo)");
        placeholderLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        placeholderLabel.setForeground(TEXT_SECONDARY);
        placeholderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        interactivePanel.add(placeholderLabel);
        
        panel.add(interactivePanel);
        
        return panel;
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
        
        // Ocultar/mostrar componentes del sidebar excepto el botón hamburger
        Component[] components = sidebarPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                if (panel.getLayout() instanceof BorderLayout) {
                    Component center = ((BorderLayout) panel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
                    Component south = ((BorderLayout) panel.getLayout()).getLayoutComponent(BorderLayout.SOUTH);
                    if (center != null) center.setVisible(sidebarExpanded);
                    if (south != null) south.setVisible(sidebarExpanded);
                } else {
                    panel.setVisible(sidebarExpanded);
                }
            }
        }
        
        sidebarPanel.revalidate();
        sidebarPanel.repaint();
    }
}