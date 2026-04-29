package co.edu.uptc.config;

import co.edu.uptc.configGlobal.ConfigGlobal;

public class PropertiesManager {
    private static final String APP_PROPERTIES = "application.properties";
    private static final String I18N_PATH_TEMPLATE = "i18n/messages_%s.properties";
    private static final String DEFAULT_LANGUAGE = "es";
    private static final String EXTERNAL_CONFIG_DIRECTORY = null;
    private static final PropertiesManager INSTANCE = new PropertiesManager();

    private final ConfigGlobal configGlobal;

    public static PropertiesManager getInstance() {
        return INSTANCE;
    }

    public PropertiesManager() {
        this.configGlobal = new ConfigGlobal(APP_PROPERTIES, I18N_PATH_TEMPLATE, DEFAULT_LANGUAGE,
                EXTERNAL_CONFIG_DIRECTORY);
    }

    public String getMessage(String key) {
        return configGlobal.getMessage(key);
    }

    public String getMessage(String key, Object... args) {
        return configGlobal.getMessage(key, args);
    }

    public int getIntProperty(String key, int defaultValue) {
        return configGlobal.getIntConfig(key, defaultValue);
    }

    public double getDoubleProperty(String key, double defaultValue) {
        return configGlobal.getDoubleConfig(key, defaultValue);
    }

    public String getStringProperty(String key, String defaultValue) {
        return configGlobal.getConfig(key, defaultValue);
    }
}
