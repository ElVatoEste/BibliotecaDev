package service;

import entity.Archivado;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservaDAO {
    // Guardar una nueva reserva

    <T> List<T> obtenerTodas(String namedQuery, Class<T> clazz);

    <T> List<T> get(String namedQuery, Class<T> clazz, Object... params);

    <T> T update(T entity);

    <T> void guardar(T entity);

    <T> void insert(T entity);

    <T> void eliminar(T entity);

    // Buscar una reserva por su CIF
    <T, ID> T buscarPorId(Class<T> clazz, ID id);

    // Verificar si hay un choque de reservas en un rango de tiempo
    int obtenerTotalPersonasReservadas(LocalDateTime fechaEntrada, LocalDateTime fechaSalida);

    boolean hayComputadoraDisponible(LocalDateTime fechaEntrada, LocalDateTime fechaSalida);

    boolean hayProyectorDisponible(LocalDateTime fechaEntrada, LocalDateTime fechaSalida);

    boolean hayPizarraDisponible(LocalDateTime fechaEntrada, LocalDateTime fechaSalida);

    <T> List<T> obtenerReservasMensuales(int mesActual, int anioActual);

    void archivarReserva(Long idReserva, Archivado.AsistenciaEstado estadoAsistencia);


}
