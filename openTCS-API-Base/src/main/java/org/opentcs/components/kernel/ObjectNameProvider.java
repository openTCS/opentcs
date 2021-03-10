/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.kernel;

import java.util.function.Function;
import org.opentcs.access.to.CreationTO;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface ObjectNameProvider
    extends Function<CreationTO, String> {

}
