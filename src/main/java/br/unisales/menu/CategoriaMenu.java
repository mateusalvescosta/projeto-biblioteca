package br.unisales.menu;

import java.util.List;
import java.util.Scanner;

import br.unisales.database.table.Categoria;
import br.unisales.manager_factory.ManagerFactory;
import br.unisales.menu.util.MenuUtil;
import br.unisales.service.CategoriaService;

public final class CategoriaMenu {
    private final Scanner scanner;

    // Inicializa o menu de categorias e gerencia o fluxo de interação com o usuário
    public CategoriaMenu(Scanner scanner) {
        this.scanner = scanner;
        System.out.println("==========================================");
        System.out.println("   SISTEMA DE CRUD DE CATEGORIA COM JPA     ");
        System.out.println("==========================================");

        ManagerFactory managerFactory = new ManagerFactory("SQLitePU");
        CategoriaService categoriaService = new CategoriaService(managerFactory.get());
        int opcao;
        do {
            exibirMenu();
            opcao = MenuUtil.lerInteiro(this.scanner, "Escolha uma opção: ");

            switch (opcao) {
                case 1 -> cadastrarCategoria(categoriaService);
                case 2 -> listarCategorias(categoriaService);
                case 3 -> buscarCategoriaPorId(categoriaService);
                case 4 -> atualizarCategoria(categoriaService);
                case 5 -> excluirCategoria(categoriaService);
                case 100 -> System.out.println("Voltando para o menu principal...");
                default -> System.out.println("Opção inválida. Tente novamente.");
            }
            System.out.println();
        } while (opcao != 100);
        managerFactory.close();
    }

    // Exibe as opções disponíveis no menu de categorias
    private static void exibirMenu() {
        System.out.println("--------------- MENU ----------------");
        System.out.println("1 - Cadastrar categoria");
        System.out.println("2 - Listar categorias");
        System.out.println("3 - Buscar categoria por ID");
        System.out.println("4 - Atualizar categoria");
        System.out.println("5 - Excluir categoria");
        System.out.println("100 - Voltar");
        System.out.println("-------------------------------------");
    }

    // Coleta o nome da categoria e aciona o cadastro
    private void cadastrarCategoria(CategoriaService categoriaService) {
        MenuUtil.limparConsole();
        System.out.println("=== CADASTRAR CATEGORIA ===");
        String nome = MenuUtil.lerTexto(this.scanner, "Informe a categoria: ");
        Categoria item = new Categoria(null, nome, null);
        categoriaService.inserirCategoria(item);
    }

    // Lista e exibe todas as categorias cadastradas
    private static void listarCategorias(CategoriaService categoriaService) {
        MenuUtil.limparConsole();
        System.out.println("=== LISTAR CATEGORIA ===");
        List<Categoria> lista = categoriaService.listarTodasCategorias();
        if (lista.isEmpty()) {
            System.out.println("Nenhuma categoria cadastrada.");
            return;
        }
        // Exibe o ID e nome de cada categoria encontrada
        for (Categoria item : lista) {
            System.out.println("-------------------------------------");
            System.out.println("ID: " + item.getId());
            System.out.println("Nome: " + item.getNome());
        }
        System.out.println("-------------------------------------");
    }

    // Busca e exibe uma categoria pelo ID informado
    private void buscarCategoriaPorId(CategoriaService categoriaService) {
        MenuUtil.limparConsole();
        System.out.println("=== BUSCAR CATEGORIA POR ID ===");
        Integer id = MenuUtil.lerInteiro(this.scanner, "Informe o ID da categoria: ");
        Categoria item = categoriaService.buscarCategoriaPorId(id);
        if (item == null) {
            System.out.println("Categoria não encontrada.");
            return;
        }
        // Exibe os dados da categoria encontrada
        System.out.println("Categoria encontrada:");
        System.out.println("-------------------------------------");
        System.out.println("ID: " + item.getId());
        System.out.println("Nome: " + item.getNome());
        System.out.println("-------------------------------------");
    }

    // Busca a categoria pelo ID e permite alterar o nome
    private void atualizarCategoria(CategoriaService categoriaService) {
        MenuUtil.limparConsole();
        System.out.println("=== ATUALIZAR CATEGORIA ===");
        Integer id = MenuUtil.lerInteiro(this.scanner, "Informe o ID da categoria que será atualizada: ");

        // Valida se a categoria existe antes de solicitar o novo nome
        Categoria item = categoriaService.buscarCategoriaPorId(id);
        if (item == null) {
            System.out.println("Categoria não encontrada.");
            return;
        }

        // Exibe o nome atual e solicita o novo nome
        System.out.println("Dados atuais da categoria:");
        System.out.println("Nome: " + item.getNome());
        String novoNome = MenuUtil.lerTexto(this.scanner, "Informe a nova categoria: ");
        item.setNome(novoNome);
        categoriaService.atualizarCategoria(item);
    }

    // Busca a categoria pelo ID e solicita confirmação antes de excluí-la
    private void excluirCategoria(CategoriaService categoriaService) {
        MenuUtil.limparConsole();
        System.out.println("=== EXCLUIR CATEGORIA ===");
        Integer id = MenuUtil.lerInteiro(this.scanner, "Informe o ID da categoria que será excluída: ");

        // Valida se a categoria existe antes de solicitar confirmação
        Categoria item = categoriaService.buscarCategoriaPorId(id);
        if (item == null) {
            System.out.println("Categoria não encontrada.");
            return;
        }

        // Exibe os dados e solicita confirmação do usuário antes de excluir
        System.out.println("Categoria localizada:");
        System.out.println("ID: " + item.getId());
        System.out.println("Nome: " + item.getNome());
        String confirmacao = MenuUtil.lerTexto(this.scanner, "Deseja realmente excluir esta categoria? (S/N): ");
        if (confirmacao.equalsIgnoreCase("S")) {
            categoriaService.deletarCategoria(id);
        } else {
            System.out.println("Exclusão cancelada.");
        }
    }
}