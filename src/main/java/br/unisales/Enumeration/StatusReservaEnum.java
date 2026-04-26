package br.unisales.Enumeration;

// Enum que representa os possíveis status de uma reserva
public enum StatusReservaEnum {

    // Reserva ativa aguardando atendimento na fila
    RESERVADO,

    // Reserva cancelada pelo usuário ou pelo sistema
    CANCELADO,

    // Reserva atendida após o empréstimo do exemplar ao usuário
    ATENDIDA;
}