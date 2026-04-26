package br.unisales.database.table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

// Entidade que representa um livro no banco de dados
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "livro")
public class Livro {

    // ISBN do livro, utilizado como chave primária
    @Id
    @Column(name = "isbn", nullable = false)
    private String isbn;

    // Título do livro
    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    // Ano de publicação do livro, pode ser nulo caso não informado
    @Column(name = "ano")
    private LocalDate ano;

    // Lista de associações entre este livro e suas categorias
    @Default
    @OneToMany(mappedBy = "livro", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LivroCategoria> livroCategorias = new ArrayList<>();

    // Lista de associações entre este livro e seus autores
    @Default
    @OneToMany(mappedBy = "livro", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LivroAutor> livroAutores = new ArrayList<>();
}