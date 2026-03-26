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
public class LivroCategoriaId implements Serializable {
    @Column(name = "livro_isbn", length = 20)
    private String livroIsbn;
    
    @Column(name = "categoria_id")
    private Integer categoriaId;
}
