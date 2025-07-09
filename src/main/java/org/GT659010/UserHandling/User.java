package org.GT659010.UserHandling;

import java.net.Socket;

public class User {
    private String username;
    private String password;
    private Long USDbalance;
    private Long BTCbalance;
    private boolean isOnline;
    //private Order activeOrders;
    private Socket socket;

    public User() {}
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.USDbalance = 2000000L;
        this.BTCbalance = 0L;
        this.isOnline = true;
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

    public Long getUSDbalance() {
        return USDbalance;
    }
    public void setUSDbalance(Long USDbalance) {
        this.USDbalance = USDbalance;
    }

    public Long getBTCbalance() {
        return BTCbalance;
    }
    public void setBTCbalance(Long BTCbalance) {
        this.BTCbalance = BTCbalance;
    }

    public boolean isOnline() {
        return isOnline;
    }
    public void setOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }
}
