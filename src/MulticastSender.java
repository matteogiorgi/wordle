import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentLinkedQueue;


public class MulticastSender extends ConcurrentLinkedQueue<String> implements Runnable {

    private final int multicastGroupPort;
    private final String multicastGroupAddress;


    public MulticastSender(int multicastGroupPort, String multicastGroupAddress) {
        super();
        this.multicastGroupPort = multicastGroupPort;
        this.multicastGroupAddress = multicastGroupAddress;
    }


    private synchronized String getNotification() throws InterruptedException {
        String notification = null;
        while ((notification = poll()) == null) {
            wait();
        }
        // ---
        return notification;
    }


    public synchronized void readNotification(String notification) {
        offer(notification);
        notify();
    }


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
            // bye bye
        }
    }
}
