// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.Objects.requireNonNull;
import static org.opentcs.commadapter.vehicle.vda5050.common.PropertyExtractions.getPropertyBoolean;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_VALIDATE_INCOMING_MESSAGES;

import jakarta.inject.Inject;
import jakarta.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.opentcs.commadapter.vehicle.vda5050.Vda5050CommAdapterFactory;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;

/**
 */
public class CommAdapterFactory
    implements
      Vda5050CommAdapterFactory {

  /**
   * Annotation to identify this implementation.
   */
  @Qualifier
  @Target({PARAMETER})
  @Retention(RUNTIME)
  public @interface V2dot0 {
  }

  /**
   * Indicates whether a vehicle has all required properties to be handled by this comm adapter.
   */
  private final VehicleHasRequiredProperties hasRequiredProperties;
  /**
   * The components factory responsible to create all components needed for the comm adapter.
   */
  private final CommAdapterComponentsFactory componentsFactory;

  /**
   * Creates a new instance.
   *
   * @param hasRequiredProperties Indicates whether a vehicle has all required properties to be
   * handled by this comm adapter.
   * @param componentsFactory The factory to create components specific to the comm adapter.
   */
  @Inject
  public CommAdapterFactory(
      VehicleHasRequiredProperties hasRequiredProperties,
      CommAdapterComponentsFactory componentsFactory
  ) {
    this.hasRequiredProperties = requireNonNull(hasRequiredProperties, "hasRequiredProperties");
    this.componentsFactory = requireNonNull(componentsFactory, "componentsFactory");
  }

  @Override
  public boolean providesAdapterFor(Vehicle vehicle) {
    return hasRequiredProperties.test(vehicle);
  }

  @Override
  public VehicleCommAdapter getAdapterFor(Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");
    if (!providesAdapterFor(vehicle)) {
      return null;
    }

    return componentsFactory.createCommAdapterImpl(
        vehicle,
        MqttSetting.forVehicle(vehicle).orElseThrow(IllegalStateException::new),
        createMessageValidator(vehicle)
    );
  }

  private MessageValidator createMessageValidator(Vehicle vehicle) {
    if (getPropertyBoolean(PROPKEY_VEHICLE_VALIDATE_INCOMING_MESSAGES, vehicle).orElse(true)) {
      return new MessageValidator();
    }
    else {
      return MessageValidator.ACCEPTING_ALL;
    }
  }
}
