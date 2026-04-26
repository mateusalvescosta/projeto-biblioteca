package br.unisales.service;

import br.unisales.database.table.Autor;
import br.unisales.database.table.Categoria;
import br.unisales.database.table.Exemplar;
import br.unisales.database.table.Livro;
import br.unisales.database.table.LivroAutor;
import br.unisales.database.table.LivroCategoria;
import br.unisales.database.table.primary_key.LivroAutorId;
import br.unisales.database.table.primary_key.LivroCategoriaId;
import br.unisales.service.util.ServiceUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import java.util.List;

public class CatalogoService {

    private final EntityManagerFactory entityManagerFactory;

    public CatalogoService(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    // Cadastra um novo livro no catálogo junto com seu autor e categoria
    public void cadastrarLivro(Livro livro, String nomeAutor, String nomeCategoria) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            // Busca a categoria pelo nome informado
            List<Categoria> categorias = entityManager.createQuery(
                    "SELECT c FROM Categoria c WHERE LOWER(c.nome) = LOWER(:nome)",
                    Categoria.class)
                    .setParameter("nome", nomeCategoria)
                    .getResultList();

            // Valida se a categoria existe
            if (categorias.isEmpty()) {
                transaction.rollback();
                System.out.println("Categoria não encontrada: " + nomeCategoria);
                return;
            }

            Categoria categoria = categorias.get(0);

            // Busca o autor pelo nome informado
            List<Autor> autores = entityManager.createQuery(
                    "SELECT a FROM Autor a WHERE LOWER(a.nome) = LOWER(:nome)",
                    Autor.class)
                    .setParameter("nome", nomeAutor)
                    .getResultList();

            // Cria o autor se não existir, ou reutiliza o existente
            Autor autor;
            if (autores.isEmpty()) {
                autor = Autor.builder()
                        .id(ServiceUtil.getNextId(this.entityManagerFactory, "SELECT MAX(a.id) FROM Autor a"))
                        .nome(nomeAutor)
                        .build();
                entityManager.persist(autor);
                System.out.println("Novo autor criado: " + nomeAutor);
            } else {
                autor = autores.get(0);
                System.out.println("Autor já existe, associando: " + autor.getNome());
            }

            // Persiste o livro e cria as associações com autor e categoria
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
            System.out.println("Erro ao cadastrar livro: " + ServiceUtil.extrairMensagemErro(e));
        } finally {
            entityManager.close();
        }
    }

    // Cadastra um novo exemplar vinculado a um livro existente
    public void cadastrarExemplar(Exemplar exemplar) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            // Gera o próximo ID disponível para o exemplar
            exemplar.setId(ServiceUtil.getNextId(this.entityManagerFactory, "SELECT MAX(e.id) FROM Exemplar e"));

            transaction.begin();
            entityManager.persist(exemplar);
            transaction.commit();
            System.out.println("Exemplar cadastrado com sucesso.");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.out.println("Erro ao cadastrar exemplar: " + ServiceUtil.extrairMensagemErro(e));
        } finally {
            entityManager.close();
        }
    }

    // Remove um livro pelo ISBN e seus exemplares em cascata
    public void removerLivro(String isbn) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            // Busca o livro pelo ISBN
            Livro livro = entityManager.find(Livro.class, isbn);
            if (livro == null) {
                System.out.println("Livro não encontrado.");
                return;
            }

            // Valida se não há exemplares desse livro com empréstimos no histórico
            Long totalEmprestimos = entityManager.createQuery(
                    "SELECT COUNT(e) FROM Emprestimo e " +
                            "WHERE e.exemplar.livro.isbn = :isbn",
                    Long.class)
                    .setParameter("isbn", isbn)
                    .getSingleResult();

            if (totalEmprestimos > 0) {
                System.out.println("Não é possível remover: este livro possui histórico de empréstimos.");
                return;
            }

            // Valida se não há reservas pendentes para o livro
            Long reservasPendentes = entityManager.createQuery(
                    "SELECT COUNT(r) FROM Reserva r " +
                            "WHERE r.isbnLivro = :isbn " +
                            "AND r.status = 'RESERVADO'",
                    Long.class)
                    .setParameter("isbn", isbn)
                    .getSingleResult();

            if (reservasPendentes > 0) {
                System.out.println("Não é possível remover: existem reservas pendentes para este livro.");
                return;
            }

            // Remove o livro do banco
            transaction.begin();
            entityManager.remove(livro);
            transaction.commit();
            System.out.println("Livro removido com sucesso.");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.out.println("Erro ao remover livro: " + ServiceUtil.extrairMensagemErro(e));
        } finally {
            entityManager.close();
        }
    }

    // Remove um exemplar pelo ID
    public void removerExemplar(Long id) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            // Busca o exemplar pelo ID
            Exemplar exemplar = entityManager.find(Exemplar.class, id);
            if (exemplar == null) {
                System.out.println("Exemplar não encontrado.");
                return;
            }

            // Valida se o exemplar não possui nenhum empréstimo no histórico
            Long totalEmprestimos = entityManager.createQuery(
                    "SELECT COUNT(e) FROM Emprestimo e " +
                            "WHERE e.exemplar.id = :id",
                    Long.class)
                    .setParameter("id", id)
                    .getSingleResult();

            if (totalEmprestimos > 0) {
                System.out.println("Não é possível remover: este exemplar possui histórico de empréstimos.");
                return;
            }

            // Valida se não há reservas pendentes para o livro deste exemplar
            Long reservasPendentes = entityManager.createQuery(
                    "SELECT COUNT(r) FROM Reserva r " +
                            "WHERE r.isbnLivro = :isbn " +
                            "AND r.status = 'RESERVADO'",
                    Long.class)
                    .setParameter("isbn", exemplar.getLivro().getIsbn())
                    .getSingleResult();

            if (reservasPendentes > 0) {
                System.out.println("Não é possível remover: existem reservas pendentes para o livro deste exemplar.");
                return;
            }

            // Remove o exemplar do banco
            transaction.begin();
            entityManager.remove(exemplar);
            transaction.commit();
            System.out.println("Exemplar removido com sucesso.");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.out.println("Erro ao remover exemplar: " + ServiceUtil.extrairMensagemErro(e));
        } finally {
            entityManager.close();
        }
    }

    // Busca um livro pelo ISBN exato carregando autores e categorias
    public Livro buscarLivroPorIsbn(String isbn) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        try {
            // Busca o livro com seus autores
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

            // Segunda query para carregar as categorias do livro
            entityManager.createQuery(
                    "SELECT DISTINCT l FROM Livro l " +
                            "LEFT JOIN FETCH l.livroCategorias lc LEFT JOIN FETCH lc.categoria " +
                            "WHERE l.isbn = :isbn",
                    Livro.class)
                    .setParameter("isbn", isbn)
                    .getResultList();

            return livro;
        } catch (Exception e) {
            System.out.println("Erro ao buscar livro por ISBN: " + ServiceUtil.extrairMensagemErro(e));
            return null;
        } finally {
            entityManager.close();
        }
    }

    // Busca livros cujo título contenha o termo informado, sem distinção de
    // maiúsculas
    public List<Livro> buscarLivrosPorTitulo(String titulo) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        try {
            // Busca os livros com seus autores pelo título parcial
            List<Livro> livros = entityManager.createQuery(
                    "SELECT DISTINCT l FROM Livro l " +
                            "LEFT JOIN FETCH l.livroAutores la LEFT JOIN FETCH la.autor " +
                            "WHERE LOWER(l.titulo) LIKE LOWER(:titulo) ORDER BY l.titulo",
                    Livro.class)
                    .setParameter("titulo", "%" + titulo + "%")
                    .getResultList();

            // Segunda query para carregar as categorias dos livros encontrados
            entityManager.createQuery(
                    "SELECT DISTINCT l FROM Livro l " +
                            "LEFT JOIN FETCH l.livroCategorias lc LEFT JOIN FETCH lc.categoria " +
                            "WHERE LOWER(l.titulo) LIKE LOWER(:titulo)",
                    Livro.class)
                    .setParameter("titulo", "%" + titulo + "%")
                    .getResultList();

            return livros;
        } catch (Exception e) {
            System.out.println("Erro ao buscar livro por título: " + ServiceUtil.extrairMensagemErro(e));
            return List.of();
        } finally {
            entityManager.close();
        }
    }

    // Lista todos os livros cadastrados com seus autores e categorias carregados
    public List<Livro> listarLivros() {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        try {
            // Busca todos os livros com seus autores
            List<Livro> livros = entityManager.createQuery(
                    "SELECT DISTINCT l FROM Livro l LEFT JOIN FETCH l.livroAutores la LEFT JOIN FETCH la.autor",
                    Livro.class)
                    .getResultList();

            // Segunda query para carregar as categorias de todos os livros
            entityManager.createQuery(
                    "SELECT DISTINCT l FROM Livro l LEFT JOIN FETCH l.livroCategorias lc LEFT JOIN FETCH lc.categoria",
                    Livro.class)
                    .getResultList();

            return livros;
        } catch (Exception e) {
            System.out.println("Erro ao listar livros: " + ServiceUtil.extrairMensagemErro(e));
            return List.of();
        } finally {
            entityManager.close();
        }
    }

    // Lista todos os exemplares de um livro pelo ISBN
    public List<Exemplar> listarExemplares(String isbn) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        try {
            // Busca os exemplares ordenados por ID
            return entityManager
                    .createQuery("SELECT e FROM Exemplar e WHERE e.livro.isbn = :isbn ORDER BY e.id", Exemplar.class)
                    .setParameter("isbn", isbn)
                    .getResultList();
        } catch (Exception e) {
            System.out.println("Erro ao listar exemplares: " + ServiceUtil.extrairMensagemErro(e));
            return List.of();
        } finally {
            entityManager.close();
        }
    }
}