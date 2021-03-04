/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.rmi;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.rmi.ClientID;
import org.opentcs.access.rmi.services.RemoteKernelServicePortal;
import org.opentcs.components.Lifecycle;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.customizations.ApplicationHome;
import org.opentcs.customizations.kernel.KernelExecutor;
import static org.opentcs.util.Assertions.checkArgument;
import static org.opentcs.util.Assertions.checkInRange;
import org.opentcs.util.event.EventHandler;
import org.opentcs.util.event.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages users allowed to connect/operate with the kernel and authenticated clients.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class UserManager
    implements EventHandler,
               Lifecycle {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(UserManager.class);
  /**
   * Where we register for application events.
   */
  private final EventSource eventSource;
  /**
   * The kernel's executor.
   */
  private final ScheduledExecutorService kernelExecutor;
  /**
   * Provides configuration data.
   */
  private final RmiKernelInterfaceConfiguration configuration;
  /**
   * The persister loading and storing account data.
   */
  private final UserAccountPersister accountPersister;
  /**
   * The directory of users allowed to connect/operate with the kernel.
   */
  private final Map<String, UserAccount> knownUsers = new HashMap<>();
  /**
   * The directory of authenticated clients (a mapping of ClientIDs to user names).
   */
  private final Map<ClientID, ClientEntry> knownClients = new HashMap<>();
  /**
   * A handle for the task that periodically cleans up known clients and event buffers.
   */
  private ScheduledFuture<?> cleanerTaskFuture;
  /**
   * Whether this kernel extension is initialized or not.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param homeDirectory The kernel's home directory (for saving user account data). Will be
   * created if it doesn't exist, yet.
   * @param eventSource Where this instance registers for application events.
   * @param kernelExecutor The kernel's executor.
   * @param configuration This class' configuration.
   */
  @Inject
  public UserManager(@ApplicationHome File homeDirectory,
                     @ApplicationEventBus EventSource eventSource,
                     @KernelExecutor ScheduledExecutorService kernelExecutor,
                     RmiKernelInterfaceConfiguration configuration) {
    requireNonNull(homeDirectory, "homeDirectory");
    this.eventSource = requireNonNull(eventSource, "eventSource");
    this.kernelExecutor = requireNonNull(kernelExecutor, "kernelExecutor");
    this.configuration = requireNonNull(configuration, "configuration");
    this.accountPersister = new XMLFileUserAccountPersister(new File(homeDirectory, "data"));
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      LOG.debug("Already initialized.");
      return;
    }

    // Register the user manager as an event listener so that the user manager can collect events 
    // and pass them to known clients polling events.
    eventSource.subscribe(this);

    try {
      knownUsers.clear();

      Set<UserAccount> accounts = accountPersister.loadUserAccounts();
      if (accounts.isEmpty()) {
        accounts.add(new UserAccount(RemoteKernelServicePortal.GUEST_USER,
                                     RemoteKernelServicePortal.GUEST_PASSWORD,
                                     EnumSet.allOf(UserPermission.class)));
        accountPersister.saveUserAccounts(accounts);

      }
      for (UserAccount curAccount : accounts) {
        knownUsers.put(curAccount.getUserName(), curAccount);
      }
    }
    catch (IOException exc) {
      throw new IllegalStateException("IOException initializing user account data", exc);
    }

    // Start the thread that periodically cleans up the list of known clients and event buffers.
    LOG.debug("Starting cleaner task...");
    cleanerTaskFuture = kernelExecutor.scheduleWithFixedDelay(new ClientCleanerTask(),
                                                              configuration.clientSweepInterval(),
                                                              configuration.clientSweepInterval(),
                                                              TimeUnit.MILLISECONDS);

    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      LOG.debug("Not initialized.");
      return;
    }

    LOG.debug("Terminating cleaner task...");
    cleanerTaskFuture.cancel(false);
    cleanerTaskFuture = null;

    knownUsers.clear();

    eventSource.unsubscribe(this);

    initialized = false;
  }

  @Override
  public void onEvent(Object event) {
    // Forward the event to all clients' event buffers.
    synchronized (getKnownClients()) {
      for (ClientEntry curEntry : getKnownClients().values()) {
        curEntry.getEventBuffer().onEvent(event);
      }
    }
  }

  /**
   * Returns the directory of users allowed to connect/operate with the kernel.
   *
   * @return The directory of users allowed to connect/operate with the kernel.
   */
  public Map<String, UserAccount> getKnownUsers() {
    return knownUsers;
  }

  /**
   * Returns the directory of authenticated clients (a mapping of ClientIDs to user names).
   *
   * @return The directory of authenticated clients (a mapping of ClientIDs to user names).
   */
  public Map<ClientID, ClientEntry> getKnownClients() {
    return knownClients;
  }

  /**
   * Returns the {@link UserAccount} for the given user name.
   *
   * @param userName The user name to get the user account for.
   * @return The user account or {@code null}, if there isn't an account associated to the given
   * user name.
   */
  @Nullable
  public UserAccount getUser(String userName) {
    return knownUsers.get(userName);
  }

  /**
   * Returns the {@link ClientEntry} for the given client id.
   *
   * @param clientID The client id to get the client entry for.
   * @return The client entry or {@code null}, if there isn't an entry associated to the given
   * client id.
   */
  @Nullable
  public ClientEntry getClient(ClientID clientID) {
    return knownClients.get(clientID);
  }

  public List<Object> pollEvents(ClientID clientID, long timeout) {
    requireNonNull(clientID, "clientID");
    checkInRange(timeout, 0, Long.MAX_VALUE, "timeout");

    ClientEntry clientEntry;
    EventBuffer eventBuffer;
    synchronized (getKnownClients()) {
      clientEntry = getClient(clientID);
      checkArgument(clientEntry != null, "Unknown client ID: %s", clientID);
      eventBuffer = clientEntry.getEventBuffer();
    }
    // Get events or wait for one to arrive if none is currently there.
    List<Object> events = eventBuffer.getEvents(timeout);
    // Set the client's 'alive' flag.
    synchronized (getKnownClients()) {
      clientEntry.setAlive(true);
    }
    return events;
  }

  /**
   * Check whether the user described by the given credentials is granted permissions according to
   * the specified user role.
   * <p>
   * This method also sets the 'alive' flag of the client's entry to prevent it from being removed
   * by the cleaner thread.
   * </p>
   *
   * @param clientID The client's identification object.
   * @param requiredPermission The required role/permission.
   * @return <code>true</code> if, and only if, the given client ID exists and the client has the
   * given permission.
   */
  private boolean checkCredentialsForRole(ClientID clientID, UserPermission requiredPermission) {
    requireNonNull(clientID, "clientID");
    requireNonNull(requiredPermission, "requiredPermission");

    synchronized (getKnownClients()) {
      ClientEntry clientEntry = getClient(clientID);
      // Check if the client is known.
      if (clientEntry == null) {
        return false;
      }
      // Set the 'alive' flag for the cleaning thread.
      clientEntry.setAlive(true);
      // Check if the user's permissions are sufficient.
      Set<UserPermission> providedPerms = clientEntry.getPermissions();
      if (!providedPerms.contains(requiredPermission)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Ensures the given client has the required permissions.
   *
   * @param clientID The client's identification object.
   * @param requiredPermission The required role/permission.
   * @throws CredentialsException If the client's permissions are insufficient.
   */
  public void verifyCredentials(ClientID clientID, UserPermission requiredPermission)
      throws CredentialsException {
    requireNonNull(clientID, "clientID");
    requireNonNull(requiredPermission, "requiredPermission");

    if (!checkCredentialsForRole(clientID, requiredPermission)) {
      throw new CredentialsException("Client permissions insufficient.");
    }
  }

  /**
   * Instances of this class are used as containers for data kept about known clients.
   */
  public static final class ClientEntry {

    /**
     * The name of the user that connected with the client.
     */
    private final String userName;
    /**
     * The client's permissions/privilege level.
     */
    private final Set<UserPermission> permissions;
    /**
     * The client's event buffer.
     */
    private final EventBuffer eventBuffer = new EventBuffer(event -> false);
    /**
     * The client's alive flag.
     */
    private boolean alive = true;

    /**
     * Creates a new ClientEntry.
     *
     * @param name The client's name.
     * @param perms The client's permissions.
     */
    public ClientEntry(String name, Set<UserPermission> perms) {
      userName = requireNonNull(name, "name");
      permissions = requireNonNull(perms, "perms");
    }

    /**
     * Checks whether the client has been seen since the last sweep of the cleaner task.
     *
     * @return <code>true</code> if, and only if, the client has been seen recently.
     */
    public boolean isAlive() {
      return alive;
    }

    /**
     * Sets this client's <em>alive</em> flag.
     *
     * @param isAlive The client's new <em>alive</em> flag.
     */
    public void setAlive(boolean isAlive) {
      alive = isAlive;
    }

    public String getUserName() {
      return userName;
    }

    public EventBuffer getEventBuffer() {
      return eventBuffer;
    }

    public Set<UserPermission> getPermissions() {
      return permissions;
    }
  }

  /**
   * A task for cleaning out stale client entries.
   */
  private class ClientCleanerTask
      implements Runnable {

    /**
     * Creates a new instance.
     */
    private ClientCleanerTask() {
    }

    @Override
    public void run() {
      LOG.debug("Sweeping client entries...");
      synchronized (knownClients) {
        Iterator<Map.Entry<ClientID, ClientEntry>> clientIter = knownClients.entrySet().iterator();
        while (clientIter.hasNext()) {
          Map.Entry<ClientID, ClientEntry> curEntry = clientIter.next();
          ClientEntry clientEntry = curEntry.getValue();
          // Only touch the entry if the buffer not currently in use by a
          // client.
          if (!clientEntry.getEventBuffer().hasWaitingClient()) {
            // If the client has been seen since the last run, reset the
            // 'alive' flag.
            if (clientEntry.isAlive()) {
              clientEntry.setAlive(false);
            }
            // If the client hasn't been seen since the last run, remove its
            // ID from the list of known clients - the client has been
            // inactive for long enough.
            else {
              LOG.debug("Removing inactive client entry (client user: {})",
                        clientEntry.getUserName());
              clientIter.remove();
            }
          }
        }
      }
    }
  }
}
