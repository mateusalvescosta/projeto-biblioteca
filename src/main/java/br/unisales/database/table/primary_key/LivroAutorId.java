package br.unisales.database.table.primary_key;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Chave primária composta da tabela livro_autor, formada pelo ISBN do livro e o ID do autor
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LivroAutorId implements Serializable {

    // ISBN do livro que compõe a chave composta
    @Column(name = "livro_isbn", length = 20)
    private String livroIsbn;

    // ID do autor que compõe a chave composta
    @Column(name = "autor_id")
    private Long autorId;
}