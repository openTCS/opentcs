/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.rmi;

import java.util.Set;

/**
 * Provides user account data.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface UserAccountProvider {

  Set<UserAccount> getUserAccounts();
}
