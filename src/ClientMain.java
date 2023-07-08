import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;


public class ClientMain {

    private static final String PATH_CONF = "lib/CLIENT.conf";
    private static ClientSetup clientProperties;
    private static Socket socket;
    // ---
    private static Scanner input;
    private static PrintWriter output;


    // private static String gestoreComando(String comando) {
    //     switch (comando) {
    //         case "new username":
    //             // chiedo username
    //             break;

    //         case "new password":
    //             // chiedo username
    //             break;

    //         case "quit":
    //             // chiudo la socket
    //             break;
    //         default:
    //             break;
    //     }
    //     return "ok";
    // }


    public static void main(String[] args) {
        // leggo il file di configurazione del client
        try {
            clientProperties = new ClientSetup(PATH_CONF);
        } catch (FileNotFoundException e) {
            System.err.printf("File di configurazione %s non trovato\n", PATH_CONF);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.printf("Errore durante la lettura del file di configurazione %s\n", PATH_CONF);
            e.printStackTrace();
        }

        // apro la socket del client
        // alloco gli stream di I/O
        try (Socket socket = new Socket(clientProperties.getHostname(), clientProperties.getPort());
             Scanner tastiera = new Scanner(System.in, "UTF-8")) {
            // ---
            input = new Scanner(socket.getInputStream());
            output = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("=== CLIENT CONNESSO ===");
//            // ---
//            for (boolean connected=true; connected;) {
//                // leggo e stampo il messaggio del server
//                System.out.println(input.nextLine());
//
//                // leggo da tastiera [register|login|quit]
//                switch (tastiera.nextLine()) {
//                    case "register":
//                        output.println(comando);
//                        // username
//                        System.out.println(input.nextLine());
//                        output.println(tastiera.nextLine());
//
//                        // password
//                        System.out.println(input.nextLine());
//                        output.println(tastiera.nextLine());
//
//                        // ---
//                        break;
//                    case "login":
//                        // leggo da tastiera [username]
//                        System.out.print("Username: ");
//                        username = tastiera.nextLine();
//                        // leggo da tastiera [password]
//                        System.out.print("Password: ");
//                        password = tastiera.nextLine();
//                        // invio il comando al server
//                        output.println(comando);
//                        // invio l'username al server
//                        output.println(username);
//                        // invio la password al server
//                        output.println(password);
//                        break;
//                    case "quit":
//                        // invio il comando al server
//                        output.println(comando);
//                        // chiudo la socket
//                        socket.close();
//                        // mi disconnetto dal server
//                        connected = false;
//                        break;
//                    default:
//                        System.out.println("Comando non riconosciuto");
//                        break;
//                }
//            }
        } catch (IOException e) {
            System.err.printf("Errore durante l'apertura della socket\n");
            e.printStackTrace();
        }
    }  // main

}  // ClientMain
