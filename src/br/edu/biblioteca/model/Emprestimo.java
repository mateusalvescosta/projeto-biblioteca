package br.edu.biblioteca.model;

import br.edu.biblioteca.service.DataService;
import br.edu.biblioteca.service.GeradorIdService;
import br.edu.biblioteca.service.StatusEmprestimo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static br.edu.biblioteca.service.StatusEmprestimo.ATIVO;

public class Emprestimo {
    private Long id;
    private Long usuarioId;
    private Long exemplarId;
    private LocalDateTime dataEmprestimo;
    private LocalDateTime dataPrevista;
    private LocalDateTime dataDevolucao;
    private StatusEmprestimo status;

    public Emprestimo() {
    }

    public Emprestimo(Long usuarioId, Long exemplarId) {
        this.id = GeradorIdService.emprestimoId();
        this.usuarioId = usuarioId;
        this.exemplarId = exemplarId;
        this.dataEmprestimo = LocalDateTime.parse(DataService.data());
        this.dataPrevista = this.dataEmprestimo.plusDays(15);
        this.status = ATIVO;
    }

    public StatusEmprestimo getStatus() {
        return status;
    }

    public void setStatus(StatusEmprestimo status) {
        this.status = status;
    }

    public LocalDateTime getDataDevolucao() {
        return dataDevolucao;
    }

    public void setDataDevolucao(LocalDateTime dataDevolucao) {
        this.dataDevolucao = dataDevolucao;
    }

    public LocalDateTime getDataPrevista() {
        return dataPrevista;
    }

    public void setDataPrevista(LocalDateTime dataPrevista) {
        this.dataPrevista = dataPrevista;
    }

    public LocalDateTime getDataEmprestimo() {
        return dataEmprestimo;
    }

    public void setDataEmprestimo(LocalDateTime dataEmprestimo) {
        this.dataEmprestimo = dataEmprestimo;
    }

    public Long getExemplarId() {
        return exemplarId;
    }

    public void setExemplarId(Long exemplarId) {
        this.exemplarId = exemplarId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
