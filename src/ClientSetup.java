import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;


/**
 * La classe ClientSetup estende la classe Properties e rappresenta la configurazione del client:
 * HOSTNAME (String)                -> nome del server
 * PORT (int)                       -> numero di porta del server
 * SERVER_NOTIFICATION_PORT (int)   -> numero di porta per le notifiche dei risultati (in UDP)
 * MULTICAST_GROUP_ADDRESS (String) -> indirizzo del gruppo multicast
 * MULTICAST_GROUP_PORT (int)       -> numero di porta del gruppo multicast
 * @see Properties
 */
public class ClientSetup extends Properties {

    private static final String CONFIG_FILE = "lib/CLIENT.conf";


    /**
     * Costruttore della classe ClientSetup.
     * @throws FileNotFoundException se il file di configurazione non viene trovato.
     * @throws IOException se si verifica un errore durante la lettura del file di configurazione.
     */
    public ClientSetup(String configFile) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(CONFIG_FILE)) {
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
     * Restituisce la porta di notifica del server.
     * @return la porta di notifica del server.
     */
    public int getServerNotificationPort() {
        return Integer.parseInt(this.getProperty("SERVER_NOTIFICATION_PORT"));
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

}  // ClientSetup
