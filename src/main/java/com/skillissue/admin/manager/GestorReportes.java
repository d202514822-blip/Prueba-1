package com.skillissue.admin.manager;

import com.skillissue.admin.model.Reporte;
import com.skillissue.admin.persistence.IPersistencia;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class GestorReportes {
    private static final String ARCHIVO_REPORTES = "reportes.txt";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final ArrayList<Reporte> reportes;
    private final IPersistencia persistencia;

    public GestorReportes(IPersistencia persistencia) {
        this.persistencia = persistencia;
        this.reportes = new ArrayList<>();
        cargarDesdeArchivo();
    }

    private void cargarDesdeArchivo() {
        reportes.clear();
        Reporte.reiniciarContador(0);
        ArrayList<?> datos = persistencia.cargarDatos(ARCHIVO_REPORTES);
        if (datos == null) return;

        for (Object item : datos) {
            if (item == null) continue;
            String[] p = item.toString().split("\\|", -1);
            if (p.length >= 9) {
                try {
                    int id = Integer.parseInt(p[0].trim());
                    Reporte r = new Reporte(
                            id,
                            p[1].trim(),
                            p[2].trim(),
                            p[3].trim(),
                            p[4].trim(),
                            p[5].trim(),
                            p[6].trim(),
                            p[7].trim(),
                            p[8].trim()
                    );
                    reportes.add(r);
                } catch (NumberFormatException e) {
                    System.err.println("[SkillIssueAdmin] Registro inválido omitido en reportes.txt: " + item);
                }
            }
        }
    }

    private void guardarEnArchivo() {
        persistencia.guardarDatos(ARCHIVO_REPORTES, reportes);
    }

    public Reporte crearReporte(String reportante, String reportado, String motivo) {
        String motivoLimpio = motivo.replace("|", "/").replace("\n", " ").replace("\r", " ").trim();
        Reporte nuevo = new Reporte(
                Reporte.getSiguienteId(),
                reportante,
                reportado,
                motivoLimpio,
                "Pendiente",
                LocalDate.now().format(FORMATO_FECHA),
                LocalTime.now().format(FORMATO_HORA),
                "Sin asignar",
                "Sin comentarios"
        );
        reportes.add(nuevo);
        guardarEnArchivo();
        return nuevo;
    }

    public boolean existeReportePendiente(String reportante, String reportado) {
        for (Reporte r : reportes) {
            if (r.getReportante().equalsIgnoreCase(reportante)
                    && r.getReportado().equalsIgnoreCase(reportado)
                    && r.getEstado().equalsIgnoreCase("Pendiente")) {
                return true;
            }
        }
        return false;
    }

    public int contarReportesDe(String reportante) {
        int total = 0;
        for (Reporte r : reportes) {
            if (r.getReportante().equalsIgnoreCase(reportante)) {
                total++;
            }
        }
        return total;
    }

    public Reporte buscarPorId(int id) {
        for (Reporte r : reportes) {
            if (r.getId() == id) {
                return r;
            }
        }
        return null;
    }

    public List<Reporte> obtenerTodos() {
        return new ArrayList<>(reportes);
    }

    public List<Reporte> filtrarReportes(String estado) {
        CriterioFiltro criterio = new CriterioFiltro(estado, null, null);
        return filtrarReportes(criterio);
    }

    public List<Reporte> filtrarReportes(CriterioFiltro criterio) {
        List<Reporte> resultado = new ArrayList<>();
        for (Reporte r : reportes) {
            if (criterio.coincide(r)) {
                resultado.add(r);
            }
        }
        return resultado;
    }

    public boolean resolverReporte(int id, String estadoNormalizado, String admin, String nota) {
        Reporte r = buscarPorId(id);
        if (r == null) return false;
        r.cambiarEstado(estadoNormalizado, admin, nota);
        guardarEnArchivo();
        return true;
    }

    public static class CriterioFiltro {
        private final String estadoBuscado;
        private final String fechaDesde;
        private final String fechaHasta;

        public CriterioFiltro(String estadoBuscado, String fechaDesde, String fechaHasta) {
            this.estadoBuscado = estadoBuscado;
            this.fechaDesde = fechaDesde;
            this.fechaHasta = fechaHasta;
        }

        public boolean coincide(Reporte r) {
            if (estadoBuscado != null && !estadoBuscado.equals("*") && !estadoBuscado.isBlank()) {
                if (!r.getEstado().equalsIgnoreCase(estadoBuscado)) {
                    return false;
                }
            }
            if (fechaDesde != null && !fechaDesde.equals("*") && !fechaDesde.isBlank()) {
                if (r.getFecha().compareTo(fechaDesde) < 0) {
                    return false;
                }
            }
            if (fechaHasta != null && !fechaHasta.equals("*") && !fechaHasta.isBlank()) {
                if (r.getFecha().compareTo(fechaHasta) > 0) {
                    return false;
                }
            }
            return true;
        }
    }
}