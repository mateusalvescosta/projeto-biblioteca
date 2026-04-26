package br.unisales.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

import br.unisales.service.util.ServiceUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

public class RelatorioService {

    private final EntityManagerFactory entityManagerFactory;

    public RelatorioService(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    // Exibe os 10 livros mais emprestados em ordem decrescente
    public void topLivrosMaisEmprestados() {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();

        try {
            // Busca os livros agrupados por título e ordenados pela quantidade de empréstimos
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

            // Exibe o ranking com posição e quantidade de empréstimos
            System.out.println("=== TOP LIVROS MAIS EMPRESTADOS ===");
            int posicao = 1;
            for (Object[] linha : resultado) {
                System.out.println("#" + posicao + " " + linha[0] + " | Empréstimos: " + linha[1]);
                posicao++;
            }

        } catch (Exception e) {
            System.out.println("Erro ao gerar relatório: " + ServiceUtil.extrairMensagemErro(e));
        } finally {
            entityManager.close();
        }
    }

    // Exibe todos os empréstimos com prazo vencido e ainda não devolvidos
    public void emprestimosEmAtraso() {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();

        try {
            // Busca empréstimos sem devolução e com prazo anterior à data atual
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

            // Exibe o nome do usuário, título do livro e quantidade de dias em atraso
            System.out.println("=== EMPRÉSTIMOS EM ATRASO ===");
            for (Object[] linha : resultado) {
                LocalDateTime dataPrevista = (LocalDateTime) linha[2];
                long diasAtraso = ChronoUnit.DAYS.between(dataPrevista, LocalDateTime.now());
                System.out.println("Usuário: " + linha[0] + " | Livro: " + linha[1] + " | Dias em atraso: " + diasAtraso);
            }

        } catch (Exception e) {
            System.out.println("Erro ao gerar relatório de atrasos: " + ServiceUtil.extrairMensagemErro(e));
        } finally {
            entityManager.close();
        }
    }

    // Exibe os usuários com maior quantidade de empréstimos em atraso
    public void usuariosComMaisAtrasos() {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();

        try {
            // Busca usuários com empréstimos vencidos, agrupados e ordenados por quantidade
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

            // Exibe o nome do usuário e a quantidade de atrasos
            System.out.println("=== USUÁRIOS COM MAIS ATRASOS ===");
            for (Object[] linha : resultado) {
                System.out.println("Usuário: " + linha[0] + " | Atrasos: " + linha[1]);
            }

        } catch (Exception e) {
            System.out.println("Erro ao gerar relatório de atrasos: " + ServiceUtil.extrairMensagemErro(e));
        } finally {
            entityManager.close();
        }
    }

    // Exibe as estatísticas de movimentação do mês atual
    public void estatisticasMensais() {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();

        try {
            LocalDate agora = LocalDate.now();
            int anoAtual = agora.getYear();
            String mesNome = agora.format(DateTimeFormatter.ofPattern("MMMM", new Locale("pt", "BR")));

            // Define o intervalo de datas do mês atual
            LocalDateTime inicioDomes = agora.withDayOfMonth(1).atStartOfDay();
            LocalDateTime fimDoMes = agora.withDayOfMonth(agora.lengthOfMonth()).atTime(23, 59, 59);

            // Conta os empréstimos realizados no mês
            Long totalEmprestimos = entityManager.createQuery(
                    "SELECT COUNT(e) FROM Emprestimo e " +
                            "WHERE e.dataEmprestimo BETWEEN :inicio AND :fim",
                    Long.class)
                    .setParameter("inicio", inicioDomes)
                    .setParameter("fim", fimDoMes)
                    .getSingleResult();

            // Conta as devoluções realizadas no mês
            Long totalDevolucoes = entityManager.createQuery(
                    "SELECT COUNT(e) FROM Emprestimo e " +
                            "WHERE e.dataDevolucao IS NOT NULL " +
                            "AND e.dataDevolucao BETWEEN :inicio AND :fim",
                    Long.class)
                    .setParameter("inicio", inicioDomes)
                    .setParameter("fim", fimDoMes)
                    .getSingleResult();

            // Conta os empréstimos do mês ainda sem devolução
            Long emprestimosAtivos = entityManager.createQuery(
                    "SELECT COUNT(e) FROM Emprestimo e " +
                            "WHERE e.dataEmprestimo BETWEEN :inicio AND :fim " +
                            "AND e.dataDevolucao IS NULL",
                    Long.class)
                    .setParameter("inicio", inicioDomes)
                    .setParameter("fim", fimDoMes)
                    .getSingleResult();

            // Conta todos os empréstimos com prazo vencido e sem devolução
            Long emprestimosEmAtraso = entityManager.createQuery(
                    "SELECT COUNT(e) FROM Emprestimo e " +
                            "WHERE e.dataDevolucao IS NULL " +
                            "AND e.dataDevolucaoPrevista < :hoje",
                    Long.class)
                    .setParameter("hoje", LocalDateTime.now())
                    .getSingleResult();

            // Exibe o resumo das estatísticas do mês
            System.out.println("\n=== MOVIMENTAÇÃO DO MÊS - " + mesNome.toUpperCase() + "/" + anoAtual + " ===");
            System.out.println("  Empréstimos realizados: " + totalEmprestimos);
            System.out.println("  Devoluções realizadas:  " + totalDevolucoes);
            System.out.println("  Empréstimos ativos:     " + emprestimosAtivos);
            System.out.println("  Empréstimos em atraso:  " + emprestimosEmAtraso);

        } catch (Exception e) {
            System.out.println("Erro ao gerar estatísticas mensais: " + ServiceUtil.extrairMensagemErro(e));
        } finally {
            entityManager.close();
        }
    }

}