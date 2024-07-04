package org.opentcs.customadapter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;
import org.opentcs.drivers.vehicle.VehicleCommAdapterFactory;

public class CustomAdapterComponentsFactory
    implements
      VehicleCommAdapterFactory {

  private static final Logger LOG = Logger.getLogger(
      CustomAdapterComponentsFactory.class.getName()
  );
  private static final String CONFIG_FILE = "vehicle_config.json";

  private final ScheduledExecutorService executor;
  private final Map<String, VehicleConfigurationInterface> vehicleConfigurations = new HashMap<>();
  private final Map<String, CommunicationStrategy> strategies = new HashMap<>();
  private boolean initialized;

  /**
   * Constructs a CustomAdapterComponentsFactory.
   *
   * @param executor The scheduled executor service to be used by the factory.
   * @inject
   */
  @Inject
  public CustomAdapterComponentsFactory(@KernelExecutor
  ScheduledExecutorService executor) {
    this.executor = executor;
    strategies.put("ModbusTCP", new ModbusTCPStrategy());
    loadConfigurations();
  }

  @Override
  public void initialize() {
    LOG.info("Initializing CustomAdapterComponentsFactory");
    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    LOG.info("Terminating CustomAdapterComponentsFactory");
    saveConfigurations();
    vehicleConfigurations.clear();
    strategies.clear();
    initialized = false;
  }

  @Override
  public VehicleCommAdapterDescription getDescription() {
    return new CustomVehicleCommAdapterDescription();
  }

  @Override
  public boolean providesAdapterFor(@Nonnull
  Vehicle vehicle) {
    LOG.fine("Checking if adapter is provided for vehicle: " + vehicle.getName());
    return vehicleConfigurations.containsKey(vehicle.getName());
  }

  @Nullable
  @Override
  public VehicleCommAdapter getAdapterFor(@Nonnull
  Vehicle vehicle) {
    LOG.info("Creating adapter for vehicle: " + vehicle.getName());

    VehicleConfigurationInterface config = vehicleConfigurations.computeIfAbsent(
        vehicle.getName(),
        k -> createConfig(vehicle)
    );

    String strategyKey = config.getCommunicationStrategy();
    CommunicationStrategy strategy = strategies.get(strategyKey);

    if (strategy == null) {
      LOG.warning(
          "No strategy found for key: " + strategyKey + ". Using default ModbusTCP strategy."
      );
      strategy = strategies.get("ModbusTCP");
    }

    return strategy.createAdapter(vehicle, config, executor);
  }

  private VehicleConfigurationInterface createConfig(Vehicle vehicle) {
    String defaultHost = "localhost";
    String defaultPort = "502";
    String defaultStrategy = "ModbusTCP";

    JTextField hostField = new JTextField(defaultHost,10);
    JTextField portField = new JTextField(defaultPort,10);
    JComboBox<String> strategyComboBox = new JComboBox<>(
        strategies.keySet().toArray(new String[0])
    );
    strategyComboBox.setSelectedItem(defaultStrategy);

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.add(new JLabel("Host:"));
    panel.add(hostField);
    panel.add(new JLabel("Port:"));
    panel.add(portField);
    panel.add(new JLabel("Communication Strategy:"));
    panel.add(strategyComboBox);

    int result = JOptionPane.showConfirmDialog(
        null, panel,
        "Enter Configuration for " + vehicle.getName(), JOptionPane.OK_CANCEL_OPTION
    );
    if (result == JOptionPane.OK_OPTION) {
      DefaultVehicleConfiguration config = new DefaultVehicleConfiguration();
      config.setHost(hostField.getText());
      try {
        config.setPort(Integer.parseInt(portField.getText()));
      }
      catch (NumberFormatException e) {
        LOG.warning("Invalid port number. Using default port 502.");
        config.setPort(502);
      }
      config.setCommunicationStrategy((String) strategyComboBox.getSelectedItem());
      saveConfigurations();
      return config;
    }
    else {
      return new DefaultVehicleConfiguration();
    }
  }

  public void setVehicleConfiguration(String vehicleName, VehicleConfigurationInterface config) {
    LOG.info("Adding configuration for vehicle: " + vehicleName);
    vehicleConfigurations.put(vehicleName, config);
    saveConfigurations();
  }

  public void removeVehicleConfiguration(String vehicleName) {
    LOG.info("Removing configuration for vehicle: " + vehicleName);
    vehicleConfigurations.remove(vehicleName);
    saveConfigurations();
  }

  private void loadConfigurations() {
    try (FileReader reader = new FileReader(CONFIG_FILE)) {
      Type type = new TypeToken<HashMap<String, DefaultVehicleConfiguration>>() {}.getType();
      Map<String, DefaultVehicleConfiguration> loadedConfigs = new Gson().fromJson(reader, type);
      vehicleConfigurations.putAll(loadedConfigs);
    }
    catch (IOException e) {
      LOG.log(Level.WARNING, "Failed to load configurations", e);
    }
  }

  private void saveConfigurations() {
    try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
      new Gson().toJson(vehicleConfigurations, writer);
    }
    catch (IOException e) {
      LOG.log(Level.WARNING, "Failed to save configurations", e);
    }
  }

  private static class CustomVehicleCommAdapterDescription
      extends
        VehicleCommAdapterDescription {

    CustomVehicleCommAdapterDescription() {
      // Do nothing
    }

    @Override
    public String getDescription() {
      return "Custom Vehicle Communication Adapter";
    }

    @Override
    public boolean isSimVehicleCommAdapter() {
      return false;
    }
  }
}

/**
 *
 */
interface CommunicationStrategy {
  VehicleCommAdapter createAdapter(
      Vehicle vehicle,
      VehicleConfigurationInterface config,
      @KernelExecutor
      ScheduledExecutorService executor
  );
}

/**
 *
 */
class ModbusTCPStrategy
    implements
      CommunicationStrategy {
  ModbusTCPStrategy() {
    // Do nothing
  }

  @Override
  public VehicleCommAdapter createAdapter(
      Vehicle vehicle,
      VehicleConfigurationInterface config,
      @KernelExecutor
      ScheduledExecutorService executor
  ) {
    return new ModbusTCPVehicleCommAdapter(
        new CustomProcessModel(vehicle),
        executor,
        config.getHost(),
        config.getPort()
    );
  }
}
