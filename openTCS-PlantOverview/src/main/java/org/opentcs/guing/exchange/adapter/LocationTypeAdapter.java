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

import com.google.inject.assistedinject.Assisted;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.Kernel;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.to.model.LocationTypeCreationTO;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.data.ObjectPropConstants;
import static org.opentcs.data.ObjectPropConstants.LOCTYPE_DEFAULT_REPRESENTATION;
import org.opentcs.data.TCSObject;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.visualization.LocationRepresentation;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.guing.components.properties.type.KeyValueProperty;
import org.opentcs.guing.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.components.properties.type.StringSetProperty;
import org.opentcs.guing.components.properties.type.SymbolProperty;
import org.opentcs.guing.exchange.EventDispatcher;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.LocationTypeModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An adapter for location types.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class LocationTypeAdapter
    extends AbstractProcessAdapter {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(LocationTypeAdapter.class);

  /**
   * Creates a new instance.
   *
   * @param model The corresponding model component.
   * @param eventDispatcher The event dispatcher.
   */
  @Inject
  public LocationTypeAdapter(@Assisted LocationTypeModel model,
                             @Assisted EventDispatcher eventDispatcher) {
    super(model, eventDispatcher);
  }

  @Override
  public LocationTypeModel getModel() {
    return (LocationTypeModel) super.getModel();
  }

  @Override // OpenTCSProcessAdapter
  public void updateModelProperties(Kernel kernel,
                                    TCSObject<?> tcsObject,
                                    @Nullable ModelLayoutElement layoutElement) {
    LocationType locationType = requireNonNull((LocationType) tcsObject,
                                               "tcsObject");
    try {
      // Name
      StringProperty pNname
          = (StringProperty) getModel().getProperty(ModelComponent.NAME);
      pNname.setText(locationType.getName());
      // Allowed operations
      StringSetProperty pOperations = (StringSetProperty) getModel()
          .getProperty(LocationTypeModel.ALLOWED_OPERATIONS);
      pOperations.setItems(new ArrayList<>(locationType.getAllowedOperations()));
      updateMiscModelProperties(locationType);
      KeyValueSetProperty miscellaneous = (KeyValueSetProperty) getModel()
          .getProperty(ModelComponent.MISCELLANEOUS);

      for (KeyValueProperty next : miscellaneous.getItems()) {
        if (next.getKey().equals(
            ObjectPropConstants.LOCTYPE_DEFAULT_REPRESENTATION)) {
          SymbolProperty symbol = (SymbolProperty) getModel().getProperty(
              ObjectPropConstants.LOCTYPE_DEFAULT_REPRESENTATION);
          symbol.setLocationRepresentation(
              LocationRepresentation.valueOf(next.getValue()));
          break;
        }
      }
    }
    catch (CredentialsException e) {
      LOG.error("", e);
    }
  }

  @Override // OpenTCSProcessAdapter
  public void storeToPlantModel(PlantModelCreationTO plantModel) {
    try {
      plantModel.getLocationTypes().add(
          new LocationTypeCreationTO(getModel().getName())
              .setAllowedOperations(getAllowedOperations())
              .setProperties(getKernelProperties())
      );

      unmarkAllPropertiesChanged();
    }
    catch (KernelRuntimeException e) {
      LOG.warn("", e);
    }
  }

  private List<String> getAllowedOperations() {
    return new LinkedList<>(
        ((StringSetProperty) getModel().getProperty(LocationTypeModel.ALLOWED_OPERATIONS))
            .getItems());
  }

  @Override
  protected Map<String, String> getKernelProperties() {
    Map<String, String> result = super.getKernelProperties();

    // Add the location representation (symbol) from the model.
    SymbolProperty pSymbol
        = (SymbolProperty) getModel().getProperty(LOCTYPE_DEFAULT_REPRESENTATION);
    LocationRepresentation locationRepresentation = pSymbol.getLocationRepresentation();

    if (locationRepresentation != null) {
      result.put(LOCTYPE_DEFAULT_REPRESENTATION, locationRepresentation.name());
    }

    return result;
  }

}
