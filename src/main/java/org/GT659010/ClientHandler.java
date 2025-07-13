package org.GT659010;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.GT659010.MessageHandling.Payload;
import org.GT659010.MessageHandling.RequestHandler;
import org.GT659010.MessageHandling.RequestMessage;
import org.GT659010.MessageHandling.RequestMessages.RegisterPayload;
import org.GT659010.MessageHandling.ResponseMessage;
import org.GT659010.UserHandling.User;

import java.io.*;
import java.net.Socket;
import java.util.Map;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final Map<String, User> userMap;

    public ClientHandler(Socket socket, Map<String, User> userMap) {
        this.socket = socket;
        this.userMap = userMap;
    }

    @Override
    public void run() throws RuntimeException {

        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
            ObjectMapper mapper = new ObjectMapper();

            String line;
            String jsonOut;
            ResponseMessage responseMessage;
            RequestHandler requestHandler = new RequestHandler(userMap);

            while ((line = in.readLine()) != null) {
                System.out.println("I'm in the while");
                System.out.println(line);
                RequestMessage requestMessage = mapper.readValue(line, RequestMessage.class);
                String operation = requestMessage.getOperation();
                switch (operation) {
                    case "register":
                        responseMessage = requestHandler.handleRegistration(requestMessage);
                        System.out.println("Register operation successful");
                        //Dopo invio
                        if (responseMessage.getResponse() == 100) {
                            ServerMain.save();
                        }
                        System.out.println("Salvato con successo il file");
                        jsonOut = mapper.writeValueAsString(responseMessage);
                        break;
                    case "login":
                        responseMessage = requestHandler.handleLogin(requestMessage);
                        //Dopo invio
                        if (responseMessage.getResponse() == 100) {
                            ServerMain.save();
                        }
                        jsonOut = mapper.writeValueAsString(responseMessage);
                        break;
                    case "logout":
                        responseMessage = requestHandler.handleLogout(requestMessage);

                        if (responseMessage.getResponse() == 100) {
                            ServerMain.save();
                        }
                        jsonOut = mapper.writeValueAsString(responseMessage);
                        break;
                    case "updatecredentials":
                        responseMessage = requestHandler.handleUpdateCredentials(requestMessage);

                        if (responseMessage.getResponse() == 100) {
                            ServerMain.save();
                        }
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

    public ResponseMessage createResponseMessage(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();

        return responseMessage;
    }
}
