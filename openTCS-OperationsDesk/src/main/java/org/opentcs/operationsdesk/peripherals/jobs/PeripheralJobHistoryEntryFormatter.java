/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.peripherals.jobs;

import static java.util.Objects.requireNonNull;
import java.util.Optional;
import org.opentcs.components.plantoverview.ObjectHistoryEntryFormatter;
import org.opentcs.data.ObjectHistory;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.data.peripherals.PeripheralJobHistoryCodes;
import org.opentcs.operationsdesk.util.I18nPlantOverviewOperating;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;

/**
 * A formatter for history events/entries related to {@link PeripheralJob}s.
 */
public class PeripheralJobHistoryEntryFormatter
    implements ObjectHistoryEntryFormatter {

  /**
   * A bundle providing localized strings.
   */
  private final ResourceBundleUtil bundle
      = ResourceBundleUtil.getBundle(I18nPlantOverviewOperating.PJDETAIL_PATH);

  /**
   * Creates a new instance.
   */
  public PeripheralJobHistoryEntryFormatter() {
  }

  @Override
  public Optional<String> apply(ObjectHistory.Entry entry) {
    requireNonNull(entry, "entry");

    switch (entry.getEventCode()) {
      case PeripheralJobHistoryCodes.JOB_CREATED:
        return Optional.of(
            bundle.getString("peripheralJobHistoryEntryFormatter.code_jobCreated.text")
        );
      case PeripheralJobHistoryCodes.JOB_REACHED_FINAL_STATE:
        return Optional.of(
            bundle.getString("peripheralJobHistoryEntryFormatter.code_jobReachedFinalState.text")
        );

      default:
        return Optional.empty();
    }
  }

}
