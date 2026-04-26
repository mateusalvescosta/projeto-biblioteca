package br.unisales.database.table;

import java.time.LocalDateTime;

import br.unisales.Enumeration.StatusReservaEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Entidade que representa a reserva de um livro feita por um usuário no banco de dados
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "reserva")
public class Reserva {

    // Identificador único da reserva
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    // ID do usuário que realizou a reserva
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    // ISBN do livro reservado
    @Column(name = "isbn_livro", nullable = false)
    private String isbnLivro;

    // Data e hora em que a reserva foi realizada
    @Column(name = "data_reserva", nullable = false)
    private LocalDateTime dataReserva;

    // Status atual da reserva: RESERVADO, ATENDIDA ou CANCELADO
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusReservaEnum status;

    // Define a data de criação como o momento atual e o status como RESERVADO ao persistir
    @PrePersist
    public void prePersist() {
        this.dataReserva = LocalDateTime.now();
        this.status = StatusReservaEnum.RESERVADO;
    }
}