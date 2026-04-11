package br.unisales.service;

import br.unisales.database.table.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.util.List;

public class UsuarioService {

    private final EntityManagerFactory entityManagerFactory;

    public UsuarioService(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    /**
     * Cadastra um novo usuário no sistema.
     * O ID é gerado automaticamente com base no maior ID existente + 1.
     * O campo 'bloqueado' é inicializado como false.
     *
     * @param usuario Objeto Usuario a ser persistido (sem ID definido).
     */
    public void cadastrarUsuario(Usuario usuario) {
        usuario.setId(this.getNextId() + 1);
        usuario.setBloqueado(Boolean.FALSE);

        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();
            entityManager.persist(usuario);
            transaction.commit();
            System.out.println("Usuário cadastrado com sucesso.");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            Throwable causa = e;
            while (causa.getCause() != null) {
                causa = causa.getCause();
            }

            System.out.println("Erro ao cadastrar usuário: " + causa.getMessage());
        } finally {
            entityManager.close();
        }
    }

    /**
     * Alterna o estado de bloqueio de um usuário.
     * Se estiver ativo, bloqueia. Se estiver bloqueado, desbloqueia.
     *
     * @param id ID do usuário a ser bloqueado ou desbloqueado.
     */
    public void bloquearDesbloquear(Long id) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            Usuario usuario = entityManager.find(Usuario.class, id);

            if (usuario == null) {
                System.out.println("Usuário não encontrado.");
                return;
            }

            boolean novoStatus = !Boolean.TRUE.equals(usuario.getBloqueado());
            usuario.setBloqueado(novoStatus);

            transaction.begin();
            entityManager.merge(usuario);
            transaction.commit();

            String acao = novoStatus ? "bloqueado" : "desbloqueado";
            System.out.println("Usuário " + acao + " com sucesso.");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.out.println("Erro ao alterar status do usuário: " + e.getMessage());
        } finally {
            entityManager.close();
        }
    }

    /**
     * Lista todos os usuários cadastrados, ordenados por ID.
     *
     * @return Lista de todos os usuários.
     */
    public List<Usuario> listar() {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        try {
            return entityManager
                    .createQuery("SELECT u FROM Usuario u ORDER BY u.id", Usuario.class)
                    .getResultList();
        } catch (Exception e) {
            System.out.println("Erro ao listar usuários: " + e.getMessage());
            return List.of();
        } finally {
            entityManager.close();
        }
    }

    /**
     * Busca um usuário pelo ID.
     *
     * @param id ID do usuário.
     * @return O usuário encontrado ou null se não existir.
     */
    public Usuario buscarPorId(Long id) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        try {
            return entityManager.find(Usuario.class, id);
        } catch (Exception e) {
            System.out.println("Erro ao buscar usuário por ID: " + e.getMessage());
            return null;
        } finally {
            entityManager.close();
        }
    }

    /**
     * Retorna o próximo ID disponível baseado no maior ID existente na tabela.
     */
    private Long getNextId() {
        EntityManager em = this.entityManagerFactory.createEntityManager();
        try {
            Long maxId = em.createQuery(
                    "SELECT MAX(u.id) FROM Usuario u",
                    Long.class
            ).getSingleResult();
            return maxId != null ? maxId : 0L;
        } catch (Exception e) {
            System.out.println("Erro ao buscar maior ID: " + e.getMessage());
            return 0L;
        } finally {
            em.close();
        }
    }
}