package org.opentcs.peripheralcustomadapter;

import static java.util.Objects.requireNonNull;

import com.google.inject.Singleton;
import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import java.util.logging.Logger;
import org.opentcs.components.kernel.services.PeripheralService;
import org.opentcs.data.model.Location;
import org.opentcs.drivers.peripherals.PeripheralCommAdapter;
import org.opentcs.drivers.peripherals.PeripheralCommAdapterDescription;

@Singleton
public class PeripheralCustomCommunicationAdapterIFactoryImpl
    implements
      PeripheralCustomCommunicationAdapterFactory {

  private static final Logger LOG = Logger.getLogger(
      PeripheralCustomCommunicationAdapterIFactoryImpl.class.getName()
  );
  /**
   * The adapter components factory.
   */
  private final PeripheralCustomAdapterComponentsFactory componentsFactory;
  /**
   * The Peripheral Device Config Provider.
   */
  private final PeripheralDeviceConfigurationProvider configProvider;
  /**
   * The Peripheral Service.
   */
  private final PeripheralService peripheralService;
  /**
   * Indicates whether this component is initialized or not.
   */
  private boolean initialized;

  /**
   * Constructs a new CustomCommunicationAdapterFactoryImpl object.
   *
   * @param componentsFactory The factory used to create adapter components.
   * @param configProvider The provider used for vehicle configurations.
   * @param peripheralService The peripheral service.
   */
  @Inject
  public PeripheralCustomCommunicationAdapterIFactoryImpl(
      PeripheralCustomAdapterComponentsFactory componentsFactory,
      PeripheralDeviceConfigurationProvider configProvider,
      PeripheralService peripheralService
  ) {
    this.componentsFactory = requireNonNull(componentsFactory, "componentsFactory");
    this.configProvider = configProvider;
    this.peripheralService = requireNonNull(peripheralService, "peripheralService");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }
    LOG.info("Initializing PeripheralCustomCommunicationAdapterImpl");
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
    LOG.info("Terminating PeripheralCustomCommunicationAdapterImpl");
    configProvider.saveConfigurations();
    initialized = false;
  }

  @Nonnull
  @Override
  public PeripheralCommAdapterDescription getDescription() {
    return new PeripheralCustomCommunicationAdapterDescription();
  }

  @Override
  public boolean providesAdapterFor(
      @Nonnull
      Location location
  ) {
    requireNonNull(location, "location");
    LOG.fine("Checking if Peripheral adapter is provided for Location: " + location.getName());
    return true;
  }

  @Override
  public PeripheralCommAdapter getAdapterFor(
      @Nonnull
      Location location
  ) {
    requireNonNull(location, "location");
    LOG.info("Creating Peripheral adapter for Location: " + location.getName());
    return componentsFactory.createPeripheralCustomCommAdapter(
        location.getReference(), peripheralService
    );
  }
}
