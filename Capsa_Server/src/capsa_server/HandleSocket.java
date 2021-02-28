/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package capsa_server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.User;
import model.Room;

/**
 *
 * @author macintosh
 */
public class HandleSocket extends Thread {
    MainServer parent;
    int priority;
    Socket client;
    BufferedReader in;
    DataOutputStream out;
    User user;
    Room room;
    String cmd; // to store multi-command from client
    
    public HandleSocket(MainServer parent, int priority, Socket client, String name) {
        super(name);
        try {
            this.parent = parent;
            this.priority = priority;
            this.client = client;
            user = new User();
            room = new Room();
            
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out  = new DataOutputStream(client.getOutputStream());
        } catch (IOException ex) {
            Logger.getLogger(HandleSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String refreshRoom() {
        ArrayList<Room> rooms = room.displayAll();
        String msg = "";
        for (Room r : rooms) {
            msg += r.getId() + ":" + r.getName() + ":" + r.getHost() + ":" + r.getStatus() + ";";
        }
        
        return msg;
    }
    
    public void broadcast(String _text) {
        try {
            out.writeBytes(_text + "\n");
        } catch (IOException ex) {
            Logger.getLogger(HandleSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void updateRoomStatus(int _idRoom, String _status) {
        room.updateStatus(_idRoom, _status);
    }

    @Override
    public void run() {
        while(true) {
            try {
                cmd = in.readLine();
                if(cmd.equals("SIGNUP")) {
                    String username = in.readLine();
                    String password = in.readLine();
                    System.out.println(username + " SIGN UP");
                    
                    if(user.signUp(username, password)) {
                        out.writeBytes("SUCCESS\n");
                    }
                    else {
                        out.writeBytes("FAILED\n");
                    }
                }
                else if(cmd.equals("LOGIN")) {
                    String username = in.readLine();
                    String password = in.readLine();
                    System.out.println(username + " LOG IN");
                    
                    if(user.logIn(username, password)) {
                        out.writeBytes("SUCCESS\n");
                    }
                    else {
                        out.writeBytes("FAILED\n");
                    }
                }
                else if(cmd.equals("EXIT")) { System.out.println("EXIT"); break; }
                else if(cmd.equals("CREATEROOM")) { 
                    String roomName = in.readLine();
                    String host = in.readLine();
                    System.out.println(host + " CREATE ROOM " + roomName);
                    
                    if(room.create(roomName, host)) {
                        out.writeBytes("SUCCESS\n");
                    }
                    else {
                        out.writeBytes("FAILED\n");
                    }
                }
                else if(cmd.equals("REFRESH")) {
                    System.out.println("REFRESH");
                    
                    out.writeBytes(refreshRoom() + "\n");
                }
                else if(cmd.equals("JOINROOM")) {
                    int idRoom = Integer.parseInt(in.readLine()); // get ID room that the user wants to join
                    String username = in.readLine();
                    System.out.println(username + " JOINROOM " + idRoom);
                    
                    String joinStatus = room.join(idRoom, username);
                    out.writeBytes(joinStatus + "\n");
                }
                else if(cmd.equals("JOINGAME")) {
                    int idRoom = Integer.parseInt(in.readLine()); // get ID room that the user wants to join
                    String username = in.readLine();
                    System.out.println(username + " JOINGAME " + idRoom);
                    
                    parent.JoinRoom(idRoom, username);
                    parent.Broadcast(idRoom, "", username + " telah bergabung ke dalam room game");
                }
                else if(cmd.equals("EXITGAME")) {
                    int idRoom = Integer.parseInt(in.readLine());
                    String username = in.readLine();
                    System.out.println(username + " EXIT GAME " + idRoom);
                    
                    if(!parent.CheckRoomIsEnded(idRoom)) {
                        // Delete users from table `user_in_room`
                        room.exitGame(idRoom, username);

                        parent.Broadcast(idRoom, "", username + " telah keluar dari room game");
                        parent.RemoveUser(idRoom, username);
                        break;
                    }
                }
                else if(cmd.equals("STARTGAME")) {
                    int idRoom = Integer.parseInt(in.readLine());
                    String username = in.readLine();
                    System.out.println(username + " START GAME " + idRoom);
                    
                    parent.Broadcast(idRoom, "", username + " menekan tombol start");
                    parent.StartGame(idRoom);
                }
                else if(cmd.equals("PUT")) {
                    int idRoom = Integer.parseInt(in.readLine());
                    String username = in.readLine();
                    String cards = in.readLine();
                    System.out.println(username + " ON ROOM: " + idRoom + " PUT " + cards);
                    
                    if(!cards.isEmpty()) {
                        String[] cardsArr = cards.split(",");
                        ArrayList<String> cardListString = new ArrayList<>(Arrays.asList(cardsArr));
                        ArrayList<Integer> cardListInt = new ArrayList<>();

                        // Parse ArrayList<String> to ArrayList<Integer>
                        for(String stringValue : cardListString) {
                            try {
                                cardListInt.add(Integer.parseInt(stringValue));
                            } catch(NumberFormatException nfe) {
                               System.out.println("Could not parse " + nfe);
                            } 
                        } 
                        parent.CheckBiggerCard(idRoom, username, cardListInt);
                    }
                    else {
                        // empty cards --> user skipped his/her turn
                        parent.CheckBiggerCard(idRoom, username, new ArrayList<Integer>());
                    }
                }
                else if(cmd.equals("WIN")) {
                    int idRoom = Integer.parseInt(in.readLine());
                    String username = in.readLine();
                    System.out.println(username + " WINS ROOM: " + idRoom);
                    
                    parent.EndGame(idRoom, username);
                }
            } catch (IOException ex) {
                Logger.getLogger(HandleSocket.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
