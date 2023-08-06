import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


/**
 * Classe che rappresenta le proprietà del server.
 * Questa classe estende <tt>Properties</tt> ed è pensata per essere utilizzata all'accensione del server per leggere
 * il file di configurazione e memorizzare le varie proprietà nelle strutture dati più opportune.
 * <p>
 * Le seguenti sono le proprietà disponibili:
 * <ul>
 * <li><tt>PORT</tt>: numero di porta del server (letto come un <tt>int</tt>)
 * <li><tt>PATH_VOCABULARY</tt>: path del file contenente il vocabolario (letto come una <tt>String</tt>)
 * <li><tt>PATH_JSON</tt>: path del file JSON contenente i dati degli utenti (letto come una <tt>String</tt>)
 * <li><tt>WORD_TIMER</tt>: tempo che intercorre tra le pubblicazioni della parola segreta (letto come un <tt>int</tt>)
 * <li><tt>MULTICAST_GROUP_ADDRESS</tt>: indirizzo del gruppo multicast (letto come una <tt>String</tt>)
 * <li><tt>MULTICAST_GROUP_PORT</tt>: numero di porta del gruppo multicast (letto come un <tt>int</tt>)
 * </ul>
 */
public class ServerSetup extends Properties {

    /**
     * Unico costruttore della classe ServerSetup.
     * 
     * @param configFile  path (relativo o assoluto) del file di configurazione.
     * @throws IOException se il file di configurazione indicato nel path non viene trovato o si verifica un errore
     * durante la lettura del file di configurazione.
     */
    public ServerSetup(String configFile) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(configFile)) {
            this.load(inputStream);
        }
    }


    /**
     * Restituisce il numero di porta del server.
     *
     * @return il numero di porta del server.
     */
    public int getPort() {
        return Integer.parseInt(this.getProperty("PORT"));
    }


    /**
     * Restituisce il path del file contenente il vocabolario.
     *
     * @return il path del file contenente il vocabolario.
     */
    public String getPathVocabulary() {
        return this.getProperty("PATH_VOCABULARY");
    }


    /**
     * Restituisce il path del file contenente i dati degli utenti (in JSON).
     *
     * @return il path del file contenente i dati degli utenti (in JSON).
     */
    public String getPathJSON() {
        return this.getProperty("PATH_JSON");
    }


    /**
     * Restituisce il tempo che intercorre tra le pubblicazioni della parola segreta.
     *
     * @return il tempo che intercorre tra le pubblicazioni della parola segreta.
     */
    public int getWordTimer() {
        return Integer.parseInt(this.getProperty("WORD_TIMER"));
    }


    /**
     * Restituisce l'indirizzo del gruppo multicast.
     *
     * @return l'indirizzo del gruppo multicast.
     */
    public String getMulticastGroupAddress() {
        return this.getProperty("MULTICAST_GROUP_ADDRESS");
    }


    /**
     * Restituisce il numero di porta del gruppo multicast.
     *
     * @return il numero di porta del gruppo multicast.
     */
    public int getMulticastGroupPort() {
        return Integer.parseInt(this.getProperty("MULTICAST_GROUP_PORT"));
    }

}
