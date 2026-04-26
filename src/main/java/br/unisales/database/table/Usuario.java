package br.unisales.database.table;

import br.unisales.Enumeration.UsuarioTipoEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Entidade que representa um usuário do sistema no banco de dados
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "usuario")
public class Usuario {

    // Identificador único do usuário
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    // Nome completo do usuário
    @Column(nullable = false, length = 100)
    private String nome;

    // Endereço de e-mail do usuário, utilizado para contato e notificações
    @Column(nullable = false, length = 150)
    private String email;

    // Senha de acesso do usuário
    @Column(nullable = false, length = 8)
    private String senha;

    // Tipo do usuário: ALUNO, PROFESSOR ou SERVIDOR
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private UsuarioTipoEnum tipo;

    // Indica se o usuário está bloqueado e impedido de realizar empréstimos e reservas
    @Column(name = "bloqueado", nullable = false)
    private Boolean bloqueado;

    // Define o usuário como não bloqueado automaticamente ao persistir
    @PrePersist
    public void prePersist() {
        this.bloqueado = Boolean.FALSE;
    }
}