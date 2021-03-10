/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.transport;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import org.opentcs.components.plantoverview.ObjectHistoryEntryFormatter;
import org.opentcs.data.ObjectHistory;

/**
 * A composite formatter for history entries that first tries to apply all registered formatters,
 * then {@link StandardObjectHistoryEntryFormatter}, then a fallback function to ensure that the
 * returned value is <em>never empty</em>.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class CompositeObjectHistoryEntryFormatter
    implements ObjectHistoryEntryFormatter {

  /**
   * The actual formatters.
   */
  private final List<ObjectHistoryEntryFormatter> formatters = new ArrayList<>();

  /**
   * Creates a new instance.
   *
   * @param customFormatters The set of custom formatters.
   */
  @Inject
  public CompositeObjectHistoryEntryFormatter(Set<ObjectHistoryEntryFormatter> customFormatters) {
    for (ObjectHistoryEntryFormatter formatter : customFormatters) {
      formatters.add(formatter);
    }

    formatters.add(new StandardObjectHistoryEntryFormatter());
    formatters.add(this::fallbackFormat);
  }

  @Override
  public Optional<String> apply(ObjectHistory.Entry entry) {
    return formatters.stream()
        .map(formatter -> formatter.apply(entry))
        .filter(result -> result.isPresent())
        .map(result -> result.get())
        .findFirst();
  }

  private Optional<String> fallbackFormat(ObjectHistory.Entry entry) {
    return Optional.of(
        "eventCode: '" + entry.getEventCode()
        + "', supplement: '" + entry.getSupplement().toString() + '\''
    );
  }

}
