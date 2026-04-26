package br.unisales.database.table;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Entidade que representa uma multa gerada por atraso na devolução de um empréstimo
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "multa")
public class Multa {

    // Identificador único da multa
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    // ID do empréstimo ao qual esta multa está vinculada
    @Column(name = "emprestimo_id", nullable = false)
    private Long emprestimoId;

    // Valor total da multa em reais, calculado com base nos dias de atraso
    @Column(name = "valor", nullable = false)
    private Double valor;

    // Quantidade de dias em atraso que originou a multa
    @Column(name = "dias_atraso", nullable = false)
    private int diasAtraso;

    // Indica se a multa já foi quitada pelo usuário
    @Column(name = "quitada", nullable = false)
    private Boolean quitada;

    // Define a multa como não quitada automaticamente ao persistir
    @PrePersist
    public void prePersist() {
        this.quitada = Boolean.FALSE;
    }
}