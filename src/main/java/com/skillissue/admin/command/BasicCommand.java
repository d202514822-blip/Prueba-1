package com.skillissue.admin.command;

public abstract class BasicCommand {
    protected String nombreComando;
    protected String descripcion;

    public BasicCommand(String nombreComando, String descripcion) {
        this.nombreComando = nombreComando;
        this.descripcion = descripcion;
    }

    public String getNombreComando() {
        return nombreComando;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public abstract void ejecutar();
}