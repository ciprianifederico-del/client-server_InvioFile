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
import java.util.Arrays;

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

        String[] parti = richiesta.trim().split("\\s+");

        // token = 0 se vuota, 1 se solo il nome file, 2 se "LEGGI nomefile"
        if (parti.length == 0 || parti[0].isEmpty()) {
            out.println("ERR SYNTAX");
            ss.close();
            s.close();
            return;
        }

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
            File prova = new File("documenti", parti[0]);
            if (prova.isFile()) {
                nomeFile = parti[0];
            } else {
                // non è un file esistente e non è un comando noto
                out.println("ERR UNKNOWNCMD");
                ss.close();
                s.close();
                return;
            }
        }

        File cartella = new File("documenti");
        File[] files = cartella.listFiles();
        boolean presente = Arrays.asList(files).contains(nomeFile);
        if (!presente) {
            out.println("ERR MISSING");
            ss.close();
            s.close();
            return;
        }

        try (BufferedReader lettoreFile = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            out.println("OK FILE");
            out.println(); // riga vuota

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