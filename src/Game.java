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
    private Scanner input = null;
    private PrintWriter output = null;
    private Word word = null;


    private void gameSession(User user) {
        System.out.println(user.getName() + " ha avviato una sessione di gioco");
        String tentativo = null;
        int contaTentativi = 0;
        // ---
        output.println(user.getName() + ", nuova partita avviata: [quit|<tentativo>]");
        while (input.hasNextLine()) {
            tentativo = input.nextLine().trim().toLowerCase();
            contaTentativi++;
            switch (tentativo) {
                case "quit":
                    // invio un messaggio di fine partita
                    // e restituisco il controllo a loginSession()
                    output.println(user.getName() + ", partita terminata!");
                    // ---
                    return;

                default:
                    // analizzo la stringa inserita dall'utente:
                    // invio la maschera in risposta
                    // termino la partitain caso di vittoria/sconfitta
                    String mask = word.getMask(tentativo);
                    if (mask.matches("\\+*")) {
                        output.println("Complimenti " + user.getName() + ", hai indovinato la parola!");
                        return;
                    }
                    // ---
                    if (contaTentativi == 12) {
                        output.println(user.getName() + ", hai esaurito i tentativi! Partita terminata.");
                        return;
                    }
                    // ---
                    output.println(mask);

            }
        }
    }


    private void loginSession(User user) {
        System.out.println("User " + user.getName() + " ha effettuato il login");
        // ---
        output.println(user.getName() + ", login effettuato con successo: [playwordle|logout]");
        while (input.hasNextLine()) {
            switch (input.nextLine().trim().toLowerCase()) {

                case "playwordle":
                    // estraggo la parola da giocare:
                    // IF non giocata => lancio sessione di gioco
                    // ELSE:          => torno al while(...)
                    word = wordList.getCurrentWord();
                    if (word.addUser(user.getName())) {
                        gameSession(user);
                    } else {
                        output.println(user.getName() + " hai già giocato questa parola!");
                    }
                    // ---
                    continue;

                case "logout":
                    // invio un messaggio di arrivederci,
                    // rimuovo l'utente dalla lista dei loggati
                    // restituisco il controllo a run()
                    output.println("Arrivederci " + user.getName());
                    userList.logoutUser(user.getName());
                    // ---
                    return;

                default:
                    // l'utente ha inserito un comando errato o non eseguibile in questo contesto:
                    // invio un messaggio di errore e torno al menu di login
                    output.println("Comando non eseguibile nella sessione di login");

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
            output.println("Benvenuto su WORDLE: [register|login|exit]\n");
            while (input.hasNextLine()) {
                try {
                    String[] splitCommand = input.nextLine().split(" ", 2);
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
                                put("streakLast", 0);
                                put("streakMax", 0);
                                put("guessd", new LinkedList<>());
                            }}) == null) {
                                output.println("Utente " + username + " già registrato");
                            } else {
                                output.println(username + " registrato con successo!");
                            }
                            // ---
                            // torno al while(...)
                            // in attessa di un nuovo comando
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
                                    // avvio la sessione di login dove l'utente può:
                                    // 1. giocare una parola
                                    // 2. effettuare il logout
                                    // 3. ricevere statistiche
                                    // 4. condividere
                                    // 5. visualizzare le condivisioni
                                    loginSession(userList.getUser(username));
                                } else {
                                    output.println(username + ": login fallito");
                                }
                            } catch (IllegalArgumentException e) {
                                output.println("Utente " + username + " non ancora registrato");
                            }
                            // ---
                            // torno al while(...)
                            // in attessa di un nuovo comando
                            continue;

                        case "exit":
                            // l'utente vuole uscire dal programma:
                            // invio un messaggio di disconnessione
                            // chiudo gli stream di I/O
                            // chiudo la socket
                            // termino la run di Game
                            output.println("Disconnessione dal server");
                            System.out.println("Disconnesso da: " + socket.getInetAddress() + ":" + socket.getPort());
                            // ---
                            input.close();
                            output.close();
                            // ---
                            try {
                                socket.close();
                            } catch (IOException e) {
                                System.err.println("Errore chiusura socket");
                                e.printStackTrace();
                            }
                            // ---
                            return;

                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    // l'utente ha inserito un comando errato/malformattato o non eseguibile in questo contesto
                    // che ha scatenato una ArrayIndexOutOfBoundsException quando ho provato a splittare
                } finally {
                    output.println("Comandi diversi da [register|login|exit] da lanciare dopo il login");
                }
            }  // while(...)
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
