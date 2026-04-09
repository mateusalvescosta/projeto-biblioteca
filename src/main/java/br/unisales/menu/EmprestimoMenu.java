package br.unisales.menu;

import java.time.LocalDateTime;
import java.util.Scanner;

import br.unisales.Enumeration.StatusEmprestimoEnum;
import br.unisales.database.table.Emprestimo;
import br.unisales.database.table.Exemplar;
import br.unisales.database.table.Usuario;
import br.unisales.manager_factory.ManagerFactory;
import br.unisales.menu.util.MenuUtil;
import br.unisales.service.EmprestimoService;

public final class EmprestimoMenu {
    private final Scanner scanner;

    public EmprestimoMenu(Scanner scanner) {
        this.scanner = scanner;
        System.out.println("==========================================");
        System.out.println("   SISTEMA DE EMPRÉSTIMOS COM JPA         ");
        System.out.println("==========================================");

        ManagerFactory emf = new ManagerFactory("SQLitePU");
        EmprestimoService emprestimoService = new EmprestimoService(emf.get());

        int opcao;
        do {
            exibirMenu();
            opcao = lerInteiro("Escolha uma opção: ");

            switch (opcao) {
                case 1 -> emprestarExemplar(emprestimoService);
                case 2 -> devolverExemplar(emprestimoService);
                case 3 -> renovarEmprestimo(emprestimoService);
                case 4 -> calcularMulta(emprestimoService);
                case 5 -> listarEmprestimos(emprestimoService);
                case 100 -> System.out.println("Voltando para o menu principal...");
                default -> System.out.println("Opção inválida. Tente novamente.");
            }
            System.out.println();
        } while (opcao != 100);
        emf.close();
    }

    private static void exibirMenu() {
        System.out.println("--------------- MENU ----------------");
        System.out.println("1 - Realizar empréstimo");
        System.out.println("2 - Devolver exemplar");
        System.out.println("3 - Renovar empréstimo");
        System.out.println("4 - Calcular multa");
        System.out.println("5 - Listar empréstimos");
        System.out.println("100 - Voltar");
        System.out.println("-------------------------------------");
    }

    private void emprestarExemplar(EmprestimoService emprestimoService) {
        MenuUtil.limparConsole();
        System.out.println("=== REALIZAR EMPRÉSTIMO ===");

        Long usuarioId = lerLong("Informe o ID do usuário: ");
        Long exemplarId = lerLong("Informe o ID do exemplar: ");

        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);

        Exemplar exemplar = new Exemplar();
        exemplar.setId(exemplarId);

        Emprestimo emprestimo = Emprestimo.builder()
                .usuario(usuario)
                .exemplar(exemplar)
                .dataEmprestimo(LocalDateTime.now())
                .dataDevolucaoPrevista(LocalDateTime.now().plusDays(7))
                .status(StatusEmprestimoEnum.ATIVO)
                .build();

        emprestimoService.emprestarExemplar(emprestimo);
    }

    private void devolverExemplar(EmprestimoService emprestimoService) {
        MenuUtil.limparConsole();
        System.out.println("=== DEVOLVER EXEMPLAR ===");
        Long emprestimoId = lerLong("Informe o ID do empréstimo: ");
        emprestimoService.devolverExemplar(emprestimoId);
    }

    private void renovarEmprestimo(EmprestimoService emprestimoService) {
        MenuUtil.limparConsole();
        System.out.println("=== RENOVAR EMPRÉSTIMO ===");
        Long emprestimoId = lerLong("Informe o ID do empréstimo: ");
        emprestimoService.renovarExemplar(emprestimoId);
    }

    private void calcularMulta(EmprestimoService emprestimoService) {
        MenuUtil.limparConsole();
        System.out.println("=== CALCULAR MULTA ===");
        Long emprestimoId = lerLong("Informe o ID do empréstimo: ");
        emprestimoService.calcularMulta(emprestimoId);
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
    private static void listarEmprestimos(EmprestimoService emprestimoService) {
    MenuUtil.limparConsole();
    System.out.println("=== LISTAR EMPRÉSTIMOS ===");
    emprestimoService.listar();
    }
}