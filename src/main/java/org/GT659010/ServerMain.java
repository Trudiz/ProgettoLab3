package org.GT659010;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerMain {
    static final int MAX = 25;

    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(9000);
             ExecutorService pool = Executors.newFixedThreadPool(MAX)){
            System.out.println("Server has started");

            //Dopo dovrò aggiungere UDP
            //DataInputStream in = new DataInputStream(socket.getInputStream());
            //DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            while(true){
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client has connected");
                pool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
