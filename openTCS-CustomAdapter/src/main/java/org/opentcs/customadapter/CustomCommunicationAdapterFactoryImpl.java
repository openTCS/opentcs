package org.opentcs.customadapter;

import static java.util.Objects.requireNonNull;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.logging.Logger;
import org.opentcs.components.kernel.services.PlantModelService;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;

@Singleton
public class CustomCommunicationAdapterFactoryImpl
    implements
      CustomCommunicationAdapterFactory {

  private static final Logger LOG = Logger.getLogger(
      CustomCommunicationAdapterFactoryImpl.class.getName()
  );

  private final CustomAdapterComponentsFactory adapterFactory;
  private final VehicleConfigurationProvider configProvider;
  private final PlantModelService plantModelService;
  private boolean initialized;

  /**
   * Constructs a new CustomCommunicationAdapterFactoryImpl object.
   *
   * @param adapterFactory The factory used to create adapter components.
   * @param configProvider The provider used for vehicle configurations.
   * @param plantModelService The service used for interacting with plant models.
   */
  @Inject
  public CustomCommunicationAdapterFactoryImpl(
      CustomAdapterComponentsFactory adapterFactory,
      VehicleConfigurationProvider configProvider,
      PlantModelService plantModelService
  ) {
    this.adapterFactory = requireNonNull(adapterFactory, "componentsFactory");
    this.configProvider = configProvider;
    this.plantModelService = plantModelService;
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }
    LOG.info("Initializing CustomCommunicationAdapterFactoryImpl");
    configProvider.loadConfigurations();
    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }
    LOG.info("Terminating CustomCommunicationAdapterFactoryImpl");
    configProvider.saveConfigurations();
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
    return true;
  }

  @Nullable
  @Override
  public VehicleCommAdapter getAdapterFor(@Nonnull
  Vehicle vehicle) {
    LOG.info("Creating adapter for vehicle: " + vehicle.getName());
    return adapterFactory.createCustomCommAdapter(vehicle);
  }
}
