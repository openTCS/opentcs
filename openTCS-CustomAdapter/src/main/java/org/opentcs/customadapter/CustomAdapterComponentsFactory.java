package org.opentcs.customadapter;

import com.google.inject.Inject;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;
import org.opentcs.drivers.vehicle.VehicleCommAdapterFactory;

/**
 * Customized vehicle communication adapter factory.
 * Responsible for creating and managing custom vehicle communication adapters.
 */
public class CustomAdapterComponentsFactory
    implements VehicleCommAdapterFactory {

  private final ScheduledExecutorService executor;


  /**
   * Constructor.
   */
  @Inject
  public CustomAdapterComponentsFactory(
      @KernelExecutor
      ScheduledExecutorService executor
  ) {
    strategies.put("ModbusTCP", new ModbusTCPStrategy());
    this.executor = executor;
  }

  /**
   * This class's logger.
   */
  private static final Logger LOG = Logger.getLogger(
      CustomAdapterComponentsFactory.class.getName());

  /**
   * Map to store vehicle configuration.
   */
  private final Map<String, VehicleConfigurationInterface> vehicleConfigurations = new HashMap<>();

  /**
   * Map to store communication strategies.
   */
  private final Map<String, CommunicationStrategy> strategies = new HashMap<>();

  /**
   * Flag indicating whether the factory has been initialized
   */
  private boolean initialized;

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

//    String strategyKey = vehicle.getProperties().getOrDefault("commStrategy", "ModbusTCP");
    String strategyKey = config.getCommunicationStrategy();
    CommunicationStrategy strategy = strategies.get(strategyKey);

    if (strategy == null) {
      LOG.warning(
          "No strategy found for key: " + strategyKey + ". Using default ModbusTCP strategy.");
      strategy = strategies.get("ModbusTCP");
    }

    return strategy.createAdapter(vehicle, config, executor);
  }

  private VehicleConfigurationInterface createConfig(Vehicle vehicle) {
    DefaultVehicleConfiguration config = new DefaultVehicleConfiguration();
//    config.setHost(vehicle.getProperties().getOrDefault("host", "localhost"));
//    config.setPort(Integer.parseInt(vehicle.getProperties().getOrDefault("port", "502")));
    return config;
  }

  /**
   * Add or update vehicle configuration
   *
   * @param vehicleName vehicle name
   * @param config Vehicle configuration
   */
  public void setVehicleConfiguration(String vehicleName, VehicleConfigurationInterface config) {
    LOG.info("Adding configuration for vehicle: " + vehicleName);
    vehicleConfigurations.put(vehicleName, config);
  }

  /**
   * Remove vehicle configuration
   *
   * @param vehicleName vehicle name
   */
  public void removeVehicleConfiguration(String vehicleName) {
    LOG.info("Removing configuration for vehicle: " + vehicleName);
    vehicleConfigurations.remove(vehicleName);
  }

  /**
   * Custom vehicle communication adapter description class
   */
  private static class CustomVehicleCommAdapterDescription
      extends VehicleCommAdapterDescription {

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
  VehicleCommAdapter createAdapter(
      Vehicle vehicle, VehicleConfigurationInterface config,
      @KernelExecutor
      ScheduledExecutorService executor
  );
}

/**
 * ModbusTCP communication strategy.
 */
class ModbusTCPStrategy
    implements CommunicationStrategy {
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

/**
 * Ethernet communication strategy.
 */
//class EthernetStrategy implements CommunicationStrategy {
//  @Override
//  public VehicleCommAdapter createAdapter(Vehicle vehicle, VehicleConfigurationInterface config) {
//    return new EthernetVehicleCommAdapter(
//        new CustomVehicleModel(vehicle),
//        config.getRechargeOperation(),
//        config.getCommandsCapacity(),
//        config.getExecutorService()
//    );
//  }
//}
