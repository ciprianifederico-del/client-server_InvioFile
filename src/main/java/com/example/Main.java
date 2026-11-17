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
            ss.close();          // connessione chiusa, niente da fare
            s.close();
            return;
        } else if (richiesta.trim().isEmpty()) {
            out.println("ERR SYNTAX");
            ss.close();
            s.close();
            return;
        }

        String[] parti = richiesta.trim().split("\\s+");
        File cartella = new File("documenti");
        File file = new File(cartella, parti[0]);   // il nome file arriva dalla richiesta

        System.out.println(file.getAbsolutePath()); // utile durante le prove

        boolean presente = file.isFile();
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
            // qui lo stato "OK FILE" è già stato inviato:
            // segnaliamo l'errore solo dopo il contenuto
            out.println("ERR READ");
        }

        ss.close();
        s.close();
    }
}