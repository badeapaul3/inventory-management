package com.inventory.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Paul Badea
 **/
public class ConfigManager {

    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    private static final ConfigManager INSTANCE = new ConfigManager();
    private final Properties properties;


    public ConfigManager() {
        properties = new Properties();
        loadProperties();
    }

    public static ConfigManager getInstance() {
        return INSTANCE;
    }

    private void loadProperties(){
        try (InputStream input = ConfigManager.class.getClassLoader().getResourceAsStream("application.properties.old"))    {
            if(input == null){
                logger.warn("application.properties.old not found, using defaults.");
                return;
            }
            properties.load(input);
            logger.info("Loaded configuration from application.properties.old.");
        }catch (IOException e){
            logger.error("Failed to load application.properties.old: {}", e.getMessage(), e);
        }
    }

    public String getProperty(String key, String defaultValue) {
        String envValue = System.getenv(key.toUpperCase().replace(".", "_"));
        if (envValue != null && !envValue.isEmpty()) {
            logger.debug("Using environment variable for {}: {}", key, envValue);
            return envValue;
        }
        String propValue = properties.getProperty(key);
        if (propValue != null && !propValue.isEmpty()) {
            logger.debug("Using property file value for {}: {}", key, propValue);
            return propValue;
        }
        logger.debug("Using default value for {}: {}", key, defaultValue);
        return defaultValue;
    }

    public int getIntProperty(String key, int defaultValue) {
        String value = getProperty(key, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer for {}: {}, using default: {}", key, value, defaultValue);
            return defaultValue;
        }
    }

    public double getDoubleProperty(String key, double defaultValue) {
        String value = getProperty(key, String.valueOf(defaultValue));
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            logger.warn("Invalid double for {}: {}, using default: {}", key, value, defaultValue);
            return defaultValue;
        }
    }

}
