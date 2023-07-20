import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


/**
 * La classe ClientSetup estende la classe Properties e rappresenta la configurazione del client:
 * HOSTNAME (String)                -> nome del server
 * PORT (int)                       -> numero di porta del server
 * MULTICAST_GROUP_ADDRESS (String) -> indirizzo del gruppo multicast
 * MULTICAST_GROUP_PORT (int)       -> numero di porta del gruppo multicast
 */
public class ClientSetup extends Properties {

    /**
     * Costruttore della classe ClientSetup.
     * @param configFile il percorso del file di configurazione.
     * @throws IOException se si verifica un errore durante la lettura del file di configurazione.
     */
    public ClientSetup(String configFile) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(configFile)) {
            this.load(inputStream);
        }
    }


    /**
     * Restituisce l'hostname del server.
     * @return l'hostname del server.
     */
    public String getHostname() {
        return this.getProperty("HOSTNAME");
    }


    /**
     * Restituisce la porta del server.
     * @return la porta del server.
     */
    public int getPort() {
        return Integer.parseInt(this.getProperty("PORT"));
    }


    /**
     * Restituisce l'indirizzo del gruppo multicast.
     * @return l'indirizzo del gruppo multicast.
     */
    public String getMulticastGroupAddress() {
        return this.getProperty("MULTICAST_GROUP_ADDRESS");
    }


    /**
     * Restituisce la porta del gruppo multicast.
     * @return la porta del gruppo multicast.
     */
    public int getMulticastGroupPort() {
        return Integer.parseInt(this.getProperty("MULTICAST_GROUP_PORT"));
    }

}  // class ClientSetup
