import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;


public class ServerMain {

    /**
     * Percorso del file di configurazione del server.
     */
    private static final String PATH_CONF = "lib/SERVER.conf";


    /**
     * Variabili statiche che memorizzano le informazioni del server:
     * - serverProperties: oggetto (ServerSetup) che memorizza le proprietà del server
     * - listaUtenti: oggetto (UserList) che memorizza gli utenti registrati al gioco
     * - listaParole: oggetto (WordList) che memorizza le parole da indovinare
     */
    private static ServerSetup serverProperties;
    private static UserList listaUtenti;
    private static WordList listaParole;


    /**
     * Variabili statiche che rappresentano la socket di benvenuto sulla quale fare la
     * accept() dei client e il thread pool che gestisce i client connessi
     */
    private static ServerSocket welcomeSocket;
    private static ExecutorService threadPool;


    /**
     * Runnable che gestisce la chiusura del server. Viene eseguito quando viene ricevuto il segnale di shutdown.
     * Viene chiesto all'utente di inserire un comando tra "exit", "quit" o "kill". In base al comando inserito,
     * il metodo chiude la socket di benvenuto, il thread pool e lo scheduler delle parole. Se il comando è "kill",
     * il server viene chiuso immediatamente. Se il comando non è riconosciuto, viene stampato un messaggio di errore.
     * Se si verifica un'eccezione durante la chiusura, viene stampato un messaggio di errore.
     */
    private static Runnable shutdownHook = new Runnable() {
        @Override
        public void run() {
            try {
                System.out.println("=== SERVER SHUTDOWN ===");
                welcomeSocket.close();
                // ricorda di gestire la shutdown del multicast:
                // multicastThreadPool.shutdown();
            } catch (IOException e) {
                System.err.println("Chiusura server-socket fallita");
                e.printStackTrace();
            } finally {
                threadPool.shutdown();
                listaParole.getSheduler().shutdown();
                try {
                    if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                        System.out.println("Forzo la chiusura del thread pool");
                        threadPool.shutdownNow();
                    }
                    if (!listaParole.getSheduler().awaitTermination(10, TimeUnit.SECONDS)) {
                        System.out.println("Forzo la chiusura dello scheduler");
                        listaParole.getSheduler().shutdownNow();
                    }
                } catch (InterruptedException e) {
                    System.err.println("Chiusura forzata dei threadpool");
                    e.printStackTrace();
                    if (!threadPool.isShutdown()) {
                        threadPool.shutdownNow();
                    }
                    if (!listaParole.getSheduler().isShutdown()) {
                        listaParole.getSheduler().shutdownNow();
                    }
                } finally {
                    try {
                        listaUtenti.setRegistrati(serverProperties.getPathJSON());
                    } catch (IOException e) {
                        System.err.println("Errore durante la scrittura del file JSON");
                        e.printStackTrace();
                    }
                }
            }
        }  // run
    };  // shutdownHook


    public static void main(String args[]) {
        // registro lo shutdown hook per gestire la chiusura del server
        Runtime.getRuntime().addShutdownHook(new Thread(shutdownHook));

        // Leggo il file di configurazione del server,
        // alloco la lista degli utenti (leggendo il file JSON)
        // e la lista delle parole (leggendo il file .txt)
        try {
            serverProperties = new ServerSetup(PATH_CONF);
            listaUtenti = new UserList(serverProperties.getPathJSON());
            listaParole = new WordList(serverProperties.getPathVocabulary(), serverProperties.getWordTimer());
        } catch (FileNotFoundException e) {
            System.err.printf("File di configurazione %s non trovato\n", PATH_CONF);
            e.printStackTrace();
            return;
        } catch (IOException e) {
            System.err.printf("Errore durante la lettura del file di configurazione %s\n", PATH_CONF);
            e.printStackTrace();
            return;
        }

        // Avvio il server sulla porta specificata nel file di configurazione
        // e mi metto in attesa di connessioni da parte dei client
        try {
            welcomeSocket = new ServerSocket(serverProperties.getPort());
            threadPool = Executors.newCachedThreadPool();
            System.out.println("=== SERVER ACCESO ===");
            // ---
            while (true) {
                Socket socket = null;
                try {
                    socket = welcomeSocket.accept();
                } catch (SocketException e) {
                    // se il server è stato chiuso,
                    // mi assicuro di uscire dal ciclo
                    break;
                } catch (IOException e) {
                    System.err.println("Errore durante la accept()");
                    e.printStackTrace();
                    continue;
                }
                // ---
                threadPool.execute(new Game(socket, listaUtenti, listaParole));
            }
        } catch (IOException e) {
            System.err.println("Errore sul server?");
            e.printStackTrace();
        }
    }  // main

}  // ServerMain
