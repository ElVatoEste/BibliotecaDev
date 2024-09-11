package service;

import entity.Archivado;
import entity.Reserva;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.TypedQuery;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

@Named("ReservaDAOImpl")
public class ReservaDAOImpl implements ReservaDAO, Serializable {

    private final EntityManager em;

    public ReservaDAOImpl() {
        em = EntityManagerAdmin.getInstance();
    }

    private void ejecutarDentroDeTransaccion(Consumer<EntityManager> operacion) {
        try {
            em.getTransaction().begin();
            operacion.accept(em);
            em.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        }
    }

    @Override
    public <T> List<T> obtenerTodas(String namedQuery, Class<T> clazz) {
        try {
            TypedQuery<T> query = em.createNamedQuery(namedQuery, clazz);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public <T> List<T> get(String namedQuery, Class<T> clazz, Object... params) {
        try {
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
    public <T> T update(T entity) {
        try {
            em.getTransaction().begin();
            T entityUpdate = em.merge(entity);
            em.getTransaction().commit();
            return entityUpdate;
        } catch (Exception e) {
            e.printStackTrace();
            em.getTransaction().rollback();
            return null;
        }
    }

    @Override
    public <T> void insert(T entity) {
        ejecutarDentroDeTransaccion(em -> em.persist(entity));
    }

    @Override
    public <T> void guardar(T entity) {
        ejecutarDentroDeTransaccion(em -> {
            if (em.contains(entity)) {
                em.merge(entity);
            } else {
                em.persist(entity);
            }
        });
    }

    @Override
    public <T> void eliminar(T entity) {
        ejecutarDentroDeTransaccion(em -> em.remove(em.merge(entity)));
    }

    @Override
    public <T, ID> T buscarPorId(Class<T> clazz, ID id) {
        try {
            return em.find(clazz, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int obtenerTotalPersonasReservadas(LocalDateTime inicio, LocalDateTime fin) {
        try {
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

    @Override
    public void archivarReserva(Long idReserva, Archivado.AsistenciaEstado estadoAsistencia) {
        ejecutarDentroDeTransaccion(em -> {
            Reserva reserva = em.find(Reserva.class, idReserva);
            if (reserva == null) {
                throw new EntityNotFoundException("Reserva no encontrada con el ID: " + idReserva);
            }

            Archivado archivado = crearArchivadoDesdeReserva(reserva, estadoAsistencia);
            em.persist(archivado);
            em.remove(reserva);
        });
    }

    private Archivado crearArchivadoDesdeReserva(Reserva reserva, Archivado.AsistenciaEstado estadoAsistencia) {
        Archivado archivado = new Archivado();
        archivado.setNombreEstudiante(reserva.getNombreEstudiante());
        archivado.setCif(reserva.getCif());
        archivado.setCorreo(reserva.getCorreo());
        archivado.setAsuntoReserva(reserva.getAsuntoReserva());
        archivado.setCantidadPersonas(reserva.getCantidadPersonas());
        archivado.setFechaEntrada(reserva.getFechaEntrada());
        archivado.setFechaSalida(reserva.getFechaSalida());
        archivado.setUtilizaPizarra(reserva.getUtilizaPizarra());
        archivado.setUtilizaProyector(reserva.getUtilizaProyector());
        archivado.setUtilizaComputadora(reserva.getUtilizaComputadora());
        archivado.setAsistencia(estadoAsistencia);
        return archivado;
    }

    @Override
    public List<Reserva> obtenerReservasMensuales(int mesActual, int anioActual) {
        try {
            String queryStr = "SELECT r FROM Reserva r WHERE MONTH(r.fechaEntrada) = :mes AND YEAR(r.fechaEntrada) = :anio";
            TypedQuery<Reserva> query = em.createQuery(queryStr, Reserva.class);
            query.setParameter("mes", mesActual);
            query.setParameter("anio", anioActual);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean hayPizarraDisponible(LocalDateTime fechaEntrada, LocalDateTime fechaSalida) {
        List<Reserva> reservasEnHorario = obtenerReservasEnHorario(fechaEntrada, fechaSalida);
        return reservasEnHorario == null || reservasEnHorario.stream().noneMatch(Reserva::getUtilizaPizarra);
    }

    @Override
    public boolean hayProyectorDisponible(LocalDateTime fechaEntrada, LocalDateTime fechaSalida) {
        List<Reserva> reservasEnHorario = obtenerReservasEnHorario(fechaEntrada, fechaSalida);
        return reservasEnHorario == null || reservasEnHorario.stream().noneMatch(Reserva::getUtilizaProyector);
    }

    @Override
    public boolean hayComputadoraDisponible(LocalDateTime fechaEntrada, LocalDateTime fechaSalida) {
        List<Reserva> reservasEnHorario = obtenerReservasEnHorario(fechaEntrada, fechaSalida);
        return reservasEnHorario == null || reservasEnHorario.stream().noneMatch(Reserva::getUtilizaComputadora);
    }

    private List<Reserva> obtenerReservasEnHorario(LocalDateTime fechaEntrada, LocalDateTime fechaSalida) {
        try {
            String queryStr = "SELECT r FROM Reserva r WHERE (r.fechaEntrada < :fin AND r.fechaSalida > :inicio)";
            TypedQuery<Reserva> query = em.createQuery(queryStr, Reserva.class);
            query.setParameter("inicio", fechaEntrada);
            query.setParameter("fin", fechaSalida);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
