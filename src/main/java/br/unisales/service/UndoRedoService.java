package br.unisales.service;

import java.util.List;

import br.unisales.Enumeration.StatusEmprestimoEnum;
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
            List<Emprestimo> lista = entityManager.createQuery(
                    "SELECT e FROM Emprestimo e ORDER BY e.id DESC",
                    Emprestimo.class)
                    .setMaxResults(1).getResultList();

            if (lista.isEmpty()) {
                System.out.println("Nenhum empréstimo encontrado para desfazer.");
                return;
            }

            Emprestimo ultimo = lista.get(0);

            if (ultimo.getStatus() != StatusEmprestimoEnum.ATIVO &&
                    ultimo.getStatus() != StatusEmprestimoEnum.RENOVADO) {
                System.out.println("Não é possível desfazer: empréstimo já foi encerrado.");
                return;
            }

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
            System.out.println("Erro ao desfazer empréstimo: " + e.getMessage());
        } finally {
            entityManager.close();
        }
    }

    public void desfazerDevolucao() {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();

        try {
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
            ultimo.setDataDevolucao(null);
            ultimo.setStatus(StatusEmprestimoEnum.ATIVO);
            if (ultimo.getExemplar() != null) {
                ultimo.getExemplar().setStatus(StatusExemplarEnum.EMPRESTADO);
            }
            entityManager.merge(ultimo);
            entityManager.getTransaction().commit();
            System.out.println("Devolução desfeita: ID " + ultimo.getId());

        } catch (Exception e) {
            if (entityManager.getTransaction().isActive())
                entityManager.getTransaction().rollback();
            System.out.println("Erro ao desfazer devolução: " + e.getMessage());
        } finally {
            entityManager.close();
        }
    }

    public void desfazerCadastroUsuario() {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();

        try {
            List<Usuario> lista = entityManager.createQuery(
                    "SELECT u FROM Usuario u ORDER BY u.id DESC",
                    Usuario.class)
                    .setMaxResults(1).getResultList();

            if (lista.isEmpty()) {
                System.out.println("Nenhum usuário encontrado para desfazer.");
                return;
            }

            Usuario ultimo = lista.get(0);

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

            entityManager.getTransaction().begin();
            entityManager.remove(entityManager.merge(ultimo));
            entityManager.getTransaction().commit();
            System.out.println("Cadastro de usuário desfeito: " + ultimo.getNome());

        } catch (Exception e) {
            if (entityManager.getTransaction().isActive())
                entityManager.getTransaction().rollback();
            System.out.println("Erro ao desfazer cadastro de usuário: " + e.getMessage());
        } finally {
            entityManager.close();
        }
    }

    public void desfazerRenovar() {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();

        try {
            List<Emprestimo> lista = entityManager.createQuery(
                    "SELECT e FROM Emprestimo e WHERE e.status = 'RENOVADO' ORDER BY e.id DESC",
                    Emprestimo.class)
                    .setMaxResults(1).getResultList();

            if (lista.isEmpty()) {
                System.out.println("Nenhuma renovação encontrada para desfazer.");
                return;
            }

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
            System.out.println("Erro ao desfazer renovação: " + e.getMessage());
        } finally {
            entityManager.close();
        }
    }
}