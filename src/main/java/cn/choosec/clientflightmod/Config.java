package cn.choosec.clientflightmod;

import java.io.*;
import java.util.Properties;

import static cn.choosec.clientflightmod.ClientFlightMod.*;

public class Config {
    static void loadConfig() {
        try {
            if (!CONFIG_FILE.exists()) createDefaultConfig();
            try (InputStream input = new FileInputStream(CONFIG_FILE)) {
                Properties props = new Properties();
                props.load(input);
                elytraToggle = Boolean.parseBoolean(props.getProperty("elytratoggle", "true"));
                nofallToggle = Boolean.parseBoolean(props.getProperty("nofalltoggle", "true"));
                speed = Math.max(0, Double.parseDouble(props.getProperty("speed", "1.0")));
                forceflightToggle = Boolean.parseBoolean(props.getProperty("forceflighttoggle", "false"));
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load config", e);
        }
    }

    private static void createDefaultConfig() throws IOException {
        File parentDir = CONFIG_FILE.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                LOGGER.error("Failed to create config directory: {}", parentDir.getAbsolutePath());
                throw new IOException("Failed to create config directory: " + parentDir.getAbsolutePath());
            }
        }

        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            Properties props = new Properties();
            props.setProperty("elytratoggle", "true");
            props.setProperty("nofalltoggle", "true");
            props.setProperty("speed", "1.0");
            props.setProperty("forceflighttoggle", "false");
            props.store(output, null);
            LOGGER.info("Created default config file at {}", CONFIG_FILE.getAbsolutePath());
        }
    }

    static synchronized void saveConfig() {
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            Properties props = new Properties();
            props.setProperty("elytratoggle", String.valueOf(elytraToggle));
            props.setProperty("nofalltoggle", String.valueOf(nofallToggle));
            props.setProperty("speed", String.valueOf(speed));
            props.setProperty("forceflighttoggle", String.valueOf(forceflightToggle));
            props.store(output, null);
        } catch (IOException e) {
            LOGGER.error("Failed to save config", e);
        }
    }
}