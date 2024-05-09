package beans;

import entity.Reserva;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import service.ReservaDAO;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleModel;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
@Getter
@Setter
public class ReservaBean {

    @Autowired
    private ReservaDAO reservaDAO;

    private Reserva reservaActual;
    private List<Reserva> reservas;
    private ScheduleModel scheduleModel;

    @PostConstruct
    public void init() {
        reservaActual = new Reserva();
        reservas = reservaDAO.obtenerTodas();
        scheduleModel = new DefaultScheduleModel();

        // Populate scheduleModel with events from reservas
        for (Reserva reserva : reservas) {
            LocalDateTime startDate = LocalDateTime.ofInstant(reserva.getFechaEntrada().toInstant(), ZoneId.systemDefault());
            LocalDateTime endDate = LocalDateTime.ofInstant(reserva.getFechaSalida().toInstant(), ZoneId.systemDefault());
            DefaultScheduleEvent event = DefaultScheduleEvent.builder()
                    .title(reserva.getAsuntoReserva())
                    .startDate(startDate)
                    .endDate(endDate)
                    .build();
            scheduleModel.addEvent(event);
        }
    }

    public String guardarReserva() {
        // Check for reservation conflicts
        if (reservaDAO.hayChoqueDeReservas(reservaActual.getFechaEntrada(), reservaActual.getFechaSalida())) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "Ya existe una reserva para el horario seleccionado.");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return null;
        }

        // Validate number of people
        if (reservaActual.getCantidadPersonas() > 15) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "La cantidad de personas excede la capacidad máxima de la sala.");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return null;
        }

        // Save the reservation
        reservaDAO.guardar(reservaActual);

        // Update reservas and scheduleModel
        reservas = reservaDAO.obtenerTodas();
        scheduleModel.clear();
        for (Reserva reserva : reservas) {
            LocalDateTime startDate = LocalDateTime.ofInstant(reserva.getFechaEntrada().toInstant(), ZoneId.systemDefault());
            LocalDateTime endDate = LocalDateTime.ofInstant(reserva.getFechaSalida().toInstant(), ZoneId.systemDefault());
            DefaultScheduleEvent event = DefaultScheduleEvent.builder()
                    .title(reserva.getAsuntoReserva())
                    .startDate(startDate)
                    .endDate(endDate)
                    .build();
            scheduleModel.addEvent(event);
        }

        // Reset reservaActual
        reservaActual = new Reserva();

        return "success";
    }
}
