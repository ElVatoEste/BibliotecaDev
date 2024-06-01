package beans;

import entity.Reserva;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import service.ReservaDAO;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Named
@ViewScoped
public class ReporteMensualBean implements Serializable {

    @Inject
    private ReservaDAO reservaDAO;

    @Getter
    private List<Reserva> reservas;
    private LocalDate fechaActual;

    public ReporteMensualBean() {
        // Inicializa con el mes y año actuales
        this.fechaActual = LocalDate.now();
    }

    @PostConstruct
    private void actualizarReservas() {
        int mesActual = fechaActual.getMonthValue();
        int anioActual = fechaActual.getYear();
        reservas = reservaDAO.obtenerReservasMensuales(mesActual, anioActual);
    }

    public void avanzarMes() {
        // Avanzar un mes
        fechaActual = fechaActual.plusMonths(1);
        actualizarReservas();
    }

    public void retrocederMes() {
        // Retroceder un mes
        fechaActual = fechaActual.minusMonths(1);
        actualizarReservas();
    }

        public String getNombreMesActual() {
            return fechaActual.getMonth().getDisplayName(TextStyle.FULL, new Locale("es")).toUpperCase();
        }

    public void editarReserva(Reserva reserva) {
        // Verificar si la reserva no es nula
        if (reserva != null) {
            // Lógica para editar la reserva
            try {
                // Llamar a la página de edición de reserva pasando el ID de la reserva como parámetro
                FacesContext.getCurrentInstance().getExternalContext().redirect("editarReserva.xhtml?id=" + reserva.getIdReserva());
            } catch (IOException e) {
                // Manejar cualquier excepción de redirección
                e.printStackTrace();
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Ocurrió un error al editar la reserva.");
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
        } else {
            // La reserva es nula, mostrar un mensaje de advertencia
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", "No se ha seleccionado ninguna reserva para editar.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    public void irAgenda() {
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("reserva.xhtml");
        } catch (IOException e) {
            // Manejar cualquier excepción de redirección
            e.printStackTrace();
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Ocurrió un error al intentar redirigir la vista de reservas.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    public void eliminarReserva(Reserva reserva) {
        // Verificar si la reserva no es nula
        if (reserva != null) {
            // Lógica para eliminar la reserva
            try {
                // Llamar al método en el DAO para eliminar la reserva de la base de datos
                reservaDAO.eliminar(reserva);
                // Eliminar la reserva de la lista local de reservas
                reservas.remove(reserva);
                // Mostrar un mensaje de éxito o cualquier acción adicional
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "La reserva ha sido eliminada.");
                FacesContext.getCurrentInstance().addMessage(null, message);
            } catch (Exception e) {
                // Manejar cualquier excepción que pueda ocurrir durante la eliminación de la reserva
                e.printStackTrace();
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Ocurrió un error al eliminar la reserva.");
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
        } else {
            // La reserva es nula, mostrar un mensaje de advertencia
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", "No se ha seleccionado ninguna reserva para eliminar.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
    public void marcarAsistencia(Reserva reserva, boolean asistio) {
        reserva.setAsistencia(asistio);
        reservaDAO.update(reserva);
        if (asistio) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Asistencia Marcada", "El estudiante asistió a la reserva.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        } else {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Asistencia Marcada", "El estudiante no asistió a la reserva.");
            FacesContext.getCurrentInstance().addMessage(null, message);
            }
    }


}
