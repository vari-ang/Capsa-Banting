/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 *
 * @author macintosh
 */
public class DB_connection {
    private String username;
    private String password;

    public DB_connection() {
        setUsername("root");
        setPassword("080798");
    }
    
    public DB_connection(String _username, String _password) {
        setUsername(_username);
        setPassword(_password);
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
