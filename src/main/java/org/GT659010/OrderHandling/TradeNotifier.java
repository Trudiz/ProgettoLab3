package org.GT659010.OrderHandling;

// Crea una nuova classe, es. TradeNotifier.java
import org.GT659010.UserHandling.User;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class TradeNotifier implements Runnable {
    private final BlockingQueue<Trade> tradeQueue;
    private final Map<String, User> users;
    private final DatagramSocket socket; // Il socket ora appartiene a questa classe

    public TradeNotifier(BlockingQueue<Trade> tradeQueue, Map<String, User> users) throws SocketException {
        this.tradeQueue = tradeQueue;
        this.users = users;
        // Inizializza il socket qui, una sola volta
        this.socket = new DatagramSocket();
        System.out.println("✅ TradeNotifier creato e pronto a inviare da " + socket.getLocalSocketAddress());
    }

    @Override
    public void run() {
        System.out.println("TradeNotifier in ascolto sulla coda dei trade...");
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Trade trade = tradeQueue.take();
                System.out.println("Notifier: Nuovo trade da notificare -> " + trade);

                User buyer = this.users.get(trade.getBuyerUserId());
                User seller = this.users.get(trade.getSellerUserId());

                String notificationJson = createNotificationPayloadForTrade(trade);

                // Ora chiama il suo stesso metodo privato per inviare
                if (buyer != null && buyer.isOnline()) this.sendUdpNotification(buyer, notificationJson);
                if (seller != null && seller.isOnline()) this.sendUdpNotification(seller, notificationJson);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("TradeNotifier interrotto.");
            }
        }
        close(); // Chiudi il socket quando il thread termina
    }

    /**
     * Metodo privato per inviare la notifica UDP. La logica è ora interna.
     */
    private void sendUdpNotification(User user, String jsonPayload) {
        if (user == null || user.getUdpAddress() == null || user.getUdpPort() == 0) {
            return;
        }
        try {
            byte[] payloadBytes = jsonPayload.getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(
                    payloadBytes,
                    payloadBytes.length,
                    user.getUdpAddress(),
                    user.getUdpPort()
            );
            this.socket.send(packet);
        } catch (IOException e) {
            System.err.println("Notifier: Impossibile inviare notifica a " + user.getUUID());
        }
    }

    private String createNotificationPayloadForTrade(Trade trade) {
        // ... Logica per creare il JSON ...
        return String.format("{\"notification\": \"closedTrades\", \"trades\": [{\"price\":%d, \"size\":%d}]}",
                trade.getPrice(), trade.getSize());
    }

    /**
     * Metodo per chiudere il socket quando il server si spegne.
     */
    public void close() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
            System.out.println("Socket UDP del Notifier chiuso.");
        }
    }
}
