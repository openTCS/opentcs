// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.modeleditor.math.path;

import java.util.function.ToDoubleFunction;
import org.opentcs.guing.base.model.elements.PathModel;

/**
 * A function that computes the length of a path.
 */
public interface PathLengthFunction
    extends
      ToDoubleFunction<PathModel> {
}
