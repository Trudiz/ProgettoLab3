package org.GT659010;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.GT659010.OrderHandling.OrderBook;
import org.GT659010.UserHandling.User;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerMain {
    static final int MAX = 25;
    /* ====== CONFIG =================================================== */
    private static final Path FILE = Paths.get("users.json");
    private static final ObjectMapper USERMAPPER =
            new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    /* ====== MAPPA GLOBALE ============================================ */
    private static final ConcurrentHashMap<String,User> USERS = new ConcurrentHashMap<>();

    /* ====== LOAD all’avvio ========================================== */
    static void load() {
        try {
            if (Files.exists(FILE)) {
                Map<String,User> disk =
                        USERMAPPER.readValue(FILE.toFile(),
                                new TypeReference<Map<String,User>>() {});
                USERS.putAll(disk);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    /* ====== SAVE dopo ogni modifica ================================= */
    static void saveUser() {
        try {
            // snapshot per evitare ConcurrentModificationException
            USERMAPPER.writeValue(FILE.toFile(), new ConcurrentHashMap<>(USERS));
        } catch     (Exception e) { e.printStackTrace(); }
    }

    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(9000);
             ExecutorService pool = Executors.newFixedThreadPool(MAX)){
            System.out.println("Server has started");
            load();
            //POI DA IMPLEMENTARE CHE VIENE INITIALIZED DA FILE
            OrderBook orderBook = new OrderBook();
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
