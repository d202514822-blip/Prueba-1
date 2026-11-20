# PLAN DE TRABAJO Y ASIGNACIÓN DE MÓDULOS - PLUGIN SKILL ISSUE ADMIN

Este documento contiene la repartición formal del código Java y la arquitectura de integración para el plugin de Minecraft "Skill Issue Admin" (Spigot / PaperMC - Java 17). 

Cada integrante debe programar de forma independiente las clases y métodos asignados para que el día jueves podamos ensamblar todo el proyecto sin conflictos de compilación.

---

## 🏗️ VISIÓN GENERAL DE ARQUITECTURA E INTEGRACIÓN DE PERSISTENCIA

Para cumplir con la corrección del profesor, la persistencia en archivos planos (`IPersistencia` -> `PersistenciaArchivo`) queda **completamente integrada** en el flujo de negocio:
1. La interfaz `IPersistencia` define los contratos universales de lectura/escritura (`guardarDatos`, `cargarDatos`, `verificarArchivo`).
2. `PersistenciaArchivo` implementa esta interfaz operando sobre archivos `.txt` en la carpeta del plugin (`jugadores.txt`, `sanciones.txt`, `reportes.txt`, `rangos.txt`).
3. Cada **Gestor** (`GestorJugadores`, `GestorSanciones`, `GestorReportes`) posee una referencia a `IPersistencia` y llama automáticamente a la carga de datos en su constructor y al guardado tras cada modificación (creación de reporte, aplicación de sanción, cambio de rango, etc.).

---

## 👥 REPARTICIÓN DE TAREAS POR INTEGRANTE

---

### 1. DAVID SANTIAGO DÍAZ
**Módulo:** Sistema de Reportes (Comandos y Gestión de Interfaz)
**Clases a su cargo:**
* `GestorReportes.java`
* `ReportCommand.java` (extiende `BasicCommand` / implementa `CommandExecutor`)
* `CriterioFiltro.java` (clase anidada dentro de `GestorReportes`)

**Responsabilidades y Métodos a Implementar:**
* **`GestorReportes`:**
  * Atributos: `ArrayList<Reporte> reportes`, `IPersistencia persistencia`.
  * `crearReporte(Jugador reportante, Jugador reportado, String motivo)`: Crea el objeto `Reporte`, lo añade a la lista y llama a `persistencia.guardarDatos(...)`.
  * `resolverReporte(int id, String estado)`: Cambia el estado del reporte ("Atendido", "Descartado") y actualiza la persistencia.
  * `filtrarReportes(String estado)` y `filtrarReportes(CriterioFiltro criterio)`: Sobrecarga de métodos para búsquedas específicas.
* **`CriterioFiltro` (Clase Anidada / Nested):**
  * Contiene `estadoBuscado`, `fechaDesde`, `fechaHasta` y el método `coincide(Reporte r)`.
* **`ReportCommand`:**
  * Comando `/report <jugador> <motivo>`.
  * Captura la ejecución desde Minecraft, valida que el jugador no se autoreporte, invoca `gestorReportes.crearReporte(...)` y envía mensajes de confirmación con formato al jugador.

---

### 2. JUAN FELIPE CAMACHO LANDAZÁBAL
**Módulo:** Capa de Comandos Base y Administración (`BasicCommand`, `BanCommand`, `RankCommand`)
**Clases a su cargo:**
* `BasicCommand.java` (Clase Base Abstracta para comandos)
* `BanCommand.java`
* `RankCommand.java`

**Responsabilidades y Métodos a Implementar:**
* **`BasicCommand` (Clase Abstracta):**
  * Implementa `CommandExecutor` de Spigot.
  * Atributos protegidos: `String nombreComando`, `String descripcion`.
  * Método abstracto `public abstract boolean ejecutarComando(CommandSender sender, String[] args)`.
  * Método `onCommand(...)` que realiza validaciones comunes (ej: verificar si el emisor es un jugador o consola) y delega en `ejecutarComando`.
* **`BanCommand`:**
  * Comando `/ban <jugador> <duracionDias/permanente> <motivo>`.
  * Valida permisos de staff (`admin.hasPermission("skillissue.admin")`), instancia un objeto `Baneo` y llama a `gestorSanciones.aplicarSancion(...)`.
* **`RankCommand`:**
  * Comando `/rank <set|create|remove> <jugador/rango>`.
  * Permite asignar rangos a jugadores o crear nuevos rangos en el servidor.

---

### 3. SAMUEL ZONA
**Módulo:** Entidades de Dominio de Jugadores y Rangos (`Jugador`, `Rango`, `ProfileCommand`)
**Clases a su cargo:**
* `Jugador.java`
* `Rango.java`
* `ProfileCommand.java`

**Responsabilidades y Métodos a Implementar:**
* **`Jugador`:**
  * Atributos: `id`, `nombre`, `skin`, `nivel`, `monedas`, `horasJugadas`, `fechaIngreso`, `Rango rangoActual`, `ArrayList<Sancion> historialSanciones`.
  * Constantes estáticas: `public static final int NIVEL_MIN_MIEMBRO = 10`, `public static final int MONEDAS_MIN_MIEMBRO = 500`.
  * Métodos: `registrarHoras(int horas)`, `asignarRango(Rango nuevoRango)`, `subirRangoGratuito()`, `agregarSancion(Sancion s)`, `tienePermiso(String permiso)`.
* **`Rango`:**
  * Atributos: `String nombre`, `int nivelPermiso`, `ArrayList<String> permisos`, `boolean esPago`.
  * Métodos: `agregarPermiso(String permiso)`, `tienePermiso(String permiso)`.
* **`ProfileCommand`:**
  * Comando `/profile [jugador]`. Muestra un menú/resumen en el chat de Minecraft con el rango, nivel, horas jugadas y estado de sanciones del jugador.

---

### 4. SAMUEL GACHARNA
**Módulo:** Jerarquía de Sanciones y Lógica de Moderación (`Sancion`, `Baneo`, `Mute`, `Advertido`, `GestorSanciones`)
**Clases a su cargo:**
* `Sancion.java` (Clase Abstracta)
* `Baneo.java` (Extiende `Sancion`)
* `Mute.java` (Extiende `Sancion`)
* `Advertido.java` (Extiende `Sancion`)
* `GestorSanciones.java`

**Responsabilidades y Métodos a Implementar:**
* **`Sancion` (Abstracta):**
  * Atributos: `int id`, `String motivo`, `String fecha`, `String adminResponsable`, `boolean activa`.
  * Atributo estático: `private static int contadorSanciones`. Método estático: `public static int getSiguienteId()`.
  * Método abstracto: `public abstract boolean aplicarEfecto(Jugador j)`.
  * Método: `revocar(String motivoPerdon)`.
* **Subclases Polimórficas:**
  * `Baneo`: Implementa `aplicarEfecto(j)` expulsando al jugador del servidor (`player.kickPlayer(...)`).
  * `Mute`: Implementa `aplicarEfecto(j)` marcando al jugador como silenciado.
  * `Advertido`: Implementa `aplicarEfecto(j)` enviando una advertencia sonora y visual en pantalla.
* **`GestorSanciones`:**
  * Atributos: `ArrayList<Sancion> sancionesActivas`, `IPersistencia persistencia`.
  * Métodos: `aplicarSancion(Jugador j, Sancion s)`, `levantarSancion(int idSancion)`, `obtenerSancionesPorJugador(String id)`, `calcularReincidencia(Jugador j)`.

---

### 5. JUAN BELLO
**Módulo:** Capa de Persistencia e Integración de Archivos Planos (`IPersistencia`, `PersistenciaArchivo`, `GestorJugadores`)
**Clases a su cargo:**
* `IPersistencia.java` (Interfaz)
* `PersistenciaArchivo.java` (Implementa `IPersistencia`)
* `GestorJugadores.java`

**Responsabilidades y Métodos a Implementar:**
* **`IPersistencia` (Interfaz):**
  * Contrato de persistencia: `guardarDatos(String nombreArchivo, List<?> datos)`, `cargarDatos(String nombreArchivo)`, `verificarArchivo(String nombreArchivo)`.
* **`PersistenciaArchivo`:**
  * Lee y escribe en archivos de texto plano (`.txt`) en formato CSV/Separado por comas o punto y coma (ej: `jugadores.txt`, `sanciones.txt`, `reportes.txt`).
  * Maneja internamente los flujos `BufferedReader`, `PrintWriter`, `File` y captura de `IOException`.
* **`GestorJugadores`:**
  * Atributos: `ArrayList<Jugador> jugadores`, `IPersistencia persistencia`.
  * Métodos: `registrarJugador(Jugador j)`, `buscarJugador(String id)`, `buscarJugador(String nombre, boolean exacto)` (Sobrecarga), `eliminarJugador(String id)`, `actualizarHoras(String id, int horas)`.
  * Método estático: `public static boolean validarConexionGlobal(String ip)`.

---

### 6. MIGUEL PEÑA
**Módulo:** Excepciones Personalizadas, Listeners de Eventos en Spigot y Transacciones (`Donacion`, `CompraRango`)
**Clases a su cargo:**
* `JugadorNoEncontradoException.java` (Extiende `Exception`)
* `SancionInvalidaException.java` (Extiende `Exception`)
* `ModeracionListener.java` (Implementa `Listener` de Spigot)
* `Donacion.java` y `CompraRango.java`

**Responsabilidades y Métodos a Implementar:**
* **Excepciones Personalizadas:**
  * Constructores con mensajes descriptivos para errores de negocio (`throw new JugadorNoEncontradoException("El jugador no está registrado");`).
* **`ModeracionListener` (Eventos en Tiempo Real de Minecraft):**
  * `@EventHandler public void onAsyncPlayerChat(AsyncPlayerChatEvent e)`: Si el jugador está Muteado, cancela el evento (`e.setCancelled(true)`) y le avisa que está silenciado.
  * `@EventHandler public void onPlayerLogin(PlayerLoginEvent e)`: Si el jugador tiene un Baneo activo, deniega el ingreso.
* **`Donacion` y `CompraRango`:**
  * Modelan las transacciones del servidor, validación de montos y entrega automática de rangos pagos.

---

## 📌 GUÍA DE ENSAMBLE PARA MAÑANA (JUEVES)

1. Cada integrante debe subir su código en su propia rama o pasarlo limpio en archivos `.java`.
2. En la clase principal del Plugin (`SkillIssueAdminPlugin.java` que extiende `JavaPlugin`):
   * En `onEnable()`: Instanciar `PersistenciaArchivo`, crear los 3 Gestores, registrar los Listeners en el `PluginManager` y registrar los Comandos (`getCommand("report").setExecutor(...)`).
   * En `onDisable()`: Llamar a los métodos de guardado final de cada gestor para garantizar que ningún dato se pierda.
