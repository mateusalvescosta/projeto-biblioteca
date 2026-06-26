package br.unisales.service;

/**
 * Sistema de Gerenciamento de Biblioteca
 *
 * Disciplina: Construção em Estruturas de Dados / Projeto e Qualidade
 *             em Engenharia de Software
 * Professor:  Vito Franzosi
 * Período:    Terceiro
 *
 * Grupo:
 *   - Arthur Yuji Mendes Suzuki
 *   - Carlos Eduardo Pisa Meireles
 *   - Felipe Souza de Jesus
 *   - Mateus Alves Costa
 */

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

    // Cadastra um novo autor de forma independente, validando duplicidade de nome
    public void cadastrarAutor(String nomeAutor) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            // Verifica se já existe um autor com o nome informado
            List<Autor> autores = entityManager.createQuery(
                    "SELECT a FROM Autor a WHERE LOWER(a.nome) = LOWER(:nome)",
                    Autor.class)
                    .setParameter("nome", nomeAutor)
                    .getResultList();

            // Impede o cadastro de um autor já existente
            if (!autores.isEmpty()) {
                System.out.println("Já existe um autor cadastrado com o nome \"" + nomeAutor + "\".");
                return;
            }

            // Cria e persiste o novo autor
            transaction.begin();
            Autor autor = Autor.builder()
                    .id(ServiceUtil.getNextId(this.entityManagerFactory, "SELECT MAX(a.id) FROM Autor a"))
                    .nome(nomeAutor)
                    .build();
            entityManager.persist(autor);
            transaction.commit();
            System.out.println("Autor cadastrado com sucesso: " + nomeAutor);
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.out.println("Erro ao cadastrar autor: " + ServiceUtil.extrairMensagemErro(e));
        } finally {
            entityManager.close();
        }
    }

    // Cadastra um novo livro no catálogo associando a um autor e categoria já existentes
    public void cadastrarLivro(Livro livro, String nomeAutor, String nomeCategoria) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            // Verifica se já existe um livro com o mesmo ISBN
            if (entityManager.find(Livro.class, livro.getIsbn()) != null) {
                System.out.println("Já existe um livro cadastrado com o ISBN \"" + livro.getIsbn() + "\".");
                return;
            }

            // Busca o autor pelo nome e valida se existe
            List<Autor> autores = entityManager.createQuery(
                    "SELECT a FROM Autor a WHERE LOWER(a.nome) = LOWER(:nome)",
                    Autor.class)
                    .setParameter("nome", nomeAutor)
                    .getResultList();

            if (autores.isEmpty()) {
                System.out.println(
                        "Autor não encontrado: " + nomeAutor + ". Cadastre o autor antes de cadastrar o livro.");
                return;
            }

            Autor autor = autores.get(0);

            // Busca a categoria pelo nome e valida se existe
            List<Categoria> categorias = entityManager.createQuery(
                    "SELECT c FROM Categoria c WHERE LOWER(c.nome) = LOWER(:nome)",
                    Categoria.class)
                    .setParameter("nome", nomeCategoria)
                    .getResultList();

            if (categorias.isEmpty()) {
                System.out.println("Categoria não encontrada: " + nomeCategoria);
                return;
            }

            Categoria categoria = categorias.get(0);

            // Persiste o livro e cria as associações com autor e categoria
            transaction.begin();
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

    // Remove um livro pelo ISBN, desde que não possua exemplares, empréstimos ou reservas
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

            // Valida se não há exemplares cadastrados para o livro
            Long totalExemplares = entityManager.createQuery(
                    "SELECT COUNT(e) FROM Exemplar e " +
                            "WHERE e.livro.isbn = :isbn",
                    Long.class)
                    .setParameter("isbn", isbn)
                    .getSingleResult();

            if (totalExemplares > 0) {
                System.out.println(
                        "Não é possível remover: este livro possui exemplares cadastrados. Remova os exemplares antes.");
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

    // Remove um autor pelo ID, desde que não possua livros vinculados
    public void removerAutor(Long id) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            // Busca o autor pelo ID
            Autor autor = entityManager.find(Autor.class, id);
            if (autor == null) {
                System.out.println("Autor não encontrado.");
                return;
            }

            // Valida se não há livros vinculados a este autor
            Long totalLivrosVinculados = entityManager.createQuery(
                    "SELECT COUNT(la) FROM LivroAutor la " +
                            "WHERE la.autor.id = :id",
                    Long.class)
                    .setParameter("id", id)
                    .getSingleResult();

            if (totalLivrosVinculados > 0) {
                System.out.println(
                        "Não é possível remover: este autor possui livros vinculados. Remova os vínculos antes.");
                return;
            }

            // Remove o autor do banco
            transaction.begin();
            entityManager.remove(autor);
            transaction.commit();
            System.out.println("Autor removido com sucesso.");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.out.println("Erro ao remover autor: " + ServiceUtil.extrairMensagemErro(e));
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

    // Busca livros cujo título contenha o termo informado, sem distinção de maiúsculas
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

    // Atualiza o nome de um autor existente pelo ID
    public void atualizarAutor(Long id, String novoNome) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            // Busca o autor pelo ID e valida se existe
            Autor autor = entityManager.find(Autor.class, id);
            if (autor == null) {
                System.out.println("Autor não encontrado.");
                return;
            }

            // Verifica se já existe outro autor com o nome informado
            List<Autor> autoresComMesmoNome = entityManager.createQuery(
                    "SELECT a FROM Autor a WHERE LOWER(a.nome) = LOWER(:nome) AND a.id <> :id",
                    Autor.class)
                    .setParameter("nome", novoNome)
                    .setParameter("id", id)
                    .getResultList();

            // Impede a duplicação de nome entre autores diferentes
            if (!autoresComMesmoNome.isEmpty()) {
                System.out.println("Já existe outro autor com esse nome.");
                return;
            }

            // Atualiza o nome do autor e persiste a alteração
            transaction.begin();
            autor.setNome(novoNome);
            entityManager.merge(autor);
            transaction.commit();
            System.out.println("Autor atualizado com sucesso.");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.out.println("Erro ao atualizar autor: " + ServiceUtil.extrairMensagemErro(e));
        } finally {
            entityManager.close();
        }
    }

    // Busca autores cujo nome contenha o termo informado, sem distinção de maiúsculas
    public List<Autor> buscarAutoresPorNome(String nome) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        try {
            return entityManager.createQuery(
                    "SELECT a FROM Autor a WHERE LOWER(a.nome) LIKE LOWER(:nome) ORDER BY a.nome",
                    Autor.class)
                    .setParameter("nome", "%" + nome + "%")
                    .getResultList();
        } catch (Exception e) {
            System.out.println("Erro ao buscar autores por nome: " + ServiceUtil.extrairMensagemErro(e));
            return List.of();
        } finally {
            entityManager.close();
        }
    }
}