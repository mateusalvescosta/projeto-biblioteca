package br.unisales.manager_factory;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class ManagerFactory {
    private EntityManagerFactory emf;

    public ManagerFactory(String persistenceUnit) {
        this.emf = Persistence.createEntityManagerFactory(persistenceUnit);
    }

    public EntityManagerFactory get() {
        return this.emf;
    }

    public void close() {
        this.emf.close();
    }

}
