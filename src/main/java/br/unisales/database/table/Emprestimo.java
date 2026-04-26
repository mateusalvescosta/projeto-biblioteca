package br.unisales.database.table;

import java.time.LocalDateTime;

import br.unisales.Enumeration.StatusEmprestimoEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Entidade que representa um empréstimo de exemplar para um usuário no banco de dados
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "emprestimo")
public class Emprestimo {

    // Identificador único do empréstimo
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    // Usuário que realizou o empréstimo
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // Exemplar físico que foi emprestado
    @ManyToOne
    @JoinColumn(name = "exemplar_id", nullable = false)
    private Exemplar exemplar;

    // Data e hora em que o empréstimo foi realizado
    @Column(name = "data_emprestimo", nullable = false)
    private LocalDateTime dataEmprestimo;

    // Data e hora prevista para a devolução do exemplar
    @Column(name = "data_devolucao_prevista", nullable = false)
    private LocalDateTime dataDevolucaoPrevista;

    // Data e hora em que o exemplar foi efetivamente devolvido, nulo enquanto ativo
    @Column(name = "data_devolucao")
    private LocalDateTime dataDevolucao;

    // Status atual do empréstimo: ATIVO, RENOVADO, DEVOLVIDO ou ATRASADO
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusEmprestimoEnum status;
}