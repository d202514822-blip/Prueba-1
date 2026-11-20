package com.skillissue.admin.gui;

import com.skillissue.admin.manager.GestorReportes;
import com.skillissue.admin.model.Reporte;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.Comparator;
import java.util.List;

public final class ReportAdminMenu implements InventoryHolder {
    private static final int[] SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private final Inventory inventory;
    private final int page;
    private final List<Reporte> reportes;

    public ReportAdminMenu(GestorReportes gestorReportes, int page) {
        this.page = Math.max(0, page);
        this.reportes = gestorReportes.obtenerTodos().stream()
                .sorted(Comparator
                        .comparing((Reporte r) -> !r.getEstado().equalsIgnoreCase("Pendiente"))
                        .thenComparing(Reporte::getId, Comparator.reverseOrder()))
                .toList();

        this.inventory = Bukkit.createInventory(this, 54, Component.text("§8Gestión de reportes • Pág. " + (this.page + 1)));
        MenuItems.frame(inventory);

        inventory.setItem(4, MenuItems.item(
                Material.WRITABLE_BOOK,
                "§bGestión de reportes",
                "§7Clic izquierdo: §aMarcar Atendido",
                "§7Clic derecho: §cMarcar Descartado",
                "§7Mayús + clic: §eReabrir como Pendiente"
        ));

        int from = this.page * SLOTS.length;
        for (int i = 0; i < SLOTS.length && from + i < reportes.size(); i++) {
            Reporte r = reportes.get(from + i);
            Material icono = switch (r.getEstado().toLowerCase()) {
                case "atendido" -> Material.LIME_DYE;
                case "descartado" -> Material.RED_DYE;
                default -> Material.YELLOW_DYE;
            };
            String colorEstado = switch (r.getEstado().toLowerCase()) {
                case "atendido" -> "§a";
                case "descartado" -> "§c";
                default -> "§e";
            };

            inventory.setItem(SLOTS[i], MenuItems.item(
                    icono,
                    "§f#" + r.getId() + " §8• §e" + r.getReportado(),
                    "§7Reportante: §f" + r.getReportante(),
                    "§7Motivo: §f" + r.getMotivo(),
                    "§7Estado: " + colorEstado + r.getEstado(),
                    "§7Fecha: §f" + r.getFecha() + " " + r.getHora(),
                    "§7Admin: §f" + r.getAdminResponsable(),
                    "§8Clic izq: Atendido | Clic der: Descartado"
            ));
        }

        if (reportes.isEmpty()) {
            inventory.setItem(22, MenuItems.item(Material.LIME_DYE, "§aNo hay reportes registrados"));
        }
        if (this.page > 0) {
            inventory.setItem(45, MenuItems.item(Material.ARROW, "§e← Página anterior"));
        }
        inventory.setItem(49, MenuItems.item(Material.BARRIER, "§cCerrar"));
        if (from + SLOTS.length < reportes.size()) {
            inventory.setItem(53, MenuItems.item(Material.ARROW, "§ePágina siguiente →"));
        }
    }

    public Reporte reporteEnSlot(int slot) {
        for (int i = 0; i < SLOTS.length; i++) {
            if (SLOTS[i] == slot) {
                int index = page * SLOTS.length + i;
                return index < reportes.size() ? reportes.get(index) : null;
            }
        }
        return null;
    }

    public int getPage() {
        return page;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
