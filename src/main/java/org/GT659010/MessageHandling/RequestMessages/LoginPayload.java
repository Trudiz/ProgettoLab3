package org.GT659010.MessageHandling.RequestMessages;

import org.GT659010.MessageHandling.Payload;

public class LoginPayload implements Payload {
    private String username;
    private String password;

    public LoginPayload() {}

    public LoginPayload(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
