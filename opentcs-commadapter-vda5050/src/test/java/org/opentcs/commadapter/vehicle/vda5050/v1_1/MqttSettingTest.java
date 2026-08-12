// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertWith;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_INTERFACE_NAME;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_MANUFACTURER;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_SERIAL_NUMBER;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_TOPIC_CONNECTION_NAME;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_TOPIC_CONNECTION_QOS;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_TOPIC_INSTANT_ACTIONS_NAME;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_TOPIC_ORDER_NAME;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_TOPIC_PREFIX;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_TOPIC_STATE_NAME;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_TOPIC_VISU_NAME;

import org.junit.jupiter.api.Test;
import org.opentcs.commadapter.vehicle.vda5050.common.mqtt.QualityOfService;
import org.opentcs.data.model.Vehicle;

/**
 */
public class MqttSettingTest {

  @Test
  void refuseVehicleLackingProperties() {
    assertThat(MqttSetting.forVehicle(new Vehicle("some-vehicle"))).isEmpty();
  }

  @Test
  void acceptAndApplyPrefix() {
    Vehicle vehicle = new Vehicle("some-vehicle")
        .withProperty(PROPKEY_VEHICLE_TOPIC_PREFIX, "some-prefix")
        .withProperty(PROPKEY_VEHICLE_MANUFACTURER, "some-manufacturer")
        .withProperty(PROPKEY_VEHICLE_SERIAL_NUMBER, "1234");

    assertWith(
        MqttSetting.forVehicle(vehicle),
        optMqttSetting -> {
          assertThat(optMqttSetting).isPresent();

          MqttSetting mqttSetting = optMqttSetting.get();
          assertThat(mqttSetting.topicNamePrefix()).isEqualTo("some-prefix");
          assertThat(mqttSetting.vehicleManufacturer()).isEqualTo("some-manufacturer");
          assertThat(mqttSetting.vehicleSerialNumber()).isEqualTo("1234");

          assertThat(mqttSetting.connectionTopicName()).isEqualTo("some-prefix/connection");
          assertThat(mqttSetting.stateTopicName()).isEqualTo("some-prefix/state");
          assertThat(mqttSetting.visualizationTopicName()).isEqualTo("some-prefix/visualization");
          assertThat(mqttSetting.orderTopicName()).isEqualTo("some-prefix/order");
          assertThat(mqttSetting.instantActionsTopicName()).isEqualTo("some-prefix/instantActions");
        }
    );
  }

  @Test
  void acceptAndApplyInterfaceAsPartialPrefix() {
    Vehicle vehicle = new Vehicle("some-vehicle")
        .withProperty(PROPKEY_VEHICLE_INTERFACE_NAME, "some-interface")
        .withProperty(PROPKEY_VEHICLE_MANUFACTURER, "some-manufacturer")
        .withProperty(PROPKEY_VEHICLE_SERIAL_NUMBER, "1234");

    assertWith(
        MqttSetting.forVehicle(vehicle),
        optMqttSetting -> {
          assertThat(optMqttSetting).isPresent();

          MqttSetting mqttSetting = optMqttSetting.get();
          assertThat(mqttSetting.topicNamePrefix())
              .isEqualTo("some-interface/v1/some-manufacturer/1234");
          assertThat(mqttSetting.vehicleManufacturer()).isEqualTo("some-manufacturer");
          assertThat(mqttSetting.vehicleSerialNumber()).isEqualTo("1234");

          assertThat(mqttSetting.connectionTopicName())
              .isEqualTo("some-interface/v1/some-manufacturer/1234/connection");
          assertThat(mqttSetting.stateTopicName())
              .isEqualTo("some-interface/v1/some-manufacturer/1234/state");
          assertThat(mqttSetting.visualizationTopicName())
              .isEqualTo("some-interface/v1/some-manufacturer/1234/visualization");
          assertThat(mqttSetting.orderTopicName())
              .isEqualTo("some-interface/v1/some-manufacturer/1234/order");
          assertThat(mqttSetting.instantActionsTopicName())
              .isEqualTo("some-interface/v1/some-manufacturer/1234/instantActions");
        }
    );
  }

  @Test
  void preferPrefixOverInterface() {
    Vehicle vehicle = new Vehicle("some-vehicle")
        .withProperty(PROPKEY_VEHICLE_TOPIC_PREFIX, "some-prefix")
        .withProperty(PROPKEY_VEHICLE_INTERFACE_NAME, "some-interface")
        .withProperty(PROPKEY_VEHICLE_MANUFACTURER, "some-manufacturer")
        .withProperty(PROPKEY_VEHICLE_SERIAL_NUMBER, "1234");

    assertThat(MqttSetting.forVehicle(vehicle))
        .isPresent()
        .get()
        .extracting(MqttSetting::topicNamePrefix)
        .isEqualTo("some-prefix");
  }

  @Test
  void applyTopicNames() {
    Vehicle vehicle = new Vehicle("some-vehicle")
        .withProperty(PROPKEY_VEHICLE_TOPIC_PREFIX, "some-prefix")
        .withProperty(PROPKEY_VEHICLE_MANUFACTURER, "some-manufacturer")
        .withProperty(PROPKEY_VEHICLE_SERIAL_NUMBER, "1234")
        .withProperty(PROPKEY_VEHICLE_TOPIC_CONNECTION_NAME, "noitcennoc")
        .withProperty(PROPKEY_VEHICLE_TOPIC_STATE_NAME, "etats")
        .withProperty(PROPKEY_VEHICLE_TOPIC_VISU_NAME, "noitazilaisuv")
        .withProperty(PROPKEY_VEHICLE_TOPIC_ORDER_NAME, "redro")
        .withProperty(PROPKEY_VEHICLE_TOPIC_INSTANT_ACTIONS_NAME, "snoitcAtnatsni");

    assertWith(
        MqttSetting.forVehicle(vehicle),
        optMqttSetting -> {
          assertThat(optMqttSetting).isPresent();

          MqttSetting mqttSetting = optMqttSetting.get();
          assertThat(mqttSetting.connectionTopicName()).isEqualTo("some-prefix/noitcennoc");
          assertThat(mqttSetting.stateTopicName()).isEqualTo("some-prefix/etats");
          assertThat(mqttSetting.visualizationTopicName()).isEqualTo("some-prefix/noitazilaisuv");
          assertThat(mqttSetting.orderTopicName()).isEqualTo("some-prefix/redro");
          assertThat(mqttSetting.instantActionsTopicName()).isEqualTo("some-prefix/snoitcAtnatsni");
        }
    );
  }

  @Test
  void parseQualityOfService() {
    Vehicle vehicle = new Vehicle("some-vehicle")
        .withProperty(PROPKEY_VEHICLE_TOPIC_PREFIX, "some-prefix")
        .withProperty(PROPKEY_VEHICLE_MANUFACTURER, "some-manufacturer")
        .withProperty(PROPKEY_VEHICLE_SERIAL_NUMBER, "1234");

    assertThat(
        MqttSetting.forVehicle(
            vehicle.withProperty(PROPKEY_VEHICLE_TOPIC_CONNECTION_QOS, "at_most_once")
        )
    )
        .isPresent()
        .get()
        .extracting(MqttSetting::connectionTopicQos)
        .isEqualTo(QualityOfService.AT_MOST_ONCE);

    assertThat(
        MqttSetting.forVehicle(
            vehicle.withProperty(PROPKEY_VEHICLE_TOPIC_CONNECTION_QOS, "0")
        )
    )
        .isPresent()
        .get()
        .extracting(MqttSetting::connectionTopicQos)
        .isEqualTo(QualityOfService.AT_MOST_ONCE);

    assertThat(
        MqttSetting.forVehicle(
            vehicle.withProperty(PROPKEY_VEHICLE_TOPIC_CONNECTION_QOS, "at_least_once")
        )
    )
        .isPresent()
        .get()
        .extracting(MqttSetting::connectionTopicQos)
        .isEqualTo(QualityOfService.AT_LEAST_ONCE);

    assertThat(
        MqttSetting.forVehicle(
            vehicle.withProperty(PROPKEY_VEHICLE_TOPIC_CONNECTION_QOS, "1")
        )
    )
        .isPresent()
        .get()
        .extracting(MqttSetting::connectionTopicQos)
        .isEqualTo(QualityOfService.AT_LEAST_ONCE);

    assertThat(
        MqttSetting.forVehicle(
            vehicle.withProperty(PROPKEY_VEHICLE_TOPIC_CONNECTION_QOS, "exactly_once")
        )
    )
        .isPresent()
        .get()
        .extracting(MqttSetting::connectionTopicQos)
        .isEqualTo(QualityOfService.EXACTLY_ONCE);

    assertThat(
        MqttSetting.forVehicle(
            vehicle.withProperty(PROPKEY_VEHICLE_TOPIC_CONNECTION_QOS, "2")
        )
    )
        .isPresent()
        .get()
        .extracting(MqttSetting::connectionTopicQos)
        .isEqualTo(QualityOfService.EXACTLY_ONCE);
  }
}
