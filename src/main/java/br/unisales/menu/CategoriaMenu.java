package br.unisales.menu;

import java.util.List;
import java.util.Scanner;

import br.unisales.database.table.Categoria;
import br.unisales.manager_factory.ManagerFactory;
import br.unisales.menu.util.MenuUtil;
import br.unisales.service.CategoriaService;

public final class CategoriaMenu {
    private final Scanner scanner;

    public CategoriaMenu(Scanner scanner) {
        this.scanner = scanner;
        System.out.println("==========================================");
        System.out.println("   SISTEMA DE CRUD DE CATEGORIA COM JPA     ");
        System.out.println("==========================================");
        /*
         * Cria a fábrica de EntityManager com base na persistence-unit
         * definida no arquivo persistence.xml.
         *
         * Troque "SQLitePU" por:
         * - "MySQLPU"
         * - "PostgresPU"
         * - "SqlServerPU"
         * conforme o banco desejado.
         */
        ManagerFactory emf = new ManagerFactory("SQLitePU");
        CategoriaService categoriaService = new CategoriaService(emf.get());
        int opcao;
        do {
            exibirMenu();
            opcao = lerInteiro("Escolha uma opção: ");

            switch (opcao) {
                case 1 -> cadastrar(categoriaService);
                case 2 -> listar(categoriaService);
                case 3 -> buscarPorId(categoriaService);
                case 4 -> atualizar(categoriaService);
                case 5 -> excluir(categoriaService);
                case 100 -> System.out.println("Voltando para o menu principal...");
                default -> System.out.println("Opção inválida. Tente novamente.");
            }
            System.out.println();
        } while (opcao != 100);
        emf.close();
    }

    /**
     * Exibe o menu principal do sistema.
     */
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

    /**
     * Realiza o cadastro de um novo categoria.
     */
    private void cadastrar(CategoriaService categoriaService) {
        MenuUtil.limparConsole();
        System.out.println("=== CADASTRAR CATEGORIA ===");
        String nome = this.lerTexto("Informe a categoria: ");
        Categoria item = new Categoria(null, nome, null);
        categoriaService.inserir(item);
    }

    /**
     * Lista todos os categorias cadastrados.
     */
    private static void listar(CategoriaService categoriaService) {
        MenuUtil.limparConsole();
        System.out.println("=== LISTAR CATEGORIA ===");
        List<Categoria> lista = categoriaService.listarTodos();
        if (lista.isEmpty()) {
            System.out.println("Nenhuma categoria cadastrada.");
            return;
        }
        for (Categoria item : lista) {
            System.out.println("-------------------------------------");
            System.out.println("ID: " + item.getId());
            System.out.println("Nome: " + item.getNome());
        }
        System.out.println("-------------------------------------");
    }

    /**
     * Busca um categoria pelo ID.
     */
    private void buscarPorId(CategoriaService categoriaService) {
        MenuUtil.limparConsole();
        System.out.println("=== BUSCAR CATEGORIA POR ID ===");
        Integer id = this.lerInteiro("Informe o ID da categoria: ");
        Categoria item = categoriaService.buscarPorId(id);
        if (item == null) {
            System.out.println("Categoria não encontrada.");
            return;
        }
        System.out.println("Categoria encontrada:");
        System.out.println("-------------------------------------");
        System.out.println("ID: " + item.getId());
        System.out.println("Nome: " + item.getNome());
        System.out.println("-------------------------------------");
    }

    /**
     * Atualiza os dados de um categoria existente.
     */
    private void atualizar(CategoriaService categoriaService) {
        MenuUtil.limparConsole();
        System.out.println("=== ATUALIZAR CATEGORIA ===");
        Integer id = this.lerInteiro("Informe o ID da categoria que será atualizada: ");
        Categoria item = categoriaService.buscarPorId(id);
        if (item == null) {
            System.out.println("Categoria não encontrada.");
            return;
        }
        System.out.println("Dados atuais da categoria:");
        System.out.println("Nome: " + item.getNome());
        String novoNome = this.lerTexto("Informe a nova categoria: ");
        item.setNome(novoNome);
        categoriaService.atualizar(item);
    }

    /**
     * Exclui um categoria pelo ID.
     */
    private void excluir(CategoriaService categoriaService) {
        MenuUtil.limparConsole();
        System.out.println("=== EXCLUIR CATEGORIA ===");
        Integer id = this.lerInteiro("Informe o ID da categoria que será excluída: ");
        Categoria item = categoriaService.buscarPorId(id);
        if (item == null) {
            System.out.println("Categoria não encontrada.");
            return;
        }
        System.out.println("Categoria localizada:");
        System.out.println("ID: " + item.getId());
        System.out.println("Nome: " + item.getNome());
        String confirmacao = this.lerTexto("Deseja realmente excluir esta categoria? (S/N): ");
        if (confirmacao.equalsIgnoreCase("S")) {
            categoriaService.deletar(id);
        } else {
            System.out.println("Exclusão cancelada.");
        }
    }

    /**
     * Lê um número inteiro digitado pelo categoria.
     */
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

    /**
     * Lê um texto digitado pelo categoria.
     */
    private String lerTexto(String mensagem) {
        System.out.print(mensagem);
        return this.scanner.nextLine();
    }

}
