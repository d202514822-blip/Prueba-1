package com.skillissue.admin;

import com.skillissue.admin.command.ReportCommand;
import com.skillissue.admin.manager.GestorJugadores;
import com.skillissue.admin.manager.GestorReportes;
import com.skillissue.admin.persistence.IPersistencia;
import com.skillissue.admin.persistence.PersistenciaArchivo;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class SkillIssueAdminPlugin extends JavaPlugin {
    private GestorJugadores gestorJugadores;
    private GestorReportes gestorReportes;

    @Override
    public void onEnable() {
        IPersistencia persistencia = new PersistenciaArchivo(getDataFolder().getPath());
        this.gestorJugadores = new GestorJugadores(persistencia);
        this.gestorReportes = new GestorReportes(persistencia);

        ReportCommand reportCommand = new ReportCommand(gestorReportes, gestorJugadores);
        PluginCommand comandoReport = getCommand("report");
        if (comandoReport != null) {
            comandoReport.setExecutor(reportCommand);
            comandoReport.setTabCompleter(reportCommand);
        }

        getLogger().info("SkillIssueAdmin habilitado correctamente (Modulo de Reportes listo).");
    }

    @Override
    public void onDisable() {
        getLogger().info("SkillIssueAdmin deshabilitado.");
    }

    public GestorJugadores getGestorJugadores() {
        return gestorJugadores;
    }

    public GestorReportes getGestorReportes() {
        return gestorReportes;
    }
}