package org.GT659010.UserHandling;

import org.GT659010.OrderHandling.Order;

import java.net.Socket;
import java.util.UUID;

public class User {
    private String username;
    private String password;
    private String userId;
    private int USDbalance;
    private int BTCbalance;
    private boolean isOnline;
    private Order[] activeOrders;
    private Socket socket;

    public User() {}
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.userId = ;
        this.USDbalance = 2000000;
        this.BTCbalance = 0;
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

    public String getUUID() {
        return userId;
    }
}
