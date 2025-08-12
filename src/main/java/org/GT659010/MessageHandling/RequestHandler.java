package org.GT659010.MessageHandling;

import org.GT659010.MessageHandling.RequestMessages.*;
import org.GT659010.OrderHandling.BookLoader;
import org.GT659010.OrderHandling.Order;
import org.GT659010.OrderHandling.OrderBook;
import org.GT659010.OrderHandling.OrderTypes.LimitOrder;
import org.GT659010.OrderHandling.OrderTypes.MarketOrder;
import org.GT659010.OrderHandling.OrderTypes.StopOrder;
import org.GT659010.OrderHandling.Side;
import org.GT659010.ServerMain;
import org.GT659010.UserHandling.User;

import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class RequestHandler {
    private final Map<String, User> userMap;
    private final OrderBook orderBook;
    private User user;
    private Socket clientSocket;

    public RequestHandler(Map<String, User> userMap, OrderBook orderBook, Socket clientSocket) {
        this.userMap = userMap;
        this.orderBook = orderBook;
        this.clientSocket = clientSocket;
    }

    public ResponseMessage handleRegistration(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        RegisterPayload r = (RegisterPayload) requestMessage.getPayload();
        String username = r.getUsername();
        String password = r.getPassword();
        if (!isValidPassword(password)) {
            responseMessage.setResponse(101);
            responseMessage.setErrorMessage("Invalid password!");
        } else if (userMap.containsKey(username)) {
            responseMessage.setResponse(102);
            responseMessage.setErrorMessage("Username not available!");
        } else {
            this.user = new User(username, password);
            // Recupera la porta UDP dal payload
            int clientUdpPort = r.getUdpPort();
            // Recupera l'IP dal socket TCP
            InetAddress clientIp = this.clientSocket.getInetAddress();
            // Salva le informazioni sull'utente
            this.user.setUdpAddress(clientIp);
            this.user.setUdpPort(clientUdpPort);

            userMap.putIfAbsent(this.user.getUUID(), this.user);
            System.out.println("DEBUG-REQUESTHANDLER: Utente " + this.user.getUUID() + " registrato. Contenuto della mappa passata: " + userMap.keySet());
            responseMessage.setResponse(100);
            responseMessage.setErrorMessage("OK!");
        }
        return responseMessage;
    }

    public ResponseMessage handleLogin(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        LoginPayload r = (LoginPayload) requestMessage.getPayload();
        String username = r.getUsername();
        String password = r.getPassword();
        User userToLogin = userMap.values().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(null);
        if (userToLogin == null) {
            responseMessage.setResponse(101);
            responseMessage.setErrorMessage("username/password mismatch or non‑existent username");
        } else if (!userToLogin.getPassword().equals(password) || !userToLogin.getUsername().equals(username)) {
            responseMessage.setResponse(101);
            responseMessage.setErrorMessage("username/password mismatch or non‑existent username");
        } else if (userToLogin.isOnline()) {
            responseMessage.setResponse(102);
            responseMessage.setErrorMessage("User already logged in");
        } else {
            responseMessage.setResponse(100);
            responseMessage.setErrorMessage("OK!");
            userToLogin.setOnline(true);
            this.user = userToLogin;
            int clientUdpPort = r.getUdpPort();
            // Recupera l'IP dal socket TCP
            InetAddress clientIp = this.clientSocket.getInetAddress();
            // Salva le informazioni sull'utente
            this.user.setUdpAddress(clientIp);
            this.user.setUdpPort(clientUdpPort);
            System.out.println("DEBUG-REQUESTHANDLER: Utente " + this.user.getUUID() + " registrato. Contenuto della mappa passata: " + userMap.keySet());
        }
        return responseMessage;
    }

    public ResponseMessage handleLogout(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        User userToLogout = userMap.values().stream()
                .filter(u -> u.getUsername().equals(this.user.getUsername()))
                .findFirst()
                .orElse(null);
        if (userMap.containsKey(userToLogout.getUUID())) {
            this.user.setOnline(false);
            responseMessage.setResponse(100);
            responseMessage.setErrorMessage("OK!");
        } else {
            responseMessage.setResponse(101);
            responseMessage.setErrorMessage("username/connection mismatch, non‑existent username, user not logged in, or other error cases!");
        }
        return responseMessage;
    }

    public ResponseMessage handleUpdateCredentials(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        UpdateCredentialsPayload r = (UpdateCredentialsPayload) requestMessage.getPayload();
        String username = r.getUsername();
        String oldPswrd = r.getOldPassword();
        String newPswrd = r.getNewPassword();

        User userToUpdate = userMap.values().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(null);
        // 1. Controlla subito se l'utente esiste
        if (userToUpdate == null) {
            responseMessage.setResponse(102);
            responseMessage.setErrorMessage("Username non-existent!");
            return responseMessage; // Esci subito
        }
        // 2. Esegui tutti i controlli di validazione in sequenza
        if (!userToUpdate.getPassword().equals(oldPswrd)) {
            responseMessage.setResponse(102);
            responseMessage.setErrorMessage("Old password mismatch!");
        } else if (newPswrd.equals(oldPswrd)) {
            responseMessage.setResponse(103);
            responseMessage.setErrorMessage("New password is the same as the old password!");
        } else if (!isValidPassword(newPswrd)) {
            responseMessage.setResponse(101);
            responseMessage.setErrorMessage("Invalid new password!");
        } else if (userToUpdate.isOnline()) {
            responseMessage.setResponse(104);
            responseMessage.setErrorMessage("User currently logged in!");
        } else {
            // 3. Se tutti i controlli sono superati, AGGIORNA la password
            userToUpdate.setPassword(newPswrd); // <-- L'OPERAZIONE MANCANTE!
            responseMessage.setResponse(100);
            responseMessage.setErrorMessage("OK!");
        }
        return responseMessage;
    }

    public ResponseMessage handleMarketOrder (RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        MarketOrderPayload r = (MarketOrderPayload) requestMessage.getPayload();
        Side side = r.getType();
        int size = r.getSize();
        MarketOrder marketOrder = new MarketOrder(this.user.getUUID(), side, size);
        orderBook.addNewOrder(marketOrder);
        responseMessage.setResponse(marketOrder.getOrderId());
        responseMessage.setErrorMessage("OK!");
        return responseMessage;
    }

    public ResponseMessage handleLimitOrder (RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        LimitOrderPayload r = (LimitOrderPayload) requestMessage.getPayload();
        Side side = r.getType();
        int size = r.getSize();
        int price = r.getPrice();
        LimitOrder limitOrder = new LimitOrder(this.user.getUUID(), side, size, price);
        orderBook.addNewOrder(limitOrder);
        responseMessage.setResponse(limitOrder.getOrderId());
        responseMessage.setErrorMessage("OK!");
        return responseMessage;
    }

    public ResponseMessage handleStopOrder (RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        StopOrderPayload r = (StopOrderPayload) requestMessage.getPayload();
        Side side = r.getType();
        int size = r.getSize();
        int price = r.getPrice();
        StopOrder stopOrder = new StopOrder(this.user.getUUID(), side, size, price);
        orderBook.addNewOrder(stopOrder);
        responseMessage.setResponse(stopOrder.getOrderId());
        responseMessage.setErrorMessage("OK!");
        return responseMessage;
    }

    public ResponseMessage handleCancelOrder (RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        CancelOrderPayload r = (CancelOrderPayload) requestMessage.getPayload();
        int orderId = r.getOrderId();
        boolean cancelled = orderBook.processCancelOrder(orderId);
        if (cancelled) {
            responseMessage.setResponse(100);
            responseMessage.setErrorMessage("Order cancelled!");
            return responseMessage;
        }
        else {
            responseMessage.setResponse(101);
            responseMessage.setErrorMessage("Order does not exist / belongs to different user / already finalized / other error cases");
            return responseMessage;
        }
    }

    public ResponseMessage handleGetPriceHistory (RequestMessage requestMessage) {
        ResponseMessage response = new ResponseMessage(); // La nuova classe
        try {
            PriceHistoryPayload r = (PriceHistoryPayload) requestMessage.getPayload();
            String monthString = r.getMonth();
            List<PriceResponseMessage> ohlcData = BookLoader.getDailyOHLCForMonth(monthString);

            // Prepara una risposta di successo
            response.setResponse(100);
            response.setErrorMessage("Dati storici recuperati con successo.");
            response.setPayload(ohlcData); // Metti la lista nel payload!

        } catch (Exception e) {
            // Prepara una risposta di errore
            response.setResponse(101);
            response.setErrorMessage("Errore interno del server.");
            response.setPayload(null); // Nessun dato da inviare
            e.printStackTrace();
        }
        return response;
    }

    public ResponseMessage handleGetUserActiveOrders (RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        List<Order> activeOrders = this.orderBook.getActiveOrdersForUser(this.user.getUUID());
        responseMessage.setResponse(100);
        responseMessage.setErrorMessage("OK!");
        responseMessage.setPayload(activeOrders);
        return responseMessage;
    }

    public boolean isValidPassword(String password) {
        if (password == null) return false;             // niente NPE
        if (password.length() < 8) return false;        // lunghezza minima
        return true;                                    // passa tutti i test
    }
}
