package br.unisales.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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

import br.unisales.database.table.Categoria;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

@ExtendWith(MockitoExtension.class)
public class CategoriaServiceTest {

        @Mock
        private EntityManagerFactory entityManagerFactory;

        @Mock
        private EntityManager entityManager;

        @Mock
        private EntityTransaction entityTransaction;

        @Mock
        private TypedQuery<Categoria> queryDeCategoria;

        @Mock
        private TypedQuery<Long> queryDeContagemDeNome;

        @Mock
        private TypedQuery<Long> queryDeContagemDeEmprestimo;

        @Mock
        private TypedQuery<Long> queryDeContagemDeReserva;

        @Mock
        private TypedQuery<Long> queryDeMaximoId;

        private CategoriaService categoriaService;

        @BeforeEach
        void setUp() {
                when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);
                lenient().when(entityManager.getTransaction()).thenReturn(entityTransaction);
                categoriaService = new CategoriaService(entityManagerFactory);
        }

        // =====================================================================
        // inserirCategoria
        // =====================================================================

        @Test
        @DisplayName("Não deve persistir categoria quando já existe outra com o mesmo nome")
        void naoDevePersistirCategoriaQuandoJaExisteOutraComOMesmoNome() {
                Categoria novaCategoria = Categoria.builder().nome("Ficção").build();

                when(entityManager.createQuery("SELECT MAX(c.id) FROM Categoria c", Long.class))
                                .thenReturn(queryDeMaximoId);
                when(queryDeMaximoId.getSingleResult()).thenReturn(5L);

                when(entityManager.createQuery(
                                "SELECT COUNT(c) FROM Categoria c WHERE LOWER(c.nome) = LOWER(:nome)",
                                Long.class))
                                .thenReturn(queryDeContagemDeNome);
                when(queryDeContagemDeNome.setParameter("nome", "Ficção")).thenReturn(queryDeContagemDeNome);
                when(queryDeContagemDeNome.getSingleResult()).thenReturn(1L);

                categoriaService.inserirCategoria(novaCategoria);

                verify(entityManager, never()).persist(any(Categoria.class));
        }

        @Test
        @DisplayName("Deve persistir categoria e atribuir ID gerado quando nome não está duplicado")
        void devePersistirCategoriaEAtribuirIdGeradoQuandoNomeNaoEstaDuplicado() {
                Categoria novaCategoria = Categoria.builder().nome("Tecnologia").build();

                when(entityManager.createQuery("SELECT MAX(c.id) FROM Categoria c", Long.class))
                                .thenReturn(queryDeMaximoId);
                when(queryDeMaximoId.getSingleResult()).thenReturn(3L);

                when(entityManager.createQuery(
                                "SELECT COUNT(c) FROM Categoria c WHERE LOWER(c.nome) = LOWER(:nome)",
                                Long.class))
                                .thenReturn(queryDeContagemDeNome);
                when(queryDeContagemDeNome.setParameter("nome", "Tecnologia")).thenReturn(queryDeContagemDeNome);
                when(queryDeContagemDeNome.getSingleResult()).thenReturn(0L);

                categoriaService.inserirCategoria(novaCategoria);

                verify(entityManager).persist(novaCategoria);
                assertEquals(4L, novaCategoria.getId());
        }

        // =====================================================================
        // listarTodasCategorias
        // =====================================================================

        @Test
        @DisplayName("Deve retornar todas as categorias quando existem registros no banco")
        void deveRetornarTodasAsCategoriasQuandoExistemRegistrosNoBanco() {
                Categoria primeiraCategoria = Categoria.builder().id(1L).nome("Ficção").build();
                Categoria segundaCategoria = Categoria.builder().id(2L).nome("Tecnologia").build();
                Categoria terceiraCategoria = Categoria.builder().id(3L).nome("História").build();

                when(entityManager.createQuery("SELECT c FROM Categoria c ORDER BY c.nome", Categoria.class))
                                .thenReturn(queryDeCategoria);
                when(queryDeCategoria.getResultList())
                                .thenReturn(List.of(primeiraCategoria, segundaCategoria, terceiraCategoria));

                List<Categoria> categoriasRetornadas = categoriaService.listarTodasCategorias();

                assertNotNull(categoriasRetornadas);
                assertEquals(3, categoriasRetornadas.size());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há categorias cadastradas no banco")
        void deveRetornarListaVaziaQuandoNaoHaCategoriasCadastradasNoBanco() {
                when(entityManager.createQuery("SELECT c FROM Categoria c ORDER BY c.nome", Categoria.class))
                                .thenReturn(queryDeCategoria);
                when(queryDeCategoria.getResultList()).thenReturn(Collections.emptyList());

                List<Categoria> categoriasRetornadas = categoriaService.listarTodasCategorias();

                assertTrue(categoriasRetornadas.isEmpty());
        }

        // =====================================================================
        // buscarCategoriaPorId
        // =====================================================================

        @Test
        @DisplayName("Deve retornar categoria quando ID corresponde a um registro existente")
        void deveRetornarCategoriaQuandoIdCorrespondeAUmRegistroExistente() {
                Categoria categoriaExistente = Categoria.builder().id(1L).nome("Ficção").build();

                when(entityManager.find(Categoria.class, 1)).thenReturn(categoriaExistente);

                Categoria categoriaRetornada = categoriaService.buscarCategoriaPorId(1);

                assertNotNull(categoriaRetornada);
                assertEquals(1L, categoriaRetornada.getId());
                assertEquals("Ficção", categoriaRetornada.getNome());
        }

        @Test
        @DisplayName("Deve retornar null quando ID não corresponde a nenhum registro")
        void deveRetornarNullQuandoIdNaoCorrespondeANenhumRegistro() {
                when(entityManager.find(Categoria.class, 99)).thenReturn(null);

                Categoria categoriaRetornada = categoriaService.buscarCategoriaPorId(99);

                assertNull(categoriaRetornada);
        }

        // =====================================================================
        // atualizarCategoria
        // =====================================================================

        @Test
        @DisplayName("Deve executar merge quando categoria válida é passada para atualização")
        void deveExecutarMergeQuandoCategoriaValidaEPassadaParaAtualizacao() {
                Categoria categoriaAtualizada = Categoria.builder().id(1L).nome("Ficção Científica").build();

                when(entityManager.merge(categoriaAtualizada)).thenReturn(categoriaAtualizada);

                categoriaService.atualizarCategoria(categoriaAtualizada);

                verify(entityManager).merge(categoriaAtualizada);
        }

        // =====================================================================
        // deletarCategoria
        // =====================================================================

        @Test
        @DisplayName("Não deve remover categoria quando ID não corresponde a nenhum registro")
        void naoDeveRemoverCategoriaQuandoIdNaoCorrespondeANenhumRegistro() {
                when(entityManager.find(Categoria.class, 99)).thenReturn(null);

                categoriaService.deletarCategoria(99);

                verify(entityManager, never()).remove(any());
        }

        @Test
        @DisplayName("Não deve remover categoria quando existe livro da categoria com empréstimo ativo")
        void naoDeveRemoverCategoriaQuandoExisteLivroComEmprestimoAtivo() {
                Categoria categoriaComEmprestimo = Categoria.builder().id(1L).nome("Ficção").build();

                when(entityManager.find(Categoria.class, 1)).thenReturn(categoriaComEmprestimo);

                when(entityManager.createQuery(
                                "SELECT COUNT(e) FROM Emprestimo e " +
                                                "WHERE (e.status = 'ATIVO' OR e.status = 'RENOVADO') " +
                                                "AND e.exemplar.livro.isbn IN (" +
                                                "    SELECT lc.livro.isbn FROM LivroCategoria lc WHERE lc.categoria.id = :id"
                                                +
                                                ")",
                                Long.class))
                                .thenReturn(queryDeContagemDeEmprestimo);
                when(queryDeContagemDeEmprestimo.setParameter("id", 1)).thenReturn(queryDeContagemDeEmprestimo);
                when(queryDeContagemDeEmprestimo.getSingleResult()).thenReturn(2L);

                lenient().when(entityManager.createQuery(
                                "SELECT COUNT(r) FROM Reserva r " +
                                                "WHERE r.isbnLivro IN (" +
                                                "    SELECT lc.livro.isbn FROM LivroCategoria lc WHERE lc.categoria.id = :id"
                                                +
                                                ") AND r.status = 'RESERVADO'",
                                Long.class))
                                .thenReturn(queryDeContagemDeReserva);

                categoriaService.deletarCategoria(1);

                verify(entityManager, never()).remove(any());
        }

        @Test
        @DisplayName("Não deve remover categoria quando existe livro da categoria com reserva pendente")
        void naoDeveRemoverCategoriaQuandoExisteLivroComReservaPendente() {
                Categoria categoriaComReserva = Categoria.builder().id(1L).nome("Tecnologia").build();

                when(entityManager.find(Categoria.class, 1)).thenReturn(categoriaComReserva);

                when(entityManager.createQuery(
                                "SELECT COUNT(e) FROM Emprestimo e " +
                                                "WHERE (e.status = 'ATIVO' OR e.status = 'RENOVADO') " +
                                                "AND e.exemplar.livro.isbn IN (" +
                                                "    SELECT lc.livro.isbn FROM LivroCategoria lc WHERE lc.categoria.id = :id"
                                                +
                                                ")",
                                Long.class))
                                .thenReturn(queryDeContagemDeEmprestimo);
                when(queryDeContagemDeEmprestimo.setParameter("id", 1)).thenReturn(queryDeContagemDeEmprestimo);
                when(queryDeContagemDeEmprestimo.getSingleResult()).thenReturn(0L);

                when(entityManager.createQuery(
                                "SELECT COUNT(r) FROM Reserva r " +
                                                "WHERE r.isbnLivro IN (" +
                                                "    SELECT lc.livro.isbn FROM LivroCategoria lc WHERE lc.categoria.id = :id"
                                                +
                                                ") AND r.status = 'RESERVADO'",
                                Long.class))
                                .thenReturn(queryDeContagemDeReserva);
                when(queryDeContagemDeReserva.setParameter("id", 1)).thenReturn(queryDeContagemDeReserva);
                when(queryDeContagemDeReserva.getSingleResult()).thenReturn(1L);

                categoriaService.deletarCategoria(1);

                verify(entityManager, never()).remove(any());
        }

        @Test
        @DisplayName("Deve remover categoria quando não há empréstimos ativos nem reservas pendentes vinculados")
        void deveRemoverCategoriaQuandoNaoHaEmprestimosAtivosNemReservasPendentesVinculados() {
                Categoria categoriaLivre = Categoria.builder().id(1L).nome("História").build();

                when(entityManager.find(Categoria.class, 1)).thenReturn(categoriaLivre);

                when(entityManager.createQuery(
                                "SELECT COUNT(e) FROM Emprestimo e " +
                                                "WHERE (e.status = 'ATIVO' OR e.status = 'RENOVADO') " +
                                                "AND e.exemplar.livro.isbn IN (" +
                                                "    SELECT lc.livro.isbn FROM LivroCategoria lc WHERE lc.categoria.id = :id"
                                                +
                                                ")",
                                Long.class))
                                .thenReturn(queryDeContagemDeEmprestimo);
                when(queryDeContagemDeEmprestimo.setParameter("id", 1)).thenReturn(queryDeContagemDeEmprestimo);
                when(queryDeContagemDeEmprestimo.getSingleResult()).thenReturn(0L);

                when(entityManager.createQuery(
                                "SELECT COUNT(r) FROM Reserva r " +
                                                "WHERE r.isbnLivro IN (" +
                                                "    SELECT lc.livro.isbn FROM LivroCategoria lc WHERE lc.categoria.id = :id"
                                                +
                                                ") AND r.status = 'RESERVADO'",
                                Long.class))
                                .thenReturn(queryDeContagemDeReserva);
                when(queryDeContagemDeReserva.setParameter("id", 1)).thenReturn(queryDeContagemDeReserva);
                when(queryDeContagemDeReserva.getSingleResult()).thenReturn(0L);

                categoriaService.deletarCategoria(1);

                verify(entityManager).remove(categoriaLivre);
        }
}