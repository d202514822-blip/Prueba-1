package com.skillissue.admin.persistence;

import java.util.ArrayList;

public interface IPersistencia {
    boolean guardarDatos(String archivo, ArrayList<?> datos);

    ArrayList<String> cargarDatos(String archivo);

    boolean verificarArchivo(String nombreArchivo);
}