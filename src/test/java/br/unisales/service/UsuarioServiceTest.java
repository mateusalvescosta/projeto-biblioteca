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

import br.unisales.database.table.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock
    private EntityManagerFactory entityManagerFactory;

    @Mock
    private EntityManager entityManager;

    @Mock
    private EntityTransaction entityTransaction;

    @Mock
    private TypedQuery<Usuario> queryDeUsuario;

    @Mock
    private TypedQuery<Long> queryDeMaximoId;

    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);
        lenient().when(entityManager.getTransaction()).thenReturn(entityTransaction);
        usuarioService = new UsuarioService(entityManagerFactory);
    }

    // =====================================================================
    // cadastrarUsuario
    // =====================================================================

    @Test
    @DisplayName("Deve persistir usuário com ID gerado e status não bloqueado ao cadastrar")
    void devePersistirUsuarioComIdGeradoEStatusNaoBloqueadoAoCadastrar() {
        Usuario novoUsuario = Usuario.builder().nome("João Silva").email("joao@email.com").build();

        when(entityManager.createQuery("SELECT MAX(u.id) FROM Usuario u", Long.class))
                .thenReturn(queryDeMaximoId);
        when(queryDeMaximoId.getSingleResult()).thenReturn(8L);

        usuarioService.cadastrarUsuario(novoUsuario);

        verify(entityManager).persist(novoUsuario);
        assertEquals(9L, novoUsuario.getId());
        assertEquals(Boolean.FALSE, novoUsuario.getBloqueado());
    }

    // =====================================================================
    // bloquearDesbloquearUsuario
    // =====================================================================

    @Test
    @DisplayName("Não deve alterar status quando usuário não existe no banco")
    void naoDeveAlterarStatusQuandoUsuarioNaoExisteNoBanco() {
        when(entityManager.find(Usuario.class, 10L)).thenReturn(null);

        usuarioService.bloquearDesbloquearUsuario(10L);

        verify(entityManager, never()).merge(any());
    }

    @Test
    @DisplayName("Deve bloquear usuário quando ele está desbloqueado")
    void deveBloquearUsuarioQuandoEleEstaDesbloqueado() {
        Usuario usuarioDesbloqueado = Usuario.builder()
                .id(10L)
                .nome("Maria")
                .bloqueado(Boolean.FALSE)
                .build();

        when(entityManager.find(Usuario.class, 10L)).thenReturn(usuarioDesbloqueado);
        when(entityManager.merge(usuarioDesbloqueado)).thenReturn(usuarioDesbloqueado);

        usuarioService.bloquearDesbloquearUsuario(10L);

        assertEquals(Boolean.TRUE, usuarioDesbloqueado.getBloqueado());
        verify(entityManager).merge(usuarioDesbloqueado);
    }

    @Test
    @DisplayName("Deve desbloquear usuário quando ele está bloqueado")
    void deveDesbloquearUsuarioQuandoEleEstaBloqueado() {
        Usuario usuarioBloqueado = Usuario.builder()
                .id(10L)
                .nome("Carlos")
                .bloqueado(Boolean.TRUE)
                .build();

        when(entityManager.find(Usuario.class, 10L)).thenReturn(usuarioBloqueado);
        when(entityManager.merge(usuarioBloqueado)).thenReturn(usuarioBloqueado);

        usuarioService.bloquearDesbloquearUsuario(10L);

        assertEquals(Boolean.FALSE, usuarioBloqueado.getBloqueado());
        verify(entityManager).merge(usuarioBloqueado);
    }

    // =====================================================================
    // listarUsuarios
    // =====================================================================

    @Test
    @DisplayName("Deve retornar todos os usuários quando existem registros no banco")
    void deveRetornarTodosOsUsuariosQuandoExistemRegistrosNoBanco() {
        Usuario primeiroUsuario = Usuario.builder().id(1L).nome("Ana").build();
        Usuario segundoUsuario = Usuario.builder().id(2L).nome("Bruno").build();
        Usuario terceiroUsuario = Usuario.builder().id(3L).nome("Carla").build();

        when(entityManager.createQuery("SELECT u FROM Usuario u ORDER BY u.id", Usuario.class))
                .thenReturn(queryDeUsuario);
        when(queryDeUsuario.getResultList())
                .thenReturn(List.of(primeiroUsuario, segundoUsuario, terceiroUsuario));

        List<Usuario> usuariosRetornados = usuarioService.listarUsuarios();

        assertNotNull(usuariosRetornados);
        assertEquals(3, usuariosRetornados.size());
        assertEquals("Ana", usuariosRetornados.get(0).getNome());
        assertEquals("Bruno", usuariosRetornados.get(1).getNome());
        assertEquals("Carla", usuariosRetornados.get(2).getNome());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há usuários cadastrados no banco")
    void deveRetornarListaVaziaQuandoNaoHaUsuariosCadastradosNoBanco() {
        when(entityManager.createQuery("SELECT u FROM Usuario u ORDER BY u.id", Usuario.class))
                .thenReturn(queryDeUsuario);
        when(queryDeUsuario.getResultList()).thenReturn(Collections.emptyList());

        List<Usuario> usuariosRetornados = usuarioService.listarUsuarios();

        assertTrue(usuariosRetornados.isEmpty());
    }

    // =====================================================================
    // buscarUsuarioPorId
    // =====================================================================

    @Test
    @DisplayName("Deve retornar usuário quando ID corresponde a um registro existente")
    void deveRetornarUsuarioQuandoIdCorrespondeAUmRegistroExistente() {
        Usuario usuarioExistente = Usuario.builder()
                .id(5L)
                .nome("Pedro")
                .email("pedro@email.com")
                .build();

        when(entityManager.find(Usuario.class, 5L)).thenReturn(usuarioExistente);

        Usuario usuarioRetornado = usuarioService.buscarUsuarioPorId(5L);

        assertNotNull(usuarioRetornado);
        assertEquals(5L, usuarioRetornado.getId());
        assertEquals("Pedro", usuarioRetornado.getNome());
    }

    @Test
    @DisplayName("Deve retornar null quando ID não corresponde a nenhum registro")
    void deveRetornarNullQuandoIdNaoCorrespondeANenhumRegistro() {
        when(entityManager.find(Usuario.class, 99L)).thenReturn(null);

        Usuario usuarioRetornado = usuarioService.buscarUsuarioPorId(99L);

        assertNull(usuarioRetornado);
    }
}