/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.services;

import java.util.HashSet;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.customizations.kernel.GlobalSyncObject;
import org.opentcs.data.ObjectHistory;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.kernel.workingset.TCSObjectManager;
import org.opentcs.kernel.workingset.TCSObjectRepository;

/**
 * This class is the standard implementation of the {@link TCSObjectService} interface.
 */
public class StandardTCSObjectService
    implements TCSObjectService {

  /**
   * A global object to be used for synchronization within the kernel.
   */
  private final Object globalSyncObject;
  /**
   * The object manager.
   */
  private final TCSObjectManager objectManager;

  /**
   * Creates a new instance.
   *
   * @param globalSyncObject The kernel threads' global synchronization object.
   * @param objectManager The object manager.
   */
  @Inject
  public StandardTCSObjectService(@GlobalSyncObject Object globalSyncObject,
                                  TCSObjectManager objectManager) {
    this.globalSyncObject = requireNonNull(globalSyncObject, "globalSyncObject");
    this.objectManager = requireNonNull(objectManager, "objectManager");
  }

  @Override
  public <T extends TCSObject<T>> T fetchObject(Class<T> clazz, TCSObjectReference<T> ref) {
    requireNonNull(clazz, "clazz");
    requireNonNull(ref, "ref");

    synchronized (getGlobalSyncObject()) {
      return getObjectRepo().getObjectOrNull(clazz, ref);
    }
  }

  @Override
  public <T extends TCSObject<T>> T fetchObject(Class<T> clazz, String name) {
    requireNonNull(clazz, "clazz");

    synchronized (getGlobalSyncObject()) {
      return getObjectRepo().getObjectOrNull(clazz, name);
    }
  }

  @Override
  public <T extends TCSObject<T>> Set<T> fetchObjects(Class<T> clazz) {
    requireNonNull(clazz, "clazz");

    synchronized (getGlobalSyncObject()) {
      Set<T> objects = getObjectRepo().getObjects(clazz);
      Set<T> copies = new HashSet<>();
      for (T object : objects) {
        copies.add(object);
      }
      return copies;
    }
  }

  @Override
  public <T extends TCSObject<T>> Set<T> fetchObjects(@Nonnull Class<T> clazz,
                                                      @Nonnull Predicate<? super T> predicate) {
    requireNonNull(clazz, "clazz");
    requireNonNull(predicate, "predicate");

    synchronized (getGlobalSyncObject()) {
      return getObjectRepo().getObjects(clazz, predicate);
    }
  }

  @Override
  public void updateObjectProperty(TCSObjectReference<?> ref, String key, @Nullable String value)
      throws ObjectUnknownException {
    requireNonNull(ref, "ref");
    requireNonNull(key, "key");

    synchronized (getGlobalSyncObject()) {
      objectManager.setObjectProperty(ref, key, value);
    }
  }

  @Override
  public void appendObjectHistoryEntry(TCSObjectReference<?> ref, ObjectHistory.Entry entry)
      throws ObjectUnknownException {
    requireNonNull(ref, "ref");
    requireNonNull(entry, "entry");

    synchronized (getGlobalSyncObject()) {
      objectManager.appendObjectHistoryEntry(ref, entry);
    }
  }

  protected Object getGlobalSyncObject() {
    return globalSyncObject;
  }

  protected TCSObjectRepository getObjectRepo() {
    return objectManager.getObjectRepo();
  }
}
