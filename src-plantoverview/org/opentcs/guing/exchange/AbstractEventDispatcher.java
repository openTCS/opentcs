/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.exchange;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.logging.Logger;
import org.opentcs.access.Kernel;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.guing.exchange.adapter.LinkAdapter;
import org.opentcs.guing.exchange.adapter.ProcessAdapter;
import org.opentcs.guing.exchange.adapter.ProcessAdapterFactory;
import org.opentcs.guing.model.ModelComponent;

/**
 * Basic implementation of a central event dispatcher between the kernel
 * and the plant overview.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public abstract class AbstractEventDispatcher
    implements EventDispatcher {

  /**
   * This class's logger.
   */
  private static final Logger logger
      = Logger.getLogger(AbstractEventDispatcher.class.getName());
  /**
   * Assignment of a model to its {@link ProcessAdapter}.
   * Often it is the case that a model is known and we have to look for
   * its ProcessAdapter. Furthermore there can be models with ProcessAdapters,
   * for which no kernel object exists.
   */
  protected final Map<ModelComponent, ProcessAdapter> fAdaptersByModel
      = new HashMap<>();
  /**
   * Assignment of a kernel object reference to a {@link ProcessAdapter}.
   */
  private final Map<TCSObjectReference<?>, ProcessAdapter> fAdaptersByProcess
      = new HashMap<>();
  /**
   * The process adapter factory to be used.
   */
  private final ProcessAdapterFactory procAdapterFactory;
  /**
   * The Kernel.
   */
  private Kernel fServer;

  /**
   * Creates a new instance.
   *
   * @param procAdapterFactory The process adapter factory to be used.
   */
  protected AbstractEventDispatcher(ProcessAdapterFactory procAdapterFactory) {
    this.procAdapterFactory = requireNonNull(procAdapterFactory,
                                             "procAdapterFactory");
  }

  @Override // EventDispatcher
  public void addProcessAdapter(ProcessAdapter adapter) {
    requireNonNull(adapter, "adapter");

    adapter.setEventDispatcher(this);

    if (adapter.getProcessObject() == null) {
      if (!(adapter instanceof LinkAdapter)) {
        // For a LinkModel there is no process object
        logger.fine("Adapter without ProcessObject: "
            + adapter.getClass().getName());
      }
    }
    else {
      fAdaptersByProcess.put((TCSObjectReference<?>) adapter.getProcessObject(),
                             adapter);
    }

    fAdaptersByModel.put(adapter.getModel(), adapter);
  }

  @Override // EventDispatcher
  public void removeProcessAdapter(ProcessAdapter adapter) {
    requireNonNull(adapter, "adapter");

    if (adapter.getProcessObject() != null) {
      TCSObjectReference<?> processObject
          = (TCSObjectReference<?>) adapter.getProcessObject();
      fAdaptersByProcess.remove(processObject);
    }

    fAdaptersByModel.remove(adapter.getModel());
  }

  @Override // EventDispatcher
  public void setKernel(Kernel server) {
    fServer = server;
  }

  @Override // EventDispatcher
  public Kernel getKernel() {
    return fServer;
  }

  @Override // EventDispatcher
  public ProcessAdapter findProcessAdapter(ModelComponent model) {
    return fAdaptersByModel.get(model);
  }

  @Override // EventDispatcher
  public ProcessAdapter findProcessAdapter(TCSObjectReference<?> processObject) {
    requireNonNull(processObject, "processObject");

    for (Map.Entry<TCSObjectReference<?>, ProcessAdapter> entry
         : fAdaptersByProcess.entrySet()) {
      if (entry.getKey().getId() == processObject.getId()) {
        return entry.getValue();
      }
    }

    return null;
  }

  @Override // EventDispatcher
  public void release() {
  }

  @Override // EventDispatcher
  public Iterator<ProcessAdapter> getProcessAdapters() {
    return fAdaptersByModel.values().iterator();
  }

  @Override // EventDispatcher
  public int countAdaptersByModel() {
    return fAdaptersByModel.size();
  }

  @Override // EventDispatcher
  public int countAdaptersByProcess() {
    return fAdaptersByProcess.size();
  }

  @Override // EventDispatcher
  public ProcessAdapter createProcessAdapter(
      Class<? extends ModelComponent> model) {
    return procAdapterFactory.createAdapter(model);
  }
}
