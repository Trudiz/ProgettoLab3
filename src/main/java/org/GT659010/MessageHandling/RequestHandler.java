package org.GT659010.MessageHandling;

import org.GT659010.MessageHandling.RequestMessages.LoginPayload;
import org.GT659010.MessageHandling.RequestMessages.RegisterPayload;
import org.GT659010.MessageHandling.RequestMessages.UpdateCredentialsPayload;
import org.GT659010.UserHandling.User;

import java.util.Map;
import java.util.Objects;

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
        if (!isValidPassword(password)) {
            responseMessage.setResponse(101);
            responseMessage.setErrorMessage("Invalid password!");
        } else if (userMap.containsKey(username)) {
            responseMessage.setResponse(102);
            responseMessage.setErrorMessage("Username not available!");
        } else {
            this.user = new User(username, password);
            userMap.putIfAbsent(username, user);
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
            userToLogin.setOnline(true);
            this.user = userToLogin;
        }
        return responseMessage;
    }

    public ResponseMessage handleLogout(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        if (userMap.containsKey(this.user.getUsername())) {
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

        // 1. Controlla subito se l'utente esiste
        if (!userMap.containsKey(username)) {
            responseMessage.setResponse(102);
            responseMessage.setErrorMessage("Username non-existent!");
            return responseMessage; // Esci subito
        }

        // Ottieni l'utente corretto dalla mappa
        User userToUpdate = userMap.get(username);

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

    public boolean isValidPassword(String password) {
        if (password == null) return false;             // niente NPE
        if (password.length() < 8) return false;        // lunghezza minima
        return true;                                    // passa tutti i test
    }
}
