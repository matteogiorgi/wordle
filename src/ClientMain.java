import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ClientMain {

    /**
     * Variabili statiche che rappresntano:
     * PATH_CONF         -> String che contiene il path del file di configurazione
     * clientProperties  -> oggetto (ClientSetup) che memorizza le proprietà del client
     * multicastListener -> Thread che rimane in ascolto delle notifiche sul multicast
     * multicastReceiver -> oggetto (MulticastReceiver) che memorizza le notifiche ricevute
     * logStatus         -> stato del log (false: non loggato, true: loggato)
     */
    private static final String PATH_CONF = "lib/CLIENT.conf";
    private static ClientSetup clientProperties = null;
    private static Thread multicastListener = null;
    private static MulticastReceiver multicastReceiver = null;
    private static boolean logStatus = false;


    /**
     * Main del client.
     * Legge il file di configurazione, si connette al server
     * e rimane in attesa di input da tastiera.
     */
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
        // da notare che un return all'interno del try-with-resources, è solo zucchero sintattico:
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
            // ---
            // il controllo sullo stato della socket non è necessario
            // perchè lato client non viene mai chiusa; sarà la hasNextLine()
            // a restituire false o lancianre una IllegalStateException
            // quando la socket verrà chiusa dal server
            // (https://stackoverflow.com/questions/25527212/detect-closed-socket)
            for (String inputLine; input.hasNextLine();) {
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
                    Pattern pattern = Pattern.compile("'(.*?)'");
                    Matcher matcher = pattern.matcher(inputLine);
                    // ---
                    multicastReceiver = new MulticastReceiver(
                        clientProperties.getMulticastGroupPort(),
                        clientProperties.getMulticastGroupAddress(),
                        matcher.find() ? matcher.group(1) : "unknown"
                    );
                    multicastListener = new Thread(multicastReceiver);
                    multicastListener.start();
                    logStatus = true;
                }
                // ---
                if (inputLine.matches("^\\[LOGOUT DONE\\].*")) {
                    multicastListener.interrupt();
                    logStatus = false;
                }
                // ---
                if (inputLine.matches("^\\[GAME START\\].*")) {
                    logStatus = false;
                }
                // ---
                if (inputLine.matches("^\\[GAME END\\].*")) {
                    logStatus = true;
                }
                // ---
                System.out.print("> ");
                for (String command; tastiera.hasNextLine(); System.out.print("> ")) {
                    command = tastiera.nextLine().trim().toLowerCase();
                    if (command.equals("showmesharing") && logStatus) {
                        while (!multicastReceiver.isEmpty()) {
                            System.out.println(multicastReceiver.poll());
                        }
                    } else {
                        output.println(command);
                        break;
                    }
                }
            }
            // ---
            System.out.println("[SORRY] connessione interrotta :(");
            if (multicastListener != null) {
                multicastListener.interrupt();
            }
        } catch (IllegalStateException e) {
            System.out.println("[SORRY] connessione interrotta :(");
            if (multicastListener != null) {
                multicastListener.interrupt();
            }
        }catch (IOException e) {
            System.err.printf("[ERROR] apertura socket/stream fallita\n");
            e.printStackTrace();
        }
    }  // main

}  // class ClientMain
