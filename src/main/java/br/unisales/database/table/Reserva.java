package br.unisales.database.table;

import java.time.LocalDateTime;

import br.unisales.Enumeration.StatusExemplarEnum;
import br.unisales.service.DataService;
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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "reserva")
public class Reserva {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "isbn_livro", nullable = false)
    private String isbnLivro;

    @Column(name = "data_reserva", nullable = false)
    private LocalDateTime dataReserva;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusExemplarEnum status;

    @PrePersist
    public void prePersist() {
            this.dataReserva = LocalDateTime.parse(DataService.data());
            this.status = StatusExemplarEnum.RESERVADO;
    }

    public void adiciona(Livro livro){
        livro.setTotalemprestimos(livro.getTotalemprestimos()+1);
    }
    

}


