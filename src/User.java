import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedList;


/**
 * Classe che rappresenta un utente del gioco.
 * <br>
 * L'utente è implementato come una mappa di coppie chiave-valore con i seguenti campi:
 * <ul>
 * <li>user: nome utente</li>
 * <li>password: password</li>
 * <li>giocate: numero di partite giocate</li>
 * <li>vinte: numero di partite vinte</li>
 * <li>streaklast: lunghezza della serie di vincite più recente</li>
 * <li>streakmax: lunghezza massima della serie di vincite</li>
 * <li>guessd: distribuzione di tentativi impiegati per arrivare alla soluzione</li>
 * </ul>
 */
public class User {

    /**
     * L'utente è rappresentato come una <code>Map&lt;String, Object&gt;</code>
     * (non estende <code>HashMap</code> per evitare di esporre i metodi di modifica della mappa).
     * <br>
     * La chiave è una <code>String</code> che rappresenta il nome del campo, il valore è archiviato come
     * un <code>Object</code> generico (può essere di tipo diverso).
     */
    private Map<String, Object> user;


    /**
     * Costruttore della classe <code>User</code>.
     * <br>
     * Viene creato un nuovo oggetto <code>User</code> a partire da una <code>Map&lt;String, Object&gt;</code>;
     * se la mappa non dovesse contenere tutti i campi necessari, viene lanciata una <code>IllegalArgumentException</code>.
     *
     * @param user  mappa di coppie chiave-valore da cui creare l'oggetto <code>User</code>
     * @return nuovo oggetto <code>User</code> creato a partire dalla mappa passata
     * @throws NullPointerException se la mappa passata è <code>null</code>
     * @throws IllegalArgumentException se la mappa passata non contiene tutti i campi corretti
     */
    public User(Map<String, Object> user) {
        if (user == null) {
            throw new NullPointerException("L'utente da aggiungere non può essere nullo.");
        }
        // ---
        if (!(user.containsKey("user") && user.get("user") instanceof String) ||
            !(user.containsKey("password") && user.get("password") instanceof String) ||
            !(user.containsKey("giocate") && user.get("giocate") instanceof Integer) ||
            !(user.containsKey("vinte") && user.get("vinte") instanceof Integer) ||
            !(user.containsKey("streaklast") && user.get("streaklast") instanceof Integer) ||
            !(user.containsKey("streakmax") && user.get("streakmax") instanceof Integer) ||
            !(user.containsKey("guessd") && user.get("guessd") instanceof List)) {
                throw new IllegalArgumentException("L'utente da aggiungere non è valido.");
        }
        // ---
        this.user = user;
    }


    /**
     * Metodo che crea una copia profonda di <code>this</code>.
     *
     * @return nuovo oggetto <code>User</code> che rappresenta una copia profonda dell'oggetto corrente
     */
    @SuppressWarnings("unchecked")
    public synchronized User copy() {
        Map<String, Object> copyMap = new HashMap<>();
        copyMap.put("user", user.get("user"));
        copyMap.put("password", user.get("password"));
        copyMap.put("giocate", user.get("giocate"));
        copyMap.put("vinte", user.get("vinte"));
        copyMap.put("streaklast", user.get("streaklast"));
        copyMap.put("streakmax", user.get("streakmax"));
        // ---
        List<Integer> copyGuessd  = new LinkedList<>();
        for (Integer i : (LinkedList<Integer>) user.get("guessd")) {
            copyGuessd.add(i);
        }
        copyMap.put("guessd", copyGuessd);
        // ---
        return new User(copyMap);
    }


    /**
     * Metodo che restituisce il nome-utente.
     *
     * @return nome-utente
     */
    public synchronized String getName() {
        return (String) user.get("user");
    }


    /**
     * Metodo che restituisce la password.
     *
     * @return password
     */
    public synchronized String getPassword() {
        return (String) user.get("password");
    }


    /**
     * Restituisce il numero di partite giocate.
     *
     * @return numero di partite giocate
     */
    public synchronized int getGiocate() {
        return (int) user.get("giocate");
    }


    /**
     * Metodo che restituisce il numero di partite vinte.
     *
     * @return numero di partite vinte
     */
    public synchronized int getVinte() {
        return (int) user.get("vinte");
    }


    /**
     * Metodo che restituisce la lunghezza della serie di vincite più recente
     *
     * @return lunghezza della serie di vincite più recente
     */
    public synchronized int getStreakLast() {
        return (int) user.get("streaklast");
    }


    /**
     * Metodo che restituisce la lunghezza massima della serie di vincite.
     *
     * @return lunghezza massima della serie di vincite
     */
    public synchronized int getStreakMax() {
        return (int) user.get("streakmax");
    }


    /**
     * Metodo che restituisce la distribuzione dei tentativi impiegati per arrivare alla soluzione.
     *
     * @return distribuzione dei tentativi
     */
    @SuppressWarnings("unchecked")
    public synchronized List<Integer> getGuessDistribution() {
        return (List<Integer>) user.get("guessd");
    }


    /**
     * Metodo che aggiorna i dati in base all'esito dell'ultima partita.
     *
     * @param win  valore booleano che indica se l'utente ha vinto (<code>true</code>) o perso la partita (<code>false</code>)
     * @param tentativi  numero di tentativi effettuati nella partita
     */
    public synchronized void update(boolean win, int tentativi) {
        int streakLast = win ? getStreakLast()+1 :0;
        int streakMax = getStreakMax();
        // ---
        user.put("giocate", getGiocate() + 1);
        user.put("vinte", getVinte() + (win ? 1 : 0));
        user.put("streaklast", streakLast);
        user.put("streakmax", streakLast>=streakMax ? streakLast : streakMax);
        getGuessDistribution().add(tentativi);
    }

}  // class User
