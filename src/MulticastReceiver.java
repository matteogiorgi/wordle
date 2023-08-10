import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Classe che rappresenta il thread che rimane in ascolto delle notifiche sul multicast.
 * <br>
 * Estende <code>ConcurrentLinkedQueue&lt;String&gt;</code> per poter aggiungere le notifiche ricevute alla
 * coda delle notifiche.
 */
public class MulticastReceiver extends ConcurrentLinkedQueue<String> implements Runnable {

    /*
     * Variabili utili.
     *
     * - multicastGroupPort: numero di porta del gruppo multicast
     * - multicastGroupAddress: indirizzo del gruppo multicast
     * - userName: nome dell'utente in ascolto
     */
    private final int multicastGroupPort;
    private final String multicastGroupAddress;
    private final String userName;


    /**
     * Costruttore della classe <code>MulticastReceiver</code>.
     *
     * @param multicastGroupPort  porta del gruppo multicast
     * @param multicastGroupAddress  indirizzo del gruppo multicast
     * @param userName  nome dell'utente in ascolto
     */
    public MulticastReceiver(int multicastGroupPort, String multicastGroupAddress, String userName) {
        super();
        this.multicastGroupPort = multicastGroupPort;
        this.multicastGroupAddress = multicastGroupAddress;
        this.userName = userName;
    }


    /**
     * Metodo run() del thread di MulticastListener.
     *
     * Il Thread che contiene MulticastReceiver rimane in ascolto delle notifiche sul multicast.
     */
    @Override
    public void run() {
        try (MulticastSocket multicastSocket = new MulticastSocket(multicastGroupPort)) {
            multicastSocket.setSoTimeout(2000);
            // ---
            InetAddress multicastGroup = InetAddress.getByName(multicastGroupAddress);
            if (!multicastGroup.isMulticastAddress()) {
                throw new IllegalArgumentException("[ERROR] indirizzo multicast non valido: " + multicastGroup.getHostAddress());
            }
            multicastSocket.joinGroup(multicastGroup);

            // ciclo (virtualmente) infinito dove ricevo una notifica,
            // e aggiungo la notifica alla coda delle notifiche se il nome dell'utente che ha
            // richiesto il messaggio di multicast è diverso dal quello dell'utente in ascolto
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    DatagramPacket packet = new DatagramPacket(new byte[8192], 8192);
                    multicastSocket.receive(packet);
                    String notification = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
                    Pattern pattern = Pattern.compile("'(.*?)'");
                    Matcher matcher = pattern.matcher(notification);
                    if (matcher.find()) {
                        String name = matcher.group(1);
                        if (!name.equals(userName)) {
                            offer(notification);
                        }
                    }
                } catch (SocketTimeoutException e) {
                    // è scaduto il timer mentre il thread era fermo sulla receive in attesa di una notifica,
                    // goto next iteration e se il thread è stato nel frattempo interrotto, esco dal ciclo
                    // e termino l'esecuzione
                }
            }
        } catch (IOException ex) {
            System.err.println("[ERROR] ricezione notifica fallita");
            ex.printStackTrace();
        }
    }  // run

}  // class MulticastReceiver
