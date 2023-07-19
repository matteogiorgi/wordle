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
     * serverProperties -> oggetto (ServerSetup) che memorizza le proprietà del server
     * listaUtenti      -> oggetto (UserList) che memorizza gli utenti registrati al gioco
     * listaParole      -> oggetto (WordList) che memorizza le parole da indovinare
     */
    private static ServerSetup serverProperties = null;
    private static UserList listaUtenti = null;
    private static WordList listaParole = null;


    /**
     * Variabili statiche che rappresentano:
     * welcomeSocket     -> ServerSocket di benvenuto sulla quale fare la accept() dei client,
     * socket            -> Socket di connessione per la comunicazione Game <-> Client,
     * threadPool        -> ExecutorService che gestisce i client connessi
     * multicastListener -> Thread che legge le notifiche e le invia sul multicast
     */
    private static ServerSocket welcomeSocket = null;
    private static Socket socket = null;
    private static ExecutorService threadPool = Executors.newCachedThreadPool();
    private static Thread multicastListener = null;


    /**
     * Runnable che gestisce la chiusura del server, eseguito quando viene ricevuto il segnale di shutdown.
     * Chiude la welcomeSocket, il threadPool, il multicastListener e il sheduler della listaParole.
     */
    private static Runnable shutdownHook = new Runnable() {
        @Override
        public void run() {
            try {
                System.out.println("=== SERVER SHUTDOWN ===");
                welcomeSocket.close();
            } catch (IOException e) {
                System.err.println("[ERROR] chiusura server-socket");
                e.printStackTrace();
            } finally {
                threadPool.shutdown();
                multicastListener.interrupt();
                listaParole.getSheduler().shutdown();
                try {
                    if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                        System.out.println("[WARNING] forzo chiusura threadpool");
                        threadPool.shutdownNow();
                    }
                    if (!listaParole.getSheduler().awaitTermination(5, TimeUnit.SECONDS)) {
                        System.out.println("[WARNING] forzo chiusura scheduler");
                        listaParole.getSheduler().shutdownNow();
                    }
                } catch (InterruptedException e) {
                    System.err.println("[WARNING] chiusura forzata dei pool");
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
                        System.err.println("[ERROR] scrittura file JSON fallita");
                        e.printStackTrace();
                    }
                }
            }
        }  // run
    };  // shutdownHook


    /**
     * Main del server.
     * Legge il file di configurazione, crea le strutture dati
     * e si mette in attesa di connessioni.
     */
    public static void main(String args[]) {
        // leggo file di configurazione server,
        // alloco lista utenti (leggendo il file JSON)
        // e lista parole (leggendo il file .txt)
        try {
            serverProperties = new ServerSetup(PATH_CONF);
            listaUtenti = new UserList(serverProperties.getPathJSON());
            listaParole = new WordList(serverProperties.getPathVocabulary(), serverProperties.getWordTimer());
        } catch (FileNotFoundException e) {
            System.err.printf("File di configurazione %s non trovato\n", PATH_CONF);
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            System.err.printf("Errore durante la lettura del file di configurazione %s\n", PATH_CONF);
            e.printStackTrace();
            System.exit(1);
        }

        // avvio il server sulla porta specificata nel file di configurazione
        // e mi metto in attesa di connessioni da parte dei client
        try {
            welcomeSocket = new ServerSocket(serverProperties.getPort());
        } catch (IOException e) {
            System.err.println("Errore creazione welcome socket");
            e.printStackTrace();
            System.exit(1);
        }

        // registro shutdown-hook la chiusura
        // e shareHook per condivisione dati
        Runtime.getRuntime().addShutdownHook(new Thread(shutdownHook));
        MulticastSender multicastSender = new MulticastSender(serverProperties.getMulticastGroupPort(), serverProperties.getMulticastGroupAddress());
        multicastListener = new Thread(multicastSender);
        multicastListener.start();

        // server finalmente attivo
        // in attesa delle connessioni
        System.out.println("=== SERVER ACCESO ===");
        while (true) {
            try {
                socket = welcomeSocket.accept();
            } catch (SocketException e) {
                // se il server è stato chiuso,
                // mi assicuro di uscire dal ciclo
                break;
            } catch (IOException e) {
                System.err.println("Server interrotto durante la accept()");
                e.printStackTrace();
                continue;
            }
            // ---
            if (socket.isBound()) {
                System.out.println("Connesso con: " + socket.getInetAddress() + ":" + socket.getPort());
                threadPool.execute(new Game(socket, listaUtenti, listaParole, multicastSender));
            }
        }
    }  // main

}  // ServerMain
