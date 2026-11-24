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
import java.nio.file.Files;

public class Main {

    public static void main(String[] args) throws IOException {

        String[][] TIPI = {
            {"txt", "text/plain; charset=utf-8"},
            {"md",  "text/markdown; charset=utf-8"},
            {"csv", "text/csv; charset=utf-8"},
        };
        boolean ok = true;
        boolean badRequest = false;
        boolean notFound = false;
        boolean notImplemented = false;
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
            badRequest = true;   // corretto: prima era false e il 400 non partiva mai
            ok = false;
        }

        File cartella = new File("documenti");
        String nomeFile;

        if (parti[0].equalsIgnoreCase("LEGGI")) {
            if (parti.length < 2) {
                badRequest = true;   // idem
                ok = false;
                nomeFile = "";       // evita ArrayIndexOutOfBoundsException su parti[1]
            } else {
                nomeFile = parti[1];
            }
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
                notFound = true;
            } else {
                notImplemented = true;
            }
            ok = false;
        }

        //try (BufferedReader lettoreFile = new BufferedReader(
                //new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            byte[] dati = Files.readAllBytes(file.toPath());
            String contenuto = new String(dati, StandardCharsets.UTF_8);
            // Risponde OK con l'estensione reale del file trovato
                String estensione = nomeFile.substring(nomeFile.lastIndexOf('.') + 1);
                for(String[] tipo : TIPI) {
                    if (tipo[0].equalsIgnoreCase(estensione)) {
                        if (ok) out.println("200 OK");
                        else if (badRequest) out.println("400 Bad Request");
                        else if (notFound) out.println("404 Not Found");
                        else if (notImplemented) out.println("501 Not Implemented");
                        out.println("Content-Type: " + tipo[1]);
                        out.println("Content-Length: " + dati.length);
                        out.println();
                        out.print(contenuto);
                        out.flush();   // print() non svuota il buffer da solo
                        break;
                    }
                }



       // } catch (IOException e) {
        //    out.println("500 Internal Server Error ");
       // }

        ss.close();
        s.close();
    }
}