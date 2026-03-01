package com.appciencias.models;

import com.appciencias.algorithms.*;
import java.awt.*;
import java.awt.print.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Gestor de archivos
 *
 * Maneja guardar, recuperar, exportar a PDF e imprimir para todas las
 * estructuras del programa. Abre el explorador de carpetas del sistema
 * (JFileChooser) para que el usuario elija nombre y ubicación.
 */
public class GestorArchivos {

    /**
     * Contiene todos los datos de una sesion de trabajo.
     * Solo guarda lo necesario para reconstruir cada estructura.
     */
    public static class SesionDatos implements Serializable {
        private static final long serialVersionUID = 1L;

        public String tipoEstructura;   // "SECUENCIAL", "BINARIO"...
        public String fecha;            
        public String nombreSesion;     // nombre que le da el usuario

        // Datos compartidos (Secuencial, Binario, Hash, Listas, Arreglos) 
        public int    tamaño;
        public int    longClave;
        public List<String> claves;     // claves insertadas en orden

        // Configuración específica de Hash 
        public String funcionHash;      // "MOD", "CUADRADO", etc.
        public String tipoColision;     // "LINEAL", "CUADRATICA", "DOBLE_HASH"
        public String tipoPlegamiento;  // "SUMA", "MULTIPLICACION"
        public int[]  posicionesTrunc;  // TRUNCAMIENTO

        public boolean ordenado;

        // Arbol Múltiple 
        public int m; // bits por nivel

        // Huffman 
        public String textoHuffman;

        public SesionDatos(String tipoEstructura) {
            this.tipoEstructura = tipoEstructura;
            this.fecha = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
            this.claves = new ArrayList<>();
        }
    }

    /**
     * Abre el explorador de carpetas y guarda la sesion en un archivo .dat
     *
     * @param parent  Componente padre para centrar el JFileChooser
     * @param sesion  Datos de la sesión actual
     * @return true si se guardó correctamente, false si el usuario canceló
     */
    public static boolean guardar(Component parent, SesionDatos sesion) {
        JFileChooser chooser = crearChooser("Guardar sesión",
                new FileNameExtensionFilter("Archivo de sesión (*.dat)", "dat"));

        // Nombre sugerido: tipo + fecha
        String nombreSugerido = sesion.tipoEstructura.toLowerCase() + "_"
                + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        chooser.setSelectedFile(new File(nombreSugerido + ".dat"));

        int resultado = chooser.showSaveDialog(parent);
        if (resultado != JFileChooser.APPROVE_OPTION) return false;

        File archivo = chooser.getSelectedFile();
        // Asegurar extensipn .dat
        if (!archivo.getName().toLowerCase().endsWith(".dat")) {
            archivo = new File(archivo.getAbsolutePath() + ".dat");
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(archivo))) {
            oos.writeObject(sesion);
            mostrarMensaje(parent, "Sesión guardada correctamente en:\n" + archivo.getAbsolutePath(),
                    "Guardado exitoso", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (IOException e) {
            mostrarMensaje(parent, "Error al guardar: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Abre el explorador de carpetas y carga una sesion desde un archivo .dat
     *
     * @param parent Componente padre para centrar el JFileChooser
     * @return SesionDatos cargada, o null si el usuario canceló o hubo error
     */
    public static SesionDatos recuperar(Component parent) {
        JFileChooser chooser = crearChooser("Recuperar sesión",
                new FileNameExtensionFilter("Archivo de sesión (*.dat)", "dat"));

        int resultado = chooser.showOpenDialog(parent);
        if (resultado != JFileChooser.APPROVE_OPTION) return null;

        File archivo = chooser.getSelectedFile();

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivo))) {
            SesionDatos sesion = (SesionDatos) ois.readObject();
            mostrarMensaje(parent,
                    "Sesión recuperada:\n" + sesion.tipoEstructura + "\nGuardada el: " + sesion.fecha,
                    "Recuperación exitosa", JOptionPane.INFORMATION_MESSAGE);
            return sesion;
        } catch (IOException | ClassNotFoundException e) {
            mostrarMensaje(parent, "Error al recuperar: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    /**
     * Genera un PDF con los datos de la sesion y abre el explorador para guardarlo.
     *
     * @param parent Componente padre
     * @param sesion Datos a exportar
     * @return true si se exportó correctamente
     */
    public static boolean exportarPDF(Component parent, SesionDatos sesion) {
        JFileChooser chooser = crearChooser("Exportar a PDF",
                new FileNameExtensionFilter("Archivo PDF (*.pdf)", "pdf"));

        String nombreSugerido = sesion.tipoEstructura.toLowerCase() + "_"
                + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        chooser.setSelectedFile(new File(nombreSugerido + ".pdf"));

        int resultado = chooser.showSaveDialog(parent);
        if (resultado != JFileChooser.APPROVE_OPTION) return false;

        File archivo = chooser.getSelectedFile();
        if (!archivo.getName().toLowerCase().endsWith(".pdf")) {
            archivo = new File(archivo.getAbsolutePath() + ".pdf");
        }

        try {
            generarPDF(archivo, sesion);
            mostrarMensaje(parent, "PDF generado correctamente en:\n" + archivo.getAbsolutePath(),
                    "PDF generado", JOptionPane.INFORMATION_MESSAGE);

            // Abrir el PDF automáticamente si el sistema lo permite
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(archivo);
            }
            return true;
        } catch (Exception e) {
            mostrarMensaje(parent, "Error al generar PDF: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Genera el archivo PDF usando PrinterJob con destino a archivo.
     * Dibuja el contenido con Graphics2D pagina por pagina.
     */
    private static void generarPDF(File archivo, SesionDatos sesion) throws Exception {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Ciencias2_" + sesion.tipoEstructura);

        PageFormat pf = job.defaultPage();
        Paper paper = new Paper();
        // Tamaño carta
        double ancho  = 612;
        double alto   = 792;
        double margen = 50;
        paper.setSize(ancho, alto);
        paper.setImageableArea(margen, margen, ancho - 2 * margen, alto - 2 * margen);
        pf.setPaper(paper);

        List<String> lineas = generarLineasContenido(sesion);

        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            int lineasPorPagina = 45;
            int totalPaginas    = (int) Math.ceil((double) lineas.size() / lineasPorPagina);
            if (pageIndex >= totalPaginas) return Printable.NO_SUCH_PAGE;

            Graphics2D g2 = (Graphics2D) graphics;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            double x = pageFormat.getImageableX();
            double y = pageFormat.getImageableY();

            // Encabezado
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            g2.setColor(new Color(60, 60, 80));
            g2.drawString("Ciencias de la Computación 2", (int) x, (int) y + 20);

            g2.setFont(new Font("Arial", Font.PLAIN, 11));
            g2.setColor(new Color(100, 100, 110));
            g2.drawString("Estructura: " + sesion.tipoEstructura
                    + "   |   Fecha: " + sesion.fecha, (int) x, (int) y + 38);

            // Linea separadora
            g2.setColor(new Color(180, 180, 200));
            g2.drawLine((int) x, (int) y + 46,
                    (int) (x + pageFormat.getImageableWidth()), (int) y + 46);

            // Contenido
            g2.setFont(new Font("Courier New", Font.PLAIN, 11));
            g2.setColor(Color.BLACK);

            int inicio = pageIndex * lineasPorPagina;
            int fin    = Math.min(inicio + lineasPorPagina, lineas.size());
            int yTexto = (int) y + 65;

            for (int i = inicio; i < fin; i++) {
                String linea = lineas.get(i);

                // Estilo diferente para encabezados de seccion
                if (linea.startsWith("===")) {
                    g2.setFont(new Font("Arial", Font.BOLD, 12));
                    g2.setColor(new Color(60, 60, 120));
                    g2.drawString(linea.replace("===", "").trim(), (int) x, yTexto);
                    g2.setFont(new Font("Courier New", Font.PLAIN, 11));
                    g2.setColor(Color.BLACK);
                } else if (linea.startsWith("---")) {
                    g2.setColor(new Color(180, 180, 200));
                    g2.drawLine((int) x, yTexto - 4,
                            (int) (x + pageFormat.getImageableWidth()), yTexto - 4);
                    g2.setColor(Color.BLACK);
                } else {
                    g2.drawString(linea, (int) x, yTexto);
                }
                yTexto += 15;
            }

            g2.setFont(new Font("Arial", Font.PLAIN, 9));
            g2.setColor(new Color(150, 150, 160));
            g2.drawString("Página " + (pageIndex + 1) + " de " + totalPaginas,
                    (int) (x + pageFormat.getImageableWidth() - 80),
                    (int) (y + pageFormat.getImageableHeight() - 5));

            return Printable.PAGE_EXISTS;
        }, pf);

        // Configurar destino: archivo PDF
        // Usa el servicio de impresion de Java para generar PDF
        javax.print.PrintService[] services = javax.print.PrintServiceLookup.lookupPrintServices(
                javax.print.DocFlavor.SERVICE_FORMATTED.PRINTABLE, null);

        try {
            javax.print.attribute.HashPrintRequestAttributeSet attrs =
                    new javax.print.attribute.HashPrintRequestAttributeSet();
            attrs.add(new javax.print.attribute.standard.Destination(archivo.toURI()));
            job.print(attrs);
        } catch (Exception e) {
            // Fallback: usar el dialogo de impresión del sistema
            // que en Windows/Mac permite "Guardar como PDF"
            job.print();
        }
    }

    /**
     * Abre la ventana de impresora del sistema y manda a imprimir.
     *
     * @param parent Componente padre
     * @param sesion Datos a imprimir
     */
    public static void imprimir(Component parent, SesionDatos sesion) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Ciencias2_" + sesion.tipoEstructura);

        List<String> lineas = generarLineasContenido(sesion);

        PageFormat pf = job.defaultPage();

        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            int lineasPorPagina = 45;
            int totalPaginas    = (int) Math.ceil((double) lineas.size() / lineasPorPagina);
            if (pageIndex >= totalPaginas) return Printable.NO_SUCH_PAGE;

            Graphics2D g2 = (Graphics2D) graphics;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            double x = pageFormat.getImageableX();
            double y = pageFormat.getImageableY();

            // Encabezado
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            g2.setColor(new Color(60, 60, 80));
            g2.drawString("Ciencias de la Computación 2", (int) x, (int) y + 20);

            g2.setFont(new Font("Arial", Font.PLAIN, 11));
            g2.setColor(new Color(100, 100, 110));
            g2.drawString("Estructura: " + sesion.tipoEstructura
                    + "   |   Fecha: " + sesion.fecha, (int) x, (int) y + 38);

            g2.setColor(new Color(180, 180, 200));
            g2.drawLine((int) x, (int) y + 46,
                    (int) (x + pageFormat.getImageableWidth()), (int) y + 46);

            g2.setFont(new Font("Courier New", Font.PLAIN, 11));
            g2.setColor(Color.BLACK);

            int inicio = pageIndex * lineasPorPagina;
            int fin    = Math.min(inicio + lineasPorPagina, lineas.size());
            int yTexto = (int) y + 65;

            for (int i = inicio; i < fin; i++) {
                String linea = lineas.get(i);
                if (linea.startsWith("===")) {
                    g2.setFont(new Font("Arial", Font.BOLD, 12));
                    g2.setColor(new Color(60, 60, 120));
                    g2.drawString(linea.replace("===", "").trim(), (int) x, yTexto);
                    g2.setFont(new Font("Courier New", Font.PLAIN, 11));
                    g2.setColor(Color.BLACK);
                } else if (linea.startsWith("---")) {
                    g2.setColor(new Color(180, 180, 200));
                    g2.drawLine((int) x, yTexto - 4,
                            (int) (x + pageFormat.getImageableWidth()), yTexto - 4);
                    g2.setColor(Color.BLACK);
                } else {
                    g2.drawString(linea, (int) x, yTexto);
                }
                yTexto += 15;
            }

            // Pie de pagina
            g2.setFont(new Font("Arial", Font.PLAIN, 9));
            g2.setColor(new Color(150, 150, 160));
            g2.drawString("Página " + (pageIndex + 1) + " de " + totalPaginas,
                    (int) (x + pageFormat.getImageableWidth() - 80),
                    (int) (y + pageFormat.getImageableHeight() - 5));

            return Printable.PAGE_EXISTS;
        }, pf);

        // Abrir impresora 
        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException e) {
                mostrarMensaje(parent, "Error al imprimir: " + e.getMessage(),
                        "Error de impresión", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Genera las lineas de texto que van en el PDF / impresion.
     */
    private static List<String> generarLineasContenido(SesionDatos sesion) {
        List<String> lineas = new ArrayList<>();

        lineas.add("=== " + sesion.tipoEstructura);
        lineas.add("Guardado el: " + sesion.fecha);
        lineas.add("---");
        lineas.add("");

        switch (sesion.tipoEstructura) {
            case "SECUENCIAL":
                lineas.add("Configuración:");
                lineas.add("  Tamaño máximo : " + sesion.tamaño);
                lineas.add("  Long. clave   : " + sesion.longClave);
                lineas.add("  Ordenado      : " + (sesion.ordenado ? "Sí" : "No"));
                lineas.add("");
                agregarTablaClaves(lineas, sesion.claves);
                break;

            case "BINARIO":
                lineas.add("Configuración:");
                lineas.add("  Tamaño máximo : " + sesion.tamaño);
                lineas.add("  Long. clave   : " + sesion.longClave);
                lineas.add("");
                agregarTablaClaves(lineas, sesion.claves);
                break;

            case "HASH":
                lineas.add("Configuración:");
                lineas.add("  Tamaño tabla  : " + sesion.tamaño);
                lineas.add("  Long. clave   : " + sesion.longClave);
                lineas.add("  Función hash  : " + sesion.funcionHash);
                lineas.add("  Tipo colisión : " + sesion.tipoColision);
                if ("PLEGAMIENTO".equals(sesion.funcionHash)) {
                    lineas.add("  Plegamiento   : " + sesion.tipoPlegamiento);
                }
                if ("TRUNCAMIENTO".equals(sesion.funcionHash) && sesion.posicionesTrunc != null) {
                    lineas.add("  Posiciones    : " + arrayToString(sesion.posicionesTrunc));
                }
                lineas.add("");
                agregarTablaHash(lineas, sesion);
                break;

            case "LISTAS_ENLAZADAS":
                lineas.add("Configuración:");
                lineas.add("  Tamaño tabla  : " + sesion.tamaño);
                lineas.add("  Long. clave   : " + sesion.longClave);
                lineas.add("");
                agregarTablaClaves(lineas, sesion.claves);
                break;

            case "ARREGLOS_ANIDADOS":
                lineas.add("Configuración:");
                lineas.add("  Tamaño arreglo: " + sesion.tamaño);
                lineas.add("  Long. clave   : " + sesion.longClave);
                lineas.add("");
                agregarTablaClaves(lineas, sesion.claves);
                break;

            case "ARBOL_DIGITAL":
            case "ARBOL_TRIES":
                lineas.add("Claves insertadas (en orden de inserción):");
                lineas.add("");
                lineas.add(String.format("  %-5s %-8s %-10s", "No.", "Clave", "Binario"));
                lineas.add("  " + "-".repeat(25));
                for (int i = 0; i < sesion.claves.size(); i++) {
                    String c   = sesion.claves.get(i);
                    String bin = claveABinario(c);
                    lineas.add(String.format("  %-5d %-8s %-10s", i + 1, c, bin));
                }
                break;

            case "ARBOL_MULTIPLE":
                lineas.add("Configuración:");
                lineas.add("  m (bits/nivel): " + sesion.m);
                lineas.add("  M (hijos/nodo): " + (int) Math.pow(2, sesion.m));
                lineas.add("");
                lineas.add("Claves insertadas:");
                lineas.add("");
                lineas.add(String.format("  %-5s %-8s %-10s", "No.", "Clave", "Binario"));
                lineas.add("  " + "-".repeat(25));
                for (int i = 0; i < sesion.claves.size(); i++) {
                    String c   = sesion.claves.get(i);
                    String bin = claveABinario(c);
                    lineas.add(String.format("  %-5d %-8s %-10s", i + 1, c, bin));
                }
                break;

            case "HUFFMAN":
                lineas.add("Texto original: " + sesion.textoHuffman);
                lineas.add("");
                // Reconstruir Huffman para mostrar tabla de códigos
                try {
                    ArbolHuffman h = new ArbolHuffman();
                    h.construir(sesion.textoHuffman);
                    lineas.add("=== Tabla de Códigos Huffman");
                    lineas.add(String.format("  %-8s %-12s %-10s", "Letra", "Frecuencia", "Código"));
                    lineas.add("  " + "-".repeat(32));
                    for (ArbolHuffman.CodigoHuffman c : h.getCodigos()) {
                        lineas.add(String.format("  %-8s %-12d %-10s",
                                c.letra, c.frecuencia, c.codigo));
                    }
                    lineas.add("");
                    lineas.add("Texto codificado:");
                    lineas.add("  " + h.codificarTexto());
                } catch (Exception e) {
                    lineas.add("Error al reconstruir Huffman: " + e.getMessage());
                }
                break;

            default:
                lineas.add("Estructura no reconocida: " + sesion.tipoEstructura);
        }

        lineas.add("");
        lineas.add("---");
        lineas.add("Total de claves: " + (sesion.claves != null ? sesion.claves.size() : "N/A"));

        return lineas;
    }

    private static void agregarTablaClaves(List<String> lineas, List<String> claves) {
        lineas.add("Datos almacenados:");
        lineas.add("");
        lineas.add(String.format("  %-6s %-15s", "Pos.", "Clave"));
        lineas.add("  " + "-".repeat(22));
        if (claves == null || claves.isEmpty()) {
            lineas.add("  (sin datos)");
        } else {
            for (int i = 0; i < claves.size(); i++) {
                lineas.add(String.format("  %-6d %-15s", i + 1, claves.get(i)));
            }
        }
    }

    private static void agregarTablaHash(List<String> lineas, SesionDatos sesion) {
        lineas.add("Tabla Hash:");
        lineas.add("");
        lineas.add(String.format("  %-8s %-15s", "Pos.", "Clave"));
        lineas.add("  " + "-".repeat(24));
        if (sesion.claves == null || sesion.claves.isEmpty()) {
            lineas.add("  (sin datos)");
        } else {
            for (String c : sesion.claves) {
                lineas.add(String.format("  %-8s %-15s", "", c));
            }
        }
        lineas.add("");
        lineas.add("  (Las posiciones exactas dependen del hash calculado al recuperar)");
    }

    private static String arrayToString(int[] arr) {
        if (arr == null) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.length; i++) {
            sb.append(arr[i]);
            if (i < arr.length - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }

    /** Convierte una letra a su representación binaria de 5 bits. */
    private static String claveABinario(String clave) {
        try {
            char c   = clave.toUpperCase().charAt(0);
            int  pos = c - 'A' + 1;
            return String.format("%5s", Integer.toBinaryString(pos)).replace(' ', '0');
        } catch (Exception e) {
            return "?????";
        }
    }

    /**
     * Crea una SesionDatos desde una instancia de Secuencial.
     */
    public static SesionDatos desdeCSecuencial(Secuencial s) {
        SesionDatos sd    = new SesionDatos("SECUENCIAL");
        sd.tamaño         = s.getTamaño();
        sd.longClave      = s.getLongClave();
        sd.ordenado       = s.isOrdenado();
        sd.claves         = new ArrayList<>(s.obtenerDatos());
        return sd;
    }

    /**
     * Crea una SesionDatos desde una instancia de Binario.
     */
    public static SesionDatos desdeBinario(Binario b) {
        SesionDatos sd = new SesionDatos("BINARIO");
        sd.tamaño      = b.getTamaño();
        sd.longClave   = b.getLongClave();
        sd.claves      = new ArrayList<>(b.obtenerDatos());
        return sd;
    }

    /**
     * Crea una SesionDatos desde una instancia de TablaHash.
     */
    public static SesionDatos desdeTablaHash(TablaHash th) {
        SesionDatos sd     = new SesionDatos("HASH");
        sd.tamaño          = th.getTamaño();
        sd.longClave       = th.getLongClave();
        sd.funcionHash     = th.getFuncionHash().name();
        sd.tipoColision    = th.getTipoColision().name();
        sd.claves          = new ArrayList<>(th.obtenerClavesActivas());
        return sd;
    }

    /**
     * Crea una SesionDatos desde una instancia de ListasEnlazadas.
     */
    public static SesionDatos desdeListasEnlazadas(ListasEnlazadas le) {
        SesionDatos sd = new SesionDatos("LISTAS_ENLAZADAS");
        sd.tamaño      = le.getTamaño();
        sd.longClave   = le.getLongClave();
        sd.claves      = new ArrayList<>(le.obtenerClavesActivas());
        return sd;
    }

    /**
     * Crea una SesionDatos desde una instancia de ArreglosAnidados.
     */
    public static SesionDatos desdeArreglosAnidados(ArreglosAnidados aa) {
        SesionDatos sd = new SesionDatos("ARREGLOS_ANIDADOS");
        sd.tamaño      = aa.getTamaño();
        sd.longClave   = aa.getLongClave();
        sd.claves      = new ArrayList<>(aa.obtenerClavesActivas());
        return sd;
    }

    /**
     * Crea una SesionDatos desde una instancia de ArbolDigital.
     */
    public static SesionDatos desdeArbolDigital(ArbolDigital ad) {
        SesionDatos sd = new SesionDatos("ARBOL_DIGITAL");
        sd.claves      = new ArrayList<>(ad.obtenerClaves());
        return sd;
    }

    /**
     * Crea una SesionDatos desde una instancia de ArbolTries.
     */
    public static SesionDatos desdeArbolTries(ArbolTries at) {
        SesionDatos sd = new SesionDatos("ARBOL_TRIES");
        sd.claves      = new ArrayList<>(at.obtenerClaves());
        return sd;
    }

    /**
     * Crea una SesionDatos desde una instancia de ArbolMultiple.
     */
    public static SesionDatos desdeArbolMultiple(ArbolMultiple am) {
        SesionDatos sd = new SesionDatos("ARBOL_MULTIPLE");
        sd.m           = am.getM();
        sd.claves      = new ArrayList<>(am.obtenerClaves());
        return sd;
    }

    /**
     * Crea una SesionDatos desde una instancia de ArbolHuffman.
     */
    public static SesionDatos desdeArbolHuffman(ArbolHuffman ah) {
        SesionDatos sd    = new SesionDatos("HUFFMAN");
        sd.textoHuffman   = ah.getTextoOriginal();
        return sd;
    }

    /**
     * Reconstruye un Secuencial desde una SesionDatos.
     */
    public static Secuencial restaurarSecuencial(SesionDatos sd) {
        Secuencial s = new Secuencial(sd.tamaño, sd.longClave, sd.ordenado);
        for (String c : sd.claves) s.insertar(c);
        return s;
    }

    /**
     * Reconstruye un Binario desde una SesionDatos.
     */
    public static Binario restaurarBinario(SesionDatos sd) {
        Binario b = new Binario(sd.tamaño, sd.longClave);
        for (String c : sd.claves) b.insertar(c);
        return b;
    }

    /**
     * Reconstruye un ArbolDigital desde una SesionDatos.
     */
    public static ArbolDigital restaurarArbolDigital(SesionDatos sd) {
        ArbolDigital ad = new ArbolDigital();
        for (String c : sd.claves) ad.insertar(c);
        return ad;
    }

    /**
     * Reconstruye un ArbolTries desde una SesionDatos.
     */
    public static ArbolTries restaurarArbolTries(SesionDatos sd) {
        ArbolTries at = new ArbolTries();
        for (String c : sd.claves) at.insertar(c);
        return at;
    }

    /**
     * Reconstruye un ArbolMultiple desde una SesionDatos.
     */
    public static ArbolMultiple restaurarArbolMultiple(SesionDatos sd) {
        ArbolMultiple am = new ArbolMultiple(sd.m);
        for (String c : sd.claves) am.insertar(c);
        return am;
    }

    /**
     * Reconstruye un ArbolHuffman desde una SesionDatos.
     */
    public static ArbolHuffman restaurarArbolHuffman(SesionDatos sd) {
        ArbolHuffman ah = new ArbolHuffman();
        ah.construir(sd.textoHuffman);
        return ah;
    }

    // Por si vas a usarlos o los necesitas
    /**
     * Crea un JFileChooser configurado con filtro y directorio inicial.
     */
    private static JFileChooser crearChooser(String titulo, FileNameExtensionFilter filtro) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(titulo);
        chooser.setFileFilter(filtro);
        chooser.setAcceptAllFileFilterUsed(false);
        // Abrir en el directorio del usuario
        chooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        return chooser;
    }

    private static void mostrarMensaje(Component parent, String msg, String titulo, int tipo) {
        JOptionPane.showMessageDialog(parent, msg, titulo, tipo);
    }
}