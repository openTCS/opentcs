/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.transport;

import static java.util.Objects.requireNonNull;
import java.util.Optional;
import org.opentcs.components.plantoverview.ObjectHistoryEntryFormatter;
import org.opentcs.data.ObjectHistory;
import org.opentcs.data.order.TransportOrderHistoryCodes;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * A formatter for standard object history events/entries.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class StandardObjectHistoryEntryFormatter
    implements ObjectHistoryEntryFormatter {

  /**
   * A bundle providing localized strings.
   */
  private final ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();

  /**
   * Creates a new instance.
   */
  public StandardObjectHistoryEntryFormatter() {
  }

  @Override
  public Optional<String> apply(ObjectHistory.Entry entry) {
    requireNonNull(entry, "entry");

    switch (entry.getEventCode()) {
      case TransportOrderHistoryCodes.ORDER_CREATED:
        return Optional.of(bundle.getString("historyEntry.text.orderCreated"));

      case TransportOrderHistoryCodes.ORDER_PROCESSING_VEHICLE_CHANGED:
        return Optional.of(
            bundle.getString("historyEntry.text.orderProcVehicleChanged")
            + " '" + entry.getSupplement().toString() + "'"
        );

      case TransportOrderHistoryCodes.ORDER_DRIVE_ORDER_FINISHED:
        return Optional.of(bundle.getString("historyEntry.text.driveOrderFinished"));

      case TransportOrderHistoryCodes.ORDER_REACHED_FINAL_STATE:
        return Optional.of(bundle.getString("historyEntry.text.orderReachedFinalState"));

      default:
        return Optional.empty();
    }
  }

}
