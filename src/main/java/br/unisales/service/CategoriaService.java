package br.unisales.service;

import br.unisales.database.table.Categoria;
import br.unisales.service.util.ServiceUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import java.util.List;

public class CategoriaService {

    private final EntityManagerFactory entityManagerFactory;

    public CategoriaService(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    // Insere uma nova categoria no banco de dados
    public void inserirCategoria(Categoria categoria) {
        // Gera o próximo ID disponível para a categoria
        categoria.setId(Long.valueOf(ServiceUtil.getNextId(entityManagerFactory, "SELECT MAX(c.id) FROM Categoria c")));

        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();
            entityManager.persist(categoria);
            transaction.commit();
            System.out.println("Categoria inserida com sucesso.");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.out.println("Erro ao inserir categoria: " + ServiceUtil.extrairMensagemErro(e));
        } finally {
            entityManager.close();
        }
    }

    // Lista todas as categorias cadastradas ordenadas por nome
    public List<Categoria> listarTodasCategorias() {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        try {
            // Busca todas as categorias ordenadas pelo nome
            return entityManager
                    .createQuery("SELECT c FROM Categoria c ORDER BY c.nome", Categoria.class)
                    .getResultList();
        } catch (Exception e) {
            System.out.println("Erro ao listar categorias: " + ServiceUtil.extrairMensagemErro(e));
            return List.of();
        } finally {
            entityManager.close();
        }
    }

    // Busca uma categoria pelo ID
    public Categoria buscarCategoriaPorId(Integer id) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        try {
            // Busca a categoria diretamente pelo ID
            return entityManager.find(Categoria.class, id);
        } catch (Exception e) {
            System.out.println("Erro ao buscar categoria por ID: " + ServiceUtil.extrairMensagemErro(e));
            return null;
        } finally {
            entityManager.close();
        }
    }

    // Atualiza os dados de uma categoria existente
    public void atualizarCategoria(Categoria categoria) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            // Persiste as alterações da categoria no banco
            transaction.begin();
            entityManager.merge(categoria);
            transaction.commit();
            System.out.println("Categoria atualizada com sucesso.");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.out.println("Erro ao atualizar categoria: " + ServiceUtil.extrairMensagemErro(e));
        } finally {
            entityManager.close();
        }
    }

    // Remove uma categoria pelo ID, validando vínculos ativos antes de excluir
    public void deletarCategoria(Integer id) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            // Busca a categoria pelo ID
            Categoria categoria = entityManager.find(Categoria.class, id);
            if (categoria == null) {
                System.out.println("Categoria nao encontrada para exclusao.");
                return;
            }

            // Valida se não há livros dessa categoria com empréstimo ativo
            Long emprestimosAtivos = entityManager.createQuery(
                    "SELECT COUNT(e) FROM Emprestimo e " +
                            "WHERE (e.status = 'ATIVO' OR e.status = 'RENOVADO') " +
                            "AND e.exemplar.livro.isbn IN (" +
                            "    SELECT lc.livro.isbn FROM LivroCategoria lc WHERE lc.categoria.id = :id" +
                            ")",
                    Long.class)
                    .setParameter("id", id)
                    .getSingleResult();

            if (emprestimosAtivos > 0) {
                System.out.println(
                        "Não é possível remover: existe livro dessa categoria com exemplar em empréstimo ativo.");
                return;
            }

            // Valida se não há livros dessa categoria com reserva pendente
            Long reservasPendentes = entityManager.createQuery(
                    "SELECT COUNT(r) FROM Reserva r " +
                            "WHERE r.isbnLivro IN (" +
                            "    SELECT lc.livro.isbn FROM LivroCategoria lc WHERE lc.categoria.id = :id" +
                            ") AND r.status = 'RESERVADO'",
                    Long.class)
                    .setParameter("id", id)
                    .getSingleResult();

            if (reservasPendentes > 0) {
                System.out.println(
                        "Não é possível remover: existe livro dessa categoria com reserva pendente.");
                return;
            }

            // Remove a categoria do banco
            transaction.begin();
            entityManager.remove(categoria);
            transaction.commit();
            System.out.println("Categoria removida com sucesso.");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.out.println("Erro ao remover categoria: " + ServiceUtil.extrairMensagemErro(e));
        } finally {
            entityManager.close();
        }
    }

}