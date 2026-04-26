package br.unisales.database.table.primary_key;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Chave primária composta da tabela livro_categoria, formada pelo ISBN do livro e o ID da categoria
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LivroCategoriaId implements Serializable {

    // ISBN do livro que compõe a chave composta
    @Column(name = "livro_isbn", length = 20)
    private String livroIsbn;

    // ID da categoria que compõe a chave composta
    @Column(name = "categoria_id")
    private Long categoriaId;
}