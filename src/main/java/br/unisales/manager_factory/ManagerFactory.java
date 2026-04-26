package br.unisales.manager_factory;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

// Classe responsável por criar e gerenciar a fábrica de EntityManager do JPA
public class ManagerFactory {

    // Fábrica de EntityManager configurada com base na persistence-unit informada
    private EntityManagerFactory entityManagerFactory;

    // Inicializa a fábrica com base na persistence-unit definida no persistence.xml
    public ManagerFactory(String persistenceUnit) {
        this.entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnit);
    }

    // Retorna a fábrica de EntityManager para uso nos services
    public EntityManagerFactory get() {
        return this.entityManagerFactory;
    }

    // Encerra a fábrica liberando os recursos de conexão com o banco
    public void close() {
        this.entityManagerFactory.close();
    }

}