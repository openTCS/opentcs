/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers;

import java.io.Serializable;

/**
 * Marks low-level communication events.
 * Can be used e.g. to distinguish low-level events that are meant for component-internal processing
 * and that are irrelevant for high-level UI visualization.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface LowLevelCommunicationEvent
    extends Serializable {

}
