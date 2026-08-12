// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.Header;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.connection.Connection;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.connection.ConnectionState;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.BatteryState;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.EStop;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.OperatingMode;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.SafetyState;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.State;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.visualization.Visualization;

/**
 * Unit tests for {@link IncomingMessageFilter}.
 */
public class IncomingMessageFilterTest {

  private IncomingMessageFilter incomingMessageFilter;
  private Instant baseTimestamp = Instant.parse("2024-01-01T00:00:00Z");

  @BeforeEach
  public void setUp() {
    incomingMessageFilter = new IncomingMessageFilter();
  }

  @Test
  public void acceptMessagesWithHeaderIdsInSequence() {
    incomingMessageFilter.accept(
        createEmptyConnection()
            .setConnectionState(ConnectionState.ONLINE)
            .setHeaderId(0L)
            .setTimestamp(baseTimestamp)
    );

    assertThat(
        incomingMessageFilter.accept(
            createEmptyState()
                .setHeaderId(0L)
                .setTimestamp(baseTimestamp)
        ),
        is(true)
    );
    assertThat(
        incomingMessageFilter.accept(
            createEmptyState()
                .setHeaderId(1L)
                .setTimestamp(baseTimestamp)
        ),
        is(true)
    );
    assertThat(
        incomingMessageFilter.accept(
            createEmptyState()
                .setHeaderId(2L)
                .setTimestamp(baseTimestamp)
        ),
        is(true)
    );

    assertThat(
        incomingMessageFilter.accept(
            createEmptyVisualization()
                .setHeaderId(0L)
                .setTimestamp(baseTimestamp)
        ),
        is(true)
    );
    assertThat(
        incomingMessageFilter.accept(
            createEmptyVisualization()
                .setHeaderId(1L)
                .setTimestamp(baseTimestamp)
        ),
        is(true)
    );
    assertThat(
        incomingMessageFilter.accept(
            createEmptyVisualization()
                .setHeaderId(2L)
                .setTimestamp(baseTimestamp)
        ),
        is(true)
    );
  }

  @Test
  public void acceptMessagesWithHeaderIdsOutOfSequenceButTimestampsInSequence() {
    incomingMessageFilter.accept(
        createEmptyConnection()
            .setConnectionState(ConnectionState.ONLINE)
            .setHeaderId(0L)
            .setTimestamp(baseTimestamp)
    );

    assertThat(
        incomingMessageFilter.accept(
            createEmptyState()
                .setHeaderId(2L)
                .setTimestamp(baseTimestamp)
        ),
        is(true)
    );
    assertThat(
        incomingMessageFilter.accept(
            createEmptyState()
                .setHeaderId(1L)
                .setTimestamp(baseTimestamp.plusMillis(10))
        ),
        is(true)
    );
    assertThat(
        incomingMessageFilter.accept(
            createEmptyState()
                .setHeaderId(0L)
                .setTimestamp(baseTimestamp.plusMillis(20))
        ),
        is(true)
    );

    assertThat(
        incomingMessageFilter.accept(
            createEmptyVisualization()
                .setHeaderId(2L)
                .setTimestamp(baseTimestamp)
        ),
        is(true)
    );
    assertThat(
        incomingMessageFilter.accept(
            createEmptyVisualization()
                .setHeaderId(1L)
                .setTimestamp(baseTimestamp.plusMillis(10))
        ),
        is(true)
    );
    assertThat(
        incomingMessageFilter.accept(
            createEmptyVisualization()
                .setHeaderId(0L)
                .setTimestamp(baseTimestamp.plusMillis(20))
        ),
        is(true)
    );
  }

  @Test
  public void ignoreMessagesWhileInitiallyOffline() {
    assertThat(
        incomingMessageFilter.accept(
            createEmptyState()
                .setHeaderId(0L)
                .setTimestamp(baseTimestamp)
        ),
        is(false)
    );
    assertThat(
        incomingMessageFilter.accept(
            createEmptyVisualization()
                .setHeaderId(0L)
                .setTimestamp(baseTimestamp)
        ),
        is(false)
    );
  }

  @Test
  public void ignoreMessagesWhenExplicitlyReportedOffline() {
    incomingMessageFilter.accept(
        createEmptyConnection()
            .setConnectionState(ConnectionState.ONLINE)
            .setHeaderId(0L)
            .setTimestamp(baseTimestamp)
    );
    incomingMessageFilter.accept(
        createEmptyConnection()
            .setConnectionState(ConnectionState.OFFLINE)
            .setHeaderId(1L)
            .setTimestamp(baseTimestamp.plusMillis(10))
    );

    assertThat(
        incomingMessageFilter.accept(
            createEmptyState()
                .setHeaderId(0L)
                .setTimestamp(baseTimestamp)
        ),
        is(false)
    );
    assertThat(
        incomingMessageFilter.accept(
            createEmptyVisualization()
                .setHeaderId(0L)
                .setTimestamp(baseTimestamp)
        ),
        is(false)
    );
  }

  @Test
  public void ignoreOutdatedMessages() {
    incomingMessageFilter.accept(
        createEmptyConnection()
            .setConnectionState(ConnectionState.ONLINE)
            .setHeaderId(0L)
            .setTimestamp(baseTimestamp)
    );

    incomingMessageFilter.accept(
        createEmptyState()
            .setHeaderId(1L)
            .setTimestamp(baseTimestamp)
    );

    assertThat(
        incomingMessageFilter.accept(
            createEmptyState()
                .setHeaderId(0L)
                .setTimestamp(baseTimestamp.minusMillis(10))
        ),
        is(false)
    );

    incomingMessageFilter.accept(
        createEmptyVisualization()
            .setHeaderId(1L)
            .setTimestamp(baseTimestamp)
    );

    assertThat(
        incomingMessageFilter.accept(
            createEmptyVisualization()
                .setHeaderId(0L)
                .setTimestamp(baseTimestamp.minusMillis(10))
        ),
        is(false)
    );
  }

  @Test
  public void acceptOutdatedMessagesAfterReconnectToBroker() {
    incomingMessageFilter.accept(
        createEmptyConnection()
            .setConnectionState(ConnectionState.ONLINE)
            .setHeaderId(0L)
            .setTimestamp(baseTimestamp)
    );

    incomingMessageFilter.reset();

    assertThat(
        incomingMessageFilter.accept(
            createEmptyConnection()
                .setConnectionState(ConnectionState.ONLINE)
                .setHeaderId(0L)
                .setTimestamp(baseTimestamp.minusMillis(10))
        ),
        is(true)
    );

    assertThat(
        incomingMessageFilter.accept(
            createEmptyState()
                .setHeaderId(0L)
                .setTimestamp(baseTimestamp.minusMillis(10))
        ),
        is(true)
    );
  }

  @Test
  public void ignoreMessagesOfUnhandledType() {
    assertThat(
        incomingMessageFilter.accept(new Header() {}),
        is(false)
    );
  }

  private Connection createEmptyConnection() {
    return new Connection(
        0L,
        Instant.now(),
        "1.1.0",
        "some-manufacturer",
        "some-serial-number",
        ConnectionState.CONNECTIONBROKEN
    );
  }

  private State createEmptyState() {
    return new State(
        "some-order-id",
        0L,
        "last-node-id",
        0L,
        List.of(),
        List.of(),
        false,
        List.of(),
        new BatteryState(100.0, false),
        OperatingMode.AUTOMATIC,
        List.of(),
        new SafetyState(EStop.NONE, false)
    );
  }

  private Visualization createEmptyVisualization() {
    return new Visualization();
  }

}
