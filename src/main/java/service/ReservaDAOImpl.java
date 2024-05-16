package service;

import entity.Reserva;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;

@Named("ReservaDAOImpl")
public class ReservaDAOImpl implements ReservaDAO {

    @Override
    public <T> List<T> obtenerTodas(String namedQuery, Class<T> clazz) {
        EntityManager em = EntityManagerAdmin.getInstance();
        try {
            TypedQuery<T> query = em.createNamedQuery(namedQuery, clazz);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            em.close();
        }
    }

    @Override
    public <T> List<T> get(String namedQuery, Class<T> clazz, Object... params) {
        try (EntityManager em = EntityManagerAdmin.getInstance()) {
            TypedQuery<T> query = em.createNamedQuery(namedQuery, clazz);
            if (params != null && params.length > 0) {
                if (params.length % 2 != 0) {
                    throw new IllegalArgumentException("La cantidad de parámetros debe ser par.");
                }
                for (int i = 0; i < params.length; i += 2) {
                    String paramName = (String) params[i];
                    Object paramValue = params[i + 1];
                    query.setParameter(paramName, paramValue);
                }
            }
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public <T> T Actualizar(T entity) {
        EntityManager em = EntityManagerAdmin.getInstance();
        T entityUpdate = null;
        try {
            em.getTransaction().begin();
            entityUpdate = em.merge(entity);
            em.flush();
            em.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
        return entityUpdate;
    }

    @Override
    public <T> T guardar(T entity) {
        EntityManager em = EntityManagerAdmin.getInstance();
        try {
            em.getTransaction().begin();
            if (em.contains(entity)) {
                entity = em.merge(entity);
            } else {
                em.persist(entity);
            }
            em.getTransaction().commit();
            return entity;
        } catch (Exception e) {
            e.printStackTrace();
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return null;
        } finally {
            em.close();
        }
    }

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
    public <T, ID> T buscarPorId(Class<T> clazz, ID id) {
        EntityManager em = EntityManagerAdmin.getInstance();
        try {
            T entity = em.find(clazz, id);
            return entity;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            em.close();
        }
    }

    @Override
    public <T> void insert(T entity) {
        EntityManager em = EntityManagerAdmin.getInstance();
        try {
            em.getTransaction().begin();
            em.persist(entity);
            em.flush();
            em.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }


    public int obtenerTotalPersonasReservadas(LocalDateTime inicio, LocalDateTime fin) {
        try (EntityManager em = EntityManagerAdmin.getInstance()) {
            String queryStr = "SELECT SUM(r.cantidadPersonas) FROM Reserva r WHERE " +
                    "(r.fechaEntrada < :fin AND r.fechaSalida > :inicio)";
            TypedQuery<Long> query = em.createQuery(queryStr, Long.class);
            query.setParameter("inicio", inicio);
            query.setParameter("fin", fin);
            Long result = query.getSingleResult();
            return result != null ? result.intValue() : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }



    public boolean hayPizarraDisponible(LocalDateTime fechaEntrada, LocalDateTime fechaSalida) {
        List<Reserva> reservasEnHorario = obtenerReservasEnHorario(fechaEntrada, fechaSalida);
        if (reservasEnHorario == null || reservasEnHorario.isEmpty()) return true; // Si no hay reservas en el horario, el recurso está disponible
        for (Reserva reserva : reservasEnHorario) {
            if (reserva.getUtilizaPizarra()) {
                return false; // La pizarra está ocupada en este horario
            }
        }
        return true; // La pizarra está disponible en este horario
    }

    @Override
    public List<Reserva> obtenerReservasMensuales(int mesActual, int anioActual) {
        EntityManager em = EntityManagerAdmin.getInstance();
        try {
            String queryStr = "SELECT r FROM Reserva r WHERE MONTH(r.fechaEntrada) = :mes AND YEAR(r.fechaEntrada) = :anio";
            TypedQuery<Reserva> query = em.createQuery(queryStr, Reserva.class);
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



    public boolean hayProyectorDisponible(LocalDateTime fechaEntrada, LocalDateTime fechaSalida) {
        List<Reserva> reservasEnHorario = obtenerReservasEnHorario(fechaEntrada, fechaSalida);
        if (reservasEnHorario == null || reservasEnHorario.isEmpty()) return true; // Si no hay reservas en el horario, el recurso está disponible
        for (Reserva reserva : reservasEnHorario) {
            if (reserva.getUtilizaProyector()) {
                return false; // El proyector está ocupado en este horario
            }
        }
        return true; // El proyector está disponible en este horario
    }

    public boolean hayComputadoraDisponible(LocalDateTime fechaEntrada, LocalDateTime fechaSalida) {
        List<Reserva> reservasEnHorario = obtenerReservasEnHorario(fechaEntrada, fechaSalida);
        if (reservasEnHorario == null || reservasEnHorario.isEmpty()) return true; // Si no hay reservas en el horario, el recurso está disponible
        for (Reserva reserva : reservasEnHorario) {
            if (reserva.getUtilizaComputadora()) {
                return false; // La computadora está ocupada en este horario
            }
        }
        return true; // La computadora está disponible en este horario
    }

    private List<Reserva> obtenerReservasEnHorario(LocalDateTime fechaEntrada, LocalDateTime fechaSalida) {
        EntityManager em = EntityManagerAdmin.getInstance();
        try {
            String queryStr = "SELECT r FROM Reserva r WHERE (r.fechaEntrada < :fin AND r.fechaSalida > :inicio)";
            TypedQuery<Reserva> query = em.createQuery(queryStr, Reserva.class);
            query.setParameter("inicio", fechaEntrada);
            query.setParameter("fin", fechaSalida);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            em.close();
        }
    }
}
