package br.unisales.database.table;

import br.unisales.Enumeration.StatusExemplarEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Entidade que representa um exemplar físico de um livro no banco de dados
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "exemplar")
public class Exemplar {

    // Identificador único do exemplar
    @Id
    @Column(name = "id", nullable = false, length = 20)
    private Long id;

    // Livro ao qual este exemplar pertence, carregado sob demanda
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "isbn")
    private Livro livro;

    // Status atual do exemplar: DISPONIVEL ou EMPRESTADO
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusExemplarEnum status;

    // Define o status inicial como DISPONIVEL automaticamente ao persistir o exemplar
    @PrePersist
    public void prePersist() {
        this.status = StatusExemplarEnum.DISPONIVEL;
    }
}