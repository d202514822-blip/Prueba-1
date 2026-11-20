package com.skillissue.admin.model;

import java.time.LocalDate;

public class Jugador {
    public static final int NIVEL_MIN_MIEMBRO = 10;
    public static final int MONEDAS_MIN_MIEMBRO = 500;

    private final String id;
    private String nombre;
    private String skin;
    private int nivel;
    private int monedas;
    private int horasJugadas;
    private final String fechaIngreso;
    private Rango rangoActual;
    private int reportesRealizados;

    public Jugador(String id, String nombre, Rango rangoActual) {
        this.id = id;
        this.nombre = nombre;
        this.skin = "default";
        this.nivel = 1;
        this.monedas = 0;
        this.horasJugadas = 0;
        this.fechaIngreso = LocalDate.now().toString();
        this.rangoActual = rangoActual;
        this.reportesRealizados = 0;
    }

    public void registrarHoras(int horas) {
        if (horas > 0) {
            this.horasJugadas += horas;
        }
    }

    public void asignarRango(Rango nuevoRango) {
        if (nuevoRango != null) {
            this.rangoActual = nuevoRango;
        }
    }

    public boolean subirRangoGratuito(Rango rangoMiembro) {
        if (rangoMiembro != null && !rangoMiembro.esRangoPago()
                && this.nivel >= NIVEL_MIN_MIEMBRO
                && this.monedas >= MONEDAS_MIN_MIEMBRO) {
            this.rangoActual = rangoMiembro;
            return true;
        }
        return false;
    }

    public boolean tienePermiso(String permiso) {
        return rangoActual != null && rangoActual.tienePermiso(permiso);
    }

    public void incrementarReportesRealizados() {
        this.reportesRealizados++;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getSkin() {
        return skin;
    }

    public void setSkin(String skin) {
        this.skin = skin;
    }

    public int getNivel() {
        return nivel;
    }

    public void setNivel(int nivel) {
        this.nivel = nivel;
    }

    public int getMonedas() {
        return monedas;
    }

    public void setMonedas(int monedas) {
        this.monedas = monedas;
    }

    public int getHorasJugadas() {
        return horasJugadas;
    }

    public String getFechaIngreso() {
        return fechaIngreso;
    }

    public Rango getRangoActual() {
        return rangoActual;
    }

    public int getReportesRealizados() {
        return reportesRealizados;
    }
}