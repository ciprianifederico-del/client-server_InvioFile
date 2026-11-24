package com.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Main {

    public static void main(String[] args) throws IOException {

        ServerSocket s = new ServerSocket(3000);
        System.out.println("Server in ascolto sulla porta 3000...");

        Socket ss = s.accept();
        System.out.println("Client connesso");

        BufferedReader in = new BufferedReader(new InputStreamReader(ss.getInputStream()));
        PrintWriter out = new PrintWriter(ss.getOutputStream(), true);

        String richiesta = in.readLine();
        if (richiesta == null) {
            ss.close();
            s.close();
            return;
        }

        // Divide la richiesta in token separati da spazi
        String[] parti = richiesta.trim().split("\\s+");

        if (parti.length == 0 || parti[0].isEmpty()) {
            out.println("ERR SYNTAX");
            ss.close();
            s.close();
            return;
        }

        File cartella = new File("documenti");
        String nomeFile;

        if (parti[0].equalsIgnoreCase("LEGGI")) {
            if (parti.length < 2) {
                out.println("ERR SYNTAX");   // "LEGGI" senza nome file
                ss.close();
                s.close();
                return;
            }
            nomeFile = parti[1];
        } else {
            // nessun comando: il primo token è direttamente il nome del file
            nomeFile = parti[0];
        }

        File file = new File(cartella, nomeFile);

        // Se il nome non esiste e non ha estensione, cerca nella cartella
        // un file il cui nome base corrisponda
        if (!file.isFile() && !nomeFile.contains(".")) {

            File[] files = cartella.listFiles(); // può essere null

            if (files != null) {
                for (File f : files) {

                    if (!f.isFile()) {
                        continue;
                    }

                    String nomeCompleto = f.getName();
                    String nomeSenzaEstensione;

                    if (nomeCompleto.contains(".")) {
                        nomeSenzaEstensione = nomeCompleto.substring(0, nomeCompleto.lastIndexOf('.'));
                    } else {
                        nomeSenzaEstensione = nomeCompleto;
                    }

                    if (nomeSenzaEstensione.equalsIgnoreCase(nomeFile)) {
                        // trovato: aggiorno nome e file col nome completo
                        nomeFile = nomeCompleto;
                        file = f;
                        break;
                    }
                }
            }
        }

        if (!file.isFile()) {
            if (parti[0].equalsIgnoreCase("LEGGI")) {
                out.println("ERR MISSING");
            } else {
                out.println("ERR UNKNOWNCMD");
            }
            ss.close();
            s.close();
            return;
        }

        try (BufferedReader lettoreFile = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            // Risponde OK con l'estensione reale del file trovato
            if (nomeFile.contains(".")) {
                String estensione = nomeFile.substring(nomeFile.lastIndexOf('.') + 1);
                out.println("OK FILE ," + "Type " + estensione);
            } else {
                out.println("OK FILE");
            }
            out.println(); // riga vuota di separazione

            String riga;
            while ((riga = lettoreFile.readLine()) != null) {
                out.println(riga);
            }

        } catch (IOException e) {
            out.println("ERR READ");
        }

        ss.close();
        s.close();
    }
}