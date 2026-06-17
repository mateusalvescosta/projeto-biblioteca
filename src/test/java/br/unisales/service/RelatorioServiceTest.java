package br.unisales.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.unisales.Enumeration.StatusEmprestimoEnum;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

@ExtendWith(MockitoExtension.class)
public class RelatorioServiceTest {

        @Mock
        private EntityManagerFactory entityManagerFactory;

        @Mock
        private EntityManager entityManager;

        @Mock
        private EntityTransaction entityTransaction;

        @Mock
        private TypedQuery<Object[]> queryDeResultadoTabular;

        @Mock
        private TypedQuery<Long> queryDeContagemDeEmprestimoNoMes;

        @Mock
        private TypedQuery<Long> queryDeContagemDeDevolucaoNoMes;

        @Mock
        private TypedQuery<Long> queryDeContagemDeEmprestimosAtivos;

        @Mock
        private TypedQuery<Long> queryDeContagemDeEmprestimosEmAtraso;

        private RelatorioService relatorioService;

        @BeforeEach
        void setUp() {
                when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);
                lenient().when(entityManager.getTransaction()).thenReturn(entityTransaction);

                relatorioService = new RelatorioService(entityManagerFactory);
        }

        // =====================================================================
        // topLivrosMaisEmprestados
        // =====================================================================

        @Test
        @DisplayName("Deve executar a query e encerrar normalmente quando não há empréstimos registrados")
        void deveExecutarAQueryEEncerrarNormalmenteQuandoNaoHaEmprestimosRegistrados() {
                when(entityManager.createQuery(
                                "SELECT l.titulo, COUNT(e) " +
                                                "FROM Emprestimo e " +
                                                "JOIN e.exemplar ex " +
                                                "JOIN ex.livro l " +
                                                "GROUP BY l.titulo " +
                                                "ORDER BY COUNT(e) DESC",
                                Object[].class))
                                .thenReturn(queryDeResultadoTabular);
                when(queryDeResultadoTabular.setMaxResults(10)).thenReturn(queryDeResultadoTabular);
                when(queryDeResultadoTabular.getResultList()).thenReturn(Collections.emptyList());

                relatorioService.topLivrosMaisEmprestados();

                verify(entityManager).createQuery(
                                "SELECT l.titulo, COUNT(e) " +
                                                "FROM Emprestimo e " +
                                                "JOIN e.exemplar ex " +
                                                "JOIN ex.livro l " +
                                                "GROUP BY l.titulo " +
                                                "ORDER BY COUNT(e) DESC",
                                Object[].class);
        }

        @Test
        @DisplayName("Deve executar a query e iterar resultados quando existem empréstimos registrados")
        void deveExecutarAQueryEIterarResultadosQuandoExistemEmprestimosRegistrados() {
                Object[] primeiraLinha = new Object[] { "Clean Code", 15L };
                Object[] segundaLinha = new Object[] { "Design Patterns", 10L };

                when(entityManager.createQuery(
                                "SELECT l.titulo, COUNT(e) " +
                                                "FROM Emprestimo e " +
                                                "JOIN e.exemplar ex " +
                                                "JOIN ex.livro l " +
                                                "GROUP BY l.titulo " +
                                                "ORDER BY COUNT(e) DESC",
                                Object[].class))
                                .thenReturn(queryDeResultadoTabular);
                when(queryDeResultadoTabular.setMaxResults(10)).thenReturn(queryDeResultadoTabular);
                when(queryDeResultadoTabular.getResultList())
                                .thenReturn(Arrays.<Object[]>asList(primeiraLinha, segundaLinha));

                relatorioService.topLivrosMaisEmprestados();

                verify(queryDeResultadoTabular).getResultList();
        }

        // =====================================================================
        // emprestimosEmAtraso
        // =====================================================================

        @Test
        @DisplayName("Deve executar a query e encerrar normalmente quando não há empréstimos em atraso")
        void deveExecutarAQueryEEncerrarNormalmenteQuandoNaoHaEmprestimosEmAtraso() {
                when(entityManager.createQuery(
                                "SELECT u.nome, l.titulo, e.dataDevolucaoPrevista " +
                                                "FROM Emprestimo e " +
                                                "JOIN e.usuario u " +
                                                "JOIN e.exemplar ex " +
                                                "JOIN ex.livro l " +
                                                "WHERE e.dataDevolucao IS NULL AND e.dataDevolucaoPrevista < :hoje " +
                                                "ORDER BY u.nome",
                                Object[].class))
                                .thenReturn(queryDeResultadoTabular);
                when(queryDeResultadoTabular.setParameter(eq("hoje"), any(LocalDateTime.class)))
                                .thenReturn(queryDeResultadoTabular);
                when(queryDeResultadoTabular.getResultList()).thenReturn(Collections.emptyList());

                relatorioService.emprestimosEmAtraso();

                verify(queryDeResultadoTabular).getResultList();
        }

        @Test
        @DisplayName("Deve executar a query e iterar resultados quando existem empréstimos em atraso")
        void deveExecutarAQueryEIterarResultadosQuandoExistemEmprestimosEmAtraso() {
                LocalDateTime prazVencido = LocalDateTime.now().minusDays(3);
                Object[] linhaDeAtraso = new Object[] { "João Silva", "Clean Code", prazVencido };

                when(entityManager.createQuery(
                                "SELECT u.nome, l.titulo, e.dataDevolucaoPrevista " +
                                                "FROM Emprestimo e " +
                                                "JOIN e.usuario u " +
                                                "JOIN e.exemplar ex " +
                                                "JOIN ex.livro l " +
                                                "WHERE e.dataDevolucao IS NULL AND e.dataDevolucaoPrevista < :hoje " +
                                                "ORDER BY u.nome",
                                Object[].class))
                                .thenReturn(queryDeResultadoTabular);
                when(queryDeResultadoTabular.setParameter(eq("hoje"), any(LocalDateTime.class)))
                                .thenReturn(queryDeResultadoTabular);
                when(queryDeResultadoTabular.getResultList()).thenReturn(Arrays.<Object[]>asList(linhaDeAtraso));

                relatorioService.emprestimosEmAtraso();

                verify(queryDeResultadoTabular).getResultList();
        }

        // =====================================================================
        // usuariosComMaisAtrasos
        // =====================================================================

        @Test
        @DisplayName("Deve executar a query e encerrar normalmente quando não há usuários com atrasos")
        void deveExecutarAQueryEEncerrarNormalmenteQuandoNaoHaUsuariosComAtrasos() {
                when(entityManager.createQuery(
                                "SELECT u.nome, COUNT(e) " +
                                                "FROM Emprestimo e " +
                                                "JOIN e.usuario u " +
                                                "WHERE e.dataDevolucao IS NULL AND e.dataDevolucaoPrevista < :hoje " +
                                                "GROUP BY u.nome " +
                                                "ORDER BY COUNT(e) DESC",
                                Object[].class))
                                .thenReturn(queryDeResultadoTabular);
                when(queryDeResultadoTabular.setParameter(eq("hoje"), any(LocalDateTime.class)))
                                .thenReturn(queryDeResultadoTabular);
                when(queryDeResultadoTabular.getResultList()).thenReturn(Collections.emptyList());

                relatorioService.usuariosComMaisAtrasos();

                verify(queryDeResultadoTabular).getResultList();
        }

        @Test
        @DisplayName("Deve executar a query e iterar resultados quando existem usuários com atrasos")
        void deveExecutarAQueryEIterarResultadosQuandoExistemUsuariosComAtrasos() {
                Object[] primeiraLinha = new Object[] { "Maria Oliveira", 3L };
                Object[] segundaLinha = new Object[] { "Carlos Santos", 1L };

                when(entityManager.createQuery(
                                "SELECT u.nome, COUNT(e) " +
                                                "FROM Emprestimo e " +
                                                "JOIN e.usuario u " +
                                                "WHERE e.dataDevolucao IS NULL AND e.dataDevolucaoPrevista < :hoje " +
                                                "GROUP BY u.nome " +
                                                "ORDER BY COUNT(e) DESC",
                                Object[].class))
                                .thenReturn(queryDeResultadoTabular);
                when(queryDeResultadoTabular.setParameter(eq("hoje"), any(LocalDateTime.class)))
                                .thenReturn(queryDeResultadoTabular);
                when(queryDeResultadoTabular.getResultList())
                                .thenReturn(Arrays.<Object[]>asList(primeiraLinha, segundaLinha));

                relatorioService.usuariosComMaisAtrasos();

                verify(queryDeResultadoTabular).getResultList();
        }

        // =====================================================================
        // estatisticasMensais
        // =====================================================================

        @Test
        @DisplayName("Deve executar as quatro queries de contagem e encerrar normalmente quando todos os valores são zero")
        void deveExecutarAsQuatroQueriesDeContagemEEncerrarNormalmenteQuandoTodosOsValoresSaoZero() {
                when(entityManager.createQuery(
                                "SELECT COUNT(e) FROM Emprestimo e " +
                                                "WHERE e.dataEmprestimo BETWEEN :inicio AND :fim",
                                Long.class))
                                .thenReturn(queryDeContagemDeEmprestimoNoMes);
                when(queryDeContagemDeEmprestimoNoMes.setParameter(eq("inicio"), any(LocalDateTime.class)))
                                .thenReturn(queryDeContagemDeEmprestimoNoMes);
                when(queryDeContagemDeEmprestimoNoMes.setParameter(eq("fim"), any(LocalDateTime.class)))
                                .thenReturn(queryDeContagemDeEmprestimoNoMes);
                when(queryDeContagemDeEmprestimoNoMes.getSingleResult()).thenReturn(0L);

                when(entityManager.createQuery(
                                "SELECT COUNT(e) FROM Emprestimo e " +
                                                "WHERE e.dataDevolucao IS NOT NULL " +
                                                "AND e.dataDevolucao BETWEEN :inicio AND :fim",
                                Long.class))
                                .thenReturn(queryDeContagemDeDevolucaoNoMes);
                when(queryDeContagemDeDevolucaoNoMes.setParameter(eq("inicio"), any(LocalDateTime.class)))
                                .thenReturn(queryDeContagemDeDevolucaoNoMes);
                when(queryDeContagemDeDevolucaoNoMes.setParameter(eq("fim"), any(LocalDateTime.class)))
                                .thenReturn(queryDeContagemDeDevolucaoNoMes);
                when(queryDeContagemDeDevolucaoNoMes.getSingleResult()).thenReturn(0L);

                when(entityManager.createQuery(
                                "SELECT COUNT(e) FROM Emprestimo e " +
                                                "WHERE e.status = :ativo OR e.status = :renovado",
                                Long.class))
                                .thenReturn(queryDeContagemDeEmprestimosAtivos);
                when(queryDeContagemDeEmprestimosAtivos.setParameter("ativo", StatusEmprestimoEnum.ATIVO))
                                .thenReturn(queryDeContagemDeEmprestimosAtivos);
                when(queryDeContagemDeEmprestimosAtivos.setParameter("renovado", StatusEmprestimoEnum.RENOVADO))
                                .thenReturn(queryDeContagemDeEmprestimosAtivos);
                when(queryDeContagemDeEmprestimosAtivos.getSingleResult()).thenReturn(0L);

                when(entityManager.createQuery(
                                "SELECT COUNT(e) FROM Emprestimo e " +
                                                "WHERE e.dataDevolucao IS NULL " +
                                                "AND e.dataDevolucaoPrevista < :hoje",
                                Long.class))
                                .thenReturn(queryDeContagemDeEmprestimosEmAtraso);
                when(queryDeContagemDeEmprestimosEmAtraso.setParameter(eq("hoje"), any(LocalDateTime.class)))
                                .thenReturn(queryDeContagemDeEmprestimosEmAtraso);
                when(queryDeContagemDeEmprestimosEmAtraso.getSingleResult()).thenReturn(0L);

                relatorioService.estatisticasMensais();

                verify(queryDeContagemDeEmprestimoNoMes).getSingleResult();
                verify(queryDeContagemDeDevolucaoNoMes).getSingleResult();
                verify(queryDeContagemDeEmprestimosAtivos).getSingleResult();
                verify(queryDeContagemDeEmprestimosEmAtraso).getSingleResult();
        }

        @Test
        @DisplayName("Deve executar as quatro queries e processar corretamente quando há movimentação no mês")
        void deveExecutarAsQuatroQueriesEProcessarCorretamenteQuandoHaMovimentacaoNoMes() {
                when(entityManager.createQuery(
                                "SELECT COUNT(e) FROM Emprestimo e " +
                                                "WHERE e.dataEmprestimo BETWEEN :inicio AND :fim",
                                Long.class))
                                .thenReturn(queryDeContagemDeEmprestimoNoMes);
                when(queryDeContagemDeEmprestimoNoMes.setParameter(eq("inicio"), any(LocalDateTime.class)))
                                .thenReturn(queryDeContagemDeEmprestimoNoMes);
                when(queryDeContagemDeEmprestimoNoMes.setParameter(eq("fim"), any(LocalDateTime.class)))
                                .thenReturn(queryDeContagemDeEmprestimoNoMes);
                when(queryDeContagemDeEmprestimoNoMes.getSingleResult()).thenReturn(12L);

                when(entityManager.createQuery(
                                "SELECT COUNT(e) FROM Emprestimo e " +
                                                "WHERE e.dataDevolucao IS NOT NULL " +
                                                "AND e.dataDevolucao BETWEEN :inicio AND :fim",
                                Long.class))
                                .thenReturn(queryDeContagemDeDevolucaoNoMes);
                when(queryDeContagemDeDevolucaoNoMes.setParameter(eq("inicio"), any(LocalDateTime.class)))
                                .thenReturn(queryDeContagemDeDevolucaoNoMes);
                when(queryDeContagemDeDevolucaoNoMes.setParameter(eq("fim"), any(LocalDateTime.class)))
                                .thenReturn(queryDeContagemDeDevolucaoNoMes);
                when(queryDeContagemDeDevolucaoNoMes.getSingleResult()).thenReturn(8L);

                when(entityManager.createQuery(
                                "SELECT COUNT(e) FROM Emprestimo e " +
                                                "WHERE e.status = :ativo OR e.status = :renovado",
                                Long.class))
                                .thenReturn(queryDeContagemDeEmprestimosAtivos);
                when(queryDeContagemDeEmprestimosAtivos.setParameter("ativo", StatusEmprestimoEnum.ATIVO))
                                .thenReturn(queryDeContagemDeEmprestimosAtivos);
                when(queryDeContagemDeEmprestimosAtivos.setParameter("renovado", StatusEmprestimoEnum.RENOVADO))
                                .thenReturn(queryDeContagemDeEmprestimosAtivos);
                when(queryDeContagemDeEmprestimosAtivos.getSingleResult()).thenReturn(4L);

                when(entityManager.createQuery(
                                "SELECT COUNT(e) FROM Emprestimo e " +
                                                "WHERE e.dataDevolucao IS NULL " +
                                                "AND e.dataDevolucaoPrevista < :hoje",
                                Long.class))
                                .thenReturn(queryDeContagemDeEmprestimosEmAtraso);
                when(queryDeContagemDeEmprestimosEmAtraso.setParameter(eq("hoje"), any(LocalDateTime.class)))
                                .thenReturn(queryDeContagemDeEmprestimosEmAtraso);
                when(queryDeContagemDeEmprestimosEmAtraso.getSingleResult()).thenReturn(2L);

                relatorioService.estatisticasMensais();

                verify(queryDeContagemDeEmprestimoNoMes).getSingleResult();
                verify(queryDeContagemDeDevolucaoNoMes).getSingleResult();
                verify(queryDeContagemDeEmprestimosAtivos).getSingleResult();
                verify(queryDeContagemDeEmprestimosEmAtraso).getSingleResult();
        }
}