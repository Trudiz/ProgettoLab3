package org.GT659010;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.GT659010.MessageHandling.Payload;
import org.GT659010.MessageHandling.RequestHandler;
import org.GT659010.MessageHandling.RequestMessage;
import org.GT659010.MessageHandling.RequestMessages.RegisterPayload;
import org.GT659010.MessageHandling.ResponseMessage;
import org.GT659010.OrderHandling.OrderBook;
import org.GT659010.UserHandling.User;

import java.io.*;
import java.net.Socket;
import java.util.Map;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final Map<String, User> userMap;
    private final OrderBook orderBook;

    public ClientHandler(Socket socket, Map<String, User> userMap, OrderBook book) {
        this.socket = socket;
        this.userMap = userMap;
        this.orderBook = book;
    }

    @Override
    public void run() throws RuntimeException {

        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
            ObjectMapper mapper = new ObjectMapper();

            String line;
            String jsonOut;
            ResponseMessage responseMessage;
            RequestHandler requestHandler = new RequestHandler(userMap, orderBook, socket);

            while ((line = in.readLine()) != null) {
                System.out.println("I'm in the while");
                System.out.println(line);
                RequestMessage requestMessage = mapper.readValue(line, RequestMessage.class);
                String operation = requestMessage.getOperation();
                switch (operation) {
                    case "register":
                        responseMessage = requestHandler.handleRegistration(requestMessage);
                        jsonOut = mapper.writeValueAsString(responseMessage);
                        break;
                    case "login":
                        responseMessage = requestHandler.handleLogin(requestMessage);
                        jsonOut = mapper.writeValueAsString(responseMessage);
                        break;
                    case "logout":
                        responseMessage = requestHandler.handleLogout(requestMessage);
                        jsonOut = mapper.writeValueAsString(responseMessage);
                        break;
                    case "updatecredentials":
                        responseMessage = requestHandler.handleUpdateCredentials(requestMessage);
                        jsonOut = mapper.writeValueAsString(responseMessage);
                        break;
                    case "insertmarketorder":
                        responseMessage = requestHandler.handleMarketOrder(requestMessage);
                        jsonOut = mapper.writeValueAsString(responseMessage);
                        break;
                    case "insertlimitorder":
                        responseMessage = requestHandler.handleLimitOrder(requestMessage);
                        jsonOut = mapper.writeValueAsString(responseMessage);
                        break;
                    case "insertstoporder":
                        responseMessage = requestHandler.handleStopOrder(requestMessage);
                        jsonOut = mapper.writeValueAsString(responseMessage);
                        break;
                    case "cancelorder":
                        responseMessage = requestHandler.handleCancelOrder(requestMessage);
                        jsonOut = mapper.writeValueAsString(responseMessage);
                        break;
                    case "getpricehistory":
                        responseMessage = requestHandler.handleGetPriceHistory(requestMessage);
                        jsonOut = mapper.writeValueAsString(responseMessage);
                        break;
                    case "getactiveorders":
                        responseMessage = requestHandler.handleGetUserActiveOrders(requestMessage);
                        jsonOut = mapper.writeValueAsString(responseMessage);
                        break;
                    default:
                        responseMessage = new ResponseMessage();
                        responseMessage.setResponse(999);
                        responseMessage.setErrorMessage("Invalid operation");
                        jsonOut = mapper.writeValueAsString(responseMessage);
                }
                out.write(jsonOut);
                out.write('\n');
                out.flush();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
