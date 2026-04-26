package br.unisales.menu;

import br.unisales.manager_factory.ManagerFactory;
import br.unisales.menu.util.MenuUtil;
import br.unisales.service.RelatorioService;

import java.util.Scanner;

public final class RelatorioMenu {
    private final Scanner scanner;

    // Inicializa o menu de relatórios e gerencia o fluxo de interação com o usuário
    public RelatorioMenu(Scanner scanner) {
        this.scanner = scanner;
        System.out.println("==========================================");
        System.out.println("   SISTEMA DE RELATÓRIOS COM JPA          ");
        System.out.println("==========================================");

        ManagerFactory managerFactory = new ManagerFactory("SQLitePU");
        RelatorioService relatorioService = new RelatorioService(managerFactory.get());

        int opcao;
        do {
            exibirMenu();
            opcao = MenuUtil.lerInteiro(this.scanner, "Escolha uma opção: ");

            switch (opcao) {
                case 1 -> topLivrosMaisEmprestados(relatorioService);
                case 2 -> emprestimosEmAtraso(relatorioService);
                case 3 -> usuariosComMaisAtrasos(relatorioService);
                case 4 -> estatisticasMensais(relatorioService);
                case 100 -> System.out.println("Voltando para o menu principal...");
                default -> System.out.println("Opção inválida. Tente novamente.");
            }
            System.out.println();
        } while (opcao != 100);
        managerFactory.close();
    }

    // Exibe as opções disponíveis no menu de relatórios
    private static void exibirMenu() {
        System.out.println("--------------- MENU ----------------");
        System.out.println("1 - Top livros mais emprestados");
        System.out.println("2 - Empréstimos em atraso");
        System.out.println("3 - Usuários com mais atrasos");
        System.out.println("4 - Estatísticas mensais");
        System.out.println("100 - Voltar");
        System.out.println("-------------------------------------");
    }

    // Exibe o ranking dos livros mais emprestados
    private static void topLivrosMaisEmprestados(RelatorioService relatorioService) {
        MenuUtil.limparConsole();
        relatorioService.topLivrosMaisEmprestados();
    }

    // Exibe os empréstimos com prazo vencido e ainda não devolvidos
    private static void emprestimosEmAtraso(RelatorioService relatorioService) {
        MenuUtil.limparConsole();
        relatorioService.emprestimosEmAtraso();
    }

    // Exibe os usuários com maior quantidade de empréstimos em atraso
    private static void usuariosComMaisAtrasos(RelatorioService relatorioService) {
        MenuUtil.limparConsole();
        relatorioService.usuariosComMaisAtrasos();
    }

    // Exibe as estatísticas de movimentação do mês atual
    private static void estatisticasMensais(RelatorioService relatorioService) {
        MenuUtil.limparConsole();
        relatorioService.estatisticasMensais();
    }
}