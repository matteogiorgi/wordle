import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutorService;


public class ClientMain {

    private static final String PATH_CONF = "lib/CLIENT.conf";
    private static ClientSetup clientProperties;


    private static ExecutorService multicastListener = Executors.newSingleThreadExecutor();


    private static Runnable shareHook = new Runnable() {
        @Override
        public void run() {
            try {
                multicastListener.shutdown();
                multicastListener.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                System.err.println("[ERROR] chiusura multicast-listener");
                e.printStackTrace();
            }
        }
    };


    public static void main(String[] args) {
        // registro uno shutdown hook per
        // gestire la condivisione dei dati
        multicastListener.submit(new Thread(shareHook));

        // leggo il file di configurazione del client
        try {
            clientProperties = new ClientSetup(PATH_CONF);
        } catch (FileNotFoundException e) {
            System.err.printf("[ERROR] file configurazione %s non trovato\n", PATH_CONF);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.printf("[ERROR] lettura file configurazione %s fallita\n", PATH_CONF);
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
            for (String inputLine; !socket.isClosed() && !socket.isInputShutdown() && input.hasNextLine();) {
                inputLine = input.nextLine();
                System.out.println(inputLine);
                // ---
                // se il comando inviato era "sendmestas",
                // devo leggere 6 righe di output dal server
                if (inputLine.matches("^\\[EXIT\\] .*")) {
                    return;
                }
                // ---
                if (inputLine.matches("^\\[STAT (?!guessd).*\\] .*")) {
                    continue;
                }
                // ---
                System.out.print("> ");
                for (String inputCommand; tastiera.hasNextLine(); System.out.print("> ")) {
                    inputCommand = tastiera.nextLine().trim().toLowerCase();
                    if (!socket.isOutputShutdown()) {
                        break;
                    }
                    if (!inputCommand.isEmpty()) {
                        output.println(inputCommand);
                        break;
                    }
                }
            }
            // ---
            System.out.println("[SORRY] connessione interrotta :(");
        } catch (IOException e) {
            System.err.printf("[ERROR] apertura socket/stream fallita\n");
            e.printStackTrace();
        }
    }  // main

}  // ClientMain
