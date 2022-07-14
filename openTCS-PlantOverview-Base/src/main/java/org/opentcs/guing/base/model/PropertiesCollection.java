/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.base.model;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import static org.opentcs.guing.base.I18nPlantOverviewBase.BUNDLE_PATH;
import org.opentcs.guing.base.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.base.components.properties.type.AbstractProperty;
import org.opentcs.guing.base.components.properties.type.MultipleDifferentValues;
import org.opentcs.guing.base.components.properties.type.Property;

/**
 * Allows to change properties with the same name of multiple model components at the same time.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class PropertiesCollection
    extends CompositeModelComponent {

  /**
   * This class's resource bundle.
   */
  private final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_PATH);

  /**
   * Creates a new instance.
   *
   * @param models The model components.
   */
  public PropertiesCollection(Collection<ModelComponent> models) {
    Objects.requireNonNull(models, "models is null");

    for (ModelComponent curModel : models) {
      add(curModel);
    }

    extractSameProperties();
  }

  /**
   * Finds the properties that can be collectively edited.
   */
  private void extractSameProperties() {
    if (getChildComponents().isEmpty()) {
      return;
    }
    ModelComponent firstModel = getChildComponents().get(0);
    Map<String, Property> properties = firstModel.getProperties();

    for (Map.Entry<String, Property> curEntry : properties.entrySet()) {
      String name = curEntry.getKey();
      Property property = curEntry.getValue();
      boolean ok = true;
      boolean differentValues = false;

      if (!property.isCollectiveEditable()) {
        ok = false;
      }

      for (int i = 1; i < getChildComponents().size(); i++) {
        ModelComponent followingModel = getChildComponents().get(i);
        if (!firstModel.getClass().equals(followingModel.getClass())) {
          return;
        }

        Property pendant = followingModel.getProperty(name);

        if (pendant == null) {
          ok = false;
          break;
        }
        else if ((!pendant.isCollectiveEditable())) {
          ok = false;
          break;
        }

        if (!(property.getComparableValue() == null && pendant.getComparableValue() == null)
            && (property.getComparableValue() != null && pendant.getComparableValue() == null
                || property.getComparableValue() == null && pendant.getComparableValue() != null
                || !property.getComparableValue().equals(pendant.getComparableValue()))) {
          differentValues = true;
        }
      }

      if (ok) {
        AbstractProperty clone = (AbstractProperty) property.clone();

        if (differentValues) {
          clone.setValue(new MultipleDifferentValues());

          clone.unmarkChanged();

          clone.setDescription(property.getDescription());
          clone.setHelptext(property.getHelptext());
          clone.setModellingEditable(property.isModellingEditable());
          clone.setOperatingEditable(property.isOperatingEditable());
          setProperty(name, clone);
        }
        else {
          // TODO: clone() Methode ?
          clone.setDescription(property.getDescription());
          clone.setHelptext(property.getHelptext());
          clone.setModellingEditable(property.isModellingEditable());
          clone.setOperatingEditable(property.isOperatingEditable());
          setProperty(name, clone);
        }
      }
    }
  }

  /**
   * Notifies the registered listeners about the changed properties.
   *
   * @param listener The listener that initiated the property change.
   */
  @Override // AbstractModelComponent
  public void propertiesChanged(AttributesChangeListener listener) {
    for (ModelComponent model : getChildComponents()) {
      copyPropertiesToModel(model);
      model.propertiesChanged(listener);
    }

    extractSameProperties();
  }

  /**
   * Copies the properties to the model component.
   *
   * @param model The model component to copy properties to.
   */
  protected void copyPropertiesToModel(ModelComponent model) {
    for (String name : getProperties().keySet()) {
      Property property = getProperty(name);
      if (property.hasChanged()) {
        Property modelProperty = model.getProperty(name);
        modelProperty.copyFrom(property);
        modelProperty.markChanged();
      }
    }
  }

  @Override // AbstractModelComponent
  public String getDescription() {
    return bundle.getString("propertiesCollection.description");
  }
}
