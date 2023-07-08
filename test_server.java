import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.BufferedReader;
import java.util.concurrent.TimeUnit;


public class ServerMain {
    /**
     * Variabile statica che contiene il path del file di configurazione del server
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
     * Legge l'ID del client dal socket passato come parametro.
     * @param socket Il socket dal quale leggere l'ID del client
     * @return       L'ID del client letto dal socket
     * @throws IOException se si verifica un errore durante la lettura dal socket
     */
    private static String readClientName(Socket socket) throws IOException {
        InputStream inputStream = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        return reader.readLine();
    }


    /**
     * Runnable che gestisce la chiusura del server. Viene eseguito quando viene ricevuto il segnale di shutdown.
     * Viene chiesto all'utente di inserire un comando tra "exit", "quit" o "kill". In base al comando inserito,
     * il metodo chiude la socket di benvenuto, il thread pool e lo scheduler delle parole. Se il comando è "kill",
     * il server viene chiuso immediatamente. Se il comando non è riconosciuto, viene stampato un messaggio di errore.
     * Se si verifica un'eccezione durante la chiusura, viene stampato un messaggio di errore.
     */
    private static void shutdownHook() {
        try {
            System.out.println("=== SERVER SHUTDOWN ===");
            welcomeSocket.close();
            threadPool.shutdown();
            listaParole.getSheduler().shutdown();
            // ricorda di gestire la shutdown del multicast:
            // mulricastThreadPool.shutdown();
            if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                System.out.println("Forzo la chiusura del thread pool");
                threadPool.shutdownNow();
            }
            if (!listaParole.getSheduler().awaitTermination(10, TimeUnit.SECONDS)) {
                System.out.println("Forzo la chiusura dello scheduler");
                listaParole.getSheduler().shutdownNow();
            }
            System.exit(0);
        } catch (InterruptedException e) {
            System.err.println("Chiusura forzata");
            if (!threadPool.isShutdown()) {
                threadPool.shutdownNow();
            }
            if (!listaParole.getSheduler().isShutdown()) {
                listaParole.getSheduler().shutdownNow();
            }
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Errore durante la chiusura del server");
            e.printStackTrace();
        }
    }


    public static void main(String args[]) {
        // registro lo shutdown hook per gestire la chiusura del server
        Runtime.getRuntime().addShutdownHook(new Thread(ServerMain::shutdownHook));

        // Leggo il file di configurazione del server,
        // alloco la lista degli utenti (leggendo il file JSON)
        // e la lista delle parole (leggendo il file .txt)
        try {
            serverProperties = new ServerSetup(PATH_CONF);
            listaUtenti = new UserList(serverProperties.getPathJSON());
            listaParole = new WordList(serverProperties.getPathVocabulary(), serverProperties.getWordTimer());
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
            while (true) {
                Socket socket = welcomeSocket.accept();           // accetto la connessione (bloccante)
                String clientName = readClientName(socket);       // leggo il nome del client
                User user = listaUtenti.getUser(clientName);      // recupero l'utente
                Word word = listaParole.getCurrentWord();         // recupero la parola corrente
                threadPool.execute(new Game(socket, user, word)); // lancio una istanza di gioco
            }
        } catch (IOException e) {
            System.err.println("Errore durante la creazione del socket");
            e.printStackTrace();
        }
    }
}
