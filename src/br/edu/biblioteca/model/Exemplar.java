package br.edu.biblioteca.model;

import br.edu.biblioteca.service.GeradorIdService;
import br.edu.biblioteca.service.StatusExemplar;

import static br.edu.biblioteca.service.StatusExemplar.DISPONIVEL;

public class Exemplar {
    private Long id;
    private String isbnLivro;
    private StatusExemplar status;

    public Exemplar() {
    }

    public Exemplar(String isbnLivro) {
        this.id = GeradorIdService.exemplarId();
        this.isbnLivro = isbnLivro;
        this.status = DISPONIVEL;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIsbnLivro() {
        return isbnLivro;
    }

    public void setIsbnLivro(String isbnLivro) {
        this.isbnLivro = isbnLivro;
    }

    public StatusExemplar getStatus() {
        return status;
    }

    public void setStatus(StatusExemplar status) {
        this.status = status;
    }
}
