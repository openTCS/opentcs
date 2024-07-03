/**
 * Copyright (c) The SAA Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.customadapter;

import com.google.inject.Inject;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;
import org.opentcs.drivers.vehicle.VehicleCommAdapterFactory;

/**
 * CustomAdapterComponentsFactory class is an implementation of VehicleCommAdapterFactory.
 * It provides communication adapter instances for vehicles to be controlled.
 */
public class CustomAdapterComponentsFactory
    implements
      VehicleCommAdapterFactory {

  /**
   * This class's logger.
   */
  private static final Logger LOG = Logger.getLogger(
      CustomAdapterComponentsFactory.class.getName()
  );

  /**
   * The kernel executor.
   */
  private final ScheduledExecutorService executor;

  /**
   * Map to store vehicle configuration.
   */
  private final Map<String, VehicleConfigurationInterface> vehicleConfigurations = new HashMap<>();

  /**
   * Map to store communication strategies.
   */
  private final Map<String, CommunicationStrategy> strategies = new HashMap<>();

  /**
   * Flag indicating whether the factory has been initialized.
   */
  private boolean initialized;

  /**
   * Constructor.
   *
   * @param executor The kernel executor.
   */
  @Inject
  public CustomAdapterComponentsFactory(
      @KernelExecutor
      ScheduledExecutorService executor
  ) {
    this.executor = executor;
    strategies.put("ModbusTCP", new ModbusTCPStrategy());
  }

  @Override
  public void initialize() {
    LOG.info("Initializing CustomAdapterComponentsFactory");
    // Add any necessary initialization logic here
    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    LOG.info("Terminating CustomAdapterComponentsFactory");
    vehicleConfigurations.clear();
    strategies.clear();
    initialized = false;
  }

  @Override
  public VehicleCommAdapterDescription getDescription() {
    return new CustomVehicleCommAdapterDescription();
  }

  @Override
  public boolean providesAdapterFor(
      @Nonnull
      Vehicle vehicle
  ) {
    LOG.fine("Checking if adapter is provided for vehicle: " + vehicle.getName());
    return vehicleConfigurations.containsKey(vehicle.getName());
  }

  /**
   * Returns the appropriate VehicleCommAdapter for the given Vehicle.
   *
   * @param vehicle The Vehicle for which to get the adapter.
   * @return The VehicleCommAdapter for the given Vehicle, or null if no suitable adapter is found.
   */
  @Nullable
  @Override
  public VehicleCommAdapter getAdapterFor(
      @Nonnull
      Vehicle vehicle
  ) {
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
    JTextField hostField = new JTextField(10);
    JTextField portField = new JTextField(10);

    JPanel panel = new JPanel();
    panel.add(new JLabel("Host:"));
    panel.add(hostField);
    panel.add(Box.createHorizontalStrut(15)); // a spacer
    panel.add(new JLabel("Port:"));
    panel.add(portField);

    int result = JOptionPane.showConfirmDialog(
        null, panel,
        "Enter Host and Port for " + vehicle.getName(), JOptionPane.OK_CANCEL_OPTION
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
      return config;
    }
    else {
      return new DefaultVehicleConfiguration();
    }
  }

  /**
   * Add or update vehicle configuration.
   *
   * @param vehicleName vehicle name
   * @param config Vehicle configuration
   */
  public void setVehicleConfiguration(String vehicleName, VehicleConfigurationInterface config) {
    LOG.info("Adding configuration for vehicle: " + vehicleName);
    vehicleConfigurations.put(vehicleName, config);
  }

  /**
   * Remove vehicle configuration.
   *
   * @param vehicleName vehicle name
   */
  public void removeVehicleConfiguration(String vehicleName) {
    LOG.info("Removing configuration for vehicle: " + vehicleName);
    vehicleConfigurations.remove(vehicleName);
  }

  /**
   * Custom vehicle communication adapter description class.
   */
  private static class CustomVehicleCommAdapterDescription
      extends
        VehicleCommAdapterDescription {

    /**
     * Creates a new instance.
     */
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
 * Interface for communication strategies.
 */
interface CommunicationStrategy {

  /**
   * Creates an adapter for the given vehicle and configuration.
   *
   * @param vehicle The vehicle.
   * @param config The vehicle configuration.
   * @param executor The kernel executor.
   * @return The created vehicle communication adapter.
   */
  VehicleCommAdapter createAdapter(
      Vehicle vehicle,
      VehicleConfigurationInterface config,
      @KernelExecutor
      ScheduledExecutorService executor
  );
}

/**
 * ModbusTCP communication strategy.
 */
class ModbusTCPStrategy
    implements
      CommunicationStrategy {

  /**
   * Creates a new instance.
   */
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
        new CustomVehicleModel(vehicle),
        executor,
        config.getHost(),
        config.getPort()
    );
  }
}
