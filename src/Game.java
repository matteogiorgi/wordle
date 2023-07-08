import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

public class Game implements Runnable {

    private final Socket socket;
    private final UserList userList;
    private final WordList wordList;
    // ---
    private Scanner input;
    private PrintWriter output;
    private Word word;


    private void gameSession(User user) {
        System.out.println(user.getName() + " ha avviato una sessione di gioco");
        String tentativo = null;
        int contaTentativi = 0;
        // ---
        output.println("┌──────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐\n" +
                       "│                                              NUOVA PARTITA AVVIATA                                               │\n" +
                       "├──────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤\n" +
                       "│ • inserisci QUIT per terminare sessione di gioco                                                                 │\n" +
                       "│ • inserisci [STRINGA] per un nuovo tentativo, il server risponderà con una maschera di [X|+|?] dove:             │\n" +
                       "│     X indica che la lettera corrispondente non è presente nella parola da indovinare                             │\n" +
                       "│     + indica che la lettera corrispondente è presente nella parola da indovinare nella posizione corretta        │\n" +
                       "│     ? indica che la lettera corrispondente è presente nella parola da indovinare ma non nella posizione corretta │\n" +
                       "└──────────────────────────────────────────────────────────────────────────────────────────────────────────────────┘");
        while (input.hasNextLine()) {
            tentativo = input.nextLine();
            contaTentativi++;
            switch (tentativo) {
                case "QUIT":
                    // invio un messaggio di arrivederci
                    // e termino la sessione di gioco
                    output.println("Arrivederci " + user.getName() + "!\n");
                    return;

                default:
                    // analizzo la stringa inserita dall'utente
                    String mask = word.getMask(tentativo);
                    if (mask.matches("\\+*")) {
                        output.println("Complimenti, hai indovinato la parola!");
                        return;
                    } else if (contaTentativi == 12) {
                        output.println("Hai esaurito i tentativi, la parola da indovinare era: " + word.getWord());
                        return;
                    }

                    // invio la maschera in risposta
                    output.println(mask);
                    break;
            }
        }
    }


    private void loginSession(User user) {
        System.out.println(user.getName() + " ha effettuato il login");
        // ---
        while (input.hasNextLine()) {
            output.println("┌─────────────────────────────────────────────────────────────╴\n" +
                           "│ Ciao " + user.getName() + ", login effettuato con successo!  \n" +
                           "├─────────────────────────────────────────────────────────────┐\n" +
                           "│ 1. play WORDLE                                              │\n" +
                           "│ 2. logout                                                   │\n" +
                           "└─────────────────────────────────────────────────────────────┘");
            switch (input.nextLine()) {
                case "1":
                    // estraggo la parola da giocare:
                    // IF: non giocata => lancio una nuova sessione di gioco
                    // ELSE:           => torno al menù di login
                    word = wordList.getCurrentWord();
                    if (word.addUser(user.getName())) {
                        gameSession(user);
                    } else {
                        output.println(user.getName() + " hai già giocato questa parola!");
                    }

                    // torno al menu di login
                    break;

                case "2":
                    // invio un messaggio di arrivederci
                    // e termino la sessione di login
                    output.println("Arrivederci " + user.getName() + "!\n");
                    userList.logoutUser(user.getName());
                    return;

                default:
                    // il comando non è tra quelli previsti:
                    // invio un messaggio di errore e torno al menu di login
                    output.println("Comando non riconosciuto");
                    break;
            }
        }
    }


    public Game(Socket socket, UserList userList, WordList wordList) {
        this.socket = socket;
        this.userList = userList;
        this.wordList = wordList;
    }


    @Override
    public void run() {
        try {
            System.out.println("Connesso con: " + socket.getInetAddress() + ":" + socket.getPort());
            input = new Scanner(socket.getInputStream());
            output = new PrintWriter(socket.getOutputStream(), true);
            // ---
            while (input.hasNextLine()) {
                output.println("┌────────────────────┐\n" +
                               "│ Benvuto su WORDLE! │\n" +
                               "├────────────────────┤\n" +
                               "│ 1. registrati      │\n" +
                               "│ 2. login           │\n" +
                               "│ 3. esci            │\n" +
                               "└────────────────────┘");
                output.println("Benvenuto su WORDLE!\n1. registrati\n2. login\n3. esci\n");
                String userInfo, username, password;

                switch (input.nextLine()) {
                    case "1":
                        // chiedo username:password
                        output.println("username:password");
                        userInfo = input.nextLine();
                        username = userInfo.split(":")[0];
                        password = userInfo.split(":")[1];

                        // aggiungo un nuovo utente alla lista
                        // e invio il messaggio di risposta
                        if (userList.addUser(new HashMap<String, Object>() {{
                            put("user", username);
                            put("password", password);
                            put("giocate", 0);
                            put("vinte", 0);
                            put("streakLast", 0);
                            put("streakMax", 0);
                            put("guessd", new LinkedList<>());
                        }}) == null) {
                            output.println("Utente " + username + " già registrato, procedi con il login");
                        } else {
                            output.println(username + " registrato con successo!");
                        }

                        // torno al menu principale
                        break;

                    case "2":
                        // chiedo username:password
                        output.println("username:password");
                        userInfo = input.nextLine();
                        username = userInfo.split(":")[0];
                        password = userInfo.split(":")[1];

                        // eseuguo il login
                        try {
                            if (userList.loginUser(username, password)) {
                                loginSession(userList.getUser(username));
                            } else {
                                output.println(username + ": password errata, riprova");
                            }
                        } catch (IllegalArgumentException e) {
                            output.println("Utente " + username + " non ancora registrato, prima procedi con la registrazione");
                        }

                        // torno al menu principale
                        break;

                    case "3":
                        // invio il messaggio di disconnessione
                        output.println("Disconnessione dal server");
                        System.out.println("Disconnesso da: " + socket.getInetAddress() + ":" + socket.getPort());

                        // chiudo gli stream di I/O
                        input.close();
                        output.close();

                        // chiudo la socket
                        try {
                            socket.close();
                        } catch (IOException eSocket) {
                            System.err.println("Errore chiusura socket");
                            eSocket.printStackTrace();
                        }

                        // termino la run del thread
                        return;

                    default:
                        // il comando non è tra quelli previsti:
                        // invio un messaggio di errore e torno al menu principale
                        output.println("Comando non riconosciuto");
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println("Errore creazione stream I/O");
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException eSocket) {
                System.err.println("Errore chiusura socket");
                eSocket.printStackTrace();
            }
        }
    }  // run()

}  // class Game
