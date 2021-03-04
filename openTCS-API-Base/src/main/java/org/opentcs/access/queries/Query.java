/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.queries;

import java.io.Serializable;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * The base class of all results to Kernel queries.
 * <p>
 * Note that this class's {@link Availability} annotation indicates it is
 * available in <em>no kernel state at all</em>, and since that annotation is
 * inherited, subclasses are required to explicitly define their availability
 * themselves to be usable at runtime.
 * </p>
 *
 * @param <E> The actual query class.
 * @see org.opentcs.access.Kernel#query(java.lang.Class)
 * @author Stefan Walter (Fraunhofer IML)
 * @deprecated Instead of queries, explicit service calls should be used.
 */
@Deprecated
@ScheduledApiChange(when = "5.0")
@Availability({})
public abstract class Query<E extends Query<E>>
    implements Serializable {
}
