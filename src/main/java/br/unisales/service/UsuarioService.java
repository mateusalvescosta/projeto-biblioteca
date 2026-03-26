package br.unisales.service;

import br.unisales.database.table.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import java.util.List;

public class UsuarioService {

    private final EntityManagerFactory entityManagerFactory;

    public UsuarioService(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public void inserir(Usuario usuario) {
        usuario.setId(Long.valueOf(this.getNextId() + 1));

        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();
            entityManager.persist(usuario);
            transaction.commit();
            System.out.println("Usuario inserido com sucesso.");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            Throwable causa = e;
            while (causa.getCause() != null) {
                causa = causa.getCause();
            }

            System.out.println("Erro ao inserir usuario: " + causa.getMessage());
        } finally {
            entityManager.close();
        }
    }

    public List<Usuario> listarTodos() {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        try {
            return entityManager
                    .createQuery("SELECT u FROM Usuario u ORDER BY u.id", Usuario.class)
                    .getResultList();
        } catch (Exception e) {
            System.out.println("Erro ao listar usuarios: " + e.getMessage());
            return List.of();
        } finally {
            entityManager.close();
        }
    }

    public Usuario buscarPorId(Integer id) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        try {
            return entityManager.find(Usuario.class, id);
        } catch (Exception e) {
            System.out.println("Erro ao buscar usuario por ID: " + e.getMessage());
            return null;
        } finally {
            entityManager.close();
        }
    }

    public void atualizar(Usuario usuario) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.merge(usuario);
            transaction.commit();
            System.out.println("Usuario atualizado com sucesso.");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.out.println("Erro ao atualizar usuario: " + e.getMessage());
        } finally {
            entityManager.close();
        }
    }

    public void deletar(Integer id) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            Usuario usuario = entityManager.find(Usuario.class, id);
            if (usuario == null) {
                System.out.println("Usuario nao encontrado para exclusao.");
                return;
            }

            transaction.begin();
            entityManager.remove(usuario);
            transaction.commit();
            System.out.println("Usuario removido com sucesso.");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.out.println("Erro ao remover usuario: " + e.getMessage());
        } finally {
            entityManager.close();
        }
    }

    private Integer getNextId() {
        EntityManager em = this.entityManagerFactory.createEntityManager();
        try {
            Integer maxId = em.createQuery(
                    "SELECT MAX(u.id) FROM Usuario u",
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
