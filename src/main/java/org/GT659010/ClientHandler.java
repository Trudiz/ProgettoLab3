package org.GT659010;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.GT659010.MessageHandling.RequestMessage;
import org.GT659010.MessageHandling.ResponseMessage;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() throws RuntimeException {

        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
            ObjectMapper mapper = new ObjectMapper();

            String line;
            ResponseMessage responseMessage;

            while ((line = in.readLine()) != null) {
                System.out.println("I'm in the while");
                System.out.println(line);
                RequestMessage requestMessage = mapper.readValue(line, RequestMessage.class);
                HandleRequest(requestMessage);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void HandleRequest(RequestMessage request){
        String operationType = request.getOperation();
        switch (operationType) {
            //handle all types of operations
        }
    }

    public ResponseMessage createResponseMessage(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();

        return responseMessage;
    }
}
