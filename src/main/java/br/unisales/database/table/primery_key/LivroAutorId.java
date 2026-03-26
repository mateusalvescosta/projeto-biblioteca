package br.unisales.database.table.primery_key;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LivroAutorId implements Serializable {
    @Column(name = "livro_isbn", length = 20)
    private String livroIsbn;
    
    @Column(name = "autor_id")
    private Long autorId;
}
