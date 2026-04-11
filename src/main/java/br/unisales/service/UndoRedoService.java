package br.unisales.service;

import br.unisales.Enumeration.StatusExemplarEnum;
import br.unisales.database.table.Emprestimo;
import br.unisales.database.table.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

public class UndoRedoService {

    private final EntityManagerFactory entityManagerFactory;

    public UndoRedoService(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public void desfazerEmprestimo() {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();

        try {
            Emprestimo ultimo = entityManager.createQuery(
                    "SELECT e FROM Emprestimo e ORDER BY e.id DESC",
                    Emprestimo.class)
                    .setMaxResults(1).getSingleResult();

            entityManager.getTransaction().begin();
            ultimo.getExemplar().setStatus(StatusExemplarEnum.DISPONIVEL);
            entityManager.remove(entityManager.merge(ultimo));
            entityManager.getTransaction().commit();

            System.out.println("Empréstimo desfeito: ID " + ultimo.getId());

        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) entityManager.getTransaction().rollback();
            System.out.println("Erro ao desfazer empréstimo: " + e.getMessage());
        } finally {
            entityManager.close();
        }
    }

    public void refazerEmprestimo() {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();

        try {
            Emprestimo ultimo = entityManager.createQuery(
                    "SELECT e FROM Emprestimo e ORDER BY e.id DESC",
                    Emprestimo.class)
                    .setMaxResults(1).getSingleResult();

            System.out.println("Último empréstimo: ID " + ultimo.getId() +
                    " | Usuário: " + ultimo.getUsuario().getNome() +
                    " | Data: " + ultimo.getDataEmprestimo());

        } catch (Exception e) {
            System.out.println("Erro ao refazer empréstimo: " + e.getMessage());
        } finally {
            entityManager.close();
        }
    }

    public void desfazerDevolucao() {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();

        try {
            Emprestimo ultimo = entityManager.createQuery(
                    "SELECT e FROM Emprestimo e WHERE e.dataDevolucao IS NOT NULL ORDER BY e.id DESC",
                    Emprestimo.class)
                    .setMaxResults(1).getSingleResult();

            entityManager.getTransaction().begin();
            ultimo.setDataDevolucao(null);
            ultimo.getExemplar().setStatus(StatusExemplarEnum.EMPRESTADO);
            entityManager.merge(ultimo);
            entityManager.getTransaction().commit();

            System.out.println("Devolução desfeita: ID " + ultimo.getId());

        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) entityManager.getTransaction().rollback();
            System.out.println("Erro ao desfazer devolução: " + e.getMessage());
        } finally {
            entityManager.close();
        }
    }

    public void refazerDevolucao() {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();

        try {
            Emprestimo ultimo = entityManager.createQuery(
                    "SELECT e FROM Emprestimo e WHERE e.dataDevolucao IS NOT NULL ORDER BY e.dataDevolucao DESC",
                    Emprestimo.class)
                    .setMaxResults(1).getSingleResult();

            System.out.println("Última devolução: ID " + ultimo.getId() +
                    " | Usuário: " + ultimo.getUsuario().getNome() +
                    " | Data devolução: " + ultimo.getDataDevolucao());

        } catch (Exception e) {
            System.out.println("Erro ao refazer devolução: " + e.getMessage());
        } finally {
            entityManager.close();
        }
    }

    public void desfazerCadastroUsuario() {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();

        try {
            Usuario ultimo = entityManager.createQuery(
                    "SELECT u FROM Usuario u ORDER BY u.id DESC",
                    Usuario.class)
                    .setMaxResults(1).getSingleResult();

            entityManager.getTransaction().begin();
            entityManager.remove(entityManager.merge(ultimo));
            entityManager.getTransaction().commit();

            System.out.println("Cadastro de usuário desfeito: " + ultimo.getNome());

        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) entityManager.getTransaction().rollback();
            System.out.println("Erro ao desfazer cadastro de usuário: " + e.getMessage());
        } finally {
            entityManager.close();
        }
    }

    public void refazerCadastroUsuario() {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();

        try {
            Usuario ultimo = entityManager.createQuery(
                    "SELECT u FROM Usuario u ORDER BY u.id DESC",
                    Usuario.class)
                    .setMaxResults(1).getSingleResult();

            System.out.println("Último usuário cadastrado: " + ultimo.getNome() +
                    " | Tipo: " + ultimo.getTipo() +
                    " | Email: " + ultimo.getEmail());

        } catch (Exception e) {
            System.out.println("Erro ao refazer cadastro de usuário: " + e.getMessage());
        } finally {
            entityManager.close();
        }
    }

    public void desfazerRenovar() {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();

        try {
            Emprestimo ultimo = entityManager.createQuery(
                    "SELECT e FROM Emprestimo e WHERE e.dataDevolucao IS NULL ORDER BY e.dataPrevista DESC",
                    Emprestimo.class)
                    .setMaxResults(1).getSingleResult();

            entityManager.getTransaction().begin();
            ultimo.setDataDevolucaoPrevista(ultimo.getDataEmprestimo().plusDays(14));
            entityManager.merge(ultimo);
            entityManager.getTransaction().commit();

            System.out.println("Renovação desfeita: ID " + ultimo.getId() +
                    " | Nova data prevista: " + ultimo.getDataDevolucaoPrevista());

        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) entityManager.getTransaction().rollback();
            System.out.println("Erro ao desfazer renovação: " + e.getMessage());
        } finally {
            entityManager.close();
        }
    }

    public void refazerRenovar() {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();

        try {
            Emprestimo ultimo = entityManager.createQuery(
                    "SELECT e FROM Emprestimo e WHERE e.dataDevolucao IS NULL ORDER BY e.dataPrevista DESC",
                    Emprestimo.class)
                    .setMaxResults(1).getSingleResult();

            System.out.println("Última renovação: ID " + ultimo.getId() +
                    " | Usuário: " + ultimo.getUsuario().getNome() +
                    " | Data prevista: " + ultimo.getDataDevolucaoPrevista());

        } catch (Exception e) {
            System.out.println("Erro ao refazer renovação: " + e.getMessage());
        } finally {
            entityManager.close();
        }
    }
}