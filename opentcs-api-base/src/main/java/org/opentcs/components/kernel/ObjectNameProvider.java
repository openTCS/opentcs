// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.components.kernel;

import java.util.function.Function;
import org.opentcs.access.to.CreationTO;

/**
 * Provides names for {@link CreationTO}s.
 */
public interface ObjectNameProvider
    extends
      Function<CreationTO, String> {

}
