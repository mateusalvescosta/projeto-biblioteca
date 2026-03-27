package br.unisales.datastructures;
import java.util.Vector;

public class Vetor {
    public static void main(String[] args) {

        Vector<Integer> vetor = new Vector<>();

        vetor.add(10);
        vetor.add(20);

        System.out.println(vetor.get(0)); // 10
    }
}