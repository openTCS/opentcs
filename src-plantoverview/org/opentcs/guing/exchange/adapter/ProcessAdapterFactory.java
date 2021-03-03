/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.exchange.adapter;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A factory for <code>ProcessAdapters</code>.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class ProcessAdapterFactory {

  /**
   * This class's logger.
   */
  private static final Logger log
      = Logger.getLogger(ProcessAdapterFactory.class.getName());
  /**
   * Maps model component classes to prototypes of <code>ProcessAdapters</code>.
   */
  private final Map<Class, ProcessAdapter> fAdapters = new HashMap<>();

  /**
   * Creates a new instance of ProcessAdapterFactory.
   */
  ProcessAdapterFactory() {
  }

  /**
   * Creates for the given class of a <code>ModelComponent</code> an adapter.
   *
   * @param model The class an adapter is needed for.
   * @return The created adapter.
   */
  public ProcessAdapter createAdapter(Class model) {
    if (model == null) {
      return null;
    }

    ProcessAdapter prototype = fAdapters.get(model);
    if (prototype == null) {
      return createAdapter(model.getSuperclass());
    }

    try {
      return prototype.getClass().newInstance();
    }
    catch (InstantiationException | IllegalAccessException ex) {
      log.log(Level.WARNING, null, ex);
      return null;
    }
  }

  /**
   * Adds a new mapping of a model component class to an adapter.
   *
   * @param model The class of a <code>ModelComponent</code>.
   * @param adapter A prototype of an adapter that shall be created then.
   */
  public void add(Class model, ProcessAdapter adapter) {
    fAdapters.put(model, adapter);
  }
}
