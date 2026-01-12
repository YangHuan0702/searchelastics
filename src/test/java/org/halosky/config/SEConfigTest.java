package org.halosky.config;

import org.junit.jupiter.api.Test;

/**
 * packageName org.halosky.config
 *
 * @author huan.yang
 * @className SEConfigTest
 * @date 2026/1/6
 */
public class SEConfigTest {


    @Test
    public void readConfigTest() {
        try {
            Config config = new Config(Config.DEFAULT_CONFIG_FILE_NAME);
            System.out.println(config);
        }catch (Exception e){
            e.printStackTrace();
        }

    }


}
