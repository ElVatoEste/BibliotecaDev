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

@Named("CargarReserva")
@ViewScoped
@Getter
@Setter
public class CargaReservaBean implements Serializable {

    @Inject
    private ReservaDAO reservaDAO;

    private Reserva reservaActual;
    private Long idReservaActual;

    @PostConstruct
    public void init() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String idReservaString = params.get("id");
        System.out.println("ID de reserva recibido: " + idReservaString);

        if (idReservaString != null && !idReservaString.isEmpty()) {
            try {
                idReservaActual = Long.parseLong(idReservaString);
                reservaActual = reservaDAO.buscarPorId(Reserva.class, idReservaActual);
                System.out.println("Reserva encontrada: " + reservaActual);
            } catch (NumberFormatException e) {
                System.out.println("El ID de la reserva no es válido: " + idReservaString);
            }
        } else {
            System.out.println("No se proporcionó ningún ID de reserva en la URL.");
        }
    }

    public void guardarCambios() {

        try {
            if (idReservaActual != null) {

                reservaDAO.update(reservaActual);

                FacesContext.getCurrentInstance().getExternalContext().redirect("reporteMensual.xhtml");
            } else {
                System.out.println("No se puede guardar porque el ID de la reserva es null.");
            }
        } catch (Exception e) {
            System.out.println("Ocurrió un error al guardar los cambios en la reserva.");
            e.printStackTrace();
        }
    }

    public void cancelarEdicion() {
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("reporteMensual.xhtml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
