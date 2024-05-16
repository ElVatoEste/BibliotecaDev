package beans;

import entity.Reserva;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import service.ReservaDAO;

@Named
@RequestScoped
@Getter
@Setter
public class cargaReserva implements Serializable {

    @Inject
    private ReservaDAO reservaDAO;

    private Reserva reservaActual;

    @PostConstruct
    public void init() {
        // Obtener el ID de la reserva de la URL
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String idReservaString = params.get("id");

        if (idReservaString != null && !idReservaString.isEmpty()) {
            try {
                // Convertir el ID de String a Long
                Long idReserva = Long.parseLong(idReservaString);
                // Buscar la reserva por su ID
                reservaActual = reservaDAO.buscarPorId(Reserva.class, idReserva);
            } catch (NumberFormatException e) {
                // Manejar el caso en que el ID no sea un número válido
                System.out.println("El ID de la reserva no es válido: " + idReservaString);
            }
        } else {
            // Manejar el caso en que no se proporciona ningún ID en la URL
            System.out.println("No se proporcionó ningún ID de reserva en la URL.");
        }

        // Inicializar la reserva actual si no se encontró ninguna en la base de datos
        if (reservaActual == null) {
            reservaActual = new Reserva(); // O crea una nueva instancia según tus necesidades
        }
    }

    public String guardarCambios() {
        // Guardar los cambios realizados en la reserva
        reservaActual = reservaDAO.Actualizar(reservaActual);
        return "reporteMensual.xhtml?faces-redirect=true";
    }

    public String cancelarEdicion() {
        // Redirigir de vuelta a la página de reporte mensual
        return "reporteMensual.xhtml?faces-redirect=true";
    }
}
