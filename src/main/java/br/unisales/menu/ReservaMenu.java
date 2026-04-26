package br.unisales.menu;

import java.util.Scanner;

import br.unisales.database.table.Livro;
import br.unisales.database.table.Reserva;
import br.unisales.manager_factory.ManagerFactory;
import br.unisales.menu.util.MenuUtil;
import br.unisales.service.CatalogoService;
import br.unisales.service.ReservaService;

public final class ReservaMenu {
    private final Scanner scanner;

    // Inicializa o menu de reservas e gerencia o fluxo de interação com o usuário
    public ReservaMenu(Scanner scanner) {
        this.scanner = scanner;
        System.out.println("==========================================");
        System.out.println("   SISTEMA DE RESERVAS COM JPA            ");
        System.out.println("==========================================");

        ManagerFactory managerFactory = new ManagerFactory("SQLitePU");
        ReservaService reservaService = new ReservaService(managerFactory.get());
        CatalogoService catalogoService = new CatalogoService(managerFactory.get());

        int opcao;
        do {
            exibirMenu();
            opcao = MenuUtil.lerInteiro(this.scanner, "Escolha uma opção: ");

            switch (opcao) {
                case 1 -> reservarLivro(reservaService, catalogoService);
                case 2 -> cancelarReserva(reservaService);
                case 3 -> atenderProximaReserva(reservaService);
                case 4 -> buscarReservas(reservaService);
                case 100 -> System.out.println("Voltando para o menu principal...");
                default -> System.out.println("Opção inválida. Tente novamente.");
            }
            System.out.println();
        } while (opcao != 100);
        managerFactory.close();
    }

    // Exibe as opções disponíveis no menu de reservas
    private static void exibirMenu() {
        System.out.println("--------------- MENU ----------------");
        System.out.println("1 - Reservar livro");
        System.out.println("2 - Cancelar reserva");
        System.out.println("3 - Atender próxima reserva");
        System.out.println("4 - Buscar reservas por livro");
        System.out.println("100 - Voltar");
        System.out.println("-------------------------------------");
    }

    // Busca o livro pelo ISBN, coleta o ID do usuário e aciona a reserva
    private void reservarLivro(ReservaService reservaService, CatalogoService catalogoService) {
        MenuUtil.limparConsole();
        System.out.println("=== RESERVAR LIVRO ===");
        String isbn = MenuUtil.lerTexto(this.scanner, "Informe o ISBN do livro: ");

        // Valida se o livro existe antes de prosseguir
        Livro livro = catalogoService.buscarLivroPorIsbn(isbn);
        if (livro == null) {
            System.out.println("Livro não encontrado.");
            return;
        }

        System.out.println("Livro encontrado: " + livro.getTitulo());
        Long usuarioId = MenuUtil.lerLong(this.scanner, "Informe o ID do usuário: ");

        // Monta a reserva apenas com os dados coletados, sem gerar ID no menu
        Reserva reserva = Reserva.builder()
                .usuarioId(usuarioId)
                .isbnLivro(isbn)
                .build();

        reservaService.reservarLivro(reserva);
    }

    // Coleta o ID da reserva e solicita confirmação antes de cancelá-la
    private void cancelarReserva(ReservaService reservaService) {
        MenuUtil.limparConsole();
        System.out.println("=== CANCELAR RESERVA ===");
        Long id = MenuUtil.lerLong(this.scanner, "Informe o ID da reserva: ");

        // Solicita confirmação do usuário antes de cancelar
        String confirmacao = MenuUtil.lerTexto(this.scanner, "Deseja realmente cancelar esta reserva? (S/N): ");
        if (confirmacao.equalsIgnoreCase("S")) {
            reservaService.cancelarReserva(id);
        } else {
            System.out.println("Cancelamento abortado.");
        }
    }

    // Coleta o ISBN e exibe o próximo usuário da fila de reservas do livro
    private void atenderProximaReserva(ReservaService reservaService) {
        MenuUtil.limparConsole();
        System.out.println("=== ATENDER PRÓXIMA RESERVA ===");
        String isbn = MenuUtil.lerTexto(this.scanner, "Informe o ISBN do livro: ");
        reservaService.atenderProximaReserva(isbn);
    }

    // Coleta o título e exibe as reservas encontradas para o livro informado
    private void buscarReservas(ReservaService reservaService) {
        MenuUtil.limparConsole();
        System.out.println("=== BUSCAR RESERVAS ===");
        String titulo = MenuUtil.lerTexto(this.scanner, "Informe o título do livro (ou parte dele): ");
        reservaService.buscarReservaPorTituloLivro(titulo);
    }
}