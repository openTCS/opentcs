package org.opentcs.customadapter;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
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
   * Map to store vehicle configuration.
   */
  private final Map<String, VehicleConfigurationInterface> vehicleConfigurations = new HashMap<>();

  /**
   * Flag indicating whether the factory has been initialized
   */
  private boolean initialized;

  @Override
  public void initialize() {
    // Do any necessary initialization here
    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    // Do any necessary cleanup here
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
    // Implement specific logic based on requirements to determine whether to provide an adapter for a given vehicle
    return true;
  }

  @Nullable
  @Override
  public VehicleCommAdapter getAdapterFor(
      @Nonnull
      Vehicle vehicle
  ) {
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
    vehicleConfigurations.put(vehicleName, config);
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
