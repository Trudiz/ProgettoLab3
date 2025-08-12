package org.GT659010;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.GT659010.OrderHandling.*;
import org.GT659010.UserHandling.User;
import org.GT659010.UserHandling.UserManager;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


//FIXARE LOADING DELL'ORDER BOOK ALL'AVVIO
//FIXARE FATTO CHE QUANDO CHIUDE IL SERVER SOVRASCRIVE I VECCHI FILE
//FIXARE CONTROLLI DI SALDO ED EVITARE CHE CRASHI IL CLIENT SE SALDO < DEL NECESSARIO

public class ServerMain {
    static final int MAX = 25;
    public static AtomicInteger orderIdGenerator;
    /* ====== MAPPA GLOBALE ============================================ */
    private static List<HistoricalRecord> historicalOrders;


    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(9000)){
            ExecutorService pool = Executors.newFixedThreadPool(MAX);
            System.out.println("Server has started");
            UserManager.loadUsersFromFile();
            historicalOrders = BookLoader.loadHistory();
            int highestId = historicalOrders.stream()
                    .mapToInt(HistoricalRecord::getOrderId)
                    .max()
                    .orElse(0);
            orderIdGenerator = new AtomicInteger(highestId);
            System.out.println("Order ID: " + orderIdGenerator);
            BlockingQueue<Trade> tradeQueue = new LinkedBlockingQueue<>();
            TradeNotifier notifier = new TradeNotifier(tradeQueue, UserManager.getUsers());
            Thread notifierThread = new Thread(notifier);
            notifierThread.start();
            //POI DA IMPLEMENTARE CHE VIENE INITIALIZED DA FILE
            OrderBook orderBook = new OrderBook(UserManager.getUsers(), tradeQueue);
            BookLoader.loadActiveOrders(orderBook);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nServer - Chiusura in corso, salvataggio ordini attivi...");
                BookLoader.saveActiveOrders(orderBook);
                BookLoader.saveHistory(historicalOrders);
                UserManager.saveUsersToFile();
                System.out.println("Dati salvati. Uscita.");
            }));

            while(true){
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client has connected");
                pool.execute(new ClientHandler(clientSocket, UserManager.getUsers(), orderBook));
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
