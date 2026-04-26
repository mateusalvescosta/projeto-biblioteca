package br.unisales.service;

import java.util.List;

import br.unisales.Enumeration.StatusEmprestimoEnum;
import br.unisales.Enumeration.StatusExemplarEnum;
import br.unisales.database.table.Emprestimo;
import br.unisales.database.table.Multa;
import br.unisales.database.table.Notificacao;
import br.unisales.database.table.Usuario;
import br.unisales.service.util.ServiceUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

public class UndoRedoService {

    private final EntityManagerFactory entityManagerFactory;

    public UndoRedoService(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    // Desfaz o último empréstimo ativo, removendo-o e liberando o exemplar
    public void desfazerEmprestimo() {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();

        try {
            // Busca o empréstimo mais recente pelo maior ID
            List<Emprestimo> lista = entityManager.createQuery(
                    "SELECT e FROM Emprestimo e ORDER BY e.id DESC",
                    Emprestimo.class)
                    .setMaxResults(1).getResultList();

            if (lista.isEmpty()) {
                System.out.println("Nenhum empréstimo encontrado para desfazer.");
                return;
            }

            Emprestimo ultimo = lista.get(0);

            // Valida se o empréstimo ainda está ativo ou renovado
            if (ultimo.getStatus() != StatusEmprestimoEnum.ATIVO &&
                    ultimo.getStatus() != StatusEmprestimoEnum.RENOVADO) {
                System.out.println("Não é possível desfazer: empréstimo já foi encerrado.");
                return;
            }

            // Remove o empréstimo e restaura o status do exemplar para disponível
            entityManager.getTransaction().begin();
            if (ultimo.getExemplar() != null) {
                ultimo.getExemplar().setStatus(StatusExemplarEnum.DISPONIVEL);
            }
            entityManager.remove(entityManager.merge(ultimo));
            entityManager.getTransaction().commit();
            System.out.println("Empréstimo desfeito: ID " + ultimo.getId());

        } catch (Exception e) {
            if (entityManager.getTransaction().isActive())
                entityManager.getTransaction().rollback();
            System.out.println("Erro ao desfazer empréstimo: " + ServiceUtil.extrairMensagemErro(e));
        } finally {
            entityManager.close();
        }
    }

    // Desfaz a última devolução, revertendo o empréstimo para ativo e removendo a multa
    public void desfazerDevolucao() {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();

        try {
            // Busca o empréstimo mais recente que possui data de devolução registrada
            List<Emprestimo> lista = entityManager.createQuery(
                    "SELECT e FROM Emprestimo e WHERE e.dataDevolucao IS NOT NULL ORDER BY e.id DESC",
                    Emprestimo.class)
                    .setMaxResults(1).getResultList();

            if (lista.isEmpty()) {
                System.out.println("Nenhuma devolução encontrada para desfazer.");
                return;
            }

            Emprestimo ultimo = lista.get(0);
            entityManager.getTransaction().begin();

            // Reverte a devolução, restaurando o status do empréstimo e do exemplar
            ultimo.setDataDevolucao(null);
            ultimo.setStatus(StatusEmprestimoEnum.ATIVO);
            if (ultimo.getExemplar() != null) {
                ultimo.getExemplar().setStatus(StatusExemplarEnum.EMPRESTADO);
            }
            entityManager.merge(ultimo);

            // Busca e remove a multa associada para que o cálculo seja refeito na próxima devolução
            List<Multa> multas = entityManager.createQuery(
                    "SELECT m FROM Multa m WHERE m.emprestimoId = :id",
                    Multa.class)
                    .setParameter("id", ultimo.getId())
                    .getResultList();

            for (Multa multa : multas) {
                entityManager.remove(entityManager.merge(multa));
            }

            entityManager.getTransaction().commit();
            System.out.println("Devolução desfeita: ID " + ultimo.getId());

        } catch (Exception e) {
            if (entityManager.getTransaction().isActive())
                entityManager.getTransaction().rollback();
            System.out.println("Erro ao desfazer devolução: " + ServiceUtil.extrairMensagemErro(e));
        } finally {
            entityManager.close();
        }
    }

    // Desfaz o último cadastro de usuário, removendo-o se não houver vínculos ativos
    public void desfazerCadastroUsuario() {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();

        try {
            // Busca o usuário cadastrado mais recentemente pelo maior ID
            List<Usuario> lista = entityManager.createQuery(
                    "SELECT u FROM Usuario u ORDER BY u.id DESC",
                    Usuario.class)
                    .setMaxResults(1).getResultList();

            if (lista.isEmpty()) {
                System.out.println("Nenhum usuário encontrado para desfazer.");
                return;
            }

            Usuario ultimo = lista.get(0);

            // Valida se o usuário não possui empréstimos ativos
            Long emprestimosAtivos = entityManager.createQuery(
                    "SELECT COUNT(e) FROM Emprestimo e WHERE e.usuario.id = :id " +
                            "AND (e.status = 'ATIVO' OR e.status = 'RENOVADO')",
                    Long.class)
                    .setParameter("id", ultimo.getId())
                    .getSingleResult();

            if (emprestimosAtivos > 0) {
                System.out.println("Não é possível desfazer: usuário possui empréstimos ativos.");
                return;
            }

            // Valida se o usuário não possui reservas pendentes
            Long reservasPendentes = entityManager.createQuery(
                    "SELECT COUNT(r) FROM Reserva r " +
                            "WHERE r.usuarioId = :id " +
                            "AND r.status = 'RESERVADO'",
                    Long.class)
                    .setParameter("id", ultimo.getId())
                    .getSingleResult();

            if (reservasPendentes > 0) {
                System.out.println("Não é possível desfazer: usuário possui reservas pendentes.");
                return;
            }

            // Remove o usuário do banco
            entityManager.getTransaction().begin();
            entityManager.remove(entityManager.merge(ultimo));
            entityManager.getTransaction().commit();
            System.out.println("Cadastro de usuário desfeito: " + ultimo.getNome());

        } catch (Exception e) {
            if (entityManager.getTransaction().isActive())
                entityManager.getTransaction().rollback();
            System.out.println("Erro ao desfazer cadastro de usuário: " + ServiceUtil.extrairMensagemErro(e));
        } finally {
            entityManager.close();
        }
    }

    // Desfaz a última renovação, revertendo o prazo em 7 dias e o status para ativo
    public void desfazerRenovar() {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();

        try {
            // Busca o empréstimo renovado mais recente pelo maior ID
            List<Emprestimo> lista = entityManager.createQuery(
                    "SELECT e FROM Emprestimo e WHERE e.status = 'RENOVADO' ORDER BY e.id DESC",
                    Emprestimo.class)
                    .setMaxResults(1).getResultList();

            if (lista.isEmpty()) {
                System.out.println("Nenhuma renovação encontrada para desfazer.");
                return;
            }

            // Reverte o prazo de devolução e restaura o status para ativo
            Emprestimo ultimo = lista.get(0);
            entityManager.getTransaction().begin();
            ultimo.setDataDevolucaoPrevista(ultimo.getDataDevolucaoPrevista().minusDays(7));
            ultimo.setStatus(StatusEmprestimoEnum.ATIVO);
            entityManager.merge(ultimo);
            entityManager.getTransaction().commit();
            System.out.println("Renovação desfeita: ID " + ultimo.getId() +
                    " | Data prevista restaurada: " + ultimo.getDataDevolucaoPrevista());

        } catch (Exception e) {
            if (entityManager.getTransaction().isActive())
                entityManager.getTransaction().rollback();
            System.out.println("Erro ao desfazer renovação: " + ServiceUtil.extrairMensagemErro(e));
        } finally {
            entityManager.close();
        }
    }

    // Desfaz o registro da última multa, removendo-a caso ainda não esteja quitada
    public void desfazerMulta() {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();

        try {
            // Busca a multa mais recente pelo maior ID
            List<Multa> lista = entityManager.createQuery(
                    "SELECT m FROM Multa m ORDER BY m.id DESC",
                    Multa.class)
                    .setMaxResults(1).getResultList();

            if (lista.isEmpty()) {
                System.out.println("Nenhuma multa encontrada para desfazer.");
                return;
            }

            Multa ultimaMulta = lista.get(0);

            // Valida se a multa ainda não foi quitada
            if (Boolean.TRUE.equals(ultimaMulta.getQuitada())) {
                System.out.println("Não é possível desfazer: a multa já foi quitada.");
                return;
            }

            // Valida se o empréstimo vinculado ainda está como devolvido
            // Para desfazer a multa, primeiro é necessário desfazer a devolução
            Emprestimo emprestimo = entityManager.find(Emprestimo.class, ultimaMulta.getEmprestimoId());
            if (emprestimo != null && emprestimo.getStatus() == StatusEmprestimoEnum.DEVOLVIDO) {
                System.out.println("Não é possível desfazer: o empréstimo ainda está como devolvido.");
                System.out.println("Execute primeiro o desfazer devolução e depois o desfazer multa.");
                return;
            }

            // Remove a multa do banco
            entityManager.getTransaction().begin();
            entityManager.remove(entityManager.merge(ultimaMulta));
            entityManager.getTransaction().commit();
            System.out.println("Multa desfeita: ID " + ultimaMulta.getId() +
                    " | Valor: R$ " + ultimaMulta.getValor());

        } catch (Exception e) {
            if (entityManager.getTransaction().isActive())
                entityManager.getTransaction().rollback();
            System.out.println("Erro ao desfazer multa: " + ServiceUtil.extrairMensagemErro(e));
        } finally {
            entityManager.close();
        }
    }

    // Desfaz o registro da última notificação, removendo-a caso ainda não tenha sido lida
    public void desfazerNotificacao() {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();

        try {
            // Busca a notificação mais recente pelo maior ID
            List<Notificacao> lista = entityManager.createQuery(
                    "SELECT n FROM Notificacao n ORDER BY n.id DESC",
                    Notificacao.class)
                    .setMaxResults(1).getResultList();

            if (lista.isEmpty()) {
                System.out.println("Nenhuma notificação encontrada para desfazer.");
                return;
            }

            Notificacao ultimaNotificacao = lista.get(0);

            // Valida se a notificação ainda não foi lida
            if (Boolean.TRUE.equals(ultimaNotificacao.getLida())) {
                System.out.println("Não é possível desfazer: a notificação já foi lida.");
                return;
            }

            // Remove a notificação do banco
            entityManager.getTransaction().begin();
            entityManager.remove(entityManager.merge(ultimaNotificacao));
            entityManager.getTransaction().commit();
            System.out.println("Notificação desfeita: ID " + ultimaNotificacao.getId());

        } catch (Exception e) {
            if (entityManager.getTransaction().isActive())
                entityManager.getTransaction().rollback();
            System.out.println("Erro ao desfazer notificação: " + ServiceUtil.extrairMensagemErro(e));
        } finally {
            entityManager.close();
        }
    }
}