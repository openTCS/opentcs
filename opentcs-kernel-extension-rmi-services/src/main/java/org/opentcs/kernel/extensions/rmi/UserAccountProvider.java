// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.rmi;

import java.util.Set;

/**
 * Provides user account data.
 */
public interface UserAccountProvider {

  Set<UserAccount> getUserAccounts();
}
