package beans;

import entity.*;
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
import service.EnvioCorreoDAO;
import service.ReservaDAO;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Named
@RequestScoped
@Getter
@Setter
public class ReservaBean implements Serializable {

    @Inject
    private ReservaDAO reservaDAO;

    @Inject
    private EnvioCorreoDAO EnvioDAO;

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

        // Crear un pool de hilos con un número de hilos igual al número de núcleos disponibles
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        for (Reserva reserva : reservas) {
            executorService.submit(() -> {
                String borderColor = generarColorAleatorio();
                DefaultScheduleEvent<?> event = DefaultScheduleEvent.builder()
                        .title(reserva.getNombreEstudiante())
                        .description(reserva.getAsuntoReserva())
                        .startDate(reserva.getFechaEntrada())
                        .endDate(reserva.getFechaSalida())
                        .borderColor(borderColor)
                        .build();
                // Synchronized para evitar conflictos en la modificación de eventModel
                synchronized (eventModel) {
                    eventModel.addEvent(event);
                }
            });
        }

        // Finalizar el pool de hilos
        executorService.shutdown();
        try {
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

        if (reservaActual.getCorreo() == null || !reservaActual.getCorreo().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Correo electrónico no válido.");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return null;
        }

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
        int capacidadMaxima = 16;
        int espacioDisponible = capacidadMaxima - totalPersonasReservadas;

        if (reservaActual.getCantidadPersonas() > espacioDisponible) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "La cantidad de personas excede la capacidad máxima de la sala. Espacio disponible: " + totalPersonasReservadas);
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

        if (reservaActual.getFechaEntrada().isAfter(reservaActual.getFechaSalida())) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "La fecha de entrada no puede ser posterior a la fecha de salida.");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return null;
        }


        reservaActual.setAsistencia(Reserva.AsistenciaEstado.PENDIENTE);

        reservaDAO.guardar(reservaActual);
        EnvioDAO.enviarCorreoExitoso(reservaActual.getCorreo(), reservaActual.getFechaEntrada(), reservaActual.getFechaSalida());

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

    public void irReporte() {
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("reporteMensual.xhtml");
        } catch (IOException e) {
            // Manejar cualquier excepción de redirección
            e.printStackTrace();
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Ocurrió un error al intentar redirigir al reporte mensual.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

}