import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
// ---
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
// ---
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;




/**
 * Questa classe rappresenta una lista di utenti registrati e loggati.
 * Gli utenti registrati sono memorizzati in una mappa, dove la chiave è il nome utente e il valore è un oggetto User.
 * Gli utenti loggati sono memorizzati in una lista di stringhe contenente i nomi utente.
 */
public class UserList {

    private Map<String, User> userRegistrati;
    private Set<String> userLoggati;


    /**
     * Controlla se il nome del campo corrente nel file JSON corrisponde al nome atteso.
     * @param nextName il nome del campo corrente nel file JSON
     * @param expectedName il nome atteso del campo
     * @throws JsonSyntaxException se il nome del campo corrente non corrisponde al nome atteso
     */
    private void checkField(String nextName, String expectedName) {
        if (!nextName.equals(expectedName)) {
            throw new JsonSyntaxException("Il file JSON non è formattato correttamente.");
        }
    }


    /**
     * Legge un file JSON contenente i dati degli utenti registrati e li aggiunge alla lista degli utenti registrati.
     * @param pathJSON il percorso del file JSON da leggere
     * @throws IOException se si verifica un errore durante la lettura del file JSON
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
     * Scrive i dati degli utenti registrati in un file JSON.
     * @param pathJSON il percorso del file JSON in cui scrivere i dati degli utenti registrati
     * @throws IOException se si verifica un errore durante la scrittura del file JSON
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
     * Costruttore della classe UserList che inizializza la lista degli utenti registrati e la lista degli utenti loggati.
     * Legge i dati degli utenti registrati dal file JSON specificato nel percorso pathJSON.
     * @param pathJSON il percorso del file JSON da leggere
     * @throws IOException se si verifica un errore durante la lettura del file JSON
     */
    public UserList (String pathJSON) throws FileNotFoundException, IOException {
        userRegistrati = new HashMap<>();
        userLoggati = new LinkedHashSet<>();
        readJSON(pathJSON);
    }


    /**
     * Restituisce un insieme di stringhe contenente i nomi degli utenti registrati.
     * @return un insieme di stringhe contenente i nomi degli utenti registrati
     */
    public synchronized Set<String> getRegistrati() {
        return new LinkedHashSet<>(userRegistrati.keySet());
    }


    /**
     * Scrive i dati degli utenti registrati in un file JSON specificato nel percorso pathJSON.
     * @param pathJSON il percorso del file JSON in cui scrivere i dati degli utenti registrati
     * @throws IOException se si verifica un errore durante la scrittura del file JSON
     */
    public synchronized void setRegistrati(String pathJSON) throws IOException {
        writeJSON(pathJSON);
    }


    /**
     * Restituisce una lista di stringhe contenente i nomi degli utenti loggati.
     * @return una lista di stringhe contenente i nomi degli utenti loggati
     */
    public synchronized Set<String> getLoggati() {
        return userLoggati;
    }


    /**
     * Verifica se l'utente con il nome utente specificato è registrato.
     * @param userName il nome utente dell'utente da cercare
     * @return true se l'utente è registrato, false altrimenti
     */
    public synchronized boolean isRegistrato(String userName) {
        return userRegistrati.containsKey(userName);
    }


    /**
     * Verifica se l'utente con il nome utente specificato è loggato.
     * @param userName il nome utente dell'utente da cercare
     * @return true se l'utente è loggato, false altrimenti
     */
    public synchronized boolean isLoggato(String userName) {
        return userLoggati.contains(userName);
    }


    /**
     * Restituisce l'oggetto User associato all'utente con il nome utente specificato.
     * @param userName il nome utente dell'utente da cercare
     * @return l'oggetto User associato all'utente con il nome utente specificato
     */
    public synchronized User getUser(String userName) {
        return userRegistrati.get(userName);
    }


    /**
     * Aggiunge un nuovo utente alla lista degli utenti registrati.
     * @param newUser una mappa contenente i dati del nuovo utente da aggiungere
     * @return l'oggetto User associato al nuovo utente aggiunto, null se l'utente è già presente
     * @throws IllegalArgumentException se i dati del nuovo utente non sono validi
     */
    public synchronized User addUser(Map<String, Object> newUser) {
        User user = new User(newUser);
        return userRegistrati.putIfAbsent((String) user.getName(), user);
    }


    /**
     * Rimuove l'utente con il nome utente specificato dalla lista degli utenti registrati e dalla lista degli utenti loggati.
     * @param userName il nome utente dell'utente da rimuovere
     * @return l'oggetto User associato all'utente rimosso, null se l'utente non è presente
     */
    public synchronized User removeUser(String userName) {
        userLoggati.remove(userName);
        return userRegistrati.remove(userName);
    }


    /**
     * Aggiorna i dati dell'utente specificato nella lista degli utenti registrati.
     * @param newUser una mappa contenente i nuovi dati dell'utente da aggiornare
     * @return l'oggetto User associato all'utente aggiornato, null se l'utente non è presente
     * @throws IllegalArgumentException se i nuovi dati dell'utente non sono validi
     */
    public synchronized User updateUser(Map<String, Object> newUser) {
        User user = new User(newUser);
        return userRegistrati.replace((String) user.getName(), user);
    }


    /**
     * Aggiunge l'utente con il nome utente specificato alla lista degli utenti loggati.
     * @param userName il nome utente dell'utente da loggare
     * @return true se l'utente è stato loggato con successo, false se l'utente era già loggato
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
     * Rimuove l'utente con il nome utente specificato dalla lista degli utenti loggati.
     * @param userName il nome utente dell'utente da sloggare
     * @return true se l'utente è stato sloggato con successo, false se l'utente non era loggato
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
