package service;

import java.util.List;

public interface ArchivadoDAO {

    <T> void eliminar(T entity);

    <T> List<T> obtenerArchivadosMensuales(int mesActual, int anioActual);
}
