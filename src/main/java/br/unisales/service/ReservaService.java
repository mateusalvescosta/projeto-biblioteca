package br.unisales.service;

import java.util.List;

import br.unisales.database.table.Livro;
import br.unisales.database.table.Reserva;
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

    /**
     * Cancela uma reserva pelo ID.
     */
    public void cancelarReserva(Long id) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            Reserva reserva = entityManager.find(Reserva.class, id);
            if (reserva == null) {
                System.out.println("Reserva não encontrada.");
                return;
            }
            transaction.begin();
            entityManager.remove(reserva);
            transaction.commit();
            System.out.println("Reserva cancelada com sucesso.");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.out.println("Erro ao cancelar reserva: " + e.getMessage());
        } finally {
            entityManager.close();
        }
    }

    /**
     * Atende a próxima reserva na fila para um livro pelo ISBN.
     */
    public Reserva atenderProximaReserva(String isbn) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            List<Reserva> reservas = entityManager
                    .createQuery(
                            "SELECT r FROM Reserva r WHERE r.isbnLivro = :isbn AND r.status = 'RESERVADO' ORDER BY r.dataReserva ASC",
                            Reserva.class)
                    .setParameter("isbn", isbn)
                    .setMaxResults(1)
                    .getResultList();

            if (reservas.isEmpty()) {
                System.out.println("Nenhuma reserva pendente para o ISBN informado.");
                return null;
            }

            Reserva proxima = reservas.get(0);
            transaction.begin();
            entityManager.remove(proxima);
            transaction.commit();
            System.out.println("Próxima reserva atendida com sucesso.");
            return proxima;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            Throwable causa = e;
            while (causa.getCause() != null) {
                causa = causa.getCause();
            }
            System.out.println("Erro ao atender próxima reserva: " + causa.getMessage());
            return null;
        } finally {
            entityManager.close();
        }
    }

}
