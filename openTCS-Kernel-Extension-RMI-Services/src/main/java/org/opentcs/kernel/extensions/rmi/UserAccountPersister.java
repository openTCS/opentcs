/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.rmi;

import java.io.IOException;
import java.util.Set;

/**
 * Provides methods to persist and load user account data.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface UserAccountPersister {
  /**
   * Returns a set of user accounts from persistent storage.
   *
   * @return A set of user accounts from persistent storage. If no user accounts
   * are available, the returned set will be empty.
   * @throws IOException If loading the account data was not possible for some
   * reason.
   */
  Set<UserAccount> loadUserAccounts()
  throws IOException;
  
  /**
   * Persists a set of user accounts.
   *
   * @param accounts The set of user accounts to be persisted.
   * @throws IOException If persisting the account data was not possible for
   * some reason.
   */
  void saveUserAccounts(Set<UserAccount> accounts)
  throws IOException;
}
