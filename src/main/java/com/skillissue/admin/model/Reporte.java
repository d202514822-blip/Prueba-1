package com.skillissue.admin.model;

public class Reporte {
    private static int contadorReportes = 0;

    private int id;
    private String reportante;
    private String reportado;
    private String motivo;
    private String estado;
    private String fecha;
    private String hora;
    private String adminResponsable;
    private String comentarios;

    public Reporte(int id, String reportante, String reportado, String motivo,
                   String estado, String fecha, String hora,
                   String adminResponsable, String comentarios) {
        this.id = id;
        this.reportante = reportante;
        this.reportado = reportado;
        this.motivo = motivo;
        this.estado = estado;
        this.fecha = fecha;
        this.hora = hora;
        this.adminResponsable = adminResponsable;
        this.comentarios = comentarios;
        if (id > contadorReportes) {
            contadorReportes = id;
        }
    }

    public static int getSiguienteId() {
        return ++contadorReportes;
    }

    public static void reiniciarContador(int maxId) {
        contadorReportes = Math.max(0, maxId);
    }

    public void cambiarEstado(String nuevoEstado, String admin, String nota) {
        this.estado = nuevoEstado;
        this.adminResponsable = (admin == null || admin.isBlank()) ? "Consola" : admin;
        this.comentarios = (nota == null || nota.isBlank()) ? "Sin comentarios" : nota.replace("|", "/").trim();
    }

    public String toLineaArchivo() {
        return id + "|" + reportante + "|" + reportado + "|" + motivo + "|" +
                estado + "|" + fecha + "|" + hora + "|" + adminResponsable + "|" + comentarios;
    }

    public int getId() { return id; }
    public String getReportante() { return reportante; }
    public String getReportado() { return reportado; }
    public String getMotivo() { return motivo; }
    public String getEstado() { return estado; }
    public String getFecha() { return fecha; }
    public String getHora() { return hora; }
    public String getAdminResponsable() { return adminResponsable; }
    public String getComentarios() { return comentarios; }

    @Override
    public String toString() {
        return toLineaArchivo();
    }
}