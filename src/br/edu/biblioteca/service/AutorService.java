package br.edu.biblioteca.service;

import br.edu.biblioteca.model.Autor;

public class AutorService {
    private int index;
    private Autor[] autores;

    public AutorService(int quantidade) {
        this.index = 0;
        this.autores = new Autor[quantidade];
    }

    public Autor[] getAutores() {
        return autores;
    }

    public void criarAutor(String nome){
        this.autores[this.index] = new Autor(Long.valueOf(this.index+1), nome);
        this.index++;
    }

    public void imprimirAutores(){
        for (int i = 0; i < autores.length; i++) {
            System.out.println("ID: " + autores[i].getId() + " — Autor: " + autores[i].getNome());
        }
    }




}
