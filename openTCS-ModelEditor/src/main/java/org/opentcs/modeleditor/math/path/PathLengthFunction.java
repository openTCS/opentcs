/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.math.path;

import java.util.function.ToDoubleFunction;
import org.opentcs.guing.base.model.elements.PathModel;

/**
 * A function that computes the length of a path.
 */
public interface PathLengthFunction
    extends ToDoubleFunction<PathModel> {
}
