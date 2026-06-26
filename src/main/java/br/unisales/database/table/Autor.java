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

    /**
 * Sistema de Gerenciamento de Biblioteca
 *
 * Disciplina: Construção em Estruturas de Dados / Projeto e Qualidade
 *             em Engenharia de Software
 * Professor:  Vito Franzosi
 * Período:    Terceiro
 *
 * Grupo:
 *   - Arthur Yuji Mendes Suzuki
 *   - Carlos Eduardo Pisa Meireles
 *   - Felipe Souza de Jesus
 *   - Mateus Alves Costa
 */
}