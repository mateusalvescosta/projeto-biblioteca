package br.unisales.Enumeration;

// Enum que representa os possíveis status de um empréstimo
public enum StatusEmprestimoEnum {

    // Empréstimo em andamento dentro do prazo
    ATIVO,

    // Exemplar devolvido pelo usuário
    DEVOLVIDO,

    // Empréstimo com prazo de devolução vencido
    ATRASADO,

    // Empréstimo com prazo estendido por mais 7 dias
    RENOVADO,

    // Empréstimo cancelado antes da devolução
    CANCELADO;
}