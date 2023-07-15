import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
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
    private static ServerSetup serverProperties = null;
    private static UserList listaUtenti = null;
    private static WordList listaParole = null;


    /**
     * Variabili statiche che rappresentano:
     * la socket di benvenuto sulla quale fare la accept() dei client,
     * la socket di connessione per la comunicazione Game <-> Client,
     * il thread pool che gestisce i client connessi
     */
    private static ServerSocket welcomeSocket = null;
    private static Socket socket = null;
    private static ExecutorService threadPool = Executors.newCachedThreadPool();
    private static ExecutorService multicastListener = Executors.newSingleThreadExecutor();


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
            } catch (IOException e) {
                System.err.println("[ERROR] chiusura server-socket");
                e.printStackTrace();
            } finally {
                threadPool.shutdown();
                multicastListener.shutdown();
                listaParole.getSheduler().shutdown();
                try {
                    if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                        System.out.println("[WARNING] forzo chiusura threadpool");
                        threadPool.shutdownNow();
                    }
                    if (!multicastListener.awaitTermination(10, TimeUnit.SECONDS)) {
                        System.out.println("[WARNING] forzo chiusura multicastlistener");
                        multicastListener.shutdownNow();
                    }
                    if (!listaParole.getSheduler().awaitTermination(10, TimeUnit.SECONDS)) {
                        System.out.println("[WARNING] forzo chiusura scheduler");
                        listaParole.getSheduler().shutdownNow();
                    }
                } catch (InterruptedException e) {
                    System.err.println("[WARNING] chiusura forzata dei pool");
                    e.printStackTrace();
                    if (!threadPool.isShutdown()) {
                        threadPool.shutdownNow();
                    }
                    if (!multicastListener.isShutdown()) {
                        multicastListener.shutdownNow();
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


    private static Runnable shareHook = new Runnable() {
        @Override
        public void run() {
            try (DatagramSocket datagramSocket = new DatagramSocket(serverProperties.getServerNotificationPort());
                 MulticastSocket multicastSocket = new MulticastSocket(serverProperties.getMulticastGroupPort())) {
                // ---
                // definisco un gruppo multicast e
                // ne controllo la validità
                InetAddress multicastGroup = InetAddress.getByName(serverProperties.getMulticastGroupAddress());
                if (!multicastGroup.isMulticastAddress()) {
                    throw new IllegalArgumentException("[ERROR] indirizzo multicast non valido: " + multicastGroup.getHostAddress());
                }
                multicastSocket.joinGroup(multicastGroup);
                DatagramPacket shareRequest = null;

                // ricevo richiesta di condivisione (con i dati partita)
                // giro in multicast i risultati ricevuti dal client
                while (true) {
                    shareRequest = new DatagramPacket(new byte[8192], 8192);
                    datagramSocket.receive(shareRequest);
                    // ---
                    multicastSocket.send(new DatagramPacket(
                        shareRequest.getData(),
                        shareRequest.getLength(),
                        multicastGroup,
                        serverProperties.getMulticastGroupPort()
                    ));
                    System.out.println("[INFO] condivisione dati partita");
                }
            } catch (SocketException e) {
                System.err.println("[ERROR] creazione datagram-socket fallita");
                e.printStackTrace();
            } catch (UnknownHostException e) {
                System.err.println("[ERROR] indirizzo multicast non valido");
                e.printStackTrace();
            } catch (IOException e) {
                System.err.println("[ERROR] ricezione pacchetto fallita");
                e.printStackTrace();
            }
        }
    };


    public static void main(String args[]) {
        // registro uno shutdown hook per gestire la chiusura del server
        // e uno per la condivisione dei dati
        Runtime.getRuntime().addShutdownHook(new Thread(shutdownHook));
        multicastListener.submit(new Thread(shareHook));

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
            System.out.println("=== SERVER ACCESO ===");
            // ---
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
                    threadPool.execute(new Game(socket, listaUtenti, listaParole));
                }
            }  // while (true)
        } catch (IOException e) {
            System.err.println("Errore creazione welcome socket");
            e.printStackTrace();
            System.exit(1);
        }
    }  // main

}  // ServerMain
