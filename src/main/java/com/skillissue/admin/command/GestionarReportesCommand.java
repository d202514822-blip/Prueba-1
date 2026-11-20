package com.skillissue.admin.command;

import com.skillissue.admin.gui.ReportAdminMenu;
import com.skillissue.admin.manager.GestorReportes;
import com.skillissue.admin.model.Reporte;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class GestionarReportesCommand extends BasicCommand implements CommandExecutor, TabCompleter {

    private final GestorReportes gestorReportes;

    public GestionarReportesCommand(GestorReportes gestorReportes) {
        super("gestionarreportes", "Panel administrativo para listar, filtrar y resolver reportes.");
        this.gestorReportes = gestorReportes;
    }

    @Override
    public void ejecutar() {
        // Ejecución gestionada mediante onCommand de Bukkit
    }

    private boolean tienePermisoAdmin(CommandSender sender) {
        return sender.isOp() || sender.hasPermission("skillissue.admin.report");
    }

    private String normalizarEstado(String raw) {
        if (raw == null) return null;
        return switch (raw.toLowerCase(Locale.ROOT)) {
            case "pendiente" -> "Pendiente";
            case "atendido" -> "Atendido";
            case "descartado" -> "Descartado";
            default -> null;
        };
    }

    private boolean esFechaValida(String fecha) {
        try {
            LocalDate.parse(fecha, DateTimeFormatter.ISO_LOCAL_DATE);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private void mostrarAyuda(CommandSender sender, String label) {
        sender.sendMessage(ChatColor.GOLD + "=== Gestión de Reportes (Admin) ===");
        sender.sendMessage(ChatColor.YELLOW + "/" + label + " listar [Pendiente|Atendido|Descartado]");
        sender.sendMessage(ChatColor.YELLOW + "/" + label + " ver <id>");
        sender.sendMessage(ChatColor.YELLOW + "/" + label + " filtrar <estado|*> <desde|*> <hasta|*>");
        sender.sendMessage(ChatColor.YELLOW + "/" + label + " resolver <id> <Atendido|Descartado> [nota]");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!tienePermisoAdmin(sender)) {
            sender.sendMessage(ChatColor.RED + "[SkillIssue] No tienes permiso para acceder a la gestión de reportes.");
            return true;
        }

        if (args.length == 0) {
            if (sender instanceof Player player) {
                player.openInventory(new ReportAdminMenu(gestorReportes, 0).getInventory());
            } else {
                mostrarAyuda(sender, label);
            }
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        switch (sub) {
            case "gui", "menu" -> {
                if (sender instanceof Player player) {
                    player.openInventory(new ReportAdminMenu(gestorReportes, 0).getInventory());
                } else {
                    sender.sendMessage(ChatColor.RED + "[SkillIssue] La interfaz GUI solo está disponible para jugadores.");
                }
            }
            case "listar", "list" -> manejarListar(sender, args);
            case "ver", "info" -> manejarVer(sender, label, args);
            case "filtrar", "filter" -> manejarFiltrar(sender, label, args);
            case "resolver", "resolve" -> manejarResolver(sender, label, args);
            default -> {
                sender.sendMessage(ChatColor.RED + "[SkillIssue] Subcomando desconocido: '" + args[0] + "'.");
                mostrarAyuda(sender, label);
            }
        }

        return true;
    }

    private void manejarListar(CommandSender sender, String[] args) {
        List<Reporte> lista;
        if (args.length >= 2) {
            String estado = normalizarEstado(args[1]);
            if (estado == null) {
                sender.sendMessage(ChatColor.RED + "[SkillIssue] Estado inválido. Usa: Pendiente, Atendido o Descartado.");
                return;
            }
            lista = gestorReportes.filtrarReportes(estado);
        } else {
            lista = gestorReportes.obtenerTodos();
        }

        if (lista.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "[SkillIssue] No hay reportes para mostrar con ese criterio.");
            return;
        }

        sender.sendMessage(ChatColor.GOLD + "--- Reportes encontrados (" + lista.size() + ") ---");
        for (Reporte r : lista) {
            ChatColor colorEstado = switch (r.getEstado()) {
                case "Atendido" -> ChatColor.GREEN;
                case "Descartado" -> ChatColor.GRAY;
                default -> ChatColor.RED;
            };
            sender.sendMessage(ChatColor.YELLOW + "#" + r.getId() + " " + colorEstado + "[" + r.getEstado() + "] " +
                    ChatColor.WHITE + r.getReportante() + ChatColor.GRAY + " -> " +
                    ChatColor.RED + r.getReportado() + ChatColor.WHITE + ": " + r.getMotivo() +
                    ChatColor.DARK_GRAY + " (" + r.getFecha() + ")");
        }
    }

    private void manejarVer(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Uso: /" + label + " ver <id>");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "[SkillIssue] El ID debe ser un número entero válido.");
            return;
        }

        if (id <= 0) {
            sender.sendMessage(ChatColor.RED + "[SkillIssue] El ID debe ser mayor que 0.");
            return;
        }

        Reporte r = gestorReportes.buscarPorId(id);
        if (r == null) {
            sender.sendMessage(ChatColor.RED + "[SkillIssue] No existe ningún reporte con ID #" + id + ".");
            return;
        }

        sender.sendMessage(ChatColor.GOLD + "=== Detalle del Reporte #" + r.getId() + " ===");
        sender.sendMessage(ChatColor.YELLOW + "Estado: " + ChatColor.WHITE + r.getEstado());
        sender.sendMessage(ChatColor.YELLOW + "Fecha y Hora: " + ChatColor.WHITE + r.getFecha() + " " + r.getHora());
        sender.sendMessage(ChatColor.YELLOW + "Reportante: " + ChatColor.WHITE + r.getReportante());
        sender.sendMessage(ChatColor.YELLOW + "Reportado: " + ChatColor.RED + r.getReportado());
        sender.sendMessage(ChatColor.YELLOW + "Motivo: " + ChatColor.WHITE + r.getMotivo());
        sender.sendMessage(ChatColor.YELLOW + "Admin Responsable: " + ChatColor.WHITE + r.getAdminResponsable());
        sender.sendMessage(ChatColor.YELLOW + "Nota Admin: " + ChatColor.WHITE + r.getComentarios());
    }

    private void manejarFiltrar(CommandSender sender, String label, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(ChatColor.RED + "Uso: /" + label + " filtrar <estado|*> <desde|*> <hasta|*>");
            sender.sendMessage(ChatColor.GRAY + "Ejemplo: /" + label + " filtrar Pendiente 2026-11-01 2026-11-30");
            return;
        }

        String estadoRaw = args[1];
        String estadoNormalizado = estadoRaw.equals("*") ? "*" : normalizarEstado(estadoRaw);
        if (estadoNormalizado == null) {
            sender.sendMessage(ChatColor.RED + "[SkillIssue] Estado inválido. Usa: Pendiente, Atendido, Descartado o *.");
            return;
        }

        String desde = args[2];
        String hasta = args[3];

        if (!desde.equals("*") && !esFechaValida(desde)) {
            sender.sendMessage(ChatColor.RED + "[SkillIssue] Fecha 'desde' inválida. Usa formato YYYY-MM-DD o *.");
            return;
        }

        if (!hasta.equals("*") && !esFechaValida(hasta)) {
            sender.sendMessage(ChatColor.RED + "[SkillIssue] Fecha 'hasta' inválida. Usa formato YYYY-MM-DD o *.");
            return;
        }

        if (!desde.equals("*") && !hasta.equals("*") && desde.compareTo(hasta) > 0) {
            sender.sendMessage(ChatColor.RED + "[SkillIssue] La fecha 'desde' no puede ser posterior a 'hasta'.");
            return;
        }

        GestorReportes.CriterioFiltro criterio = new GestorReportes.CriterioFiltro(estadoNormalizado, desde, hasta);
        List<Reporte> filtrados = gestorReportes.filtrarReportes(criterio);

        if (filtrados.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "[SkillIssue] Ningún reporte coincide con esos filtros.");
            return;
        }

        sender.sendMessage(ChatColor.GOLD + "--- Resultados filtrados (" + filtrados.size() + ") ---");
        for (Reporte r : filtrados) {
            sender.sendMessage(ChatColor.YELLOW + "#" + r.getId() + " [" + r.getEstado() + "] " +
                    ChatColor.WHITE + r.getReportante() + " -> " + r.getReportado() +
                    ChatColor.GRAY + " (" + r.getFecha() + "): " + ChatColor.WHITE + r.getMotivo());
        }
    }

    private void manejarResolver(CommandSender sender, String label, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Uso: /" + label + " resolver <id> <Atendido|Descartado> [nota]");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "[SkillIssue] El ID '" + args[1] + "' no es un número válido.");
            return;
        }

        if (id <= 0) {
            sender.sendMessage(ChatColor.RED + "[SkillIssue] El ID debe ser mayor a 0.");
            return;
        }

        String nuevoEstado = normalizarEstado(args[2]);
        if (nuevoEstado == null || nuevoEstado.equals("Pendiente")) {
            sender.sendMessage(ChatColor.RED + "[SkillIssue] Estado de resolución inválido. Usa: Atendido o Descartado.");
            return;
        }

        Reporte existente = gestorReportes.buscarPorId(id);
        if (existente == null) {
            sender.sendMessage(ChatColor.RED + "[SkillIssue] No se encontró ningún reporte con ID #" + id + ".");
            return;
        }

        if (!existente.getEstado().equalsIgnoreCase("Pendiente")) {
            sender.sendMessage(ChatColor.YELLOW + "[SkillIssue] El reporte #" + id + " ya estaba cerrado como '" +
                    existente.getEstado() + "' por " + existente.getAdminResponsable() + ".");
            return;
        }

        StringBuilder notaSb = new StringBuilder();
        for (int i = 3; i < args.length; i++) {
            if (!args[i].isBlank()) {
                if (!notaSb.isEmpty()) notaSb.append(" ");
                notaSb.append(args[i].trim());
            }
        }
        String nota = notaSb.isEmpty() ? "Sin comentarios" : notaSb.toString();

        gestorReportes.resolverReporte(id, nuevoEstado, sender.getName(), nota);

        sender.sendMessage(ChatColor.GREEN + "[SkillIssue] Reporte #" + id + " marcado como " +
                nuevoEstado + " por " + sender.getName() + ".");

        Player reportanteOnline = Bukkit.getPlayerExact(existente.getReportante());
        if (reportanteOnline != null) {
            reportanteOnline.sendMessage(ChatColor.AQUA + "[SkillIssue] Tu reporte #" + id + " contra " +
                    existente.getReportado() + " fue revisado (" + nuevoEstado + ").");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!tienePermisoAdmin(sender)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return filtrarPorPrefijo(Arrays.asList("gui", "listar", "ver", "filtrar", "resolver"), args[0]);
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        if (args.length == 2) {
            return switch (sub) {
                case "listar", "list" -> filtrarPorPrefijo(Arrays.asList("Pendiente", "Atendido", "Descartado"), args[1]);
                case "filtrar", "filter" -> filtrarPorPrefijo(Arrays.asList("*", "Pendiente", "Atendido", "Descartado"), args[1]);
                case "ver", "info", "resolver", "resolve" -> {
                    List<String> ids = new ArrayList<>();
                    for (Reporte r : gestorReportes.obtenerTodos()) {
                        ids.add(String.valueOf(r.getId()));
                    }
                    yield filtrarPorPrefijo(ids, args[1]);
                }
                default -> Collections.emptyList();
            };
        }

        if (args.length == 3) {
            return switch (sub) {
                case "resolver", "resolve" -> filtrarPorPrefijo(Arrays.asList("Atendido", "Descartado"), args[2]);
                case "filtrar", "filter" -> filtrarPorPrefijo(Arrays.asList("*", LocalDate.now().toString()), args[2]);
                default -> Collections.emptyList();
            };
        }

        if (args.length == 4 && (sub.equals("filtrar") || sub.equals("filter"))) {
            return filtrarPorPrefijo(Arrays.asList("*", LocalDate.now().toString()), args[3]);
        }

        return Collections.emptyList();
    }

    private List<String> filtrarPorPrefijo(List<String> opciones, String prefijo) {
        String p = prefijo.toLowerCase(Locale.ROOT);
        List<String> res = new ArrayList<>();
        for (String op : opciones) {
            if (op.toLowerCase(Locale.ROOT).startsWith(p)) {
                res.add(op);
            }
        }
        return res;
    }
}