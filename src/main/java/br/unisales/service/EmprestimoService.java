package br.unisales.service;

import br.unisales.Enumeration.StatusEmprestimoEnum;
import br.unisales.Enumeration.StatusExemplarEnum;
import br.unisales.Enumeration.StatusReservaEnum;
import br.unisales.database.table.Emprestimo;
import br.unisales.database.table.Exemplar;
import br.unisales.database.table.Multa;
import br.unisales.database.table.Reserva;
import br.unisales.database.table.Usuario;
import br.unisales.service.util.ServiceUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class EmprestimoService {

    private final EntityManagerFactory entityManagerFactory;

    public EmprestimoService(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    // Realiza o empréstimo de um exemplar para um usuário
    public void emprestarExemplar(Emprestimo emprestimo) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            // Busca o exemplar no banco e valida se existe
            Exemplar exemplar = entityManager.find(Exemplar.class, emprestimo.getExemplar().getId());
            if (exemplar == null) {
                transaction.rollback();
                System.out.println("Exemplar não encontrado.");
                return;
            }

            // Valida se o exemplar está disponível para empréstimo
            if (exemplar.getStatus() != StatusExemplarEnum.DISPONIVEL) {
                transaction.rollback();
                System.out.println("Exemplar não está disponível.");
                return;
            }

            // Busca o usuário no banco e valida se existe
            Usuario usuario = entityManager.find(Usuario.class, emprestimo.getUsuario().getId());
            if (usuario == null) {
                transaction.rollback();
                System.out.println("Usuário não encontrado.");
                return;
            }

            // Valida se o usuário não está bloqueado
            if (Boolean.TRUE.equals(usuario.getBloqueado())) {
                transaction.rollback();
                System.out.println("Não é possível emprestar: usuário está bloqueado.");
                return;
            }

            // Busca a fila de reservas ativas para o livro, ordenada por data
            List<Reserva> fila = entityManager.createQuery(
                    "SELECT r FROM Reserva r WHERE r.isbnLivro = :isbn AND r.status = 'RESERVADO' ORDER BY r.dataReserva ASC",
                    Reserva.class)
                    .setParameter("isbn", exemplar.getLivro().getIsbn())
                    .setMaxResults(1)
                    .getResultList();

            // Valida se o usuário é o próximo da fila de reservas
            if (!fila.isEmpty()) {
                Reserva proxima = fila.get(0);
                if (!proxima.getUsuarioId().equals(emprestimo.getUsuario().getId())) {
                    transaction.rollback();
                    Usuario proximoUsuario = entityManager.find(Usuario.class, proxima.getUsuarioId());
                    System.out.println("Não é possível emprestar: existe uma fila de reserva para este livro.");
                    System.out.println("O próximo a ser atendido é:");
                    System.out.println(
                            "  Nome:  " + (proximoUsuario != null ? proximoUsuario.getNome() : "Não encontrado"));
                    System.out.println(
                            "  Email: " + (proximoUsuario != null ? proximoUsuario.getEmail() : "Não encontrado"));
                    System.out.println("Entre em contato com o usuário acima antes de realizar o empréstimo.");
                    return;
                }
            }

            // Busca reservas ativas do usuário para este livro
            List<Reserva> reservas = entityManager.createQuery(
                    "SELECT r FROM Reserva r WHERE r.isbnLivro = :isbn AND r.usuarioId = :usuarioId AND r.status = 'RESERVADO'",
                    Reserva.class)
                    .setParameter("isbn", exemplar.getLivro().getIsbn())
                    .setParameter("usuarioId", emprestimo.getUsuario().getId())
                    .getResultList();

            // Persiste o empréstimo e atualiza o status do exemplar
            emprestimo.setId(ServiceUtil.getNextId(this.entityManagerFactory, "SELECT MAX(e.id) FROM Emprestimo e"));
            emprestimo.setUsuario(usuario);
            emprestimo.setExemplar(exemplar);
            exemplar.setStatus(StatusExemplarEnum.EMPRESTADO);
            entityManager.merge(exemplar);
            entityManager.persist(emprestimo);

            // Marca a reserva como atendida, preservando o histórico
            if (!reservas.isEmpty()) {
                Reserva reserva = entityManager.merge(reservas.get(0));
                reserva.setStatus(StatusReservaEnum.ATENDIDA);
                entityManager.merge(reserva);
                System.out.println("Reserva atendida automaticamente.");
            }

            transaction.commit();
            System.out.println("Empréstimo cadastrado com sucesso.");

        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.out.println("Erro ao cadastrar empréstimo: " + ServiceUtil.extrairMensagemErro(e));
        } finally {
            entityManager.close();
        }
    }

    // Registra a devolução de um exemplar emprestado
    public void devolverExemplar(Long emprestimoId) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            // Busca o empréstimo pelo ID e valida se existe
            Emprestimo emprestimo = entityManager.find(Emprestimo.class, emprestimoId);
            if (emprestimo == null) {
                transaction.rollback();
                System.out.println("Empréstimo não encontrado.");
                return;
            }

            // Valida se o empréstimo ainda está ativo ou renovado
            if (emprestimo.getStatus() != StatusEmprestimoEnum.ATIVO &&
                    emprestimo.getStatus() != StatusEmprestimoEnum.RENOVADO) {
                transaction.rollback();
                System.out.println("Este empréstimo já foi encerrado.");
                return;
            }

            // Busca o exemplar vinculado ao empréstimo
            Exemplar exemplar = entityManager.find(Exemplar.class, emprestimo.getExemplar().getId());
            if (exemplar == null) {
                transaction.rollback();
                System.out.println("Exemplar não encontrado.");
                return;
            }

            // Atualiza o status do exemplar e registra a devolução
            exemplar.setStatus(StatusExemplarEnum.DISPONIVEL);
            entityManager.merge(exemplar);
            emprestimo.setDataDevolucao(LocalDateTime.now());
            emprestimo.setStatus(StatusEmprestimoEnum.DEVOLVIDO);
            entityManager.merge(emprestimo);
            transaction.commit();
            System.out.println("Exemplar devolvido com sucesso.");

        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.out.println("Erro ao devolver exemplar: " + ServiceUtil.extrairMensagemErro(e));
        } finally {
            entityManager.close();
        }
    }

    // Renova um empréstimo ativo, estendendo o prazo em 7 dias
    public void renovarExemplar(Long emprestimoId) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            // Busca o empréstimo pelo ID e valida se existe
            Emprestimo emprestimo = entityManager.find(Emprestimo.class, emprestimoId);
            if (emprestimo == null) {
                transaction.rollback();
                System.out.println("Empréstimo não encontrado.");
                return;
            }

            // Valida se o empréstimo está ativo e ainda não foi renovado
            if (emprestimo.getStatus() != StatusEmprestimoEnum.ATIVO) {
                transaction.rollback();
                System.out.println("Não pode renovar, empréstimo não está ativo ou já foi renovado uma vez.");
                return;
            }

            // Estende o prazo de devolução e atualiza o status
            emprestimo.setDataDevolucaoPrevista(
                    emprestimo.getDataDevolucaoPrevista().plusDays(7));
            emprestimo.setStatus(StatusEmprestimoEnum.RENOVADO);
            entityManager.merge(emprestimo);
            transaction.commit();
            System.out.println("Renovação realizada com sucesso.");

        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.out.println("Erro ao renovar empréstimo: " + ServiceUtil.extrairMensagemErro(e));
        } finally {
            entityManager.close();
        }
    }

    // Calcula e registra a multa de um empréstimo devolvido com atraso
    public double calcularMulta(Long emprestimoId) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            // Busca o empréstimo pelo ID e valida se existe
            Emprestimo emprestimo = entityManager.find(Emprestimo.class, emprestimoId);
            if (emprestimo == null) {
                System.out.println("Empréstimo não encontrado.");
                return 0;
            }

            // Valida se o livro já foi devolvido
            if (emprestimo.getStatus() == StatusEmprestimoEnum.ATIVO ||
                    emprestimo.getStatus() == StatusEmprestimoEnum.RENOVADO) {
                System.out.println("Livro ainda não foi devolvido.");
                return 0;
            }

            // Valida se as datas necessárias para o cálculo estão preenchidas
            if (emprestimo.getDataDevolucao() == null) {
                System.out.println("Data de devolução não registrada.");
                return 0;
            }

            if (emprestimo.getDataDevolucaoPrevista() == null) {
                System.out.println("Data de devolução prevista não registrada.");
                return 0;
            }

            // Calcula a multa caso a devolução tenha ocorrido após o prazo
            if (emprestimo.getDataDevolucao().isAfter(emprestimo.getDataDevolucaoPrevista())) {
                long dias = ChronoUnit.DAYS.between(
                        emprestimo.getDataDevolucaoPrevista(),
                        emprestimo.getDataDevolucao());
                double multa = dias * 2.0;

                // Verifica se já existe multa registrada para esse empréstimo
                List<Multa> multasExistentes = entityManager.createQuery(
                        "SELECT m FROM Multa m WHERE m.emprestimoId = :emprestimoId",
                        Multa.class)
                        .setParameter("emprestimoId", emprestimoId)
                        .getResultList();

                // Persiste a multa caso ainda não tenha sido registrada
                if (multasExistentes.isEmpty()) {
                    Long maxId = entityManager.createQuery(
                            "SELECT MAX(m.id) FROM Multa m", Long.class).getSingleResult();
                    Long novoId = maxId != null ? maxId + 1 : 1L;

                    Multa novaMulta = Multa.builder()
                            .id(novoId)
                            .emprestimoId(emprestimoId)
                            .valor(multa)
                            .diasAtraso((int) dias)
                            .build();

                    transaction.begin();
                    entityManager.persist(novaMulta);
                    transaction.commit();
                    System.out.println("Multa registrada no sistema.");
                } else {
                    System.out.println("Multa já registrada anteriormente.");
                }

                System.out.println("Multa: R$ " + multa + " (" + dias + " dias de atraso)");
                return multa;
            }

            System.out.println("Sem multa.");
            return 0;

        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.out.println("Erro ao calcular multa: " + ServiceUtil.extrairMensagemErro(e));
            return 0;
        } finally {
            entityManager.close();
        }
    }

    // Lista todos os empréstimos ativos, renovados ou atrasados
    public void listarEmprestimos() {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();

        try {
            // Busca empréstimos com status ativo, renovado ou atrasado
            List<Emprestimo> emprestimos = entityManager
                    .createQuery(
                            "SELECT e FROM Emprestimo e WHERE e.status = 'ATIVO' OR e.status = 'RENOVADO' OR e.status = 'ATRASADO'",
                            Emprestimo.class)
                    .getResultList();

            if (emprestimos.isEmpty()) {
                System.out.println("Nenhum empréstimo ativo.");
                return;
            }

            // Exibe os dados de cada empréstimo encontrado
            for (Emprestimo emprestimo : emprestimos) {
                System.out.println("-------------------------------------");
                System.out.println("ID:               " + emprestimo.getId());
                System.out.println("Usuário ID:       " + emprestimo.getUsuario().getId());
                System.out.println("Usuário:          " + emprestimo.getUsuario().getNome());
                System.out.println("Exemplar ID:      " + emprestimo.getExemplar().getId());
                System.out.println("Livro:            " + emprestimo.getExemplar().getLivro().getTitulo());
                System.out.println("Data Empréstimo:  " + emprestimo.getDataEmprestimo());
                System.out.println("Devolução Prev.:  " + emprestimo.getDataDevolucaoPrevista());
                System.out.println("Status:           " + emprestimo.getStatus());
            }
            System.out.println("-------------------------------------");

        } catch (Exception e) {
            System.out.println("Erro ao listar empréstimos: " + ServiceUtil.extrairMensagemErro(e));
        } finally {
            entityManager.close();
        }
    }

}