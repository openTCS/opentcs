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
   * Retrieves the configuration for a specific vehicle.
   *
   * @param vehicleName The name of the vehicle.
   * @return The configuration for the specified vehicle, or null if the configuration does not
   * exist.
   */
  public PeripheralDeviceConfiguration getConfiguration(String vehicleName) {
    return configurations.getOrDefault(
        vehicleName, new PeripheralDeviceConfiguration("ModbusTCP", "0.0.0.0", 502)
    );
  }

  /**
   * Sets the configuration for a specific vehicle.
   * The configuration is stored based on the vehicle name.
   * After setting the configuration, it is saved to a file.
   *
   * @param vehicleName The name of the vehicle.
   * @param config The configuration to be set.
   */
  public void setConfiguration(String vehicleName, PeripheralDeviceConfiguration config) {
    configurations.put(vehicleName, config);
    saveConfigurations();
  }

  /**
   * Loads vehicle configurations from a JSON file.
   */
  public void loadConfigurations() {
    try (FileReader reader = new FileReader(CONFIG_FILE)) {
      Type type = new TypeToken<HashMap<String, PeripheralDeviceConfiguration>>() {}.getType();
      Map<String, PeripheralDeviceConfiguration> loadedConfigs = new Gson().fromJson(reader, type);
      if (loadedConfigs != null && !loadedConfigs.isEmpty()) {
        configurations.clear();
        configurations.putAll(loadedConfigs);

        for (int i = 0; i < 2; i++) {
          String specificVehicle = "";
          if (i == 0) {
            specificVehicle = "SAA-mini-OHT-Sensor0001";
          }
          else {
            specificVehicle = "SAA-mini-OHT-Sensor0002";
          }
          PeripheralDeviceConfiguration config = loadedConfigs.get(specificVehicle);

          if (config != null) {
            LOG.info(
                String.format(
                    "Configuration for %s: currentStrategy: %s, host: %s, port: %d",
                    specificVehicle, config.currentStrategy(), config.host(), config.port()
                )
            );
          }
          else {
            LOG.warning("Configuration for " + specificVehicle + " not found.");
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
