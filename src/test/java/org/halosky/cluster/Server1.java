package org.halosky.cluster;

import org.halosky.SEServer;
import org.halosky.config.Config;

import java.util.Objects;

/**
 * packageName org.halosky.cluster
 *
 * @author huan.yang
 * @className Server1
 * @date 2026/1/14
 */
public class Server1 {

    public static void main(String[] args) {
        SEServer server = null;
        try{
            Config config = new Config(Config.DEFAULT_CONFIG_FILE_NAME);
            server = new SEServer(config);
            server.start();
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            if(Objects.nonNull(server)){
                server.close();
            }
        }
    }
}
