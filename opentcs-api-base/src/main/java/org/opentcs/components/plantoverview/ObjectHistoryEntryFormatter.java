// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.components.plantoverview;

import java.util.Optional;
import java.util.function.Function;
import org.opentcs.data.ObjectHistory;

/**
 * A formatter for {@link ObjectHistory} entries, mapping an entry to a user-presentable string, if
 * possible.
 * Indicates that it cannot map an entry by returning an empty {@code Optional}.
 */
public interface ObjectHistoryEntryFormatter
    extends
      Function<ObjectHistory.Entry, Optional<String>> {

}
