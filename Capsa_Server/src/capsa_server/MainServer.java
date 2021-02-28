/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package capsa_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author macintosh
 */
public class MainServer {
    Socket incoming;
    ServerSocket ss;
    HandleSocket hs;
    
    // to store subscriber list (room with all users in it)
    HashMap<Integer, ArrayList<HashMap<String, HandleSocket>>> map = new HashMap<>();
    
    // to store how many users in room who press `START` button
    HashMap<Integer, Integer> mapCountStart = new HashMap<>();
    
    // to store the last card for each round in a room
    HashMap<Integer, ArrayList<Integer>> lastCard = new HashMap<>();
    
    // to store the users who do not SKIP round in a room
    HashMap<Integer, ArrayList<HashMap<String, HandleSocket>>> notSkippedUsers = new HashMap<>();
    
    // to store wheter the game in a room is being bombed by 4 of a kind combo
    HashMap<Integer, Boolean> bombed = new HashMap<>();
    
    // to store wheter or not the game in a room is being flipped by 4 of a kind combo
    HashMap<Integer, Boolean> flipped = new HashMap<>();
    
    // to store finished rooms
    HashMap<Integer, Boolean> finishedRooms = new HashMap<>();

    public MainServer() {
        try {
            ss = new ServerSocket(6341);
            while(true)
            {
                incoming = ss.accept();
                hs = new HandleSocket(this, 0, incoming, "Thread");
                hs.start(); 
            }
        } catch (IOException ex) {
            Logger.getLogger(MainServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void Broadcast(int _idRoom, String _username, String _text) {        
        ArrayList<HashMap<String, HandleSocket>> arrlist = map.get(_idRoom);
        
        // Broadcast to all users in the room
        if(_username.equals("")) {
            for (HashMap<String, HandleSocket> hashMap : arrlist) {
                for (HandleSocket hs : hashMap.values()) {
                    hs.broadcast(_text);
                }
            }
        }
        // Broadcast to specific users in the room
        else {
            for (HashMap<String, HandleSocket> hashMap : arrlist) {
                if(hashMap.containsKey(_username)) {
                    hashMap.get(_username).broadcast(_text);
                    break;
                }
            }
        }
    }
    
    public void JoinRoom(Integer _idRoom, String _username) {
        // check to see if the room is already created on server
        if(map.containsKey(_idRoom)) {
            ArrayList<HashMap<String, HandleSocket>> arrlist = map.get(_idRoom);
            if(arrlist.size() != 4) {
               HashMap<String, HandleSocket> innerMap = new HashMap<>();
               innerMap.put(_username, hs);
               arrlist.add(innerMap);
               
               map.put(_idRoom, arrlist); // replace the Room's ID index with the new array list
               System.out.println(_username + " ditambahkan di room");
            }
        }
        else {
            HashMap<String, HandleSocket> innerMap = new HashMap<>();
            innerMap.put(_username, hs);
            
            ArrayList<HashMap<String, HandleSocket>> arrlist = new ArrayList<HashMap<String, HandleSocket>>();
            arrlist.add(innerMap);
            
            map.put(_idRoom, arrlist);
            System.out.println("room " + _idRoom + " ditambahkan");
        }
    }
    
    public void RemoveUser(int _idRoom, String _username) {
        if(map.containsKey(_idRoom)) {
            ArrayList<HashMap<String, HandleSocket>> arrlist = map.get(_idRoom);
            for (HashMap<String, HandleSocket> hashMap : arrlist) {
                if(hashMap.containsKey(_username)) {
                    arrlist.remove(hashMap); System.out.println(_username + " dihapus dari MainServer");
                    break;
                }
            }
        }
    }
    
    public void SkipUser(int _idRoom, String _username) {
        ArrayList<HashMap<String, HandleSocket>> arrlist = notSkippedUsers.get(_idRoom);
        for (HashMap<String, HandleSocket> hashMap : arrlist) {
            if(hashMap.containsKey(_username)) {
                arrlist.remove(hashMap);
                break;
            }
        }
        
        // If this user is the only one who didn't skip
        // then this user wins the round
        if(arrlist.size() == 1) {
            for (String username : arrlist.get(0).keySet()) {
                Broadcast(_idRoom, "", "--" + username + " memenangkan ronde ini --"); 
            }
            
            // Check for BOMB
            if(bombed.containsKey(_idRoom)) {
                boolean isBombed = bombed.get(_idRoom);
                if(isBombed) {
                    if(checkFlipped(_idRoom)) { 
                        flipped.put(_idRoom, false); 
                        Broadcast(_idRoom, "", "BOM!!! Urutan kartu dibalik! 3 wajik menjadi kartu terkecil, sedangkan 2 sekop menjadi kartu terbesar");
                    }
                    else { 
                        flipped.put(_idRoom, true); 
                        Broadcast(_idRoom, "", "BOM!!! Urutan kartu dibalik! 3 wajik menjadi kartu terbesar, sedangkan 2 sekop menjadi kartu terkecil");
                    }
                }

                // clear bomb status from this room
                bombed.put(_idRoom, false);
            }
            
            // clear lastCards for this room
            lastCard.remove(_idRoom);

            // clear notSkippedUsers for this room
            notSkippedUsers.remove(_idRoom);
        }
    }
    
    public void StartGame(int _idRoom) {
        Integer newCount = 1;
        if(mapCountStart.containsKey(_idRoom)) {
            newCount = mapCountStart.get(_idRoom) + 1;
            
            // Replace with new count
            mapCountStart.put(_idRoom, newCount);
        }
        else {
            mapCountStart.put(_idRoom, 1);
        }
        
        // Start the game if already 4 users start
        if(newCount == 4) {
            Broadcast(_idRoom, "", "-- GAME TELAH DIMULAI --");
            ShareCard(_idRoom);
            
            // update room status to `bermain`
            ArrayList<HashMap<String, HandleSocket>> arrlist = map.get(_idRoom);
        
            for (HashMap<String, HandleSocket> hashMap : arrlist) {
                for (HandleSocket hs : hashMap.values()) {
                    hs.updateRoomStatus(_idRoom, "bermain");
                }
            }
        }
    }
    
    public void ShareCard(int _idRoom) {
        // List of index card from 1 - 52
        ArrayList<Integer> ixList = new ArrayList<Integer>(); 
        ArrayList<HashMap<String, HandleSocket>> arrlist = map.get(_idRoom);
        
        // Populates the ixList with number from 1 to 52
        for (int i = 1; i <= 52; i++) { ixList.add(i); }
        
        // Shuffle the index card
        Collections.shuffle(ixList);
        
        for (int i = 0; i < arrlist.size(); i++) {
            // get 13 cards from list of index card
            List<Integer> list = ixList.subList(i*13, i*13+13);
            
            // check if this user gets 3 wajik card
            boolean get3wajik = false;
            if(list.contains(1)) { get3wajik = true ; }
            
            // convert to List<String> so that String.join is usable
            List<String> listString = new ArrayList<>(list.size());
            list.forEach((myInt) -> {
                listString.add(String.valueOf(myInt));
            });
            
            // get the username for this HashMap
            for (String username : arrlist.get(i).keySet()) {
                String joinedCards = String.join(",", listString);
                System.out.println("send " + joinedCards + " to " + username);
                
                // share the cards to each users in the room
                Broadcast(_idRoom, username, "CARDS");
                Broadcast(_idRoom, username, joinedCards);
                
                // the user who gets 3 wajib will get the first turn
                if(get3wajik) {
                    Broadcast(_idRoom, "", "-- Giliran " + username + "--"); 
                    Broadcast(_idRoom, username, "TURN"); 
                }
            }
        }
    }
    
    public void NextPlayerTurn(int _idRoom, String _username) {
        if(!notSkippedUsers.containsKey(_idRoom)) {
            // Populate notSkippedUsers array list
            ArrayList<HashMap<String, HandleSocket>> listFromMap = new  ArrayList<>(map.get(_idRoom));
            notSkippedUsers.put(_idRoom, listFromMap);
        }
        
        ArrayList<HashMap<String, HandleSocket>> arrlist = notSkippedUsers.get(_idRoom);
        
        int ix = 0; // Get index of this user
        for (HashMap<String, HandleSocket> hashMap : arrlist) {
            if(hashMap.containsKey(_username)) { break; }
            ix++;
        }

        // Get next user which is on current index + 1
        int nextIx = (ix+1) % arrlist.size();

        for (String uname : arrlist.get(nextIx).keySet()) {
            Broadcast(_idRoom, "", "-- Giliran " + uname + "--"); 
            Broadcast(_idRoom, uname, "TURN"); 
        }
    }
    
    public String CardsIxToName(ArrayList<Integer> _cards) {
        String name = "";
        for (Integer _ix : _cards) {
            if(name.length() != 0) { name += ", "; }
            
            // RULES:
            // 1. Getting the number:
            // Divide (index-1) by 4, then plus 3 to the division result
            Integer n = ((_ix-1) / 4) + 3; // by default, java does rounding to zero 

            // 2. Check to see if n is special cards, i.e. Jack, Queen, King, Ace, 2
            if(n == 11) { name += "J "; }
            else if(n == 12) { name += "Q"; }
            else if(n == 13) { name += "K";}
            else if(n == 14) { name += "A";}
            else if(n == 15) { name += "2";}
            else { name += String.valueOf(n); } // normal cards

            // 3. Getting the color:
            // n mod 4
            Integer mod = _ix % 4;

            // 4. Check to see the color
            if(mod == 1) { name += " wajik"; }
            else if(mod == 2) { name += " keriting"; }
            else if(mod == 3) { name += " hati"; }
            else if(mod == 0) { name += " sekop"; }
        }
        
        return name;
    }
    
    public static int FiveCardsRankNumber(ArrayList<Integer> _cards) {
        // output is 4 digits
        // first digit is 5 card rank status (1 - 5), 5 is the highest
        // second digit is used for indexing FLUSH cards
        // last 2 digits (3 & 4) is the largest card index 
        
        Collections.sort(_cards, Collections.reverseOrder());
        
        Integer largestIx = _cards.get(0);
        String rankNumber = "";
        
        // Rank 1 (lowest) = STRAIGHT
        boolean isStraight = true;
        for (int i = 0; i < _cards.size() - 1; i++) {
            if (((_cards.get(i)-1) / 4) + 3 != ((_cards.get(i + 1)-1)/ 4) + 3 + 1) {
              // Not sequential
              isStraight = false;
              break;
            }
        }
        if(isStraight) {
            // 1 digit
            if(largestIx < 10) { rankNumber = "100" + String.valueOf(largestIx); }
            // 2 digit
            else { rankNumber = "10" + String.valueOf(largestIx);  }
        }
        
        // Rank 2 = FLUSH
        boolean isFlush = true;
        for (int i = 0; i < _cards.size() - 1; i++) {
            if ((_cards.get(i) % 4) != (_cards.get(i + 1) % 4)) {
              // Is not the same color
              isFlush = false;
              break;
            }
        }
        if(isFlush) {
            // Indexing card color (wajik, hati, keriting, and sekop)
            // NOTES :
            // Wajik is indexed as 1
            // Keriting is indexed as 2
            // Hati is indexed as 3
            // Sekop is indexed as 4
            
            Integer colorIx = largestIx % 4;
            if(colorIx == 0) { colorIx = 4; }
            
            // 1 digit
            if(largestIx < 10) { rankNumber = "2" + colorIx + "0" + String.valueOf(largestIx); }
            // 2 digit
            else { rankNumber = "2" + colorIx + String.valueOf(largestIx); }
        }
        
        // Store occurance of cards in map
        boolean isFullHouse = false;
        boolean is4OfAKind = false;
        
        Map<Integer, Integer> hm = new HashMap<Integer, Integer>(); 
        for (Integer i : _cards) { 
            int cardNumber = ((i-1)/4) + 3;
            Integer j = hm.get(cardNumber); 
            hm.put(cardNumber, (j == null) ? 1 : j + 1); 
        } 
        
        // Rank 3 = FULL HOUSE
        // get the occurrence of elements in the arraylist 
        if(hm.entrySet().size() == 2) {
            for (Map.Entry<Integer, Integer> val : hm.entrySet()) { 
                if(val.getValue() == 3) {
                    largestIx = val.getKey();
                    isFullHouse = true;
                }
            }
        }
        
        if(isFullHouse) {
            // 1 digit
            if(largestIx < 10) { rankNumber = "300" + String.valueOf(largestIx); }
            // 2 digit
            else { rankNumber = "30" + String.valueOf(largestIx); }
        }
        
        // Rank 4 = 4 OF A KIND
        // get the occurrence of elements in the arraylist 
        if(hm.entrySet().size() == 2) {
            for (Map.Entry<Integer, Integer> val : hm.entrySet()) {
                if(val.getValue() == 4) {
                    largestIx = val.getKey();
                    is4OfAKind = true;
                }
            }
        }
        if(is4OfAKind) {
            // 1 digit
            if(largestIx < 10) { rankNumber = "400" + String.valueOf(largestIx); }
            // 2 digit
            else { rankNumber = "40" + String.valueOf(largestIx); }
        }
        
        // Rank 5 = STRAIGHT FLUSH
        if(isStraight && isFlush) {
            // 1 digit
            if(largestIx < 10) { rankNumber = "500" + String.valueOf(largestIx); }
            // 2 digit
            else { rankNumber = "50" + String.valueOf(largestIx); }
        }
        
        return Integer.parseInt(rankNumber);
    }
    
    public boolean checkFlipped(int _idRoom) {
        // will return true if the game for this room is being flipped
        // which is `3 wajik` is the largest, whereas `2 sekop` is the smallest 
        
        boolean isFlipped = false;
        
        if(flipped.containsKey(_idRoom)) {
            isFlipped = flipped.get(_idRoom);
        }
        else {
            flipped.put(_idRoom, false);
        }
        
        return isFlipped;
    }
    
    public void CheckBiggerCard(int _idRoom, String _username, ArrayList<Integer> _cards) {
        if(lastCard.containsKey(_idRoom)) {
            ArrayList<Integer> arrlist = lastCard.get(_idRoom);
            
            /*** Check if the user SKIPPED his/her turn ***/
            if(_cards.size() == 0) {
                // next player turn
                Broadcast(_idRoom, "", _username + " melakukan SKIP");
                NextPlayerTurn(_idRoom, _username);
                SkipUser(_idRoom, _username);
            }
            else {
                /*** Check if the user put the same number of cards ***/
                // prior to the last cards
                if(_cards.size() != arrlist.size()) {
                    Broadcast(_idRoom, _username, "ERROR");
                    Broadcast(_idRoom, _username, "Jumlah Kartu Anda Tidak Sama Dengan Kartu User Sebelumnya");
                    return;
                }
                else {
                    Collections.sort(arrlist, Collections.reverseOrder());
                    Collections.sort(_cards, Collections.reverseOrder());
                    
                    // Get the size of array cards to see what type of game is played
                    // i.e. SATUAN, PAIR, THREES, or 5 Cards 
                    
                    if(checkFlipped(_idRoom)) { //-- begin of flipped game --//
                        if(_cards.size() != 5) { //-- begin of NON five cards --//
                            // SATUAN, PAIR & TRIS
                            if(_cards.get(0) < arrlist.get(0)) {
                                Broadcast(_idRoom, _username, "ACCEPTED"); // Card is accepted
                                lastCard.put(_idRoom, _cards);

                                String cardStatus = ""; // single, pair, or tris
                                if(_cards.size() == 1) { cardStatus = "(SINGLE)"; }
                                else if(_cards.size() == 2) { cardStatus = "(PAIR)"; }
                                else if(_cards.size() == 3) { cardStatus = "(TRIS)"; }

                                // next player turn
                                Broadcast(_idRoom, "", _username + " menaruh " + CardsIxToName(_cards) + " " + cardStatus);
                                NextPlayerTurn(_idRoom, _username);
                            }
                            else {
                                // The user put smaller card
                                Broadcast(_idRoom, _username, "ERROR");
                                Broadcast(_idRoom, _username, "Kartu Anda Lebih Kecil Dari Kartu User Sebelumnya");
                                return;
                            }
                        } //-- end of NON five cards --//
                        else { //-- begin of five cards --//
                            Integer fiveCardRank = FiveCardsRankNumber(_cards);
                            if(fiveCardRank < FiveCardsRankNumber(arrlist)) {
                                Broadcast(_idRoom, _username, "ACCEPTED"); // Card is accepted
                                lastCard.put(_idRoom, _cards);

                                String cardStatus = ""; // straight, flush, full house, 4 of a kind, or straight flush
                                if(fiveCardRank > 5000) { cardStatus = "(STRAIGHT FLUSH)";  }
                                else if(fiveCardRank > 4000) { 
                                    cardStatus = "(FOUR OF A KIND)"; 
                                    
                                    // BOMB!
                                    bombed.put(_idRoom, true);
                                }
                                else if(fiveCardRank > 3000) { 
                                    cardStatus = "(FULL HOUSE)"; 
                                    
                                    // Unbomb
                                    bombed.put(_idRoom, false);
                                }
                                else if(fiveCardRank > 2000) { 
                                    cardStatus = "(FLUSH)"; 
                                    
                                    // Unbomb
                                    bombed.put(_idRoom, false);
                                }
                                else if(fiveCardRank > 1000) { 
                                    cardStatus = "(STRAIGHT)"; 
                                    
                                    // Unbomb
                                    bombed.put(_idRoom, false);
                                }

                                // next player turn
                                Broadcast(_idRoom, "", _username + " menaruh " + CardsIxToName(_cards) + " " + cardStatus);
                                NextPlayerTurn(_idRoom, _username);
                            }
                            else {
                                // The user put smaller card
                                Broadcast(_idRoom, _username, "ERROR");
                                Broadcast(_idRoom, _username, "Kartu Anda Lebih Kecil Dari Kartu User Sebelumnya");
                                return;
                            }
                        } //-- end of five cards --//
                    } //-- end of flipped game --//
                    else { //-- begin of NON flipped game --//
                        if(_cards.size() != 5) { //-- begin of NON five cards --//
                            // SATUAN, PAIR & TRIS
                            if(_cards.get(0) > arrlist.get(0)) {
                                Broadcast(_idRoom, _username, "ACCEPTED"); // Card is accepted
                                lastCard.put(_idRoom, _cards);

                                String cardStatus = ""; // single, pair, or tris
                                if(_cards.size() == 1) { cardStatus = "(SINGLE)"; }
                                else if(_cards.size() == 2) { cardStatus = "(PAIR)"; }
                                else if(_cards.size() == 3) { cardStatus = "(TRIS)"; }

                                // next player turn
                                Broadcast(_idRoom, "", _username + " menaruh " + CardsIxToName(_cards) + " " + cardStatus);
                                NextPlayerTurn(_idRoom, _username);
                            }
                            else {
                                // The user put smaller card
                                Broadcast(_idRoom, _username, "ERROR");
                                Broadcast(_idRoom, _username, "Kartu Anda Lebih Kecil Dari Kartu User Sebelumnya");
                                return;
                            }
                        } //-- end of NON five cards --//
                        else { //-- begin of five cards --//
                            Integer fiveCardRank = FiveCardsRankNumber(_cards);
                            if(fiveCardRank > FiveCardsRankNumber(arrlist)) {
                                Broadcast(_idRoom, _username, "ACCEPTED"); // Card is accepted
                                lastCard.put(_idRoom, _cards);

                                String cardStatus = ""; // straight, flush, full house, 4 of a kind, or straight flush
                                if(fiveCardRank > 5000) { 
                                    cardStatus = "(STRAIGHT FLUSH)";  
                                    
                                    // Unbomb
                                    bombed.put(_idRoom, false);
                                }
                                else if(fiveCardRank > 4000) { 
                                    cardStatus = "(FOUR OF A KIND)"; 
                                    
                                    // BOMB!
                                    bombed.put(_idRoom, true);
                                }
                                else if(fiveCardRank > 3000) { cardStatus = "(FULL HOUSE)"; }
                                else if(fiveCardRank > 2000) { cardStatus = "(FLUSH)"; }
                                else if(fiveCardRank > 1000) { cardStatus = "(STRAIGHT)"; }

                                // next player turn
                                Broadcast(_idRoom, "", _username + " menaruh " + CardsIxToName(_cards) + " " + cardStatus);
                                NextPlayerTurn(_idRoom, _username);
                            }
                            else {
                                // The user put smaller card
                                Broadcast(_idRoom, _username, "ERROR");
                                Broadcast(_idRoom, _username, "Kartu Anda Lebih Kecil Dari Kartu User Sebelumnya");
                                return;
                            }
                        } //-- end of five cards --//
                        
                    } //-- end of NON flipped game --//
                }
            }
        }
        else {
            // Player must send at least 1 card when he/she won previous round
            if(_cards.size() == 0) {
                Broadcast(_idRoom, _username, "ERROR");
                Broadcast(_idRoom, _username, "Anda Harus Menaruh Setidaknya Satu Kartu Karena Anda Menang Ronde Sebelumnya");
            }
            else {
                Broadcast(_idRoom, _username, "ACCEPTED"); // Card is accepted
                lastCard.put(_idRoom, _cards);
                
                String cardStatus = ""; // single, pair, or tris
                if(_cards.size() == 1) { cardStatus = "(SINGLE)"; }
                else if(_cards.size() == 2) { cardStatus = "(PAIR)"; }
                else if(_cards.size() == 3) { cardStatus = "(TRIS)"; }
                else if(_cards.size() == 5) {
                    Integer fiveCardRank = FiveCardsRankNumber(_cards);
                    if(fiveCardRank > 5000) { cardStatus = "(STRAIGHT FLUSH)";  }
                    else if(fiveCardRank > 4000) { 
                        cardStatus = "(FOUR OF A KIND)"; 
                        
                        // BOMB!
                        bombed.put(_idRoom, true);
                    }
                    else if(fiveCardRank > 3000) { cardStatus = "(FULL HOUSE)"; }
                    else if(fiveCardRank > 2000) { cardStatus = "(FLUSH)"; }
                    else if(fiveCardRank > 1000) { cardStatus = "(STRAIGHT)"; }
                }

                // next player turn
                Broadcast(_idRoom, "", _username + " menaruh " + CardsIxToName(_cards) + " " + cardStatus);
                NextPlayerTurn(_idRoom, _username);
            }   
        }
    }
    
    public void EndGame(int _idRoom, String _username) {
        Broadcast(_idRoom, "", "=== GAME INI DIMENANGKAN OLEH " + _username + " === ");
        
        // Disabled all client button
        Broadcast(_idRoom, "", "ENDGAME");
        
        // update room status to `selesai`
        ArrayList<HashMap<String, HandleSocket>> arrlist = map.get(_idRoom);

        for (HashMap<String, HandleSocket> hashMap : arrlist) {
            for (HandleSocket hs : hashMap.values()) {
                hs.updateRoomStatus(_idRoom, "selesai");
            }
        }
        
        // CLEAR ALL
        // clear subscribers list from this room
        map.remove(_idRoom);
        
         // clear users who pressed `START` button from this room
        mapCountStart.remove(_idRoom);
        
        // clear lastCards for this room
        lastCard.remove(_idRoom);

        // clear notSkippedUsers for this room
        notSkippedUsers.remove(_idRoom);
        
        // set finished rooms
        finishedRooms.put(_idRoom, true);
    }
    
    public boolean CheckRoomIsEnded(int _idRoom) {
        if(finishedRooms.containsKey(_idRoom)) {
            return finishedRooms.get(_idRoom);
        }
        return false;
    }
}
