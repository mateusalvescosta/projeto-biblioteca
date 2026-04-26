package br.unisales.database.table;

import br.unisales.database.table.primary_key.LivroCategoriaId;
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

// Entidade de associação entre Livro e Categoria, representando a tabela livro_categoria
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "livro_categoria")
public class LivroCategoria {

    // Chave primária composta formada pelo ISBN do livro e o ID da categoria
    @EmbeddedId
    @Column(name = "id", nullable = false)
    private LivroCategoriaId id;

    // Livro vinculado a esta associação, carregado sob demanda
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("livroIsbn")
    @JoinColumn(name = "livro_isbn")
    private Livro livro;

    // Categoria vinculada a esta associação, carregada sob demanda
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("categoriaId")
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    // Indica se a associação entre o livro e a categoria está ativa
    @Column(name = "ativo", nullable = false)
    private Boolean ativo;

    // Define a associação como ativa automaticamente ao persistir
    @PrePersist
    public void prePersist() {
        this.ativo = Boolean.TRUE;
    }
}