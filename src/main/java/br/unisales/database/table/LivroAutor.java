package br.unisales.database.table;

import br.unisales.database.table.primery_key.LivroAutorId;
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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "livro_autor")
public class LivroAutor {
    @EmbeddedId
    private LivroAutorId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("livroIsbn")
    @JoinColumn(name = "livro_isbn")
    private Livro livro;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("autorId")
    @JoinColumn(name = "autor_id")
    private Autor autor;
    
    
    @Column(name = "ativo", nullable = false)
    private Boolean ativo;

    @PrePersist
    public void prePersist() {
            this.ativo = Boolean.TRUE;
    }
}