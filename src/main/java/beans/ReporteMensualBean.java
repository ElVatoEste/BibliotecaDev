package beans;

import entity.Archivado;
import entity.Reserva;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DialogFrameworkOptions;
import service.EnvioCorreoDAO;
import service.ReservaDAO;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


@Named
@ViewScoped
public class ReporteMensualBean implements Serializable {

    @Inject
    private ReservaDAO reservaDAO;

    @Inject
    private EnvioCorreoDAO EnvioDAO;

    @Getter
    @Setter
    private List<Reserva> reservas;

    @Getter
    @Setter
    private List<Archivado> archivados;


    @Getter
    @Setter
    private Reserva selectedReserva;

    private LocalDate fechaActual;



    public ReporteMensualBean() {
        // Inicializa con el mes y año actuales
        this.fechaActual = LocalDate.now();
    }

    @PostConstruct
    private void actualizarReservas() {
        int mesActual = fechaActual.getMonthValue();
        int anioActual = fechaActual.getYear();

        // Crear un pool de hilos con un número de hilos igual al número de núcleos disponibles
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        try {
            // Ejecutar la carga de reservas en un hilo separado
            executorService.submit(() -> {
                // Cargar las reservas para el mes actual
                List<Reserva> reservasCargadas = reservaDAO.obtenerReservasMensuales(mesActual, anioActual);

                // Bloquear el acceso a la lista de reservas para evitar problemas de concurrencia
                synchronized (this) {
                    this.reservas = reservasCargadas;
                }
            });
        } finally {
            // Finalizar el pool de hilos
            executorService.shutdown();
            try {
                executorService.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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
        if (selectedReserva != null) {
            try {
                if (asistio) {
                    selectedReserva.setAsistencia(Reserva.AsistenciaEstado.ASISTENCIA);
                    reservaDAO.archivarReserva(selectedReserva.getIdReserva(), Archivado.AsistenciaEstado.ASISTENCIA);
                    eliminarReserva(selectedReserva);
                    FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Asistencia Marcada", "El estudiante asistió a la reserva.");
                    FacesContext.getCurrentInstance().addMessage(null, message);
                } else {
                    EnvioDAO.enviarCorreoCancelacion(selectedReserva.getCorreo());
                    selectedReserva.setAsistencia(Reserva.AsistenciaEstado.INASISTENCIA);
                    reservaDAO.archivarReserva(selectedReserva.getIdReserva(), Archivado.AsistenciaEstado.INASISTENCIA);
                    eliminarReserva(selectedReserva);
                    FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Asistencia Marcada", "El estudiante no asistió a la reserva.");
                    FacesContext.getCurrentInstance().addMessage(null, message);
                }
                actualizarReservas();
            } catch (Exception e) {
                e.printStackTrace();
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Ocurrió un error al procesar la asistencia.");
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
        } else {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", "No se ha seleccionado ninguna reserva.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    public String getNombreArchivoReporte() {
        String nombreMes = fechaActual.getMonth().getDisplayName(TextStyle.FULL, new Locale("es"));
        int anio = fechaActual.getYear();
        return "ReporteMensual_" + nombreMes + "_" + anio;
    }

    public void abrirDialogoAsistencia(Reserva reserva) {
        this.selectedReserva = reserva;
        PrimeFaces.current().executeScript("PF('dlgAsistencia').show();");
    }

    public void irArchivado() {
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("reporteArchivado.xhtml");
        } catch (IOException e) {
            // Manejar cualquier excepción de redirección
            e.printStackTrace();
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Ocurrió un error al intentar redirigir la vista de archivados.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
}
