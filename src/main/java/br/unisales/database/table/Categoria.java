package br.unisales.database.table;

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

// Entidade que representa uma categoria de livros no banco de dados
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "categoria")
public class Categoria {

    // Identificador único da categoria
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    // Nome da categoria, deve ser único na tabela
    @Column(name = "nome", nullable = false, length = 100, unique = true)
    private String nome;

    // Lista de associações entre esta categoria e os livros vinculados a ela
    @Default
    @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LivroCategoria> livroCategorias = new ArrayList<>();
}