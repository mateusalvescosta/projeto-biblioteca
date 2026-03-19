package br.edu.biblioteca.model;

import br.edu.biblioteca.service.DataService;
import br.edu.biblioteca.service.GeradorIdService;

import java.time.LocalDateTime;

public class Notificacao {
    private Long id;
    private Long usuarioId;
    private String mensagem;
    private LocalDateTime data;
    private boolean lida;

    public Notificacao() {
    }

    public Notificacao(Long usuarioId, String mensagem) {
        this.id = GeradorIdService.notificacaoId();
        this.usuarioId = usuarioId;
        this.mensagem = mensagem;
        this.data = LocalDateTime.parse(DataService.data());
        this.lida = false;
    }
}
