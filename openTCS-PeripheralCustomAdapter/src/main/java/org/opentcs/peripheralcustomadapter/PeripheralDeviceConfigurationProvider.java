package org.opentcs.peripheralcustomadapter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Singleton;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opentcs.configuration.ConfigurationPrefix;

@Singleton
@ConfigurationPrefix("peripheraldevicecustom")
public class PeripheralDeviceConfigurationProvider {
  private static final Logger LOG = Logger.getLogger(
      PeripheralDeviceConfigurationProvider.class.getName()
  );
  private static final String CONFIG_FILE
      = "C:\\Users\\user\\Desktop\\minioht\\config\\peripheral_config.json";

  private final Map<String, PeripheralDeviceConfiguration> configurations = new HashMap<>();

  PeripheralDeviceConfigurationProvider() {
    LOG.info("Initializing PeripheralDeviceConfigurationProvider");
    loadConfigurations();
    LOG.info(
        "PeripheralDeviceConfigurationProvider initialized with "
            + configurations.size() + " configurations"
    );
  }

  /**
   * Retrieves the configuration for a specific peripheral.
   *
   * @param peripheralName The name of the peripheral.
   * @return The configuration for the specified peripheral, or null if the configuration does not
   * exist.
   */
  public PeripheralDeviceConfiguration getConfiguration(String peripheralName) {
    return configurations.getOrDefault(
        peripheralName, new PeripheralDeviceConfiguration("ModbusTCP", "0.0.0.0", 502)
    );
  }

  /**
   * Sets the configuration for a specific peripheral.
   * The configuration is stored based on the peripheral name.
   * After setting the configuration, it is saved to a file.
   *
   * @param peripheralName The name of the peripheral.
   * @param config The configuration to be set.
   */
  public void setConfiguration(String peripheralName, PeripheralDeviceConfiguration config) {
    configurations.put(peripheralName, config);
    saveConfigurations();
  }

  /**
   * Loads peripheral configurations from a JSON file.
   */
  public void loadConfigurations() {

    try (FileReader reader = new FileReader(CONFIG_FILE)) {
      Type type = new TypeToken<HashMap<String, PeripheralDeviceConfiguration>>() {}.getType();
      Map<String, PeripheralDeviceConfiguration> loadedConfigs = new Gson().fromJson(reader, type);
      if (loadedConfigs != null && !loadedConfigs.isEmpty()) {
        configurations.clear();
        configurations.putAll(loadedConfigs);
        String specificPeripheral = "";
        for (int i = 0; i < 4; i++) {
          if (i == 0) {
            specificPeripheral = "STK_IN";
          }
          else if (i == 1) {
            specificPeripheral = "OHB";
          }
          else if (i == 2) {
            specificPeripheral = "Sidefork";
          }
          else {
            specificPeripheral = "Magazine_loadport";
          }
          PeripheralDeviceConfiguration config = loadedConfigs.get(specificPeripheral);

          if (config != null) {
            LOG.info(
                String.format(
                    "Configuration for %s: currentStrategy: %s, host: %s, port: %d",
                    specificPeripheral, config.currentStrategy(), config.host(), config.port()
                )
            );
          }
          else {
            LOG.warning("Configuration for " + specificPeripheral + " not found.");
          }
        }
      }
      else {
        LOG.warning("No configurations loaded or file is empty.");
      }
    }
    catch (IOException e) {
      LOG.log(Level.WARNING, "Failed to load configurations. Using empty configuration.", e);
    }
  }

  /**
   * Saves the configurations to a file.
   */
  public void saveConfigurations() {
    try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
      new Gson().toJson(configurations, writer);
      LOG.info("Configurations saved successfully.");
    }
    catch (IOException e) {
      LOG.log(Level.SEVERE, "Failed to save configurations", e);
    }
  }
}
