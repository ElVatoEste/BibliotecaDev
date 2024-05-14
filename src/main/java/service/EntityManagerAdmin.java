package service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;

import javax.xml.stream.events.EntityReference;
import java.util.Date;

public class EntityManagerAdmin {
    private static EntityManager instance;
    private static final String PERSISTENCE_UNIT = "default";
    public static EntityManager getInstance() {
        if (instance == null) {
            EntityManagerFactory emf = Persistence
                    .createEntityManagerFactory(PERSISTENCE_UNIT);
            return emf.createEntityManager();
        }
        return instance;
    }

    private EntityManagerAdmin(){}
}

