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

import java.util.Set;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.guing.exchange.adapter.ProcessAdapter;
import org.opentcs.guing.model.ModelComponent;

/**
 * Basic interface of a central event dispatcher between the kernel
 * and the plant overview.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface EventDispatcher {

  /**
   * Adds a {@link ProcessAdapter}.
   *
   * @param processAdapter The {@link ProcessAdapter} to add.
   */
  void addProcessAdapter(ProcessAdapter processAdapter);

  /**
   * Removes a {@link ProcessAdapter}.
   *
   * @param processAdapter The {@link ProcessAdapter} to remove.
   */
  void removeProcessAdapter(ProcessAdapter processAdapter);

  /**
   * Finds the process adapter to the given model.
   *
   * @param model The model you need the process adapter for.
   * @return The {@link ProcessAdapter} for the model.
   */
  ProcessAdapter findProcessAdapter(ModelComponent model);

  /**
   * Finds the {@link ProcessAdapter} to the given object reference.
   *
   * @param processObject A reference to a kernel object.
   * @return The {@link ProcessAdapter} for the reference.
   */
  ProcessAdapter findProcessAdapter(TCSObjectReference<?> processObject);

  /**
   * Returns all process adapters associated with models.
   *
   * @return All process adapters associated with models.
   */
  Set<ProcessAdapter> getProcessAdapters();

  /**
   * Registers as a listener at the kernel.
   */
  void register();

  /**
   * Removes this dispatcher as a listener from the kernel.
   */
  void release();
}
