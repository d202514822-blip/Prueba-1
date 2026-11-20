package com.skillissue.admin.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class ReportMotivoMenu implements InventoryHolder {
    private static final int[] SLOTS = {11, 12, 13, 14, 15, 22};

    private final Inventory inventory;
    private final String targetName;

    public ReportMotivoMenu(String targetName) {
        this.targetName = targetName;
        this.inventory = Bukkit.createInventory(this, 36, Component.text("§8Motivo • " + targetName));
        MenuItems.frame(inventory);

        inventory.setItem(4, MenuItems.item(
                Material.PAPER,
                "§bReporte contra §f" + targetName,
                "§7Elige el motivo para enviar el reporte."
        ));

        MotivoReporte[] motivos = MotivoReporte.values();
        for (int i = 0; i < motivos.length && i < SLOTS.length; i++) {
            MotivoReporte m = motivos[i];
            inventory.setItem(SLOTS[i], MenuItems.item(
                    m.getIcono(),
                    "§e" + m.getEtiqueta(),
                    "§7Clic para enviar reporte"
            ));
        }

        inventory.setItem(27, MenuItems.item(Material.ARROW, "§e← Volver a jugadores"));
        inventory.setItem(31, MenuItems.item(Material.BARRIER, "§cCancelar"));
    }

    public MotivoReporte motivoEnSlot(int slot) {
        MotivoReporte[] motivos = MotivoReporte.values();
        for (int i = 0; i < SLOTS.length && i < motivos.length; i++) {
            if (SLOTS[i] == slot) {
                return motivos[i];
            }
        }
        return null;
    }

    public String getTargetName() {
        return targetName;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
