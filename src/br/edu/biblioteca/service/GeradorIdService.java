package br.edu.biblioteca.service;

public class GeradorIdService {
    private static long autorId;
    private static long categoriaId;
    private static long exemplarId;
    private static long usuarioId;
    private static long emprestimoId;
    private static long reservaId;
    private static long multaId;
    private static long notificacaoId;

    public static long getAutorId() {
        return autorId;
    }

    public static long getCategoriaId() {
        return categoriaId;
    }

    public static long getExemplarId() {
        return exemplarId;
    }

    public static long getUsuarioId() {
        return usuarioId;
    }

    public static long getEmprestimoId() {
        return emprestimoId;
    }

    public static long getReservaId() {
        return reservaId;
    }

    public static long getMultaId() {
        return multaId;
    }

    public static long getNotificacaoId() {
        return notificacaoId;
    }

    public static long autorId(){
        return ++autorId;
    }

    public static long categoriaId(){
        return ++categoriaId;
    }

    public static long usuarioId(){
        return ++usuarioId;
    }

    public static long emprestimoId(){
        return ++emprestimoId;
    }

    public static long reservaId(){
        return ++reservaId;
    }

    public static long multaId(){
        return ++multaId;
    }

    public static long exemplarId(){
        return ++exemplarId;
    }

    public static long notificacaoId(){
        return ++notificacaoId;
    }
}
