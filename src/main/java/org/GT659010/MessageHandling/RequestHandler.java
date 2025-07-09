package org.GT659010.MessageHandling;

import org.GT659010.MessageHandling.RequestMessages.LoginPayload;
import org.GT659010.MessageHandling.RequestMessages.RegisterPayload;
import org.GT659010.UserHandling.User;

import java.util.Map;

public class RequestHandler {
    private final Map<String, User> userMap;
    private User user;

    public RequestHandler(Map<String, User> userMap) {
        this.userMap = userMap;
    }

    public ResponseMessage  handleRegistration(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        RegisterPayload r = (RegisterPayload) requestMessage.getPayload();
        String username = r.getUsername();
        String password = r.getPassword();
        this.user = new User(username, password);
        User old = userMap.putIfAbsent(username, user);
        if (!isValidPassword(password)) {
            responseMessage.setResponse(101);
            responseMessage.setErrorMessage("Invalid password!");
        } else if (old != null) {
            responseMessage.setResponse(102);
            responseMessage.setErrorMessage("Username not available!");
        } else {
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
        User userToLogin = userMap.get(username);
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
            this.user = userToLogin;
            userToLogin.setOnline(true);
        }
        return responseMessage;
    }

    public boolean isValidPassword(String password) {
        if (password == null) return false;             // niente NPE
        if (password.length() < 8) return false;        // lunghezza minima
        return true;                                    // passa tutti i test
    }
}
