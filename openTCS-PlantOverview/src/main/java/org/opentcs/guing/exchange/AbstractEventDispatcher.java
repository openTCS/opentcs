/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.exchange;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import org.opentcs.access.SharedKernelProvider;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.guing.exchange.adapter.ProcessAdapter;
import org.opentcs.guing.model.ModelComponent;

/**
 * Basic implementation of a central event dispatcher between the kernel and the plant overview.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public abstract class AbstractEventDispatcher
    implements EventDispatcher {

  /**
   * Assignment of a model to its {@link ProcessAdapter}.
   * Often it is the case that a model is known and we have to look for
   * its ProcessAdapter. Furthermore there can be models with ProcessAdapters,
   * for which no kernel object exists.
   */
  private final Map<ModelComponent, ProcessAdapter> fAdaptersByModel = new HashMap<>();
  /**
   * Provides access to a kernel.
   */
  private final SharedKernelProvider kernelProvider;

  /**
   * Creates a new instance.
   *
   * @param kernelProvider Provides a access to a kernel.
   */
  public AbstractEventDispatcher(SharedKernelProvider kernelProvider) {
    this.kernelProvider = requireNonNull(kernelProvider, "kernelProvider");
  }

  @Override // EventDispatcher
  public void addProcessAdapter(ProcessAdapter adapter) {
    requireNonNull(adapter, "adapter");

    fAdaptersByModel.put(adapter.getModel(), adapter);
  }

  @Override // EventDispatcher
  public void removeProcessAdapter(ProcessAdapter adapter) {
    requireNonNull(adapter, "adapter");

    fAdaptersByModel.remove(adapter.getModel());
  }

  @Override // EventDispatcher
  public ProcessAdapter findProcessAdapter(ModelComponent model) {
    return fAdaptersByModel.get(model);
  }

  @Override // EventDispatcher
  public ProcessAdapter findProcessAdapter(TCSObjectReference<?> processObject) {
    requireNonNull(processObject, "processObject");

    // XXX We look up the object by its name here, assuming that the name does
    // XXX not change during runtime. This is true for model objects in
    // XXX operating mode now, but the assumption may not hold forever.
    for (Map.Entry<ModelComponent, ProcessAdapter> entry : fAdaptersByModel.entrySet()) {
      if (Objects.equals(entry.getKey().getName(), processObject.getName())) {
        return entry.getValue();
      }
    }

    return null;
  }

  @Override // EventDispatcher
  public Set<ProcessAdapter> getProcessAdapters() {
    return new HashSet<>(fAdaptersByModel.values());
  }

  protected SharedKernelProvider getKernelProvider() {
    return kernelProvider;
  }
}
