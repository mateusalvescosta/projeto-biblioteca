package br.unisales.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

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
                    "SELECT u.nome, l.titulo, e.dataDevolucaoPrevista " +
                            "FROM Emprestimo e " +
                            "JOIN e.usuario u " +
                            "JOIN e.exemplar ex " +
                            "JOIN ex.livro l " +
                            "WHERE e.dataDevolucao IS NULL AND e.dataDevolucaoPrevista < :hoje " +
                            "ORDER BY u.nome",
                    Object[].class)
                    .setParameter("hoje", LocalDateTime.now())
                    .getResultList();

            if (resultado.isEmpty()) {
                System.out.println("Nenhum usuário com empréstimos em atraso.");
                return;
            }

            System.out.println("=== EMPRÉSTIMOS EM ATRASO ===");
            for (Object[] linha : resultado) {
                LocalDateTime dataPrevista = (LocalDateTime) linha[2];
                long diasAtraso = ChronoUnit.DAYS.between(dataPrevista, LocalDateTime.now());
                System.out
                        .println("Usuário: " + linha[0] + " | Livro: " + linha[1] + " | Dias em atraso: " + diasAtraso);
            }

        } catch (Exception e) {
            Throwable causa = e;
            while (causa.getCause() != null)
                causa = causa.getCause();
            System.out.println("Erro ao gerar relatório de atrasos: " + causa.getMessage());
        } finally {
            entityManager.close();
        }
    }

    public void usuariosComMaisAtrasos() {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();

        try {
            List<Object[]> resultado = entityManager.createQuery(
                    "SELECT u.nome, COUNT(e) " +
                            "FROM Emprestimo e " +
                            "JOIN e.usuario u " +
                            "WHERE e.dataDevolucao IS NULL AND e.dataDevolucaoPrevista < :hoje " +
                            "GROUP BY u.nome " +
                            "ORDER BY COUNT(e) DESC",
                    Object[].class)
                    .setParameter("hoje", LocalDateTime.now())
                    .getResultList();

            if (resultado.isEmpty()) {
                System.out.println("Nenhum usuário com atrasos registrados.");
                return;
            }

            System.out.println("=== USUÁRIOS COM MAIS ATRASOS ===");
            for (Object[] linha : resultado) {
                System.out.println("Usuário: " + linha[0] + " | Atrasos: " + linha[1]);
            }

        } catch (Exception e) {
            Throwable causa = e;
            while (causa.getCause() != null)
                causa = causa.getCause();
            System.out.println("Erro ao gerar relatório de atrasos: " + causa.getMessage());
        } finally {
            entityManager.close();
        }
    }

    public void estatisticasMensais() {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();

        try {
            LocalDate agora = LocalDate.now();
            int anoAtual = agora.getYear();

            String mesNome = agora.format(DateTimeFormatter.ofPattern("MMMM", new Locale("pt", "BR")));

            LocalDateTime inicioDomes = agora.withDayOfMonth(1).atStartOfDay();
            LocalDateTime fimDoMes = agora.withDayOfMonth(agora.lengthOfMonth()).atTime(23, 59, 59);

            Long totalEmprestimos = entityManager.createQuery(
                    "SELECT COUNT(e) FROM Emprestimo e " +
                            "WHERE e.dataEmprestimo BETWEEN :inicio AND :fim",
                    Long.class)
                    .setParameter("inicio", inicioDomes)
                    .setParameter("fim", fimDoMes)
                    .getSingleResult();

            Long totalDevolucoes = entityManager.createQuery(
                    "SELECT COUNT(e) FROM Emprestimo e " +
                            "WHERE e.dataDevolucao IS NOT NULL " +
                            "AND e.dataDevolucao BETWEEN :inicio AND :fim",
                    Long.class)
                    .setParameter("inicio", inicioDomes)
                    .setParameter("fim", fimDoMes)
                    .getSingleResult();

            Long emprestimosAtivos = entityManager.createQuery(
                    "SELECT COUNT(e) FROM Emprestimo e " +
                            "WHERE e.dataEmprestimo BETWEEN :inicio AND :fim " +
                            "AND e.dataDevolucao IS NULL",
                    Long.class)
                    .setParameter("inicio", inicioDomes)
                    .setParameter("fim", fimDoMes)
                    .getSingleResult();

            Long emprestimosEmAtraso = entityManager.createQuery(
                    "SELECT COUNT(e) FROM Emprestimo e " +
                            "WHERE e.dataDevolucao IS NULL " +
                            "AND e.dataDevolucaoPrevista < :hoje",
                    Long.class)
                    .setParameter("hoje", LocalDateTime.now())
                    .getSingleResult();

            System.out.println("\n=== MOVIMENTAÇÃO DO MÊS - " + mesNome.toUpperCase() + "/" + anoAtual + " ===");
            System.out.println("  Empréstimos realizados: " + totalEmprestimos);
            System.out.println("  Devoluções realizadas:  " + totalDevolucoes);
            System.out.println("  Empréstimos ativos:     " + emprestimosAtivos);
            System.out.println("  Empréstimos em atraso:  " + emprestimosEmAtraso);

        } catch (Exception e) {
            Throwable causa = e;
            while (causa.getCause() != null)
                causa = causa.getCause();
            System.out.println("Erro ao gerar estatísticas mensais: " + causa.getMessage());
        } finally {
            entityManager.close();
        }
    }

}
