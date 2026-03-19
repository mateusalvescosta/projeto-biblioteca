package br.edu.biblioteca.model;

import br.edu.biblioteca.service.DataService;
import br.edu.biblioteca.service.GeradorIdService;
import br.edu.biblioteca.service.StatusExemplar;
import static br.edu.biblioteca.service.StatusExemplar.RESERVADO;
import java.time.LocalDateTime;

public class Reserva {
    private Long id;
    private Long usuarioId;
    private String isbnLivro;
    private LocalDateTime dataReserva;
    private StatusExemplar status;

    public Reserva(String isbnLivro, Long usuarioId){
        this.status = RESERVADO;
        this.dataReserva = LocalDateTime.parse(DataService.data());
        this.isbnLivro = isbnLivro;
        this.usuarioId = usuarioId;
        this.id = GeradorIdService.reservaId();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getIsbnLivro() {
        return isbnLivro;
    }

    public void setIsbnLivro(String isbnLivro) {
        this.isbnLivro = isbnLivro;
    }

    public LocalDateTime getDataReserva() {
        return dataReserva;
    }

    public void setDataReserva(LocalDateTime dataReserva) {
        this.dataReserva = dataReserva;
    }

    public StatusExemplar getStatus() {
        return status;
    }

    public void setStatus(StatusExemplar status) {
        this.status = status;
    }
}
