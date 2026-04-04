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
                    "SELECT titulo l, COUNT(e) " +
                            "FROM Emprestimo e " +
                            "JOIN Exemplar ex ON e.exemplarId = ex.id " +
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

}
