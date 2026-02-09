import java.awt.*;
import javax.swing.*;

/**
 * Panel que muestra mensajes de resultados y permite limpiar.
 */
public class ResultadosPanel extends JPanel {
    private MainApp mainApp;
    private JLabel lblTitulo;
    private JTextArea txtResultados;
    private JButton btnLimpiar;
    private JScrollPane scrollResultados;
    
    /**
     * Crea el panel y configura sus componentes.
     */
    public ResultadosPanel(MainApp mainApp) {
        this.mainApp = mainApp;
        UIUtils.configurarPanel(this);
        inicializarComponentes();
        configurarLayout();
        agregarListeners();
    }
    
    /**
     * Inicializa los componentes visuales.
     */
    private void inicializarComponentes() {
        lblTitulo = new JLabel("RESULTADOS", SwingConstants.CENTER);
        lblTitulo.setFont(UIUtils.TITULO);
        lblTitulo.setForeground(UIUtils.TEXTO_PRIMARIO);
        
        txtResultados = new JTextArea();
        txtResultados.setFont(new Font("Consolas", Font.PLAIN, 13));
        txtResultados.setEditable(false);
        txtResultados.setBackground(new Color(248, 250, 252));
        txtResultados.setForeground(UIUtils.TEXTO_PRIMARIO);
        txtResultados.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        
        scrollResultados = new JScrollPane(txtResultados);
        scrollResultados.setBorder(null);
        scrollResultados.setPreferredSize(new Dimension(0, 120));
        scrollResultados.getVerticalScrollBar().setUnitIncrement(10);
        
        btnLimpiar = UIUtils.crearBoton("LIMPIAR", new Color(107, 114, 128));
    }
    
    /**
     * Organiza los componentes en el panel.
     */
    private void configurarLayout() {
        setLayout(new BorderLayout(10, 10));
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UIUtils.BG_PRIMARIO);
        topPanel.add(lblTitulo, BorderLayout.NORTH);
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        add(topPanel, BorderLayout.NORTH);
        add(scrollResultados, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(UIUtils.BG_PRIMARIO);
        bottomPanel.add(btnLimpiar);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Registra los listeners de la UI.
     */
    private void agregarListeners() {
        btnLimpiar.addActionListener(e -> limpiar());
    }
    
    /**
     * Agrega un mensaje a la salida.
     */
    public void mostrarResultado(String mensaje) {
        String timestamp = java.time.LocalTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
        );
        String linea = String.format("[%s] %s\n", timestamp, mensaje);
        
        String contenidoActual = txtResultados.getText();
        txtResultados.setText(contenidoActual + linea);
        
        txtResultados.setCaretPosition(txtResultados.getDocument().getLength());
        
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
    
    /**
     * Limpia la salida.
     */
    public void limpiar() {
        txtResultados.setText("");
        txtResultados.setBackground(new Color(248, 250, 252));
    }
}
