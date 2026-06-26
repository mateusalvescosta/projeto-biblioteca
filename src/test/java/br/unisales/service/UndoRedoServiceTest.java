package br.unisales.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.unisales.Enumeration.StatusEmprestimoEnum;
import br.unisales.Enumeration.StatusExemplarEnum;
import br.unisales.database.table.Emprestimo;
import br.unisales.database.table.Exemplar;
import br.unisales.database.table.Multa;
import br.unisales.database.table.Notificacao;
import br.unisales.database.table.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

@ExtendWith(MockitoExtension.class)
public class UndoRedoServiceTest {

        @Mock
        private EntityManagerFactory entityManagerFactory;

        @Mock
        private EntityManager entityManager;

        @Mock
        private EntityTransaction entityTransaction;

        @Mock
        private TypedQuery<Emprestimo> queryDeEmprestimo;

        @Mock
        private TypedQuery<Usuario> queryDeUsuario;

        @Mock
        private TypedQuery<Multa> queryDeMulta;

        @Mock
        private TypedQuery<Notificacao> queryDeNotificacao;

        @Mock
        private TypedQuery<Long> queryDeContagemDeEmprestimo;

        @Mock
        private TypedQuery<Long> queryDeContagemDeReserva;

        @Mock
        private TypedQuery<Long> queryDeContagemDeNotificacao;

        @Mock
        private TypedQuery<Long> queryDeContagemDeMulta;

        private UndoRedoService undoRedoService;

        @BeforeEach
        void setUp() {
                when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);
                lenient().when(entityManager.getTransaction()).thenReturn(entityTransaction);
                undoRedoService = new UndoRedoService(entityManagerFactory);
        }

        // =====================================================================
        // desfazerEmprestimo
        // =====================================================================

        @Test
        @DisplayName("Não deve remover empréstimo quando não há nenhum registrado no banco")
        void naoDeveRemoverEmprestimoQuandoNaoHaNenhumRegistradoNoBanco() {
                when(entityManager.createQuery(
                                "SELECT e FROM Emprestimo e ORDER BY e.id DESC",
                                Emprestimo.class))
                                .thenReturn(queryDeEmprestimo);
                when(queryDeEmprestimo.setMaxResults(1)).thenReturn(queryDeEmprestimo);
                when(queryDeEmprestimo.getResultList()).thenReturn(Collections.emptyList());

                undoRedoService.desfazerEmprestimo();

                verify(entityManager, never()).remove(any());
        }

        @Test
        @DisplayName("Não deve remover empréstimo quando o último já está com status devolvido")
        void naoDeveRemoverEmprestimoQuandoOUltimoJaEstaComStatusDevolvido() {
                Emprestimo emprestimoDevolvido = Emprestimo.builder()
                                .id(1L)
                                .status(StatusEmprestimoEnum.DEVOLVIDO)
                                .build();

                when(entityManager.createQuery(
                                "SELECT e FROM Emprestimo e ORDER BY e.id DESC",
                                Emprestimo.class))
                                .thenReturn(queryDeEmprestimo);
                when(queryDeEmprestimo.setMaxResults(1)).thenReturn(queryDeEmprestimo);
                when(queryDeEmprestimo.getResultList()).thenReturn(List.of(emprestimoDevolvido));

                undoRedoService.desfazerEmprestimo();

                verify(entityManager, never()).remove(any());
        }

        @Test
        @DisplayName("Deve remover empréstimo ativo e restaurar status do exemplar para disponível")
        void deveRemoverEmprestimoAtivoERestaurarStatusDoExemplarParaDisponivel() {
                Exemplar exemplarEmprestado = Exemplar.builder()
                                .id(5L)
                                .status(StatusExemplarEnum.EMPRESTADO)
                                .build();
                Emprestimo ultimoEmprestimoAtivo = Emprestimo.builder()
                                .id(1L)
                                .status(StatusEmprestimoEnum.ATIVO)
                                .exemplar(exemplarEmprestado)
                                .build();

                when(entityManager.createQuery(
                                "SELECT e FROM Emprestimo e ORDER BY e.id DESC",
                                Emprestimo.class))
                                .thenReturn(queryDeEmprestimo);
                when(queryDeEmprestimo.setMaxResults(1)).thenReturn(queryDeEmprestimo);
                when(queryDeEmprestimo.getResultList()).thenReturn(List.of(ultimoEmprestimoAtivo));

                when(entityManager.merge(ultimoEmprestimoAtivo)).thenReturn(ultimoEmprestimoAtivo);

                undoRedoService.desfazerEmprestimo();

                verify(entityManager).remove(ultimoEmprestimoAtivo);
                assertEquals(StatusExemplarEnum.DISPONIVEL, exemplarEmprestado.getStatus());
        }

        @Test
        @DisplayName("Deve remover empréstimo renovado e restaurar status do exemplar para disponível")
        void deveRemoverEmprestimoRenovadoERestaurarStatusDoExemplarParaDisponivel() {
                Exemplar exemplarEmprestado = Exemplar.builder()
                                .id(6L)
                                .status(StatusExemplarEnum.EMPRESTADO)
                                .build();
                Emprestimo ultimoEmprestimoRenovado = Emprestimo.builder()
                                .id(2L)
                                .status(StatusEmprestimoEnum.RENOVADO)
                                .exemplar(exemplarEmprestado)
                                .build();

                when(entityManager.createQuery(
                                "SELECT e FROM Emprestimo e ORDER BY e.id DESC",
                                Emprestimo.class))
                                .thenReturn(queryDeEmprestimo);
                when(queryDeEmprestimo.setMaxResults(1)).thenReturn(queryDeEmprestimo);
                when(queryDeEmprestimo.getResultList()).thenReturn(List.of(ultimoEmprestimoRenovado));

                when(entityManager.merge(ultimoEmprestimoRenovado)).thenReturn(ultimoEmprestimoRenovado);

                undoRedoService.desfazerEmprestimo();

                verify(entityManager).remove(ultimoEmprestimoRenovado);
                assertEquals(StatusExemplarEnum.DISPONIVEL, exemplarEmprestado.getStatus());
        }

        // =====================================================================
        // desfazerDevolucao
        // =====================================================================

        @Test
        @DisplayName("Não deve reverter devolução quando não há nenhuma registrada no banco")
        void naoDeveReverterDevolucaoQuandoNaoHaNenhumaRegistradaNoBanco() {
                when(entityManager.createQuery(
                                "SELECT e FROM Emprestimo e WHERE e.dataDevolucao IS NOT NULL ORDER BY e.id DESC",
                                Emprestimo.class))
                                .thenReturn(queryDeEmprestimo);
                when(queryDeEmprestimo.setMaxResults(1)).thenReturn(queryDeEmprestimo);
                when(queryDeEmprestimo.getResultList()).thenReturn(Collections.emptyList());

                undoRedoService.desfazerDevolucao();

                verify(entityManager, never()).merge(any());
                verify(entityManager, never()).remove(any());
        }

        @Test
        @DisplayName("Não deve reverter devolução quando exemplar já foi emprestado novamente")
        void naoDeveReverterDevolucaoQuandoExemplarJaFoiEmprestadoNovamente() {
                Exemplar exemplarReferenciado = Exemplar.builder().id(5L).build();
                Emprestimo ultimaDevolucao = Emprestimo.builder()
                                .id(1L)
                                .exemplar(exemplarReferenciado)
                                .build();

                when(entityManager.createQuery(
                                "SELECT e FROM Emprestimo e WHERE e.dataDevolucao IS NOT NULL ORDER BY e.id DESC",
                                Emprestimo.class))
                                .thenReturn(queryDeEmprestimo);
                when(queryDeEmprestimo.setMaxResults(1)).thenReturn(queryDeEmprestimo);
                when(queryDeEmprestimo.getResultList()).thenReturn(List.of(ultimaDevolucao));

                when(entityManager.find(Exemplar.class, 5L)).thenReturn(
                                Exemplar.builder().id(5L).status(StatusExemplarEnum.EMPRESTADO).build());

                undoRedoService.desfazerDevolucao();

                verify(entityManager, never()).merge(any());
                verify(entityManager, never()).remove(any());
        }

        @Test
        @DisplayName("Deve reverter devolução e restaurar empréstimo para ativo quando não há multa associada")
        void deveReverterDevolucaoERestaurarEmprestimoParaAtivoQuandoNaoHaMultaAssociada() {
                Exemplar exemplarDisponivel = Exemplar.builder()
                                .id(5L)
                                .status(StatusExemplarEnum.DISPONIVEL)
                                .build();
                Emprestimo ultimaDevolucao = Emprestimo.builder()
                                .id(1L)
                                .status(StatusEmprestimoEnum.DEVOLVIDO)
                                .dataDevolucao(LocalDateTime.of(2025, 6, 5, 0, 0))
                                .exemplar(exemplarDisponivel)
                                .build();

                when(entityManager.createQuery(
                                "SELECT e FROM Emprestimo e WHERE e.dataDevolucao IS NOT NULL ORDER BY e.id DESC",
                                Emprestimo.class))
                                .thenReturn(queryDeEmprestimo);
                when(queryDeEmprestimo.setMaxResults(1)).thenReturn(queryDeEmprestimo);
                when(queryDeEmprestimo.getResultList()).thenReturn(List.of(ultimaDevolucao));

                when(entityManager.find(Exemplar.class, 5L)).thenReturn(exemplarDisponivel);
                when(entityManager.merge(exemplarDisponivel)).thenReturn(exemplarDisponivel);
                when(entityManager.merge(ultimaDevolucao)).thenReturn(ultimaDevolucao);

                when(entityManager.createQuery(
                                "SELECT m FROM Multa m WHERE m.emprestimoId = :id",
                                Multa.class))
                                .thenReturn(queryDeMulta);
                when(queryDeMulta.setParameter("id", 1L)).thenReturn(queryDeMulta);
                when(queryDeMulta.getResultList()).thenReturn(Collections.emptyList());

                undoRedoService.desfazerDevolucao();

                assertEquals(StatusEmprestimoEnum.ATIVO, ultimaDevolucao.getStatus());
                assertNull(ultimaDevolucao.getDataDevolucao());
                assertEquals(StatusExemplarEnum.EMPRESTADO, exemplarDisponivel.getStatus());
                verify(entityManager, never()).remove(any());
        }

        @Test
        @DisplayName("Deve reverter devolução e remover multa associada quando ela existe")
        void deveReverterDevolucaoERemoverMultaAssociadaQuandoElaExiste() {
                Exemplar exemplarDisponivel = Exemplar.builder()
                                .id(6L)
                                .status(StatusExemplarEnum.DISPONIVEL)
                                .build();
                Emprestimo ultimaDevolucao = Emprestimo.builder()
                                .id(2L)
                                .status(StatusEmprestimoEnum.DEVOLVIDO)
                                .dataDevolucao(LocalDateTime.of(2025, 6, 10, 0, 0))
                                .exemplar(exemplarDisponivel)
                                .build();
                Multa multaAssociada = Multa.builder().id(1L).emprestimoId(2L).valor(6.0).build();

                when(entityManager.createQuery(
                                "SELECT e FROM Emprestimo e WHERE e.dataDevolucao IS NOT NULL ORDER BY e.id DESC",
                                Emprestimo.class))
                                .thenReturn(queryDeEmprestimo);
                when(queryDeEmprestimo.setMaxResults(1)).thenReturn(queryDeEmprestimo);
                when(queryDeEmprestimo.getResultList()).thenReturn(List.of(ultimaDevolucao));

                when(entityManager.find(Exemplar.class, 6L)).thenReturn(exemplarDisponivel);
                when(entityManager.merge(exemplarDisponivel)).thenReturn(exemplarDisponivel);
                when(entityManager.merge(ultimaDevolucao)).thenReturn(ultimaDevolucao);

                when(entityManager.createQuery(
                                "SELECT m FROM Multa m WHERE m.emprestimoId = :id",
                                Multa.class))
                                .thenReturn(queryDeMulta);
                when(queryDeMulta.setParameter("id", 2L)).thenReturn(queryDeMulta);
                when(queryDeMulta.getResultList()).thenReturn(List.of(multaAssociada));
                when(entityManager.merge(multaAssociada)).thenReturn(multaAssociada);

                undoRedoService.desfazerDevolucao();

                verify(entityManager).remove(multaAssociada);
        }

        @Test
        @DisplayName("Não deve remover usuário quando não há nenhum cadastrado no banco")
        void naoDeveRemoverUsuarioQuandoNaoHaNenhumCadastradoNoBanco() {
                when(entityManager.createQuery(
                                "SELECT u FROM Usuario u ORDER BY u.id DESC",
                                Usuario.class))
                                .thenReturn(queryDeUsuario);
                when(queryDeUsuario.setMaxResults(1)).thenReturn(queryDeUsuario);
                when(queryDeUsuario.getResultList()).thenReturn(Collections.emptyList());

                undoRedoService.desfazerCadastroUsuario();

                verify(entityManager, never()).remove(any());
        }

        @Test
        @DisplayName("Não deve remover usuário quando ele possui empréstimos ativos")
        void naoDeveRemoverUsuarioQuandoElePossuiEmprestimosAtivos() {
                Usuario ultimoUsuario = Usuario.builder().id(10L).nome("João").build();

                when(entityManager.createQuery(
                                "SELECT u FROM Usuario u ORDER BY u.id DESC",
                                Usuario.class))
                                .thenReturn(queryDeUsuario);
                when(queryDeUsuario.setMaxResults(1)).thenReturn(queryDeUsuario);
                when(queryDeUsuario.getResultList()).thenReturn(List.of(ultimoUsuario));

                when(entityManager.createQuery(
                                "SELECT COUNT(e) FROM Emprestimo e WHERE e.usuario.id = :id " +
                                                "AND (e.status = 'ATIVO' OR e.status = 'RENOVADO')",
                                Long.class))
                                .thenReturn(queryDeContagemDeEmprestimo);
                when(queryDeContagemDeEmprestimo.setParameter("id", 10L)).thenReturn(queryDeContagemDeEmprestimo);
                when(queryDeContagemDeEmprestimo.getSingleResult()).thenReturn(2L);

                undoRedoService.desfazerCadastroUsuario();

                verify(entityManager, never()).remove(any());
        }

        @Test
        @DisplayName("Não deve remover usuário quando ele possui notificações vinculadas")
        void naoDeveRemoverUsuarioQuandoElePossuiNotificacoesVinculadas() {
                Usuario ultimoUsuario = Usuario.builder().id(10L).nome("Ana").build();

                when(entityManager.createQuery(
                                "SELECT u FROM Usuario u ORDER BY u.id DESC",
                                Usuario.class))
                                .thenReturn(queryDeUsuario);
                when(queryDeUsuario.setMaxResults(1)).thenReturn(queryDeUsuario);
                when(queryDeUsuario.getResultList()).thenReturn(List.of(ultimoUsuario));

                when(entityManager.createQuery(
                                "SELECT COUNT(e) FROM Emprestimo e WHERE e.usuario.id = :id " +
                                                "AND (e.status = 'ATIVO' OR e.status = 'RENOVADO')",
                                Long.class))
                                .thenReturn(queryDeContagemDeEmprestimo);
                when(queryDeContagemDeEmprestimo.setParameter("id", 10L)).thenReturn(queryDeContagemDeEmprestimo);
                when(queryDeContagemDeEmprestimo.getSingleResult()).thenReturn(0L);

                when(entityManager.createQuery(
                                "SELECT COUNT(n) FROM Notificacao n WHERE n.usuarioId = :id",
                                Long.class))
                                .thenReturn(queryDeContagemDeNotificacao);
                when(queryDeContagemDeNotificacao.setParameter("id", 10L)).thenReturn(queryDeContagemDeNotificacao);
                when(queryDeContagemDeNotificacao.getSingleResult()).thenReturn(1L);

                undoRedoService.desfazerCadastroUsuario();

                verify(entityManager, never()).remove(any());
        }

        @Test
        @DisplayName("Não deve remover usuário quando ele possui reservas vinculadas")
        void naoDeveRemoverUsuarioQuandoElePossuiReservasVinculadas() {
                Usuario ultimoUsuario = Usuario.builder().id(10L).nome("Maria").build();

                when(entityManager.createQuery(
                                "SELECT u FROM Usuario u ORDER BY u.id DESC",
                                Usuario.class))
                                .thenReturn(queryDeUsuario);
                when(queryDeUsuario.setMaxResults(1)).thenReturn(queryDeUsuario);
                when(queryDeUsuario.getResultList()).thenReturn(List.of(ultimoUsuario));

                when(entityManager.createQuery(
                                "SELECT COUNT(e) FROM Emprestimo e WHERE e.usuario.id = :id " +
                                                "AND (e.status = 'ATIVO' OR e.status = 'RENOVADO')",
                                Long.class))
                                .thenReturn(queryDeContagemDeEmprestimo);
                when(queryDeContagemDeEmprestimo.setParameter("id", 10L)).thenReturn(queryDeContagemDeEmprestimo);
                when(queryDeContagemDeEmprestimo.getSingleResult()).thenReturn(0L);

                when(entityManager.createQuery(
                                "SELECT COUNT(n) FROM Notificacao n WHERE n.usuarioId = :id",
                                Long.class))
                                .thenReturn(queryDeContagemDeNotificacao);
                when(queryDeContagemDeNotificacao.setParameter("id", 10L)).thenReturn(queryDeContagemDeNotificacao);
                when(queryDeContagemDeNotificacao.getSingleResult()).thenReturn(0L);

                when(entityManager.createQuery(
                                "SELECT COUNT(r) FROM Reserva r WHERE r.usuarioId = :id",
                                Long.class))
                                .thenReturn(queryDeContagemDeReserva);
                when(queryDeContagemDeReserva.setParameter("id", 10L)).thenReturn(queryDeContagemDeReserva);
                when(queryDeContagemDeReserva.getSingleResult()).thenReturn(1L);

                undoRedoService.desfazerCadastroUsuario();

                verify(entityManager, never()).remove(any());
        }

        @Test
        @DisplayName("Não deve remover usuário quando ele possui multas vinculadas")
        void naoDeveRemoverUsuarioQuandoElePossuiMultasVinculadas() {
                Usuario ultimoUsuario = Usuario.builder().id(10L).nome("Pedro").build();

                when(entityManager.createQuery(
                                "SELECT u FROM Usuario u ORDER BY u.id DESC",
                                Usuario.class))
                                .thenReturn(queryDeUsuario);
                when(queryDeUsuario.setMaxResults(1)).thenReturn(queryDeUsuario);
                when(queryDeUsuario.getResultList()).thenReturn(List.of(ultimoUsuario));

                when(entityManager.createQuery(
                                "SELECT COUNT(e) FROM Emprestimo e WHERE e.usuario.id = :id " +
                                                "AND (e.status = 'ATIVO' OR e.status = 'RENOVADO')",
                                Long.class))
                                .thenReturn(queryDeContagemDeEmprestimo);
                when(queryDeContagemDeEmprestimo.setParameter("id", 10L)).thenReturn(queryDeContagemDeEmprestimo);
                when(queryDeContagemDeEmprestimo.getSingleResult()).thenReturn(0L);

                when(entityManager.createQuery(
                                "SELECT COUNT(n) FROM Notificacao n WHERE n.usuarioId = :id",
                                Long.class))
                                .thenReturn(queryDeContagemDeNotificacao);
                when(queryDeContagemDeNotificacao.setParameter("id", 10L)).thenReturn(queryDeContagemDeNotificacao);
                when(queryDeContagemDeNotificacao.getSingleResult()).thenReturn(0L);

                when(entityManager.createQuery(
                                "SELECT COUNT(r) FROM Reserva r WHERE r.usuarioId = :id",
                                Long.class))
                                .thenReturn(queryDeContagemDeReserva);
                when(queryDeContagemDeReserva.setParameter("id", 10L)).thenReturn(queryDeContagemDeReserva);
                when(queryDeContagemDeReserva.getSingleResult()).thenReturn(0L);

                when(entityManager.createQuery(
                                "SELECT COUNT(m) FROM Multa m " +
                                                "WHERE m.emprestimoId IN (" +
                                                "    SELECT e.id FROM Emprestimo e WHERE e.usuario.id = :id" +
                                                ")",
                                Long.class))
                                .thenReturn(queryDeContagemDeMulta);
                when(queryDeContagemDeMulta.setParameter("id", 10L)).thenReturn(queryDeContagemDeMulta);
                when(queryDeContagemDeMulta.getSingleResult()).thenReturn(1L);

                undoRedoService.desfazerCadastroUsuario();

                verify(entityManager, never()).remove(any());
        }

        @Test
        @DisplayName("Deve remover usuário quando ele não possui nenhum vínculo")
        void deveRemoverUsuarioQuandoEleNaoPossuiNenhumVinculo() {
                Usuario ultimoUsuario = Usuario.builder().id(10L).nome("Carlos").build();

                when(entityManager.createQuery(
                                "SELECT u FROM Usuario u ORDER BY u.id DESC",
                                Usuario.class))
                                .thenReturn(queryDeUsuario);
                when(queryDeUsuario.setMaxResults(1)).thenReturn(queryDeUsuario);
                when(queryDeUsuario.getResultList()).thenReturn(List.of(ultimoUsuario));

                when(entityManager.createQuery(
                                "SELECT COUNT(e) FROM Emprestimo e WHERE e.usuario.id = :id " +
                                                "AND (e.status = 'ATIVO' OR e.status = 'RENOVADO')",
                                Long.class))
                                .thenReturn(queryDeContagemDeEmprestimo);
                when(queryDeContagemDeEmprestimo.setParameter("id", 10L)).thenReturn(queryDeContagemDeEmprestimo);
                when(queryDeContagemDeEmprestimo.getSingleResult()).thenReturn(0L);

                when(entityManager.createQuery(
                                "SELECT COUNT(n) FROM Notificacao n WHERE n.usuarioId = :id",
                                Long.class))
                                .thenReturn(queryDeContagemDeNotificacao);
                when(queryDeContagemDeNotificacao.setParameter("id", 10L)).thenReturn(queryDeContagemDeNotificacao);
                when(queryDeContagemDeNotificacao.getSingleResult()).thenReturn(0L);

                when(entityManager.createQuery(
                                "SELECT COUNT(r) FROM Reserva r WHERE r.usuarioId = :id",
                                Long.class))
                                .thenReturn(queryDeContagemDeReserva);
                when(queryDeContagemDeReserva.setParameter("id", 10L)).thenReturn(queryDeContagemDeReserva);
                when(queryDeContagemDeReserva.getSingleResult()).thenReturn(0L);

                when(entityManager.createQuery(
                                "SELECT COUNT(m) FROM Multa m " +
                                                "WHERE m.emprestimoId IN (" +
                                                "    SELECT e.id FROM Emprestimo e WHERE e.usuario.id = :id" +
                                                ")",
                                Long.class))
                                .thenReturn(queryDeContagemDeMulta);
                when(queryDeContagemDeMulta.setParameter("id", 10L)).thenReturn(queryDeContagemDeMulta);
                when(queryDeContagemDeMulta.getSingleResult()).thenReturn(0L);

                when(entityManager.merge(ultimoUsuario)).thenReturn(ultimoUsuario);

                undoRedoService.desfazerCadastroUsuario();

                verify(entityManager).remove(ultimoUsuario);
        }

        // =====================================================================
        // desfazerRenovar
        // =====================================================================

        @Test
        @DisplayName("Não deve reverter renovação quando não há empréstimo renovado no banco")
        void naoDeveReverterRenovacaoQuandoNaoHaEmprestimoRenovadoNoBanco() {
                when(entityManager.createQuery(
                                "SELECT e FROM Emprestimo e WHERE e.status = 'RENOVADO' ORDER BY e.id DESC",
                                Emprestimo.class))
                                .thenReturn(queryDeEmprestimo);
                when(queryDeEmprestimo.setMaxResults(1)).thenReturn(queryDeEmprestimo);
                when(queryDeEmprestimo.getResultList()).thenReturn(Collections.emptyList());

                undoRedoService.desfazerRenovar();

                verify(entityManager, never()).merge(any());
        }

        @Test
        @DisplayName("Deve subtrair 7 dias do prazo e restaurar status para ativo ao desfazer renovação")
        void deveSubtrair7DiasDosPrazoERestaurarStatusParaAtivoAoDesfazerRenovacao() {
                LocalDateTime prazoAposRenovacao = LocalDateTime.of(2025, 6, 17, 0, 0);
                Emprestimo ultimoEmprestimoRenovado = Emprestimo.builder()
                                .id(1L)
                                .status(StatusEmprestimoEnum.RENOVADO)
                                .dataDevolucaoPrevista(prazoAposRenovacao)
                                .build();

                when(entityManager.createQuery(
                                "SELECT e FROM Emprestimo e WHERE e.status = 'RENOVADO' ORDER BY e.id DESC",
                                Emprestimo.class))
                                .thenReturn(queryDeEmprestimo);
                when(queryDeEmprestimo.setMaxResults(1)).thenReturn(queryDeEmprestimo);
                when(queryDeEmprestimo.getResultList()).thenReturn(List.of(ultimoEmprestimoRenovado));
                when(entityManager.merge(ultimoEmprestimoRenovado)).thenReturn(ultimoEmprestimoRenovado);

                undoRedoService.desfazerRenovar();

                assertEquals(LocalDateTime.of(2025, 6, 10, 0, 0), ultimoEmprestimoRenovado.getDataDevolucaoPrevista());
                assertEquals(StatusEmprestimoEnum.ATIVO, ultimoEmprestimoRenovado.getStatus());
        }

        // =====================================================================
        // desfazerMulta
        // =====================================================================

        @Test
        @DisplayName("Não deve remover multa quando não há nenhuma registrada no banco")
        void naoDeveRemoverMultaQuandoNaoHaNenhumaRegistradaNoBanco() {
                when(entityManager.createQuery(
                                "SELECT m FROM Multa m ORDER BY m.id DESC",
                                Multa.class))
                                .thenReturn(queryDeMulta);
                when(queryDeMulta.setMaxResults(1)).thenReturn(queryDeMulta);
                when(queryDeMulta.getResultList()).thenReturn(Collections.emptyList());

                undoRedoService.desfazerMulta();

                verify(entityManager, never()).remove(any());
        }

        @Test
        @DisplayName("Não deve remover multa quando ela já foi quitada")
        void naoDeveRemoverMultaQuandoElaJaFoiQuitada() {
                Multa multaQuitada = Multa.builder()
                                .id(1L)
                                .quitada(Boolean.TRUE)
                                .build();

                when(entityManager.createQuery(
                                "SELECT m FROM Multa m ORDER BY m.id DESC",
                                Multa.class))
                                .thenReturn(queryDeMulta);
                when(queryDeMulta.setMaxResults(1)).thenReturn(queryDeMulta);
                when(queryDeMulta.getResultList()).thenReturn(List.of(multaQuitada));

                undoRedoService.desfazerMulta();

                verify(entityManager, never()).remove(any());
        }

        @Test
        @DisplayName("Deve remover multa quando ela ainda não foi quitada")
        void deveRemoverMultaQuandoElaAindaNaoFoiQuitada() {
                Multa multaPendente = Multa.builder()
                                .id(1L)
                                .valor(6.0)
                                .quitada(Boolean.FALSE)
                                .build();

                when(entityManager.createQuery(
                                "SELECT m FROM Multa m ORDER BY m.id DESC",
                                Multa.class))
                                .thenReturn(queryDeMulta);
                when(queryDeMulta.setMaxResults(1)).thenReturn(queryDeMulta);
                when(queryDeMulta.getResultList()).thenReturn(List.of(multaPendente));
                when(entityManager.merge(multaPendente)).thenReturn(multaPendente);

                undoRedoService.desfazerMulta();

                verify(entityManager).remove(multaPendente);
        }

        // =====================================================================
        // desfazerNotificacao
        // =====================================================================

        @Test
        @DisplayName("Não deve remover notificação quando não há nenhuma registrada no banco")
        void naoDeveRemoverNotificacaoQuandoNaoHaNenhumaRegistradaNoBanco() {
                when(entityManager.createQuery(
                                "SELECT n FROM Notificacao n ORDER BY n.id DESC",
                                Notificacao.class))
                                .thenReturn(queryDeNotificacao);
                when(queryDeNotificacao.setMaxResults(1)).thenReturn(queryDeNotificacao);
                when(queryDeNotificacao.getResultList()).thenReturn(Collections.emptyList());

                undoRedoService.desfazerNotificacao();

                verify(entityManager, never()).remove(any());
        }

        @Test
        @DisplayName("Não deve remover notificação quando ela já foi lida")
        void naoDeveRemoverNotificacaoQuandoElaJaFoiLida() {
                Notificacao notificacaoLida = Notificacao.builder()
                                .id(1L)
                                .lida(Boolean.TRUE)
                                .build();

                when(entityManager.createQuery(
                                "SELECT n FROM Notificacao n ORDER BY n.id DESC",
                                Notificacao.class))
                                .thenReturn(queryDeNotificacao);
                when(queryDeNotificacao.setMaxResults(1)).thenReturn(queryDeNotificacao);
                when(queryDeNotificacao.getResultList()).thenReturn(List.of(notificacaoLida));

                undoRedoService.desfazerNotificacao();

                verify(entityManager, never()).remove(any());
        }

        @Test
        @DisplayName("Deve remover notificação quando ela ainda não foi lida")
        void deveRemoverNotificacaoQuandoElaAindaNaoFoiLida() {
                Notificacao notificacaoNaoLida = Notificacao.builder()
                                .id(1L)
                                .lida(Boolean.FALSE)
                                .build();

                when(entityManager.createQuery(
                                "SELECT n FROM Notificacao n ORDER BY n.id DESC",
                                Notificacao.class))
                                .thenReturn(queryDeNotificacao);
                when(queryDeNotificacao.setMaxResults(1)).thenReturn(queryDeNotificacao);
                when(queryDeNotificacao.getResultList()).thenReturn(List.of(notificacaoNaoLida));
                when(entityManager.merge(notificacaoNaoLida)).thenReturn(notificacaoNaoLida);

                undoRedoService.desfazerNotificacao();

                verify(entityManager).remove(notificacaoNaoLida);
        }
}