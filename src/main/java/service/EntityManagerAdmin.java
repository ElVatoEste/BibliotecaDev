package service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.io.Serializable;

public class EntityManagerAdmin implements Serializable {
    private static final long serialVersionUID = 1L;

    private static EntityManager instance;
    private static final String PERSISTENCE_UNIT = "default";

    public static EntityManager getInstance() {
        if (instance == null) {
            EntityManagerFactory emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
            return emf.createEntityManager();
        }
        return instance;
    }

    private EntityManagerAdmin() {
    }
}
