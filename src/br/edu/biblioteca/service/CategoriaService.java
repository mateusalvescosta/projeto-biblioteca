package br.edu.biblioteca.service;

import br.edu.biblioteca.model.Categoria;

public class CategoriaService {
    private int index;
    private Categoria[] categorias;

    public CategoriaService(int quantidade){
        this.index = 0;
        this.categorias = new Categoria[quantidade];
    }

    public Categoria[] getCategorias() {
        return categorias;
    }

    public void criarCategoria(String nome){
        this.categorias[this.index] = new Categoria(Long.valueOf(this.index+1), nome);
        this.index++;
    }

    public void imprimirCategorias(){
        for (int i = 0; i < categorias.length; i++) {
            System.out.println("Categoria: " + categorias[i].getNome());
        }
    }





}
