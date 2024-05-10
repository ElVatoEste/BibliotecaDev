package beans;

import entity.Reserva;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleModel;
import service.ReservaDAO;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Named
@ApplicationScoped
public class ReservaBean {

    @Inject
    private ReservaDAO reservaDAO;

    @Getter @Setter
    private Reserva reservaActual = new Reserva(); // Initialize here

    @Getter
    private List<Reserva> reservas;

    @Getter
    private ScheduleModel eventModel = new DefaultScheduleModel(); // Initialize here

    private String newEventTitle;
    private String newEventDescription;
    private java.util.Date newEventStartDate;
    private java.util.Date newEventEndDate;

    @PostConstruct
    public void init() {
        reservas = reservaDAO.obtenerTodas(); // Fetch all reservations on init

        // Populate scheduleModel with events from reservas (same logic)
        for (Reserva reserva : reservas) {
            LocalDateTime startDate = LocalDateTime.ofInstant(reserva.getFechaEntrada().toInstant(), ZoneId.systemDefault());
            LocalDateTime endDate = LocalDateTime.ofInstant(reserva.getFechaSalida().toInstant(), ZoneId.systemDefault());
            DefaultScheduleEvent event = DefaultScheduleEvent.builder()
                    .title(reserva.getAsuntoReserva())
                    .startDate(startDate)
                    .endDate(endDate)
                    .build();
            eventModel.addEvent(event);
        }
    }


    public String guardarReserva() {
        // Check for reservation conflicts (same logic)
        if (reservaDAO.hayChoqueDeReservas(reservaActual.getFechaEntrada(), reservaActual.getFechaSalida())) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "Ya existe una reserva para el horario seleccionado.");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return null;
        }

        // Validate number of people (same logic)
        if (reservaActual.getCantidadPersonas() > 15) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "La cantidad de personas excede la capacidad máxima de la sala.");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return null;
        }

        // Save the reservation
        reservaDAO.guardar(reservaActual);

        // Update UI after successful save
        reservas = reservaDAO.obtenerTodas(); // Refresh list
        eventModel.clear(); // Clear existing events
        for (Reserva reserva : reservas) {
            LocalDateTime startDate = LocalDateTime.ofInstant(reserva.getFechaEntrada().toInstant(), ZoneId.systemDefault());
            LocalDateTime endDate = LocalDateTime.ofInstant(reserva.getFechaSalida().toInstant(), ZoneId.systemDefault());
            DefaultScheduleEvent event = DefaultScheduleEvent.builder()
                    .title(reserva.getAsuntoReserva())
                    .startDate(startDate)
                    .endDate(endDate)
                    .build();
            eventModel.addEvent(event);
        }

        // Reset reservaActual
        reservaActual = new Reserva();

        return "success";
    }
}
