package br.unisales.menu;

import br.unisales.manager_factory.ManagerFactory;
import br.unisales.menu.util.MenuUtil;
import br.unisales.service.UndoRedoService;

import java.util.Scanner;

public final class UndoRedoMenu {
    private final Scanner scanner;

    // Inicializa o menu de undo/redo e gerencia o fluxo de interação com o usuário
    public UndoRedoMenu(Scanner scanner) {
        this.scanner = scanner;
        System.out.println("==========================================");
        System.out.println("   SISTEMA DE UNDO/REDO COM JPA           ");
        System.out.println("==========================================");

        ManagerFactory managerFactory = new ManagerFactory("SQLitePU");
        UndoRedoService undoRedoService = new UndoRedoService(managerFactory.get());

        int opcao;
        do {
            exibirMenu();
            opcao = MenuUtil.lerInteiro(this.scanner, "Escolha uma opção: ");

            // Aciona a operação de desfazer correspondente à opção escolhida
            switch (opcao) {
                case 1 -> undoRedoService.desfazerEmprestimo();
                case 2 -> undoRedoService.desfazerDevolucao();
                case 3 -> undoRedoService.desfazerCadastroUsuario();
                case 4 -> undoRedoService.desfazerRenovar();
                case 5 -> undoRedoService.desfazerMulta();
                case 6 -> undoRedoService.desfazerNotificacao();
                case 100 -> System.out.println("Voltando para o menu principal...");
                default -> System.out.println("Opção inválida. Tente novamente.");
            }
            System.out.println();
        } while (opcao != 100);
        managerFactory.close();
    }

    // Exibe as opções disponíveis no menu de undo/redo
    private static void exibirMenu() {
        System.out.println("--------------- MENU ----------------");
        System.out.println("1 - Desfazer empréstimo");
        System.out.println("2 - Desfazer devolução");
        System.out.println("3 - Desfazer cadastro de usuário");
        System.out.println("4 - Desfazer renovação");
        System.out.println("5 - Desfazer multa");
        System.out.println("6 - Desfazer notificação");
        System.out.println("100 - Voltar");
        System.out.println("-------------------------------------");
    }

}