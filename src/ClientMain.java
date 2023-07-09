import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;


public class ClientMain {

    private static final String PATH_CONF = "lib/CLIENT.conf";
    private static ClientSetup clientProperties;
    // ---
    private static Scanner input;
    private static PrintWriter output;


    public static void main(String[] args) {
        // leggo il file di configurazione del client
        try {
            clientProperties = new ClientSetup(PATH_CONF);
        } catch (FileNotFoundException e) {
            System.err.printf("File di configurazione %s non trovato\n", PATH_CONF);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.printf("Errore durante la lettura del file di configurazione %s\n", PATH_CONF);
            e.printStackTrace();
        }

        // apro la socket del client
        // alloco gli stream di I/O
        try (Socket socket = new Socket(clientProperties.getHostname(), clientProperties.getPort());
             Scanner tastiera = new Scanner(System.in, "UTF-8")) {
            // ---
            input = new Scanner(socket.getInputStream());
            output = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("=== CONNESSIONE EFFETTUATA ===");
            // ---
            while (input.hasNextLine()) {
                System.out.println(input.nextLine());
                output.println(tastiera.nextLine());
            }
        } catch (IOException e) {
            System.err.printf("Errore durante l'apertura della socket\n");
            e.printStackTrace();
        }
    }  // main

}  // ClientMain
