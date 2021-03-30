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
import org.opentcs.kernel.workingset.TCSObjectPool;

/**
 * This class is the standard implementation of the {@link TCSObjectService} interface.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class StandardTCSObjectService
    implements TCSObjectService {

  /**
   * A global object to be used for synchronization within the kernel.
   */
  private final Object globalSyncObject;
  /**
   * The container of all course model and transport order objects.
   */
  private final TCSObjectPool globalObjectPool;

  /**
   * Creates a new instance.
   *
   * @param globalSyncObject The kernel threads' global synchronization object.
   * @param globalObjectPool The object pool to be used.
   */
  @Inject
  public StandardTCSObjectService(@GlobalSyncObject Object globalSyncObject,
                                  TCSObjectPool globalObjectPool) {
    this.globalSyncObject = requireNonNull(globalSyncObject, "globalSyncObject");
    this.globalObjectPool = requireNonNull(globalObjectPool, "globalObjectPool");
  }

  @Override
  public <T extends TCSObject<T>> T fetchObject(Class<T> clazz, TCSObjectReference<T> ref) {
    synchronized (getGlobalSyncObject()) {
      return getGlobalObjectPool().getObjectOrNull(clazz, ref);
    }
  }

  @Override
  public <T extends TCSObject<T>> T fetchObject(Class<T> clazz, String name) {
    synchronized (getGlobalSyncObject()) {
      return getGlobalObjectPool().getObjectOrNull(clazz, name);
    }
  }

  @Override
  public <T extends TCSObject<T>> Set<T> fetchObjects(Class<T> clazz) {
    synchronized (getGlobalSyncObject()) {
      Set<T> objects = getGlobalObjectPool().getObjects(clazz);
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
    synchronized (getGlobalSyncObject()) {
      return getGlobalObjectPool().getObjects(clazz, predicate);
    }
  }

  @Override
  public void updateObjectProperty(TCSObjectReference<?> ref, String key, @Nullable String value)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getGlobalObjectPool().setObjectProperty(ref, key, value);
    }
  }

  @Override
  public void appendObjectHistoryEntry(TCSObjectReference<?> ref, ObjectHistory.Entry entry)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getGlobalObjectPool().appendObjectHistoryEntry(ref, entry);
    }
  }

  protected Object getGlobalSyncObject() {
    return globalSyncObject;
  }

  protected TCSObjectPool getGlobalObjectPool() {
    return globalObjectPool;
  }
}
