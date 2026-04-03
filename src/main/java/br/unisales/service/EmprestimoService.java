package br.unisales.service;
import br.unisales.Enumeration.StatusEmprestimoEnum;
import br.unisales.Enumeration.StatusExemplarEnum;
import br.unisales.database.table.Emprestimo;
import br.unisales.database.table.Exemplar;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

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
            
            Exemplar exemplar = entityManager.find(Exemplar.class, emprestimo.getExemplarId());
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
        } 
        
        catch (Exception e) {
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

    public void devolverExemplar(Long emprestimoId){
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
                System.out.println("Este empréstimo já foi encerrado.");
                return;   
            }

            Exemplar exemplar = entityManager.find(Exemplar.class, emprestimo.getExemplarId());
            if (exemplar == null) {
                transaction.rollback();
                System.out.println("Exemplar não encontrado.");
                return;
            }

            exemplar.setStatus(StatusExemplarEnum.DISPONIVEL);
            entityManager.merge(exemplar);
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



















}