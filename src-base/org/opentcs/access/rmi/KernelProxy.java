/*
 * openTCS copyright information:
 * Copyright (c) 2010 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.rmi;

import org.opentcs.access.Kernel;

/**
 * A proxy for a <code>Kernel</code> instance that allows to login/logout
 * cleanly and repeatedly without the need to create new proxy instances.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface KernelProxy
extends Kernel, RemoteKernelConnection {

}
