import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedList;




/**
 * Questa classe rappresenta un utente del gioco Wordle.
 * L'utente è rappresentato come una mappa di coppie chiave-valore, dove la chiave è una
 * stringa che rappresenta il nome del campo e il valore è un oggetto generico.
 * La classe estende la classe HashMap e fornisce metodi per accedere ai campi dell'utente.
 */
public class User {

    private Map<String, Object> user;


    /**
     * Costruttore che crea un nuovo oggetto User a partire da una mappa di coppie chiave-valore.
     * @param map la mappa di coppie chiave-valore da cui creare l'oggetto User
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
     * Restituisce il nome utente dell'utente.
     * @return il nome utente dell'utente
     */
    public synchronized String getName() {
        return (String) user.get("user");
    }

    /**
     * Restituisce la password dell'utente.
     * @return la password dell'utente
     */
    public synchronized String getPassword() {
        return (String) user.get("password");
    }

    /**
     * Restituisce il numero di giocate dell'utente.
     * @return il numero di giocate dell'utente
     */
    public synchronized int getGiocate() {
        return (int) user.get("giocate");
    }

    /**
     * Restituisce il numero di partite vinte dall'utente.
     * @return il numero di partite vinte dall'utente
     */
    public synchronized int getVinte() {
        return (int) user.get("vinte");
    }

    /**
     * Restituisce la lunghezza della serie di tentativi corretti più recente dell'utente.
     * @return la lunghezza della serie di tentativi corretti più recente dell'utente
     */
    public synchronized int getStreakLast() {
        return (int) user.get("streaklast");
    }

    /**
     * Restituisce la lunghezza massima della serie di tentativi corretti dell'utente.
     * @return la lunghezza massima della serie di tentativi corretti dell'utente
     */
    public synchronized int getStreakMax() {
        return (int) user.get("streakmax");
    }

    /**
     * Restituisce la distribuzione dei tentativi dell'utente.
     * @return la distribuzione dei tentativi dell'utente
     */
    @SuppressWarnings("unchecked")
    public synchronized List<Integer> getGuessDistribution() {
        return (List<Integer>) user.get("guessd");
    }

    /**
     * Aggiorna i dati dell'utente in base all'esito dell'ultima partita.
     * @param win true se l'utente ha vinto la partita, false altrimenti
     * @param tentativi il numero di tentativi effettuati dall'utente nella partita
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