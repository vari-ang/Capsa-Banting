/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author macintosh
 */
public class User {
    private String username;
    private String password;
    
    Connection conn;
    ResultSet result;
    Statement stat;
    
    public User() {
        this.getConnection();
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.getConnection();
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
    
    public Connection getConnection(){
        try {
            Class.forName("com.mysql.jdbc.Driver");
            
            DB_connection dbc = new DB_connection();
            conn = DriverManager.getConnection("jdbc:mysql://localhost/peter_capsa", dbc.getUsername(), dbc.getPassword());
        } catch (Exception ex) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
        }
        return conn;
    }
    
    public boolean signUp(String _username, String _password) {
        boolean res = true;
        
        try {
            stat = (Statement) conn.createStatement();
            if(!conn.isClosed()) {
                PreparedStatement sql1 = (PreparedStatement) conn.prepareStatement("SELECT * FROM user WHERE username = ?");
                sql1.setString(1, _username);
                result = sql1.executeQuery();
                
                int rowCount = 0;
                while(result.next()) { rowCount++; }
                
                // User has not registered
                if(rowCount == 0) {
                    // Register the user
                    PreparedStatement sql2 = (PreparedStatement) conn.prepareStatement("INSERT INTO user VALUES(?,?)");
                    sql2.setString(1, _username);
                    sql2.setString(2, _password);
                    sql2.execute();
                }
                // User has already registered
                else { 
                    res = false;
                }
            }
        }
        catch(SQLException exc) {
            System.out.println(exc);
            res = false;
        }
        
        return res;
    }
    
    public boolean logIn(String _username, String _password){
        ArrayList<User> collection = new ArrayList<>();
        boolean res = true;
        
        try {
            stat = (Statement) conn.createStatement();
            if(!conn.isClosed()) {
                PreparedStatement sql = (PreparedStatement) conn.prepareStatement("SELECT * FROM user WHERE username = ? AND password = ?");
                sql.setString(1, _username);
                sql.setString(2, _password);
                result = sql.executeQuery();

                while(result.next()) {
                    User user = new User();
                    user.setUsername(_username);
                    user.setPassword(_password);
                    collection.add(user); // input to ArrayList
                }
                
                if(collection.isEmpty()) { res = false; }
            }
        }
        catch(SQLException exc) {
            System.out.println(exc);
            res = false;
        }
        
        return res;
    }
}
