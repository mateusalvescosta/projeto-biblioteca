package br.unisales.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import br.unisales.database.table.Livro;
import br.unisales.database.table.Notificacao;
import br.unisales.database.table.Reserva;
import br.unisales.database.table.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

public class ReservaService {

    private final EntityManagerFactory entityManagerFactory;

    public ReservaService(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public void reservarLivro(Reserva reserva) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            // Verifica se o usuário está bloqueado
            Usuario usuario = entityManager.find(Usuario.class, reserva.getUsuarioId());
            if (usuario == null) {
                System.out.println("Usuário não encontrado.");
                return;
            }

            if (Boolean.TRUE.equals(usuario.getBloqueado())) {
                System.out.println("Não é possível reservar: usuário está bloqueado.");
                return;
            }

            // Verifica se o usuário já possui empréstimo ativo desse livro
            Long emprestimosAtivos = entityManager.createQuery(
                    "SELECT COUNT(e) FROM Emprestimo e " +
                            "WHERE e.usuario.id = :usuarioId " +
                            "AND e.exemplar.livro.isbn = :isbn " +
                            "AND (e.status = 'ATIVO' OR e.status = 'RENOVADO')",
                    Long.class)
                    .setParameter("usuarioId", reserva.getUsuarioId())
                    .setParameter("isbn", reserva.getIsbnLivro())
                    .getSingleResult();

            if (emprestimosAtivos > 0) {
                System.out.println("Não é possível reservar: usuário já possui um empréstimo ativo deste livro.");
                return;
            }

            transaction.begin();
            entityManager.persist(reserva);
            transaction.commit();
            System.out.println("Livro reservado com sucesso.");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            Throwable causa = e;
            while (causa.getCause() != null) {
                causa = causa.getCause();
            }
            System.out.println("Erro ao reservar livro: " + causa.getMessage());
        } finally {
            entityManager.close();
        }
    }

    /**
     * Cancela uma reserva pelo ID.
     */
    public void cancelarReserva(Long id) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();
            Reserva reserva = entityManager.find(Reserva.class, id);
            if (reserva == null) {
                transaction.rollback();
                System.out.println("Reserva não encontrada.");
                return;
            }
            entityManager.remove(reserva);
            transaction.commit();
            System.out.println("Reserva cancelada com sucesso.");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.out.println("Erro ao cancelar reserva: " + e.getMessage());
        } finally {
            entityManager.close();
        }
    }

    /**
     * Mostra o próximo usuário na fila de reserva para um livro pelo ISBN.
     */
    public void atenderProximaReserva(String isbn) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            List<Reserva> reservas = entityManager
                    .createQuery(
                            "SELECT r FROM Reserva r WHERE r.isbnLivro = :isbn AND r.status = 'RESERVADO' ORDER BY r.dataReserva ASC",
                            Reserva.class)
                    .setParameter("isbn", isbn)
                    .setMaxResults(1)
                    .getResultList();

            if (reservas.isEmpty()) {
                System.out.println("Nenhuma reserva pendente para o ISBN informado.");
                return;
            }

            Reserva proxima = reservas.get(0);
            Usuario usuario = entityManager.find(Usuario.class, proxima.getUsuarioId());
            Livro livro = entityManager.find(Livro.class, proxima.getIsbnLivro());

            System.out.println("-------------------------------------");
            System.out.println("Próximo da fila:");
            System.out.println("ID:              " + proxima.getId());
            System.out.println("Usuário:         " + (usuario != null ? usuario.getNome() : "Não encontrado"));
            System.out.println("E-mail:          " + (usuario != null ? usuario.getEmail() : "Não encontrado"));
            System.out.println("Livro:           " + (livro != null ? livro.getTitulo() : "Não encontrado"));
            System.out.println("Data da reserva: " + proxima.getDataReserva());
            System.out.println("-------------------------------------");

            if (usuario != null && livro != null) {
                LocalDateTime prazo = LocalDateTime.now().plusDays(2);
                String mensagem = "Olá, " + usuario.getNome() + "! O livro \"" + livro.getTitulo() +
                        "\" que você reservou está disponível. Por favor, retire-o até " +
                        prazo.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ".";

                Notificacao notificacao = Notificacao.builder()
                        .id(getNextNotificacaoId(entityManager))
                        .usuarioId(usuario.getId())
                        .mensagem(mensagem)
                        .build();

                transaction.begin();
                entityManager.persist(notificacao);
                transaction.commit();

                System.out.println("-------------------------------------");
                System.out.println("NOTIFICAÇÃO ENVIADA:");
                System.out.println("Para:     " + usuario.getNome());
                System.out.println("E-mail:   " + usuario.getEmail());
                System.out.println("Mensagem: " + mensagem);
                System.out.println(
                        "Data:     " + notificacao.getDataCriacao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                System.out.println("-------------------------------------");
            }

        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            Throwable causa = e;
            while (causa.getCause() != null) {
                causa = causa.getCause();
            }
            System.out.println("Erro ao atender próxima reserva: " + causa.getMessage());
        } finally {
            entityManager.close();
        }
    }

    /**
     * Busca reservas pelo título do livro
     */
    public void buscarPorTituloLivro(String titulo) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        try {
            List<Reserva> reservas = entityManager
                    .createQuery(
                            "SELECT r FROM Reserva r WHERE r.isbnLivro IN (" +
                                    "SELECT l.isbn FROM Livro l WHERE LOWER(l.titulo) LIKE LOWER(:titulo)) " +
                                    "ORDER BY r.dataReserva ASC",
                            Reserva.class)
                    .setParameter("titulo", "%" + titulo + "%")
                    .getResultList();

            if (reservas.isEmpty()) {
                System.out.println("Nenhuma reserva encontrada para o livro informado.");
                return;
            }

            for (Reserva r : reservas) {
                Usuario usuario = entityManager.find(Usuario.class, r.getUsuarioId());
                Livro livro = entityManager.find(Livro.class, r.getIsbnLivro());

                System.out.println("-------------------------------------");
                System.out.println("ID:      " + r.getId());
                System.out.println("Usuário: " + (usuario != null ? usuario.getNome() : "Não encontrado"));
                System.out.println("Livro:   " + (livro != null ? livro.getTitulo() : "Não encontrado"));
                System.out.println("Data:    " + r.getDataReserva());
                System.out.println("Status:  " + r.getStatus());
            }
            System.out.println("-------------------------------------");
        } catch (Exception e) {
            Throwable causa = e;
            while (causa.getCause() != null) {
                causa = causa.getCause();
            }
            System.out.println("Erro ao buscar reservas: " + causa.getMessage());
        } finally {
            entityManager.close();
        }
    }

    /**
     * Retorna o próximo ID disponível baseado no maior ID existente na tabela.
     */
    public Long getNextId() {
        EntityManager em = this.entityManagerFactory.createEntityManager();
        try {
            Long maxId = em.createQuery(
                    "SELECT MAX(r.id) FROM Reserva r",
                    Long.class).getSingleResult();
            return maxId != null ? maxId + 1 : 1L;
        } catch (Exception e) {
            System.out.println("Erro ao buscar maior ID: " + e.getMessage());
            return 1L;
        } finally {
            em.close();
        }
    }

    private Long getNextNotificacaoId(EntityManager entityManager) {
        Long maxId = entityManager.createQuery(
                "SELECT MAX(n.id) FROM Notificacao n", Long.class).getSingleResult();
        return maxId != null ? maxId + 1 : 1L;
    }
}
