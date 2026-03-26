package br.unisales.database.table;

import br.unisales.database.table.primery_key.LivroCategoriaId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
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
@Table(name = "livro_categoria")
public class LivroCategoria {
    @EmbeddedId
    private LivroCategoriaId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("livroIsbn")
    @JoinColumn(name = "livro_isbn")
    private Livro livro;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("categoriaId")
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Builder.Default
    @Column(name = "ativo", nullable = false)
    private Boolean ativo = Boolean.TRUE;
}
