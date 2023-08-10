import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;


/**
 * Classe che implementa l'elenco degli utenti.
 * <br>
 * La classe è implementata con un <code>Set&lt;String&gt;</code> che contiene i nomi degli utenti registrati
 * e un <code>Map&lt;String&gt;</code> che contiene i nomi degli utenti loggati.
 */
public class UserList {

    /**
     * Variabili che rappresentano le due strutture dati principali.
     *
     * - userRegistrati: Map<String,User> degli utenti registrati
     * - userLoggati: Set<String> dei nomi degli utenti loggati
     */
    private Map<String, User> userRegistrati;
    private Set<String> userLoggati;


    /**
     * Metodo privato usato per controllare che il file JSON sia formattato correttamente e lancia una
     * JsonSyntaxException se se il nome del campo corrente nel JSON non corrispondesse al nome atteso.
     */
    private void checkField(String nextName, String expectedName) {
        if (!nextName.equals(expectedName)) {
            throw new JsonSyntaxException("Il file JSON non è formattato correttamente.");
        }
    }


    /**
     * Metodo privato che legge un file JSON contenente i dati degli utenti registrati e li aggiunge alla
     * lista degli utenti registrati.
     */
    private void readJSON(String pathJSON) throws FileNotFoundException, IOException {
        try (JsonReader jsonReader = new JsonReader(new FileReader(pathJSON))) {
            jsonReader.beginArray();
                while (jsonReader.hasNext()) {
                    Map<String, Object> newMap = new HashMap<>();
                    jsonReader.beginObject();
                        checkField(jsonReader.nextName(), "user");
                        newMap.put("user", jsonReader.nextString());

                        checkField(jsonReader.nextName(), "password");
                        newMap.put("password", jsonReader.nextString());

                        checkField(jsonReader.nextName(), "giocate");
                        newMap.put("giocate", jsonReader.nextInt());

                        checkField(jsonReader.nextName(), "vinte");
                        newMap.put("vinte", jsonReader.nextInt());

                        checkField(jsonReader.nextName(), "streaklast");
                        newMap.put("streaklast", jsonReader.nextInt());

                        checkField(jsonReader.nextName(), "streakmax");
                        newMap.put("streakmax", jsonReader.nextInt());

                        checkField(jsonReader.nextName(), "guessd");
                        List<Integer> guessd = new LinkedList<>();
                        newMap.put("guessd", guessd);
                        jsonReader.beginArray();
                            while (jsonReader.hasNext()) {
                                guessd.add(jsonReader.nextInt());
                            }
                        jsonReader.endArray();
                    jsonReader.endObject();
                    // ---
                    User newUser = new User(newMap);
                    userRegistrati.put(newUser.getName(), newUser);
                }
            jsonReader.endArray();
        }
    }


    /**
     * Metodo privato che scrive i dati degli utenti registrati in un file JSON e lancia una IOException
     * se si verifica un errore durante la scrittura del file JSON.
     */
    private void writeJSON(String pathJSON) throws IOException {
        try (JsonWriter jsonWriter = new JsonWriter(new FileWriter(pathJSON))) {
            jsonWriter.setIndent("\t");
            jsonWriter.beginArray();
                for (User user : userRegistrati.values()) {
                    User copyUser = user.copy();
                    jsonWriter.beginObject();
                        jsonWriter.name("user").value(copyUser.getName());
                        jsonWriter.name("password").value(copyUser.getPassword());
                        jsonWriter.name("giocate").value(copyUser.getGiocate());
                        jsonWriter.name("vinte").value(copyUser.getVinte());
                        jsonWriter.name("streaklast").value(copyUser.getStreakLast());
                        jsonWriter.name("streakmax").value(copyUser.getStreakMax());
                        jsonWriter.name("guessd");
                        jsonWriter.beginArray();
                            for (Integer num : copyUser.getGuessDistribution()) {
                                jsonWriter.value(num);
                            }
                        jsonWriter.endArray();
                    jsonWriter.endObject();
                }
            jsonWriter.endArray();
        }
    }


    /**
     * Costruttore della classe <code>UserList</code>.
     * <br>
     * Inizializza la lista degli utenti registrati, la lista degli utenti loggati e legge i dati degli utenti
     * registrati dal file JSON specificato, nel percorso pathJSON.
     *
     * @param pathJSON  percorso del file JSON da leggere
     * @throws FileNotFoundException se il file JSON specificato, non viene trovato
     * @throws IOException se si verifica un errore durante la lettura del file JSON
     */
    public UserList (String pathJSON) throws FileNotFoundException, IOException {
        userRegistrati = new HashMap<>();
        userLoggati = new LinkedHashSet<>();
        readJSON(pathJSON);
    }


    /**
     * Metodo che restituisce un insieme di stringhe contenente i nomi degli utenti registrati.
     *
     * @return un <code>Set&lt;String&gt;</code> contenente i nomi degli utenti registrati
     */
    public synchronized Set<String> getRegistrati() {
        return new LinkedHashSet<>(userRegistrati.keySet());
    }


    /**
     * Metoo che scrive i dati degli utenti registrati in un file JSON specificato, nel percorso pathJSON.
     *
     * @param pathJSON  percorso del file JSON in cui scrivere i dati degli utenti registrati
     * @throws IOException se si verifica un errore durante la scrittura del file JSON
     */
    public synchronized void setRegistrati(String pathJSON) throws IOException {
        writeJSON(pathJSON);
    }


    /**
     * Metodo che restituisce una insieme di stringhe contenente i nomi degli utenti loggati.
     * @return un <code>Set&lt;String&gt;</code> contenente i nomi degli utenti loggati
     */
    public synchronized Set<String> getLoggati() {
        return userLoggati;
    }


    /**
     * Metodo che verifica se l'utente con il nome-utente specificato, è registrato.
     *
     * @param userName  nome-utente dell'utente da cercare
     * @return <i>true</i> se l'utente è registrato, <i>false</i> altrimenti
     */
    public synchronized boolean isRegistrato(String userName) {
        return userRegistrati.containsKey(userName);
    }


    /**
     * Metodo che verifica se l'utente con il nome-utente specificato, è loggato.
     *
     * @param userName  nome-utente dell'utente da cercare
     * @return <i>true</i> se l'utente è loggato, <i>false</i> altrimenti
     */
    public synchronized boolean isLoggato(String userName) {
        return userLoggati.contains(userName);
    }


    /**
     * Metodo che restituisce l'oggetto User associato all'utente con il nome-utente specificato.
     *
     * @param userName  nome-utente dell'utente da cercare
     * @return oggetto <code>User</code> associato all'utente con il nome-utente specificato
     */
    public synchronized User getUser(String userName) {
        return userRegistrati.get(userName);
    }


    /**
     * Metodo che aggiunge una <code>&lt;Map<String, Object>&gt;</code> rappresentante un <code>Utente</code>
     * alla lista degli utenti registrati.
     *
     * @param newUser  <code>&lt;Map<String, Object>&gt;</code> contenente i dati del nuovo utente da aggiungere
     * @return oggetto <code>User</code> associato al nuovo utente aggiunto, <i>null</i> se l'utente è già presente
     * @throws IllegalArgumentException se i dati del nuovo utente non sono validi
     */
    public synchronized User addUser(Map<String, Object> newUser) {
        User user = new User(newUser);
        return userRegistrati.putIfAbsent((String) user.getName(), user);
    }


    /**
     * Metodo che aggiunge un nuovo utente alla lista degli utenti registrati.
     *
     * @param newUser  oggetto <code>User</code> contenente i dati del nuovo utente da aggiungere
     * @return oggetto <code>User</code> associato al nuovo utente aggiunto, <i>null</i> se l'utente è già presente
     * @throws IllegalArgumentException se i dati del nuovo utente non sono validi
     */
    public synchronized User addUser(User newUser) {
        return userRegistrati.putIfAbsent(newUser.getName(), newUser);
    }


    /**
     * Metodo che rimuove l'utente con il nome-utente specificato, dalla lista degli utenti registrati e
     * dalla lista degli utenti loggati.
     *
     * @param userName  nome-utente dell'utente da rimuovere
     * @return oggetto <code>User</code> associato all'utente rimosso, <i>null</i> se l'utente non è presente
     */
    public synchronized User removeUser(String userName) {
        userLoggati.remove(userName);
        return userRegistrati.remove(userName);
    }


    /**
     * Metodo che aggiorna i dati dell'utente specificato, nella lista degli utenti registrati.
     *
     * @param newUser  <code>Map&lt;String, Object&gt;</code> contenente i nuovi dati dell'utente da aggiornare
     * @return oggetto <code>User</code> associato all'utente aggiornato, <i>null</i> se l'utente non è presente
     * @throws IllegalArgumentException se i nuovi dati dell'utente non sono validi
     */
    public synchronized User updateUser(Map<String, Object> newUser) {
        User user = new User(newUser);
        return userRegistrati.replace((String) user.getName(), user);
    }


    /**
     * Metodo che aggiunge l'utente con il nome-utente specificato, alla lista degli utenti loggati.
     *
     * @param userName  nome-utente dell'utente da loggare
     * @return <i>true</i> se l'utente è stato loggato con successo, <i>false</i> se l'utente era già loggato
     * @throws IllegalArgumentException se l'utente non è registrato
     */
    public synchronized boolean loginUser(String userName, String password) {
        if (!userRegistrati.containsKey(userName)) {
            throw new IllegalArgumentException("Non posso loggare un utente non registrato.");
        }
        // ---
        return userRegistrati.get(userName).getPassword().equals(password) && userLoggati.add(userName);
    }


    /**
     * Metodo che rimuove l'utente con il nome-utente specificato, dalla lista degli utenti loggati.
     *
     * @param userName  nome-utente dell'utente per cui effettuare il logout
     * @return <i>trueo</i> se il logout dell'utente è stato effettuato con successo, <i>false</i> se l'utente non è loggato
     * @throws IllegalArgumentException se l'utente non è registrato
     */
    public synchronized boolean logoutUser(String userName) {
        if (!userRegistrati.containsKey(userName)) {
            throw new IllegalArgumentException("Non posso sloggare un utente non registrato.");
        }
        // ---
        return userLoggati.remove(userName);
    }

}  // class UserList
