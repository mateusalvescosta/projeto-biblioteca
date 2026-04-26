package br.unisales.database.table;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Entidade que representa um autor no banco de dados
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "autor")
public class Autor {

    // Identificador único do autor
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    // Nome completo do autor, deve ser único na tabela
    @Column(name = "nome", nullable = false, length = 100, unique = true)
    private String nome;
}