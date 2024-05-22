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
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;
import service.ReservaDAO;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Random;

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

    private String serverTimeZone = ZoneId.systemDefault().toString();
    private String clientTimeZone = "local";
    private ScheduleEvent<?> event = new DefaultScheduleEvent<>();

    @PostConstruct
    public void init() {
        eventModel = new DefaultScheduleModel();
        reservas = reservaDAO.obtenerTodas("Reserva.findAll", Reserva.class);

        for (Reserva reserva : reservas) {
            String borderColor = generarColorAleatorio();
            DefaultScheduleEvent<?> event = DefaultScheduleEvent.builder()
                    .title(reserva.getNombreEstudiante())
                    .description(reserva.getAsuntoReserva())
                    .startDate(reserva.getFechaEntrada())
                    .endDate(reserva.getFechaSalida())
                    .borderColor(borderColor)
                    .build();
            eventModel.addEvent(event);
        }
    }

    public void addEvent() {
        String borderColor = generarColorAleatorio();

        DefaultScheduleEvent<?> newEvent = DefaultScheduleEvent.builder()
                .title(newEventTitle)
                .description(newEventDescription)
                .startDate(newEventStartDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .endDate(newEventEndDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .borderColor(borderColor)
                .build();

        eventModel.addEvent(newEvent);

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

    public void onEventSelect(SelectEvent<ScheduleEvent<?>> selectEvent) {
        if (selectEvent != null && selectEvent.getObject() != null) {
            event = selectEvent.getObject();
            selectedEvent = null; // Reiniciar la reserva seleccionada

            String selectedEventTitle = event.getTitle();
            String selectedEventDescription = event.getDescription();

            for (Reserva reserva : reservas) {
                if (reserva.getNombreEstudiante().equals(selectedEventTitle) &&
                        reserva.getAsuntoReserva().equals(selectedEventDescription)) {
                    selectedEvent = reserva;
                    break;
                }
            }
        }
    }




    public void onDateSelect(SelectEvent<LocalDateTime> selectEvent) {
        LocalDateTime selectedDate = selectEvent.getObject();
        LocalDateTime endDate = selectedDate.plusMinutes(30); // Añadir 30 minutos al inicio para obtener el final

        DefaultScheduleEvent<?> newEvent = DefaultScheduleEvent.builder()
                .startDate(selectedDate)
                .endDate(endDate)
                .build();
        eventModel.addEvent(newEvent);
    }

    public String guardarReserva() {
        if (reservaActual.getUtilizaPizarra() == null) {
            reservaActual.setUtilizaPizarra(false);
        }
        if (reservaActual.getUtilizaProyector() == null) {
            reservaActual.setUtilizaProyector(false);
        }
        if (reservaActual.getUtilizaComputadora() == null) {
            reservaActual.setUtilizaComputadora(false);
        }

        int totalPersonasReservadas = reservaDAO.obtenerTotalPersonasReservadas(
                reservaActual.getFechaEntrada(), reservaActual.getFechaSalida());

        if (totalPersonasReservadas + reservaActual.getCantidadPersonas() > 15) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "La cantidad de personas excede la capacidad máxima de la sala.");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return null;
        }

        if (reservaActual.getUtilizaPizarra() && !reservaDAO.hayPizarraDisponible(
                reservaActual.getFechaEntrada(), reservaActual.getFechaSalida())) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "La pizarra no está disponible en el horario seleccionado.");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return null;
        }

        if (reservaActual.getUtilizaProyector() && !reservaDAO.hayProyectorDisponible(
                reservaActual.getFechaEntrada(), reservaActual.getFechaSalida())) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "El proyector no está disponible en el horario seleccionado.");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return null;
        }

        if (reservaActual.getUtilizaComputadora() && !reservaDAO.hayComputadoraDisponible(
                reservaActual.getFechaEntrada(), reservaActual.getFechaSalida())) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "La computadora no está disponible en el horario seleccionado.");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return null;
        }

        reservaDAO.guardar(reservaActual);
        reservaActual = new Reserva();
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Reserva guardada con éxito", null);
        FacesContext.getCurrentInstance().addMessage(null, message);

        return "success";
    }


    public ScheduleModel getEventModel() {
        if (eventModel == null) {
            eventModel = new DefaultScheduleModel();
        }
        return eventModel;
    }

    public String generarColorAleatorio() {
        Random rand = new Random();
        int r = rand.nextInt(256);
        int g = rand.nextInt(256);
        int b = rand.nextInt(256);
        return String.format("#%02x%02x%02x", r, g, b);
    }
}
