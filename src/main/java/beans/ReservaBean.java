package beans;

import entity.Reserva;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import service.ReservaDAO;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleModel;
import service.ReservaDAOImpl;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Named
@RequestScoped
@Data
public class ReservaBean implements Serializable {

    @Inject
    @Named ("ReservaDAOImpl")
    private ReservaDAO reservaDAO;

    private Reserva reservaActual;
    private List<Reserva> reservas;
    private ScheduleModel scheduleModel;
    private LocalDateTime fechaMinima;

    @PostConstruct
    public void init() {
        reservaActual = new Reserva();

        reservas = new ArrayList<>();

        Reserva reserva1 = new Reserva();
        reserva1.setCif(123456789L);
        reserva1.setDiaReserva(new Date());
        reserva1.setFechaEntrada(new Date());
        reserva1.setFechaSalida(new Date());
        reserva1.setCantidadPersonas(5);
        reserva1.setAsuntoReserva("Reunión de equipo");
        reserva1.setUtilizaPizarra(true);
        reserva1.setUtilizaProyector(true);
        reserva1.setUtilizaComputadora(false);
        reservas.add(reserva1);

        
        scheduleModel = new DefaultScheduleModel();
        
        fechaMinima = LocalDateTime.now();
        
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
