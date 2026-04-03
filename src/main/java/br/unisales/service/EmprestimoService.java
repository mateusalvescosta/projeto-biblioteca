package br.unisales.service;
import br.unisales.database.table.Emprestimo;

import java.util.ArrayList;
import java.time.LocalDateTime;

public class EmprestimoService {
    private ArrayList<Emprestimo> emprestimos;
    public EmprestimoService() {
        emprestimos = new ArrayList<>();
    }

    public void emprestarExemplar(long usuarioId, long exemplarId){
        Emprestimo emp = new Emprestimo(); //
        emp.setUsuarioId(usuarioId);
        emp.setExemplarId(exemplarId);
        emp.setDataDevolucaoPrevista(LocalDateTime.now().plusDays(7));
        emp.setDataDevolucao(null);
        emp.setDataEmprestimo(LocalDateTime.now());
        emprestimos.add(emp); //
    }

    public void devolverExemplar(long exemplarId){
        for(Emprestimo emp : emprestimos){ //para cada emprestimo da lista

            boolean encontrado = false;
            if(emp.getExemplarId() == exemplarId){
                emp.setDataDevolucao(LocalDateTime.now());
                System.out.println("Livro devolvido!");
                encontrado = true;
                break;
            }
            if (!encontrado){
                System.out.println("Livro não encontrado!");
            }
        }
    }

    public void renovarExemplar (long exemplarId){
        for(Emprestimo emp : emprestimos){
            if(emp.getExemplarId() == exemplarId){
                
            }
        }
    }
}