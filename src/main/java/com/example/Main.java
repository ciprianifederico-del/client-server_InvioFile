package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Main {
    public static void main(String[] args) throws UnknownHostException, IOException {
        Socket socket = new Socket("127.0.0.1", 3000);

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        // Invia la richiesta: il nome del file (con eventuali argomenti)
        // In un programma reale qui leggeresti da tastiera o dagli args
        String richiesta = "documenti.txt";      // <-- file da chiedere al server
        out.println(richiesta);

        // Legge la riga di stato
        String stato = in.readLine();
        if (stato == null) {
            System.out.println("Server chiuso la connessione senza rispondere");
        } else if (stato.equals("OK FILE")) {
            in.readLine(); // salta la riga vuota

            System.out.println("Contenuto del file:");
            String riga;
            while ((riga = in.readLine()) != null) {
                System.out.println(riga);
            }
        } else {
            // "ERR SYNTAX", "ERR MISSING", ...
            System.out.println("Errore dal server: " + stato);
        }

        socket.close();
    }
}