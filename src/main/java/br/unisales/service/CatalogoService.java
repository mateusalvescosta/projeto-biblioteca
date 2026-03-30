package br.unisales.service;

import br.unisales.database.table.Exemplar;
import br.unisales.database.table.Livro;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import java.util.List;

public class CatalogoService {

    private final EntityManagerFactory entityManagerFactory;

    public CatalogoService(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    /**
     * Cadastra um novo livro no catálogo.
     */
    public void cadastrarLivro(Livro livro) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();
            entityManager.persist(livro);
            transaction.commit();
            System.out.println("Livro cadastrado com sucesso.");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            Throwable causa = e;
            while (causa.getCause() != null) {
                causa = causa.getCause();
            }
            System.out.println("Erro ao cadastrar livro: " + causa.getMessage());
        } finally {
            entityManager.close();
        }
    }

    /**
     * Cadastra um novo exemplar vinculado a um livro existente.
     */
    public void cadastrarExemplar(Exemplar exemplar) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();
            entityManager.persist(exemplar);
            transaction.commit();
            System.out.println("Exemplar cadastrado com sucesso.");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            Throwable causa = e;
            while (causa.getCause() != null) {
                causa = causa.getCause();
            }
            System.out.println("Erro ao cadastrar exemplar: " + causa.getMessage());
        } finally {
            entityManager.close();
        }
    }

    /**
     * Remove um livro pelo ISBN (e seus exemplares em cascata).
     */
    public void removerLivro(String isbn) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            Livro livro = entityManager.find(Livro.class, isbn);
            if (livro == null) {
                System.out.println("Livro não encontrado.");
                return;
            }
            transaction.begin();
            entityManager.remove(livro);
            transaction.commit();
            System.out.println("Livro removido com sucesso.");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.out.println("Erro ao remover livro: " + e.getMessage());
        } finally {
            entityManager.close();
        }
    }

    /**
     * Remove um exemplar pelo ID.
     */
    public void removerExemplar(Long id) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            Exemplar exemplar = entityManager.find(Exemplar.class, id);
            if (exemplar == null) {
                System.out.println("Exemplar não encontrado.");
                return;
            }
            transaction.begin();
            entityManager.remove(exemplar);
            transaction.commit();
            System.out.println("Exemplar removido com sucesso.");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.out.println("Erro ao remover exemplar: " + e.getMessage());
        } finally {
            entityManager.close();
        }
    }

    /**
     * Busca um livro pelo ISBN exato.
     */
    public Livro buscarPorIsbn(String isbn) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        try {
            return entityManager.find(Livro.class, isbn);
        } catch (Exception e) {
            System.out.println("Erro ao buscar livro por ISBN: " + e.getMessage());
            return null;
        } finally {
            entityManager.close();
        }
    }

    /**
     * Busca livros cujo título contenha o termo informado (busca parcial, case-insensitive).
     */
    public List<Livro> buscarPorTitulo(String titulo) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        try {
            return entityManager
                    .createQuery("SELECT l FROM Livro l WHERE LOWER(l.titulo) LIKE LOWER(:titulo) ORDER BY l.titulo", Livro.class)
                    .setParameter("titulo", "%" + titulo + "%")
                    .getResultList();
        } catch (Exception e) {
            System.out.println("Erro ao buscar livro por título: " + e.getMessage());
            return List.of();
        } finally {
            entityManager.close();
        }
    }

    /**
     * Lista todos os livros com seus autores carregados.
     */
    public List<Livro> listar() {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        try {
            return entityManager
                    .createQuery(
                        "SELECT DISTINCT l FROM Livro l LEFT JOIN FETCH l.livroAutores la LEFT JOIN FETCH la.autor",
                        Livro.class
                    )
                    .getResultList();
        } catch (Exception e) {
            System.out.println("Erro ao listar livros: " + e.getMessage());
            return List.of();
        } finally {
            entityManager.close();
        }
    }

    /**
     * Lista todos os exemplares de um livro pelo ISBN.
     */
    public List<Exemplar> listarExemplares(String isbn) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        try {
            return entityManager
                    .createQuery("SELECT e FROM Exemplar e WHERE e.livro.isbn = :isbn ORDER BY e.id", Exemplar.class)
                    .setParameter("isbn", isbn)
                    .getResultList();
        } catch (Exception e) {
            System.out.println("Erro ao listar exemplares: " + e.getMessage());
            return List.of();
        } finally {
            entityManager.close();
        }
    }
}