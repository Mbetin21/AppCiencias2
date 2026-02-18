package com.appciencias.views;

import java.awt.*;
import javax.swing.*;

/**
 * Ventana principal de la aplicación
 * Gestiona la navegación entre diferentes paneles sin abrir nuevas ventanas
 */
public class MainWindow extends JFrame {
    
    private CardLayout cardLayout;
    private JPanel panelContainer;
    
    // Referencias a los paneles
    private HomePanel homePanel;
    private BusquedasPanel busquedasPanel;
    
    public MainWindow() {
        initializeWindow();
        initializePanels();
        initializeComponents();
    }
    
    /**
     * Configura las propiedades básicas de la ventana
     */
    private void initializeWindow() {
        setTitle("Ciencias de la Computación 2");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null); // Centra la ventana
        setMinimumSize(new Dimension(800, 500));
    }
    
    /**
     * Inicializa el sistema de paneles con CardLayout
     */
    private void initializePanels() {
        cardLayout = new CardLayout();
        panelContainer = new JPanel(cardLayout);
        
        // Panel principal (home)
        homePanel = new HomePanel(this);
        panelContainer.add(homePanel, "HOME");
        
        // Panel de búsquedas
        busquedasPanel = new BusquedasPanel(this);
        panelContainer.add(busquedasPanel, "BUSQUEDAS_INTERNAS");
    }
    
    /**
     * Agrega los componentes a la ventana
     */
    private void initializeComponents() {
        add(panelContainer);
    }
    
    /**
     * Cambia el panel visible
     * @param panelName nombre del panel a mostrar
     */
    public void showPanel(String panelName) {
        cardLayout.show(panelContainer, panelName);
    }
    
    /**
     * Crea un panel temporal para futura implementación
     */
    private JPanel createPlaceholderPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(240, 240, 245));
        
        JLabel label = new JLabel("Panel: " + title + " (En desarrollo)", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        label.setForeground(new Color(80, 80, 90));
        panel.add(label, BorderLayout.CENTER);
        
        JButton backButton = new JButton("Volver al Inicio");
        backButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        backButton.setBackground(new Color(200, 200, 210));
        backButton.setForeground(new Color(60, 60, 70));
        backButton.setFocusPainted(false);
        backButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        backButton.addActionListener(e -> showPanel("HOME"));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20));
        buttonPanel.setBackground(new Color(240, 240, 245));
        buttonPanel.add(backButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Muestra la ventana principal
     */
    public void display() {
        setVisible(true);
    }
    
    /**
     * Punto de entrada de la aplicación
     */
    public static void main(String[] args) {
        // Usar el look and feel del sistema para mejor integración
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Ejecutar en el hilo de eventos de Swing
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.display();
        });
    }
}
