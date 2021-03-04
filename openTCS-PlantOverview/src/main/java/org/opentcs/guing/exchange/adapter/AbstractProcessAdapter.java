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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.opentcs.access.to.model.VisualLayoutCreationTO;
import org.opentcs.data.TCSObject;
import org.opentcs.guing.components.properties.type.KeyValueProperty;
import org.opentcs.guing.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.components.properties.type.Property;
import org.opentcs.guing.model.ModelComponent;

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

  protected Map<String, String> getKernelProperties(ModelComponent model) {
    Map<String, String> result = new HashMap<>();

    KeyValueSetProperty misc = (KeyValueSetProperty) model.getProperty(
        ModelComponent.MISCELLANEOUS);

    if (misc != null) {
      for (KeyValueProperty p : misc.getItems()) {
        result.put(p.getKey(), p.getValue());
      }
    }

    return result;
  }

  protected void unmarkAllPropertiesChanged(ModelComponent model) {
    for (Property prop : model.getProperties().values()) {
      prop.unmarkChanged();
    }
  }

  /**
   * Reads the current misc properties from the kernel and adopts these for the model object.
   *
   * @param model The model object to adopt the properties for.
   * @param tcsObject The <code>TCSObject</code> to read from.
   */
  protected void updateMiscModelProperties(ModelComponent model, TCSObject<?> tcsObject) {
    List<KeyValueProperty> items = new ArrayList<>();

    for (Map.Entry<String, String> curEntry : tcsObject.getProperties().entrySet()) {
      if (!curEntry.getValue().contains("Unknown")) {
        items.add(new KeyValueProperty(model, curEntry.getKey(), curEntry.getValue()));
      }
    }

    KeyValueSetProperty miscellaneous = (KeyValueSetProperty) model
        .getProperty(ModelComponent.MISCELLANEOUS);
    miscellaneous.setItems(items);
  }

  protected List<VisualLayoutCreationTO> updatedLayouts(ModelComponent model,
                                                        List<VisualLayoutCreationTO> layouts) {
    List<VisualLayoutCreationTO> result = new ArrayList<>(layouts.size());

    for (VisualLayoutCreationTO layout : layouts) {
      result.add(updatedLayout(model, layout));
    }

    return result;
  }

  protected VisualLayoutCreationTO updatedLayout(ModelComponent model,
                                                 VisualLayoutCreationTO layout) {
    return layout;
  }
}
