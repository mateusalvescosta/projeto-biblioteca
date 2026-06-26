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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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

import br.unisales.Enumeration.StatusReservaEnum;
import br.unisales.database.table.Livro;
import br.unisales.database.table.Notificacao;
import br.unisales.database.table.Reserva;
import br.unisales.database.table.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

@ExtendWith(MockitoExtension.class)
public class ReservaServiceTest {

        @Mock
        private EntityManagerFactory entityManagerFactory;

        @Mock
        private EntityManager entityManager;

        @Mock
        private EntityTransaction entityTransaction;

        @Mock
        private TypedQuery<Reserva> queryDeReserva;

        @Mock
        private TypedQuery<Long> queryDeContagemDeEmprestimo;

        @Mock
        private TypedQuery<Long> queryDeContagemDeReserva;
       
        @Mock
        private TypedQuery<Notificacao> queryDeNotificacao;

        @Mock
        private TypedQuery<Long> queryDeMaximoId;

        private ReservaService reservaService;

        @BeforeEach
        void setUp() {
                when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);
                lenient().when(entityManager.getTransaction()).thenReturn(entityTransaction);
                reservaService = new ReservaService(entityManagerFactory);
        }

        // =====================================================================
        // reservarLivro
        // =====================================================================

        @Test
        @DisplayName("Não deve persistir reserva quando usuário não existe no banco")
        void naoDevePersistirReservaQuandoUsuarioNaoExisteNoBanco() {
                Reserva reserva = Reserva.builder()
                                .usuarioId(10L)
                                .isbnLivro("978-1111")
                                .build();

                when(entityManager.find(Usuario.class, 10L)).thenReturn(null);

                reservaService.reservarLivro(reserva);

                verify(entityManager, never()).persist(any(Reserva.class));
        }

        @Test
        @DisplayName("Não deve persistir reserva quando usuário está bloqueado")
        void naoDevePersistirReservaQuandoUsuarioEstaBloqueado() {
                Reserva reserva = Reserva.builder()
                                .usuarioId(10L)
                                .isbnLivro("978-1111")
                                .build();
                Usuario usuarioBloqueado = Usuario.builder()
                                .id(10L)
                                .bloqueado(Boolean.TRUE)
                                .build();

                when(entityManager.find(Usuario.class, 10L)).thenReturn(usuarioBloqueado);

                reservaService.reservarLivro(reserva);

                verify(entityManager, never()).persist(any(Reserva.class));
        }

        @Test
        @DisplayName("Não deve persistir reserva quando usuário já tem empréstimo ativo do mesmo livro")
        void naoDevePersistirReservaQuandoUsuarioJaTemEmprestimoAtivoDoMesmoLivro() {
                Reserva reserva = Reserva.builder()
                                .usuarioId(10L)
                                .isbnLivro("978-1111")
                                .build();
                Usuario usuarioValido = Usuario.builder()
                                .id(10L)
                                .bloqueado(Boolean.FALSE)
                                .build();

                when(entityManager.find(Usuario.class, 10L)).thenReturn(usuarioValido);

                when(entityManager.createQuery(
                                "SELECT COUNT(e) FROM Emprestimo e " +
                                                "WHERE e.usuario.id = :usuarioId " +
                                                "AND e.exemplar.livro.isbn = :isbn " +
                                                "AND (e.status = 'ATIVO' OR e.status = 'RENOVADO')",
                                Long.class))
                                .thenReturn(queryDeContagemDeEmprestimo);
                when(queryDeContagemDeEmprestimo.setParameter("usuarioId", 10L))
                                .thenReturn(queryDeContagemDeEmprestimo);
                when(queryDeContagemDeEmprestimo.setParameter("isbn", "978-1111"))
                                .thenReturn(queryDeContagemDeEmprestimo);
                when(queryDeContagemDeEmprestimo.getSingleResult()).thenReturn(1L);

                lenient().when(entityManager.createQuery(
                                "SELECT COUNT(r) FROM Reserva r " +
                                                "WHERE r.usuarioId = :usuarioId " +
                                                "AND r.isbnLivro = :isbn " +
                                                "AND r.status = 'RESERVADO'",
                                Long.class))
                                .thenReturn(queryDeContagemDeReserva);

                reservaService.reservarLivro(reserva);

                verify(entityManager, never()).persist(any(Reserva.class));
        }

        @Test
        @DisplayName("Não deve persistir reserva quando usuário já tem reserva ativa para o mesmo livro")
        void naoDevePersistirReservaQuandoUsuarioJaTemReservaAtivaParaOMesmoLivro() {
                Reserva reserva = Reserva.builder()
                                .usuarioId(10L)
                                .isbnLivro("978-1111")
                                .build();
                Usuario usuarioValido = Usuario.builder()
                                .id(10L)
                                .bloqueado(Boolean.FALSE)
                                .build();

                when(entityManager.find(Usuario.class, 10L)).thenReturn(usuarioValido);

                when(entityManager.createQuery(
                                "SELECT COUNT(e) FROM Emprestimo e " +
                                                "WHERE e.usuario.id = :usuarioId " +
                                                "AND e.exemplar.livro.isbn = :isbn " +
                                                "AND (e.status = 'ATIVO' OR e.status = 'RENOVADO')",
                                Long.class))
                                .thenReturn(queryDeContagemDeEmprestimo);
                when(queryDeContagemDeEmprestimo.setParameter("usuarioId", 10L))
                                .thenReturn(queryDeContagemDeEmprestimo);
                when(queryDeContagemDeEmprestimo.setParameter("isbn", "978-1111"))
                                .thenReturn(queryDeContagemDeEmprestimo);
                when(queryDeContagemDeEmprestimo.getSingleResult()).thenReturn(0L);

                when(entityManager.createQuery(
                                "SELECT COUNT(r) FROM Reserva r " +
                                                "WHERE r.usuarioId = :usuarioId " +
                                                "AND r.isbnLivro = :isbn " +
                                                "AND r.status = 'RESERVADO'",
                                Long.class))
                                .thenReturn(queryDeContagemDeReserva);
                when(queryDeContagemDeReserva.setParameter("usuarioId", 10L)).thenReturn(queryDeContagemDeReserva);
                when(queryDeContagemDeReserva.setParameter("isbn", "978-1111")).thenReturn(queryDeContagemDeReserva);
                when(queryDeContagemDeReserva.getSingleResult()).thenReturn(1L);

                reservaService.reservarLivro(reserva);

                verify(entityManager, never()).persist(any(Reserva.class));
        }

        @Test
        @DisplayName("Deve persistir reserva e atribuir ID gerado quando todas as validações passam")
        void devePersistirReservaEAtribuirIdGeradoQuandoTodasAsValidacoesPassam() {
                Reserva reserva = Reserva.builder()
                                .usuarioId(10L)
                                .isbnLivro("978-2222")
                                .build();
                Usuario usuarioValido = Usuario.builder()
                                .id(10L)
                                .bloqueado(Boolean.FALSE)
                                .build();

                when(entityManager.find(Usuario.class, 10L)).thenReturn(usuarioValido);

                when(entityManager.createQuery(
                                "SELECT COUNT(e) FROM Emprestimo e " +
                                                "WHERE e.usuario.id = :usuarioId " +
                                                "AND e.exemplar.livro.isbn = :isbn " +
                                                "AND (e.status = 'ATIVO' OR e.status = 'RENOVADO')",
                                Long.class))
                                .thenReturn(queryDeContagemDeEmprestimo);
                when(queryDeContagemDeEmprestimo.setParameter("usuarioId", 10L))
                                .thenReturn(queryDeContagemDeEmprestimo);
                when(queryDeContagemDeEmprestimo.setParameter("isbn", "978-2222"))
                                .thenReturn(queryDeContagemDeEmprestimo);
                when(queryDeContagemDeEmprestimo.getSingleResult()).thenReturn(0L);

                when(entityManager.createQuery(
                                "SELECT COUNT(r) FROM Reserva r " +
                                                "WHERE r.usuarioId = :usuarioId " +
                                                "AND r.isbnLivro = :isbn " +
                                                "AND r.status = 'RESERVADO'",
                                Long.class))
                                .thenReturn(queryDeContagemDeReserva);
                when(queryDeContagemDeReserva.setParameter("usuarioId", 10L)).thenReturn(queryDeContagemDeReserva);
                when(queryDeContagemDeReserva.setParameter("isbn", "978-2222")).thenReturn(queryDeContagemDeReserva);
                when(queryDeContagemDeReserva.getSingleResult()).thenReturn(0L);

                when(entityManager.createQuery("SELECT MAX(r.id) FROM Reserva r", Long.class))
                                .thenReturn(queryDeMaximoId);
                when(queryDeMaximoId.getSingleResult()).thenReturn(4L);

                reservaService.reservarLivro(reserva);

                verify(entityManager).persist(reserva);
                assertEquals(5L, reserva.getId());
        }

        // =====================================================================
        // cancelarReserva
        // =====================================================================

        @Test
        @DisplayName("Não deve alterar status quando reserva não existe no banco")
        void naoDeveAlterarStatusQuandoReservaNaoExisteNoBanco() {
                when(entityManager.find(Reserva.class, 1L)).thenReturn(null);

                reservaService.cancelarReserva(1L);

                verify(entityManager, never()).merge(any());
        }

        @Test
        @DisplayName("Não deve alterar status quando reserva já está cancelada")
        void naoDeveAlterarStatusQuandoReservaJaEstaCancelada() {
                Reserva reservaJaCancelada = Reserva.builder()
                                .id(1L)
                                .status(StatusReservaEnum.CANCELADO)
                                .build();

                when(entityManager.find(Reserva.class, 1L)).thenReturn(reservaJaCancelada);

                reservaService.cancelarReserva(1L);

                verify(entityManager, never()).merge(any());
        }

        @Test
        @DisplayName("Não deve alterar status quando reserva já foi atendida")
        void naoDeveAlterarStatusQuandoReservaJaFoiAtendida() {
                Reserva reservaAtendida = Reserva.builder()
                                .id(1L)
                                .status(StatusReservaEnum.ATENDIDA)
                                .build();

                when(entityManager.find(Reserva.class, 1L)).thenReturn(reservaAtendida);

                reservaService.cancelarReserva(1L);

                verify(entityManager, never()).merge(any());
        }

        @Test
        @DisplayName("Deve alterar status para cancelado quando reserva está ativa")
        void deveAlterarStatusParaCanceladoQuandoReservaEstaAtiva() {
                Reserva reservaAtiva = Reserva.builder()
                                .id(1L)
                                .status(StatusReservaEnum.RESERVADO)
                                .build();

                when(entityManager.find(Reserva.class, 1L)).thenReturn(reservaAtiva);
                when(entityManager.merge(reservaAtiva)).thenReturn(reservaAtiva);

                reservaService.cancelarReserva(1L);

                assertEquals(StatusReservaEnum.CANCELADO, reservaAtiva.getStatus());
                verify(entityManager).merge(reservaAtiva);
        }

        // =====================================================================
        // atenderProximaReserva
        // =====================================================================

        @Test
        @DisplayName("Não deve persistir notificação quando não há reservas pendentes para o ISBN")
        void naoDevePersistirNotificacaoQuandoNaoHaReservasPendentesParaOIsbn() {
                when(entityManager.createQuery(
                                "SELECT r FROM Reserva r WHERE r.isbnLivro = :isbn AND r.status = 'RESERVADO' ORDER BY r.dataReserva ASC",
                                Reserva.class))
                                .thenReturn(queryDeReserva);
                when(queryDeReserva.setParameter("isbn", "978-0000")).thenReturn(queryDeReserva);
                when(queryDeReserva.setMaxResults(1)).thenReturn(queryDeReserva);
                when(queryDeReserva.getResultList()).thenReturn(Collections.emptyList());

                reservaService.atenderProximaReserva("978-0000");

                verify(entityManager, never()).persist(any(Notificacao.class));
        }

        @Test
        @DisplayName("Não deve persistir notificação quando usuário vinculado à reserva não existe no banco")
        void naoDevePersistirNotificacaoQuandoUsuarioVinculadoAReservaNaoExisteNoBanco() {
                Reserva proximaReserva = Reserva.builder()
                                .id(1L)
                                .usuarioId(10L)
                                .isbnLivro("978-1111")
                                .dataReserva(LocalDateTime.now())
                                .build();

                when(entityManager.createQuery(
                                "SELECT r FROM Reserva r WHERE r.isbnLivro = :isbn AND r.status = 'RESERVADO' ORDER BY r.dataReserva ASC",
                                Reserva.class))
                                .thenReturn(queryDeReserva);
                when(queryDeReserva.setParameter("isbn", "978-1111")).thenReturn(queryDeReserva);
                when(queryDeReserva.setMaxResults(1)).thenReturn(queryDeReserva);
                when(queryDeReserva.getResultList()).thenReturn(List.of(proximaReserva));

                when(entityManager.find(Usuario.class, 10L)).thenReturn(null);
                when(entityManager.find(Livro.class, "978-1111")).thenReturn(
                                Livro.builder().isbn("978-1111").titulo("Livro Qualquer").build());

                reservaService.atenderProximaReserva("978-1111");

                verify(entityManager, never()).persist(any(Notificacao.class));
        }

        @Test
        @DisplayName("Não deve persistir notificação quando livro vinculado à reserva não existe no banco")
        void naoDevePersistirNotificacaoQuandoLivroVinculadoAReservaNaoExisteNoBanco() {
                Reserva proximaReserva = Reserva.builder()
                                .id(1L)
                                .usuarioId(10L)
                                .isbnLivro("978-1111")
                                .dataReserva(LocalDateTime.now())
                                .build();

                when(entityManager.createQuery(
                                "SELECT r FROM Reserva r WHERE r.isbnLivro = :isbn AND r.status = 'RESERVADO' ORDER BY r.dataReserva ASC",
                                Reserva.class))
                                .thenReturn(queryDeReserva);
                when(queryDeReserva.setParameter("isbn", "978-1111")).thenReturn(queryDeReserva);
                when(queryDeReserva.setMaxResults(1)).thenReturn(queryDeReserva);
                when(queryDeReserva.getResultList()).thenReturn(List.of(proximaReserva));

                when(entityManager.find(Usuario.class, 10L)).thenReturn(
                                Usuario.builder().id(10L).nome("Usuário Teste").build());
                when(entityManager.find(Livro.class, "978-1111")).thenReturn(null);

                reservaService.atenderProximaReserva("978-1111");

                verify(entityManager, never()).persist(any(Notificacao.class));
        }

        @Test
        @DisplayName("Deve persistir notificação quando usuário e livro da reserva são encontrados")
        void devePersistirNotificacaoQuandoUsuarioELivroDaReservaSaoEncontrados() {
                Reserva proximaReserva = Reserva.builder()
                                .id(1L)
                                .usuarioId(10L)
                                .isbnLivro("978-3333")
                                .dataReserva(LocalDateTime.now())
                                .build();
                Usuario usuarioEncontrado = Usuario.builder()
                                .id(10L)
                                .nome("Maria Silva")
                                .email("maria@email.com")
                                .build();
                Livro livroEncontrado = Livro.builder()
                                .isbn("978-3333")
                                .titulo("Clean Code")
                                .build();

                when(entityManager.createQuery(
                                "SELECT r FROM Reserva r WHERE r.isbnLivro = :isbn AND r.status = 'RESERVADO' ORDER BY r.dataReserva ASC",
                                Reserva.class))
                                .thenReturn(queryDeReserva);
                when(queryDeReserva.setParameter("isbn", "978-3333")).thenReturn(queryDeReserva);
                when(queryDeReserva.setMaxResults(1)).thenReturn(queryDeReserva);
                when(queryDeReserva.getResultList()).thenReturn(List.of(proximaReserva));

                when(entityManager.find(Usuario.class, 10L)).thenReturn(usuarioEncontrado);
                when(entityManager.find(Livro.class, "978-3333")).thenReturn(livroEncontrado);

                when(entityManager.createQuery("SELECT MAX(n.id) FROM Notificacao n", Long.class))
                                .thenReturn(queryDeMaximoId);
                when(queryDeMaximoId.getSingleResult()).thenReturn(3L);

                reservaService.atenderProximaReserva("978-3333");

                verify(entityManager).persist(any(Notificacao.class));
        }

        // =====================================================================
        // buscarReservaPorTituloLivro
        // =====================================================================

        @Test
        @DisplayName("Não deve executar find de usuário ou livro quando nenhuma reserva é encontrada pelo título")
        void naoDeveExecutarFindDeUsuarioOuLivroQuandoNenhumaReservaEEncontradaPeloTitulo() {
                when(entityManager.createQuery(
                                "SELECT r FROM Reserva r WHERE r.isbnLivro IN (" +
                                                "SELECT l.isbn FROM Livro l WHERE LOWER(l.titulo) LIKE LOWER(:titulo)) "
                                                +
                                                "ORDER BY r.dataReserva ASC",
                                Reserva.class))
                                .thenReturn(queryDeReserva);
                when(queryDeReserva.setParameter("titulo", "%Título Inexistente%")).thenReturn(queryDeReserva);
                when(queryDeReserva.getResultList()).thenReturn(Collections.emptyList());

                reservaService.buscarReservaPorTituloLivro("Título Inexistente");

                verify(entityManager, never()).find(any(), any());
        }

        @Test
        @DisplayName("Deve executar find de usuário e livro para cada reserva encontrada pelo título")
        void deveExecutarFindDeUsuarioELivroParaCadaReservaEncontradaPeloTitulo() {
                Reserva primeiraReserva = Reserva.builder()
                                .id(1L)
                                .usuarioId(10L)
                                .isbnLivro("978-4444")
                                .dataReserva(LocalDateTime.now())
                                .status(StatusReservaEnum.RESERVADO)
                                .build();
                Reserva segundaReserva = Reserva.builder()
                                .id(2L)
                                .usuarioId(20L)
                                .isbnLivro("978-4444")
                                .dataReserva(LocalDateTime.now())
                                .status(StatusReservaEnum.RESERVADO)
                                .build();

                when(entityManager.createQuery(
                                "SELECT r FROM Reserva r WHERE r.isbnLivro IN (" +
                                                "SELECT l.isbn FROM Livro l WHERE LOWER(l.titulo) LIKE LOWER(:titulo)) "
                                                +
                                                "ORDER BY r.dataReserva ASC",
                                Reserva.class))
                                .thenReturn(queryDeReserva);
                when(queryDeReserva.setParameter("titulo", "%Java%")).thenReturn(queryDeReserva);
                when(queryDeReserva.getResultList()).thenReturn(List.of(primeiraReserva, segundaReserva));

                when(entityManager.find(Usuario.class, 10L)).thenReturn(
                                Usuario.builder().id(10L).nome("João").build());
                when(entityManager.find(Usuario.class, 20L)).thenReturn(
                                Usuario.builder().id(20L).nome("Ana").build());
                when(entityManager.find(Livro.class, "978-4444")).thenReturn(
                                Livro.builder().isbn("978-4444").titulo("Java Efetivo").build());

                reservaService.buscarReservaPorTituloLivro("Java");

                verify(entityManager).find(Usuario.class, 10L);
                verify(entityManager).find(Usuario.class, 20L);
                verify(entityManager, times(2)).find(Livro.class, "978-4444");
        }

        // =====================================================================
        // listarNotificacoes
        // =====================================================================

        @Test
        @DisplayName("Deve exibir mensagem quando não houver notificações registradas")
        void deveExibirMensagemQuandoNaoHouverNotificacoesRegistradas() {
                when(entityManager.createQuery("SELECT n FROM Notificacao n ORDER BY n.id", Notificacao.class))
                                .thenReturn(queryDeNotificacao);
                when(queryDeNotificacao.getResultList()).thenReturn(Collections.emptyList());

                reservaService.listarNotificacoes();

                verify(queryDeNotificacao).getResultList();
                verify(entityManager, never()).find(eq(Usuario.class), any());
        }

        @Test
        @DisplayName("Deve listar notificações exibindo o usuário vinculado quando houver registros")
        void deveListarNotificacoesExibindoUsuarioVinculadoQuandoHouverRegistros() {
                Notificacao notificacaoNaoLida = Notificacao.builder()
                                .id(1L)
                                .usuarioId(10L)
                                .mensagem("Seu livro está disponível.")
                                .lida(false)
                                .build();

                Notificacao notificacaoLida = Notificacao.builder()
                                .id(2L)
                                .usuarioId(20L)
                                .mensagem("Sua reserva foi atendida.")
                                .lida(true)
                                .build();

                Usuario primeiroUsuario = Usuario.builder().id(10L).nome("Carlos").build();
                Usuario segundoUsuario = Usuario.builder().id(20L).nome("Ana").build();

                when(entityManager.createQuery("SELECT n FROM Notificacao n ORDER BY n.id", Notificacao.class))
                                .thenReturn(queryDeNotificacao);
                when(queryDeNotificacao.getResultList()).thenReturn(List.of(notificacaoNaoLida, notificacaoLida));
                when(entityManager.find(Usuario.class, 10L)).thenReturn(primeiroUsuario);
                when(entityManager.find(Usuario.class, 20L)).thenReturn(segundoUsuario);

                reservaService.listarNotificacoes();

                verify(entityManager).find(Usuario.class, 10L);
                verify(entityManager).find(Usuario.class, 20L);
        }

        @Test
        @DisplayName("Deve exibir 'Não encontrado' quando o usuário da notificação não existir no banco")
        void deveExibirNaoEncontradoQuandoUsuarioDaNotificacaoNaoExistirNoBanco() {
                Notificacao notificacao = Notificacao.builder()
                                .id(1L)
                                .usuarioId(99L)
                                .mensagem("Mensagem qualquer")
                                .lida(false)
                                .build();

                when(entityManager.createQuery("SELECT n FROM Notificacao n ORDER BY n.id", Notificacao.class))
                                .thenReturn(queryDeNotificacao);
                when(queryDeNotificacao.getResultList()).thenReturn(List.of(notificacao));
                when(entityManager.find(Usuario.class, 99L)).thenReturn(null);

                reservaService.listarNotificacoes();

                verify(entityManager).find(Usuario.class, 99L);
        }
}