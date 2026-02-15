package com.appciencias.views;

import java.awt.*;
import javax.swing.*;

/**
 * Panel principal de inicio
 * Presenta las opciones principales de navegación
 */
public class HomePanel extends JPanel {
    
    private MainWindow mainWindow;
    
    public HomePanel(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        initializePanel();
    }
    
    /**
     * Configura el panel y sus componentes
     */
    private void initializePanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(245, 245, 250));
        setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));
        
        // Panel superior con título y descripción
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Panel central con los botones principales
        JPanel buttonsPanel = createButtonsPanel();
        add(buttonsPanel, BorderLayout.CENTER);

    }
    
    /**
     * Crea el panel de encabezado con título y descripción
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(245, 245, 250));
        
        // Título principal
        JLabel titleLabel = new JLabel("Ciencias de la Computación 2");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(new Color(70, 70, 80));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Descripción
        JLabel descriptionLabel = new JLabel("Seleccione el tipo de búsqueda que desea explorar");
        descriptionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        descriptionLabel.setForeground(new Color(100, 100, 110));
        descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(descriptionLabel);
        panel.add(Box.createVerticalStrut(20));
        
        return panel;
    }
    
    /**
     * Crea el panel con los botones principales
     */
    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 245, 250));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        
        // Botón Búsquedas Internas
        JButton btnBusquedasInternas = createMainButton(
            "Búsquedas Internas",
            "Algoritmos de búsqueda en estructuras de datos",
            "BUSQUEDAS_INTERNAS"
        );
        panel.add(btnBusquedasInternas, gbc);
        
        gbc.gridy = 1;
        
        // Botón Búsquedas Externas
        JButton btnBusquedasExternas = createMainButton(
            "Búsquedas Externas",
            "Algoritmos de búsqueda en archivos y bases de datos",
            "BUSQUEDAS_EXTERNAS"
        );
        panel.add(btnBusquedasExternas, gbc);
        
        return panel;
    }
    
    /**
     * Crea un botón principal con estilo consistente
     */
    private JButton createMainButton(String title, String description, String panelName) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout(10, 10));
        button.setBackground(new Color(230, 230, 240));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 210), 1),
            BorderFactory.createEmptyBorder(25, 30, 25, 30)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Título del botón
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(60, 60, 70));
        
        // Descripción del botón
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descLabel.setForeground(new Color(100, 100, 110));
        
        // Panel interno para organizar texto
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(8));
        textPanel.add(descLabel);
        
        button.add(textPanel, BorderLayout.CENTER);
        
        // Acción del botón
        button.addActionListener(e -> mainWindow.showPanel(panelName));
        
        return button;
    }
    
}
