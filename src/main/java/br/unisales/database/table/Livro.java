package br.unisales.database.table;

import java.util.ArrayList;
import java.util.List;

import br.unisales.database.table.primery_key.LivroCategoriaId;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "livro")
public class Livro {
    @Id
    @Column(name = "isbn", nullable = false, length = 20)
    private String isbn;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "ano")
    private int ano;

    @Default
    @OneToMany(mappedBy = "livro", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LivroCategoria> livroCategorias = new ArrayList<>();

    /**
     * @apiNote Adiciona uma categoria ao livro para ser salva no banco de dados
     * @param categoria
     * @author Vito Rodrigues Franzosi
     * @Data Criação 19.03.2026
     */
    public void addCategoria(Categoria categoria) {
        LivroCategoria lc = new LivroCategoria();

        lc.setLivro(this);
        lc.setCategoria(categoria);
        lc.setId(new LivroCategoriaId(this.isbn, categoria.getId()));

        livroCategorias.add(lc);
        categoria.getLivroCategorias().add(lc);
    }

    /**
     * @apiNote Remove uma categoria do livro para ser excluída do banco de dados
     * @param categoria
     * @author Vito Rodrigues Franzosi
     * @Data Criação 19.03.2026
     */
    public void removeCategoria(Categoria categoria) {
        livroCategorias.removeIf(lc -> {
            boolean match = lc.getCategoria().equals(categoria);
            if (match) {
                lc.getCategoria().getLivroCategorias().remove(lc);
                lc.setLivro(null);
                lc.setCategoria(null);
            }
            return match;
        });
    }    
}