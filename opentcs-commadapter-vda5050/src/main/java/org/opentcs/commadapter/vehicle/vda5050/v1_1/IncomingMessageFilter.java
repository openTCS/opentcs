// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1;

import static java.util.Objects.requireNonNull;

import java.time.Instant;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.Header;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.connection.Connection;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state.State;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.visualization.Visualization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks whether incoming messages should be accepted or ignored.
 */
public class IncomingMessageFilter {
  private static final Logger LOG = LoggerFactory.getLogger(IncomingMessageFilter.class);

  private final HeaderRecord headerRecordConnection = new HeaderRecord();
  private final HeaderRecord headerRecordState = new HeaderRecord();
  private final HeaderRecord headerRecordVisualization = new HeaderRecord();
  private boolean online;

  /**
   * Creates a new instance.
   */
  public IncomingMessageFilter() {
  }

  /**
   * Checks whether the given message should be accepted, based on what other messages were accepted
   * previously.
   *
   * @param header The message to be checked.
   * @return {@code true} if the message should be accepted.
   */
  public boolean accept(Header header) {
    requireNonNull(header, "header");

    if (header instanceof Visualization visualization) {
      return processVisualization(visualization);
    }
    else if (header instanceof State state) {
      return processState(state);
    }
    else if (header instanceof Connection connection) {
      return processConnection(connection);
    }
    else {
      LOG.warn("Not accepting message of unhandled type: {}", header);
      return false;
    }
  }

  /**
   * Resets the filter to its initial state.
   */
  public void reset() {
    headerRecordConnection.lastSeenTimestamp = Instant.EPOCH;
    headerRecordState.lastSeenTimestamp = Instant.EPOCH;
    headerRecordVisualization.lastSeenTimestamp = Instant.EPOCH;

    online = false;
  }

  private boolean processConnection(Connection message) {
    // Note that the following scenario is possible with VDA5050:
    // 1. The driver's connection to the MQTT broker is lost.
    // 2. An OFFLINE/CONNECTIONBROKEN message is published by the vehicle/broker.
    // 3. The vehicle resets its timestamps and header IDs, e.g. as part of a reboot.
    // 4. An ONLINE message is published by the vehicle.
    // 5. The driver's connection to the MQTT broker is restored.
    // In this situation, only the ONLINE message would be delivered to us, containing the new,
    // "older" timestamps and header IDs. For this reason, we accept ANY message with a valid
    // connection state here, and reset our header records for EVERY topic, too. Not doing so would
    // mean not accepting any further messages from the vehicle until its timestamps/header IDs have
    // caught up with the previously-reached values.
    switch (message.getConnectionState()) {
      case CONNECTIONBROKEN:
        reset();

        return true;
      case OFFLINE:
        if (message.getHeaderId() <= headerRecordConnection.lastSeenHeaderId
            && !message.getTimestamp().isAfter(headerRecordConnection.lastSeenTimestamp)) {
          LOG.info("Connection message header outdated, accepting anyway: {}", message);
        }

        reset();

        return true;
      case ONLINE:
        if (message.getHeaderId() <= headerRecordConnection.lastSeenHeaderId
            && !message.getTimestamp().isAfter(headerRecordConnection.lastSeenTimestamp)) {
          LOG.info("Connection message header outdated, accepting anyway: {}", message);
        }

        reset();
        online = true;
        headerRecordConnection.lastSeenHeaderId = message.getHeaderId();
        headerRecordConnection.lastSeenTimestamp = message.getTimestamp();

        return true;
      default:
        LOG.warn("Unhandled connection state: {}", message.getConnectionState());
        return false;
    }
  }

  private boolean processState(State message) {
    if (!online) {
      LOG.info("Not accepting message while vehicle is considered offline: {}", message);
      return false;
    }

    if (message.getHeaderId() <= headerRecordState.lastSeenHeaderId
        && !message.getTimestamp().isAfter(headerRecordState.lastSeenTimestamp)) {
      LOG.info("Not accepting message with outdated header: {}", message);
      return false;
    }

    headerRecordState.lastSeenHeaderId = message.getHeaderId();
    headerRecordState.lastSeenTimestamp = message.getTimestamp();

    return true;
  }

  private boolean processVisualization(Visualization message) {
    if (!online) {
      LOG.info("Not accepting message while vehicle is considered offline: {}", message);
      return false;
    }

    if (message.getHeaderId() <= headerRecordVisualization.lastSeenHeaderId
        && !message.getTimestamp().isAfter(headerRecordVisualization.lastSeenTimestamp)) {
      LOG.info("Not accepting message with outdated header: {}", message);
      return false;
    }

    headerRecordVisualization.lastSeenHeaderId = message.getHeaderId();
    headerRecordVisualization.lastSeenTimestamp = message.getTimestamp();

    return true;
  }

  private static class HeaderRecord {
    private long lastSeenHeaderId;
    private Instant lastSeenTimestamp = Instant.EPOCH;

    private HeaderRecord() {
    }
  }
}
