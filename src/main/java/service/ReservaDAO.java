package service;
import entity.Reserva;

import java.util.Date;
import java.util.List;

public interface ReservaDAO {
    // Guardar una nueva reserva
    void guardar(Reserva reserva);

    // Actualizar una reserva existente
    void actualizar(Reserva reserva);

    // Eliminar una reserva por su CIF
    void eliminar(Long IdReserva);

    // Buscar una reserva por su CIF
    Reserva buscarPorID(Long IdReserva);

    // Obtener todas las reservas
    List<Reserva> obtenerTodas();

    // Verificar si hay un choque de reservas en un rango de tiempo
    boolean hayChoqueDeReservas(Date fechaEntrada, Date fechaSalida);
}
