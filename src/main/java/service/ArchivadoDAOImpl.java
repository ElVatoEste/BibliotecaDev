package service;

import entity.Archivado;
import entity.Reserva;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.io.Serializable;
import java.util.List;

@Named("ArchivadoDAOImpl")
public class ArchivadoDAOImpl implements ArchivadoDAO, Serializable {

    @Override
    public <T> void eliminar(T entity) {
        EntityManager em = EntityManagerAdmin.getInstance();
        try {
            em.getTransaction().begin();
            em.remove(em.merge(entity));
            em.flush();
            em.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Archivado> obtenerArchivadosMensuales(int mesActual, int anioActual) {
        EntityManager em = EntityManagerAdmin.getInstance();
        try {
            String queryStr = "SELECT r FROM Archivado r WHERE MONTH(r.fechaEntrada) = :mes AND YEAR(r.fechaEntrada) = :anio ORDER BY r.fechaEntrada ASC";
            TypedQuery<Archivado> query = em.createQuery(queryStr, Archivado.class);
            query.setParameter("mes", mesActual);
            query.setParameter("anio", anioActual);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            em.close();
        }
    }

}
