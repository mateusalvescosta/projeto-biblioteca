package br.unisales.database.table;

import java.time.LocalDateTime;

import br.unisales.Enumeration.StatusEmprestimoEnum;
import br.unisales.service.DataService;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "emprestimo")
public class Emprestimo {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;
    
    @Column(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @ManyToOne
    @JoinColumn(name = "exemplar_id", nullable = false)
    private Exemplar exemplar;
    
    @ManyToOne
    @JoinColumn(name = "data_emprestimo", nullable = false)
    private LocalDateTime dataEmprestimo;
    
    @Column(name = "data_devolucao_prevista", nullable = false)
    private LocalDateTime dataDevolucaoPrevista;
    
    @Column(name = "data_devolucao")
    private LocalDateTime dataDevolucao;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusEmprestimoEnum status;

    @PrePersist
    public void prePersist() {
            this.dataEmprestimo = LocalDateTime.parse(DataService.data());
            this.dataDevolucaoPrevista = this.dataEmprestimo.plusDays(15);
            this.status = StatusEmprestimoEnum.ATIVO;
    }
}
