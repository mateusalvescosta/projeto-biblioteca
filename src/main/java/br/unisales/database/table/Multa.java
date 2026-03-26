package br.unisales.database.table;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "multa")
public class Multa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    
    @Column(name = "emprestimo_id", nullable = false)
    private Long emprestimoId;

    @Column(name = "valor", nullable = false)
    private Double valor;

    @Column(name = "dias_atraso", nullable = false)
    private int diasAtraso;

    @Column(name = "quitada", nullable = false)
    private Boolean quitada;

    @PrePersist
    public void prePersist() {
            this.quitada = Boolean.FALSE;
    }
}
