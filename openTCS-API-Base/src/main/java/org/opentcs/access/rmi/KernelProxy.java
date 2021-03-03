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
 * A proxy for a {@link Kernel} instance that hides the details of communication with a remote
 * kernel, so a client can call methods of the proxy as if it was a reference to a kernel instance
 * within the same JVM.
 * Allows to login/logout cleanly and repeatedly.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @see KernelProxyBuilder
 */
public interface KernelProxy
    extends Kernel,
            RemoteKernelConnection {

}
