// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1;

import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_INTERFACE_NAME;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_MANUFACTURER;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_SERIAL_NUMBER;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_TOPIC_CONNECTION_NAME;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_TOPIC_CONNECTION_QOS;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_TOPIC_INSTANT_ACTIONS_NAME;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_TOPIC_INSTANT_ACTIONS_QOS;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_TOPIC_ORDER_NAME;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_TOPIC_ORDER_QOS;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_TOPIC_PREFIX;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_TOPIC_STATE_NAME;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_TOPIC_STATE_QOS;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_TOPIC_VISU_NAME;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_TOPIC_VISU_QOS;

import java.util.Optional;
import javax.annotation.Nonnull;
import org.opentcs.commadapter.vehicle.vda5050.common.mqtt.QualityOfService;
import org.opentcs.data.model.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MQTT settings for a vehicle.
 *
 * @param vehicleManufacturer The vehicle's manufacturer, to be included in message headers.
 * @param vehicleSerialNumber The vehicle's serial number, to be included in message headers.
 * @param topicNamePrefix The common prefix for all topic names.
 * @param connectionTopicName The name of the topic to subscribe to for receiving connection
 * messages from the vehicle (already including the topic prefix.)
 * @param connectionTopicQos The QoS to be used for subscribing to the connection topic.
 * @param stateTopicName The name of the topic to subscribe to for receiving state messages from the
 * vehicle (already including the topic prefix.)
 * @param stateTopicQos The QoS to be used for subscribing to the state topic.
 * @param visualizationTopicName The name of the topic to subscribe to for receiving visualization
 * messages from the vehicle (already including the topic prefix.)
 * @param visualizationTopicQos The QoS to be used for subscribing to the visualization topic.
 * @param orderTopicName The name of the topic to publish order messages to (already including the
 * topic prefix.)
 * @param orderTopicQos The QoS to be used for publishing order messages.
 * @param instantActionsTopicName The name of the topic to publish instant actions messages to
 * (already including the topic prefix.)
 * @param instantActionsTopicQos The QoS to be used for publishing instant action messages.
 */
public record MqttSetting(
    @Nonnull
    String vehicleManufacturer,
    @Nonnull
    String vehicleSerialNumber,
    @Nonnull
    String topicNamePrefix,
    @Nonnull
    String connectionTopicName,
    @Nonnull
    QualityOfService connectionTopicQos,
    @Nonnull
    String stateTopicName,
    @Nonnull
    QualityOfService stateTopicQos,
    @Nonnull
    String visualizationTopicName,
    @Nonnull
    QualityOfService visualizationTopicQos,
    @Nonnull
    String orderTopicName,
    @Nonnull
    QualityOfService orderTopicQos,
    @Nonnull
    String instantActionsTopicName,
    @Nonnull
    QualityOfService instantActionsTopicQos
) {

  /**
   * Major interface version.
   */
  public static final int VERSION_MAJOR = 1;
  /**
   * Minor interface version.
   */
  public static final int VERSION_MINOR = 1;
  /**
   * Patch version.
   */
  public static final int VERSION_PATCH = 0;
  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(MqttSetting.class);

  /**
   * Creates an {@link MqttSetting} instance using the information provided in the given vehicle.
   *
   * @param vehicle The vehicle.
   * @return A new {@link MqttSetting}, if the vehicle contains all required information; an empty
   * optional if it does not.
   */
  public static Optional<MqttSetting> forVehicle(
      @Nonnull
      Vehicle vehicle
  ) {
    String vehicleManufacturer = vehicle.getProperty(PROPKEY_VEHICLE_MANUFACTURER);
    String vehicleSerialNumber = vehicle.getProperty(PROPKEY_VEHICLE_SERIAL_NUMBER);

    String prefix = Optional.ofNullable(vehicle.getProperty(PROPKEY_VEHICLE_TOPIC_PREFIX))
        .or(() -> {
          String interfaceName = vehicle.getProperty(PROPKEY_VEHICLE_INTERFACE_NAME);

          if (interfaceName == null) {
            return Optional.empty();
          }
          return Optional.of(
              interfaceName
                  + "/" + "v" + VERSION_MAJOR
                  + "/" + vehicleManufacturer
                  + "/" + vehicleSerialNumber
          );
        })
        .orElse(null);

    if (vehicleManufacturer == null || vehicleSerialNumber == null || prefix == null) {
      return Optional.empty();
    }

    return Optional.of(
        new MqttSetting(
            vehicleManufacturer,
            vehicleSerialNumber,
            prefix,
            prefix + "/" + Optional.ofNullable(
                vehicle.getProperty(PROPKEY_VEHICLE_TOPIC_CONNECTION_NAME)
            )
                .filter(s -> !s.isBlank())
                .orElse("connection"),
            Optional.ofNullable(vehicle.getProperty(PROPKEY_VEHICLE_TOPIC_CONNECTION_QOS))
                .map(propValue -> toQos(propValue, QualityOfService.AT_MOST_ONCE))
                .orElse(QualityOfService.AT_MOST_ONCE),
            prefix + "/" + Optional.ofNullable(
                vehicle.getProperty(PROPKEY_VEHICLE_TOPIC_STATE_NAME)
            )
                .filter(s -> !s.isBlank())
                .orElse("state"),
            Optional.ofNullable(vehicle.getProperty(PROPKEY_VEHICLE_TOPIC_STATE_QOS))
                .map(propValue -> toQos(propValue, QualityOfService.AT_MOST_ONCE))
                .orElse(QualityOfService.AT_MOST_ONCE),
            prefix + "/" + Optional.ofNullable(vehicle.getProperty(PROPKEY_VEHICLE_TOPIC_VISU_NAME))
                .filter(s -> !s.isBlank())
                .orElse("visualization"),
            Optional.ofNullable(vehicle.getProperty(PROPKEY_VEHICLE_TOPIC_VISU_QOS))
                .map(propValue -> toQos(propValue, QualityOfService.AT_MOST_ONCE))
                .orElse(QualityOfService.AT_MOST_ONCE),
            prefix + "/" + Optional.ofNullable(
                vehicle.getProperty(PROPKEY_VEHICLE_TOPIC_ORDER_NAME)
            )
                .filter(s -> !s.isBlank())
                .orElse("order"),
            Optional.ofNullable(vehicle.getProperty(PROPKEY_VEHICLE_TOPIC_ORDER_QOS))
                .map(propValue -> toQos(propValue, QualityOfService.AT_MOST_ONCE))
                .orElse(QualityOfService.AT_MOST_ONCE),
            prefix + "/" + Optional.ofNullable(
                vehicle.getProperty(PROPKEY_VEHICLE_TOPIC_INSTANT_ACTIONS_NAME)
            )
                .filter(s -> !s.isBlank())
                .orElse("instantActions"),
            Optional.ofNullable(vehicle.getProperty(PROPKEY_VEHICLE_TOPIC_INSTANT_ACTIONS_QOS))
                .map(propValue -> toQos(propValue, QualityOfService.AT_MOST_ONCE))
                .orElse(QualityOfService.AT_MOST_ONCE)
        )
    );
  }

  @Nonnull
  private static QualityOfService toQos(String input, QualityOfService defaultValue) {
    return switch (input.strip().toUpperCase()) {
      case "AT_MOST_ONCE", "0" -> QualityOfService.AT_MOST_ONCE;
      case "AT_LEAST_ONCE", "1" -> QualityOfService.AT_LEAST_ONCE;
      case "EXACTLY_ONCE", "2" -> QualityOfService.EXACTLY_ONCE;
      default -> {
        LOG.warn("Invalid QoS value '{}', using default value '{}'", input, defaultValue);
        yield defaultValue;
      }
    };
  }

}
