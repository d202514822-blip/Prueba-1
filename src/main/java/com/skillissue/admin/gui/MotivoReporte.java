package com.skillissue.admin.gui;

import org.bukkit.Material;

public enum MotivoReporte {
    HACKS("Hacks / Trampas", Material.IRON_SWORD),
    GRIEFING("Griefing / Daño", Material.TNT),
    INSULTOS("Insultos / Toxicidad", Material.POISONOUS_POTATO),
    SPAM("Spam / Publicidad", Material.PAPER),
    ABUSO_BUGS("Abuso de bugs", Material.COBWEB),
    OTRO("Otro motivo", Material.WRITABLE_BOOK);

    private final String etiqueta;
    private final Material icono;

    MotivoReporte(String etiqueta, Material icono) {
        this.etiqueta = etiqueta;
        this.icono = icono;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public Material getIcono() {
        return icono;
    }
}
