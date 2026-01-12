package org.halosky.config;

import com.twelvemonkeys.lang.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

/**
 * packageName org.halosky.config
 *
 * @author huan.yang
 * @className Config
 * @date 2026/1/6
 */
@Slf4j
public class Config {

    public final static String DEFAULT_CONFIG_FILE_NAME = "config.yaml";

    private final ConfigContext configContext;

    public Config(String fileName) throws IOException {
        String targetName = StringUtil.isEmpty(fileName) ?  DEFAULT_CONFIG_FILE_NAME : fileName;
        log.info("[Config] ready read to config file [{}] ",targetName);

        Yaml yaml = new Yaml(new Constructor(ConfigContext.class,new LoaderOptions()));
        try(InputStream inputStream = getClass().getClassLoader().getResourceAsStream(targetName)) {
            configContext = yaml.load(inputStream);
        }
        log.info("[Config] config initialization done. context: \n {}",configContext);
    }


    public HttpConfig getHttpConfig() {
        if(Objects.isNull(configContext)){
            throw new NullPointerException("[Config]configContext is null,unread to http config.");
        }
        return configContext.getHttp();
    }


    public NetworkConfig getNetworkConfig() {
        if(Objects.isNull(configContext)){
            throw new NullPointerException("[Config]configContext is null,unread to network config.");
        }
        return configContext.getNetwork();
    }


}
