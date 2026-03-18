import br.edu.biblioteca.service.AutorService;
import br.edu.biblioteca.service.CategoriaService;
import br.edu.biblioteca.service.LivroService;


public class Main {
    public static void main(String[] args) {
        AutorService autores = new AutorService(2);
        CategoriaService categorias = new CategoriaService(2);
        LivroService livro = new LivroService();

        autores.criarAutor("Mateus");
        autores.criarAutor("Carlos");
        autores.imprimirAutores();

        System.out.println(" ");

        categorias.criarCategoria("Romance");
        categorias.criarCategoria("Terror");
        categorias.imprimirCategorias();

        System.out.println(" ");

        livro.criarLivro("8599296361", "A Cabana", 2008, categorias.getCategorias(), autores.getAutores());
        livro.imprimirLivro();










    }
}
