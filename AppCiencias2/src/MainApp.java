import java.awt.*;
import javax.swing.*;

public class MainApp extends JFrame {
    private ArregloNumeros arreglo;
    private ConfigPanel configPanel;
    private DatosPanel datosPanel;
    private TablaPanel tablaPanel;
    private OperacionesPanel operacionesPanel;
    private ResultadosPanel resultadosPanel;
    
    public MainApp() {
        inicializarArreglo();
        configurarVentana();
        crearComponentes();
        organizarLayout();
        mostrarVentana();
    }
    
    private void inicializarArreglo() {
        // Por defecto hasta que el usuario configure
        arreglo = new ArregloNumeros(10, 3);
    }
    
    private void configurarVentana() {
        setTitle("🔍 Algoritmos de Búsqueda - App Profesional");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centrar en pantalla
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(248, 250, 252));
    }
    
    private void crearComponentes() {
        configPanel = new ConfigPanel(this);
        datosPanel = new DatosPanel(this);
        tablaPanel = new TablaPanel(this);
        operacionesPanel = new OperacionesPanel(this);
        resultadosPanel = new ResultadosPanel(this);
    }
    
    private void organizarLayout() {
        // Panel superior: Configuración
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(configPanel, BorderLayout.CENTER);
        add(northPanel, BorderLayout.NORTH);
        
        // Panel central: Datos + Tabla
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        centerPanel.add(datosPanel);
        centerPanel.add(tablaPanel);
        add(centerPanel, BorderLayout.CENTER);
        
        // Panel inferior: Operaciones + Resultados
        JPanel southPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        southPanel.add(operacionesPanel);
        southPanel.add(resultadosPanel);
        add(southPanel, BorderLayout.SOUTH);
    }
    
    private void mostrarVentana() {
        setVisible(true);
    }

    public void configurarInfoPaneles(int tamaño, int digitos) {
    try {
        datosPanel.actualizarInfoDigitos(digitos);
        tablaPanel.actualizarTabla();
    } catch (Exception e) {
        // Paneles no inicializados aún
    }
    }
    
    // GETTERS para que otros paneles accedan al arreglo
    public ArregloNumeros getArreglo() { return arreglo; }
    public void actualizarTabla() { tablaPanel.actualizarTabla(); }
    public void mostrarResultado(String texto) { resultadosPanel.mostrarResultado(texto); }
    
    // Método para configurar nuevo arreglo
    public void configurarArreglo(int tamano, int digitos) {
    arreglo = new ArregloNumeros(tamano, digitos);
    configurarInfoPaneles(tamano, digitos);  // ← Nueva línea
    datosPanel.limpiarCampos();
    resultadosPanel.limpiar();
}
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainApp());
    }
}
