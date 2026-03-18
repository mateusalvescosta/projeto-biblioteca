package br.edu.biblioteca.service;

import br.edu.biblioteca.model.Autor;
import br.edu.biblioteca.model.Categoria;
import br.edu.biblioteca.model.Livro;

import java.util.Arrays;

public class LivroService {
    private int index;
    private Livro livro;
    private String[] palavrasChave;

    public void criarLivro(String isbn, String titulo, int ano, Categoria[] categorias, Autor[] autores){
        this.palavrasChave = new String[categorias.length + autores.length];
        for (int i = 0; i < categorias.length; i++) {
            this.palavrasChave[i] = categorias[i].getNome();
        }
        for (int i = 0; i < autores.length; i++) {
            this.palavrasChave[categorias.length + i] = autores[i].getNome();
        }

        this.livro = new Livro(isbn, titulo, ano, categorias, autores, palavrasChave);
    }


    public void imprimirLivro(){

        System.out.println("Informações do Livro:" + "\n" +
                           "ISBN: " + this.livro.getIsbn() + "\n" +
                           "Título: " + this.livro.getTitulo() + "\n" +
                           "Ano: " + this.livro.getAno()  + "\n" +
                           "Autores: " + Arrays.toString(this.livro.getAutores()) + "\n" +
                           "Categorias: " + Arrays.toString(this.livro.getCategorias()) + "\n" +
                           "Palavras-chave: " + Arrays.toString(this.livro.getPalavrasChave()));
    }
}

