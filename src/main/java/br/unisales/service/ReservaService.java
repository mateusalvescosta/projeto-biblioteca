package br.unisales.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import br.unisales.Enumeration.StatusReservaEnum;
import br.unisales.database.table.Livro;
import br.unisales.database.table.Notificacao;
import br.unisales.database.table.Reserva;
import br.unisales.database.table.Usuario;
import br.unisales.service.util.ServiceUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

public class ReservaService {

    private final EntityManagerFactory entityManagerFactory;

    public ReservaService(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    // Realiza a reserva de um livro para um usuário
    public void reservarLivro(Reserva reserva) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            // Busca o usuário no banco e valida se existe
            Usuario usuario = entityManager.find(Usuario.class, reserva.getUsuarioId());
            if (usuario == null) {
                System.out.println("Usuário não encontrado.");
                return;
            }

            // Valida se o usuário não está bloqueado
            if (Boolean.TRUE.equals(usuario.getBloqueado())) {
                System.out.println("Não é possível reservar: usuário está bloqueado.");
                return;
            }

            // Valida se o usuário já possui empréstimo ativo desse livro
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

            // Valida se o usuário já possui reserva ativa para o mesmo livro
            Long reservasAtivas = entityManager.createQuery(
                    "SELECT COUNT(r) FROM Reserva r " +
                            "WHERE r.usuarioId = :usuarioId " +
                            "AND r.isbnLivro = :isbn " +
                            "AND r.status = 'RESERVADO'",
                    Long.class)
                    .setParameter("usuarioId", reserva.getUsuarioId())
                    .setParameter("isbn", reserva.getIsbnLivro())
                    .getSingleResult();

            if (reservasAtivas > 0) {
                System.out.println("Não é possível reservar: usuário já possui uma reserva ativa para este livro.");
                return;
            }

            // Gera o próximo ID e persiste a reserva no banco
            reserva.setId(ServiceUtil.getNextId(this.entityManagerFactory, "SELECT MAX(r.id) FROM Reserva r"));
            transaction.begin();
            entityManager.persist(reserva);
            transaction.commit();
            System.out.println("Livro reservado com sucesso.");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.out.println("Erro ao reservar livro: " + ServiceUtil.extrairMensagemErro(e));
        } finally {
            entityManager.close();
        }
    }

    // Cancela uma reserva ativa pelo ID
    public void cancelarReserva(Long id) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            // Busca a reserva pelo ID e valida se existe
            Reserva reserva = entityManager.find(Reserva.class, id);
            if (reserva == null) {
                transaction.rollback();
                System.out.println("Reserva não encontrada.");
                return;
            }

            // Valida se a reserva ainda está com status ativo
            if (reserva.getStatus() != StatusReservaEnum.RESERVADO) {
                transaction.rollback();
                System.out.println("Não é possível cancelar: reserva não está ativa.");
                return;
            }

            // Atualiza o status da reserva para cancelado
            reserva.setStatus(StatusReservaEnum.CANCELADO);
            entityManager.merge(reserva);
            transaction.commit();
            System.out.println("Reserva cancelada com sucesso.");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.out.println("Erro ao cancelar reserva: " + ServiceUtil.extrairMensagemErro(e));
        } finally {
            entityManager.close();
        }
    }

    // Exibe o próximo usuário na fila de reserva de um livro e envia notificação
    public void atenderProximaReserva(String isbn) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            // Busca a reserva mais antiga com status ativo para o ISBN informado
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

            // Busca o usuário e o livro vinculados à reserva
            Reserva proximaReserva = reservas.get(0);
            Usuario usuario = entityManager.find(Usuario.class, proximaReserva.getUsuarioId());
            Livro livro = entityManager.find(Livro.class, proximaReserva.getIsbnLivro());

            // Exibe os dados do próximo da fila
            System.out.println("-------------------------------------");
            System.out.println("Próximo da fila:");
            System.out.println("ID:              " + proximaReserva.getId());
            System.out.println("Usuário:         " + (usuario != null ? usuario.getNome() : "Não encontrado"));
            System.out.println("E-mail:          " + (usuario != null ? usuario.getEmail() : "Não encontrado"));
            System.out.println("Livro:           " + (livro != null ? livro.getTitulo() : "Não encontrado"));
            System.out.println("Data da reserva: " + proximaReserva.getDataReserva());
            System.out.println("-------------------------------------");

            // Cria e persiste uma notificação para o usuário caso os dados estejam disponíveis
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

                // Exibe os dados da notificação enviada
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
            System.out.println("Erro ao atender próxima reserva: " + ServiceUtil.extrairMensagemErro(e));
        } finally {
            entityManager.close();
        }
    }

    // Busca e exibe reservas filtrando pelo título do livro
    public void buscarReservaPorTituloLivro(String titulo) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        try {
            // Busca reservas cujo livro contenha o título informado
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

            // Exibe os dados de cada reserva encontrada
            for (Reserva reserva : reservas) {
                Usuario usuario = entityManager.find(Usuario.class, reserva.getUsuarioId());
                Livro livro = entityManager.find(Livro.class, reserva.getIsbnLivro());

                System.out.println("-------------------------------------");
                System.out.println("ID:      " + reserva.getId());
                System.out.println("Usuário: " + (usuario != null ? usuario.getNome() : "Não encontrado"));
                System.out.println("Livro:   " + (livro != null ? livro.getTitulo() : "Não encontrado"));
                System.out.println("Data:    " + reserva.getDataReserva());
                System.out.println("Status:  " + reserva.getStatus());
            }
            System.out.println("-------------------------------------");
        } catch (Exception e) {
            System.out.println("Erro ao buscar reservas: " + ServiceUtil.extrairMensagemErro(e));
        } finally {
            entityManager.close();
        }
    }

    // Retorna o próximo ID disponível para notificação usando o EntityManager já aberto
    private Long getNextNotificacaoId(EntityManager entityManager) {
        Long maxId = entityManager.createQuery(
                "SELECT MAX(n.id) FROM Notificacao n", Long.class).getSingleResult();
        return maxId != null ? maxId + 1 : 1L;
    }
}