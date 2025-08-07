package org.GT659010.UserHandling;

import org.GT659010.OrderHandling.Order;

import java.net.Socket;
import java.util.UUID;

public class User {
    private String username;
    private String password;
    private String uuid;
    private int USDbalance;
    private int BTCbalance;
    private boolean isOnline;

    public User() {}
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.uuid = UUID.randomUUID().toString();
        this.USDbalance = 20000000;
        this.BTCbalance = 1000000;
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

    public int getUSDbalance() {return USDbalance;}
    public void setUSDbalance(int USDbalance) {
        this.USDbalance = USDbalance;
    }

    public synchronized void decreaseUSDbalance(int amount) {
        if (this.USDbalance < amount) {
            throw new IllegalStateException("Saldo USD insufficiente per l'utente " + this.uuid);
        }
        this.USDbalance -= amount;
    }

    public synchronized void increaseUSDbalance(int amount) {
        this.USDbalance += amount;
    }

    public int getBTCbalance() {
        return BTCbalance;
    }
    public void setBTCbalance(int BTCbalance) {
        this.BTCbalance = BTCbalance;
    }

    public synchronized void decreaseBTCbalance(int amount) {
        if (this.BTCbalance < amount) {
            throw new IllegalStateException("Saldo BTC insufficiente per l'utente " + this.uuid);
        }
        this.BTCbalance -= amount;
    }

    public synchronized void increaseBTCbalance(int amount) {
        this.BTCbalance += amount;
    }

    public boolean isOnline() {
        return isOnline;
    }
    public void setOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public String getUUID() {
        return uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }
}
