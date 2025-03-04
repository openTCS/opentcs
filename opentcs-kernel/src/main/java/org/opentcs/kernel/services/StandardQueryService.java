// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.services;

import static java.util.Objects.requireNonNull;
import static org.opentcs.util.Assertions.checkArgument;

import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import org.opentcs.components.kernel.Query;
import org.opentcs.components.kernel.QueryResponder;
import org.opentcs.components.kernel.services.InternalQueryService;
import org.opentcs.customizations.kernel.GlobalSyncObject;

/**
 * The default implementation of the {@link InternalQueryService} interface.
 */
public class StandardQueryService
    implements
      InternalQueryService {

  /**
   * A global object to be used for synchronization within the kernel.
   */
  private final Object globalSyncObject;
  /**
   * The responders, by query type.
   */
  private final Map<Class<? extends Query<?>>, QueryResponder> respondersByQueryType
      = new HashMap<>();

  /**
   * Creates a new instance.
   *
   * @param globalSyncObject The kernel threads' global synchronization object.
   */
  @Inject
  public StandardQueryService(
      @GlobalSyncObject
      Object globalSyncObject
  ) {
    this.globalSyncObject = requireNonNull(globalSyncObject, "globalSyncObject");
  }

  @Override
  public <T> T query(Query<T> query) {
    requireNonNull(query, "query");

    synchronized (globalSyncObject) {
      QueryResponder responder = respondersByQueryType.get(query.getClass());

      checkArgument(responder != null, "Query class not taken: %s", query.getClass().getName());
      return responder.query(query);
    }
  }

  @Override
  public void registerResponder(
      @Nonnull
      Class<? extends Query<?>> clazz,
      @Nonnull
      QueryResponder responder
  ) {
    requireNonNull(clazz, "clazz");
    requireNonNull(responder, "responder");

    synchronized (globalSyncObject) {
      checkArgument(
          !respondersByQueryType.containsKey(clazz),
          "Query class already taken: %s",
          clazz.getName()
      );

      respondersByQueryType.put(clazz, responder);
    }
  }

  @Override
  public void unregisterResponder(
      @Nonnull
      Class<? extends Query<?>> clazz
  ) {
    requireNonNull(clazz, "clazz");

    synchronized (globalSyncObject) {
      respondersByQueryType.remove(clazz);
    }
  }
}
