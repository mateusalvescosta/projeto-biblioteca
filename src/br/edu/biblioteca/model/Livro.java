package br.edu.biblioteca.model;

public class Livro {
    private String isbn;
    private String titulo;
    private int ano;
    private Categoria[] categorias;
    private Autor[] autores;
    private String[] palavrasChave;

    public Livro() {
    }

    public Livro(String isbn, String titulo, int ano, Categoria[] categorias, Autor[] autores, String[] palavrasChave) {
        this.isbn = isbn;
        this.titulo = titulo;
        this.ano = ano;
        this.categorias = categorias;
        this.autores = autores;
        this.palavrasChave = palavrasChave;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public int getAno() {
        return ano;
    }

    public void setAno(int ano) {
        this.ano = ano;
    }

    public Categoria[] getCategorias() {
        return categorias;
    }

    public void setCategorias(Categoria[] categorias) {
        this.categorias = categorias;
    }

    public Autor[] getAutores() {
        return autores;
    }

    public void setAutores(Autor[] autores) {
        this.autores = autores;
    }

    public String[] getPalavrasChave() {
        return palavrasChave;
    }

    public void setPalavrasChave(String[] palavrasChave) {
        this.palavrasChave = palavrasChave;
    }

}
