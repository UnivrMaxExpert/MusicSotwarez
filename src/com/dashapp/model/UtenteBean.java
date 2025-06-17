package com.dashapp.model;

import java.io.Serializable;
    /*Classe dato per gli utenti*/
public class UtenteBean implements Serializable
{
    private int id;
    private String username;
    private String password;
    private boolean admin;

    public UtenteBean(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public UtenteBean(int id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getId() {return id;
    }

    public void setAdmin(boolean admin) {this.admin = admin;}

    public boolean isAdmin() {
        return admin;
    }
}
