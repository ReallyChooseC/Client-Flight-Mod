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
            }
        } catch (Exception e) { System.err.println("Failed to load config"); }
    }

    private static void createDefaultConfig() throws IOException {
        CONFIG_FILE.getParentFile().mkdirs();
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            Properties props = new Properties();
            props.setProperty("elytratoggle", "true");
            props.setProperty("nofalltoggle", "true");
            props.setProperty("speed", "1.0");
            props.store(output, null);
        }
    }

    static synchronized void saveConfig() {
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            Properties props = new Properties();
            props.setProperty("elytratoggle", String.valueOf(elytraToggle));
            props.setProperty("speed", String.valueOf(speed));
            props.store(output, null);
        } catch (IOException e) { System.err.println("Failed to save config"); }
    }
}
