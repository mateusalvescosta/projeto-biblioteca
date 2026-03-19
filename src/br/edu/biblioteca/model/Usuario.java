package br.edu.biblioteca.model;

import br.edu.biblioteca.service.GeradorIdService;
import br.edu.biblioteca.service.TipoUsuario;

public class Usuario {
    private Long id;
    private String nome;
    private TipoUsuario tipo;
    private String email;

    public Usuario() {
    }

    public Usuario(String nome, TipoUsuario tipo, String email) {
        this.id = GeradorIdService.usuarioId();
        this.nome = nome;
        this.tipo = tipo;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public TipoUsuario getTipo() {
        return tipo;
    }

    public void setTipo(TipoUsuario tipo) {
        this.tipo = tipo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
