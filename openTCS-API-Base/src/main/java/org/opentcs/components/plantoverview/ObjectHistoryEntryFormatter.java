/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.plantoverview;

import java.util.Optional;
import java.util.function.Function;
import org.opentcs.data.ObjectHistory;

/**
 * A formatter for {@link ObjectHistory} entries, mapping an entry to a user-presentable string, if
 * possible.
 * Indicates that it cannot map an entry by returning an empty {@code Optional}.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface ObjectHistoryEntryFormatter
    extends Function<ObjectHistory.Entry, Optional<String>> {

}
