package beans;

import entity.Reserva;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import service.ReservaDAO;

@Named("CargarReserva")
@ViewScoped
@Getter
@Setter
public class CargaReservaBean implements Serializable{

    @Inject
    private ReservaDAO reservaDAO;

    private Reserva reservaActual;
    private Long idReservaActual; // Campo para almacenar el ID de la reserva

    @PostConstruct
        public void init() {
        // Obtener el ID de la reserva de la URL
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String idReservaString = params.get("id");
        System.out.println("ID de reserva recibido: " + idReservaString); // Mensaje de depuración

        if (idReservaString != null && !idReservaString.isEmpty()) {
            try {
                // Convertir el ID de String a Long
                idReservaActual = Long.parseLong(idReservaString);
                // Buscar la reserva por su ID
                reservaActual = reservaDAO.buscarPorId(Reserva.class, idReservaActual);
                System.out.println("Reserva encontrada: " + reservaActual); // Mensaje de depuración
            } catch (NumberFormatException e) {
                // Manejar el caso en que el ID no sea un número válido
                System.out.println("El ID de la reserva no es válido: " + idReservaString);
            }
        } else if (params.containsKey("idReservaLabel")) {
            // Si no hay un ID en la URL, intenta obtenerlo desde el campo de etiqueta
            idReservaActual = Long.parseLong(params.get("idReservaLabel"));
        } else {
            // Manejar el caso en que no se proporciona ningún ID en la URL ni en el campo de etiqueta
            System.out.println("No se proporcionó ningún ID de reserva en la URL ni en el campo de etiqueta.");
        }

        // Inicializar la reserva actual si no se encontró ninguna en la base de datos
        if (reservaActual == null) {
            reservaActual = new Reserva(); // O crea una nueva instancia según tus necesidades
            System.out.println("Inicializando nueva reserva."); // Mensaje de depuración
        }
    }

    public void guardarCambios() {

        try {
            if (idReservaActual != null) {
                // Guardar los cambios realizados en la reserva
                reservaActual = reservaDAO.Actualizar(reservaActual);
                // Redirigir a la página de reporte mensual
            } else {
                System.out.println("No se puede guardar porque el ID de la reserva es null.");
            }
        } catch (Exception e) {
            // Manejar cualquier otra excepción que pueda ocurrir
            System.out.println("Ocurrió un error al guardar los cambios en la reserva.");
            e.printStackTrace();
        }
    }


    public String cancelarEdicion() {
        // Redirigir de vuelta a la página de reporte mensual
        return "reporteMensual.xhtml?faces-redirect=true";
    }
}
