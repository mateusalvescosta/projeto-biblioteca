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

    // Inicializa o menu de empréstimos e gerencia o fluxo de interação com o usuário
    public EmprestimoMenu(Scanner scanner) {
        this.scanner = scanner;
        System.out.println("==========================================");
        System.out.println("   SISTEMA DE EMPRÉSTIMOS COM JPA         ");
        System.out.println("==========================================");

        ManagerFactory managerFactory = new ManagerFactory("SQLitePU");
        EmprestimoService emprestimoService = new EmprestimoService(managerFactory.get());

        int opcao;
        do {
            exibirMenu();
            opcao = MenuUtil.lerInteiro(this.scanner, "Escolha uma opção: ");

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
        managerFactory.close();
    }

    // Exibe as opções disponíveis no menu de empréstimos
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

    // Coleta o ID do usuário e do exemplar e aciona o empréstimo com prazo de 7 dias
    private void emprestarExemplar(EmprestimoService emprestimoService) {
        MenuUtil.limparConsole();
        System.out.println("=== REALIZAR EMPRÉSTIMO ===");

        Long usuarioId = MenuUtil.lerLong(this.scanner, "Informe o ID do usuário: ");
        Long exemplarId = MenuUtil.lerLong(this.scanner, "Informe o ID do exemplar: ");

        // Monta os objetos de referência com os IDs informados
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);

        Exemplar exemplar = new Exemplar();
        exemplar.setId(exemplarId);

        // Cria o empréstimo com data atual e prazo de devolução de 7 dias
        Emprestimo emprestimo = Emprestimo.builder()
                .usuario(usuario)
                .exemplar(exemplar)
                .dataEmprestimo(LocalDateTime.now())
                .dataDevolucaoPrevista(LocalDateTime.now().plusDays(7))
                .status(StatusEmprestimoEnum.ATIVO)
                .build();

        emprestimoService.emprestarExemplar(emprestimo);
    }

    // Coleta o ID do empréstimo e aciona a devolução do exemplar
    private void devolverExemplar(EmprestimoService emprestimoService) {
        MenuUtil.limparConsole();
        System.out.println("=== DEVOLVER EXEMPLAR ===");
        Long emprestimoId = MenuUtil.lerLong(this.scanner, "Informe o ID do empréstimo: ");
        emprestimoService.devolverExemplar(emprestimoId);
    }

    // Coleta o ID do empréstimo e aciona a renovação do prazo
    private void renovarEmprestimo(EmprestimoService emprestimoService) {
        MenuUtil.limparConsole();
        System.out.println("=== RENOVAR EMPRÉSTIMO ===");
        Long emprestimoId = MenuUtil.lerLong(this.scanner, "Informe o ID do empréstimo: ");
        emprestimoService.renovarExemplar(emprestimoId);
    }

    // Coleta o ID do empréstimo e aciona o cálculo e registro da multa
    private void calcularMulta(EmprestimoService emprestimoService) {
        MenuUtil.limparConsole();
        System.out.println("=== CALCULAR MULTA ===");
        Long emprestimoId = MenuUtil.lerLong(this.scanner, "Informe o ID do empréstimo: ");
        emprestimoService.calcularMulta(emprestimoId);
    }

    // Exibe todos os empréstimos ativos, renovados ou atrasados
    private static void listarEmprestimos(EmprestimoService emprestimoService) {
        MenuUtil.limparConsole();
        System.out.println("=== LISTAR EMPRÉSTIMOS ===");
        emprestimoService.listarEmprestimos();
    }
}