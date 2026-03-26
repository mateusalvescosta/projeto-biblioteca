package br.unisales.service;

import br.unisales.database.table.Categoria;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import java.util.List;

public class CategoriaService {

    private final EntityManagerFactory entityManagerFactory;

    public CategoriaService(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public void inserir(Categoria categoria) {
        categoria.setId(this.getNextId() + 1);

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

            Throwable causa = e;
            while (causa.getCause() != null) {
                causa = causa.getCause();
            }

            System.out.println("Erro ao inserir categoria: " + causa.getMessage());
        } finally {
            entityManager.close();
        }
    }

    public List<Categoria> listarTodos() {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        try {
            return entityManager
                    .createQuery("SELECT c FROM Categoria c ORDER BY c.nome", Categoria.class)
                    .getResultList();
        } catch (Exception e) {
            System.out.println("Erro ao listar categorias: " + e.getMessage());
            return List.of();
        } finally {
            entityManager.close();
        }
    }

    public Categoria buscarPorId(Integer id) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        try {
            return entityManager.find(Categoria.class, id);
        } catch (Exception e) {
            System.out.println("Erro ao buscar categoria por ID: " + e.getMessage());
            return null;
        } finally {
            entityManager.close();
        }
    }

    public void atualizar(Categoria categoria) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.merge(categoria);
            transaction.commit();
            System.out.println("Categoria atualizada com sucesso.");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.out.println("Erro ao atualizar categoria: " + e.getMessage());
        } finally {
            entityManager.close();
        }
    }

    public void deletar(Integer id) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            Categoria categoria = entityManager.find(Categoria.class, id);
            if (categoria == null) {
                System.out.println("Categoria nao encontrada para exclusao.");
                return;
            }

            transaction.begin();
            entityManager.remove(categoria);
            transaction.commit();
            System.out.println("Categoria removida com sucesso.");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.out.println("Erro ao remover categoria: " + e.getMessage());
        } finally {
            entityManager.close();
        }
    }

    private Integer getNextId() {
        EntityManager em = this.entityManagerFactory.createEntityManager();
        try {
            Integer maxId = em.createQuery(
                    "SELECT MAX(c.id) FROM Categoria c",
                    Integer.class
            ).getSingleResult();
            return maxId != null ? maxId : 0;
        } catch (Exception e) {
            System.out.println("Erro ao buscar maior ID: " + e.getMessage());
            return 1;
        } finally {
            em.close();
        }
    }
}
