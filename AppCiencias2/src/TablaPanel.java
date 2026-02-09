import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Panel que muestra los datos del arreglo en una tabla.
 */
public class TablaPanel extends JPanel {
    private MainApp mainApp;
    private JTable tablaDatos;
    private DefaultTableModel modeloTabla;
    private JLabel lblTitulo, lblEstadisticas;
    
    /**
     * Crea el panel y configura sus componentes.
     */
    public TablaPanel(MainApp mainApp) {
        this.mainApp = mainApp;
        UIUtils.configurarPanel(this);
        inicializarComponentes();
        configurarLayout();
    }
    
    /**
     * Inicializa los componentes visuales.
     */
    private void inicializarComponentes() {
        lblTitulo = new JLabel("TABLA DE DATOS", SwingConstants.CENTER);
        lblTitulo.setFont(UIUtils.TITULO);
        lblTitulo.setForeground(UIUtils.TEXTO_PRIMARIO);
        
        lblEstadisticas = new JLabel("Elementos: 0 | Capacidad: -- | Dígitos: --", 
                                   SwingConstants.CENTER);
        lblEstadisticas.setFont(UIUtils.MUTED);
        lblEstadisticas.setForeground(UIUtils.TEXTO_MUTED);
        
        String[] columnas = {"#", "Número", "Formateado"};
        modeloTabla = new DefaultTableModel(columnas, 0);
        tablaDatos = new JTable(modeloTabla);
        
        tablaDatos.setFont(UIUtils.NORMAL);
        tablaDatos.setRowHeight(35);
        tablaDatos.setGridColor(new Color(229, 231, 235));
        tablaDatos.setShowHorizontalLines(true);
        tablaDatos.setShowVerticalLines(false);
        tablaDatos.setSelectionBackground(new Color(59, 130, 246));
        tablaDatos.setSelectionForeground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(tablaDatos);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
    }
    
    /**
     * Organiza los componentes en el panel.
     */
    private void configurarLayout() {
        setLayout(new BorderLayout(10, 10));
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UIUtils.BG_PRIMARIO);
        topPanel.add(lblTitulo, BorderLayout.NORTH);
        topPanel.add(lblEstadisticas, BorderLayout.SOUTH);
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(tablaDatos), BorderLayout.CENTER);
    }
    
    /**
     * Actualiza los datos mostrados en la tabla.
     */
    public void actualizarTabla() {
        modeloTabla.setRowCount(0);
        
        ArregloNumeros arreglo = mainApp.getArreglo();
        ArrayList<Integer> datos = arreglo.obtenerDatos();
        ArrayList<String> formateados = arreglo.obtenerFormateado();
        
        for (int i = 0; i < datos.size(); i++) {
            modeloTabla.addRow(new Object[]{
                i + 1, 
                datos.get(i), 
                formateados.get(i)
            });
        }
        
        actualizarEstadisticas(arreglo);
        
        for (int i = 0; i < tablaDatos.getColumnCount(); i++) {
            tablaDatos.getColumnModel().getColumn(i).setPreferredWidth(120);
        }
    }
    
    /**
     * Actualiza el texto de estadísticas.
     */
    private void actualizarEstadisticas(ArregloNumeros arreglo) {
        int elementos = arreglo.obtenerDatos().size();
        lblEstadisticas.setText(String.format(
            "Elementos: %d | Capacidad: %d | Dígitos: %d", 
            elementos, 
            arreglo.getTamaño(),
            arreglo.getDigitos()
        ));
        lblEstadisticas.setForeground(UIUtils.TEXTO_PRIMARIO);
    }
    
    /**
     * Limpia la tabla y el texto de estadísticas.
     */
    public void limpiar() {
        modeloTabla.setRowCount(0);
        lblEstadisticas.setText("Elementos: 0 | Capacidad: -- | Dígitos: --");
        lblEstadisticas.setForeground(UIUtils.TEXTO_MUTED);
    }
}
