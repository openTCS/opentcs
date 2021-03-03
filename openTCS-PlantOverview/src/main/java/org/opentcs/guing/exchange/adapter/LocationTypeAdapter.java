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
import static java.util.Objects.requireNonNull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.Kernel;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.data.ObjectPropConstants;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
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
  private static final Logger log
      = LoggerFactory.getLogger(LocationTypeAdapter.class);

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
      log.error("", e);
    }
  }

  @Override // OpenTCSProcessAdapter
  public void updateProcessProperties(Kernel kernel) {
    LocationType locType = kernel.createLocationType();
    TCSObjectReference<LocationType> reference = locType.getReference();

    StringProperty pName
        = (StringProperty) getModel().getProperty(ModelComponent.NAME);
    String name = pName.getText();

    try {
      kernel.renameTCSObject(reference, name);

      updateProcessActions(kernel, reference);

      updateMiscProcessProperties(kernel, reference);
    }
    catch (KernelRuntimeException e) {
      log.warn("", e);
    }
  }

  private void updateProcessActions(Kernel kernel,
                                    TCSObjectReference<LocationType> reference)
      throws KernelRuntimeException {
    StringSetProperty pActions = (StringSetProperty) getModel().getProperty(
        LocationTypeModel.ALLOWED_OPERATIONS);

    for (String newOp : pActions.getItems()) {
      kernel.addLocationTypeAllowedOperation(reference, newOp);
    }
  }

  @Override // OpenTCSProcessAdapter
  protected void updateMiscProcessProperties(Kernel kernel,
                                             TCSObjectReference<?> ref)
      throws KernelRuntimeException {
    kernel.clearTCSObjectProperties(ref);
    KeyValueSetProperty pMisc = (KeyValueSetProperty) getModel().getProperty(
        ModelComponent.MISCELLANEOUS);

    if (pMisc != null) {
      // Update the location representation (symbol) from the model.
      SymbolProperty pSymbol = (SymbolProperty) getModel().getProperty(
          ObjectPropConstants.LOCTYPE_DEFAULT_REPRESENTATION);
      LocationRepresentation locationRepresentation = pSymbol
          .getLocationRepresentation();

      if (locationRepresentation != null) {
        KeyValueProperty kvp = new KeyValueProperty(getModel(),
                                                    ObjectPropConstants.LOCTYPE_DEFAULT_REPRESENTATION,
                                                    locationRepresentation
                                                    .name());
        pMisc.addItem(kvp);
      }
      else {
        for (KeyValueProperty kvp : pMisc.getItems()) {
          if (kvp.getKey().equals(
              ObjectPropConstants.LOCTYPE_DEFAULT_REPRESENTATION)) {
            pMisc.removeItem(kvp);
            break;
          }
        }
      }

      // Set all properties on the kernel object.
      for (KeyValueProperty kvp : pMisc.getItems()) {
        kernel.setTCSObjectProperty(ref, kvp.getKey(), kvp.getValue());
      }
    }
  }
}
