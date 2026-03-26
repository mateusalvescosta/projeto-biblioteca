package br.unisales.database.table;



import java.time.LocalDateTime;

import br.unisales.service.DataService;
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
@Table(name = "notificacao")
public class Notificacao {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
            this.dataCriacao = LocalDateTime.parse(DataService.data());
            this.lida = Boolean.FALSE;
    }
}
