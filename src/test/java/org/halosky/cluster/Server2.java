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
public class Server2 {

    public static void main(String[] args) {
        SEServer server = null;
        try{
            Config config = new Config(Config.DEFAULT_CONFIG_FILE_NAME);
            config.getNodeConfig().setName("node2");
            config.getHttpConfig().setPort(9300);
            config.getTcpConfig().setPort(9301);
            config.getPathConfig().setData("D:/searchelastics/node2/data");
            config.getPathConfig().setLogs("D:/searchelastics/node2/logs");
            config.getPathConfig().setWork("D:/searchelastics/node2/work");
            config.getPathConfig().setPlugins("D:/searchelastics/node2/plugins");

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
