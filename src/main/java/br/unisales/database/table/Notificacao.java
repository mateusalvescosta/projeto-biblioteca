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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "notificacao")
public class Notificacao {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;
    
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;
    
    @Column(name = "mensagem", nullable = false, length = 500)
    private String mensagem;
    
    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;
    
    @Column(name = "lida", nullable = false)
    private Boolean lida;

    @PrePersist
    public void prePersist() {
            this.dataCriacao = LocalDateTime.now();
            this.lida = Boolean.FALSE;
    }
}
