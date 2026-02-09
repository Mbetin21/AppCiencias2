import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class TablaPanel extends JPanel {
    private MainApp mainApp;
    private JTable tablaDatos;
    private DefaultTableModel modeloTabla;
    private JLabel lblTitulo, lblEstadisticas;
    
    public TablaPanel(MainApp mainApp) {
        this.mainApp = mainApp;
        UIUtils.configurarPanel(this);
        inicializarComponentes();
        configurarLayout();
    }
    
    private void inicializarComponentes() {
        // Título
        lblTitulo = new JLabel("📊 TABLA DE DATOS", SwingConstants.CENTER);
        lblTitulo.setFont(UIUtils.TITULO);
        lblTitulo.setForeground(UIUtils.TEXTO_PRIMARIO);
        
        // Estadísticas
        lblEstadisticas = new JLabel("Elementos: 0 | Capacidad: -- | Dígitos: --", 
                                   SwingConstants.CENTER);
        lblEstadisticas.setFont(UIUtils.MUTED);
        lblEstadisticas.setForeground(UIUtils.TEXTO_MUTED);
        
        // Modelo de tabla
        String[] columnas = {"#", "Número", "Formateado"};
        modeloTabla = new DefaultTableModel(columnas, 0);
        tablaDatos = new JTable(modeloTabla);
        
        // Estilo profesional de la tabla
        tablaDatos.setFont(UIUtils.NORMAL);
        tablaDatos.setRowHeight(35);
        tablaDatos.setGridColor(new Color(229, 231, 235));
        tablaDatos.setShowHorizontalLines(true);
        tablaDatos.setShowVerticalLines(false);
        tablaDatos.setSelectionBackground(new Color(59, 130, 246));
        tablaDatos.setSelectionForeground(Color.WHITE);
        
        // Scroll con estilo
        JScrollPane scrollPane = new JScrollPane(tablaDatos);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
    }
    
    private void configurarLayout() {
        setLayout(new BorderLayout(10, 10));
        
        // Panel superior: Título + Estadísticas
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UIUtils.BG_PRIMARIO);
        topPanel.add(lblTitulo, BorderLayout.NORTH);
        topPanel.add(lblEstadisticas, BorderLayout.SOUTH);
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Tabla central
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(tablaDatos), BorderLayout.CENTER);
    }
    
    // Método público para actualizar la tabla
    public void actualizarTabla() {
        modeloTabla.setRowCount(0); // Limpiar tabla
        
        ArregloNumeros arreglo = mainApp.getArreglo();
        ArrayList<Integer> datos = arreglo.obtenerDatos();
        ArrayList<String> formateados = arreglo.obtenerFormateado();
        
        // Llenar tabla
        for (int i = 0; i < datos.size(); i++) {
            modeloTabla.addRow(new Object[]{
                i + 1, 
                datos.get(i), 
                formateados.get(i)
            });
        }
        
        // Actualizar estadísticas
        actualizarEstadisticas(arreglo);
        
        // Auto-ajustar columnas
        for (int i = 0; i < tablaDatos.getColumnCount(); i++) {
            tablaDatos.getColumnModel().getColumn(i).setPreferredWidth(120);
        }
    }
    
    private void actualizarEstadisticas(ArregloNumeros arreglo) {
        int elementos = arreglo.obtenerDatos().size();
        lblEstadisticas.setText(String.format(
            "Elementos: %d | Capacidad: %d | Dígitos: %d", 
            elementos, 
            arreglo.getTamaño(),  // ← Necesitarás este getter también
            arreglo.getDigitos()
        ));
        lblEstadisticas.setForeground(UIUtils.TEXTO_PRIMARIO);
    }
    
    // Método público para limpiar al configurar nuevo arreglo
    public void limpiar() {
        modeloTabla.setRowCount(0);
        lblEstadisticas.setText("Elementos: 0 | Capacidad: -- | Dígitos: --");
        lblEstadisticas.setForeground(UIUtils.TEXTO_MUTED);
    }
}
