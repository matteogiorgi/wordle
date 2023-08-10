import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


/**
 * Classe che rappresenta le proprietà del server.
 * <br>
 * Questa classe estende <code>Properties</code> ed è pensata per essere utilizzata all'accensione del server per
 * leggere il file di configurazione e memorizzare le varie proprietà nelle strutture dati più opportune.
 * <p>
 * Le seguenti sono le proprietà disponibili:
 * <ul>
 * <li><code>PORT</code>: numero di porta del server (letto come un <code>int</code>)</li>
 * <li><code>PATH_VOCABULARY</code>: path del file contenente il vocabolario (letto come una <code>String</code>)</li>
 * <li><code>PATH_JSON</code>: path del file JSON contenente i dati degli utenti (letto come una <code>String</code>)</li>
 * <li><code>WORD_TIMER</code>: tempo che intercorre tra le pubblicazioni della parola segreta (letto come un <code>int</code>)</li>
 * <li><code>MULTICAST_GROUP_ADDRESS</code>: indirizzo del gruppo multicast (letto come una <code>String</code>)</li>
 * <li><code>MULTICAST_GROUP_PORT</code>: numero di porta del gruppo multicast (letto come un <code>int</code>)</li>
 * </ul>
 */
public class ServerSetup extends Properties {

    /**
     * Costruttore della classe <code>ServerSetup</code>.
     *
     * @param configFile  path (relativo o assoluto) del file di configurazione
     * @throws IOException se il file di configurazione indicato nel path non viene trovato o si verifica un errore
     * durante la lettura del file di configurazione
     */
    public ServerSetup(String configFile) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(configFile)) {
            this.load(inputStream);
        }
    }


    /**
     * Restituisce il numero di porta del server.
     *
     * @return il numero di porta del server
     */
    public int getPort() {
        return Integer.parseInt(this.getProperty("PORT"));
    }


    /**
     * Restituisce il path del file contenente il vocabolario.
     *
     * @return il path del file contenente il vocabolario
     */
    public String getPathVocabulary() {
        return this.getProperty("PATH_VOCABULARY");
    }


    /**
     * Restituisce il path del file contenente i dati degli utenti (in JSON).
     *
     * @return il path del file contenente i dati degli utenti (in JSON)
     */
    public String getPathJSON() {
        return this.getProperty("PATH_JSON");
    }


    /**
     * Restituisce il tempo che intercorre tra le pubblicazioni della parola segreta.
     *
     * @return il tempo che intercorre tra le pubblicazioni della parola segreta
     */
    public int getWordTimer() {
        return Integer.parseInt(this.getProperty("WORD_TIMER"));
    }


    /**
     * Restituisce l'indirizzo del gruppo multicast.
     *
     * @return l'indirizzo del gruppo multicast
     */
    public String getMulticastGroupAddress() {
        return this.getProperty("MULTICAST_GROUP_ADDRESS");
    }


    /**
     * Restituisce il numero di porta del gruppo multicast.
     *
     * @return il numero di porta del gruppo multicast
     */
    public int getMulticastGroupPort() {
        return Integer.parseInt(this.getProperty("MULTICAST_GROUP_PORT"));
    }

}
