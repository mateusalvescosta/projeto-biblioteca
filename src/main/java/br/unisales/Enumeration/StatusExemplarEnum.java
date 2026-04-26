package br.unisales.Enumeration;

// Enum que representa os possíveis status de um exemplar físico
public enum StatusExemplarEnum {

    // Exemplar disponível para empréstimo
    DISPONIVEL,

    // Exemplar atualmente emprestado a um usuário
    EMPRESTADO,

    // Exemplar reservado por um usuário na fila de reservas
    RESERVADO;
}