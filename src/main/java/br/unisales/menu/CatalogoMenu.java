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

    public CatalogoMenu(Scanner scanner) {
        this.scanner = scanner;
        System.out.println("==========================================");
        System.out.println("   SISTEMA DE CATÁLOGO DE LIVROS COM JPA  ");
        System.out.println("==========================================");

        ManagerFactory emf = new ManagerFactory("SQLitePU");
        CatalogoService catalogoService = new CatalogoService(emf.get());

        int opcao;
        do {
            exibirMenu();
            opcao = lerInteiro("Escolha uma opção: ");

            switch (opcao) {
                case 1 -> cadastrarLivro(catalogoService);
                case 2 -> cadastrarExemplar(catalogoService);
                case 3 -> removerLivro(catalogoService);
                case 4 -> removerExemplar(catalogoService);
                case 5 -> buscarPorIsbn(catalogoService);
                case 6 -> buscarPorTitulo(catalogoService);
                case 7 -> listar(catalogoService);
                case 8 -> listarExemplares(catalogoService);
                case 100 -> System.out.println("Voltando para o menu principal...");
                default -> System.out.println("Opção inválida. Tente novamente.");
            }
            System.out.println();
        } while (opcao != 100);
        emf.close();
    }

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

    private void cadastrarLivro(CatalogoService catalogoService) {
        MenuUtil.limparConsole();
        System.out.println("=== CADASTRAR LIVRO ===");
        String isbn = lerTexto("Informe o ISBN: ");
        String titulo = lerTexto("Informe o título: ");
        String anoStr = lerTexto("Informe o ano de publicação (AAAA-MM-DD) ou deixe em branco: ");

        LocalDate ano = null;
        if (!anoStr.isBlank()) {
            try {
                ano = LocalDate.parse(anoStr);
            } catch (Exception e) {
                System.out.println("Data inválida, o campo ano será ignorado.");
            }
        }

        Livro livro = Livro.builder()
                .isbn(isbn)
                .titulo(titulo)
                .ano(ano)
                .build();

        catalogoService.cadastrarLivro(livro);
    }

    private void cadastrarExemplar(CatalogoService catalogoService) {
        MenuUtil.limparConsole();
        System.out.println("=== CADASTRAR EXEMPLAR ===");
        String isbn = lerTexto("Informe o ISBN do livro: ");

        Livro livro = catalogoService.buscarPorIsbn(isbn);
        if (livro == null) {
            System.out.println("Livro não encontrado para o ISBN informado.");
            return;
        }

        Exemplar exemplar = Exemplar.builder()
                .livro(livro)
                .build();

        catalogoService.cadastrarExemplar(exemplar);
    }

    private void removerLivro(CatalogoService catalogoService) {
        MenuUtil.limparConsole();
        System.out.println("=== REMOVER LIVRO ===");
        String isbn = lerTexto("Informe o ISBN do livro a ser removido: ");

        Livro livro = catalogoService.buscarPorIsbn(isbn);
        if (livro == null) {
            System.out.println("Livro não encontrado.");
            return;
        }

        System.out.println("Livro encontrado: " + livro.getTitulo());
        String confirmacao = lerTexto("Deseja realmente remover este livro e todos os seus exemplares? (S/N): ");
        if (confirmacao.equalsIgnoreCase("S")) {
            catalogoService.removerLivro(isbn);
        } else {
            System.out.println("Remoção cancelada.");
        }
    }

    private void removerExemplar(CatalogoService catalogoService) {
        MenuUtil.limparConsole();
        System.out.println("=== REMOVER EXEMPLAR ===");
        Long id = lerLong("Informe o ID do exemplar a ser removido: ");
        String confirmacao = lerTexto("Deseja realmente remover este exemplar? (S/N): ");
        if (confirmacao.equalsIgnoreCase("S")) {
            catalogoService.removerExemplar(id);
        } else {
            System.out.println("Remoção cancelada.");
        }
    }

    private void buscarPorIsbn(CatalogoService catalogoService) {
        MenuUtil.limparConsole();
        System.out.println("=== BUSCAR POR ISBN ===");
        String isbn = lerTexto("Informe o ISBN: ");
        Livro livro = catalogoService.buscarPorIsbn(isbn);
        if (livro == null) {
            System.out.println("Livro não encontrado.");
            return;
        }
        exibirLivro(livro);
    }

    private void buscarPorTitulo(CatalogoService catalogoService) {
        MenuUtil.limparConsole();
        System.out.println("=== BUSCAR POR TÍTULO ===");
        String titulo = lerTexto("Informe o título (ou parte dele): ");
        List<Livro> lista = catalogoService.buscarPorTitulo(titulo);
        if (lista.isEmpty()) {
            System.out.println("Nenhum livro encontrado.");
            return;
        }
        lista.forEach(CatalogoMenu::exibirLivro);
    }

    private static void listar(CatalogoService catalogoService) {
        MenuUtil.limparConsole();
        System.out.println("=== LISTAR TODOS OS LIVROS ===");
        List<Livro> lista = catalogoService.listar();
        if (lista.isEmpty()) {
            System.out.println("Nenhum livro cadastrado.");
            return;
        }
        lista.forEach(CatalogoMenu::exibirLivro);
    }

    private void listarExemplares(CatalogoService catalogoService) {
        MenuUtil.limparConsole();
        System.out.println("=== LISTAR EXEMPLARES DE UM LIVRO ===");
        String isbn = lerTexto("Informe o ISBN do livro: ");

        Livro livro = catalogoService.buscarPorIsbn(isbn);
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

        System.out.println("-------------------------------------");
        for (Exemplar e : exemplares) {
            System.out.println("ID:     " + e.getId());
            System.out.println("Status: " + e.getStatus());
            System.out.println("-------------------------------------");
        }
    }

    private static void exibirLivro(Livro livro) {
        System.out.println("-------------------------------------");
        System.out.println("ISBN:   " + livro.getIsbn());
        System.out.println("Título: " + livro.getTitulo());
        System.out.println("Ano:    " + (livro.getAno() != null ? livro.getAno() : "Não informado"));
        System.out.println("-------------------------------------");
    }

    private Integer lerInteiro(String mensagem) {
        while (true) {
            try {
                System.out.print(mensagem);
                return Integer.parseInt(this.scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Valor inválido. Digite um número inteiro.");
            }
        }
    }

    private Long lerLong(String mensagem) {
        while (true) {
            try {
                System.out.print(mensagem);
                return Long.parseLong(this.scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Valor inválido. Digite um número inteiro.");
            }
        }
    }

    private String lerTexto(String mensagem) {
        System.out.print(mensagem);
        return this.scanner.nextLine();
    }
}