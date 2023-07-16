import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.Scanner;


public class ClientMain {

    private static final String PATH_CONF = "lib/CLIENT.conf";
    private static ClientSetup clientProperties = null;
    private static Thread multicastListener = null;
    private static LinkedList<String> sharedNotifications = new LinkedList<>();


    private static Runnable shareHook = new Runnable() {
        @Override
        public void run() {
            try (MulticastSocket multicastSocket = new MulticastSocket(clientProperties.getMulticastGroupPort())) {
                multicastSocket.setSoTimeout(1000);
                DatagramPacket sharedRequest = null;

                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        sharedRequest = new DatagramPacket(new byte[8192], 8192);
                        multicastSocket.receive(sharedRequest);
                        // ---
                        sharedNotifications.add(new String(sharedRequest.getData(), 0, sharedRequest.getLength(), "UTF-8"));
                        System.out.println("[MULTICAST] nuova notifica");
                    } catch (SocketTimeoutException e) {
                        if (Thread.currentThread().isInterrupted()) {
                            break;
                        }
                    }
                }
            } catch (IOException ex) {
                System.err.println("[ERROR] Errore durante la ricezione di una notifica.");
                ex.printStackTrace();
            }
        }
    };


    public static void main(String[] args) {
        // leggo file configurazione client
        try {
            clientProperties = new ClientSetup(PATH_CONF);
        } catch (FileNotFoundException e) {
            System.err.printf("[ERROR] file configurazione %s non trovato\n", PATH_CONF);
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            System.err.printf("[ERROR] lettura file configurazione %s fallita\n", PATH_CONF);
            e.printStackTrace();
            System.exit(1);
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
                if (inputLine.matches("^\\[EXIT\\].*")) {
                    return;
                }
                // ---
                if (inputLine.matches("^\\[STAT (?!guessd).*\\].*")) {
                    continue;
                }
                // ---
                if (inputLine.matches("^\\[LOGIN DONE\\].*")) {
                    multicastListener = new Thread(shareHook);
                    multicastListener.start();
                }
                // ---
                if (inputLine.matches("^\\[LOGOUT DONE\\].*")) {
                    multicastListener.interrupt();
                }
                // ---
                System.out.print("> ");
                for (String inputCommand; !socket.isOutputShutdown() && tastiera.hasNextLine(); System.out.print("> ")) {
                    inputCommand = tastiera.nextLine().trim().toLowerCase();
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
