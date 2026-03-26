package br.unisales.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DataService {
    //método para data e hora
    public static String data(){
        LocalDateTime data = LocalDateTime.now();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return data.format(formato);
    }
}
