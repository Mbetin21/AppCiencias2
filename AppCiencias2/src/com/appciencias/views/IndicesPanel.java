package com.appciencias.views;

import com.appciencias.algorithms.Indices;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

/**
 * Panel para el calculo tecnico de indices en busquedas externas.
 */
public class IndicesPanel extends JPanel {

    private static final String METODO_PRIMARIO = "Índice Primario";
    private static final String METODO_SECUNDARIO = "Índice Secundario";
    private static final String METODO_MULTI_PRIMARIO = "Multinivel Primario";
    private static final String METODO_MULTI_SECUNDARIO = "Multinivel Secundario";

    private final Color backgroundColor = UIManager.getColor("Panel.background") != null
            ? UIManager.getColor("Panel.background")
            : new Color(245, 245, 250);
    private final Color panelColor = UIManager.getColor("Panel.background") != null
            ? UIManager.getColor("Panel.background")
            : new Color(235, 235, 245);
    private final Color textPrimary = UIManager.getColor("Label.foreground") != null
            ? UIManager.getColor("Label.foreground")
            : new Color(70, 70, 80);
    private final Color borderColor = UIManager.getColor("Separator.foreground") != null
            ? UIManager.getColor("Separator.foreground")
            : new Color(200, 200, 210);

    private JTextField rField;
    private JTextField bField;
    private JTextField rLengthField;
    private JTextField riField;
    private JComboBox<String> metodoCombo;
    private JTextArea resultadosArea;

    public IndicesPanel() {
        initializePanel();
    }

    private void initializePanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(backgroundColor);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(backgroundColor);

        JLabel titleLabel = new JLabel("Cálculo de Índices");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(textPrimary);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(titleLabel);
        header.add(Box.createVerticalStrut(8));

        JTextArea description = new JTextArea(
                "Calcula el factor de bloqueo, bloques y accesos para índice primario, secundario y multinivel "
                + "a partir de r, B, R y Ri, mostrando la traza técnica completa del método seleccionado.");
        description.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        description.setForeground(textPrimary);
        description.setBackground(backgroundColor);
        description.setEditable(false);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(description);

        return header;
    }

    private JPanel createCenterPanel() {
        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setBackground(backgroundColor);

        JPanel fixedTop = new JPanel();
        fixedTop.setLayout(new BoxLayout(fixedTop, BoxLayout.Y_AXIS));
        fixedTop.setBackground(panelColor);
        fixedTop.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JPanel configPanel = createConfigPanel();
        configPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        fixedTop.add(configPanel);

        Dimension topPref = fixedTop.getPreferredSize();
        fixedTop.setMaximumSize(new Dimension(Integer.MAX_VALUE, topPref.height));

        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setBackground(backgroundColor);
        topWrapper.add(fixedTop, BorderLayout.NORTH);

        center.add(topWrapper, BorderLayout.NORTH);
        center.add(createResultsPanel(), BorderLayout.CENTER);

        return center;
    }

    private JPanel createConfigPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(panelColor);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel rLabel = createLabel("r (Registros)");
        JLabel bLabel = createLabel("B (Tamaño Bloque)");
        JLabel rLengthLabel = createLabel("R (Longitud Dato)");
        JLabel riLabel = createLabel("Ri (Longitud Índice)");
        JLabel metodoLabel = createLabel("Método");

        rField = createTextField(10);
        bField = createTextField(10);
        rLengthField = createTextField(10);
        riField = createTextField(10);

        metodoCombo = new JComboBox<>(new String[]{
            METODO_PRIMARIO,
            METODO_SECUNDARIO,
            METODO_MULTI_PRIMARIO,
            METODO_MULTI_SECUNDARIO
        });
        metodoCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JButton calcularButton = createButton("Calcular");
        calcularButton.addActionListener(e -> handleCalcular());

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        panel.add(rLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.4;
        panel.add(rField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        panel.add(bLabel, gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.4;
        panel.add(bField, gbc);

        gbc.gridx = 4;
        gbc.weightx = 0.0;
        panel.add(rLengthLabel, gbc);

        gbc.gridx = 5;
        gbc.weightx = 0.4;
        panel.add(rLengthField, gbc);

        gbc.gridx = 6;
        gbc.weightx = 0.0;
        panel.add(riLabel, gbc);

        gbc.gridx = 7;
        gbc.weightx = 0.4;
        panel.add(riField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        panel.add(metodoLabel, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 5;
        gbc.weightx = 1.0;
        panel.add(metodoCombo, gbc);

        gbc.gridx = 6;
        gbc.gridwidth = 2;
        gbc.weightx = 0.0;
        panel.add(calcularButton, gbc);

        return panel;
    }

    private JScrollPane createResultsPanel() {
        resultadosArea = new JTextArea();
        resultadosArea.setEditable(false);
        resultadosArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        resultadosArea.setLineWrap(false);
        resultadosArea.setWrapStyleWord(false);
        resultadosArea.setBackground(Color.WHITE);
        resultadosArea.setForeground(textPrimary);

        resultadosArea.setText(
                "Resultados técnicos del cálculo\n"
                + "Ingrese r, B, R, Ri, seleccione el método y presione Calcular."
        );

        JScrollPane scrollPane = new JScrollPane(resultadosArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(borderColor, 1));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(textPrimary);
        return label;
    }

    private JTextField createTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return field;
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setFocusPainted(false);
        return button;
    }

    private void handleCalcular() {
        try {
            long r = parsePositiveLong(rField.getText(), "r (Registros)");
            int b = parsePositiveInt(bField.getText(), "B (Tamaño Bloque)");
            int rLen = parsePositiveInt(rLengthField.getText(), "R (Longitud Dato)");
            int ri = parsePositiveInt(riField.getText(), "Ri (Longitud Índice)");

            String metodo = (String) metodoCombo.getSelectedItem();
            ResultadoIndiceView resultado = calcularSegunMetodo(metodo, r, b, rLen, ri);
            renderResultado(resultado);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Validación de datos",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Ocurrió un error al calcular índices: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private ResultadoIndiceView calcularSegunMetodo(String metodo, long r, int b, int rLen, int ri) {
        if (metodo == null) {
            throw new IllegalArgumentException("Seleccione un método de cálculo.");
        }

        switch (metodo) {
            case METODO_PRIMARIO:
                return calcularPrimario(r, b, rLen, ri);
            case METODO_SECUNDARIO:
                return calcularSecundario(r, b, rLen, ri);
            case METODO_MULTI_PRIMARIO:
                return calcularMultinivelPrimario(r, b, rLen, ri);
            case METODO_MULTI_SECUNDARIO:
                return calcularMultinivelSecundario(r, b, rLen, ri);
            default:
                throw new IllegalArgumentException("Método no soportado: " + metodo);
        }
    }

    private ResultadoIndiceView calcularPrimario(long r, int b, int rLen, int ri) {
        Object resultadoReflexion = tryInvokeBackend("calcularPrimario", r, b, rLen, ri);
        if (resultadoReflexion != null) {
            return mapResultadoIndiceReflect(resultadoReflexion);
        }

        Indices.Resultado resultado = Indices.calcular(r, b, rLen, ri);
        return new ResultadoIndiceView(
                METODO_PRIMARIO,
                resultado.bfr,
                resultado.b,
                resultado.bfri,
                resultado.bi_primario,
                resultado.accesos_primario,
                null
        );
    }

    private ResultadoIndiceView calcularSecundario(long r, int b, int rLen, int ri) {
        Object resultadoReflexion = tryInvokeBackend("calcularSecundario", r, b, rLen, ri);
        if (resultadoReflexion != null) {
            return mapResultadoIndiceReflect(resultadoReflexion);
        }

        Indices.Resultado resultado = Indices.calcular(r, b, rLen, ri);
        return new ResultadoIndiceView(
                METODO_SECUNDARIO,
                resultado.bfr,
                resultado.b,
                resultado.bfri,
                resultado.bi_secundario,
                resultado.accesos_secundario,
                null
        );
    }

    private ResultadoIndiceView calcularMultinivelPrimario(long r, int b, int rLen, int ri) {
        Object resultadoReflexion = tryInvokeBackend("calcularMultinivelPrimario", r, b, rLen, ri);
        if (resultadoReflexion != null) {
            return mapResultadoIndiceReflect(resultadoReflexion);
        }

        Indices.Resultado resultado = Indices.calcular(r, b, rLen, ri);
        long biPrimerNivel = resultado.bloquesPorNivel_primario != null && resultado.bloquesPorNivel_primario.length > 0
                ? resultado.bloquesPorNivel_primario[0]
                : 0;

        return new ResultadoIndiceView(
                METODO_MULTI_PRIMARIO,
                resultado.bfr,
                resultado.b,
                resultado.bfri,
                biPrimerNivel,
                resultado.accesos_multi_primario,
                resultado.bloquesPorNivel_primario
        );
    }

    private ResultadoIndiceView calcularMultinivelSecundario(long r, int b, int rLen, int ri) {
        Object resultadoReflexion = tryInvokeBackend("calcularMultinivelSecundario", r, b, rLen, ri);
        if (resultadoReflexion != null) {
            return mapResultadoIndiceReflect(resultadoReflexion);
        }

        Indices.Resultado resultado = Indices.calcular(r, b, rLen, ri);
        long biPrimerNivel = resultado.bloquesPorNivel_secundario != null && resultado.bloquesPorNivel_secundario.length > 0
                ? resultado.bloquesPorNivel_secundario[0]
                : 0;

        return new ResultadoIndiceView(
                METODO_MULTI_SECUNDARIO,
                resultado.bfr,
                resultado.b,
                resultado.bfri,
                biPrimerNivel,
                resultado.accesos_multi_secundario,
                resultado.bloquesPorNivel_secundario
        );
    }

    private Object tryInvokeBackend(String methodName, long r, int b, int rLen, int ri) {
        try {
            Method method = Indices.class.getMethod(methodName, long.class, int.class, int.class, int.class);
            return method.invoke(null, r, b, rLen, ri);
        } catch (NoSuchMethodException ex) {
            return null;
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("No se pudo acceder al método " + methodName + ".", ex);
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new IllegalStateException("Error interno al invocar " + methodName + ".", ex);
        }
    }

    private ResultadoIndiceView mapResultadoIndiceReflect(Object resultado) {
        String tipo = readStringField(resultado, "tipo", "Índice");
        int bfr = readIntField(resultado, "bfr");
        long b = readLongField(resultado, "b");
        int bfri = readIntField(resultado, "bfri");
        long bi = readLongField(resultado, "bi");
        long accesosTotales = readLongField(resultado, "accesosTotales");
        long[] bloquesPorNivel = readLongArrayField(resultado, "bloquesPorNivel");

        return new ResultadoIndiceView(tipo, bfr, b, bfri, bi, accesosTotales, bloquesPorNivel);
    }

    private String readStringField(Object source, String fieldName, String defaultValue) {
        Object value = readField(source, fieldName);
        if (value == null) {
            return defaultValue;
        }
        return String.valueOf(value);
    }

    private int readIntField(Object source, String fieldName) {
        Object value = readField(source, fieldName);
        if (!(value instanceof Number)) {
            throw new IllegalStateException("El campo " + fieldName + " no es numérico.");
        }
        return ((Number) value).intValue();
    }

    private long readLongField(Object source, String fieldName) {
        Object value = readField(source, fieldName);
        if (!(value instanceof Number)) {
            throw new IllegalStateException("El campo " + fieldName + " no es numérico.");
        }
        return ((Number) value).longValue();
    }

    private long[] readLongArrayField(Object source, String fieldName) {
        Object value = readField(source, fieldName);
        if (value == null) {
            return null;
        }
        if (!(value instanceof long[])) {
            throw new IllegalStateException("El campo " + fieldName + " no es long[].");
        }
        return (long[]) value;
    }

    private Object readField(Object source, String fieldName) {
        try {
            Field field = source.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(source);
        } catch (NoSuchFieldException ex) {
            throw new IllegalStateException("No existe el campo " + fieldName + " en el resultado del backend.", ex);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("No se pudo leer el campo " + fieldName + " del resultado.", ex);
        }
    }

    private void renderResultado(ResultadoIndiceView resultado) {
        StringBuilder sb = new StringBuilder();
        sb.append("Método seleccionado: ").append(resultado.tipo).append("\n\n");

        sb.append("bfr (Factor bloqueo datos): ").append(resultado.bfr).append("\n");
        sb.append("b (Bloques de datos): ").append(resultado.b).append("\n");
        sb.append("bfri (Factor bloqueo índices): ").append(resultado.bfri).append("\n");
        sb.append("bi (Bloques índice primer nivel): ").append(resultado.bi).append("\n");

        long accesosIndice = Math.max(0, resultado.accesosTotales - 1);
        sb.append("Accesos: (")
                .append(accesosIndice)
                .append(") lecturas en la estructura del índice + 1 lectura al bloque de datos = ")
                .append(resultado.accesosTotales)
                .append(" accesos totales")
                .append("\n");

        if (resultado.bloquesPorNivel != null) {
            sb.append("\nBloques por nivel:\n");
            for (int i = 0; i < resultado.bloquesPorNivel.length; i++) {
                sb.append("Nivel ")
                        .append(i + 1)
                        .append(": ")
                        .append(resultado.bloquesPorNivel[i])
                        .append(" bloques")
                        .append("\n");
            }
        }

        resultadosArea.setText(sb.toString());
        resultadosArea.setCaretPosition(0);
    }

    private long parsePositiveLong(String value, String fieldName) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("El campo " + fieldName + " es obligatorio.");
        }

        try {
            long parsed = Long.parseLong(normalized);
            if (parsed <= 0) {
                throw new IllegalArgumentException("El campo " + fieldName + " debe ser mayor que 0.");
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("El campo " + fieldName + " debe ser numérico entero.");
        }
    }

    private int parsePositiveInt(String value, String fieldName) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("El campo " + fieldName + " es obligatorio.");
        }

        try {
            int parsed = Integer.parseInt(normalized);
            if (parsed <= 0) {
                throw new IllegalArgumentException("El campo " + fieldName + " debe ser mayor que 0.");
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("El campo " + fieldName + " debe ser numérico entero.");
        }
    }

    private static class ResultadoIndiceView {

        private final String tipo;
        private final int bfr;
        private final long b;
        private final int bfri;
        private final long bi;
        private final long accesosTotales;
        private final long[] bloquesPorNivel;

        ResultadoIndiceView(String tipo, int bfr, long b, int bfri, long bi, long accesosTotales, long[] bloquesPorNivel) {
            this.tipo = tipo;
            this.bfr = bfr;
            this.b = b;
            this.bfri = bfri;
            this.bi = bi;
            this.accesosTotales = accesosTotales;
            this.bloquesPorNivel = bloquesPorNivel;
        }
    }
}
