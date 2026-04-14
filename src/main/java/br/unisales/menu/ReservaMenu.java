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

    public ReservaMenu(Scanner scanner) {
        this.scanner = scanner;
        System.out.println("==========================================");
        System.out.println("   SISTEMA DE RESERVAS COM JPA            ");
        System.out.println("==========================================");

        ManagerFactory emf = new ManagerFactory("SQLitePU");
        ReservaService reservaService = new ReservaService(emf.get());
        CatalogoService catalogoService = new CatalogoService(emf.get());

        int opcao;
        do {
            exibirMenu();
            opcao = lerInteiro("Escolha uma opção: ");

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
        emf.close();
    }

    private static void exibirMenu() {
        System.out.println("--------------- MENU ----------------");
        System.out.println("1 - Reservar livro");
        System.out.println("2 - Cancelar reserva");
        System.out.println("3 - Atender próxima reserva");
        System.out.println("4 - Buscar reservas por livro");
        System.out.println("100 - Voltar");
        System.out.println("-------------------------------------");
    }

    private void reservarLivro(ReservaService reservaService, CatalogoService catalogoService) {
        MenuUtil.limparConsole();
        System.out.println("=== RESERVAR LIVRO ===");
        String isbn = lerTexto("Informe o ISBN do livro: ");

        Livro livro = catalogoService.buscarPorIsbn(isbn);
        if (livro == null) {
            System.out.println("Livro não encontrado.");
            return;
        }

        System.out.println("Livro encontrado: " + livro.getTitulo());
        Long usuarioId = lerLong("Informe o ID do usuário: ");

        Reserva reserva = Reserva.builder()
                .id(reservaService.getNextId())
                .usuarioId(usuarioId)
                .isbnLivro(isbn)
                .build();

        reservaService.reservarLivro(reserva);
    }

    private void cancelarReserva(ReservaService reservaService) {
        MenuUtil.limparConsole();
        System.out.println("=== CANCELAR RESERVA ===");
        Long id = lerLong("Informe o ID da reserva: ");
        String confirmacao = lerTexto("Deseja realmente cancelar esta reserva? (S/N): ");
        if (confirmacao.equalsIgnoreCase("S")) {
            reservaService.cancelarReserva(id);
        } else {
            System.out.println("Cancelamento abortado.");
        }
    }

    private void atenderProximaReserva(ReservaService reservaService) {
        MenuUtil.limparConsole();
        System.out.println("=== ATENDER PRÓXIMA RESERVA ===");
        String isbn = lerTexto("Informe o ISBN do livro: ");
        reservaService.atenderProximaReserva(isbn);
    }

    private void buscarReservas(ReservaService reservaService) {
        MenuUtil.limparConsole();
        System.out.println("=== BUSCAR RESERVAS ===");
        String titulo = lerTexto("Informe o título do livro (ou parte dele): ");
        reservaService.buscarPorTituloLivro(titulo);
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

    private String lerTexto(String mensagem) {
        System.out.print(mensagem);
        return this.scanner.nextLine();
    }
}