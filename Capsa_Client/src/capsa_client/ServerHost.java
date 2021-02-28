/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package capsa_client;

/**
 *
 * @author macintosh
 */
public class ServerHost {
    private String domain;
    private int port;
    
    public ServerHost() {
        setDomain("localhost");
        setPort(6341);
    }

    public ServerHost(String _domain, int _port) {
        setDomain(_domain);
        setPort(_port);
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
