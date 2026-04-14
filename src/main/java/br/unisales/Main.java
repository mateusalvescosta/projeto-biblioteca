package br.unisales;

import br.unisales.menu.CatalogoMenu;
import br.unisales.menu.CategoriaMenu;
import br.unisales.menu.EmprestimoMenu;
import br.unisales.menu.RelatorioMenu;
import br.unisales.menu.ReservaMenu;
import br.unisales.menu.UndoRedoMenu;
import br.unisales.menu.UsuarioMenu;
import br.unisales.menu.util.MenuUtil;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static Scanner scanner;

    public static void main(String[] args) {
        configurarLogs();
        scanner = new Scanner(System.in, StandardCharsets.UTF_8);

        System.out.println("==========================================");
        System.out.println("      SISTEMA DE BIBLIOTECA COM JPA       ");
        System.out.println("==========================================");
        int opcao;

        do {
            exibirMenu();
            opcao = LerInteiro("Escolha uma opcao: ");

            switch (opcao) {
                case 1 -> menuCategoria();
                case 2 -> menuUsuario();
                case 3 -> menuCatalogo();
                case 4 -> menuEmprestimo();
                case 5 -> menuReserva();
                case 6 -> menuRelatorio();
                case 7 -> menuUndoRedo();
                case 0 -> System.out.println("Encerrando o sistema...");
                default -> System.out.println("Opcao invalida. Tente novamente.");
            }
            System.out.println();
        } while (opcao != 0);

        scanner.close();
    }

    private static void configurarLogs() {
        Logger hibernateLogger = Logger.getLogger("org.hibernate");
        hibernateLogger.setLevel(Level.SEVERE);
        hibernateLogger.setUseParentHandlers(false);

        Logger jbossLogger = Logger.getLogger("org.jboss");
        jbossLogger.setLevel(Level.SEVERE);
        jbossLogger.setUseParentHandlers(false);
    }

    private static void exibirMenu() {
        System.out.println("--------------- MENU ----------------");
        System.out.println("1 - Categoria");
        System.out.println("2 - Usuário");
        System.out.println("3 - Catálogo");
        System.out.println("4 - Empréstimos");
        System.out.println("5 - Reservas");
        System.out.println("6 - Relatórios");
        System.out.println("7 - Undo/Redo");
        System.out.println("0 - Sair");
        System.out.println("-------------------------------------");
    }

    private static void menuCategoria() {
        MenuUtil.limparConsole();
        new CategoriaMenu(scanner);
    }

    private static void menuUsuario() {
        MenuUtil.limparConsole();
        new UsuarioMenu(scanner);
    }

    private static void menuCatalogo() {
        MenuUtil.limparConsole();
        new CatalogoMenu(scanner);
    }

    private static void menuEmprestimo() {
        MenuUtil.limparConsole();
        new EmprestimoMenu(scanner);
    }

    private static void menuReserva() {
        MenuUtil.limparConsole();
        new ReservaMenu(scanner);
    }

    private static void menuRelatorio() {
        MenuUtil.limparConsole();
        new RelatorioMenu(scanner);
    }

    private static void menuUndoRedo() {
        MenuUtil.limparConsole();
        new UndoRedoMenu(scanner);
    }

    private static Integer LerInteiro(String mensagem) {
        while (true) {
            try {
                System.out.print(mensagem);
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Valor invalido. Digite um numero inteiro.");
            }
        }
    }
}