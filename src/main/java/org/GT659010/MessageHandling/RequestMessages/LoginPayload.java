package org.GT659010.MessageHandling.RequestMessages;

import org.GT659010.MessageHandling.Payload;

public class LoginPayload implements Payload {
    private String username;
    private String password;
    private int udpPort;

    public LoginPayload() {}

    public LoginPayload(String username, String password, int udpPort) {
        this.username = username;
        this.password = password;

        this.udpPort = udpPort;
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

    public int getUdpPort() {
        return udpPort;
    }

    public void setUdpPort(int udpPort) {
        this.udpPort = udpPort;
    }
}
