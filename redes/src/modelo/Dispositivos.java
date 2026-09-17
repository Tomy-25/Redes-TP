package modelo;

public class Dispositivos {
	private String ip;
	private String nombre;
	private boolean conectado;
	private long tiempoRespuesta;
	
	
	
	
	
	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public boolean getConectado() {
		return conectado;
	}
	public void setConectado(boolean conectado) {
		this.conectado = conectado;
	}
	public long getTiempoRespuesta() {
		return tiempoRespuesta;
	}

	public void setTiempoRespuesta(long tiempoRespuesta) {
		this.tiempoRespuesta = tiempoRespuesta;
	}



	@Override
	public String toString() {
		return 
		"IP='" + ip + '\'' +
        ", Nombre='" + nombre + '\'' +
        ", Estado=" + (conectado ? "Activo" : "Inactivo") +
        ", Tiempo=" + tiempoRespuesta + " ms" +
        '}';
	}



	public Dispositivos(String ip, String nombre, boolean conectado, long tiempoRespuesta) {
		super();
		this.ip = ip;
		this.nombre = nombre;
		this.conectado = conectado;
		this.tiempoRespuesta = tiempoRespuesta;
	}
	
	
	
	

}


