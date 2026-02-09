import java.awt.*;
import javax.swing.*;

public class ResultadosPanel extends JPanel {
    private MainApp mainApp;
    private JLabel lblTitulo;
    private JTextArea txtResultados;
    private JButton btnLimpiar;
    private JScrollPane scrollResultados;
    
    public ResultadosPanel(MainApp mainApp) {
        this.mainApp = mainApp;
        UIUtils.configurarPanel(this);
        inicializarComponentes();
        configurarLayout();
        agregarListeners();
    }
    
    private void inicializarComponentes() {
        // Título
        lblTitulo = new JLabel("📋 RESULTADOS", SwingConstants.CENTER);
        lblTitulo.setFont(UIUtils.TITULO);
        lblTitulo.setForeground(UIUtils.TEXTO_PRIMARIO);
        
        // Área de resultados (multilínea)
        txtResultados = new JTextArea();
        txtResultados.setFont(new Font("Consolas", Font.PLAIN, 13));
        txtResultados.setEditable(false);
        txtResultados.setBackground(new Color(248, 250, 252));
        txtResultados.setForeground(UIUtils.TEXTO_PRIMARIO);
        txtResultados.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        
        // Scroll para resultados
        scrollResultados = new JScrollPane(txtResultados);
        scrollResultados.setBorder(null);
        scrollResultados.setPreferredSize(new Dimension(0, 120));
        scrollResultados.getVerticalScrollBar().setUnitIncrement(10);
        
        // Botón limpiar
        btnLimpiar = UIUtils.crearBoton("🧹 LIMPIAR", new Color(107, 114, 128));
    }
    
    private void configurarLayout() {
        setLayout(new BorderLayout(10, 10));
        
        // Panel superior: Título
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UIUtils.BG_PRIMARIO);
        topPanel.add(lblTitulo, BorderLayout.NORTH);
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Panel central: Scroll de resultados
        add(topPanel, BorderLayout.NORTH);
        add(scrollResultados, BorderLayout.CENTER);
        
        // Panel inferior: Botón limpiar
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(UIUtils.BG_PRIMARIO);
        bottomPanel.add(btnLimpiar);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void agregarListeners() {
        btnLimpiar.addActionListener(e -> limpiar());
    }
    
    // Método público que llama MainApp
    public void mostrarResultado(String mensaje) {
        String timestamp = java.time.LocalTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
        );
        String linea = String.format("[%s] %s\n", timestamp, mensaje);
        
        // Agregar al final
        String contenidoActual = txtResultados.getText();
        txtResultados.setText(contenidoActual + linea);
        
        // Auto-scroll al final
        txtResultados.setCaretPosition(txtResultados.getDocument().getLength());
        
        // Cambiar color de fondo temporalmente para destacar
        txtResultados.setBackground(new Color(240, 249, 255));
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                SwingUtilities.invokeLater(() -> {
                    txtResultados.setBackground(new Color(248, 250, 252));
                });
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    public void limpiar() {
        txtResultados.setText("");
        txtResultados.setBackground(new Color(248, 250, 252));
    }
}
