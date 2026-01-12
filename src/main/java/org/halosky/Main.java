package org.halosky;

import org.halosky.config.Config;
import org.halosky.http.HttpServer;

import java.util.Objects;

public class Main {
    public static void main(String[] args) {
        HttpServer server = null;
        try{
            Config config = new Config(Config.DEFAULT_CONFIG_FILE_NAME);
            server = new HttpServer(config.getHttpConfig(),config.getNetworkConfig());
            server.start();
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            if(Objects.nonNull(server)){
                server.stop();
            }
        }
        System.out.println("Hello world!");
    }
}