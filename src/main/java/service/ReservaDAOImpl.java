package service;
import entity.Reserva;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Repository
@Transactional
public class ReservaDAOImpl implements ReservaDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void guardar(Reserva reserva) {
        entityManager.persist(reserva);
    }

    @Override
    public void actualizar(Reserva reserva) {
        entityManager.merge(reserva);
    }

    @Override
    public void eliminar(Long cif) {
        Reserva reserva = buscarPorCif(cif);
        if (reserva != null) {
            entityManager.remove(reserva);
        }
    }

    @Override
    public Reserva buscarPorCif(Long cif) {
        return entityManager.find(Reserva.class, cif);
    }

    @Override
    public List<Reserva> obtenerTodas() {
        TypedQuery<Reserva> query = entityManager.createQuery("SELECT r FROM Reserva r", Reserva.class);
        return query.getResultList();
    }

    @Override
    public boolean hayChoqueDeReservas(Date fechaEntrada, Date fechaSalida) {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(r) FROM Reserva r WHERE " +
                        "(r.fechaEntrada < :fechaSalida AND r.fechaSalida > :fechaEntrada)", Long.class);
        query.setParameter("fechaEntrada", fechaEntrada);
        query.setParameter("fechaSalida", fechaSalida);
        Long count = query.getSingleResult();
        return count > 0;
    }
}
