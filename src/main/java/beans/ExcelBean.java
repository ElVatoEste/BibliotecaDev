package beans;

import entity.Reserva;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.poi.ss.usermodel.*;

import java.io.Serializable;
import java.util.List;

@Named
@RequestScoped
public class ExcelBean implements Serializable {

    @PersistenceContext(unitName = "default")
    private EntityManager entityManager;

    private List<Reserva> reservas;

    @PostConstruct
    public void init() {
        loadReservasFromDatabase();
    }

    public void preProcessXLSX(Object document) {
        Workbook wb = (Workbook) document;
        Sheet sheet = wb.getSheetAt(0);
        for (Row row : sheet) {
            for (Cell cell : row) {
                cell.setCellValue(cell.getStringCellValue());
            }
        }
    }

    private void loadReservasFromDatabase() {
        reservas = entityManager.createNamedQuery("Reserva.findAll", Reserva.class).getResultList();
    }
}
