package br.unisales.menu;

import br.unisales.manager_factory.ManagerFactory;
import br.unisales.menu.util.MenuUtil;
import br.unisales.service.RelatorioService;

import java.util.Scanner;

public final class RelatorioMenu {
    private final Scanner scanner;

    public RelatorioMenu(Scanner scanner) {
        this.scanner = scanner;
        System.out.println("==========================================");
        System.out.println("   SISTEMA DE RELATÓRIOS COM JPA          ");
        System.out.println("==========================================");

        ManagerFactory emf = new ManagerFactory("SQLitePU");
        RelatorioService relatorioService = new RelatorioService(emf.get());

        int opcao;
        do {
            exibirMenu();
            opcao = lerInteiro("Escolha uma opção: ");

            switch (opcao) {
                case 1 -> topMaisEmprestados(relatorioService);
                case 2 -> emAtraso(relatorioService);
                case 3 -> usuariosComMaisAtrasos(relatorioService);
                case 4 -> estatisticasMensais(relatorioService);
                case 100 -> System.out.println("Voltando para o menu principal...");
                default -> System.out.println("Opção inválida. Tente novamente.");
            }
            System.out.println();
        } while (opcao != 100);
        emf.close();
    }

    private static void exibirMenu() {
        System.out.println("--------------- MENU ----------------");
        System.out.println("1 - Top livros mais emprestados");
        System.out.println("2 - Empréstimos em atraso");
        System.out.println("3 - Usuários com mais atrasos");
        System.out.println("4 - Estatísticas mensais");
        System.out.println("100 - Voltar");
        System.out.println("-------------------------------------");
    }

    private static void topMaisEmprestados(RelatorioService relatorioService) {
        MenuUtil.limparConsole();
        relatorioService.topMaisEmprestados();
    }

    private static void emAtraso(RelatorioService relatorioService) {
        MenuUtil.limparConsole();
        relatorioService.emAtraso();
    }

    private static void usuariosComMaisAtrasos(RelatorioService relatorioService) {
        MenuUtil.limparConsole();
        relatorioService.usuariosComMaisAtrasos();
    }

    private static void estatisticasMensais(RelatorioService relatorioService) {
        MenuUtil.limparConsole();
        relatorioService.estatisticasMensais();
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
}