import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;


public class MulticastSender implements Runnable {

    /**
     * Variabili private che identificano:
     * multicastGroupPort    -> numero di porta del gruppo multicast
     * multicastGroupAddress -> indirizzo del gruppo multicast
     * queue                 -> coda di notifiche da inviare sul multicast
     */
    private final int multicastGroupPort;
    private final String multicastGroupAddress;
    private final Queue<String> queue = new LinkedList<>();


    /**
     * Costruttore che inizializza le variabili private.
     * @param multicastGroupPort porta del gruppo multicast
     * @param multicastGroupAddress indirizzo del gruppo multicast
     */
    public MulticastSender(int multicastGroupPort, String multicastGroupAddress) {
        this.multicastGroupPort = multicastGroupPort;
        this.multicastGroupAddress = multicastGroupAddress;
    }


    /**
     * Metodo privato che ritorna la notifica da inviare sul multicast.
     * @return notifica da inviare sul multicast
     * @throws InterruptedException
     */
    private synchronized String getNotification() throws InterruptedException {
        String notification = null;
        while ((notification = queue.poll()) == null) {
            wait();
        }
        // ---
        return notification;
    }


    /**
     * Metodo pubblico che aggiunge una notifica alla coda e sveglia il thread.
     * @param notification notifica da inviare sul multicast
     */
    public synchronized void readNotification(String notification) {
        queue.offer(notification);
        notify();
    }


    /**
     * run() del thread di MulticastListener.
     * Il Thread che contiene MulticastSender legge le notifiche dalla coda e le invia sul multicast.
     */
    @Override
    public void run() {
        try (MulticastSocket multicastSocket = new MulticastSocket(multicastGroupPort)) {
            // ---
            InetAddress multicastGroup = InetAddress.getByName(multicastGroupAddress);
            if (!multicastGroup.isMulticastAddress()) {
                throw new IllegalArgumentException("[ERROR] indirizzo multicast non valido: " + multicastGroup.getHostAddress());
            }

            // ciclo (virtualmente) infinito dove vengo svegliato,
            // leggo una nuova notifica e invio in multicast
            while (!Thread.currentThread().isInterrupted()) {
                String notification = getNotification();
                System.out.println(notification);
                // ---
                byte[] content = notification.getBytes();
                DatagramPacket packet = new DatagramPacket(content, content.length, multicastGroup, multicastGroupPort);
                multicastSocket.send(packet);
                System.out.println("[MULTICAST] inviato share");
            }
        } catch (UnknownHostException e) {
            System.err.println("[ERROR] indirizzo multicast non valido");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("[ERROR] multicast socket");
            e.printStackTrace();
        } catch (InterruptedException e) {
            // il thread è stato interrotto mentre in wait sulla getNotification in attesa di una notifica
            // goto next iteration e se il thread è stato nel frattempo interrotto, esco dal ciclo
            // e termino l'esecuzione
        }
    }  // run

}  // class MulticastSender
