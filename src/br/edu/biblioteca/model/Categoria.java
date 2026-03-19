package br.edu.biblioteca.model;

import br.edu.biblioteca.service.GeradorIdService;

public class Categoria {
    private Long id;
    private String nome;

    public Categoria() {
    }

    public Categoria(String nome) {
        this.id = GeradorIdService.categoriaId();
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

