package service;
import entity.Reserva;

import java.util.Date;
import java.util.List;

public interface ReservaDAO {
    // Guardar una nueva reserva

    <T> List<T> obtenerTodas(String namedQuery, Class<T> clazz);

    <T> List<T> get(String namedQuery, Class<T> clazz, Object... params);

    <T> T Actualizar(T entity);

    <T> T guardar(T entity);

    <T> void eliminar(T entity);

    // Buscar una reserva por su CIF
    <T, ID> T buscarPorId(Class<T> clazz, ID id);

    // Verificar si hay un choque de reservas en un rango de tiempo
    boolean hayChoqueDeReservas(Date fechaEntrada, Date fechaSalida);
}
