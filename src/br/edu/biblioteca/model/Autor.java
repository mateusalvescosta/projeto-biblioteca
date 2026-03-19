package br.edu.biblioteca.model;

import br.edu.biblioteca.service.GeradorIdService;

public class Autor {
    private Long id;
    private String nome;

    public Autor() {
    }

    public Autor(String nome) {
        this.id = GeradorIdService.autorId();
        this.nome = nome;
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

    @Override
    public String toString() {
        return nome;
    }
}
