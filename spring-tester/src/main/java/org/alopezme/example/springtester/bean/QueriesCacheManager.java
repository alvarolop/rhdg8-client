package org.alopezme.example.springtester.bean;

import org.springframework.stereotype.Component;

@Component
public class QueriesCacheManager {

    private int port;
    private String host;

    public QueriesCacheManager(){
        this.port = 11222;
        this.host = "localhost";
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
