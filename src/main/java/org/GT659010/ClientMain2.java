package org.GT659010;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.GT659010.MessageHandling.RequestMessage;
import org.GT659010.MessageHandling.RequestMessages.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import java.io.*;
import java.net.Socket;

public class ClientMain2 {
    public static void main(String[] args) {
        try (Socket   socket      = new Socket("localhost", 9000);
             // read server’s first message: a UTF string that *is* JSON
             DataInputStream  srvIn  = new DataInputStream(socket.getInputStream());
             DataOutputStream srvOut = new DataOutputStream(socket.getOutputStream());
             // read your keyboard
             BufferedReader   console = new BufferedReader(new InputStreamReader(System.in))) {

            ObjectMapper mapper = new ObjectMapper();

            /* 3️⃣  Interactive loop */
            String msg;
            while ((msg = console.readLine()) != null && !msg.equalsIgnoreCase("exit")) {
                msg = msg.toLowerCase();
                String[] msgParts = msg.split("\\s+");
                RequestMessage requestMsg = createRequest(msgParts);
                String jsonOut = mapper.writeValueAsString(requestMsg);
                srvOut.writeUTF(jsonOut);
                srvOut.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();  // shows the real reason instead of “Exception Occurred”
        }
    }

    static public RequestMessage createRequest(String[] tokens) {
        RequestMessage request = new RequestMessage();
        switch(tokens[0]) {
            case "register":
                if (tokens.length == 3) {
                    RegisterPayload p = new RegisterPayload(tokens[1], tokens[2]);
                    request.setOperation(tokens[0]);
                    request.setPayload(p);
                } else {
                    System.out.println("Wrong Number of arguments!");
                }
                break;
            case "login":
                if (tokens.length == 3) {
                    LoginPayload p = new LoginPayload(tokens[1], tokens[2]);
                    request.setOperation(tokens[0]);
                    request.setPayload(p);
                } else  {
                    System.out.println("Wrong Number of arguments!");
                }
                break;
            case "updatecredentials":
                if (tokens.length == 4) {
                    UpdateCredentialsPayload p = new UpdateCredentialsPayload(tokens[1], tokens[2], tokens[3]);
                    request.setOperation(tokens[0]);
                    request.setPayload(p);
                } else {
                    System.out.println("Wrong Number of arguments!");
                }
                break;
            case "insertlimitorder":
                if (tokens.length == 4) {
                    int size = Integer.parseInt(tokens[2]);
                    int price = Integer.parseInt(tokens[3]);
                    LimitOrderPayload p = new LimitOrderPayload(tokens[1], size, price);
                    request.setOperation(tokens[0]);
                    request.setPayload(p);
                } else {
                    System.out.println("Wrong Number of arguments for insertlimitorder!");
                }
                break;
            case "insertmarketorder":
                if (tokens.length == 3) {
                    int size = Integer.parseInt(tokens[2]);
                    MarketOrderPayload p = new MarketOrderPayload(tokens[1], size);
                    request.setOperation(tokens[0]);
                    request.setPayload(p);
                } else {
                    System.out.println("Wrong Number of arguments for insertmarketorder!");
                }
                break;
            case "insertstoporder":
                if (tokens.length == 4) {
                    int size = Integer.parseInt(tokens[2]);
                    int price = Integer.parseInt(tokens[3]);
                    StopOrderPayload p = new StopOrderPayload(tokens[1], size, price);
                    request.setOperation(tokens[0]);
                    request.setPayload(p);
                } else {
                    System.out.println("Wrong Number of arguments for insertstoporder!");
                }
                break;
            case "cancelorder":
                if (tokens.length == 2) {
                    int orderId = Integer.parseInt(tokens[1]);
                    CancelOrderPayload p = new CancelOrderPayload(orderId);
                    request.setOperation(tokens[0]);
                    request.setPayload(p);
                } else {
                    System.out.println("Wrong Number of arguments for cancelorder!");
                }
                break;
            case "getpricehistory":
                if (tokens.length == 2) {
                    PriceHistoryPayload p = new PriceHistoryPayload(tokens[1]);
                    request.setOperation(tokens[0]);
                    request.setPayload(p);
                } else {
                    System.out.println("Wrong Number of arguments for getpricehistory!");
                }
                break;
            case "logout":
                if (tokens.length == 1) {
                    LogoutPayload p = new LogoutPayload();
                    request.setOperation(tokens[0]);
                    request.setPayload(p);
                } else   {
                    System.out.println("Wrong Number of arguments!");
                }
                break;
            default:
                System.out.println("Unknow command!");
                break;
        }
        return request;
    }
}

