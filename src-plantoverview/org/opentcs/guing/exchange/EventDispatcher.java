/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.exchange;

import java.util.Iterator;
import org.opentcs.access.Kernel;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.guing.exchange.adapter.ProcessAdapter;
import org.opentcs.guing.model.ModelComponent;

/**
 * Basic interface of a central event dispatcher between the kernel
 * and the plant overview.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
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
   * Returns an <code>Iterator</code> which contains all adapters
   * to the kernel.
   * 
   * @return An <code>Iterator</code> with {@link ProcessAdapter}s.
   */
  Iterator<ProcessAdapter> getProcessAdapters();

  /**
   * Sets the kernel.
   *
   * @param server The kernel.
   */
  void setKernel(Kernel server);

  /**
   * Returns the kernel.
   * 
   * @return The kernel.
   */
  Kernel getKernel();

  /**
   * Finds the process adapter to the given model
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
   * Registers as a listener at the kernel.
   */
  void register();

  /**
   * Removes this dispatcher as a listener from the kernel.
   */
  void release();

  /**
   * Returns the amount of {@link ProcessAdapter}s to kernel objects.
   *
   * @return The amount of {@link ProcessAdapter}s.
   */
  int countAdaptersByProcess();

  /**
   * Returns the amount of {@link ProcessAdapter}s to models.
   *
   * @return The amount of {@link ProcessAdapter}s.
   */
  int countAdaptersByModel();

  /**
   * Creates a {@link ProcessAdapter} for the given class of a model.
   *
   * @param model The class of a {@link ModelComponent} a {@link ProcessAdapter}
   * shall be created for.
   * @return The created {@link ProcessAdapter}.
   */
  ProcessAdapter createProcessAdapter(Class<? extends ModelComponent> model);
}
