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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.unisales.database.table.Autor;
import br.unisales.database.table.Categoria;
import br.unisales.database.table.Exemplar;
import br.unisales.database.table.Livro;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

@ExtendWith(MockitoExtension.class)
public class CatalogoServiceTest {

        @Mock
        private EntityManagerFactory entityManagerFactory;

        @Mock
        private EntityManager entityManager;

        @Mock
        private EntityTransaction entityTransaction;

        @Mock
        private TypedQuery<Categoria> queryDeCategoria;

        @Mock
        private TypedQuery<Autor> queryDeAutor;

        @Mock
        private TypedQuery<Livro> queryDeLivro;

        @Mock
        private TypedQuery<Livro> queryDeLivroCategoria;

        @Mock
        private TypedQuery<Exemplar> queryDeExemplar;

        @Mock
        private TypedQuery<Long> queryDeContagemDeEmprestimo;

        @Mock
        private TypedQuery<Long> queryDeContagemDeReserva;

        @Mock
        private TypedQuery<Long> queryDeContagemDeExemplar;

        @Mock
        private TypedQuery<Long> queryDeContagemDeLivroAutor;

        @Mock
        private TypedQuery<Long> queryDeMaximoId;

        private CatalogoService catalogoService;

        @BeforeEach
        void setUp() {
                when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);
                lenient().when(entityManager.getTransaction()).thenReturn(entityTransaction);
                catalogoService = new CatalogoService(entityManagerFactory);
        }

        // =====================================================================
        // cadastrarAutor
        // =====================================================================

        @Test
        @DisplayName("Não deve persistir autor quando já existe um com o mesmo nome")
        void naoDevePersistirAutorQuandoJaExisteComMesmoNome() {
                Autor autorJaExistente = Autor.builder().id(10L).nome("Autor Existente").build();

                when(entityManager.createQuery(
                                "SELECT a FROM Autor a WHERE LOWER(a.nome) = LOWER(:nome)",
                                Autor.class))
                                .thenReturn(queryDeAutor);
                when(queryDeAutor.setParameter("nome", "Autor Existente")).thenReturn(queryDeAutor);
                when(queryDeAutor.getResultList()).thenReturn(List.of(autorJaExistente));

                catalogoService.cadastrarAutor("Autor Existente");

                verify(entityManager, never()).persist(any(Autor.class));
        }

        @Test
        @DisplayName("Deve persistir novo autor com id e nome corretos quando nome não está cadastrado")
        void devePersistirNovoAutorComIdENomeCorretosQuandoNomeNaoEstaCadastrado() {
                when(entityManager.createQuery(
                                "SELECT a FROM Autor a WHERE LOWER(a.nome) = LOWER(:nome)",
                                Autor.class))
                                .thenReturn(queryDeAutor);
                when(queryDeAutor.setParameter("nome", "Autor Novo")).thenReturn(queryDeAutor);
                when(queryDeAutor.getResultList()).thenReturn(Collections.emptyList());

                when(entityManager.createQuery("SELECT MAX(a.id) FROM Autor a", Long.class))
                                .thenReturn(queryDeMaximoId);
                when(queryDeMaximoId.getSingleResult()).thenReturn(5L);

                catalogoService.cadastrarAutor("Autor Novo");

                // Verifica que o autor persistido tem o próximo id (5 + 1) e o nome correto
                verify(entityManager).persist(Autor.builder().id(6L).nome("Autor Novo").build());
        }

        // =====================================================================
        // cadastrarLivro
        // =====================================================================

        @Test
        @DisplayName("Não deve persistir livro quando ISBN já está cadastrado")
        void naoDevePersistirLivroQuandoIsbnJaEstaCadastrado() {
                Livro livroJaExistente = Livro.builder().isbn("978-1111").titulo("Livro Existente").build();
                Livro novoLivroComIsbnDuplicado = Livro.builder().isbn("978-1111").titulo("Outro Livro").build();

                when(entityManager.find(Livro.class, "978-1111")).thenReturn(livroJaExistente);

                catalogoService.cadastrarLivro(novoLivroComIsbnDuplicado, "Qualquer Autor", "Qualquer Categoria");

                verify(entityManager, never()).persist(novoLivroComIsbnDuplicado);
        }

        @Test
        @DisplayName("Não deve persistir livro quando autor informado não existe no banco")
        void naoDevePersistirLivroQuandoAutorNaoExisteNoBanco() {
                Livro novoLivro = Livro.builder().isbn("978-2222").titulo("Livro Sem Autor").build();

                when(entityManager.find(Livro.class, "978-2222")).thenReturn(null);

                when(entityManager.createQuery(
                                "SELECT a FROM Autor a WHERE LOWER(a.nome) = LOWER(:nome)",
                                Autor.class))
                                .thenReturn(queryDeAutor);
                when(queryDeAutor.setParameter("nome", "Autor Inexistente")).thenReturn(queryDeAutor);
                when(queryDeAutor.getResultList()).thenReturn(Collections.emptyList());

                catalogoService.cadastrarLivro(novoLivro, "Autor Inexistente", "Qualquer Categoria");

                verify(entityManager, never()).persist(novoLivro);
        }

        @Test
        @DisplayName("Não deve persistir livro quando categoria informada não existe no banco")
        void naoDevePersistirLivroQuandoCategoriaNaoExisteNoBanco() {
                Livro novoLivro = Livro.builder().isbn("978-3333").titulo("Livro Sem Categoria").build();
                Autor autorExistente = Autor.builder().id(10L).nome("Autor Existente").build();

                when(entityManager.find(Livro.class, "978-3333")).thenReturn(null);

                when(entityManager.createQuery(
                                "SELECT a FROM Autor a WHERE LOWER(a.nome) = LOWER(:nome)",
                                Autor.class))
                                .thenReturn(queryDeAutor);
                when(queryDeAutor.setParameter("nome", "Autor Existente")).thenReturn(queryDeAutor);
                when(queryDeAutor.getResultList()).thenReturn(List.of(autorExistente));

                when(entityManager.createQuery(
                                "SELECT c FROM Categoria c WHERE LOWER(c.nome) = LOWER(:nome)",
                                Categoria.class))
                                .thenReturn(queryDeCategoria);
                when(queryDeCategoria.setParameter("nome", "Categoria Inexistente")).thenReturn(queryDeCategoria);
                when(queryDeCategoria.getResultList()).thenReturn(Collections.emptyList());

                catalogoService.cadastrarLivro(novoLivro, "Autor Existente", "Categoria Inexistente");

                verify(entityManager, never()).persist(novoLivro);
        }

        @Test
        @DisplayName("Deve persistir livro e associações quando autor e categoria já existem")
        void devePersistirLivroEAssociacoesQuandoAutorECategoriaJaExistem() {
                Livro novoLivro = Livro.builder().isbn("978-4444").titulo("Livro Com Autor Existente").build();
                Autor autorJaExistente = Autor.builder().id(10L).nome("Autor Existente").build();
                Categoria categoriaExistente = Categoria.builder().id(2L).nome("Ficção").build();

                when(entityManager.find(Livro.class, "978-4444")).thenReturn(null);

                when(entityManager.createQuery(
                                "SELECT a FROM Autor a WHERE LOWER(a.nome) = LOWER(:nome)",
                                Autor.class))
                                .thenReturn(queryDeAutor);
                when(queryDeAutor.setParameter("nome", "Autor Existente")).thenReturn(queryDeAutor);
                when(queryDeAutor.getResultList()).thenReturn(List.of(autorJaExistente));

                when(entityManager.createQuery(
                                "SELECT c FROM Categoria c WHERE LOWER(c.nome) = LOWER(:nome)",
                                Categoria.class))
                                .thenReturn(queryDeCategoria);
                when(queryDeCategoria.setParameter("nome", "Ficção")).thenReturn(queryDeCategoria);
                when(queryDeCategoria.getResultList()).thenReturn(List.of(categoriaExistente));

                catalogoService.cadastrarLivro(novoLivro, "Autor Existente", "Ficção");

                verify(entityManager, atLeastOnce()).persist(novoLivro);
                verify(entityManager, never()).persist(any(Autor.class));
        }

        // =====================================================================
        // cadastrarExemplar
        // =====================================================================

        @Test
        @DisplayName("Deve persistir exemplar e atribuir ID gerado quando todos os dados são válidos")
        void devePersistirExemplarEAtribuirIdGeradoQuandoTodosOsDadosSaoValidos() {
                Livro livroVinculado = Livro.builder().isbn("978-5555").titulo("Livro Base").build();
                Exemplar novoExemplar = Exemplar.builder().livro(livroVinculado).build();

                when(entityManager.createQuery("SELECT MAX(e.id) FROM Exemplar e", Long.class))
                                .thenReturn(queryDeMaximoId);
                when(queryDeMaximoId.getSingleResult()).thenReturn(3L);

                catalogoService.cadastrarExemplar(novoExemplar);

                verify(entityManager).persist(novoExemplar);
                assertEquals(4L, novoExemplar.getId());
        }

        // =====================================================================
        // removerLivro
        // =====================================================================

        @Test
        @DisplayName("Não deve remover livro quando ISBN não corresponde a nenhum registro")
        void naoDeveRemoverLivroQuandoIsbnNaoCorrespondeANenhumRegistro() {
                when(entityManager.find(Livro.class, "978-0000")).thenReturn(null);

                catalogoService.removerLivro("978-0000");

                verify(entityManager, never()).remove(any());
        }

        @Test
        @DisplayName("Não deve remover livro quando possui exemplares cadastrados")
        void naoDeveRemoverLivroQuandoPossuiExemplaresCadastrados() {
                Livro livroComExemplares = Livro.builder().isbn("978-1234").titulo("Livro Com Exemplares").build();

                when(entityManager.find(Livro.class, "978-1234")).thenReturn(livroComExemplares);

                when(entityManager.createQuery(
                                "SELECT COUNT(e) FROM Exemplar e WHERE e.livro.isbn = :isbn",
                                Long.class))
                                .thenReturn(queryDeContagemDeExemplar);
                when(queryDeContagemDeExemplar.setParameter("isbn", "978-1234"))
                                .thenReturn(queryDeContagemDeExemplar);
                when(queryDeContagemDeExemplar.getSingleResult()).thenReturn(2L);

                catalogoService.removerLivro("978-1234");

                verify(entityManager, never()).remove(livroComExemplares);
        }

        @Test
        @DisplayName("Não deve remover livro quando existe histórico de empréstimos vinculado")
        void naoDeveRemoverLivroQuandoExisteHistoricoDeEmprestimosVinculado() {
                Livro livroComHistorico = Livro.builder().isbn("978-1234").titulo("Livro Com Histórico").build();

                when(entityManager.find(Livro.class, "978-1234")).thenReturn(livroComHistorico);

                when(entityManager.createQuery(
                                "SELECT COUNT(e) FROM Exemplar e WHERE e.livro.isbn = :isbn",
                                Long.class))
                                .thenReturn(queryDeContagemDeExemplar);
                when(queryDeContagemDeExemplar.setParameter("isbn", "978-1234"))
                                .thenReturn(queryDeContagemDeExemplar);
                when(queryDeContagemDeExemplar.getSingleResult()).thenReturn(0L);

                when(entityManager.createQuery(
                                "SELECT COUNT(e) FROM Emprestimo e WHERE e.exemplar.livro.isbn = :isbn",
                                Long.class))
                                .thenReturn(queryDeContagemDeEmprestimo);
                when(queryDeContagemDeEmprestimo.setParameter("isbn", "978-1234"))
                                .thenReturn(queryDeContagemDeEmprestimo);
                when(queryDeContagemDeEmprestimo.getSingleResult()).thenReturn(3L);

                lenient().when(entityManager.createQuery(
                                "SELECT COUNT(r) FROM Reserva r WHERE r.isbnLivro = :isbn AND r.status = 'RESERVADO'",
                                Long.class))
                                .thenReturn(queryDeContagemDeReserva);

                catalogoService.removerLivro("978-1234");

                verify(entityManager, never()).remove(livroComHistorico);
        }

        @Test
        @DisplayName("Não deve remover livro quando existem reservas pendentes para ele")
        void naoDeveRemoverLivroQuandoExistemReservasPendentesParaEle() {
                Livro livroComReservas = Livro.builder().isbn("978-1234").titulo("Livro Com Reservas").build();

                when(entityManager.find(Livro.class, "978-1234")).thenReturn(livroComReservas);

                when(entityManager.createQuery(
                                "SELECT COUNT(e) FROM Exemplar e WHERE e.livro.isbn = :isbn",
                                Long.class))
                                .thenReturn(queryDeContagemDeExemplar);
                when(queryDeContagemDeExemplar.setParameter("isbn", "978-1234"))
                                .thenReturn(queryDeContagemDeExemplar);
                when(queryDeContagemDeExemplar.getSingleResult()).thenReturn(0L);

                when(entityManager.createQuery(
                                "SELECT COUNT(e) FROM Emprestimo e WHERE e.exemplar.livro.isbn = :isbn",
                                Long.class))
                                .thenReturn(queryDeContagemDeEmprestimo);
                when(queryDeContagemDeEmprestimo.setParameter("isbn", "978-1234"))
                                .thenReturn(queryDeContagemDeEmprestimo);
                when(queryDeContagemDeEmprestimo.getSingleResult()).thenReturn(0L);

                when(entityManager.createQuery(
                                "SELECT COUNT(r) FROM Reserva r WHERE r.isbnLivro = :isbn AND r.status = 'RESERVADO'",
                                Long.class))
                                .thenReturn(queryDeContagemDeReserva);
                when(queryDeContagemDeReserva.setParameter("isbn", "978-1234"))
                                .thenReturn(queryDeContagemDeReserva);
                when(queryDeContagemDeReserva.getSingleResult()).thenReturn(2L);

                catalogoService.removerLivro("978-1234");

                verify(entityManager, never()).remove(livroComReservas);
        }

        @Test
        @DisplayName("Deve remover livro quando não há exemplares, empréstimos nem reservas pendentes")
        void deveRemoverLivroQuandoNaoHaExemplaresEmprestimosNemReservasPendentes() {
                Livro livroSemVinculos = Livro.builder().isbn("978-9876").titulo("Livro Sem Vínculos").build();

                when(entityManager.find(Livro.class, "978-9876")).thenReturn(livroSemVinculos);

                when(entityManager.createQuery(
                                "SELECT COUNT(e) FROM Exemplar e WHERE e.livro.isbn = :isbn",
                                Long.class))
                                .thenReturn(queryDeContagemDeExemplar);
                when(queryDeContagemDeExemplar.setParameter("isbn", "978-9876"))
                                .thenReturn(queryDeContagemDeExemplar);
                when(queryDeContagemDeExemplar.getSingleResult()).thenReturn(0L);

                when(entityManager.createQuery(
                                "SELECT COUNT(e) FROM Emprestimo e WHERE e.exemplar.livro.isbn = :isbn",
                                Long.class))
                                .thenReturn(queryDeContagemDeEmprestimo);
                when(queryDeContagemDeEmprestimo.setParameter("isbn", "978-9876"))
                                .thenReturn(queryDeContagemDeEmprestimo);
                when(queryDeContagemDeEmprestimo.getSingleResult()).thenReturn(0L);

                when(entityManager.createQuery(
                                "SELECT COUNT(r) FROM Reserva r WHERE r.isbnLivro = :isbn AND r.status = 'RESERVADO'",
                                Long.class))
                                .thenReturn(queryDeContagemDeReserva);
                when(queryDeContagemDeReserva.setParameter("isbn", "978-9876"))
                                .thenReturn(queryDeContagemDeReserva);
                when(queryDeContagemDeReserva.getSingleResult()).thenReturn(0L);

                catalogoService.removerLivro("978-9876");

                verify(entityManager).remove(livroSemVinculos);
        }

        // =====================================================================
        // removerExemplar
        // =====================================================================

        @Test
        @DisplayName("Não deve remover exemplar quando ID não corresponde a nenhum registro")
        void naoDeveRemoverExemplarQuandoIdNaoCorrespondeANenhumRegistro() {
                when(entityManager.find(Exemplar.class, 99L)).thenReturn(null);

                catalogoService.removerExemplar(99L);

                verify(entityManager, never()).remove(any());
        }

        @Test
        @DisplayName("Não deve remover exemplar quando existe histórico de empréstimos vinculado a ele")
        void naoDeveRemoverExemplarQuandoExisteHistoricoDeEmprestimosVinculadoAEle() {
                Exemplar exemplarComHistorico = Exemplar.builder().id(1L).build();

                when(entityManager.find(Exemplar.class, 1L)).thenReturn(exemplarComHistorico);

                when(entityManager.createQuery(
                                "SELECT COUNT(e) FROM Emprestimo e WHERE e.exemplar.id = :id",
                                Long.class))
                                .thenReturn(queryDeContagemDeEmprestimo);
                when(queryDeContagemDeEmprestimo.setParameter("id", 1L))
                                .thenReturn(queryDeContagemDeEmprestimo);
                when(queryDeContagemDeEmprestimo.getSingleResult()).thenReturn(5L);

                lenient().when(entityManager.createQuery(
                                "SELECT COUNT(r) FROM Reserva r WHERE r.isbnLivro = :isbn AND r.status = 'RESERVADO'",
                                Long.class))
                                .thenReturn(queryDeContagemDeReserva);

                catalogoService.removerExemplar(1L);

                verify(entityManager, never()).remove(exemplarComHistorico);
        }

        @Test
        @DisplayName("Não deve remover exemplar quando livro vinculado tem reservas pendentes")
        void naoDeveRemoverExemplarQuandoLivroVinculadoTemReservasPendentes() {
                Livro livroDoExemplar = Livro.builder().isbn("978-1234").build();
                Exemplar exemplarComReservaNoLivro = Exemplar.builder().id(1L).livro(livroDoExemplar).build();

                when(entityManager.find(Exemplar.class, 1L)).thenReturn(exemplarComReservaNoLivro);

                when(entityManager.createQuery(
                                "SELECT COUNT(e) FROM Emprestimo e WHERE e.exemplar.id = :id",
                                Long.class))
                                .thenReturn(queryDeContagemDeEmprestimo);
                when(queryDeContagemDeEmprestimo.setParameter("id", 1L))
                                .thenReturn(queryDeContagemDeEmprestimo);
                when(queryDeContagemDeEmprestimo.getSingleResult()).thenReturn(0L);

                when(entityManager.createQuery(
                                "SELECT COUNT(r) FROM Reserva r WHERE r.isbnLivro = :isbn AND r.status = 'RESERVADO'",
                                Long.class))
                                .thenReturn(queryDeContagemDeReserva);
                when(queryDeContagemDeReserva.setParameter("isbn", "978-1234"))
                                .thenReturn(queryDeContagemDeReserva);
                when(queryDeContagemDeReserva.getSingleResult()).thenReturn(1L);

                catalogoService.removerExemplar(1L);

                verify(entityManager, never()).remove(exemplarComReservaNoLivro);
        }

        @Test
        @DisplayName("Deve remover exemplar quando não há empréstimos nem reservas vinculados")
        void deveRemoverExemplarQuandoNaoHaEmprestimosNemReservasVinculados() {
                Livro livroDoExemplar = Livro.builder().isbn("978-5555").build();
                Exemplar exemplarSemVinculos = Exemplar.builder().id(2L).livro(livroDoExemplar).build();

                when(entityManager.find(Exemplar.class, 2L)).thenReturn(exemplarSemVinculos);

                when(entityManager.createQuery(
                                "SELECT COUNT(e) FROM Emprestimo e WHERE e.exemplar.id = :id",
                                Long.class))
                                .thenReturn(queryDeContagemDeEmprestimo);
                when(queryDeContagemDeEmprestimo.setParameter("id", 2L))
                                .thenReturn(queryDeContagemDeEmprestimo);
                when(queryDeContagemDeEmprestimo.getSingleResult()).thenReturn(0L);

                when(entityManager.createQuery(
                                "SELECT COUNT(r) FROM Reserva r WHERE r.isbnLivro = :isbn AND r.status = 'RESERVADO'",
                                Long.class))
                                .thenReturn(queryDeContagemDeReserva);
                when(queryDeContagemDeReserva.setParameter("isbn", "978-5555"))
                                .thenReturn(queryDeContagemDeReserva);
                when(queryDeContagemDeReserva.getSingleResult()).thenReturn(0L);

                catalogoService.removerExemplar(2L);

                verify(entityManager).remove(exemplarSemVinculos);
        }

        // =====================================================================
        // removerAutor
        // =====================================================================

        @Test
        @DisplayName("Não deve remover autor quando ID não corresponde a nenhum registro")
        void naoDeveRemoverAutorQuandoIdNaoCorrespondeANenhumRegistro() {
                when(entityManager.find(Autor.class, 1L)).thenReturn(null);

                catalogoService.removerAutor(1L);

                verify(entityManager, never()).remove(any());
        }

        @Test
        @DisplayName("Não deve remover autor quando possui livros vinculados")
        void naoDeveRemoverAutorQuandoPossuiLivrosVinculados() {
                Autor autorComVinculos = Autor.builder().id(1L).nome("Autor Com Vínculos").build();

                when(entityManager.find(Autor.class, 1L)).thenReturn(autorComVinculos);

                when(entityManager.createQuery(
                                "SELECT COUNT(la) FROM LivroAutor la WHERE la.autor.id = :id",
                                Long.class))
                                .thenReturn(queryDeContagemDeLivroAutor);
                when(queryDeContagemDeLivroAutor.setParameter("id", 1L))
                                .thenReturn(queryDeContagemDeLivroAutor);
                when(queryDeContagemDeLivroAutor.getSingleResult()).thenReturn(2L);

                catalogoService.removerAutor(1L);

                verify(entityManager, never()).remove(autorComVinculos);
        }

        @Test
        @DisplayName("Deve remover autor quando não possui livros vinculados")
        void deveRemoverAutorQuandoNaoPossuiLivrosVinculados() {
                Autor autorSemVinculos = Autor.builder().id(2L).nome("Autor Sem Vínculos").build();

                when(entityManager.find(Autor.class, 2L)).thenReturn(autorSemVinculos);

                when(entityManager.createQuery(
                                "SELECT COUNT(la) FROM LivroAutor la WHERE la.autor.id = :id",
                                Long.class))
                                .thenReturn(queryDeContagemDeLivroAutor);
                when(queryDeContagemDeLivroAutor.setParameter("id", 2L))
                                .thenReturn(queryDeContagemDeLivroAutor);
                when(queryDeContagemDeLivroAutor.getSingleResult()).thenReturn(0L);

                catalogoService.removerAutor(2L);

                verify(entityManager).remove(autorSemVinculos);
        }

        // =====================================================================
        // buscarLivroPorIsbn
        // =====================================================================

        @Test
        @DisplayName("Deve retornar livro quando ISBN corresponde a um registro existente")
        void deveRetornarLivroQuandoIsbnCorrespondeAUmRegistroExistente() {
                Livro livroEsperado = Livro.builder().isbn("978-7777").titulo("Livro Encontrado").build();

                when(entityManager.createQuery(
                                "SELECT DISTINCT l FROM Livro l " +
                                                "LEFT JOIN FETCH l.livroAutores la LEFT JOIN FETCH la.autor " +
                                                "WHERE l.isbn = :isbn",
                                Livro.class))
                                .thenReturn(queryDeLivro);
                when(queryDeLivro.setParameter(eq("isbn"), any())).thenReturn(queryDeLivro);
                when(queryDeLivro.getResultList()).thenReturn(List.of(livroEsperado));

                when(entityManager.createQuery(
                                "SELECT DISTINCT l FROM Livro l " +
                                                "LEFT JOIN FETCH l.livroCategorias lc LEFT JOIN FETCH lc.categoria " +
                                                "WHERE l.isbn = :isbn",
                                Livro.class))
                                .thenReturn(queryDeLivroCategoria);
                when(queryDeLivroCategoria.setParameter(eq("isbn"), any())).thenReturn(queryDeLivroCategoria);
                when(queryDeLivroCategoria.getResultList()).thenReturn(List.of(livroEsperado));

                Livro livroRetornado = catalogoService.buscarLivroPorIsbn("978-7777");

                assertNotNull(livroRetornado);
                assertEquals("978-7777", livroRetornado.getIsbn());
                assertEquals("Livro Encontrado", livroRetornado.getTitulo());
        }

        @Test
        @DisplayName("Deve retornar null quando ISBN não corresponde a nenhum registro")
        void deveRetornarNullQuandoIsbnNaoCorrespondeANenhumRegistro() {
                when(entityManager.createQuery(
                                "SELECT DISTINCT l FROM Livro l " +
                                                "LEFT JOIN FETCH l.livroAutores la LEFT JOIN FETCH la.autor " +
                                                "WHERE l.isbn = :isbn",
                                Livro.class))
                                .thenReturn(queryDeLivro);
                when(queryDeLivro.setParameter(eq("isbn"), any())).thenReturn(queryDeLivro);
                when(queryDeLivro.getResultList()).thenReturn(Collections.emptyList());

                lenient().when(entityManager.createQuery(
                                "SELECT DISTINCT l FROM Livro l " +
                                                "LEFT JOIN FETCH l.livroCategorias lc LEFT JOIN FETCH lc.categoria " +
                                                "WHERE l.isbn = :isbn",
                                Livro.class))
                                .thenReturn(queryDeLivroCategoria);

                Livro livroRetornado = catalogoService.buscarLivroPorIsbn("000-0000");

                assertNull(livroRetornado);
        }

        // =====================================================================
        // buscarLivrosPorTitulo
        // =====================================================================

        @Test
        @DisplayName("Deve retornar lista de livros quando título parcial corresponde a registros existentes")
        void deveRetornarListaDeLivrosQuandoTituloParcialCorrespondeARegistrosExistentes() {
                Livro primeiroLivro = Livro.builder().isbn("978-1000").titulo("Java na Prática").build();
                Livro segundoLivro = Livro.builder().isbn("978-1001").titulo("Java Avançado").build();

                when(entityManager.createQuery(
                                "SELECT DISTINCT l FROM Livro l " +
                                                "LEFT JOIN FETCH l.livroAutores la LEFT JOIN FETCH la.autor " +
                                                "WHERE LOWER(l.titulo) LIKE LOWER(:titulo) ORDER BY l.titulo",
                                Livro.class))
                                .thenReturn(queryDeLivro);
                when(queryDeLivro.setParameter(eq("titulo"), any())).thenReturn(queryDeLivro);
                when(queryDeLivro.getResultList()).thenReturn(List.of(primeiroLivro, segundoLivro));

                when(entityManager.createQuery(
                                "SELECT DISTINCT l FROM Livro l " +
                                                "LEFT JOIN FETCH l.livroCategorias lc LEFT JOIN FETCH lc.categoria " +
                                                "WHERE LOWER(l.titulo) LIKE LOWER(:titulo)",
                                Livro.class))
                                .thenReturn(queryDeLivroCategoria);
                when(queryDeLivroCategoria.setParameter(eq("titulo"), any())).thenReturn(queryDeLivroCategoria);
                when(queryDeLivroCategoria.getResultList()).thenReturn(List.of(primeiroLivro, segundoLivro));

                List<Livro> livrosRetornados = catalogoService.buscarLivrosPorTitulo("Java");

                assertNotNull(livrosRetornados);
                assertEquals(2, livrosRetornados.size());
                assertEquals("Java na Prática", livrosRetornados.get(0).getTitulo());
                assertEquals("Java Avançado", livrosRetornados.get(1).getTitulo());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando nenhum título corresponde ao termo buscado")
        void deveRetornarListaVaziaQuandoNenhumTituloCorrespondeAoTermoBuscado() {
                when(entityManager.createQuery(
                                "SELECT DISTINCT l FROM Livro l " +
                                                "LEFT JOIN FETCH l.livroAutores la LEFT JOIN FETCH la.autor " +
                                                "WHERE LOWER(l.titulo) LIKE LOWER(:titulo) ORDER BY l.titulo",
                                Livro.class))
                                .thenReturn(queryDeLivro);
                when(queryDeLivro.setParameter(eq("titulo"), any())).thenReturn(queryDeLivro);
                when(queryDeLivro.getResultList()).thenReturn(Collections.emptyList());

                when(entityManager.createQuery(
                                "SELECT DISTINCT l FROM Livro l " +
                                                "LEFT JOIN FETCH l.livroCategorias lc LEFT JOIN FETCH lc.categoria " +
                                                "WHERE LOWER(l.titulo) LIKE LOWER(:titulo)",
                                Livro.class))
                                .thenReturn(queryDeLivroCategoria);
                when(queryDeLivroCategoria.setParameter(eq("titulo"), any())).thenReturn(queryDeLivroCategoria);
                when(queryDeLivroCategoria.getResultList()).thenReturn(Collections.emptyList());

                List<Livro> livrosRetornados = catalogoService.buscarLivrosPorTitulo("Título Inexistente");

                assertTrue(livrosRetornados.isEmpty());
        }

        // =====================================================================
        // listarLivros
        // =====================================================================

        @Test
        @DisplayName("Deve retornar todos os livros quando existem registros no banco")
        void deveRetornarTodosOsLivrosQuandoExistemRegistrosNoBanco() {
                Livro primeiroLivro = Livro.builder().isbn("978-2000").titulo("Livro A").build();
                Livro segundoLivro = Livro.builder().isbn("978-2001").titulo("Livro B").build();
                Livro terceiroLivro = Livro.builder().isbn("978-2002").titulo("Livro C").build();

                when(entityManager.createQuery(
                                "SELECT DISTINCT l FROM Livro l LEFT JOIN FETCH l.livroAutores la LEFT JOIN FETCH la.autor",
                                Livro.class))
                                .thenReturn(queryDeLivro);
                when(queryDeLivro.getResultList())
                                .thenReturn(List.of(primeiroLivro, segundoLivro, terceiroLivro));

                when(entityManager.createQuery(
                                "SELECT DISTINCT l FROM Livro l LEFT JOIN FETCH l.livroCategorias lc LEFT JOIN FETCH lc.categoria",
                                Livro.class))
                                .thenReturn(queryDeLivroCategoria);
                when(queryDeLivroCategoria.getResultList())
                                .thenReturn(List.of(primeiroLivro, segundoLivro, terceiroLivro));

                List<Livro> livrosRetornados = catalogoService.listarLivros();

                assertNotNull(livrosRetornados);
                assertEquals(3, livrosRetornados.size());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há livros cadastrados no banco")
        void deveRetornarListaVaziaQuandoNaoHaLivrosCadastradosNoBanco() {
                when(entityManager.createQuery(
                                "SELECT DISTINCT l FROM Livro l LEFT JOIN FETCH l.livroAutores la LEFT JOIN FETCH la.autor",
                                Livro.class))
                                .thenReturn(queryDeLivro);
                when(queryDeLivro.getResultList()).thenReturn(Collections.emptyList());

                when(entityManager.createQuery(
                                "SELECT DISTINCT l FROM Livro l LEFT JOIN FETCH l.livroCategorias lc LEFT JOIN FETCH lc.categoria",
                                Livro.class))
                                .thenReturn(queryDeLivroCategoria);
                when(queryDeLivroCategoria.getResultList()).thenReturn(Collections.emptyList());

                List<Livro> livrosRetornados = catalogoService.listarLivros();

                assertTrue(livrosRetornados.isEmpty());
        }

        // =====================================================================
        // listarExemplares
        // =====================================================================

        @Test
        @DisplayName("Deve retornar lista de exemplares quando existem exemplares cadastrados para o ISBN")
        void deveRetornarListaDeExemplaresQuandoExistemExemplaresCadastradosParaOIsbn() {
                Livro livroBase = Livro.builder().isbn("978-3000").titulo("Livro Base").build();
                Exemplar primeiroExemplar = Exemplar.builder().id(1L).livro(livroBase).build();
                Exemplar segundoExemplar = Exemplar.builder().id(2L).livro(livroBase).build();

                when(entityManager.createQuery(
                                "SELECT e FROM Exemplar e WHERE e.livro.isbn = :isbn ORDER BY e.id",
                                Exemplar.class))
                                .thenReturn(queryDeExemplar);
                when(queryDeExemplar.setParameter("isbn", "978-3000")).thenReturn(queryDeExemplar);
                when(queryDeExemplar.getResultList()).thenReturn(List.of(primeiroExemplar, segundoExemplar));

                List<Exemplar> exemplaresRetornados = catalogoService.listarExemplares("978-3000");

                assertNotNull(exemplaresRetornados);
                assertEquals(2, exemplaresRetornados.size());
                assertEquals(1L, exemplaresRetornados.get(0).getId());
                assertEquals(2L, exemplaresRetornados.get(1).getId());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há exemplares cadastrados para o ISBN")
        void deveRetornarListaVaziaQuandoNaoHaExemplaresCadastradosParaOIsbn() {
                when(entityManager.createQuery(
                                "SELECT e FROM Exemplar e WHERE e.livro.isbn = :isbn ORDER BY e.id",
                                Exemplar.class))
                                .thenReturn(queryDeExemplar);
                when(queryDeExemplar.setParameter("isbn", "978-0000")).thenReturn(queryDeExemplar);
                when(queryDeExemplar.getResultList()).thenReturn(Collections.emptyList());

                List<Exemplar> exemplaresRetornados = catalogoService.listarExemplares("978-0000");

                assertTrue(exemplaresRetornados.isEmpty());
        }

        // =====================================================================
        // atualizarAutor
        // =====================================================================

        @Test
        @DisplayName("Não deve atualizar autor quando ele não existe no banco")
        void naoDeveAtualizarAutorQuandoEleNaoExisteNoBanco() {
                when(entityManager.find(Autor.class, 1L)).thenReturn(null);

                catalogoService.atualizarAutor(1L, "Novo Nome");

                verify(entityManager, never()).merge(any(Autor.class));
                verify(entityTransaction, never()).begin();
        }

        @Test
        @DisplayName("Não deve atualizar autor quando já existe outro com o mesmo nome")
        void naoDeveAtualizarAutorQuandoJaExisteOutroComMesmoNome() {
                Autor autorExistente = Autor.builder().id(1L).nome("Nome Antigo").build();
                Autor outroAutor = Autor.builder().id(2L).nome("Nome Novo").build();

                when(entityManager.find(Autor.class, 1L)).thenReturn(autorExistente);

                when(entityManager.createQuery(
                                "SELECT a FROM Autor a WHERE LOWER(a.nome) = LOWER(:nome) AND a.id <> :id",
                                Autor.class))
                                .thenReturn(queryDeAutor);
                when(queryDeAutor.setParameter("nome", "Nome Novo")).thenReturn(queryDeAutor);
                when(queryDeAutor.setParameter("id", 1L)).thenReturn(queryDeAutor);
                when(queryDeAutor.getResultList()).thenReturn(List.of(outroAutor));

                catalogoService.atualizarAutor(1L, "Nome Novo");

                verify(entityManager, never()).merge(any(Autor.class));
                verify(entityTransaction, never()).begin();
        }

        @Test
        @DisplayName("Deve atualizar o nome do autor quando ele existe e o nome não está em uso")
        void deveAtualizarNomeDoAutorQuandoEleExisteENomeNaoEstaEmUso() {
                Autor autorExistente = Autor.builder().id(1L).nome("Nome Antigo").build();

                when(entityManager.find(Autor.class, 1L)).thenReturn(autorExistente);

                when(entityManager.createQuery(
                                "SELECT a FROM Autor a WHERE LOWER(a.nome) = LOWER(:nome) AND a.id <> :id",
                                Autor.class))
                                .thenReturn(queryDeAutor);
                when(queryDeAutor.setParameter("nome", "Nome Novo")).thenReturn(queryDeAutor);
                when(queryDeAutor.setParameter("id", 1L)).thenReturn(queryDeAutor);
                when(queryDeAutor.getResultList()).thenReturn(Collections.emptyList());

                catalogoService.atualizarAutor(1L, "Nome Novo");

                assertEquals("Nome Novo", autorExistente.getNome());
                verify(entityTransaction).begin();
                verify(entityManager).merge(autorExistente);
                verify(entityTransaction).commit();
        }

        // =====================================================================
        // buscarAutoresPorNome
        // =====================================================================

        @Test
        @DisplayName("Deve retornar lista vazia quando nenhum autor corresponder ao nome informado")
        void deveRetornarListaVaziaQuandoNenhumAutorCorresponderAoNomeInformado() {
                when(entityManager.createQuery(
                                "SELECT a FROM Autor a WHERE LOWER(a.nome) LIKE LOWER(:nome) ORDER BY a.nome",
                                Autor.class))
                                .thenReturn(queryDeAutor);
                when(queryDeAutor.setParameter("nome", "%Inexistente%")).thenReturn(queryDeAutor);
                when(queryDeAutor.getResultList()).thenReturn(Collections.emptyList());

                List<Autor> resultado = catalogoService.buscarAutoresPorNome("Inexistente");

                assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Deve retornar autores cujo nome contenha o termo informado")
        void deveRetornarAutoresCujoNomeContenhaOTermoInformado() {
                Autor primeiroAutor = Autor.builder().id(1L).nome("Machado de Assis").build();
                Autor segundoAutor = Autor.builder().id(2L).nome("Maria Machado").build();

                when(entityManager.createQuery(
                                "SELECT a FROM Autor a WHERE LOWER(a.nome) LIKE LOWER(:nome) ORDER BY a.nome",
                                Autor.class))
                                .thenReturn(queryDeAutor);
                when(queryDeAutor.setParameter("nome", "%Machado%")).thenReturn(queryDeAutor);
                when(queryDeAutor.getResultList()).thenReturn(List.of(primeiroAutor, segundoAutor));

                List<Autor> resultado = catalogoService.buscarAutoresPorNome("Machado");

                assertEquals(2, resultado.size());
                assertEquals("Machado de Assis", resultado.get(0).getNome());
                assertEquals("Maria Machado", resultado.get(1).getNome());
        }
}