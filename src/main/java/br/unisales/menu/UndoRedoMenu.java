package br.unisales.menu;

import br.unisales.manager_factory.ManagerFactory;
import br.unisales.service.UndoRedoService;

import java.util.Scanner;

public final class UndoRedoMenu {
    private final Scanner scanner;

    public UndoRedoMenu(Scanner scanner) {
        this.scanner = scanner;
        System.out.println("==========================================");
        System.out.println("   SISTEMA DE UNDO/REDO COM JPA           ");
        System.out.println("==========================================");

        ManagerFactory emf = new ManagerFactory("SQLitePU");
        UndoRedoService undoRedoService = new UndoRedoService(emf.get());

        int opcao;
        do {
            exibirMenu();
            opcao = lerInteiro("Escolha uma opção: ");

            switch (opcao) {
                case 1 -> undoRedoService.desfazerEmprestimo();
                case 2 -> undoRedoService.desfazerDevolucao();
                case 3 -> undoRedoService.desfazerCadastroUsuario();
                case 4 -> undoRedoService.desfazerRenovar();
                case 100 -> System.out.println("Voltando para o menu principal...");
                default -> System.out.println("Opção inválida. Tente novamente.");
            }
            System.out.println();
        } while (opcao != 100);
        emf.close();
    }

    private static void exibirMenu() {
        System.out.println("--------------- MENU ----------------");
        System.out.println("1 - Desfazer empréstimo");
        System.out.println("2 - Desfazer devolução");
        System.out.println("3 - Desfazer cadastro de usuário");
        System.out.println("4 - Desfazer renovação");
        System.out.println("100 - Voltar");
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
}