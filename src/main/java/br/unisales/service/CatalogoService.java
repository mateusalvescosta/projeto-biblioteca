package br.unisales.service;

import br.unisales.database.table.Autor;
import br.unisales.database.table.Categoria;
import br.unisales.database.table.Exemplar;
import br.unisales.database.table.Livro;
import br.unisales.database.table.LivroAutor;
import br.unisales.database.table.LivroCategoria;
import br.unisales.database.table.primery_key.LivroAutorId;
import br.unisales.database.table.primery_key.LivroCategoriaId;
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
    public void cadastrarLivro(Livro livro, String nomeAutor, String nomeCategoria) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            // Verifica categoria
            List<Categoria> categorias = entityManager.createQuery(
                    "SELECT c FROM Categoria c WHERE LOWER(c.nome) = LOWER(:nome)",
                    Categoria.class)
                    .setParameter("nome", nomeCategoria)
                    .getResultList();

            if (categorias.isEmpty()) {
                transaction.rollback();
                System.out.println("Categoria não encontrada: " + nomeCategoria);
                return;
            }

            Categoria categoria = categorias.get(0);

            // Verifica se autor já existe, senão cria
            List<Autor> autores = entityManager.createQuery(
                    "SELECT a FROM Autor a WHERE LOWER(a.nome) = LOWER(:nome)",
                    Autor.class)
                    .setParameter("nome", nomeAutor)
                    .getResultList();

            Autor autor;
            if (autores.isEmpty()) {
                Long maxId = entityManager.createQuery(
                        "SELECT MAX(a.id) FROM Autor a", Long.class).getSingleResult();
                autor = Autor.builder()
                        .id(maxId != null ? maxId + 1 : 1L)
                        .nome(nomeAutor)
                        .build();
                entityManager.persist(autor);
                System.out.println("Novo autor criado: " + nomeAutor);
            } else {
                autor = autores.get(0);
                System.out.println("Autor já existe, associando: " + autor.getNome());
            }

            entityManager.persist(livro);

            entityManager.persist(LivroAutor.builder()
                    .id(new LivroAutorId(livro.getIsbn(), autor.getId()))
                    .livro(livro)
                    .autor(autor)
                    .build());

            entityManager.persist(LivroCategoria.builder()
                    .id(new LivroCategoriaId(livro.getIsbn(), categoria.getId()))
                    .livro(livro)
                    .categoria(categoria)
                    .build());

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
            Long maxId = entityManager.createQuery(
                    "SELECT MAX(e.id) FROM Exemplar e", Long.class).getSingleResult();
            exemplar.setId(maxId != null ? maxId + 1 : 1L);

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

            // Verifica se existe algum exemplar desse livro com empréstimo ativo
            Long emprestimosAtivos = entityManager.createQuery(
                    "SELECT COUNT(e) FROM Emprestimo e " +
                            "WHERE e.exemplar.livro.isbn = :isbn " +
                            "AND (e.status = 'ATIVO' OR e.status = 'RENOVADO')",
                    Long.class)
                    .setParameter("isbn", isbn)
                    .getSingleResult();

            if (emprestimosAtivos > 0) {
                System.out.println("Não é possível remover: existem exemplares desse livro com empréstimo ativo.");
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

            // Verifica se esse exemplar possui empréstimo ativo
            Long emprestimosAtivos = entityManager.createQuery(
                    "SELECT COUNT(e) FROM Emprestimo e " +
                            "WHERE e.exemplar.id = :id " +
                            "AND (e.status = 'ATIVO' OR e.status = 'RENOVADO')",
                    Long.class)
                    .setParameter("id", id)
                    .getSingleResult();

            if (emprestimosAtivos > 0) {
                System.out.println("Não é possível remover: este exemplar possui empréstimo ativo.");
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
            List<Livro> resultado = entityManager.createQuery(
                    "SELECT DISTINCT l FROM Livro l " +
                            "LEFT JOIN FETCH l.livroAutores la LEFT JOIN FETCH la.autor " +
                            "WHERE l.isbn = :isbn",
                    Livro.class)
                    .setParameter("isbn", isbn)
                    .getResultList();

            if (resultado.isEmpty())
                return null;

            Livro livro = resultado.get(0);

            entityManager.createQuery(
                    "SELECT DISTINCT l FROM Livro l " +
                            "LEFT JOIN FETCH l.livroCategorias lc LEFT JOIN FETCH lc.categoria " +
                            "WHERE l.isbn = :isbn",
                    Livro.class)
                    .setParameter("isbn", isbn)
                    .getResultList();

            return livro;
        } catch (Exception e) {
            System.out.println("Erro ao buscar livro por ISBN: " + e.getMessage());
            return null;
        } finally {
            entityManager.close();
        }
    }

    /**
     * Busca livros cujo título contenha o termo informado (busca parcial,
     * case-insensitive).
     */
    public List<Livro> buscarPorTitulo(String titulo) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        try {
            List<Livro> livros = entityManager.createQuery(
                    "SELECT DISTINCT l FROM Livro l " +
                            "LEFT JOIN FETCH l.livroAutores la LEFT JOIN FETCH la.autor " +
                            "WHERE LOWER(l.titulo) LIKE LOWER(:titulo) ORDER BY l.titulo",
                    Livro.class)
                    .setParameter("titulo", "%" + titulo + "%")
                    .getResultList();

            entityManager.createQuery(
                    "SELECT DISTINCT l FROM Livro l " +
                            "LEFT JOIN FETCH l.livroCategorias lc LEFT JOIN FETCH lc.categoria " +
                            "WHERE LOWER(l.titulo) LIKE LOWER(:titulo)",
                    Livro.class)
                    .setParameter("titulo", "%" + titulo + "%")
                    .getResultList();

            return livros;
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
            List<Livro> livros = entityManager.createQuery(
                    "SELECT DISTINCT l FROM Livro l LEFT JOIN FETCH l.livroAutores la LEFT JOIN FETCH la.autor",
                    Livro.class)
                    .getResultList();

            // Segunda query para carregar as categorias
            entityManager.createQuery(
                    "SELECT DISTINCT l FROM Livro l LEFT JOIN FETCH l.livroCategorias lc LEFT JOIN FETCH lc.categoria",
                    Livro.class)
                    .getResultList();

            return livros;
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