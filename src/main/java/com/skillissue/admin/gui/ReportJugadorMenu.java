package com.skillissue.admin.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class ReportJugadorMenu implements InventoryHolder {
    public static final int PAGE_SIZE = 28;
    private static final int[] SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private final Inventory inventory;
    private final int page;
    private final List<Player> jugadores;

    public ReportJugadorMenu(UUID reporterId, int page) {
        this.page = Math.max(0, page);
        this.jugadores = Bukkit.getOnlinePlayers().stream()
                .filter(p -> !p.getUniqueId().equals(reporterId))
                .sorted(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER))
                .map(p -> (Player) p)
                .toList();

        this.inventory = Bukkit.createInventory(this, 54, Component.text("§8Reportar jugador • Pág. " + (this.page + 1)));
        MenuItems.frame(inventory);

        int from = this.page * PAGE_SIZE;
        for (int i = 0; i < PAGE_SIZE && from + i < jugadores.size(); i++) {
            Player target = jugadores.get(from + i);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(target);
            meta.displayName(Component.text("§e" + target.getName()));
            meta.lore(List.of(Component.text("§7Clic para elegir motivo")));
            head.setItemMeta(meta);
            inventory.setItem(SLOTS[i], head);
        }

        if (jugadores.isEmpty()) {
            inventory.setItem(22, MenuItems.item(Material.BARRIER, "§cNo hay otros jugadores conectados"));
        }

        inventory.setItem(4, MenuItems.item(Material.PAPER, "§bSelecciona a quién reportar", "§7O usa /report <jugador> <motivo>"));
        if (this.page > 0) {
            inventory.setItem(45, MenuItems.item(Material.ARROW, "§e← Página anterior"));
        }
        inventory.setItem(49, MenuItems.item(Material.BARRIER, "§cCancelar", "§7Cerrar el menú"));
        if (from + PAGE_SIZE < jugadores.size()) {
            inventory.setItem(53, MenuItems.item(Material.ARROW, "§ePágina siguiente →"));
        }
    }

    public Player jugadorEnSlot(int slot) {
        for (int i = 0; i < SLOTS.length; i++) {
            if (SLOTS[i] == slot) {
                int index = page * PAGE_SIZE + i;
                return index < jugadores.size() ? jugadores.get(index) : null;
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
