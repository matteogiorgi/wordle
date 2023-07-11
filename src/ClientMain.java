import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;


public class ClientMain {

    private static final String PATH_CONF = "lib/CLIENT.conf";
    private static ClientSetup clientProperties;


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

        // alloco le seguenti risorse dentro un try-with-resources:
        // Socket, Scanner per l'input, PrintWriter per l'output
        // ---
        // Da notare che un return all'interno del try-with-resources, è solo zucchero sintattico:
        // la JVM chiude le risorse correttamente spostando il return alla fine del blocco try.
        // (https://stackoverflow.com/questions/22947755/try-with-resources-and-return-statements-in-java)
        try (Socket socket = new Socket(clientProperties.getHostname(), clientProperties.getPort());
             Scanner input = new Scanner(socket.getInputStream());
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
             Scanner tastiera = new Scanner(System.in, "UTF-8")) {
            System.out.println("=== CONNESSIONE EFFETTUATA ===");
            // ---
            // leggo i messaggi inviati dal server e li stampo a video
            // leggo i comandi da tastiera e li invio al server
            boolean exitStatus = true;
            for (String inputLine, command; !socket.isClosed() && !socket.isInputShutdown() && input.hasNextLine();) {
                inputLine = input.nextLine();
                exitStatus = inputLine.matches("^(Arrivederci|Main-menu WORDLE).*");
                // ---
                System.out.println(inputLine);
                System.out.print("> ");
                if (!socket.isOutputShutdown() && tastiera.hasNextLine()) {
                    output.println(command = tastiera.nextLine().trim().toLowerCase());
                    // ---
                    // se il comando è "exit" chiudo
                    // gli stream di I/O e la socket
                    if (exitStatus && command.equals("exit")) {
                        System.out.println(input.nextLine());
                        return;
                    }
                }
            }
            // ---
            System.out.println("Il server ha interrotto la connessione");
        } catch (IOException e) {
            System.err.printf("Errore durante l'apertura della socket o degli stream di I/O\n");
            e.printStackTrace();
        }
    }  // main

}  // ClientMain
