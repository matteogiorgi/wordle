import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


/**
 * Classe che rappresenta le proprietà del server.
 * PORT (int)                        ->  numero di porta del server
 * PATH_VOCABULARY (String)          ->  path del file contenente il vocabolario
 * PATH_JSON (String)                ->  path del file contenente i dati degli utenti (in JSON)
 * WORD_TIMER (int)                  ->  tempo che intercorre tra le pubblicazioni della parola segreta
 * MULTICAST_GROUP_ADDRESS (String)  ->  indirizzo del gruppo multicast
 * MULTICAST_GROUP_PORT (int)        ->  numero di porta del gruppo multicast
 */
public class ServerSetup extends Properties {

    /**
     * Costruttore della classe ServerSetup.
     * @param configFile il percorso del file di configurazione.
     * @throws IOException
     */
    public ServerSetup(String configFile) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(configFile)) {
            this.load(inputStream);
        }
    }


    /**
     * Restituisce il numero di porta del server.
     * @return il numero di porta del server.
     */
    public int getPort() {
        return Integer.parseInt(this.getProperty("PORT"));
    }


    /**
     * Restituisce il path del file contenente il vocabolario.
     * @return il path del file contenente il vocabolario.
     */
    public String getPathVocabulary() {
        return this.getProperty("PATH_VOCABULARY");
    }


    /**
     * Restituisce il path del file contenente i dati degli utenti (in JSON).
     * @return il path del file contenente i dati degli utenti (in JSON).
     */
    public String getPathJSON() {
        return this.getProperty("PATH_JSON");
    }


    /**
     * Restituisce il tempo che intercorre tra le pubblicazioni della parola segreta.
     * @return il tempo che intercorre tra le pubblicazioni della parola segreta.
     */
    public int getWordTimer() {
        return Integer.parseInt(this.getProperty("WORD_TIMER"));
    }


    /**
     * Restituisce l'indirizzo del gruppo multicast.
     * @return l'indirizzo del gruppo multicast.
     */
    public String getMulticastGroupAddress() {
        return this.getProperty("MULTICAST_GROUP_ADDRESS");
    }


    /**
     * Restituisce il numero di porta del gruppo multicast.
     * @return il numero di porta del gruppo multicast.
     */
    public int getMulticastGroupPort() {
        return Integer.parseInt(this.getProperty("MULTICAST_GROUP_PORT"));
    }

}
