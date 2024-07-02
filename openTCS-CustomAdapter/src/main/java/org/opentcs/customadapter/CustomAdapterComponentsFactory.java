package org.opentcs.customadapter;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
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

  /**
   * This class's logger.
   */
  private static final Logger LOG = Logger.getLogger(CustomAdapterComponentsFactory.class.getName());

  /**
   * Map to store vehicle configuration.
   */
  private final Map<String, VehicleConfigurationInterface> vehicleConfigurations = new HashMap<>();

  /**
   * Flag indicating whether the factory has been initialized
   */
  private boolean initialized;

  @Override
  public void initialize() {
    LOG.info("Initializing CustomAdapterComponentsFactory");
    // Add any necessary initialization logic here
    // For example, you might want to load default configurations for known vehicles
    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    LOG.info("Terminating CustomAdapterComponentsFactory");
    // Add any necessary cleanup logic here
    // For example, you might want to close any open connections or release resources
    vehicleConfigurations.clear();
    initialized = false;
  }

  @Override
  public VehicleCommAdapterDescription getDescription() {
    return new CustomVehicleCommAdapterDescription();
  }

  @Override
  public boolean providesAdapterFor(@Nonnull Vehicle vehicle) {
    LOG.fine("Checking if adapter is provided for vehicle: " + vehicle.getName());
    // Implement specific logic based on requirements to determine whether to provide an adapter for a given vehicle
    // For example, you might check if the vehicle has certain properties or capabilities
    return vehicleConfigurations.containsKey(vehicle.getName()) ||
        vehicle.getProperties().containsKey("customAdapter");
  }

  @Nullable
  @Override
  public VehicleCommAdapter getAdapterFor(@Nonnull Vehicle vehicle) {
    LOG.info("Creating adapter for vehicle: " + vehicle.getName());
    VehicleConfigurationInterface config = vehicleConfigurations.getOrDefault(
        vehicle.getName(),
        new DefaultVehicleConfiguration()
    );
    return new CustomVehicleCommAdapter(
        new CustomVehicleModel(vehicle),
        config.getRechargeOperation(),
        config.getCommandsCapacity(),
        config.getExecutorService()
    );
  }

  /**
   * Add or update vehicle configuration
   *
   * @param vehicleName vehicle name
   * @param config Vehicle configuration
   */
  public void addVehicleConfiguration(String vehicleName, VehicleConfigurationInterface config) {
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
