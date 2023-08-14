import java.io.EOFException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;


/**
 * Classe che implementa un thread per la gestione dell'interazione tra il server e un client.
 * <br>
 * Game propone una interfaccia testuala al client composta di tre fasi:
 * <ul>
 * <li><code>[MAIN SESSION]</code>: fase iniziale in cui l'utente può</li>
 *     <ul>
 *     <li>uscire dal programma (<code>exit</code>)</li>
 *     <li>registrarsi al gioco (<code>register</code>)</li>
 *     <li>cancellarsi dal gioco (<code>remove</code>)</li>
 *     <li>autenticarsi al gioco (<code>login</code>)</li>
 *     </ul>
 * <li><code>[LOGIN SESSION]</code>: fase in cui l'utente può</li>
 *     <ul>
 *     <li>giocare una nuova partita (<code>playwordle</code>)</li>
 *     <li>ricevere le proprie statistiche (<code>sendmestat</code>)</li>
 *     <li>condividere le proprie statistiche (<code>sharemestat</code>)</li>
 *     <li>effettuare il logout (<code>logout</code>)</li>
 *     </ul>
 * <li><code>[GAME SESSION]</code>: fase in cui l'utente può</li>
 *     <ul>
 *     <li>interrompere la partita in corso (<i>quit!</i>)</li>
 *     <li>inserire una stringa (<i>Guessed-Word</i>)</li>
 *     </ul>
 * </ul>
 */
public class Game implements Runnable {

    /**
     * Variabili utili.
     *
     * - socket: Socket di connessione per la comunicazione tra Game e Client
     * - userList: oggetto (UserList) che memorizza gli utenti registrati al gioco
     * - wordList: oggetto (WordList) che memorizza le parole da indovinare
     * - multicastSender: oggetto (MulticastSender) che invia le notifiche sul multicast
     */
    private final Socket socket;
    private final UserList userList;
    private final WordList wordList;
    private final MulticastSender multicastSender;


    /**
     * Variabili utili.
     *
     * - input: Scanner per la lettura dalla socket
     * - output: PrintWriter per la scrittura sulla socket
     * - word: oggetto (Word) che memorizza la parola da indovinare
     */
    private Scanner input = null;
    private PrintWriter output = null;
    private Word word = null;


    /**
     * Costruttore della classe <code>Game</code>.
     *
     * @param socket  socket di connessione per la comunicazione tra Game e Client
     * @param userList  oggetto (UserList) che memorizza gli utenti registrati al gioco
     * @param wordList  oggetto (WordList) che memorizza le parole da indovinare
     * @param multicastSender  oggetto (MulticastSender) che invia le notifiche sul multicast
     */
    public Game(Socket socket, UserList userList, WordList wordList, MulticastSender multicastSender) {
        this.socket = socket;
        this.userList = userList;
        this.wordList = wordList;
        this.multicastSender = multicastSender;
    }


    /**
     * Metodo che gestisce la sessione di gioco.
     *
     * In questa fase l'utente può:
     * - interrompere la partita in corso (quit!)
     * - inserire una stringa (Guessed-Word)
     */
    private void gameSession(User user) {
        System.out.println("[GAME START] user " + user.getName() + ", nuova partita (" + word.getWord() + ")");
        output.println("[GAME START] user " + user.getName() + ", nuova partita: quit!|<tentativo>");
        // ---
        String tentativo = null;
        int contaTentativi = 0;
        // ---
        while (input.hasNextLine()) {
            tentativo = input.nextLine().trim().toLowerCase();
            contaTentativi++;
            switch (tentativo) {

                case "quit!":
                    // invio un messaggio di fine partita
                    // e restituisco il controllo a loginSession()
                    output.println("[GAME END] user " + user.getName() + ", partita interrotta");
                    user.update(false, contaTentativi);
                    return;

                default:
                    // analizzo la stringa inserita e invio la maschera in risposta
                    // (termino la partita in caso di vittoria/sconfitta)
                    if (tentativo.equals(word.getWord())) {
                        output.println("[GAME QUIT] complimenti user " + user.getName() + ", parola indovinata");
                        user.update(true, contaTentativi);
                        return;
                    }
                    // ---
                    if (contaTentativi == 12) {
                        output.println("[GAME QUIT] spiacente user " + user.getName() + ", tentativi esauriti");
                        user.update(false, contaTentativi);
                        return;
                    }
                    // ---
                    if (!wordList.containsWord(tentativo)) {
                        output.println("  " + "-".repeat(tentativo.length()));
                        contaTentativi--;
                        continue;
                    }
                    // ---
                    output.println("  " + word.getMask(tentativo));

            }
        }  // while
    }  // gameSession


    /**
     * Metodo che gestisce la sessione di login.
     *
     * In questa fase l'utente può:
     * - giocare una nuova partita (playwordle)
     * - ricevere le proprie statistiche (sendmestat)
     * - condividere le proprie statistiche (share)
     * - effettuare il logout (logout)
     */
    private void loginSession(User user) {
        System.out.println("[LOGIN DONE] user " + user.getName());
        output.println("[LOGIN DONE] user '" + user.getName() + "': playwordle|sendmestat|share|showmesharing|logout");
        // ---
        while (input.hasNextLine()) {
            switch (input.nextLine().trim().toLowerCase()) {

                case "playwordle":
                    // estraggo la parola da giocare:
                    // se non giocata, lancio sessione di gioco
                    // altrimenti torno al while
                    word = wordList.getCurrentWord();
                    if (word.addUser(user.getName())) {
                        gameSession(user);
                    } else {
                        output.println("[WARNING] user " + user.getName() + " attendere nuova parola");
                    }
                    continue;

                case "sendmestat":
                    // invio contenuto (utile) della mappa utente
                    // torno al while
                    output.println("[STAT '" + user.getName() + "']");
                    output.println("[STAT giocate] " + user.getGiocate());
                    output.println("[STAT vinte] " + user.getVinte());
                    output.println("[STAT streaklast] " + user.getStreakLast());
                    output.println("[STAT streakmax] " + user.getStreakMax());
                    output.println("[STAT guessd] " + user.getGuessDistribution().toString());
                    continue;

                case "share":
                    multicastSender.readNotification(
                        "[STAT '" + user.getName() + "']\n" +
                        "[STAT giocate] " + user.getGiocate() + "\n" +
                        "[STAT vinte] " + user.getVinte() + "\n" +
                        "[STAT streaklast] " + user.getStreakLast() + "\n" +
                        "[STAT streakmax] " + user.getStreakMax() + "\n" +
                        "[STAT guessd] " + user.getGuessDistribution().toString()
                    );
                    output.println("[LOGIN SESSION] user " + user.getName() + ", share effettuato");
                    continue;

                case "logout":
                    // rimuovo l'utente dalla lista dei loggati
                    // restituisco il controllo a run()
                    userList.logoutUser(user.getName());
                    output.println("[LOGOUT DONE] arrivederci " + user.getName());
                    return;

                default:
                    // l'utente ha inserito un comando errato
                    // o non eseguibile in questo contesto
                    output.println("[LOGIN SESSION] comando non eseguibile: playwordle|sendmestat|share|showmesharing|logout");

            }
        }  // while
    }  // loginSession


    /**
     * Metodo run() del thread Game.
     * <br>
     * Il Thread che contiene Game gestisce la comunicazione tra Game e Client: prepara gli stream di I/O sulla socket,
     * invia un messaggio di benvenuto e rimane in attesa di un comando.
     * <p>
     * In questa fase l'utente può:
     * <ul>
     * <li>uscire dal programma (exit)</ol>
     * <li>registrarsi (register)</ol>
     * <li>cancellarsi (remove)</ol>
     * <li>effettuare il login (login)</ol>
     * </ul>
     */
    @Override
    public void run() {
        try {
            input = new Scanner(socket.getInputStream());
            output = new PrintWriter(socket.getOutputStream(), true);
            output.println("[MAIN SESSION] Welcome to WORDLE: register|remove|login|exit");
            // ---
            while (!socket.isClosed() && !socket.isInputShutdown() && input.hasNextLine()) {
                String inpuString = input.nextLine().trim().toLowerCase();
                if (inpuString.equals("exit")) {
                    // l'utente vuole uscire dal programma:
                    // invio un messaggio di disconnessione
                    // chiudo gli stream di I/O e la socket
                    System.out.println("[EXIT] " + socket.getInetAddress() + ":" + socket.getPort());
                    output.println("[EXIT] disconnessione effettuata");
                    // ---
                    input.close();
                    output.close();
                    socket.close();
                    // ---
                    // termino il thread
                    return;
                }

                try {
                    String[] splitCommand = inpuString.split(" ", 2);
                    String command = splitCommand[0].trim().toLowerCase();
                    // ---
                    String [] userInfo;
                    String username, password;
                    switch (command) {

                        case "register":
                            // l'utente vuole registrarsi:
                            // taglio l'argomento in due parti => username:password
                            // costruisco un nuovo utente e lo aggiungo alla lista
                            userInfo = splitCommand[1].split(":", 2);
                            username = userInfo[0].trim().toLowerCase();
                            password = userInfo[1].trim().toLowerCase();
                            // ---
                            if (userList.addUser(new HashMap<String, Object>() {{
                                put("user", username);
                                put("password", password);
                                put("giocate", 0);
                                put("vinte", 0);
                                put("streaklast", 0);
                                put("streakmax", 0);
                                put("guessd", new LinkedList<>());
                            }}) == null) {
                                System.out.println("[REGISTER] user " + username + " registrato");
                                output.println("[REGISTER] user " + username + " registrato");
                            } else {
                                output.println("[REGISTER] user " + username + " precedentemente registrato");
                            }
                            // ---
                            // torno al while(...)
                            // in attessa di un nuovo comando
                            continue;

                        case "remove":
                            // l'utente vuole cancellarsi:
                            // taglio l'argomento in due parti => username:password
                            // rimuovo l'utente dalla lista dei registrati
                            userInfo = splitCommand[1].split(":", 2);
                            username = userInfo[0].trim().toLowerCase();
                            password = userInfo[1].trim().toLowerCase();
                            // ---
                            User tempUser = userList.removeUser(username);
                            if (tempUser == null) {
                                output.println("[REMOVE] user " + username + " non registrato");
                                continue;
                            }
                            // ---
                            if (!tempUser.getPassword().equals(password)) {
                                output.println("[REMOVE] user " + username + " password errata");
                                userList.addUser(tempUser);
                                continue;
                            }
                            // ---
                            System.out.println("[REMOVE] user " + username + " rimosso");
                            output.println("[REMOVE] user " + username + " rimosso");
                            continue;

                        case "login":
                            // l'utente vuole effettuare il login:
                            // taglio l'argomento in due parti => username:password
                            // aggiungo l'utente alla lista dei loggati
                            userInfo = splitCommand[1].split(":", 2);
                            username = userInfo[0].trim().toLowerCase();
                            password = userInfo[1].trim().toLowerCase();
                            // ---
                            try {
                                if (userList.loginUser(username, password)) {
                                    // avvio la sessione di login
                                    loginSession(userList.getUser(username));
                                } else {
                                    output.println("[LOGIN FAILED] user " + username);
                                }
                            } catch (IllegalArgumentException e) {
                                output.println("[LOGIN FAILED] user " + username);
                            }
                            // ---
                            // torno al while(...)
                            // in attessa di un nuovo comando
                            continue;

                        default:
                            // questo blocco finally verrà eseguito solo nei casi di ArrayIndexOutOfBoundsException
                            // oppure quando l'utente ha inserito un comando che non sia [register|remove|login|exit]
                            output.println("[MAIN SESSION] comando non eseguibile: register|remove|login|exit");

                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    // l'utente ha inserito un comando errato/malformattato o non eseguibile in questo contesto
                    // che ha scatenato una ArrayIndexOutOfBoundsException quando ho provato a splittare
                    output.println("[MAIN SESSION] comando register|remove|login malformattato");
                }
            }  // while(...)
        } catch (EOFException e) {
            System.err.println("[MAIN SESSION] errore lettura stream");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("[MAIN SESSION] errore creazione stream o chiusura socket");
            e.printStackTrace();
        }
    }  // run

}  // class Game
