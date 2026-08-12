// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.common.mqtt;

/**
 * An MQTT QoS value.
 */
public enum QualityOfService {
  /**
   * Deliver a message at most once (= QoS value of 0).
   */
  AT_MOST_ONCE(0),
  /**
   * Deliver a message at least once (= QoS value of 1).
   */
  AT_LEAST_ONCE(1),
  /**
   * Deliver a message exactly once (= QoS value of 2).
   */
  EXACTLY_ONCE(2);

  /**
   * The QoS value as specified by MQTT.
   */
  private final int qosValue;

  /**
   * Creates a new instance.
   *
   * @param qosValue The QoS value.
   */
  QualityOfService(int qosValue) {
    this.qosValue = qosValue;
  }

  /**
   * Returns the QoS value as specified by MQTT.
   *
   * @return The QoS value as specified by MQTT.
   */
  public int getQosValue() {
    return qosValue;
  }
}
