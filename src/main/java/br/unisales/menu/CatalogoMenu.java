package br.unisales.menu;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

import br.unisales.database.table.Exemplar;
import br.unisales.database.table.Livro;
import br.unisales.manager_factory.ManagerFactory;
import br.unisales.menu.util.MenuUtil;
import br.unisales.service.CatalogoService;

public final class CatalogoMenu {
    private final Scanner scanner;

    // Inicializa o menu do catálogo e gerencia o fluxo de interação com o usuário
    public CatalogoMenu(Scanner scanner) {
        this.scanner = scanner;
        System.out.println("==========================================");
        System.out.println("   SISTEMA DE CATÁLOGO DE LIVROS COM JPA  ");
        System.out.println("==========================================");

        ManagerFactory managerFactory = new ManagerFactory("SQLitePU");
        CatalogoService catalogoService = new CatalogoService(managerFactory.get());

        int opcao;
        do {
            exibirMenu();
            opcao = MenuUtil.lerInteiro(this.scanner, "Escolha uma opção: ");

            switch (opcao) {
                case 1 -> cadastrarLivro(catalogoService);
                case 2 -> cadastrarExemplar(catalogoService);
                case 3 -> removerLivro(catalogoService);
                case 4 -> removerExemplar(catalogoService);
                case 5 -> buscarLivroPorIsbn(catalogoService);
                case 6 -> buscarLivroPorTitulo(catalogoService);
                case 7 -> listarLivros(catalogoService);
                case 8 -> listarExemplares(catalogoService);
                case 100 -> System.out.println("Voltando para o menu principal...");
                default -> System.out.println("Opção inválida. Tente novamente.");
            }
            System.out.println();
        } while (opcao != 100);
        managerFactory.close();
    }

    // Exibe as opções disponíveis no menu do catálogo
    private static void exibirMenu() {
        System.out.println("--------------- MENU ----------------");
        System.out.println("1 - Cadastrar livro");
        System.out.println("2 - Cadastrar exemplar");
        System.out.println("3 - Remover livro");
        System.out.println("4 - Remover exemplar");
        System.out.println("5 - Buscar livro por ISBN");
        System.out.println("6 - Buscar livro por título");
        System.out.println("7 - Listar todos os livros");
        System.out.println("8 - Listar exemplares de um livro");
        System.out.println("100 - Voltar");
        System.out.println("-------------------------------------");
    }

    // Coleta os dados do livro, autor e categoria e aciona o cadastro
    private void cadastrarLivro(CatalogoService catalogoService) {
        MenuUtil.limparConsole();
        System.out.println("=== CADASTRAR LIVRO ===");
        String isbn = MenuUtil.lerTexto(this.scanner, "Informe o ISBN: ");
        String titulo = MenuUtil.lerTexto(this.scanner, "Informe o título: ");
        String recebeAno = MenuUtil.lerTexto(this.scanner, "Informe o ano de publicação (AAAA-MM-DD) ou deixe em branco: ");

        // Tenta converter o ano informado, ignorando o campo em caso de formato inválido
        LocalDate ano = null;
        if (!recebeAno.isBlank()) {
            try {
                ano = LocalDate.parse(recebeAno);
            } catch (Exception e) {
                System.out.println("Data inválida, o campo ano será ignorado.");
            }
        }

        String nomeAutor = MenuUtil.lerTexto(this.scanner, "Informe o nome do autor: ");
        String nomeCategoria = MenuUtil.lerTexto(this.scanner, "Informe o nome da categoria: ");

        Livro livro = Livro.builder()
                .isbn(isbn)
                .titulo(titulo)
                .ano(ano)
                .build();

        catalogoService.cadastrarLivro(livro, nomeAutor, nomeCategoria);
    }

    // Busca o livro pelo ISBN e cadastra um novo exemplar vinculado a ele
    private void cadastrarExemplar(CatalogoService catalogoService) {
        MenuUtil.limparConsole();
        System.out.println("=== CADASTRAR EXEMPLAR ===");
        String isbn = MenuUtil.lerTexto(this.scanner, "Informe o ISBN do livro: ");

        // Valida se o livro existe antes de criar o exemplar
        Livro livro = catalogoService.buscarLivroPorIsbn(isbn);
        if (livro == null) {
            System.out.println("Livro não encontrado para o ISBN informado.");
            return;
        }

        Exemplar exemplar = Exemplar.builder()
                .livro(livro)
                .build();

        catalogoService.cadastrarExemplar(exemplar);
    }

    // Busca o livro pelo ISBN e solicita confirmação antes de removê-lo
    private void removerLivro(CatalogoService catalogoService) {
        MenuUtil.limparConsole();
        System.out.println("=== REMOVER LIVRO ===");
        String isbn = MenuUtil.lerTexto(this.scanner, "Informe o ISBN do livro a ser removido: ");

        // Valida se o livro existe antes de solicitar confirmação
        Livro livro = catalogoService.buscarLivroPorIsbn(isbn);
        if (livro == null) {
            System.out.println("Livro não encontrado.");
            return;
        }

        // Solicita confirmação do usuário antes de remover
        System.out.println("Livro encontrado: " + livro.getTitulo());
        String confirmacao = MenuUtil.lerTexto(this.scanner, "Deseja realmente remover este livro e todos os seus exemplares? (S/N): ");
        if (confirmacao.equalsIgnoreCase("S")) {
            catalogoService.removerLivro(isbn);
        } else {
            System.out.println("Remoção cancelada.");
        }
    }

    // Solicita o ID do exemplar e confirmação antes de removê-lo
    private void removerExemplar(CatalogoService catalogoService) {
        MenuUtil.limparConsole();
        System.out.println("=== REMOVER EXEMPLAR ===");
        Long id = MenuUtil.lerLong(this.scanner, "Informe o ID do exemplar a ser removido: ");

        // Solicita confirmação do usuário antes de remover
        String confirmacao = MenuUtil.lerTexto(this.scanner, "Deseja realmente remover este exemplar? (S/N): ");
        if (confirmacao.equalsIgnoreCase("S")) {
            catalogoService.removerExemplar(id);
        } else {
            System.out.println("Remoção cancelada.");
        }
    }

    // Busca e exibe um livro pelo ISBN exato
    private void buscarLivroPorIsbn(CatalogoService catalogoService) {
        MenuUtil.limparConsole();
        System.out.println("=== BUSCAR POR ISBN ===");
        String isbn = MenuUtil.lerTexto(this.scanner, "Informe o ISBN: ");
        Livro livro = catalogoService.buscarLivroPorIsbn(isbn);
        if (livro == null) {
            System.out.println("Livro não encontrado.");
            return;
        }
        exibirLivro(livro);
    }

    // Busca e exibe livros cujo título contenha o termo informado
    private void buscarLivroPorTitulo(CatalogoService catalogoService) {
        MenuUtil.limparConsole();
        System.out.println("=== BUSCAR POR TÍTULO ===");
        String titulo = MenuUtil.lerTexto(this.scanner, "Informe o título (ou parte dele): ");
        List<Livro> lista = catalogoService.buscarLivrosPorTitulo(titulo);
        if (lista.isEmpty()) {
            System.out.println("Nenhum livro encontrado.");
            return;
        }
        lista.forEach(CatalogoMenu::exibirLivro);
    }

    // Lista e exibe todos os livros cadastrados no catálogo
    private static void listarLivros(CatalogoService catalogoService) {
        MenuUtil.limparConsole();
        System.out.println("=== LISTAR TODOS OS LIVROS ===");
        List<Livro> lista = catalogoService.listarLivros();
        if (lista.isEmpty()) {
            System.out.println("Nenhum livro cadastrado.");
            return;
        }
        lista.forEach(CatalogoMenu::exibirLivro);
    }

    // Busca o livro pelo ISBN e exibe todos os seus exemplares
    private void listarExemplares(CatalogoService catalogoService) {
        MenuUtil.limparConsole();
        System.out.println("=== LISTAR EXEMPLARES DE UM LIVRO ===");
        String isbn = MenuUtil.lerTexto(this.scanner, "Informe o ISBN do livro: ");

        // Valida se o livro existe antes de listar os exemplares
        Livro livro = catalogoService.buscarLivroPorIsbn(isbn);
        if (livro == null) {
            System.out.println("Livro não encontrado.");
            return;
        }

        System.out.println("Livro: " + livro.getTitulo());
        List<Exemplar> exemplares = catalogoService.listarExemplares(isbn);
        if (exemplares.isEmpty()) {
            System.out.println("Nenhum exemplar cadastrado para este livro.");
            return;
        }

        // Exibe o ID e status de cada exemplar encontrado
        System.out.println("-------------------------------------");
        for (Exemplar e : exemplares) {
            System.out.println("ID:     " + e.getId());
            System.out.println("Status: " + e.getStatus());
            System.out.println("-------------------------------------");
        }
    }

    // Exibe os dados formatados de um livro incluindo autores e categorias
    private static void exibirLivro(Livro livro) {
        System.out.println("-------------------------------------");
        System.out.println("ISBN:   " + livro.getIsbn());
        System.out.println("Título: " + livro.getTitulo());
        System.out.println("Ano:    " + (livro.getAno() != null ? livro.getAno() : "Não informado"));

        // Exibe os autores vinculados ao livro
        if (livro.getLivroAutores() != null && !livro.getLivroAutores().isEmpty()) {
            livro.getLivroAutores().forEach(la -> System.out.println("Autor:     " + la.getAutor().getNome()));
        } else {
            System.out.println("Autor:     Não informado");
        }

        // Exibe as categorias vinculadas ao livro
        if (livro.getLivroCategorias() != null && !livro.getLivroCategorias().isEmpty()) {
            livro.getLivroCategorias().forEach(lc -> System.out.println("Categoria: " + lc.getCategoria().getNome()));
        } else {
            System.out.println("Categoria: Não informada");
        }

        System.out.println("-------------------------------------");
    }
}