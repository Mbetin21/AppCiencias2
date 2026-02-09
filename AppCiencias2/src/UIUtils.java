import java.awt.*;
import javax.swing.*;

public class UIUtils {
    // 🎨 COLORES CONSISTENTES EN TODA LA APP
    public static final Color BG_PRIMARIO = new Color(248, 250, 252);
    public static final Color TEXTO_PRIMARIO = new Color(17, 24, 39);
    public static final Color TEXTO_SECUNDARIO = new Color(51, 65, 85);
    public static final Color TEXTO_MUTED = new Color(100, 116, 139);
    public static final Color BG_BOTON = new Color(34, 197, 94);
    public static final Color BG_BOTON_SECUNDARIO = new Color(59, 130, 246);
    public static final Color BG_ROJO = new Color(239, 68, 68);
    
    // 📝 FUENTES CONSISTENTES
    public static final Font TITULO = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font SUBTITULO = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font NORMAL = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font MUTED = new Font("Segoe UI", Font.ITALIC, 12);
    
    // 🛠️ CONFIGURACIONES PREDEFINIDAS
    public static void configurarPanel(JPanel panel) {
        panel.setBackground(BG_PRIMARIO);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    }
    
    public static JButton crearBoton(String texto, Color bgColor) {
        JButton btn = new JButton(texto);
        btn.setFont(SUBTITULO);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        return btn;
    }
    
    public static void aplicarEstiloCampo(JTextField campo) {
        campo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        campo.setBackground(Color.WHITE);
    }
}
