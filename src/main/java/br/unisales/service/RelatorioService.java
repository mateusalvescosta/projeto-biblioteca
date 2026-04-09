package br.unisales.service;

import java.time.LocalDate;
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

    public void estatisticasMensais() {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();

        try {
            LocalDate agora = LocalDate.now();
            int mesAtual = agora.getMonthValue();
            int anoAtual = agora.getYear();

            // Consulta empréstimos do mês atual
            Long totalEmprestimos = entityManager.createQuery(
                    "SELECT COUNT(e) FROM Emprestimo e " +
                            "WHERE YEAR(e.dataEmprestimo) = :ano " +
                            "AND MONTH(e.dataEmprestimo) = :mes",
                    Long.class)
                    .setParameter("ano", anoAtual)
                    .setParameter("mes", mesAtual)
                    .getSingleResult();

            // Consulta devoluções do mês atual
            Long totalDevolucoes = entityManager.createQuery(
                    "SELECT COUNT(e) FROM Emprestimo e " +
                            "WHERE YEAR(e.dataDevolucao) = :ano " +
                            "AND MONTH(e.dataDevolucao) = :mes " +
                            "AND e.dataDevolucao IS NOT NULL",
                    Long.class)
                    .setParameter("ano", anoAtual)
                    .setParameter("mes", mesAtual)
                    .getSingleResult();

            // Empréstimos ativos (feitos no mês mas ainda não devolvidos)
            Long emprestimosAtivos = entityManager.createQuery(
                    "SELECT COUNT(e) FROM Emprestimo e " +
                            "WHERE YEAR(e.dataEmprestimo) = :ano " +
                            "AND MONTH(e.dataEmprestimo) = :mes " +
                            "AND e.dataDevolucao IS NULL",
                    Long.class)
                    .setParameter("ano", anoAtual)
                    .setParameter("mes", mesAtual)
                    .getSingleResult();

            // Total de livros cadastrados no sistema
            Long totalLivros = entityManager.createQuery(
                    "SELECT COUNT(l) FROM Livro l",
                    Long.class)
                    .getSingleResult();

            // Total de exemplares cadastrados
            Long totalExemplares = entityManager.createQuery(
                    "SELECT COUNT(ex) FROM Exemplar ex",
                    Long.class)
                    .getSingleResult();

            // Exemplares disponíveis
            Long exemplaresDisponiveis = entityManager.createQuery(
                    "SELECT COUNT(ex) FROM Exemplar ex WHERE ex.status = 'DISPONIVEL'",
                    Long.class)
                    .getSingleResult();

            // Total de usuários cadastrados
            Long totalUsuarios = entityManager.createQuery(
                    "SELECT COUNT(u) FROM Usuario u",
                    Long.class)
                    .getSingleResult();

            // Usuários por tipo
            Long totalAlunos = entityManager.createQuery(
                    "SELECT COUNT(u) FROM Usuario u WHERE u.tipo = 'ALUNO'",
                    Long.class)
                    .getSingleResult();

            Long totalProfessores = entityManager.createQuery(
                    "SELECT COUNT(u) FROM Usuario u WHERE u.tipo = 'PROFESSOR'",
                    Long.class)
                    .getSingleResult();

            Long totalServidores = entityManager.createQuery(
                    "SELECT COUNT(u) FROM Usuario u WHERE u.tipo = 'SERVIDOR'",
                    Long.class)
                    .getSingleResult();

            // Usuários bloqueados por tipo
            Long alunosBloqueados = entityManager.createQuery(
                    "SELECT COUNT(u) FROM Usuario u WHERE u.tipo = 'ALUNO' AND u.bloqueado = true",
                    Long.class)
                    .getSingleResult();

            Long professoresBloqueados = entityManager.createQuery(
                    "SELECT COUNT(u) FROM Usuario u WHERE u.tipo = 'PROFESSOR' AND u.bloqueado = true",
                    Long.class)
                    .getSingleResult();

            Long servidoresBloqueados = entityManager.createQuery(
                    "SELECT COUNT(u) FROM Usuario u WHERE u.tipo = 'SERVIDOR' AND u.bloqueado = true",
                    Long.class)
                    .getSingleResult();

            String mesNome = obterNomeMes(mesAtual);

            System.out.println("=== ESTATÍSTICAS DO SISTEMA - " + mesNome.toUpperCase() + "/" + anoAtual + " ===");
            System.out.println();
            System.out.println("ACERVO:");
            System.out.println("  Livros cadastrados: " + totalLivros);
            System.out.println("  Total de exemplares: " + totalExemplares);
            System.out.println("  Exemplares disponíveis: " + exemplaresDisponiveis);
            System.out.println();
            System.out.println("USUÁRIOS:");
            System.out.println("  Total de usuários: " + totalUsuarios);
            System.out.println("  Alunos: " + totalAlunos + " (Bloqueados: " + alunosBloqueados + ")");
            System.out.println("  Professores: " + totalProfessores + " (Bloqueados: " + professoresBloqueados + ")");
            System.out.println("  Servidores: " + totalServidores + " (Bloqueados: " + servidoresBloqueados + ")");
            System.out.println();
            System.out.println("MOVIMENTAÇÃO DO MÊS:");
            System.out.println("  Empréstimos realizados: " + totalEmprestimos);
            System.out.println("  Devoluções realizadas: " + totalDevolucoes);
            System.out.println("  Empréstimos ainda ativos: " + emprestimosAtivos);

        } catch (Exception e) {
            Throwable causa = e;
            while (causa.getCause() != null)
                causa = causa.getCause();
            System.out.println("Erro ao gerar estatísticas mensais: " + causa.getMessage());
        } finally {
            entityManager.close();
        }

    }

    private String obterNomeMes(int mes) {
        String[] meses = {
                "Janeiro", "Fevereiro", "Março", "Abril",
                "Maio", "Junho", "Julho", "Agosto",
                "Setembro", "Outubro", "Novembro", "Dezembro"
        };
        return meses[mes - 1];
    }

}
