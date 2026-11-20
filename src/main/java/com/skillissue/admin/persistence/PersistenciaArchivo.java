package com.skillissue.admin.persistence;

import com.skillissue.admin.model.Reporte;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PersistenciaArchivo implements IPersistencia {
    private final String rutaBase;

    public PersistenciaArchivo(String rutaBase) {
        this.rutaBase = rutaBase;
        try {
            Files.createDirectories(Paths.get(rutaBase));
        } catch (IOException e) {
            System.err.println("[SkillIssueAdmin] No se pudo crear la carpeta de datos: " + e.getMessage());
        }
    }

    @Override
    public boolean guardarDatos(String archivo, ArrayList<?> datos) {
        if (archivo == null || archivo.isBlank() || datos == null) {
            return false;
        }
        Path rutaArchivo = Paths.get(rutaBase, archivo);
        List<String> lineas = new ArrayList<>();
        for (Object item : datos) {
            if (item instanceof Reporte reporte) {
                lineas.add(reporte.toLineaArchivo());
            } else if (item != null) {
                lineas.add(item.toString());
            }
        }
        try {
            if (rutaArchivo.getParent() != null) {
                Files.createDirectories(rutaArchivo.getParent());
            }
            Files.write(rutaArchivo, lineas, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            System.err.println("[SkillIssueAdmin] Error guardando " + archivo + ": " + e.getMessage());
            return false;
        }
    }

    @Override
    public ArrayList<String> cargarDatos(String archivo) {
        ArrayList<String> resultado = new ArrayList<>();
        if (!verificarArchivo(archivo)) {
            return resultado;
        }
        Path rutaArchivo = Paths.get(rutaBase, archivo);
        try {
            List<String> lineas = Files.readAllLines(rutaArchivo, StandardCharsets.UTF_8);
            for (String linea : lineas) {
                if (linea != null && !linea.isBlank()) {
                    resultado.add(linea);
                }
            }
        } catch (IOException e) {
            System.err.println("[SkillIssueAdmin] Error leyendo " + archivo + ": " + e.getMessage());
        }
        return resultado;
    }

    @Override
    public boolean verificarArchivo(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.isBlank()) {
            return false;
        }
        Path rutaArchivo = Paths.get(rutaBase, nombreArchivo);
        return Files.exists(rutaArchivo) && Files.isRegularFile(rutaArchivo);
    }
}