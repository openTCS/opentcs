/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.exchange.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import org.opentcs.access.Kernel;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.guing.components.properties.type.KeyValueProperty;
import org.opentcs.guing.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.exchange.EventDispatcher;
import org.opentcs.guing.model.ModelComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic implementation of a <code>ProcessAdapter</code>.
 * Synchronizes between the local <code>ModelComponent</code> and the
 * corresponding kernel object.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public abstract class AbstractProcessAdapter
    implements ProcessAdapter {

  /**
   * This class's logger.
   */
  private static final Logger log
      = LoggerFactory.getLogger(AbstractProcessAdapter.class);

  /**
   * The <code>ModelComponent</code>.
   */
  private final ModelComponent fModelComponent;
  /**
   * Maintains a map which relates the model component and the kernel object.
   */
  private final EventDispatcher fEventDispatcher;

  /**
   * Creates a new instance.
   *
   * @param modelComponent The corresponding ModelComponent.
   * @param eventDispatcher The event dispatcher.
   */
  public AbstractProcessAdapter(ModelComponent modelComponent,
                                EventDispatcher eventDispatcher) {
    this.fModelComponent = requireNonNull(modelComponent, "modelComponent");
    this.fEventDispatcher = requireNonNull(eventDispatcher, "eventDispatcher");
  }

  @Override // ProcessAdapter
  public void register() {
    fEventDispatcher.addProcessAdapter(this);
  }

  @Override // ProcessAdapter
  public ModelComponent getModel() {
    return fModelComponent;
  }

  /**
   * Returns the <code>EventDispatcher</code>.
   *
   * @return The <code>EventDispatcher</code>.
   */
  protected EventDispatcher getEventDispatcher() {
    return fEventDispatcher;
  }

  /**
   * Reads the current misc properties from the model component and adopts these
   * for the kernel object.
   *
   * @param kernel The kernel.
   * @param ref A reference to the corresponding kernel object.
   */
  protected void updateMiscProcessProperties(Kernel kernel,
                                             TCSObjectReference<?> ref)
      throws KernelRuntimeException {
    kernel.clearTCSObjectProperties(ref);

    KeyValueSetProperty misc = (KeyValueSetProperty) getModel().getProperty(
        ModelComponent.MISCELLANEOUS);

    if (misc != null) {
      for (KeyValueProperty p : misc.getItems()) {
        kernel.setTCSObjectProperty(ref, p.getKey(), p.getValue());
      }
    }
  }

  /**
   * Reads the current misc properties from the kernel and adopts these for
   * the model object.
   *
   * @param tcsObject The <code>TCSObject</code> to read from.
   */
  protected void updateMiscModelProperties(TCSObject<?> tcsObject) {
    List<KeyValueProperty> items = new ArrayList<>();
    Map<String, String> misc = tcsObject.getProperties();

    for (Map.Entry<String, String> curEntry : misc.entrySet()) {
      if (!curEntry.getValue().contains("Unknown")) {
        items.add(new KeyValueProperty(getModel(),
                                       curEntry.getKey(),
                                       curEntry.getValue()));
      }
    }

    KeyValueSetProperty miscellaneous = (KeyValueSetProperty) getModel()
        .getProperty(ModelComponent.MISCELLANEOUS);
    miscellaneous.setItems(items);
  }
}
