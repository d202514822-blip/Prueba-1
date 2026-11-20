package com.skillissue.admin.manager;

import com.skillissue.admin.model.Jugador;
import com.skillissue.admin.model.Rango;
import com.skillissue.admin.persistence.IPersistencia;

import java.util.ArrayList;

public class GestorJugadores {
    private final ArrayList<Jugador> jugadores;
    private final IPersistencia persistencia;
    private final Rango rangoPorDefecto;

    public GestorJugadores(IPersistencia persistencia) {
        this.persistencia = persistencia;
        this.jugadores = new ArrayList<>();
        this.rangoPorDefecto = new Rango("Usuario", false, 0);
        this.rangoPorDefecto.agregarPermiso("skillissue.report");
    }

    public void registrarJugador(Jugador j) {
        if (j != null && buscarJugador(j.getId()) == null) {
            jugadores.add(j);
        }
    }

    public Jugador buscarJugador(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        for (Jugador j : jugadores) {
            if (j.getId().equalsIgnoreCase(id)) {
                return j;
            }
        }
        return null;
    }

    public Jugador buscarJugador(String nombre, boolean exacto) {
        if (nombre == null || nombre.isBlank()) {
            return null;
        }
        for (Jugador j : jugadores) {
            if (exacto && j.getNombre().equalsIgnoreCase(nombre)) {
                return j;
            }
            if (!exacto && j.getNombre().toLowerCase().contains(nombre.toLowerCase())) {
                return j;
            }
        }
        return null;
    }

    public Jugador obtenerOCrearJugador(String id, String nombre) {
        Jugador existente = buscarJugador(nombre, true);
        if (existente != null) {
            return existente;
        }
        Jugador nuevo = new Jugador(id, nombre, rangoPorDefecto);
        registrarJugador(nuevo);
        return nuevo;
    }
}