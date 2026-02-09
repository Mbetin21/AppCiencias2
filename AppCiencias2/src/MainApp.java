import java.awt.*;
import javax.swing.*;

/**
 * Ventana principal de la aplicación.
 */
public class MainApp extends JFrame {
    private ArregloNumeros arreglo;
    private ConfigPanel configPanel;
    private DatosPanel datosPanel;
    private TablaPanel tablaPanel;
    private OperacionesPanel operacionesPanel;
    private ResultadosPanel resultadosPanel;
    
    /**
     * Inicializa la UI y los paneles.
     */
    public MainApp() {
        inicializarArreglo();
        configurarVentana();
        crearComponentes();
        organizarLayout();
        mostrarVentana();
    }
    
    /**
     * Crea el arreglo por defecto.
     */
    private void inicializarArreglo() {
        // Por defecto hasta que el usuario configure
        arreglo = new ArregloNumeros(10, 3);
    }
    
    /**
     * Configura la ventana principal.
     */
    private void configurarVentana() {
        setTitle(" Algoritmos de Búsqueda - App Profesional");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centrar en pantalla
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(248, 250, 252));
    }
    
    /**
     * Crea los paneles de la interfaz.
     */
    private void crearComponentes() {
        configPanel = new ConfigPanel(this);
        datosPanel = new DatosPanel(this);
        tablaPanel = new TablaPanel(this);
        operacionesPanel = new OperacionesPanel(this);
        resultadosPanel = new ResultadosPanel(this);
    }
    
    /**
     * Organiza los paneles en la ventana.
     */
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
    
    /**
     * Muestra la ventana.
     */
    private void mostrarVentana() {
        setVisible(true);
    }

    /**
     * Actualiza paneles con la configuración actual.
     */
    public void configurarInfoPaneles(int tamaño, int digitos) {
        try {
            datosPanel.actualizarInfoDigitos(digitos);
            tablaPanel.actualizarTabla();
        } catch (Exception e) {
            // Paneles no inicializados aún
        }
    }
    
    /** Devuelve el arreglo actual. */
    public ArregloNumeros getArreglo() { return arreglo; }
    /** Refresca la tabla. */
    public void actualizarTabla() { tablaPanel.actualizarTabla(); }
    /** Muestra un resultado en el panel. */
    public void mostrarResultado(String texto) { resultadosPanel.mostrarResultado(texto); }
    
    /**
     * Crea un nuevo arreglo con la configuración indicada.
     */
    public void configurarArreglo(int tamano, int digitos) {
        arreglo = new ArregloNumeros(tamano, digitos);
        configurarInfoPaneles(tamano, digitos);
        datosPanel.limpiarCampos();
        resultadosPanel.limpiar();
    }
    
    /**
     * Punto de entrada de la aplicación.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainApp());
    }
}
