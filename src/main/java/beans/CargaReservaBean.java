package beans;

import entity.Reserva;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import service.ReservaDAO;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named("CargarReserva")
@ViewScoped
@Getter
@Setter
public class CargaReservaBean implements Serializable {

    private static final Logger logger = Logger.getLogger(CargaReservaBean.class.getName());

    @Inject
    private ReservaDAO reservaDAO;

    private Reserva reservaActual;
    private Long idReservaActual;

    @PostConstruct
    public void init() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String idReservaString = params.get("id");

        if (idReservaString != null && !idReservaString.isEmpty()) {
            try {
                idReservaActual = Long.parseLong(idReservaString);
                // Solo cargar reserva si no se ha cargado previamente
                if (idReservaActual != null && reservaActual == null) {
                    reservaActual = reservaDAO.buscarPorId(Reserva.class, idReservaActual);
                    if (reservaActual != null) {
                        logger.log(Level.INFO, "Reserva encontrada: {0}", reservaActual);
                    } else {
                        logger.log(Level.WARNING, "No se encontró ninguna reserva con ID: {0}", idReservaActual);
                    }
                }
            } catch (NumberFormatException e) {
                logger.log(Level.SEVERE, "El ID de la reserva no es válido: {0}", idReservaString);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error al cargar la reserva: ", e);
            }
        } else {
            logger.log(Level.INFO, "No se proporcionó ningún ID de reserva en la URL.");
        }
    }

    public void guardarCambios() {
        if (idReservaActual != null) {
            try {
                // Comprobar si la reserva ha cambiado antes de guardarla
                if (reservaActual != null) {
                    reservaDAO.update(reservaActual);
                    logger.log(Level.INFO, "Cambios guardados correctamente para la reserva con ID: {0}", idReservaActual);
                    // Redirigir solo si es necesario
                    FacesContext.getCurrentInstance().getExternalContext().redirect("reporteMensual.xhtml");
                } else {
                    logger.log(Level.WARNING, "No se puede guardar porque la reserva actual es null.");
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error de redirección después de guardar la reserva: ", e);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Ocurrió un error al guardar los cambios en la reserva: ", e);
            }
        } else {
            logger.log(Level.WARNING, "No se puede guardar porque el ID de la reserva es null.");
        }
    }

    public void cancelarEdicion() {
        try {
            // Redirigir sin cargar toda la página
            FacesContext.getCurrentInstance().getExternalContext().redirect("reporteMensual.xhtml");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error al redirigir al reporte mensual: ", e);
        }
    }
}
