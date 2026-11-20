package com.skillissue.admin.gui;

import com.skillissue.admin.manager.GestorReportes;
import com.skillissue.admin.model.Reporte;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

public final class ReportMenuListener implements Listener {
    private final GestorReportes gestorReportes;

    public ReportMenuListener(GestorReportes gestorReportes) {
        this.gestorReportes = gestorReportes;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getView().getTopInventory().getHolder(false);
        if (!(holder instanceof ReportJugadorMenu || holder instanceof ReportMotivoMenu || holder instanceof ReportAdminMenu)) {
            return;
        }
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)
                || event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }

        int slot = event.getRawSlot();

        if (holder instanceof ReportJugadorMenu menu) {
            if (slot == 49) {
                player.closeInventory();
                return;
            }
            if (slot == 45 && menu.getPage() > 0) {
                player.openInventory(new ReportJugadorMenu(player.getUniqueId(), menu.getPage() - 1).getInventory());
                return;
            }
            if (slot == 53) {
                player.openInventory(new ReportJugadorMenu(player.getUniqueId(), menu.getPage() + 1).getInventory());
                return;
            }
            Player target = menu.jugadorEnSlot(slot);
            if (target != null) {
                player.openInventory(new ReportMotivoMenu(target.getName()).getInventory());
            }
            return;
        }

        if (holder instanceof ReportMotivoMenu menu) {
            if (slot == 31) {
                player.closeInventory();
                return;
            }
            if (slot == 27) {
                player.openInventory(new ReportJugadorMenu(player.getUniqueId(), 0).getInventory());
                return;
            }
            MotivoReporte motivo = menu.motivoEnSlot(slot);
            if (motivo == null) {
                return;
            }
            String targetName = menu.getTargetName();
            if (gestorReportes.existeReportePendiente(player.getName(), targetName)) {
                player.closeInventory();
                player.sendMessage(Component.text("§c[SkillIssue] Ya tienes un reporte pendiente contra " + targetName + "."));
                return;
            }

            Reporte creado = gestorReportes.crearReporte(player.getName(), targetName, motivo.getEtiqueta());
            player.closeInventory();
            player.sendMessage(Component.text("§a[SkillIssue] Reporte #" + creado.getId()
                    + " registrado contra §f" + targetName + "§a. Motivo: §f" + creado.getMotivo()));

            Component alertaStaff = Component.text("§6[ALERTA REPORTE #" + creado.getId() + "] §e"
                    + player.getName() + " §freportó a §c" + targetName + "§f: " + creado.getMotivo());
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.hasPermission("skillissue.admin.report") || online.isOp()) {
                    online.sendMessage(alertaStaff);
                }
            }
            return;
        }

        if (holder instanceof ReportAdminMenu menu) {
            if (slot == 49) {
                player.closeInventory();
                return;
            }
            if (slot == 45 && menu.getPage() > 0) {
                player.openInventory(new ReportAdminMenu(gestorReportes, menu.getPage() - 1).getInventory());
                return;
            }
            if (slot == 53) {
                player.openInventory(new ReportAdminMenu(gestorReportes, menu.getPage() + 1).getInventory());
                return;
            }
            Reporte reporte = menu.reporteEnSlot(slot);
            if (reporte == null) {
                return;
            }

            String nuevoEstado;
            String nota;
            if (event.isShiftClick()) {
                nuevoEstado = "Pendiente";
                nota = "Reabierto desde GUI";
            } else if (event.isRightClick()) {
                nuevoEstado = "Descartado";
                nota = "Descartado desde GUI";
            } else {
                nuevoEstado = "Atendido";
                nota = "Atendido desde GUI";
            }

            gestorReportes.resolverReporte(reporte.getId(), nuevoEstado, player.getName(), nota);
            player.sendMessage(Component.text("§a[SkillIssue] Reporte #" + reporte.getId() + " marcado como " + nuevoEstado + "."));

            if (!nuevoEstado.equals("Pendiente")) {
                Player reportanteOnline = Bukkit.getPlayerExact(reporte.getReportante());
                if (reportanteOnline != null) {
                    reportanteOnline.sendMessage(Component.text("§b[SkillIssue] Tu reporte #" + reporte.getId()
                            + " contra " + reporte.getReportado() + " fue revisado (" + nuevoEstado + ")."));
                }
            }

            player.openInventory(new ReportAdminMenu(gestorReportes, menu.getPage()).getInventory());
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getView().getTopInventory().getHolder(false);
        if (holder instanceof ReportJugadorMenu || holder instanceof ReportMotivoMenu || holder instanceof ReportAdminMenu) {
            event.setCancelled(true);
        }
    }
}
