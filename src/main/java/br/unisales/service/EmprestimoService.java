package br.unisales.service;

import br.unisales.Enumeration.StatusEmprestimoEnum;
import br.unisales.Enumeration.StatusExemplarEnum;
import br.unisales.database.table.Emprestimo;
import br.unisales.database.table.Exemplar;
import br.unisales.database.table.Reserva;
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

    public void emprestarExemplar(Emprestimo emprestimo) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            Exemplar exemplar = entityManager.find(Exemplar.class, emprestimo.getExemplar().getId());
            if (exemplar == null) {
                transaction.rollback();
                System.out.println("Exemplar não encontrado.");
                return;
            }

            if (exemplar.getStatus() != StatusExemplarEnum.DISPONIVEL) {
                transaction.rollback();
                System.out.println("Exemplar não está disponível.");
                return;
            }

            exemplar.setStatus(StatusExemplarEnum.EMPRESTADO);
            entityManager.merge(exemplar);
            entityManager.persist(emprestimo);
            transaction.commit();
            System.out.println("Empréstimo cadastrado com sucesso.");

            // verifica e remove reserva se existir para o livro e usuário
            List<Reserva> reservas = entityManager.createQuery(
                    "SELECT r FROM Reserva r WHERE r.isbnLivro = :isbn AND r.usuarioId = :usuarioId",
                    Reserva.class)
                    .setParameter("isbn", exemplar.getLivro().getIsbn())
                    .setParameter("usuarioId", emprestimo.getUsuario().getId())
                    .getResultList();

            if (!reservas.isEmpty()) {
                transaction.begin();
                entityManager.remove(entityManager.merge(reservas.get(0)));
                transaction.commit();
                System.out.println("Reserva removida automaticamente.");
            }

        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            Throwable causa = e;
            while (causa.getCause() != null) {
                causa = causa.getCause();
            }
            System.out.println("Erro ao cadastrar empréstimo: " + causa.getMessage());
        } finally {
            entityManager.close();
        }
    }

    public void devolverExemplar(Long emprestimoId) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            Emprestimo emprestimo = entityManager.find(Emprestimo.class, emprestimoId);
            if (emprestimo == null) {
                transaction.rollback();
                System.out.println("Empréstimo não encontrado.");
                return;
            }

            if (emprestimo.getStatus() != StatusEmprestimoEnum.ATIVO &&
                    emprestimo.getStatus() != StatusEmprestimoEnum.RENOVADO) {
                transaction.rollback();
                System.out.println("Este empréstimo já foi encerrado.");
                return;
            }

            Exemplar exemplar = entityManager.find(Exemplar.class, emprestimo.getExemplar().getId());
            if (exemplar == null) {
                transaction.rollback();
                System.out.println("Exemplar não encontrado.");
                return;
            }

            exemplar.setStatus(StatusExemplarEnum.DISPONIVEL);
            entityManager.merge(exemplar);
            emprestimo.setDataDevolucao(LocalDateTime.now());
            emprestimo.setStatus(StatusEmprestimoEnum.DEVOLVIDO);
            entityManager.merge(emprestimo);
            transaction.commit();
            System.out.println("Exemplar devolvido com sucesso.");
        }

        catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            Throwable causa = e;
            while (causa.getCause() != null) {
                causa = causa.getCause();
            }

            System.out.println("Erro ao devolver exemplar: " + causa.getMessage());
        } finally {
            entityManager.close();
        }
    }

    public void renovarExemplar(Long emprestimoId) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            Emprestimo emprestimo = entityManager.find(Emprestimo.class, emprestimoId);
            if (emprestimo == null) {
                transaction.rollback();
                System.out.println("Empréstimo não encontrado.");
                return;
            }

            if (emprestimo.getStatus() != StatusEmprestimoEnum.ATIVO) {
                transaction.rollback();
                System.out.println("Não pode renovar, empréstimo não está ativo.");
                return;
            }

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

            Throwable causa = e;
            while (causa.getCause() != null) {
                causa = causa.getCause();
            }

            System.out.println("Erro ao renovar empréstimo: " + causa.getMessage());
        } finally {
            entityManager.close();
        }
    }

    public double calcularMulta(Long emprestimoId) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();

        try {
            Emprestimo emprestimo = entityManager.find(Emprestimo.class, emprestimoId);
            if (emprestimo == null) {
                System.out.println("Empréstimo não encontrado.");
                return 0;
            }

            if (emprestimo.getStatus() == StatusEmprestimoEnum.ATIVO ||
                    emprestimo.getStatus() == StatusEmprestimoEnum.RENOVADO) {
                System.out.println("Livro ainda não foi devolvido.");
                return 0;
            }

            // Verificação adicionada
            if (emprestimo.getDataDevolucao() == null) {
                System.out.println("Data de devolução não registrada.");
                return 0;
            }

            if (emprestimo.getDataDevolucaoPrevista() == null) {
                System.out.println("Data de devolução prevista não registrada.");
                return 0;
            }

            if (emprestimo.getDataDevolucao().isAfter(emprestimo.getDataDevolucaoPrevista())) {
                long dias = ChronoUnit.DAYS.between(
                        emprestimo.getDataDevolucaoPrevista(),
                        emprestimo.getDataDevolucao());
                double multa = dias * 2.0;
                System.out.println("Multa: R$ " + multa);
                return multa;
            }

            System.out.println("Sem multa.");
            return 0;

        } finally {
            entityManager.close();
        }
    }

    public void listar() {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();

        try {
            List<Emprestimo> emprestimos = entityManager
                    .createQuery(
                            "SELECT e FROM Emprestimo e WHERE e.status = 'ATIVO' OR e.status = 'RENOVADO' OR e.status = 'ATRASADO'",
                            Emprestimo.class)
                    .getResultList();

            if (emprestimos.isEmpty()) {
                System.out.println("Nenhum empréstimo ativo.");
                return;
            }

            for (Emprestimo emp : emprestimos) {
                System.out.println("-------------------------------------");
                System.out.println("ID:               " + emp.getId());
                System.out.println("Usuário ID:       " + emp.getUsuario().getId());
                System.out.println("Usuário:          " + emp.getUsuario().getNome());
                System.out.println("Exemplar ID:      " + emp.getExemplar().getId());
                System.out.println("Data Empréstimo:  " + emp.getDataEmprestimo());
                System.out.println("Devolução Prev.:  " + emp.getDataDevolucaoPrevista());
                System.out.println("Status:           " + emp.getStatus());
            }
            System.out.println("-------------------------------------");

        } finally {
            entityManager.close();
        }
    }

}
