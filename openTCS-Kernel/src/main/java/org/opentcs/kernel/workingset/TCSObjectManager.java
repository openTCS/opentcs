/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.workingset;

import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.data.ObjectHistory;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.util.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles generic modifications of objects contained in a {@link TCSObjectRepository}.
 * <p>
 * Note that no synchronization is done inside this class. Concurrent access of instances of this
 * class must be synchronized externally.
 * </p>
 */
public class TCSObjectManager {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(TCSObjectManager.class);
  /**
   * The object repo.
   */
  private final TCSObjectRepository objectRepo;
  /**
   * A handler we should emit object events to.
   */
  private final EventHandler eventHandler;

  /**
   * Creates a new instance.
   *
   * @param objectRepo The object repo.
   * @param eventHandler The event handler to publish events to.
   */
  @Inject
  public TCSObjectManager(@Nonnull TCSObjectRepository objectRepo,
                          @Nonnull @ApplicationEventBus EventHandler eventHandler) {
    this.objectRepo = requireNonNull(objectRepo, "objectRepo");
    this.eventHandler = requireNonNull(eventHandler, "eventHandler");
  }

  /**
   * Returns the underlying object repo.
   *
   * @return The underlying object repo.
   */
  @Nonnull
  public TCSObjectRepository getObjectRepo() {
    return objectRepo;
  }

  /**
   * Sets a property for the referenced object.
   *
   * @param ref A reference to the object to be modified.
   * @param key The property's key/name.
   * @param value The property's value. If <code>null</code>, removes the
   * property from the object.
   * @throws ObjectUnknownException If the referenced object does not exist.
   */
  public void setObjectProperty(@Nonnull TCSObjectReference<?> ref,
                                @Nonnull String key,
                                @Nullable String value)
      throws ObjectUnknownException {
    requireNonNull(ref, "ref");
    requireNonNull(key, "key");

    TCSObject<?> object = objectRepo.getObject(ref);
    TCSObject<?> previousState = object;
    LOG.debug("Setting property on object named '{}': key='{}', value='{}'",
              ref.getName(),
              key,
              value);
    object = object.withProperty(key, value);
    objectRepo.replaceObject(object);
    emitObjectEvent(object, previousState, TCSObjectEvent.Type.OBJECT_MODIFIED);
  }

  /**
   * Appends a history entry to the referenced object.
   *
   * @param ref A reference to the object to be modified.
   * @param entry The history entry to be appended.
   * @throws ObjectUnknownException If the referenced object does not exist.
   */
  public void appendObjectHistoryEntry(@Nonnull TCSObjectReference<?> ref,
                                       @Nonnull ObjectHistory.Entry entry)
      throws ObjectUnknownException {
    requireNonNull(ref, "ref");
    requireNonNull(entry, "entry");

    TCSObject<?> object = objectRepo.getObject(ref);
    TCSObject<?> previousState = object;
    LOG.debug("Appending history entry to object named '{}': {}", ref.getName(), entry);
    object = object.withHistoryEntry(entry);
    objectRepo.replaceObject(object);
    emitObjectEvent(object, previousState, TCSObjectEvent.Type.OBJECT_MODIFIED);
  }

  /**
   * Emits an event for the given object with the given type.
   *
   * @param currentObjectState The current state of the object to emit an event
   * for.
   * @param previousObjectState The previous state of the object to emit an
   * event for.
   * @param evtType The type of event to emit.
   */
  public void emitObjectEvent(TCSObject<?> currentObjectState,
                              TCSObject<?> previousObjectState,
                              TCSObjectEvent.Type evtType) {
    eventHandler.onEvent(new TCSObjectEvent(currentObjectState, previousObjectState, evtType));
  }

}
