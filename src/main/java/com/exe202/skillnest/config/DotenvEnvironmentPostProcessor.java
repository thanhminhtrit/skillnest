package com.exe202.skillnest.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String PROPERTY_SOURCE_NAME = "dotenvProperties";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        File dotenvFile = findDotenvFile();
        if (dotenvFile == null || !dotenvFile.exists()) {
            return;
        }

        Map<String, Object> properties = new LinkedHashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(dotenvFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int idx = line.indexOf('=');
                if (idx < 1) {
                    continue;
                }
                String key = line.substring(0, idx).trim();
                String value = line.substring(idx + 1).trim();
                // Only add if not already set by system env or system properties
                if (!environment.containsProperty(key)) {
                    properties.put(key, value);
                }
            }
        } catch (IOException e) {
            // silently skip if file cannot be read
            return;
        }

        if (!properties.isEmpty()) {
            environment.getPropertySources().addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, properties));
        }
    }

    private File findDotenvFile() {
        // Look in working directory first, then user home
        File cwd = new File(System.getProperty("user.dir"), ".env");
        if (cwd.exists()) {
            return cwd;
        }
        return null;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 10;
    }
}
