package com.skillissue.admin.model;

import java.util.ArrayList;
import java.util.List;

public class Rango {
    private String nombre;
    private final ArrayList<String> permisos;
    private boolean esPago;
    private int nivelJerarquia;

    public Rango(String nombre, boolean esPago, int nivelJerarquia) {
        this.nombre = nombre;
        this.esPago = esPago;
        this.nivelJerarquia = nivelJerarquia;
        this.permisos = new ArrayList<>();
    }

    public void agregarPermiso(String permiso) {
        if (permiso != null && !permiso.isBlank() && !permisos.contains(permiso)) {
            permisos.add(permiso.trim());
        }
    }

    public boolean tienePermiso(String permiso) {
        if (permiso == null || permiso.isBlank()) {
            return false;
        }
        return permisos.contains("*") || permisos.contains(permiso);
    }

    public boolean esRangoPago() {
        return esPago;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<String> getPermisos() {
        return new ArrayList<>(permisos);
    }

    public int getNivelJerarquia() {
        return nivelJerarquia;
    }

    public void setNivelJerarquia(int nivelJerarquia) {
        this.nivelJerarquia = nivelJerarquia;
    }
}