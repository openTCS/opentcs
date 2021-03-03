/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.queries;

import java.io.Serializable;

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
 */
@Availability({})
public abstract class Query<E extends Query<E>>
    implements Serializable {
}
