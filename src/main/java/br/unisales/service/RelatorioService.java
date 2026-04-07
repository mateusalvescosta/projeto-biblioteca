package br.unisales.service;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

public class RelatorioService {

    private final EntityManagerFactory entityManagerFactory;

    public RelatorioService(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public void topMaisEmprestados() {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();

        try {
            List<Object[]> resultado = entityManager.createQuery(
                    "SELECT l.titulo, COUNT(e) " +
                            "FROM Emprestimo e " +
                            "JOIN e.exemplar ex " +
                            "JOIN ex.livro l " +
                            "GROUP BY l.titulo " +
                            "ORDER BY COUNT(e) DESC",
                    Object[].class)
                    .setMaxResults(10)
                    .getResultList();

            if (resultado.isEmpty()) {
                System.out.println("Nenhum empréstimo registrado.");
                return;
            }

            System.out.println("=== TOP LIVROS MAIS EMPRESTADOS ===");
            int posicao = 1;
            for (Object[] linha : resultado) {
                System.out.println("#" + posicao + " " + linha[0] + " | Empréstimos: " + linha[1]);
                posicao++;
            }

        }

        catch (Exception e) {
            Throwable causa = e;
            while (causa.getCause() != null)
                causa = causa.getCause();
            System.out.println("Erro ao gerar relatório: " + causa.getMessage());
        }

        finally {
            entityManager.close();
        }

    }

    public void emAtraso() {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();

        try {
            List<Object[]> resultado = entityManager.createQuery(
                    "SELECT u.nome, l.titulo " +
                            "FROM Emprestimo e " +
                            "JOIN e.usuario u " +
                            "JOIN e.exemplar ex " +
                            "JOIN ex.livro l " +
                            "WHERE e.dataDevolucao IS NULL AND e.dataDevolucaoPrevista < CURRENT_DATE " +
                            "ORDER BY u.nome",
                    Object[].class)
                    .setMaxResults(10)
                    .getResultList();

            if (resultado.isEmpty()) {
                System.out.println("Nenhum cliente com empréstimos em atraso.");
                return;
            }

            System.out.println("=== EMPRÉSTIMOS EM ATRASO ===");
            for (Object[] linha : resultado) {
                System.out.println("Usuário: " + linha[0] + " | Livro: " + linha[1]);
            }

        }

        catch (Exception e) {
            Throwable causa = e;
            while (causa.getCause() != null)
                causa = causa.getCause();
            System.out.println("Erro ao gerar relatório de atrasos: " + causa.getMessage());
        }

        finally {
            entityManager.close();
        }
    }

}
