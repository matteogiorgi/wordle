import java.io.EOFException;
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
        System.out.println("User " + user.getName() + " ha avviato una sessione di gioco");
        output.println("User " + user.getName() + ", nuova partita avviata: [quit|<tentativo>]");
        // ---
        String tentativo = null;
        int contaTentativi = 0;
        // ---
        while (input.hasNextLine()) {
            tentativo = input.nextLine().trim().toLowerCase();
            contaTentativi++;
            switch (tentativo) {

                case "quit":
                    // invio un messaggio di fine partita
                    // e restituisco il controllo a loginSession()
                    output.println("User " + user.getName() + ", partita terminata!");
                    return;

                default:
                    // analizzo la stringa inserita e invio la maschera in risposta
                    // (termino la partita in caso di vittoria/sconfitta)
                    if (tentativo.equals(word.getWord())) {
                        output.println("Complimenti " + user.getName() + ", hai indovinato la parola!");
                        return;
                    }
                    // ---
                    if (contaTentativi == 12) {
                        output.println("User " + user.getName() + ", hai esaurito i tentativi! Partita terminata.");
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
        }  // while(...)
    }


    private void loginSession(User user) {
        System.out.println("User " + user.getName() + " ha effettuato il login");
        output.println("User " + user.getName() + ", login effettuato con successo: [playwordle|logout]");
        // ---
        while (input.hasNextLine()) {
            switch (input.nextLine().trim().toLowerCase()) {

                case "playwordle":
                    // estraggo la parola da giocare:
                    // se non giocata, lancio sessione di gioco
                    // altrimenti torno al while(...)
                    word = wordList.getCurrentWord();
                    if (word.addUser(user.getName())) {
                        gameSession(user);
                    } else {
                        output.println("User " + user.getName() + " hai già giocato questa parola!");
                    }
                    continue;

                case "logout":
                    // rimuovo l'utente dalla lista dei loggati
                    // restituisco il controllo a run()
                    userList.logoutUser(user.getName());
                    output.println("Arrivederci " + user.getName());
                    return;

                default:
                    // l'utente ha inserito un comando errato
                    // o non eseguibile in questo contesto
                    output.println("Comando non eseguibile nella sessione di login: [playwordle|logout]");

            }
        }  // while(...)
    }


    public Game(Socket socket, UserList userList, WordList wordList) {
        this.socket = socket;
        this.userList = userList;
        this.wordList = wordList;
    }


    @Override
    public void run() {
        try {
            input = new Scanner(socket.getInputStream());
            output = new PrintWriter(socket.getOutputStream(), true);
            output.println("Main-menu WORDLE: [register|remove|login|exit]");
            // ---
            while (!socket.isClosed() && !socket.isInputShutdown() && input.hasNextLine()) {
                String inpuString = input.nextLine().trim().toLowerCase();
                if (inpuString.equals("exit")) {
                    // l'utente vuole uscire dal programma:
                    // invio un messaggio di disconnessione
                    // chiudo gli stream di I/O e la socket
                    System.out.println("Disconnesso da: " + socket.getInetAddress() + ":" + socket.getPort());
                    output.println("Disconnessione ...");
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
                            System.out.println("Nuova registrazione: " + username + ":" + password);
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
                                output.println("Utente " + username + " registrato con successo!");
                            } else {
                                output.println("Utente " + username + " già registrato");
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
                            System.out.println("Cancellazione user " + username);
                            // ---
                            User tempUser = userList.removeUser(username);
                            if (tempUser == null) {
                                output.println("Utente " + username + " non registrato");
                                continue;
                            }
                            // ---
                            if (!tempUser.getPassword().equals(password)) {
                                output.println("Password utente " + username + " errata");
                                userList.addUser(tempUser);
                                continue;
                            }
                            // ---
                            output.println("Utente " + username + " rimosso con successo");
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

                        default:
                            // questo blocco finally verrà eseguito solo nei casi di ArrayIndexOutOfBoundsException
                            // oppure quando l'utente ha inserito un comando che non sia [register|remove|login|exit]
                            output.println("Comando non eseguibile nel Main-menu WORDLE: [register|remove|login|exit]");

                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    // l'utente ha inserito un comando errato/malformattato o non eseguibile in questo contesto
                    // che ha scatenato una ArrayIndexOutOfBoundsException quando ho provato a splittare
                    output.println("Comando [register|remove|login] malformattato");
                }
            }  // while(...)
        } catch (EOFException e) {
            System.err.println("Errore in lettura stream I/O");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Errore creazione stream I/O o chiusura socket");
            e.printStackTrace();
        }
    }  // run()

}  // class Game
