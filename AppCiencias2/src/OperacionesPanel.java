import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;

/**
 * Panel para ordenar y buscar números en el arreglo.
 */
public class OperacionesPanel extends JPanel {
    private MainApp mainApp;
    private JButton btnOrdenar, btnBuscarSecuencial, btnBuscarBinaria;
    private JLabel lblTitulo;
    private JTextField txtBuscar;
    
    /**
     * Crea el panel y configura sus componentes.
     */
    public OperacionesPanel(MainApp mainApp) {
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
        lblTitulo = new JLabel("OPERACIONES", SwingConstants.CENTER);
        lblTitulo.setFont(UIUtils.TITULO);
        lblTitulo.setForeground(UIUtils.TEXTO_PRIMARIO);
        
        txtBuscar = new JTextField("", 12);
        txtBuscar.setFont(UIUtils.NORMAL);
        
        btnOrdenar = UIUtils.crearBoton("ORDENAR", new Color(245, 158, 11));
        btnBuscarSecuencial = UIUtils.crearBoton("BÚSQUEDA SECUENCIAL", UIUtils.BG_BOTON);
        btnBuscarBinaria = UIUtils.crearBoton("BÚSQUEDA BINARIA", UIUtils.BG_BOTON);
    }
    
    /**
     * Organiza los componentes en el panel.
     */
    private void configurarLayout() {
        setLayout(new BorderLayout());
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UIUtils.BG_PRIMARIO);
        topPanel.add(lblTitulo, BorderLayout.NORTH);
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(UIUtils.BG_PRIMARIO);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JLabel lblBuscar = new JLabel("Número a buscar:");
        lblBuscar.setFont(UIUtils.SUBTITULO);
        lblBuscar.setForeground(UIUtils.TEXTO_SECUNDARIO);
        centerPanel.add(lblBuscar, gbc);
        
        gbc.gridy = 1; gbc.gridwidth = 1;
        centerPanel.add(txtBuscar, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        centerPanel.add(btnOrdenar, gbc);
        
        gbc.gridy = 3; gbc.gridwidth = 1;
        gbc.gridx = 0;
        centerPanel.add(btnBuscarSecuencial, gbc);
        gbc.gridx = 1;
        centerPanel.add(btnBuscarBinaria, gbc);
        
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }
    
    /**
     * Registra los listeners de la UI.
     */
    private void agregarListeners() {
        btnOrdenar.addActionListener(e -> ordenarDatos());
        btnBuscarSecuencial.addActionListener(e -> buscarSecuencial());
        btnBuscarBinaria.addActionListener(e -> buscarBinaria());
        txtBuscar.addActionListener(e -> buscarSecuencial());
    }
    
    /**
     * Ordena los datos del arreglo.
     */
    private void ordenarDatos() {
        try {
            ArregloNumeros arreglo = mainApp.getArreglo();
            if (arreglo.obtenerDatos().isEmpty()) {
                mostrarAdvertencia("El arreglo está vacío. Inserta datos primero.");
                return;
            }
            
            arreglo.ordenar();
            mainApp.actualizarTabla();
            mainApp.mostrarResultado("Arreglo ordenado correctamente (ascendente)");
            
        } catch (Exception ex) {
            mostrarError("Error al ordenar: " + ex.getMessage());
        }
    }
    
    /**
     * Ejecuta búsqueda secuencial.
     */
    private void buscarSecuencial() {
        ejecutarBusqueda(BusquedaSecuencial::buscar, "Búsqueda Secuencial");
    }
    
    /**
     * Ejecuta búsqueda binaria.
     */
    private void buscarBinaria() {
        ArregloNumeros arreglo = mainApp.getArreglo();
        if (!arreglo.obtenerDatos().isEmpty() && !estaOrdenado(arreglo)) {
            mostrarAdvertencia("Para Búsqueda Binaria, el arreglo debe estar ORDENADO primero.");
            return;
        }
        ejecutarBusqueda(BusquedaBinaria::buscar, "Búsqueda Binaria");
    }
    
    /**
     * Valida entrada y ejecuta el algoritmo de búsqueda.
     */
    private void ejecutarBusqueda(java.util.function.BiFunction<ArrayList<Integer>, Integer, Integer> algoritmo,
                                String nombreAlgoritmo) {
        try {
            String input = txtBuscar.getText().trim();
            if (input.isEmpty()) {
                mostrarAdvertencia("Ingresa el número a buscar");
                return;
            }
            
            int valor = Integer.parseInt(input);
            ArregloNumeros arreglo = mainApp.getArreglo();
            
            if (arreglo.obtenerDatos().isEmpty()) {
                mostrarAdvertencia("El arreglo está vacío. Inserta datos primero.");
                return;
            }
            
            if (!validarDigitos(valor, arreglo.getDigitos())) {
                mostrarAdvertencia("El número debe tener exactamente " + arreglo.getDigitos() + " dígitos");
                return;
            }
            
            ArrayList<Integer> datos = arreglo.obtenerDatos();
            int posicion = algoritmo.apply(datos, valor);
            
            if (posicion != -1) {
                mainApp.mostrarResultado(
                    String.format("%s: ENCONTRADO en posición %d (índice %d)", 
                                nombreAlgoritmo, posicion + 1, posicion)
                );
            } else {
                mainApp.mostrarResultado(String.format("%s: NO ENCONTRADO", nombreAlgoritmo));
            }
            
        } catch (NumberFormatException ex) {
            mostrarError("Ingresa solo números enteros válidos");
        }
    }
    
    /**
     * Verifica si el arreglo está ordenado.
     */
    private boolean estaOrdenado(ArregloNumeros arreglo) {
        ArrayList<Integer> datos = arreglo.obtenerDatos();
        for (int i = 1; i < datos.size(); i++) {
            if (datos.get(i) < datos.get(i - 1)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Verifica la cantidad de dígitos del número.
     */
    private boolean validarDigitos(int numero, int digitosRequeridos) {
        String numStr = String.valueOf(Math.abs(numero));
        return numStr.length() == digitosRequeridos;
    }
    
    /**
     * Muestra un mensaje de error.
     */
    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Muestra un mensaje de advertencia.
     */
    private void mostrarAdvertencia(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Advertencia", JOptionPane.WARNING_MESSAGE);
    }
}
