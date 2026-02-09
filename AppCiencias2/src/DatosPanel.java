import java.awt.*;
import javax.swing.*;

public class DatosPanel extends JPanel {
    private MainApp mainApp;
    private JTextField txtNumero;
    private JButton btnInsertar, btnEliminar;
    private JLabel lblTitulo, lblInstruccion, lblDigitosReq;
    
    public DatosPanel(MainApp mainApp) {
        this.mainApp = mainApp;
        UIUtils.configurarPanel(this);
        inicializarComponentes();
        configurarLayout();
        agregarListeners();
    }
    
    private void inicializarComponentes() {
        // Título
        lblTitulo = new JLabel("📝 GESTIÓN DE DATOS", SwingConstants.CENTER);
        lblTitulo.setFont(UIUtils.TITULO);
        lblTitulo.setForeground(UIUtils.TEXTO_PRIMARIO);
        
        // Instrucción
        lblInstruccion = new JLabel("Ingresa números para el arreglo:", SwingConstants.CENTER);
        lblInstruccion.setFont(UIUtils.SUBTITULO);
        lblInstruccion.setForeground(UIUtils.TEXTO_SECUNDARIO);
        
        // Dígitos requeridos (se actualiza dinámicamente)
        lblDigitosReq = new JLabel("Dígitos requeridos: --", SwingConstants.CENTER);
        lblDigitosReq.setFont(UIUtils.NORMAL);
        lblDigitosReq.setForeground(UIUtils.TEXTO_MUTED);
        
        // Campo de número
        txtNumero = new JTextField("", 10);
        txtNumero.setFont(UIUtils.NORMAL);
        
        // Botones
        btnInsertar = UIUtils.crearBoton("➕ INSERTAR", UIUtils.BG_BOTON);
        btnEliminar = UIUtils.crearBoton("🗑️ ELIMINAR", UIUtils.BG_BOTON);
    }
    
    private void configurarLayout() {
        setLayout(new BorderLayout());
        
        // Título e instrucción
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UIUtils.BG_PRIMARIO);
        topPanel.add(lblTitulo, BorderLayout.NORTH);
        topPanel.add(lblInstruccion, BorderLayout.CENTER);
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Dígitos requeridos
        JPanel digitosPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        digitosPanel.setBackground(UIUtils.BG_PRIMARIO);
        digitosPanel.add(lblDigitosReq);
        
        // Formulario central
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UIUtils.BG_PRIMARIO);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JLabel lblNumero = new JLabel("Número:");
        lblNumero.setFont(UIUtils.SUBTITULO);
        lblNumero.setForeground(UIUtils.TEXTO_SECUNDARIO);
        formPanel.add(lblNumero, gbc);
        
        gbc.gridy = 1; gbc.gridwidth = 1;
        formPanel.add(txtNumero, gbc);
        
        gbc.gridx = 1;
        formPanel.add(btnInsertar, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        formPanel.add(new JLabel(""), gbc); // Espaciador
        
        gbc.gridx = 0; gbc.gridwidth = 2;
        formPanel.add(btnEliminar, gbc);
        
        // Ensamblar
        add(topPanel, BorderLayout.NORTH);
        add(digitosPanel, BorderLayout.CENTER);
        add(formPanel, BorderLayout.SOUTH);
    }
    
    private void agregarListeners() {
        btnInsertar.addActionListener(e -> insertarNumero());
        btnEliminar.addActionListener(e -> eliminarNumero());
        
        // Enter en campo = Insertar
        txtNumero.addActionListener(e -> insertarNumero());
    }
    
    private void insertarNumero() {
        try {
            String input = txtNumero.getText().trim();
            if (input.isEmpty()) {
                mostrarError("Ingresa un número");
                return;
            }
            
            int numero = Integer.parseInt(input);
            ArregloNumeros arreglo = mainApp.getArreglo();
            
            int digitosRequeridos = arreglo.getDigitos(); 
            if (!validarDigitos(numero, digitosRequeridos)) {
                mostrarError("❌ El número debe tener EXACTAMENTE " + digitosRequeridos + " dígitos");
                return;
            }
            
            // Insertar
            arreglo.insertar(numero);
            mainApp.actualizarTabla();
            mainApp.mostrarResultado("✅ Número " + numero + " insertado correctamente");
            txtNumero.setText(""); // Limpiar campo
            
        } catch (NumberFormatException ex) {
            mostrarError("❌ Ingresa solo números enteros válidos");
        } catch (RuntimeException ex) {
            mostrarError(ex.getMessage());
        }
    }
    
    private void eliminarNumero() {
        try {
            String input = txtNumero.getText().trim();
            if (input.isEmpty()) {
                mostrarError("Ingresa el número a eliminar");
                return;
            }
            
            int numero = Integer.parseInt(input);
            ArregloNumeros arreglo = mainApp.getArreglo();
            arreglo.eliminar(numero);
            mainApp.actualizarTabla();
            mainApp.mostrarResultado("✅ Número " + numero + " eliminado correctamente");
            txtNumero.setText("");
            
        } catch (NumberFormatException ex) {
            mostrarError("❌ Ingresa solo números enteros válidos");
        }
    }
    
    private boolean validarDigitos(int numero, int digitosRequeridos) {
        // Convierte a string y cuenta dígitos (sin ceros a la izquierda)
        String numStr = String.valueOf(Math.abs(numero));
        return numStr.length() == digitosRequeridos;
    }
    
    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
        txtNumero.requestFocus();
    }
    
    // Método público para actualizar info de dígitos cuando se configura
    public void actualizarInfoDigitos(int digitos) {
        lblDigitosReq.setText("Dígitos requeridos: EXACTAMENTE " + digitos);
        lblDigitosReq.setForeground(UIUtils.TEXTO_PRIMARIO);
    }
    
    public void limpiarCampos() {
        txtNumero.setText("");
    }
}
