package vista;


import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List; 
import java.util.regex.Pattern;
 
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import javax.swing.table.*;
 
import controlador.ControladorEscanner;
import modelo.Dispositivos;
 
public class VistaPrincipal extends JFrame {
	private JTextField txtIp;
	private JTextField txtInicioIp;
	private JTextField txtFinIp;
	private JTextField txtTiempoMax;
	private JTextField txtFiltro;
	private JComboBox<String> cmbEstado;
	private JButton btnEscanear;
	private JButton btnLimpiar;
	private JButton btnGuardar;
	private JLabel lblMensaje;
	private JLabel lblTotal;
    private JTable tablaResultados;
    private DefaultTableModel modeloTabla;
    private TableRowSorter<DefaultTableModel> sorter;
    private JProgressBar barraProgreso;
 
    private ControladorEscanner controlador;
    private List<Dispositivos> resultados = new ArrayList<>();
    private boolean escaneando = false;
 
    public VistaPrincipal() {
    	controlador = new ControladorEscanner();
 
    	setTitle("Escáner de Red LAN - TP Redes");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
 
        // Fila 1: datos del rango
        JPanel jpFormulario = new JPanel(new FlowLayout(FlowLayout.LEFT,10,10));
 
        jpFormulario.add(new JLabel("IP Subred:"));
        txtIp = new JTextField("192.168.1", 8);
        jpFormulario.add(txtIp);
 
        jpFormulario.add(new JLabel("Desde:"));
        txtInicioIp = new JTextField("1", 3);
        jpFormulario.add(txtInicioIp);
 
        jpFormulario.add(new JLabel("Hasta:"));
        txtFinIp = new JTextField("20", 3);
        jpFormulario.add(txtFinIp);
 
        jpFormulario.add(new JLabel("Tiempo Máx (ms):"));
        txtTiempoMax = new JTextField("1000", 4);
        jpFormulario.add(txtTiempoMax);
 
        
        JPanel jpBotones = new JPanel(new FlowLayout(FlowLayout.LEFT,10,5));
 
        btnEscanear = new JButton("Escanear");
        jpBotones.add(btnEscanear);
 
        btnLimpiar = new JButton("Limpiar");
        jpBotones.add(btnLimpiar);
 
        btnGuardar = new JButton("Guardar resultados");
        jpBotones.add(btnGuardar);
 
        jpBotones.add(new JLabel("Filtrar (IP o nombre):"));
        txtFiltro = new JTextField(10);
        jpBotones.add(txtFiltro);
 
        cmbEstado = new JComboBox<>(new String[] {"Todos", "Activos", "Inactivos"});
        jpBotones.add(cmbEstado);
 
        
        lblMensaje = new JLabel(" ");
        lblMensaje.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
 
        JPanel jpNorte = new JPanel(new GridLayout(3, 1));
        jpNorte.add(jpFormulario);
        jpNorte.add(jpBotones);
        jpNorte.add(lblMensaje);
        add(jpNorte, BorderLayout.NORTH);
 
 
        String[] columnas = {"IP","Nombre Dispositivo","Estado","Tiempo Respuesta (ms)"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
        	@Override
        	public boolean isCellEditable(int fila, int columna) {
        		return false; 
        	}
        	@Override
        	public Class<?> getColumnClass(int columna) {
        	
        		return columna == 3 ? Long.class : String.class;
        	}
        };
        tablaResultados = new JTable(modeloTabla);
 
      
        sorter = new TableRowSorter<>(modeloTabla);
        sorter.setComparator(0, (a, b) -> compararIp((String) a, (String) b));
        tablaResultados.setRowSorter(sorter);
 
        JScrollPane scrollTabla = new JScrollPane(tablaResultados);
        add(scrollTabla, BorderLayout.CENTER);
 
 
        JPanel panelInferior = new JPanel(new BorderLayout(5, 5));
        barraProgreso = new JProgressBar();
        barraProgreso.setStringPainted(true);
        panelInferior.add(barraProgreso, BorderLayout.CENTER);
 
        lblTotal = new JLabel("Equipos que respondieron: 0");
        panelInferior.add(lblTotal, BorderLayout.SOUTH);
 
        add(panelInferior, BorderLayout.SOUTH);
 
 
        DocumentListener validar = alCambiar(this::validarCampos);
        txtIp.getDocument().addDocumentListener(validar);
        txtInicioIp.getDocument().addDocumentListener(validar);
        txtFinIp.getDocument().addDocumentListener(validar);
        txtTiempoMax.getDocument().addDocumentListener(validar);

        txtFiltro.getDocument().addDocumentListener(alCambiar(this::filtrar));
        cmbEstado.addActionListener(e -> filtrar());
 
        btnEscanear.addActionListener(e -> escanear());
        btnLimpiar.addActionListener(e -> limpiar());
        btnGuardar.addActionListener(e -> guardar());
 
        validarCampos();
    }
 
    private DocumentListener alCambiar(Runnable accion) {
    	return new DocumentListener() {
    		@Override
    		public void insertUpdate(DocumentEvent e) { accion.run(); }
    		@Override
    		public void removeUpdate(DocumentEvent e) { accion.run(); }
    		@Override
    		public void changedUpdate(DocumentEvent e) { accion.run(); }
    	};
    }
 
    
    private void validarCampos() {
    	String error = controlador.validarDatos(txtIp.getText(), txtInicioIp.getText(),
    			txtFinIp.getText(), txtTiempoMax.getText());
 
    	boolean subredMal = !txtIp.getText().isEmpty() && !controlador.esSubredValida(txtIp.getText());
    	txtIp.setBackground(subredMal ? new Color(255, 200, 200) : Color.WHITE);
 
    	if (error == null) {
    		lblMensaje.setForeground(new Color(0, 120, 0));
    		lblMensaje.setText("Datos correctos. Listo para escanear.");
    	} else {
    		lblMensaje.setForeground(Color.RED);
    		lblMensaje.setText(error);
    	}
    	btnEscanear.setEnabled(error == null && !escaneando);
    }
 
    private void escanear() {
    	String ip = txtIp.getText().trim();
    	int inicio = Integer.parseInt(txtInicioIp.getText().trim());
    	int fin = Integer.parseInt(txtFinIp.getText().trim());
    	int tiempo = Integer.parseInt(txtTiempoMax.getText().trim());
 
    	limpiar();
    	escaneando = true;
    	btnEscanear.setEnabled(false);
    	btnLimpiar.setEnabled(false);
    	btnGuardar.setEnabled(false);
    	lblMensaje.setForeground(Color.DARK_GRAY);
    	lblMensaje.setText("Escaneando...");
    	barraProgreso.setMaximum(fin - inicio + 1);
 
    	
    	SwingWorker<List<Dispositivos>, Integer> worker = new SwingWorker<List<Dispositivos>, Integer>() {
    		@Override
    		protected List<Dispositivos> doInBackground() {
    			return controlador.escanearRango(ip, inicio, fin, tiempo, terminados -> publish(terminados));
    		}
 
    		@Override
    		protected void process(List<Integer> avances) {
    			int ultimo = avances.get(avances.size() - 1);
    			barraProgreso.setValue(ultimo);
    			barraProgreso.setString(ultimo + " / " + barraProgreso.getMaximum());
    		}
 
    		@Override
    		protected void done() {
    			try {
    				resultados = get();
    				mostrarResultados();
    				lblMensaje.setText("Escaneo finalizado.");
    			} catch (Exception ex) {
    				lblMensaje.setForeground(Color.RED);
    				lblMensaje.setText("Ocurrió un error durante el escaneo.");
    				JOptionPane.showMessageDialog(VistaPrincipal.this,
    						"Ocurrió un error durante el escaneo:\n" + ex.getMessage(),
    						"Error", JOptionPane.ERROR_MESSAGE);
    			}
    			escaneando = false;
    			btnLimpiar.setEnabled(true);
    			btnGuardar.setEnabled(true);
    			validarCampos();
    		}
    	};
    	worker.execute();
    }
 
    private void mostrarResultados() {
    	resultados.sort((a, b) -> compararIp(a.getIp(), b.getIp()));
    	int activos = 0;
    	for (Dispositivos d : resultados) {
    		modeloTabla.addRow(new Object[] {
    				d.getIp(),
    				d.getNombre(),
    				d.getConectado() ? "Activo" : "Inactivo",
    				d.getConectado() ? Long.valueOf(d.getTiempoRespuesta()) : null });
    		if (d.getConectado()) {
    			activos++;
    		}
    	}
    	lblTotal.setText("Equipos que respondieron: " + activos + " de " + resultados.size());
    }
 
    private void limpiar() {
    	modeloTabla.setRowCount(0);
    	resultados = new ArrayList<>();
    	barraProgreso.setValue(0);
    	barraProgreso.setString(null);
    	lblTotal.setText("Equipos que respondieron: 0");
    }
 
    private void guardar() {
    	if (resultados.isEmpty()) {
    		JOptionPane.showMessageDialog(this, "Todavía no hay resultados para guardar.",
    				"Sin resultados", JOptionPane.INFORMATION_MESSAGE);
    		return;
    	}
    	JFileChooser selector = new JFileChooser();
    	selector.setSelectedFile(new File("resultados_escaneo.csv"));
    	selector.setFileFilter(new FileNameExtensionFilter("Archivo CSV (*.csv)", "csv"));
    	if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
    		return;
    	}
    	File archivo = selector.getSelectedFile();
    	if (!archivo.getName().toLowerCase().endsWith(".csv")) {
    		archivo = new File(archivo.getParentFile(), archivo.getName() + ".csv");
    	}
    	try {
    		controlador.guardarResultados(resultados, archivo);
    		JOptionPane.showMessageDialog(this, "Resultados guardados en:\n" + archivo.getAbsolutePath(),
    				"Guardado", JOptionPane.INFORMATION_MESSAGE);
    	} catch (IOException ex) {
    		JOptionPane.showMessageDialog(this, "No se pudo guardar el archivo:\n" + ex.getMessage(),
    				"Error", JOptionPane.ERROR_MESSAGE);
    	}
    }
    private void filtrar() {
    	List<RowFilter<Object, Object>> filtros = new ArrayList<>();
    	String texto = txtFiltro.getText().trim();
    	if (!texto.isEmpty()) {
    		filtros.add(RowFilter.regexFilter("(?i)" + Pattern.quote(texto), 0, 1));
    	}
    	if (cmbEstado.getSelectedIndex() == 1) {
    		filtros.add(RowFilter.regexFilter("^Activo$", 2));
    	} else if (cmbEstado.getSelectedIndex() == 2) {
    		filtros.add(RowFilter.regexFilter("^Inactivo$", 2));
    	}
 
    	if (filtros.isEmpty()) {
    		sorter.setRowFilter(null);
    	} else {
    		sorter.setRowFilter(RowFilter.andFilter(filtros));
    	}
    }
 
    private int compararIp(String a, String b) {
    	String[] pa = a.split("\\.");
    	String[] pb = b.split("\\.");
    	for (int i = 0; i < 4; i++) {
    		int c = Integer.compare(Integer.parseInt(pa[i]), Integer.parseInt(pb[i]));
    		if (c != 0) {
    			return c;
    		}
    	}
    	return 0;
    }
 
}
