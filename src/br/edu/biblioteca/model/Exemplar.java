package br.edu.biblioteca.model;

import br.edu.biblioteca.service.StatusExemplar;

public class Exemplar {
    private Long id;
    private String isbnLivro;
    private StatusExemplar status;

    public Exemplar() {
    }

    public Exemplar(Long id, String isbnLivro, StatusExemplar status) {
        this.id = id;
        this.isbnLivro = isbnLivro;
        this.status = status;
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
