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
public class Room {
    private int id;
    private String name;
    private String host;
    private String status;
    
    public Room() {
        this.getConnection();
    }

    public Room(int id, String nama, String host, String status) {
        this.id = id;
        this.name = nama;
        this.host = host;
        this.status = status;
        
        this.getConnection();
    }

    private Connection conn;
    private ResultSet result;
    private Statement stat;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String nama) {
        this.name = nama;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Connection getConn() {
        return conn;
    }

    public void setConn(Connection conn) {
        this.conn = conn;
    }

    public ResultSet getResult() {
        return result;
    }

    public void setResult(ResultSet result) {
        this.result = result;
    }

    public Statement getStat() {
        return stat;
    }

    public void setStat(Statement stat) {
        this.stat = stat;
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
    
    public boolean create(String _name, String _host) {
        boolean res = true;
        try {
            stat = (Statement) conn.createStatement();
            if(!conn.isClosed()) {
                // CREATE GAME ROOM
                PreparedStatement sql = (PreparedStatement) conn.prepareStatement("INSERT INTO room(name,host,status) VALUES(?,?,?)");
                sql.setString(1, _name);
                sql.setString(2, _host);
                sql.setString(3, "MENUNGGU");
                sql.execute();
            }
        }
        catch(SQLException exc) {
            System.out.println(exc);
            res = false;
        }
        return res;
    }
    
    public ArrayList<Room> displayAll() {
        ArrayList<Room> collection = new ArrayList<>();
        try {
            stat = (Statement) conn.createStatement();
            
            // execute select query & store to result variable
            result = stat.executeQuery("SELECT * FROM room");
            
            while(result.next()) {
                // change the data of selected results to Book object
                Room r = new Room(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getString("host"),
                        result.getString("status")                       
                );
                collection.add(r); // input to ArrayList
            }
        }
        catch(Exception exc) {
            System.out.println(exc);
        }
        return collection;
    }
    
    public String join(int _idRoom, String _username) {
        String res = "";
        int total = 0;
        try {
            stat = (Statement) conn.createStatement();
            if(!conn.isClosed()) {
                // ONLY PROCEEDS IF LESS THAN 4 USERS HAVE JOINED
                PreparedStatement sql1 = (PreparedStatement) conn.prepareStatement("SELECT COUNT(*) AS total FROM user_in_room WHERE id_room = ?");
                sql1.setInt(1, _idRoom);
                result = sql1.executeQuery();
                while(result.next()){ total = result.getInt("total"); }
                
                if(total >= 4) {
                    res = "Mohon maaf. Room yang Anda pilih penuh, sedang atau sudah selesai bermain.";
                }
                else {
                    // JOIN GAME ROOM
                    PreparedStatement sql2 = (PreparedStatement) conn.prepareStatement("INSERT INTO user_in_room(id_room, username) VALUES(?,?)");
                    sql2.setInt(1, _idRoom);
                    sql2.setString(2, _username);
                    sql2.execute();
                    
                    res = "Anda telah berhasil bergabung";
                }
            }
        }
        catch(SQLException exc) {
            System.out.println(exc);
            res = "Terjadi kesalahan pada sistem. Silahkan coba lagi";
        }
        return res;
    }
    
    // check total number of users on table 'user_in_room'
    public int getUserCountInRoom(int _idRoom) {
        int total = 0;
        
        try {
            stat = (Statement) conn.createStatement();
            if(!conn.isClosed()) {
                PreparedStatement sql = (PreparedStatement) conn.prepareStatement("SELECT COUNT(*) AS total FROM user_in_room WHERE id_room = ?");
                sql.setInt(1, _idRoom);
                result = sql.executeQuery();
                
                while(result.next()){ total = result.getInt("total"); }
            }
        }
        catch(SQLException exc) {
            System.out.println(exc);
        }
        
        return total;
    }
    
    public boolean exitGame(int _idRoom, String _username) {
        boolean res = true;
        try {
            stat = (Statement) conn.createStatement();
            if(!conn.isClosed()) {
                // CREATE GAME ROOM
                PreparedStatement sql = (PreparedStatement) conn.prepareStatement("DELETE FROM user_in_room WHERE id_room=? AND username=?");
                sql.setInt(1, _idRoom);
                sql.setString(2, _username);
                sql.execute();
            }
        }
        catch(SQLException exc) {
            System.out.println(exc);
            res = false;
        }
        return res;
    }
    
    public boolean updateStatus(int _idRoom, String _status) {
        boolean res = true;
        try {
            stat = (Statement) conn.createStatement();
            if(!conn.isClosed()) {
                // CREATE GAME ROOM
                PreparedStatement sql = (PreparedStatement) conn.prepareStatement("UPDATE room SET status=? WHERE id=?");
                sql.setString(1, _status);
                sql.setInt(2, _idRoom);
                sql.execute();
            }
        }
        catch(SQLException exc) {
            System.out.println(exc);
            res = false;
        }
        return res;
    }
}
