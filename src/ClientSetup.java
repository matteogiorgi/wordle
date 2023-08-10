import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


/**
 * Classe che rappresenta le proprietà del client.
 * <br>
 * Questa classe estende <code>Properties</code> ed è pensata per essere utilizzata all'accensione del client
 * per leggere il file di configurazione e memorizzare le varie proprietà nelle strutture dati più opportune.
 * <p>
 * Le seguenti sono le proprietà disponibili:
 * <ul>
 * <li>HOSTNAME: nome del server (letto come una <code>String</code>)</li>
 * <li>PORT: numero di porta del server (letto come un <code>int</code>)</li>
 * <li>MULTICAST_GROUP_ADDRESS: indirizzo del gruppo multicast (letto come una <code>String</code>)</li>
 * <li>MULTICAST_GROUP_PORT: numero di porta del gruppo multicast (letto come un <code>int</code>)</li>
 * </ul>
 */
public class ClientSetup extends Properties {

    /**
     * Costruttore della classe <code>ClientSetup</code>.
     *
     * @param configFile  percorso del file di configurazione
     * @throws IOException se si verifica un errore durante la lettura del file di configurazione
     */
    public ClientSetup(String configFile) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(configFile)) {
            this.load(inputStream);
        }
    }


    /**
     * Metodo che restituisce l'hostname del server.
     *
     * @return hostname del server
     */
    public String getHostname() {
        return this.getProperty("HOSTNAME");
    }


    /**
     * Metodo che restituisce la porta del server.
     *
     * @return porta del server
     */
    public int getPort() {
        return Integer.parseInt(this.getProperty("PORT"));
    }


    /**
     * Metodo che restituisce l'indirizzo del gruppo multicast.
     *
     * @return indirizzo del gruppo multicast
     */
    public String getMulticastGroupAddress() {
        return this.getProperty("MULTICAST_GROUP_ADDRESS");
    }


    /**
     * Metodo che restituisce la porta del gruppo multicast.
     *
     * @return porta del gruppo multicast
     */
    public int getMulticastGroupPort() {
        return Integer.parseInt(this.getProperty("MULTICAST_GROUP_PORT"));
    }

}  // class ClientSetup
