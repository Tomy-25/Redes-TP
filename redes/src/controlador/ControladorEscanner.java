package controlador;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.*;

import modelo.Dispositivos;

public class ControladorEscanner {
	
	public Dispositivos escanearIP(String ip, int tiempoMax) {
		try	{
			InetAddress direccion = InetAddress.getByName(ip);
			long inicio = System.currentTimeMillis();
			boolean isActivo = direccion.isReachable(tiempoMax);
            long fin = System.currentTimeMillis();
            long tiempoRespuesta = fin - inicio;

            if (isActivo) {
                String nomDispositivo = direccion.getHostName();
                return new Dispositivos(ip, nomDispositivo, true, tiempoRespuesta);
            } else {
                return new Dispositivos(ip, "desconocido", false, 0);
            }
        } catch (IOException e) {
            return new Dispositivos(ip, "Hubo un error de red", false, 0);
        }
	}
	public List<Dispositivos> escanearRango(String ip, int inicioIp, int finIp, int tiempoMax){
		List<Dispositivos> lisDispositivos = new ArrayList<>();
		ExecutorService executor = Executors.newFixedThreadPool(20);
		
		for(int i = inicioIp ;i <= finIp ; i++ ) {
			String ipPrueba = ip + "." + i;
			executor.submit(() -> {
			});
		}
		
		return lisDispositivos;
	}
	
	
	
}
	

	
	


