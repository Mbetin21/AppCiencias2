import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;  


/**
 * Panel de configuración inicial de la aplicación.
 * Permite definir tamaño máximo del arreglo y dígitos por número.
 */
public class ConfigPanel extends JPanel {
    private MainApp mainApp;
    private JTextField txtTamaño;
    private JTextField txtDigitos;
    private JButton btnConfigurar;
    private JLabel lblTitulo, lblAyuda, lblTamaño, lblDigitos;
    
    /**
     * Crea el panel y configura sus componentes.
     */
    public ConfigPanel(MainApp mainApp) {
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
        lblTitulo = new JLabel("CONFIGURACIÓN INICIAL", SwingConstants.CENTER);
        lblTitulo.setFont(UIUtils.TITULO);
        lblTitulo.setForeground(UIUtils.TEXTO_PRIMARIO);
        
        lblTamaño = new JLabel("Tamaño máximo del arreglo:");
        lblTamaño.setFont(UIUtils.SUBTITULO);
        lblTamaño.setForeground(UIUtils.TEXTO_SECUNDARIO);
        txtTamaño = new JTextField("10", 8);
        txtTamaño.setFont(UIUtils.NORMAL);
        
        lblDigitos = new JLabel("Dígitos por número:");
        lblDigitos.setFont(UIUtils.SUBTITULO);
        lblDigitos.setForeground(UIUtils.TEXTO_SECUNDARIO);
        txtDigitos = new JTextField("3", 8);
        txtDigitos.setFont(UIUtils.NORMAL);
        
        btnConfigurar = UIUtils.crearBoton("CONFIGURAR Y EMPEZAR", UIUtils.BG_BOTON);
        
        lblAyuda = new JLabel(
            "<html><center>Define el tamaño máximo del arreglo y<br>los dígitos que tendrán cada número.</center></html>",
            SwingConstants.CENTER
        );
        lblAyuda.setFont(UIUtils.MUTED);
        lblAyuda.setForeground(UIUtils.TEXTO_MUTED);
    }
    
    /**
     * Configura el layout y organiza los componentes.
     */
    private void configurarLayout() {
        setLayout(new BorderLayout());
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UIUtils.BG_PRIMARIO);
        topPanel.add(lblTitulo, BorderLayout.NORTH);
        topPanel.add(lblAyuda, BorderLayout.CENTER);
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UIUtils.BG_PRIMARIO);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(lblTamaño, gbc);
        gbc.gridx = 1;
        formPanel.add(txtTamaño, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(lblDigitos, gbc);
        gbc.gridx = 1;
        formPanel.add(txtDigitos, gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(UIUtils.BG_PRIMARIO);
        buttonPanel.add(btnConfigurar);
        
        add(topPanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Agrega los listeners del panel.
     */
    private void agregarListeners() {
        btnConfigurar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                configurarArreglo();
            }
        });
    }
    
    /**
     * Valida la entrada y aplica la configuración en la aplicación.
     */
    private void configurarArreglo() {
        try {
            String tamStr = txtTamaño.getText().trim();
            String digStr = txtDigitos.getText().trim();
            
            if (tamStr.isEmpty() || digStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Por favor completa todos los campos", 
                    "Error de configuración", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int tamaño = Integer.parseInt(tamStr);
            int digitos = Integer.parseInt(digStr);
            
            if (tamaño <= 0 || tamaño > 100) {
                JOptionPane.showMessageDialog(this, 
                    "El tamaño debe estar entre 1 y 100", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (digitos < 1 || digitos > 5) {
                JOptionPane.showMessageDialog(this, 
                    "Los dígitos deben estar entre 1 y 5", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            mainApp.configurarArreglo(tamaño, digitos);
            JOptionPane.showMessageDialog(this, 
                "Arreglo configurado correctamente!\nTamaño máximo: " + tamaño + "\nDígitos por número: " + digitos,
                "Configuración exitosa", JOptionPane.INFORMATION_MESSAGE);
                
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, 
                "Ingresa solo números enteros válidos", 
                "Error de formato", JOptionPane.ERROR_MESSAGE);
        }
    }
}
