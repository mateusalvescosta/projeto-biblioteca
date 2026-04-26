package br.unisales.service.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

public class ServiceUtil {

    // Retorna o próximo ID disponível com base no maior ID existente na tabela
    public static Long getNextId(EntityManagerFactory emf, String jpql) {
        EntityManager entityManager = emf.createEntityManager();
        try {
            // Executa a query recebida para buscar o maior ID atual
            Long maxId = entityManager.createQuery(jpql, Long.class).getSingleResult();
            return maxId != null ? maxId + 1 : 1L;
        } catch (Exception e) {
            System.out.println("Erro ao buscar maior ID: " + e.getMessage());
            return 1L;
        } finally {
            entityManager.close();
        }
    }

    // Percorre a cadeia de causas da exceção e retorna a mensagem da causa raiz
    public static String extrairMensagemErro(Exception e) {
        // Navega até a causa mais profunda da exceção
        Throwable causa = e;
        while (causa.getCause() != null) {
            causa = causa.getCause();
        }
        return causa.getMessage();
    }

}