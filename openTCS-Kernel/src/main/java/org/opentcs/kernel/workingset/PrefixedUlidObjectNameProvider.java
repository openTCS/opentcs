/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.workingset;

import de.huxhorn.sulky.ulid.ULID;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import org.opentcs.access.to.CreationTO;
import org.opentcs.components.kernel.ObjectNameProvider;

/**
 * Provides names for objects based on ULIDs, prefixed with the name taken from a given
 * {@link CreationTO}.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class PrefixedUlidObjectNameProvider
    implements ObjectNameProvider {

  /**
   * Generates ULIDs for us.
   */
  private final ULID ulid = new ULID();
  /**
   * The previously generated ULID value.
   */
  private ULID.Value previousUlid = ulid.nextValue();

  /**
   * Creates a new instance.
   */
  public PrefixedUlidObjectNameProvider() {
  }

  @Override
  public String apply(CreationTO to) {
    requireNonNull(to, "to");

    Optional<ULID.Value> newValue = ulid.nextStrictlyMonotonicValue(previousUlid);
    while (newValue.isEmpty()) {
      newValue = ulid.nextStrictlyMonotonicValue(previousUlid);
    }
    previousUlid = newValue.get();
    return to.getName() + newValue.get().toString();
  }
}
