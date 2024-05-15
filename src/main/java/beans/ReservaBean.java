package beans;

import entity.Reserva;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleModel;
import service.ReservaDAO;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Named
@RequestScoped
@Getter
@Setter
public class ReservaBean implements Serializable {

    @Inject
    private ReservaDAO reservaDAO;

    private Reserva reservaActual = new Reserva();
    private List<Reserva> reservas;
    private ScheduleModel eventModel;
    private LocalDateTime fechaMinima;

    private String newEventTitle;
    private String newEventDescription;
    private Date newEventStartDate;
    private Date newEventEndDate;

    private Reserva selectedEvent;

    @PostConstruct
    public void init() {
        eventModel = new DefaultScheduleModel();

        // Obtén la lista de todas las reservas
        reservas = reservaDAO.obtenerTodas("Reserva.findAll", Reserva.class);

        // Convierte los eventos de la base de datos a objetos DefaultScheduleEvent y agrégales al modelo
        for (Reserva reserva : reservas) {
            DefaultScheduleEvent<?> event = DefaultScheduleEvent.builder()
                    .title(reserva.getNombreEstudiante())
                    .description(reserva.getAsuntoReserva())
                    .startDate(reserva.getFechaEntrada())
                    .endDate(reserva.getFechaSalida())
                    .build();
            eventModel.addEvent(event);
        }
    }

    public void addEvent() {
        DefaultScheduleEvent<?> newEvent = DefaultScheduleEvent.builder()
                .title(newEventTitle)
                .description(newEventDescription)
                .startDate(newEventStartDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .endDate(newEventEndDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .build();

        // Agrega el evento al modelo
        eventModel.addEvent(newEvent);

        // Guarda el nuevo evento en la base de datos
        Reserva reserva = new Reserva();
        reserva.setNombreEstudiante(newEventTitle);
        reserva.setAsuntoReserva(newEventDescription);
        reserva.setFechaEntrada(newEvent.getStartDate());
        reserva.setFechaSalida(newEvent.getEndDate());

        reservaDAO.insert(reserva);
        System.out.println("ID asignado: " + reserva.getIdReserva());

        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Evento agregado con éxito", null);
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    public void onEventSelect(SelectEvent<Reserva> event) {
        selectedEvent = (Reserva) event.getObject();
    }
    public void onDateSelect(SelectEvent<Date> event) {
        Date selectedDate = event.getObject();

        // Define la fecha mínima para la selección de la fecha de salida
        fechaMinima = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        // Establece la fecha de entrada y salida predeterminadas en la reserva actual
        reservaActual.setFechaEntrada(fechaMinima);
        reservaActual.setFechaSalida(fechaMinima.plusHours(1));
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

        // Reset reservaActual
        reservaActual = new Reserva();

        return "success";
    }
    // Getter para eventModel
    public ScheduleModel getEventModel() {
        if (eventModel == null) {
            eventModel = new DefaultScheduleModel();
        }
        return eventModel;
    }

}
