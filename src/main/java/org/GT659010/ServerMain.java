package org.GT659010;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.GT659010.OrderHandling.BookMaster;
import org.GT659010.OrderHandling.HistoricalRecord;
import org.GT659010.OrderHandling.Order;
import org.GT659010.OrderHandling.OrderBook;
import org.GT659010.UserHandling.User;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


//FIXARE LOADING DELL'ORDER BOOK ALL'AVVIO
//FIXARE FATTO CHE QUANDO CHIUDE IL SERVER SOVRASCRIVE I VECCHI FILE
//FIXARE CONTROLLI DI SALDO ED EVITARE CHE CRASHI IL CLIENT SE SALDO < DEL NECESSARIO

public class ServerMain {
    static final int MAX = 25;
    public static AtomicInteger orderIdGenerator;
    /* ====== CONFIG =================================================== */
    private static final Path USERFILE = Paths.get("users.json");
    private static final ObjectMapper USERMAPPER =
            new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    /* ====== MAPPA GLOBALE ============================================ */
    public static final ConcurrentHashMap<String,User> USERS = new ConcurrentHashMap<>();
    private static List<HistoricalRecord> historicalOrders;

    /* ====== LOAD all’avvio ========================================== */
    static void load() {
        try {
            if (Files.exists(USERFILE)) {
                Map<String,User> disk =
                        USERMAPPER.readValue(USERFILE.toFile(),
                                new TypeReference<Map<String,User>>() {});
                USERS.putAll(disk);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    /* ====== SAVE dopo ogni modifica ================================= */
    static void saveUser() {
        try {
            // snapshot per evitare ConcurrentModificationException
            USERMAPPER.writeValue(USERFILE.toFile(), new ConcurrentHashMap<>(USERS));
        } catch     (Exception e) { e.printStackTrace(); }
    }

    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(9000)){
            ExecutorService pool = Executors.newFixedThreadPool(MAX);
            System.out.println("Server has started");
            load();
            historicalOrders = BookMaster.loadHistory();
            int highestId = historicalOrders.stream()
                    .mapToInt(HistoricalRecord::getOrderId)
                    .max()
                    .orElse(0);
            orderIdGenerator = new AtomicInteger(highestId);
            System.out.println("Order ID: " + orderIdGenerator);
            //POI DA IMPLEMENTARE CHE VIENE INITIALIZED DA FILE
            OrderBook orderBook = new OrderBook(historicalOrders);
            BookMaster.loadActiveOrders(orderBook);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nServer - Chiusura in corso, salvataggio ordini attivi...");
                BookMaster.saveActiveOrders(orderBook);
                saveUser();
                System.out.println("Dati salvati. Uscita.");
            }));
            //Dopo dovrò aggiungere UDP
            //DataInputStream in = new DataInputStream(socket.getInputStream());
            //DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            while(true){
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client has connected");
                pool.execute(new ClientHandler(clientSocket, USERS, orderBook));
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
