package br.unisales.database.table;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Entidade que representa uma notificação enviada a um usuário no banco de dados
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "notificacao")
public class Notificacao {

    // Identificador único da notificação
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    // ID do usuário destinatário da notificação
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    // Conteúdo da mensagem enviada ao usuário
    @Column(name = "mensagem", nullable = false, length = 500)
    private String mensagem;

    // Data e hora em que a notificação foi criada
    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    // Indica se a notificação já foi lida pelo usuário
    @Column(name = "lida", nullable = false)
    private Boolean lida;

    // Define a data de criação como o momento atual e a notificação como não lida ao persistir
    @PrePersist
    public void prePersist() {
        this.dataCriacao = LocalDateTime.now();
        this.lida = Boolean.FALSE;
    }
}