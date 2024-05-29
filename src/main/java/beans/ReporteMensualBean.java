package beans;

import entity.Reserva;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import service.ReservaDAO;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Named
@RequestScoped
public class ReporteMensualBean implements Serializable {

    @Inject
    private ReservaDAO reservaDAO;

    private List<Reserva> reservas;

    public List<Reserva> getReservas() {
        // Obtener reservas del mes actual
        LocalDate fechaActual = LocalDate.now();
        int mesActual = fechaActual.getMonthValue();
        int anioActual = fechaActual.getYear();
        reservas = reservaDAO.obtenerReservasMensuales(mesActual, anioActual);
        return reservas;
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

}
