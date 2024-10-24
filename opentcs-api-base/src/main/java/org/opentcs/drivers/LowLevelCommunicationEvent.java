// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.drivers;

import java.io.Serializable;

/**
 * Marks low-level communication events.
 * Can be used e.g. to distinguish low-level events that are meant for component-internal processing
 * and that are irrelevant for high-level UI visualization.
 */
public interface LowLevelCommunicationEvent
    extends
      Serializable {

}
