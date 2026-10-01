Escáner de Red LAN

Aplicación de escritorio en Java (Swing) que escanea un rango de direcciones IP de una red local, indica qué equipos están activos y muestra su nombre, usando los comandos ping y nslookup del sistema operativo.

Funcionalidades
Escaneo de un rango de IP dentro de una misma subred (ej. 192.168.1.1 a 192.168.1.254).
Verificación de conectividad con el comando ping del sistema.
Resolución del nombre de cada equipo con nslookup.
Validación de los datos ingresados mientras se escriben (subred, rango, tiempo de espera).
Escaneo en segundo plano con barra de progreso (la ventana no se congela).
Tabla de resultados ordenable (clic en los encabezados) y filtrable por IP/nombre y por estado.
Contador de equipos que respondieron.
Exportación de los resultados a un archivo CSV.
Manejo de errores: IP inválidas, ping no disponible, fallas de nslookup.
Requisitos
JDK 11 o superior instalado y configurado (java -version y javac -version deben funcionar desde la terminal).
Eclipse IDE (para desarrollo) u otro entorno que compile Java estándar.
Sistema operativo Windows, Linux o macOS, con los comandos ping y nslookup disponibles (vienen instalados por defecto en los tres).
Conexión a una red local (Wi-Fi o cable) para que el escaneo tenga sentido.

El programa solo usa las bibliotecas estándar de Java (javax.swing, java.io, java.util.concurrent, etc.). No depende de ninguna librería externa.

Estructura del proyecto

El código sigue el patrón Modelo-Vista-Controlador (MVC):

escaner-red/
└── src/
    ├── principal/
    │   └── Main.java                  → Punto de entrada del programa
    ├── modelo/
    │   └── Dispositivos.java          → Datos de un equipo escaneado
    ├── controlador/
    │   └── ControladorEscanner.java   → Validación, ping, nslookup y guardado
    └── vista/
        └── VistaPrincipal.java        → Ventana e interacción con el usuario

modelo: representa los datos (un equipo encontrado en la red). No sabe nada de la interfaz gráfica ni de cómo se obtienen los datos.
controlador: contiene toda la lógica: valida los campos, ejecuta ping y nslookup, arma la lista de equipos y guarda el archivo CSV. No conoce ningún componente de Swing.
vista: es la ventana (JFrame). Solo se encarga de mostrar datos y de reaccionar a lo que hace el usuario (botones, tabla, filtros); delega todo el trabajo al controlador.
Cómo ejecutarlo en Eclipse
Abrir Eclipse y elegir File → Open Projects from File System..., apuntando a la carpeta escaner-red (o crear un proyecto Java nuevo e importar la carpeta src).
Verificar que el proyecto use un JRE/JDK 11 o superior (clic derecho sobre el proyecto → Properties → Java Build Path → Libraries).
Abrir src/principal/Main.java.
Ejecutarlo con Run → Run As → Java Application (o el botón ▶ con Main.java seleccionado).
Cómo usarlo
En IP Subred escribir los primeros tres octetos de la red a analizar (ej. 192.168.1).
En Desde y Hasta indicar el rango del último octeto a escanear (ej. 1 a 254).
En Tiempo Máx (ms) indicar cuánto esperar la respuesta de cada ping (entre 100 y 10000 ms; 1000 ms es un buen valor por defecto).
Presionar Escanear. La barra de progreso muestra el avance; al terminar se completa la tabla y el contador de equipos que respondieron.
Usar el campo Filtrar y el combo Estado para buscar un equipo puntual o ver solo los activos/inactivos. Se puede ordenar la tabla haciendo clic en cualquier encabezado de columna.
Presionar Guardar resultados para exportar la tabla a un archivo .csv (se puede abrir con Excel).
Presionar Limpiar para vaciar la tabla y empezar un nuevo escaneo.
Notas
Rangos muy grandes (por ejemplo, 1 a 254) pueden tardar varios minutos según la cantidad de equipos y el tiempo de espera configurado.
Si ping o nslookup no están disponibles en el sistema, el programa lo informa en la tabla en vez de cerrarse con un error

Autor "Tomy" - Tomas Tepero 5°1° ET36