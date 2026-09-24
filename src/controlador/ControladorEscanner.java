package controlador;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.regex.*;

import modelo.Dispositivos;

public class ControladorEscanner {
	private static final String OCTETO = "(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)";
 

	public boolean esSubredValida(String ip) {
		return ip.trim().matches(OCTETO + "\\." + OCTETO + "\\." + OCTETO);
	}
 
	public String validarDatos(String ip, String inicio, String fin, String tiempo) {
		if (!esSubredValida(ip)) {
			return "La IP de subred no es válida (ejemplo: 192.168.1)";
		}
		try {
			int ini = Integer.parseInt(inicio.trim());
			int f = Integer.parseInt(fin.trim());
			if (ini < 0 || ini > 255 || f < 0 || f > 255) {
				return "Desde y Hasta deben estar entre 0 y 255";
			}
			if (ini > f) {
				return "'Desde' no puede ser mayor que 'Hasta'";
			}
		} catch (NumberFormatException e) {
			return "Desde y Hasta deben ser números enteros";
		}
		try {
			int t = Integer.parseInt(tiempo.trim());
			if (t < 100 || t > 10000) {
				return "El tiempo máximo debe estar entre 100 y 10000 ms";
			}
		} catch (NumberFormatException e) {
			return "El tiempo máximo debe ser un número entero";
		}
		return null;
	}
	
	public Dispositivos escanearIP(String ip, int tiempoMax) {
		try {
			long tiempoRespuesta = hacerPing(ip, tiempoMax);
 
			if (tiempoRespuesta >= 0) {
				String nomDispositivo = obtenerNombre(ip);
				return new Dispositivos(ip, nomDispositivo, true, tiempoRespuesta);
			} else {
				return new Dispositivos(ip, "desconocido", false, 0);
			}
		} catch (IOException e) {
			return new Dispositivos(ip, "Hubo un error de red", false, 0);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return new Dispositivos(ip, "Escaneo interrumpido", false, 0);
		}
		
	}
	public List<Dispositivos> escanearRango(String ip, int inicioIp, int finIp, int tiempoMax, Consumer<Integer> progreso) {
			List<Dispositivos> lisDispositivos = new ArrayList<>();
			ExecutorService executor = Executors.newFixedThreadPool(20);
			AtomicInteger terminados = new AtomicInteger(0);
	 
			for (int i = inicioIp; i <= finIp; i++) {
				String ipPrueba = ip + "." + i;
				executor.submit(() -> {
					Dispositivos dev = escanearIP(ipPrueba, tiempoMax);
					
					synchronized (lisDispositivos) {
						lisDispositivos.add(dev);
					}
					progreso.accept(terminados.incrementAndGet());
				});
			}
		
		executor.shutdown();
		try {
			executor.awaitTermination(30, TimeUnit.SECONDS);
		}catch(InterruptedException e){
			Thread.currentThread().interrupt();
		}
		
		return lisDispositivos;
	}
	
	
	

	

private long hacerPing(String ip, int tiempoMax) throws IOException, InterruptedException {
	String so = System.getProperty("os.name").toLowerCase();
	ProcessBuilder pb;
	if (so.contains("win")) {
		
		pb = new ProcessBuilder("ping", "-n", "1", "-w", String.valueOf(tiempoMax), ip);
	} else if (so.contains("mac")) {
		pb = new ProcessBuilder("ping", "-c", "1", "-W", String.valueOf(tiempoMax), ip);
	} else {
		
		pb = new ProcessBuilder("ping", "-c", "1", "-W", String.valueOf(Math.max(1, tiempoMax / 1000)), ip);
	}
	pb.redirectErrorStream(true);

	long inicio = System.currentTimeMillis();
	String salida = leerSalida(pb.start());
	long medido = System.currentTimeMillis() - inicio;

	
	if (!salida.toLowerCase().contains("ttl=")) {
		return -1;
	}

	
	Matcher m = Pattern.compile("(?:tiempo|time)\\s*([=<])\\s*(\\d+)", Pattern.CASE_INSENSITIVE).matcher(salida);
	if (m.find()) {
		return m.group(1).equals("<") ? 1 : Long.parseLong(m.group(2));
	}
	return medido;
}

private String obtenerNombre(String ip) {
	try {
		ProcessBuilder pb = new ProcessBuilder("nslookup", ip);
		pb.redirectErrorStream(true);
		String salida = leerSalida(pb.start());

		for (String linea : salida.split("\\R")) {
			linea = linea.trim();
			String minuscula = linea.toLowerCase();
		
			if (minuscula.startsWith("name:") || minuscula.startsWith("nombre:")) {
				return linea.substring(linea.indexOf(':') + 1).trim();
			}
			
			int pos = minuscula.indexOf("name =");
			if (pos >= 0) {
				String nombre = linea.substring(pos + 6).trim();
				return nombre.endsWith(".") ? nombre.substring(0, nombre.length() - 1) : nombre;
			}
		}
		return "Nombre no encontrado";
	} catch (IOException | InterruptedException e) {
		if (e instanceof InterruptedException) {
			Thread.currentThread().interrupt();
		}
		return "Error al buscar el nombre";
	}
}


private String leerSalida(Process proceso) throws IOException, InterruptedException {
	StringBuilder salida = new StringBuilder();
	try (BufferedReader lector = new BufferedReader(new InputStreamReader(proceso.getInputStream()))) {
		String linea;
		while ((linea = lector.readLine()) != null) {
			salida.append(linea).append("\n");
		}
	}
	proceso.waitFor();
	return salida.toString();
}


public void guardarResultados(List<Dispositivos> lista, File archivo) throws IOException {
	try (PrintWriter pw = new PrintWriter(new FileWriter(archivo))) {
		pw.println("IP;Nombre;Estado;Tiempo (ms)");
		for (Dispositivos d : lista) {
			pw.println(d.getIp() + ";" + d.getNombre() + ";"
					+ (d.getConectado() ? "Activo" : "Inactivo") + ";"
					+ (d.getConectado() ? String.valueOf(d.getTiempoRespuesta()) : ""));
		}
	}
}
}

	
	


