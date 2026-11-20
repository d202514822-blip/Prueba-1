package com.skillissue.admin.command;

import com.skillissue.admin.exception.JugadorNoEncontradoException;
import com.skillissue.admin.gui.ReportJugadorMenu;
import com.skillissue.admin.gui.ReportMotivoMenu;
import com.skillissue.admin.manager.GestorJugadores;
import com.skillissue.admin.manager.GestorReportes;
import com.skillissue.admin.model.Reporte;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class ReportCommand extends BasicCommand implements CommandExecutor, TabCompleter {

    private static final long COOLDOWN_BASE_SEGUNDOS = 30L;
    private static final int LONGITUD_MIN_MOTIVO = 4;
    private static final int LONGITUD_MAX_MOTIVO = 150;
    private static final Set<String> PALABRAS_RESERVADAS = Set.of(
            "list", "listar", "filter", "filtrar", "resolve", "resolver", "ver", "info", "help", "ayuda"
    );

    private final GestorReportes gestorReportes;
    private final GestorJugadores gestorJugadores;
    private final Map<UUID, Long> ultimosReportes = new HashMap<>();

    public ReportCommand(GestorReportes gestorReportes, GestorJugadores gestorJugadores) {
        super("report", "Reporta a un jugador por incumplir las reglas.");
        this.gestorReportes = gestorReportes;
        this.gestorJugadores = gestorJugadores;
    }

    @Override
    public void ejecutar() {
        // Ejecución gestionada mediante onCommand de Bukkit
    }

    private long coolDown(int cantidadReportes) {
        int factor = Math.max(1, Math.min(cantidadReportes, 4));
        return COOLDOWN_BASE_SEGUNDOS * factor;
    }

    private String validarJugadorExistente(String nombreIngresado) throws JugadorNoEncontradoException {
        if (nombreIngresado == null || nombreIngresado.isBlank()
                || PALABRAS_RESERVADAS.contains(nombreIngresado.toLowerCase(Locale.ROOT))) {
            throw new JugadorNoEncontradoException("El jugador '" + nombreIngresado + "' no es un usuario válido.");
        }

        Player conectado = Bukkit.getPlayerExact(nombreIngresado);
        if (conectado != null) {
            return conectado.getName();
        }

        @SuppressWarnings("deprecation")
        OfflinePlayer offline = Bukkit.getOfflinePlayer(nombreIngresado);
        if (offline.hasPlayedBefore() && offline.getName() != null) {
            return offline.getName();
        }

        throw new JugadorNoEncontradoException(
                "El jugador '" + nombreIngresado + "' no existe o nunca ha entrado al servidor."
        );
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "[SkillIssue] Solo un jugador conectado puede enviar un reporte.");
            return true;
        }

        if (!player.hasPermission("skillissue.report")) {
            player.sendMessage(ChatColor.RED + "[SkillIssue] No tienes permiso para enviar reportes.");
            return true;
        }

        if (args.length == 0) {
            player.openInventory(new ReportJugadorMenu(player.getUniqueId(), 0).getInventory());
            return true;
        }

        if (args.length == 1) {
            String objetivoInput = args[0].trim();
            if (objetivoInput.equalsIgnoreCase(player.getName())) {
                player.sendMessage(ChatColor.RED + "[SkillIssue] No puedes reportarte a ti mismo.");
                return true;
            }
            try {
                String objetivoReal = validarJugadorExistente(objetivoInput);
                player.openInventory(new ReportMotivoMenu(objetivoReal).getInventory());
            } catch (JugadorNoEncontradoException e) {
                player.sendMessage(ChatColor.RED + "[SkillIssue] " + e.getMessage());
            }
            return true;
        }

        String objetivoInput = args[0].trim();

        if (objetivoInput.equalsIgnoreCase(player.getName())) {
            player.sendMessage(ChatColor.RED + "No puedes reportarte a ti mismo.");
            return true;
        }

        String objetivoReal;
        try {
            objetivoReal = validarJugadorExistente(objetivoInput);
        } catch (JugadorNoEncontradoException e) {
            player.sendMessage(ChatColor.RED + "[SkillIssue] " + e.getMessage());
            return true;
        }

        if (objetivoReal.equalsIgnoreCase(player.getName())) {
            player.sendMessage(ChatColor.RED + "No puedes reportarte a ti mismo.");
            return true;
        }

        // Validación de cooldown
        UUID uuid = player.getUniqueId();
        long ahora = System.currentTimeMillis();
        int cantidadPrevios = gestorReportes.contarReportesDe(player.getName());
        long esperaRequeridaMs = coolDown(cantidadPrevios) * 1000L;

        if (ultimosReportes.containsKey(uuid)) {
            long transcurrido = ahora - ultimosReportes.get(uuid);
            if (transcurrido < esperaRequeridaMs) {
                long restantes = (esperaRequeridaMs - transcurrido + 999L) / 1000L;
                player.sendMessage(ChatColor.RED + "[SkillIssue] Debes esperar " + restantes +
                        " segundos antes de enviar otro reporte.");
                return true;
            }
        }

        // Validación RF-01: no duplicar reporte pendiente contra el mismo jugador
        if (gestorReportes.existeReportePendiente(player.getName(), objetivoReal)) {
            player.sendMessage(ChatColor.RED + "[SkillIssue] Ya tienes un reporte pendiente contra " +
                    objetivoReal + ". Espera a que un administrador lo revise.");
            return true;
        }

        // Construcción y validación del motivo
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (!args[i].isBlank()) {
                if (!sb.isEmpty()) sb.append(" ");
                sb.append(args[i].trim());
            }
        }
        String motivo = sb.toString().trim();

        if (motivo.length() < LONGITUD_MIN_MOTIVO) {
            player.sendMessage(ChatColor.RED + "[SkillIssue] El motivo es demasiado corto (mínimo " +
                    LONGITUD_MIN_MOTIVO + " caracteres).");
            return true;
        }

        if (motivo.length() > LONGITUD_MAX_MOTIVO) {
            player.sendMessage(ChatColor.RED + "[SkillIssue] El motivo es demasiado largo (máximo " +
                    LONGITUD_MAX_MOTIVO + " caracteres).");
            return true;
        }

        Reporte creado = gestorReportes.crearReporte(player.getName(), objetivoReal, motivo);
        ultimosReportes.put(uuid, ahora);

        player.sendMessage(ChatColor.GREEN + "[SkillIssue] Reporte #" + creado.getId() +
                " registrado contra " + objetivoReal + ". Motivo: " + ChatColor.WHITE + creado.getMotivo());

        // Notificar únicamente al staff conectado
        String alertaStaff = ChatColor.GOLD + "[ALERTA REPORTE #" + creado.getId() + "] " +
                ChatColor.YELLOW + player.getName() + ChatColor.WHITE + " reportó a " +
                ChatColor.RED + objetivoReal + ChatColor.WHITE + ": " + creado.getMotivo();

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("skillissue.admin.report") || online.isOp()) {
                online.sendMessage(alertaStaff);
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player) || !player.hasPermission("skillissue.report")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            String prefijo = args[0].toLowerCase(Locale.ROOT);
            List<String> jugadores = new ArrayList<>();
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.getUniqueId().equals(player.getUniqueId())
                        && online.getName().toLowerCase(Locale.ROOT).startsWith(prefijo)) {
                    jugadores.add(online.getName());
                }
            }
            return jugadores;
        }

        if (args.length == 2) {
            List<String> ejemplos = Arrays.asList("Hacks", "Griefing", "Insultos", "Spam", "Abuso_de_bugs");
            String prefijo = args[1].toLowerCase(Locale.ROOT);
            List<String> filtrados = new ArrayList<>();
            for (String ej : ejemplos) {
                if (ej.toLowerCase(Locale.ROOT).startsWith(prefijo)) {
                    filtrados.add(ej);
                }
            }
            return filtrados;
        }

        return Collections.emptyList();
    }
}