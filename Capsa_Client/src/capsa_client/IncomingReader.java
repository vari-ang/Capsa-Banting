/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package capsa_client;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author macintosh
 */
public class IncomingReader extends Thread {
    BufferedReader reader;
    FormGame parent;
    String data; // from server
    
    public IncomingReader(BufferedReader _reader, FormGame _parent) {
        reader = _reader;
        parent = _parent;
    }

    @Override
    public void run() {
        super.run(); //To change body of generated methods, choose Tools | Templates.
        while(true) {
            try {
                data = reader.readLine();
                if(data.equals("CARDS")) {
                    String cards = reader.readLine();
                    parent.refreshCard(cards);
                }
                else if(data.equals("TURN")) {
                    parent.appendTextArea("GILIRAN ANDA");
                    parent.enablePutButton();
                }
                else if(data.equals("ACCEPTED")) {
                    parent.removeSelectedCards();
                    parent.checkWinningStatus();
                }
                else if(data.equals("ERROR")) {
                    String text = reader.readLine();
                    parent.warningMsgDialog(text);
                    parent.enablePutButton();
                }
                else if(data.equals("ENDGAME")) {
                    parent.gameEnd();
                }
                else { // Just a normal text
                    parent.appendTextArea(data);
                }
            } catch (IOException ex) {
                Logger.getLogger(IncomingReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        } 
    }
}
