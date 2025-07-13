package org.GT659010;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.GT659010.MessageHandling.RequestMessage;
import org.GT659010.MessageHandling.RequestMessages.*;
import org.GT659010.MessageHandling.ResponseMessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientMain {

    private static boolean clientIsRegistered = false;

    public static void main(String[] args) {
        try (Socket s = new Socket("localhost", 9000);
             BufferedWriter out = new BufferedWriter(
                     new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8));
             BufferedReader  in  = new BufferedReader(
                     new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
             BufferedReader console = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {

            ObjectMapper mapper = new ObjectMapper();

            /* 3️⃣  Interactive loop */
            String msg;
            while ((msg = console.readLine()) != null && !msg.equalsIgnoreCase("exit")) {
                msg = msg.toLowerCase();
                String[] msgParts = msg.split("\\s+");
                RequestMessage requestMsg = createRequest(msgParts);
                if (requestMsg.getPayload() != null) {
                    String jsonOut = mapper.writeValueAsString(requestMsg);
                    out.write(jsonOut);
                    out.write('\n');
                    out.flush();
                    String line = in.readLine();          // blocca finché il server risponde
                    ResponseMessage resp = mapper.readValue(line, ResponseMessage.class);

                    System.out.println("Codice:  " + resp.getResponse());
                    System.out.println("Messaggio: " + resp.getErrorMessage());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();  // shows the real reason instead of “Exception Occurred”
        }
    }

    static public RequestMessage createRequest(String[] tokens) {
        RequestMessage request = new RequestMessage();
        switch(tokens[0]) {
            case "register":
                if (tokens.length == 3 && !clientIsRegistered) {
                    clientIsRegistered = true;
                    RegisterPayload p = new RegisterPayload(tokens[1], tokens[2]);
                    request.setOperation(tokens[0]);
                    request.setPayload(p);
                } else {
                    System.out.println("Wrong Number of arguments!");
                }
                break;
            case "login":
                if (tokens.length == 3 && !clientIsRegistered) {
                    //DA RIMUOVERE DOPO
                    clientIsRegistered = true;
                    LoginPayload p = new LoginPayload(tokens[1], tokens[2]);
                    request.setOperation(tokens[0]);
                    request.setPayload(p);
                } else  {
                    System.out.println("Wrong Number of arguments!");
                }
                break;
            case "updatecredentials":
                if (tokens.length == 4 && clientIsRegistered) {
                    UpdateCredentialsPayload p = new UpdateCredentialsPayload(tokens[1], tokens[2], tokens[3]);
                    request.setOperation(tokens[0]);
                    request.setPayload(p);
                } else {
                    System.out.println("Wrong Number of arguments!");
                }
                break;
            case "insertlimitorder":
                if (tokens.length == 4 && clientIsRegistered) {
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
                if (tokens.length == 3 && clientIsRegistered) {
                    int size = Integer.parseInt(tokens[2]);
                    MarketOrderPayload p = new MarketOrderPayload(tokens[1], size);
                    request.setOperation(tokens[0]);
                    request.setPayload(p);
                } else {
                    System.out.println("Wrong Number of arguments for insertmarketorder!");
                }
                break;
            case "insertstoporder":
                if (tokens.length == 4 && clientIsRegistered) {
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
                if (tokens.length == 2 && clientIsRegistered) {
                    int orderId = Integer.parseInt(tokens[1]);
                    CancelOrderPayload p = new CancelOrderPayload(orderId);
                    request.setOperation(tokens[0]);
                    request.setPayload(p);
                } else {
                    System.out.println("Wrong Number of arguments for cancelorder!");
                }
                break;
            case "getpricehistory":
                if (tokens.length == 2 && clientIsRegistered) {
                    PriceHistoryPayload p = new PriceHistoryPayload(tokens[1]);
                    request.setOperation(tokens[0]);
                    request.setPayload(p);
                } else {
                    System.out.println("Wrong Number of arguments for getpricehistory!");
                }
                break;
            case "logout":
                if (tokens.length == 1 && clientIsRegistered) {
                    clientIsRegistered = false;
                    LogoutPayload p = new LogoutPayload();
                    request.setOperation(tokens[0]);
                    request.setPayload(p);
                } else  {
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
