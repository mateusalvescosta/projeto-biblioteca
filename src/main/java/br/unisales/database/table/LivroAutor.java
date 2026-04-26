package br.unisales.database.table;

import br.unisales.database.table.primary_key.LivroAutorId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Entidade de associação entre Livro e Autor, representando a tabela livro_autor
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "livro_autor")
public class LivroAutor {

    // Chave primária composta formada pelo ISBN do livro e o ID do autor
    @EmbeddedId
    @Column(name = "id", nullable = false)
    private LivroAutorId id;

    // Livro vinculado a esta associação, carregado sob demanda
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("livroIsbn")
    @JoinColumn(name = "livro_isbn")
    private Livro livro;

    // Autor vinculado a esta associação, carregado sob demanda
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("autorId")
    @JoinColumn(name = "autor_id")
    private Autor autor;

    // Indica se a associação entre o livro e o autor está ativa
    @Column(name = "ativo", nullable = false)
    private Boolean ativo;

    // Define a associação como ativa automaticamente ao persistir
    @PrePersist
    public void prePersist() {
        this.ativo = Boolean.TRUE;
    }
}