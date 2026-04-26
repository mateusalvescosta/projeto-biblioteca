package br.unisales.service;

import br.unisales.database.table.Usuario;
import br.unisales.service.util.ServiceUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.util.List;

public class UsuarioService {

    private final EntityManagerFactory entityManagerFactory;

    public UsuarioService(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    // Cadastra um novo usuário gerando o ID automaticamente e inicializando como não bloqueado
    public void cadastrarUsuario(Usuario usuario) {
        // Gera o próximo ID disponível e define o status inicial como não bloqueado
        usuario.setId(ServiceUtil.getNextId(this.entityManagerFactory, "SELECT MAX(u.id) FROM Usuario u"));
        usuario.setBloqueado(Boolean.FALSE);

        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            // Persiste o novo usuário no banco
            transaction.begin();
            entityManager.persist(usuario);
            transaction.commit();
            System.out.println("Usuário cadastrado com sucesso.");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.out.println("Erro ao cadastrar usuário: " + ServiceUtil.extrairMensagemErro(e));
        } finally {
            entityManager.close();
        }
    }

    // Alterna o status de bloqueio do usuário entre bloqueado e desbloqueado
    public void bloquearDesbloquearUsuario(Long id) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            // Busca o usuário pelo ID e valida se existe
            Usuario usuario = entityManager.find(Usuario.class, id);
            if (usuario == null) {
                System.out.println("Usuário não encontrado.");
                return;
            }

            // Inverte o status atual de bloqueio do usuário
            boolean novoStatus = !Boolean.TRUE.equals(usuario.getBloqueado());
            usuario.setBloqueado(novoStatus);

            // Persiste a alteração no banco
            transaction.begin();
            entityManager.merge(usuario);
            transaction.commit();

            String acaoBloquearDesbloquear = novoStatus ? "bloqueado" : "desbloqueado";
            System.out.println("Usuário " + acaoBloquearDesbloquear + " com sucesso.");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.out.println("Erro ao alterar status do usuário: " + ServiceUtil.extrairMensagemErro(e));
        } finally {
            entityManager.close();
        }
    }

    // Lista todos os usuários cadastrados ordenados por ID
    public List<Usuario> listarUsuarios() {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        try {
            // Busca todos os usuários ordenados pelo ID
            return entityManager
                    .createQuery("SELECT u FROM Usuario u ORDER BY u.id", Usuario.class)
                    .getResultList();
        } catch (Exception e) {
            System.out.println("Erro ao listar usuários: " + ServiceUtil.extrairMensagemErro(e));
            return List.of();
        } finally {
            entityManager.close();
        }
    }

    // Busca um usuário pelo ID
    public Usuario buscarUsuarioPorId(Long id) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        try {
            // Busca o usuário diretamente pelo ID
            return entityManager.find(Usuario.class, id);
        } catch (Exception e) {
            System.out.println("Erro ao buscar usuário por ID: " + ServiceUtil.extrairMensagemErro(e));
            return null;
        } finally {
            entityManager.close();
        }
    }
}