/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.rmi;

import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import org.opentcs.access.rmi.ClientID;
import org.opentcs.access.rmi.services.RemoteTCSObjectService;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the standard implementation of the {@link RemoteTCSObjectService} interface.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public abstract class StandardRemoteTCSObjectService
    extends KernelRemoteService
    implements RemoteTCSObjectService {

  /**
   * This class' logger.
   */
  private final static Logger LOG = LoggerFactory.getLogger(StandardRemoteTCSObjectService.class);
  /**
   * The object service to invoke methods on.
   */
  private final TCSObjectService objectService;
  /**
   * The user manager.
   */
  private final UserManager userManager;
  /**
   * Executes tasks modifying kernel data.
   */
  private final ExecutorService kernelExecutor;

  /**
   * Creates a new instance.
   *
   * @param objectService The object service.
   * @param userManager The user manager.
   * @param kernelExecutor Executes tasks modifying kernel data.
   */
  public StandardRemoteTCSObjectService(TCSObjectService objectService,
                                        UserManager userManager,
                                        @KernelExecutor ExecutorService kernelExecutor) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.userManager = requireNonNull(userManager, "userManager");
    this.kernelExecutor = requireNonNull(kernelExecutor, "kernelExecutor");
  }

  @Override
  public <T extends TCSObject<T>> T fetchObject(ClientID clientId, Class<T> clazz,
                                                TCSObjectReference<T> ref) {
    userManager.verifyCredentials(clientId, UserPermission.READ_DATA);

    return objectService.fetchObject(clazz, ref);
  }

  @Override
  public <T extends TCSObject<T>> T fetchObject(ClientID clientId, Class<T> clazz, String name) {
    userManager.verifyCredentials(clientId, UserPermission.READ_DATA);

    return objectService.fetchObject(clazz, name);
  }

  @Override
  public <T extends TCSObject<T>> Set<T> fetchObjects(ClientID clientId, Class<T> clazz) {
    userManager.verifyCredentials(clientId, UserPermission.READ_DATA);

    return objectService.fetchObjects(clazz);
  }

  @Override
  public <T extends TCSObject<T>> Set<T> fetchObjects(ClientID clientId,
                                                      Class<T> clazz,
                                                      Predicate<? super T> predicate) {
    userManager.verifyCredentials(clientId, UserPermission.READ_DATA);

    return objectService.fetchObjects(clazz, predicate);
  }

  @Override
  public void updateObjectProperty(ClientID clientId,
                                   TCSObjectReference<?> ref,
                                   String key,
                                   @Nullable String value) {
    userManager.verifyCredentials(clientId, UserPermission.MODIFY_MODEL);

    try {
      kernelExecutor.submit(() -> objectService.updateObjectProperty(ref, key, value)).get();
    }
    catch (InterruptedException | ExecutionException exc) {
      throw findSuitableExceptionFor(exc);
    }
  }
}
