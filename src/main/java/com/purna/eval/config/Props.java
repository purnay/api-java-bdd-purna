package com.purna.eval.config;

import io.cucumber.spring.ScenarioScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.Properties;


@Slf4j
@Component
@ScenarioScope
public class Props {

    private static final Properties properties = new Properties();
    private static final String TEST_ENV_PROPS = "test-environment.properties";

    public Props() {
        try {
            File resource = new ClassPathResource(TEST_ENV_PROPS).getFile();
            String propsAsString = new String(Files.readAllBytes(resource.toPath()));
            properties.load(new StringReader(propsAsString));
        } catch (IOException e) {
            log.error("Error while getting properties from file: {}", TEST_ENV_PROPS);
            log.error(e.toString());
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

}
