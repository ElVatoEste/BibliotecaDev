package beans;

import entity.Archivado;
import entity.Reserva;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import service.ArchivadoDAO;

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
public class ReporteMensualArchivadoBean implements Serializable {

    @Inject
    private ArchivadoDAO archivadoDAO;


    @Getter
    @Setter
    private List<Archivado> archivados;

    private LocalDate fechaActual;

    public ReporteMensualArchivadoBean() {
        // Inicializa con el mes y año actuales
        this.fechaActual = LocalDate.now();
    }

    @PostConstruct
    private void actualizarArchivados() {

        int mesActual = fechaActual.getMonthValue();
        int anioActual = fechaActual.getYear();

        // Crear un pool de hilos con un número de hilos igual al número de núcleos disponibles
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        try {
            // Ejecutar la carga de reservas en un hilo separado
            executorService.submit(() -> {
                // Cargar las reservas para el mes actual
                List<Archivado> archivadosCargados = archivadoDAO.obtenerArchivadosMensuales(mesActual, anioActual);

                // Bloquear el acceso a la lista de reservas para evitar problemas de concurrencia
                synchronized (this) {
                    this.archivados = archivadosCargados;
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
        actualizarArchivados();
    }

    public void retrocederMes() {
        // Retroceder un mes
        fechaActual = fechaActual.minusMonths(1);
        actualizarArchivados();
    }

    public String getNombreMesActual() {
        return fechaActual.getMonth().getDisplayName(TextStyle.FULL, new Locale("es")).toUpperCase();
    }


    public void irAgenda() {
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("reserva.xhtml");
        } catch (IOException e) {
            // Manejar cualquier excepción de redirección
            e.printStackTrace();
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Ocurrió un error al intentar redirigir la vista de archivados.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    public void irReporte() {
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("reporteMensual.xhtml");
        } catch (IOException e) {
            // Manejar cualquier excepción de redirección
            e.printStackTrace();
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Ocurrió un error al intentar redirigir la vista de archivados.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    public void eliminarArchivado(Archivado archivado) {
        // Verificar si el archivado no es nulo
        if (archivado != null) {
            // Lógica para eliminar el archivado
            try {
                // Llamar al método en el DAO para eliminar el archivado de la base de datos
                archivadoDAO.eliminar(archivado);
                // Eliminar el archivado de la lista local de archivados
                archivados.remove(archivado);

                // Mostrar un mensaje de éxito o cualquier acción adicional
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "El archivado ha sido eliminado.");
                FacesContext.getCurrentInstance().addMessage(null, message);
            } catch (Exception e) {
                // Manejar cualquier excepción que pueda ocurrir durante la eliminación del archivado
                e.printStackTrace();
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Ocurrió un error al eliminar el archivado.");
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
        } else {
            // El archivado es nulo, mostrar un mensaje de advertencia
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", "No se ha seleccionado ningún archivado para eliminar.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    public String getNombreArchivoReporte() {
        String nombreMes = fechaActual.getMonth().getDisplayName(TextStyle.FULL, new Locale("es"));
        int anio = fechaActual.getYear();
        return "ReporteMensual_Archivado_" + nombreMes + "_" + anio;
    }
}
