package br.unisales.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import br.unisales.database.table.Livro;
import br.unisales.database.table.Multa;
import br.unisales.database.table.Reserva;
import br.unisales.database.table.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

@ExtendWith(MockitoExtension.class)
public class EmprestimoServiceTest {

        @Mock
        private EntityManagerFactory entityManagerFactory;

        @Mock
        private EntityManager entityManager;

        @Mock
        private EntityTransaction entityTransaction;

        @Mock
        private TypedQuery<Reserva> queryDeReserva;

        @Mock
        private TypedQuery<Reserva> queryDeReservaDoUsuario;

        @Mock
        private TypedQuery<Emprestimo> queryDeEmprestimo;

        @Mock
        private TypedQuery<Multa> queryDeMulta;

        @Mock
        private TypedQuery<Long> queryDeMaximoId;

        private EmprestimoService emprestimoService;

        @BeforeEach
        void setUp() {
                when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);
                lenient().when(entityManager.getTransaction()).thenReturn(entityTransaction);
                emprestimoService = new EmprestimoService(entityManagerFactory);
        }

        // =====================================================================
        // emprestarExemplar
        // =====================================================================

        @Test
        @DisplayName("Não deve persistir empréstimo quando exemplar não existe no banco")
        void naoDevePersistirEmprestimoQuandoExemplarNaoExisteNoBanco() {
                Exemplar exemplarReferenciado = Exemplar.builder().id(1L).build();
                Usuario usuarioReferenciado = Usuario.builder().id(10L).build();
                Emprestimo emprestimo = Emprestimo.builder()
                                .exemplar(exemplarReferenciado)
                                .usuario(usuarioReferenciado)
                                .build();

                when(entityManager.find(Exemplar.class, 1L)).thenReturn(null);

                emprestimoService.emprestarExemplar(emprestimo);

                verify(entityManager, never()).persist(any(Emprestimo.class));
        }

        @Test
        @DisplayName("Não deve persistir empréstimo quando exemplar não está disponível")
        void naoDevePersistirEmprestimoQuandoExemplarNaoEstaDisponivel() {
                Livro livro = Livro.builder().isbn("978-1111").build();
                Exemplar exemplarEmprestado = Exemplar.builder()
                                .id(1L)
                                .livro(livro)
                                .status(StatusExemplarEnum.EMPRESTADO)
                                .build();
                Usuario usuarioReferenciado = Usuario.builder().id(10L).build();
                Emprestimo emprestimo = Emprestimo.builder()
                                .exemplar(exemplarEmprestado)
                                .usuario(usuarioReferenciado)
                                .build();

                when(entityManager.find(Exemplar.class, 1L)).thenReturn(exemplarEmprestado);

                emprestimoService.emprestarExemplar(emprestimo);

                verify(entityManager, never()).persist(any(Emprestimo.class));
        }

        @Test
        @DisplayName("Não deve persistir empréstimo quando usuário não existe no banco")
        void naoDevePersistirEmprestimoQuandoUsuarioNaoExisteNoBanco() {
                Livro livro = Livro.builder().isbn("978-1111").build();
                Exemplar exemplarDisponivel = Exemplar.builder()
                                .id(1L)
                                .livro(livro)
                                .status(StatusExemplarEnum.DISPONIVEL)
                                .build();
                Usuario usuarioReferenciado = Usuario.builder().id(10L).build();
                Emprestimo emprestimo = Emprestimo.builder()
                                .exemplar(exemplarDisponivel)
                                .usuario(usuarioReferenciado)
                                .build();

                when(entityManager.find(Exemplar.class, 1L)).thenReturn(exemplarDisponivel);
                when(entityManager.find(Usuario.class, 10L)).thenReturn(null);

                emprestimoService.emprestarExemplar(emprestimo);

                verify(entityManager, never()).persist(any(Emprestimo.class));
        }

        @Test
        @DisplayName("Não deve persistir empréstimo quando usuário está bloqueado")
        void naoDevePersistirEmprestimoQuandoUsuarioEstaBloqueado() {
                Livro livro = Livro.builder().isbn("978-1111").build();
                Exemplar exemplarDisponivel = Exemplar.builder()
                                .id(1L)
                                .livro(livro)
                                .status(StatusExemplarEnum.DISPONIVEL)
                                .build();
                Usuario usuarioBloqueado = Usuario.builder()
                                .id(10L)
                                .bloqueado(Boolean.TRUE)
                                .build();
                Emprestimo emprestimo = Emprestimo.builder()
                                .exemplar(exemplarDisponivel)
                                .usuario(usuarioBloqueado)
                                .build();

                when(entityManager.find(Exemplar.class, 1L)).thenReturn(exemplarDisponivel);
                when(entityManager.find(Usuario.class, 10L)).thenReturn(usuarioBloqueado);

                emprestimoService.emprestarExemplar(emprestimo);

                verify(entityManager, never()).persist(any(Emprestimo.class));
        }

        @Test
        @DisplayName("Não deve persistir empréstimo quando outro usuário é o próximo da fila de reservas")
        void naoDevePersistirEmprestimoQuandoOutroUsuarioEOProximoDaFilaDeReservas() {
                Livro livro = Livro.builder().isbn("978-1111").build();
                Exemplar exemplarDisponivel = Exemplar.builder()
                                .id(1L)
                                .livro(livro)
                                .status(StatusExemplarEnum.DISPONIVEL)
                                .build();
                Usuario usuarioSolicitante = Usuario.builder().id(10L).bloqueado(Boolean.FALSE).build();
                Reserva reservaDeOutroUsuario = Reserva.builder().usuarioId(99L).build();
                Emprestimo emprestimo = Emprestimo.builder()
                                .exemplar(exemplarDisponivel)
                                .usuario(usuarioSolicitante)
                                .build();

                when(entityManager.find(Exemplar.class, 1L)).thenReturn(exemplarDisponivel);
                when(entityManager.find(Usuario.class, 10L)).thenReturn(usuarioSolicitante);

                when(entityManager.createQuery(
                                "SELECT r FROM Reserva r WHERE r.isbnLivro = :isbn AND r.status = 'RESERVADO' ORDER BY r.dataReserva ASC",
                                Reserva.class))
                                .thenReturn(queryDeReserva);
                when(queryDeReserva.setParameter("isbn", "978-1111")).thenReturn(queryDeReserva);
                when(queryDeReserva.setMaxResults(1)).thenReturn(queryDeReserva);
                when(queryDeReserva.getResultList()).thenReturn(List.of(reservaDeOutroUsuario));

                lenient().when(entityManager.find(Usuario.class, 99L)).thenReturn(null);

                emprestimoService.emprestarExemplar(emprestimo);

                verify(entityManager, never()).persist(any(Emprestimo.class));
        }

        @Test
        @DisplayName("Deve persistir empréstimo quando exemplar está disponível, usuário válido e sem fila de reservas")
        void devePersistirEmprestimoQuandoExemplarDisponivelUsuarioValidoESemFilaDeReservas() {
                Livro livro = Livro.builder().isbn("978-2222").build();
                Exemplar exemplarDisponivel = Exemplar.builder()
                                .id(2L)
                                .livro(livro)
                                .status(StatusExemplarEnum.DISPONIVEL)
                                .build();
                Usuario usuarioValido = Usuario.builder().id(20L).bloqueado(Boolean.FALSE).build();
                Emprestimo emprestimo = Emprestimo.builder()
                                .exemplar(exemplarDisponivel)
                                .usuario(usuarioValido)
                                .build();

                when(entityManager.find(Exemplar.class, 2L)).thenReturn(exemplarDisponivel);
                when(entityManager.find(Usuario.class, 20L)).thenReturn(usuarioValido);

                when(entityManager.createQuery(
                                "SELECT r FROM Reserva r WHERE r.isbnLivro = :isbn AND r.status = 'RESERVADO' ORDER BY r.dataReserva ASC",
                                Reserva.class))
                                .thenReturn(queryDeReserva);
                when(queryDeReserva.setParameter("isbn", "978-2222")).thenReturn(queryDeReserva);
                when(queryDeReserva.setMaxResults(1)).thenReturn(queryDeReserva);
                when(queryDeReserva.getResultList()).thenReturn(Collections.emptyList());

                when(entityManager.createQuery(
                                "SELECT r FROM Reserva r WHERE r.isbnLivro = :isbn AND r.usuarioId = :usuarioId AND r.status = 'RESERVADO'",
                                Reserva.class))
                                .thenReturn(queryDeReservaDoUsuario);
                when(queryDeReservaDoUsuario.setParameter("isbn", "978-2222")).thenReturn(queryDeReservaDoUsuario);
                when(queryDeReservaDoUsuario.setParameter("usuarioId", 20L)).thenReturn(queryDeReservaDoUsuario);
                when(queryDeReservaDoUsuario.getResultList()).thenReturn(Collections.emptyList());

                when(entityManager.createQuery("SELECT MAX(e.id) FROM Emprestimo e", Long.class))
                                .thenReturn(queryDeMaximoId);
                when(queryDeMaximoId.getSingleResult()).thenReturn(7L);

                when(entityManager.merge(exemplarDisponivel)).thenReturn(exemplarDisponivel);

                emprestimoService.emprestarExemplar(emprestimo);

                verify(entityManager).persist(emprestimo);
                assertEquals(StatusExemplarEnum.EMPRESTADO, exemplarDisponivel.getStatus());
                assertEquals(8L, emprestimo.getId());
        }

        @Test
        @DisplayName("Deve persistir empréstimo e marcar reserva como atendida quando usuário é o próximo da fila")
        void devePersistirEmprestimoEMarcarReservaComoAtendidaQuandoUsuarioEOProximoDaFila() {
                Livro livro = Livro.builder().isbn("978-3333").build();
                Exemplar exemplarDisponivel = Exemplar.builder()
                                .id(3L)
                                .livro(livro)
                                .status(StatusExemplarEnum.DISPONIVEL)
                                .build();
                Usuario usuarioComReserva = Usuario.builder().id(30L).bloqueado(Boolean.FALSE).build();
                Reserva reservaDoProprioUsuario = Reserva.builder().usuarioId(30L).build();
                Emprestimo emprestimo = Emprestimo.builder()
                                .exemplar(exemplarDisponivel)
                                .usuario(usuarioComReserva)
                                .build();

                when(entityManager.find(Exemplar.class, 3L)).thenReturn(exemplarDisponivel);
                when(entityManager.find(Usuario.class, 30L)).thenReturn(usuarioComReserva);

                when(entityManager.createQuery(
                                "SELECT r FROM Reserva r WHERE r.isbnLivro = :isbn AND r.status = 'RESERVADO' ORDER BY r.dataReserva ASC",
                                Reserva.class))
                                .thenReturn(queryDeReserva);
                when(queryDeReserva.setParameter("isbn", "978-3333")).thenReturn(queryDeReserva);
                when(queryDeReserva.setMaxResults(1)).thenReturn(queryDeReserva);
                when(queryDeReserva.getResultList()).thenReturn(List.of(reservaDoProprioUsuario));

                when(entityManager.createQuery(
                                "SELECT r FROM Reserva r WHERE r.isbnLivro = :isbn AND r.usuarioId = :usuarioId AND r.status = 'RESERVADO'",
                                Reserva.class))
                                .thenReturn(queryDeReservaDoUsuario);
                when(queryDeReservaDoUsuario.setParameter("isbn", "978-3333")).thenReturn(queryDeReservaDoUsuario);
                when(queryDeReservaDoUsuario.setParameter("usuarioId", 30L)).thenReturn(queryDeReservaDoUsuario);
                when(queryDeReservaDoUsuario.getResultList()).thenReturn(List.of(reservaDoProprioUsuario));

                when(entityManager.createQuery("SELECT MAX(e.id) FROM Emprestimo e", Long.class))
                                .thenReturn(queryDeMaximoId);
                when(queryDeMaximoId.getSingleResult()).thenReturn(9L);

                when(entityManager.merge(exemplarDisponivel)).thenReturn(exemplarDisponivel);
                when(entityManager.merge(reservaDoProprioUsuario)).thenReturn(reservaDoProprioUsuario);

                emprestimoService.emprestarExemplar(emprestimo);

                verify(entityManager).persist(emprestimo);
                assertEquals(StatusExemplarEnum.EMPRESTADO, exemplarDisponivel.getStatus());
        }

        // =====================================================================
        // devolverExemplar
        // =====================================================================

        @Test
        @DisplayName("Não deve registrar devolução quando empréstimo não existe no banco")
        void naoDeveRegistrarDevolucaoQuandoEmprestimoNaoExisteNoBanco() {
                when(entityManager.find(Emprestimo.class, 1L)).thenReturn(null);

                emprestimoService.devolverExemplar(1L);

                verify(entityManager, never()).merge(any());
        }

        @Test
        @DisplayName("Não deve registrar devolução quando empréstimo já está encerrado")
        void naoDeveRegistrarDevolucaoQuandoEmprestimoJaEstaEncerrado() {
                Emprestimo emprestimoDevolvido = Emprestimo.builder()
                                .id(1L)
                                .status(StatusEmprestimoEnum.DEVOLVIDO)
                                .build();

                when(entityManager.find(Emprestimo.class, 1L)).thenReturn(emprestimoDevolvido);

                emprestimoService.devolverExemplar(1L);

                verify(entityManager, never()).merge(any());
        }

        @Test
        @DisplayName("Não deve registrar devolução quando exemplar vinculado não existe no banco")
        void naoDeveRegistrarDevolucaoQuandoExemplarVinculadoNaoExisteNoBanco() {
                Exemplar exemplarReferenciado = Exemplar.builder().id(5L).build();
                Emprestimo emprestimoAtivo = Emprestimo.builder()
                                .id(1L)
                                .status(StatusEmprestimoEnum.ATIVO)
                                .exemplar(exemplarReferenciado)
                                .build();

                when(entityManager.find(Emprestimo.class, 1L)).thenReturn(emprestimoAtivo);
                when(entityManager.find(Exemplar.class, 5L)).thenReturn(null);

                emprestimoService.devolverExemplar(1L);

                verify(entityManager, never()).merge(any());
        }

        @Test
        @DisplayName("Deve registrar devolução e liberar exemplar quando empréstimo está ativo")
        void deveRegistrarDevolucaoELiberarExemplarQuandoEmprestimoEstaAtivo() {
                Exemplar exemplarEmprestado = Exemplar.builder()
                                .id(5L)
                                .status(StatusExemplarEnum.EMPRESTADO)
                                .build();
                Emprestimo emprestimoAtivo = Emprestimo.builder()
                                .id(1L)
                                .status(StatusEmprestimoEnum.ATIVO)
                                .exemplar(exemplarEmprestado)
                                .build();

                when(entityManager.find(Emprestimo.class, 1L)).thenReturn(emprestimoAtivo);
                when(entityManager.find(Exemplar.class, 5L)).thenReturn(exemplarEmprestado);
                when(entityManager.merge(exemplarEmprestado)).thenReturn(exemplarEmprestado);
                when(entityManager.merge(emprestimoAtivo)).thenReturn(emprestimoAtivo);

                emprestimoService.devolverExemplar(1L);

                assertEquals(StatusExemplarEnum.DISPONIVEL, exemplarEmprestado.getStatus());
                assertEquals(StatusEmprestimoEnum.DEVOLVIDO, emprestimoAtivo.getStatus());
                assertNotNull(emprestimoAtivo.getDataDevolucao());
        }

        @Test
        @DisplayName("Deve registrar devolução quando empréstimo está renovado")
        void deveRegistrarDevolucaoQuandoEmprestimoEstaRenovado() {
                Exemplar exemplarEmprestado = Exemplar.builder()
                                .id(6L)
                                .status(StatusExemplarEnum.EMPRESTADO)
                                .build();
                Emprestimo emprestimoRenovado = Emprestimo.builder()
                                .id(2L)
                                .status(StatusEmprestimoEnum.RENOVADO)
                                .exemplar(exemplarEmprestado)
                                .build();

                when(entityManager.find(Emprestimo.class, 2L)).thenReturn(emprestimoRenovado);
                when(entityManager.find(Exemplar.class, 6L)).thenReturn(exemplarEmprestado);
                when(entityManager.merge(exemplarEmprestado)).thenReturn(exemplarEmprestado);
                when(entityManager.merge(emprestimoRenovado)).thenReturn(emprestimoRenovado);

                emprestimoService.devolverExemplar(2L);

                assertEquals(StatusExemplarEnum.DISPONIVEL, exemplarEmprestado.getStatus());
                assertEquals(StatusEmprestimoEnum.DEVOLVIDO, emprestimoRenovado.getStatus());
        }

        // =====================================================================
        // renovarExemplar
        // =====================================================================

        @Test
        @DisplayName("Não deve renovar quando empréstimo não existe no banco")
        void naoDeveRenovarQuandoEmprestimoNaoExisteNoBanco() {
                when(entityManager.find(Emprestimo.class, 1L)).thenReturn(null);

                emprestimoService.renovarExemplar(1L);

                verify(entityManager, never()).merge(any());
        }

        @Test
        @DisplayName("Não deve renovar quando empréstimo já foi renovado anteriormente")
        void naoDeveRenovarQuandoEmprestimoJaFoiRenovadoAnteriormente() {
                Emprestimo emprestimoJaRenovado = Emprestimo.builder()
                                .id(1L)
                                .status(StatusEmprestimoEnum.RENOVADO)
                                .build();

                when(entityManager.find(Emprestimo.class, 1L)).thenReturn(emprestimoJaRenovado);

                emprestimoService.renovarExemplar(1L);

                verify(entityManager, never()).merge(any());
        }

        @Test
        @DisplayName("Não deve renovar quando empréstimo já foi devolvido")
        void naoDeveRenovarQuandoEmprestimoJaFoiDevolvido() {
                Emprestimo emprestimoDevolvido = Emprestimo.builder()
                                .id(1L)
                                .status(StatusEmprestimoEnum.DEVOLVIDO)
                                .build();

                when(entityManager.find(Emprestimo.class, 1L)).thenReturn(emprestimoDevolvido);

                emprestimoService.renovarExemplar(1L);

                verify(entityManager, never()).merge(any());
        }

        @Test
        @DisplayName("Deve estender prazo em 7 dias e alterar status para renovado quando empréstimo está ativo")
        void deveEstenderPrazoEmSeteDiasEAlterarStatusParaRenovadoQuandoEmprestimoEstaAtivo() {
                LocalDateTime dataOriginal = LocalDateTime.of(2025, 6, 10, 0, 0);
                Emprestimo emprestimoAtivo = Emprestimo.builder()
                                .id(1L)
                                .status(StatusEmprestimoEnum.ATIVO)
                                .dataDevolucaoPrevista(dataOriginal)
                                .build();

                when(entityManager.find(Emprestimo.class, 1L)).thenReturn(emprestimoAtivo);
                when(entityManager.merge(emprestimoAtivo)).thenReturn(emprestimoAtivo);

                emprestimoService.renovarExemplar(1L);

                assertEquals(dataOriginal.plusDays(7), emprestimoAtivo.getDataDevolucaoPrevista());
                assertEquals(StatusEmprestimoEnum.RENOVADO, emprestimoAtivo.getStatus());
        }

        // =====================================================================
        // calcularMulta
        // =====================================================================

        @Test
        @DisplayName("Deve retornar zero quando empréstimo não existe no banco")
        void deveRetornarZeroQuandoEmprestimoNaoExisteNoBanco() {
                when(entityManager.find(Emprestimo.class, 1L)).thenReturn(null);

                double multaCalculada = emprestimoService.calcularMulta(1L);

                assertEquals(0.0, multaCalculada);
        }

        @Test
        @DisplayName("Deve retornar zero quando livro ainda não foi devolvido")
        void deveRetornarZeroQuandoLivroAindaNaoFoiDevolvido() {
                Emprestimo emprestimoAindaAtivo = Emprestimo.builder()
                                .id(1L)
                                .status(StatusEmprestimoEnum.ATIVO)
                                .build();

                when(entityManager.find(Emprestimo.class, 1L)).thenReturn(emprestimoAindaAtivo);

                double multaCalculada = emprestimoService.calcularMulta(1L);

                assertEquals(0.0, multaCalculada);
        }

        @Test
        @DisplayName("Deve retornar zero quando devolução ocorreu dentro do prazo")
        void deveRetornarZeroQuandoDevolucaoOcorreuDentroNoPrazo() {
                Emprestimo emprestimoNoPrazo = Emprestimo.builder()
                                .id(1L)
                                .status(StatusEmprestimoEnum.DEVOLVIDO)
                                .dataDevolucao(LocalDateTime.of(2025, 6, 5, 10, 0))
                                .dataDevolucaoPrevista(LocalDateTime.of(2025, 6, 10, 23, 59))
                                .build();

                when(entityManager.find(Emprestimo.class, 1L)).thenReturn(emprestimoNoPrazo);

                double multaCalculada = emprestimoService.calcularMulta(1L);

                assertEquals(0.0, multaCalculada);
        }

        @Test
        @DisplayName("Deve calcular multa corretamente e persistir quando devolução foi entregue com atraso")
        void deveCalcularMultaCorretamenteEPersistirQuandoDevolucaoFoiEntregueComAtraso() {
                // prazo era dia 1, devolução no dia 4: 3 dias de atraso = R$ 6,00
                Emprestimo emprestimoComAtraso = Emprestimo.builder()
                                .id(1L)
                                .status(StatusEmprestimoEnum.DEVOLVIDO)
                                .dataDevolucao(LocalDateTime.of(2025, 6, 4, 0, 0))
                                .dataDevolucaoPrevista(LocalDateTime.of(2025, 6, 1, 0, 0))
                                .build();

                when(entityManager.find(Emprestimo.class, 1L)).thenReturn(emprestimoComAtraso);

                when(entityManager.createQuery(
                                "SELECT m FROM Multa m WHERE m.emprestimoId = :emprestimoId",
                                Multa.class))
                                .thenReturn(queryDeMulta);
                when(queryDeMulta.setParameter("emprestimoId", 1L)).thenReturn(queryDeMulta);
                when(queryDeMulta.getResultList()).thenReturn(Collections.emptyList());

                when(entityManager.createQuery("SELECT MAX(m.id) FROM Multa m", Long.class))
                                .thenReturn(queryDeMaximoId);
                when(queryDeMaximoId.getSingleResult()).thenReturn(2L);

                double multaCalculada = emprestimoService.calcularMulta(1L);

                assertEquals(6.0, multaCalculada);
                verify(entityManager).persist(any(Multa.class));
        }

        @Test
        @DisplayName("Deve retornar valor da multa sem persistir quando multa já havia sido registrada")
        void deveRetornarValorDaMultaSemPersistirQuandoMultaJaHaviaSidoRegistrada() {
                Emprestimo emprestimoComAtraso = Emprestimo.builder()
                                .id(2L)
                                .status(StatusEmprestimoEnum.DEVOLVIDO)
                                .dataDevolucao(LocalDateTime.of(2025, 6, 10, 0, 0))
                                .dataDevolucaoPrevista(LocalDateTime.of(2025, 6, 5, 0, 0))
                                .build();
                Multa multaJaExistente = Multa.builder().id(1L).emprestimoId(2L).valor(10.0).build();

                when(entityManager.find(Emprestimo.class, 2L)).thenReturn(emprestimoComAtraso);

                when(entityManager.createQuery(
                                "SELECT m FROM Multa m WHERE m.emprestimoId = :emprestimoId",
                                Multa.class))
                                .thenReturn(queryDeMulta);
                when(queryDeMulta.setParameter("emprestimoId", 2L)).thenReturn(queryDeMulta);
                when(queryDeMulta.getResultList()).thenReturn(List.of(multaJaExistente));

                double multaCalculada = emprestimoService.calcularMulta(2L);

                assertEquals(10.0, multaCalculada);
                verify(entityManager, never()).persist(any(Multa.class));
        }

        // =====================================================================
        // listarEmprestimos
        // =====================================================================

        @Test
        @DisplayName("Não deve lançar exceção e encerrar normalmente quando não há empréstimos ativos")
        void naoDeveLancarExcecaoEEncerrarNormalmenteQuandoNaoHaEmprestimosAtivos() {
                when(entityManager.createQuery(
                                "SELECT e FROM Emprestimo e WHERE e.status = 'ATIVO' OR e.status = 'RENOVADO' OR e.status = 'ATRASADO'",
                                Emprestimo.class))
                                .thenReturn(queryDeEmprestimo);
                when(queryDeEmprestimo.getResultList()).thenReturn(Collections.emptyList());

                emprestimoService.listarEmprestimos();

                verify(entityManager).createQuery(
                                "SELECT e FROM Emprestimo e WHERE e.status = 'ATIVO' OR e.status = 'RENOVADO' OR e.status = 'ATRASADO'",
                                Emprestimo.class);
        }

        @Test
        @DisplayName("Deve executar a query e iterar resultados quando existem empréstimos ativos")
        void deveExecutarAQueryEIterarResultadosQuandoExistemEmprestimosAtivos() {
                Livro livro = Livro.builder().isbn("978-9999").titulo("Livro Ativo").build();
                Exemplar exemplar = Exemplar.builder().id(1L).livro(livro).build();
                Usuario usuario = Usuario.builder().id(1L).nome("Usuário Teste").build();
                Emprestimo emprestimoAtivo = Emprestimo.builder()
                                .id(1L)
                                .status(StatusEmprestimoEnum.ATIVO)
                                .usuario(usuario)
                                .exemplar(exemplar)
                                .dataEmprestimo(LocalDateTime.now())
                                .dataDevolucaoPrevista(LocalDateTime.now().plusDays(7))
                                .build();

                when(entityManager.createQuery(
                                "SELECT e FROM Emprestimo e WHERE e.status = 'ATIVO' OR e.status = 'RENOVADO' OR e.status = 'ATRASADO'",
                                Emprestimo.class))
                                .thenReturn(queryDeEmprestimo);
                when(queryDeEmprestimo.getResultList()).thenReturn(List.of(emprestimoAtivo));

                emprestimoService.listarEmprestimos();

                verify(entityManager).createQuery(
                                "SELECT e FROM Emprestimo e WHERE e.status = 'ATIVO' OR e.status = 'RENOVADO' OR e.status = 'ATRASADO'",
                                Emprestimo.class);
        }

        // =====================================================================
        // listarMultas
        // =====================================================================

        @Test
        @DisplayName("Deve exibir mensagem quando não houver multas registradas")
        void deveExibirMensagemQuandoNaoHouverMultasRegistradas() {
                when(entityManager.createQuery("SELECT m FROM Multa m ORDER BY m.id", Multa.class))
                                .thenReturn(queryDeMulta);
                when(queryDeMulta.getResultList()).thenReturn(Collections.emptyList());

                emprestimoService.listarMultas();

                verify(queryDeMulta).getResultList();
        }

        @Test
        @DisplayName("Deve listar multas quando houver registros")
        void deveListarMultasQuandoHouverRegistros() {
                Multa multaPendente = Multa.builder()
                                .id(1L)
                                .emprestimoId(10L)
                                .valor(4.0)
                                .diasAtraso(2)
                                .quitada(false)
                                .build();

                Multa multaQuitada = Multa.builder()
                                .id(2L)
                                .emprestimoId(20L)
                                .valor(8.0)
                                .diasAtraso(4)
                                .quitada(true)
                                .build();

                when(entityManager.createQuery("SELECT m FROM Multa m ORDER BY m.id", Multa.class))
                                .thenReturn(queryDeMulta);
                when(queryDeMulta.getResultList()).thenReturn(List.of(multaPendente, multaQuitada));

                emprestimoService.listarMultas();

                verify(queryDeMulta).getResultList();
        }
}