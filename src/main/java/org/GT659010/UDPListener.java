package org.GT659010;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPListener implements Runnable {
    private final DatagramSocket socket;

    public UDPListener(DatagramSocket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        byte[] receiveBuffer = new byte[65535]; // Buffer per ricevere i dati
        System.out.println("[UDP Listener started on port " + socket.getLocalPort() + "]");

        while (!Thread.currentThread().isInterrupted()) {
            try {
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket); // Chiamata bloccante: aspetta un pacchetto

                String notification = new String(receivePacket.getData(), 0, receivePacket.getLength());

                System.out.println("\n--- 🔔 Notifica Trade Ricevuta ---");
                System.out.println(notification);
                System.out.print("> "); // Ristampa il prompt per l'utente

            } catch (Exception e) {
                System.err.println("UDP Listener error: " + e.getMessage());
                // Se il socket è stato chiuso, interrompi il thread
                if (socket.isClosed()) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        System.out.println("[UDP Listener stopped]");
    }
}
