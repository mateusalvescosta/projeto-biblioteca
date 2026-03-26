package br.unisales.database.table;

import br.unisales.Enumeration.UsuarioTipoEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
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
@Table(name = "usuario")
public class Usuario {
    @Id
    private Integer id;
    
    @Column(nullable = false, length = 100)
    private String nome;

    @Default
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private UsuarioTipoEnum tipo = UsuarioTipoEnum.ALUNO;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = false, length = 8)
    private String senha;
}