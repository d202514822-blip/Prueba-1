package ErrorCapa8.skillIssueAdmin;

import com.skillissue.admin.command.GestionarReportesCommand;
import com.skillissue.admin.command.ReportCommand;
import com.skillissue.admin.gui.ReportMenuListener;
import com.skillissue.admin.manager.GestorJugadores;
import com.skillissue.admin.manager.GestorReportes;
import com.skillissue.admin.persistence.IPersistencia;
import com.skillissue.admin.persistence.PersistenciaArchivo;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class SkillIssueAdmin extends JavaPlugin {

    private GestorReportes gestorReportes;
    private GestorJugadores gestorJugadores;

    @Override
    public void onEnable() {
        IPersistencia persistencia = new PersistenciaArchivo(getDataFolder().getPath());
        this.gestorJugadores = new GestorJugadores(persistencia);
        this.gestorReportes = new GestorReportes(persistencia);

        ReportCommand reportCommand = new ReportCommand(gestorReportes, gestorJugadores);
        PluginCommand cmdReport = getCommand("report");
        if (cmdReport != null) {
            cmdReport.setExecutor(reportCommand);
            cmdReport.setTabCompleter(reportCommand);
        }

        GestionarReportesCommand gestionarCommand = new GestionarReportesCommand(gestorReportes);
        PluginCommand cmdGestionar = getCommand("gestionarreportes");
        if (cmdGestionar != null) {
            cmdGestionar.setExecutor(gestionarCommand);
            cmdGestionar.setTabCompleter(gestionarCommand);
        }

        getServer().getPluginManager().registerEvents(new ReportMenuListener(gestorReportes), this);

        getLogger().info("SkillIssueAdmin habilitado correctamente.");
    }

    @Override
    public void onDisable() {
        getLogger().info("SkillIssueAdmin deshabilitado.");
    }
}