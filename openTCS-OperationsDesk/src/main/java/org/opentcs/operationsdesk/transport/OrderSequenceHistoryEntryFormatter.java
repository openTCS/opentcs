/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.transport;

import static java.util.Objects.requireNonNull;
import java.util.Optional;
import org.opentcs.components.plantoverview.ObjectHistoryEntryFormatter;
import org.opentcs.data.ObjectHistory;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.OrderSequenceHistoryCodes;
import org.opentcs.operationsdesk.util.I18nPlantOverviewOperating;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;

/**
 * A formatter for history events/entries related to {@link OrderSequence}s.
 */
public class OrderSequenceHistoryEntryFormatter
    implements ObjectHistoryEntryFormatter {

  /**
   * A bundle providing localized strings.
   */
  private final ResourceBundleUtil bundle
      = ResourceBundleUtil.getBundle(I18nPlantOverviewOperating.OSDETAIL_PATH);

  /**
   * Creates a new instance.
   */
  public OrderSequenceHistoryEntryFormatter() {
  }

  @Override
  public Optional<String> apply(ObjectHistory.Entry entry) {
    requireNonNull(entry, "entry");

    switch (entry.getEventCode()) {
      case OrderSequenceHistoryCodes.SEQUENCE_CREATED:
        return Optional.of(
            bundle.getString("orderSequenceHistoryEntryFormatter.code_sequenceCreated.text")
        );

      case OrderSequenceHistoryCodes.SEQUENCE_ORDER_APPENDED:
        return Optional.of(
            bundle.getString(
                "orderSequenceHistoryEntryFormatter.code_sequenceOrderAppended.text"
            )
            + " '" + entry.getSupplement().toString() + "'"
        );

      case OrderSequenceHistoryCodes.SEQUENCE_PROCESSING_VEHICLE_CHANGED:
        return Optional.of(
            bundle.getString(
                "orderSequenceHistoryEntryFormatter.code_sequenceProcVehicleChanged.text"
            )
            + " '" + entry.getSupplement().toString() + "'"
        );

      case OrderSequenceHistoryCodes.SEQUENCE_COMPLETED:
        return Optional.of(
            bundle.getString("orderSequenceHistoryEntryFormatter.code_sequenceCompleted.text")
        );

      case OrderSequenceHistoryCodes.SEQUENCE_FINISHED:
        return Optional.of(
            bundle.getString(
                "orderSequenceHistoryEntryFormatter.code_sequenceFinished.text"
            )
        );

      default:
        return Optional.empty();
    }
  }

}
