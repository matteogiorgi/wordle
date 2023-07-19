import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MulticastReceiver extends ConcurrentLinkedQueue<String> implements Runnable {

    private final int multicastGroupPort;
    private final String multicastGroupAddress;
    private final String userName;

    public MulticastReceiver(int multicastGroupPort, String multicastGroupAddress, String userName) {
        super();
        this.multicastGroupPort = multicastGroupPort;
        this.multicastGroupAddress = multicastGroupAddress;
        this.userName = userName;
    }


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
                    // goto next iteration
                }
            }
        } catch (IOException ex) {
            System.err.println("[ERROR] ricezione notifica fallita");
            ex.printStackTrace();
        }
    }
}
