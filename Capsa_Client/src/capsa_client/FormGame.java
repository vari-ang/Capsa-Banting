/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package capsa_client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.DefaultCaret;
import static javax.swing.text.DefaultCaret.ALWAYS_UPDATE;

/**
 *
 * @author macintosh
 */
public class FormGame extends javax.swing.JFrame {

    /**
     * Creates new form FormGame
     */
    
    Socket s;
    BufferedReader in;
    DataOutputStream out;
    String username;
    int idRoom;
    String roomName;
    IncomingReader reader;
    
    String[] cardListIxString; // 13 cards `INDEX` from server
    String[] cardListNameString; // 13 sorted (descending) NAMED cards
    Integer[] cardListIxInt; // 13 sorted (descending) `INDEX` cards
    
    public FormGame(String _username, int _idRoom, String _roomName) {
        try {
            initComponents();
            
            ServerHost host = new ServerHost();
            s = new Socket(host.getDomain(), host.getPort());
            
            out = new DataOutputStream(s.getOutputStream());
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            
            jLabelRoom.setText("#" + _idRoom + " (" + _roomName + ")");
            jLabelUsername.setText(_username);
            jTextAreaLog.setEditable(false);
            jButtonPut.setEnabled(false);
            jTableCards.setEnabled(false);
            
            // always scroll jTextAreaLog to bottom
            DefaultCaret caret = (DefaultCaret) jTextAreaLog.getCaret();
            caret.setUpdatePolicy(ALWAYS_UPDATE);
            
            username = _username;
            idRoom = _idRoom;
            roomName = _roomName;
            
            out.writeBytes("JOINGAME\n"); // be MainServer subscriber
            out.writeBytes(idRoom + "\n");
            out.writeBytes(username + "\n");
            
            reader = new IncomingReader(in, this);
            reader.start();
        } catch (IOException ex) {
            Logger.getLogger(FormGame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void appendTextArea(String _text) {
        jTextAreaLog.append(_text + "\n");
    }
    
    public void enablePutButton() {
        jButtonPut.setEnabled(true);
    }
    
    public void gameEnd() {
        jButtonExit.setEnabled(true);
        jButtonPut.setEnabled(false);
    }
    
    public void warningMsgDialog(String _text) {
        JOptionPane.showMessageDialog(this, 
            _text, 
            "Error", 
            JOptionPane.WARNING_MESSAGE);
    }
    
    public String cardIxToName(Integer _ix) {
        String name = "";
        
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
        
        return name;
    }
    
    public void populateComboBoxes() {
        // 1. Clear all items on the combobox
        jComboBox1.removeAllItems(); jComboBox1.addItem("---");
        jComboBox2.removeAllItems(); jComboBox2.addItem("---");
        jComboBox3.removeAllItems(); jComboBox3.addItem("---");
        jComboBox4.removeAllItems(); jComboBox4.addItem("---");
        jComboBox5.removeAllItems(); jComboBox5.addItem("---");
        
        // 2. Set the value
        for (String _card : cardListNameString) {
            jComboBox1.addItem(_card);
            jComboBox2.addItem(_card);
            jComboBox3.addItem(_card);
            jComboBox4.addItem(_card);
            jComboBox5.addItem(_card);
        }
        
        // 3. Set `0` as default selected index 
        jComboBox1.setSelectedIndex(0);
        jComboBox2.setSelectedIndex(0);
        jComboBox3.setSelectedIndex(0);
        jComboBox4.setSelectedIndex(0);
        jComboBox5.setSelectedIndex(0);
    }
    
    public void refreshCard(String _cards) {
        DefaultTableModel model = (DefaultTableModel) jTableCards.getModel();
        
        // Reset table
        model.setRowCount(0);
        
        if(!_cards.isEmpty()) {
            cardListIxString = _cards.split(","); 
        }
        else {
            cardListIxString = new String[0];
        }
        
        cardListNameString = new String[cardListIxString.length]; 
        cardListIxInt = new Integer[cardListIxString.length]; 
        
        for(int i = 0; i < cardListIxString.length; i++) {
            cardListIxInt[i] = Integer.parseInt(cardListIxString[i]);
        }
        
        // sort cardListIxInt in descending order
        Arrays.sort(cardListIxInt, Collections.reverseOrder());
        
        // re-populate cardListIxString & populate cardListNameString
        for(int i = 0; i < cardListIxInt.length; i++) {
            cardListIxString[i] = String.valueOf(cardListIxInt[i]);
            cardListNameString[i] = cardIxToName(cardListIxInt[i]);
        }
         
        // Shows cards to jTable
        for (String _card : cardListNameString) {
            model.addRow(new Object[]{ _card });
        }
        
        // Populate combo boxes
        populateComboBoxes();
    }
    
    public boolean checkCardStatus(ArrayList<Integer> _cards) {
        // will return true if card status (combination) is okay
        
        Collections.sort(_cards, Collections.reverseOrder());
        
        if(_cards.size() == 1) {
            return true;
        } 
        else if(_cards.size() == 2) {
            // PAIR
            Integer cardNumber1 = ((_cards.get(0)-1) / 4) + 3;
            Integer cardNumber2 = ((_cards.get(1)-1) / 4) + 3;

            if(cardNumber1 != cardNumber2) {
                return false;
            }
        }
        else if(_cards.size() == 3) {
            // TRIS
            Integer cardNumber1 = ((_cards.get(0)-1) / 4) + 3;
            Integer cardNumber2 = ((_cards.get(1)-1) / 4) + 3;
            Integer cardNumber3 = ((_cards.get(2)-1) / 4) + 3;
            
            if (cardNumber1 != cardNumber2 && cardNumber2 != cardNumber3) {
                return false;
            }
        }
        else if(_cards.size() == 4) {
            return false;
        }
        else if(_cards.size() == 5) {
            Integer cardNumber1 = ((_cards.get(0)-1) / 4) + 3;
            Integer cardNumber2 = ((_cards.get(1)-1) / 4) + 3;
            Integer cardNumber3 = ((_cards.get(2)-1) / 4) + 3;
            Integer cardNumber4 = ((_cards.get(3)-1) / 4) + 3;
            Integer cardNumber5 = ((_cards.get(4)-1) / 4) + 3;
            
            boolean isStraight = true;
            boolean isFlush = true;
            
            // STRAIGHT & ROYAL FLUSH
            for (int i = 0; i < _cards.size() - 1; i++) {
                if (((_cards.get(i)-1) / 4) + 3 != ((_cards.get(i + 1)-1)/ 4) + 3 + 1) {
                  // Not sequential
                  isStraight = false;
                  break;
                }
            }
            if(isStraight) { return true; }
            
            // FLUSH
            for (int i = 0; i < _cards.size() - 1; i++) {
                if ((_cards.get(i) % 4) != (_cards.get(i + 1) % 4)) {
                  // Is not the same color
                  isFlush = false;
                  break;
                }
            }
            if(isFlush) { return true; }
            
            // FULL HOUSE & FOUR OF A KIND
            // only 2 unique cards left after duplication-removal
            ArrayList<Integer> arrlist = new ArrayList<>();
            arrlist.add(cardNumber1);
            arrlist.add(cardNumber2);
            arrlist.add(cardNumber3);
            arrlist.add(cardNumber4);
            arrlist.add(cardNumber5);
            
            Set<Integer> set = new HashSet<Integer>(arrlist);
            if(set.size() == 2){ return true; }
            
            return false;
        }
        
        return true;
    }
    
    public void removeSelectedCards() {
        // stores all cards that the user have
        ArrayList<Integer> cards = new ArrayList<>();
        // Convert integer array to array list
        for (int i : cardListIxInt) { cards.add(i); }
        
        // For selected cards on the comboBox
        ArrayList<Integer> selectedCards = new ArrayList<>();
        
        // Get selected index on comboBoxes
        int ix1 = jComboBox1.getSelectedIndex();
        int ix2 = jComboBox2.getSelectedIndex();
        int ix3 = jComboBox3.getSelectedIndex();
        int ix4 = jComboBox4.getSelectedIndex();
        int ix5 = jComboBox5.getSelectedIndex();
        
        // Populate cards array
        // Insert `INDEX` cards based on selected values from all comboboxes
        if(ix1 != 0) { selectedCards.add(cardListIxInt[ix1-1]); } 
        if(ix2 != 0) { selectedCards.add(cardListIxInt[ix2-1]); } 
        if(ix3 != 0) { selectedCards.add(cardListIxInt[ix3-1]); } 
        if(ix4 != 0) { selectedCards.add(cardListIxInt[ix4-1]); } 
        if(ix5 != 0) { selectedCards.add(cardListIxInt[ix5-1]); } 
        
        // Remove seelctedCards on cards
        for (Integer i : selectedCards) {
            cards.remove(i);
        }
        
        // convert cards arraylist to List<String> so that String.join is usable
        List<String> listString = new ArrayList<>(cards.size());
        cards.forEach((ix) -> {
            listString.add(String.valueOf(ix));
        });
        
        if(cards.size() != 0) { 
            String joinedCards = String.join(",", listString); 
            refreshCard(joinedCards); 
        }
        else { 
            refreshCard(""); 
        }
    }
    
    public void checkWinningStatus() {
        // check if this user wins the game 
        // the first user to have no more cards left
        if(cardListIxInt.length == 0) {
            try {
                // this user wins
                out.writeBytes("WIN\n");
                out.writeBytes(idRoom + "\n");
                out.writeBytes(username + "\n");
                
                JOptionPane.showMessageDialog(this, "SELAMAT! Anda memenangkan game ini :)");
            } catch (IOException ex) {
                Logger.getLogger(FormGame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel6 = new javax.swing.JLabel();
        jButtonStart = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabelRoom = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextAreaLog = new javax.swing.JTextArea();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTableCards = new javax.swing.JTable();
        jComboBox1 = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        jComboBox5 = new javax.swing.JComboBox<>();
        jComboBox4 = new javax.swing.JComboBox<>();
        jComboBox2 = new javax.swing.JComboBox<>();
        jComboBox3 = new javax.swing.JComboBox<>();
        jButtonPut = new javax.swing.JButton();
        jLabelUsername = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jButtonExit = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(jTable1);

        jLabel6.setText("<html><b style=\"color:red\">NOTE:</b> <br>Username hanya boleh<br> terdiri dari huruf dan angka (tanpa spasi)</html>");
        jLabel6.setToolTipText("");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jButtonStart.setText("START");
        jButtonStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStartActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Lucida Grande", 1, 18)); // NOI18N
        jLabel2.setText("Game Capsa Banting");

        jLabel1.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
        jLabel1.setText("Room :");

        jLabelRoom.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
        jLabelRoom.setText("#id (name)");

        jTextAreaLog.setColumns(20);
        jTextAreaLog.setRows(5);
        jScrollPane1.setViewportView(jTextAreaLog);

        jTableCards.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                "Kartu Anda"
            }
        ));
        jScrollPane4.setViewportView(jTableCards);

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "---" }));

        jLabel4.setText("Pilih Kartu");

        jComboBox5.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "---" }));

        jComboBox4.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "---" }));

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "---" }));

        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "---" }));

        jButtonPut.setText("PUT");
        jButtonPut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPutActionPerformed(evt);
            }
        });

        jLabelUsername.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
        jLabelUsername.setText("username");

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/capsa_client/capsa logo.png"))); // NOI18N

        jButtonExit.setText("EXIT");
        jButtonExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExitActionPerformed(evt);
            }
        });

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("<html><p style=\"text-align:left\"><b style=\"color:red\">NOTE:</b> <br>Setelah menekan tombol `START`, Anda tidak dapat keluar hingga game berakhir</p></html>");
        jLabel7.setToolTipText("");

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("<html><p style=\"text-align:center\"><b style=\"color:blue\">INFO:</b> <br>Jika ingin SKIP giliran, kosongkan semua pilihan</p></html>");
        jLabel8.setToolTipText("");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(45, 45, 45)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jComboBox5, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jButtonPut, javax.swing.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE)
                                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                .addGap(34, 34, 34))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(78, 78, 78)
                                .addComponent(jLabel4)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jButtonStart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabelUsername, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 268, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(84, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabelRoom)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel5))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 502, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jLabelRoom))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelUsername)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButtonExit)
                            .addComponent(jButtonStart))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)))
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 235, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(7, 7, 7)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jButtonPut, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(8, 8, 8))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jComboBox5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(32, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStartActionPerformed
        try {
            // TODO add your handling code here:
            jButtonStart.setEnabled(false);
            jButtonExit.setEnabled(false);
            
            out.writeBytes("STARTGAME\n");
            out.writeBytes(idRoom + "\n");
            out.writeBytes(username + "\n");
        } catch (IOException ex) {
            Logger.getLogger(FormGame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButtonStartActionPerformed

    private void jButtonPutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPutActionPerformed
        // TODO add your handling code here:
        
        // Set array to store select values
        ArrayList<Integer> cards = new ArrayList<>();
        
        // Get selected index on comboBoxes
        int ix1 = jComboBox1.getSelectedIndex();
        int ix2 = jComboBox2.getSelectedIndex();
        int ix3 = jComboBox3.getSelectedIndex();
        int ix4 = jComboBox4.getSelectedIndex();
        int ix5 = jComboBox5.getSelectedIndex();
        
        // Populate cards array
        // Insert `INDEX` cards based on selected values from all comboboxes
        if(ix1 != 0) { cards.add(cardListIxInt[ix1-1]); } 
        if(ix2 != 0) { cards.add(cardListIxInt[ix2-1]); } 
        if(ix3 != 0) { cards.add(cardListIxInt[ix3-1]); } 
        if(ix4 != 0) { cards.add(cardListIxInt[ix4-1]); } 
        if(ix5 != 0) { cards.add(cardListIxInt[ix5-1]); } 
        
        Set<Integer> set = new HashSet<Integer>(cards);
        if(set.size() < cards.size()){
            /* There are duplicates */
            warningMsgDialog("Anda memasukkan kartu yang sama dua kali atau lebih");
        }
        else {
            if(!checkCardStatus(cards)) {
                warningMsgDialog("Kombinasi Kartu Anda Salah");
            }
            else {
                /*** Check if the user has 3 wajik ***/
                boolean has3Wajik = false;
                for (Integer card : cardListIxInt) {
                    if(card == 1) { has3Wajik = true; break; }
                }

                // User must put 3 wajib in his/her first turn
                if(has3Wajik && !cards.contains(1)) {
                    warningMsgDialog("Anda harus mengeluarkan kartu 3 wajik terlebih dahulu");
                }
                else {
                    // Send cards to server
                    try {
                        out.writeBytes("PUT\n");
                        out.writeBytes(idRoom + "\n");
                        out.writeBytes(username + "\n");

                        // convert cards arraylist to List<String> so that String.join is usable
                        List<String> listString = new ArrayList<>(cards.size());
                        cards.forEach((ix) -> {
                            listString.add(String.valueOf(ix));
                        });

                        String joinedCards = String.join(",", listString);

                        out.writeBytes(joinedCards + "\n");
                        jButtonPut.setEnabled(false);
                    } catch (IOException ex) {
                        Logger.getLogger(FormGame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }//GEN-LAST:event_jButtonPutActionPerformed

    private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
        try {
            // TODO add your handling code here:
            out.writeBytes("EXITGAME\n");
            out.writeBytes(idRoom + "\n");
            out.writeBytes(username + "\n");
            
            JOptionPane.showMessageDialog(this, "Berhasil keluar dari Room: #" + idRoom + " (" + roomName + ")");
            this.setVisible(false);
        } catch (IOException ex) {
            Logger.getLogger(FormGame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButtonExitActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FormGame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FormGame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FormGame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FormGame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FormGame("", 0, "").setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonPut;
    private javax.swing.JButton jButtonStart;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JComboBox<String> jComboBox3;
    private javax.swing.JComboBox<String> jComboBox4;
    private javax.swing.JComboBox<String> jComboBox5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabelRoom;
    private javax.swing.JLabel jLabelUsername;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTableCards;
    private javax.swing.JTextArea jTextAreaLog;
    // End of variables declaration//GEN-END:variables
}
