package br.edu.biblioteca.model;

import br.edu.biblioteca.service.GeradorIdService;

public class Multa {
    private Long id;
    private Long emprestimoId;
    private Double valor;
    private boolean quitada;

    public Multa() {
    }

    public Multa(Long emprestimoId, Double valor) {
        this.quitada = false;
        this.valor = valor;
        this.emprestimoId = emprestimoId;
        this.id = GeradorIdService.multaId();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmprestimoId() {
        return emprestimoId;
    }

    public void setEmprestimoId(Long emprestimoId) {
        this.emprestimoId = emprestimoId;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public boolean isQuitada() {
        return quitada;
    }

    public void setQuitada(boolean quitada) {
        this.quitada = quitada;
    }
}
