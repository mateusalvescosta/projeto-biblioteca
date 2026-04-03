package br.unisales.service;

import br.unisales.database.table.Livro;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

public class ReservaService {
    
    private final EntityManagerFactory entityManagerFactory;

    public ReservaService(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public void reservarLivro(Livro livro) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();
            if (livro == null) {
                transaction.rollback();
                System.out.println("Livro não encontrado.");
                return;
            }

            entityManager.persist(livro);
            transaction.commit();
            System.out.println("Livro reservado com sucesso.");
        }
        
        catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            Throwable causa = e;
            while (causa.getCause() != null) {
                causa = causa.getCause();
            }

            System.out.println("Erro ao reservar livro: " + causa.getMessage());
            } finally {
                entityManager.close();
            }
    }















}
