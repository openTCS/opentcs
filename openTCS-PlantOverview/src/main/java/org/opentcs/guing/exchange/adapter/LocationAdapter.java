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
import java.awt.geom.Point2D;
import java.util.Map;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.Kernel;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.data.ObjectPropConstants;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.data.model.visualization.LocationRepresentation;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.guing.components.drawing.figures.LabeledLocationFigure;
import org.opentcs.guing.components.drawing.figures.LocationFigure;
import org.opentcs.guing.components.drawing.figures.TCSLabelFigure;
import org.opentcs.guing.components.properties.type.CoordinateProperty;
import org.opentcs.guing.components.properties.type.KeyValueProperty;
import org.opentcs.guing.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.components.properties.type.LocationTypeProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.components.properties.type.SymbolProperty;
import org.opentcs.guing.exchange.EventDispatcher;
import org.opentcs.guing.model.AbstractFigureComponent;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.storage.PlantModelCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNull;

/**
 * An adapter for locations.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class LocationAdapter
    extends AbstractProcessAdapter {

  /**
   * This class's logger.
   */
  private static final Logger log
      = LoggerFactory.getLogger(LocationAdapter.class);

  /**
   * Creates a new instance.
   *
   * @param model The corresponding model component.
   * @param eventDispatcher The event dispatcher.
   */
  @Inject
  public LocationAdapter(@Assisted LocationModel model,
                         @Assisted EventDispatcher eventDispatcher) {
    super(model, eventDispatcher);
  }

  @Override
  public LocationModel getModel() {
    return (LocationModel) super.getModel();
  }

  @Override  // OpenTCSProcessAdapter
  public void updateModelProperties(Kernel kernel,
                                    TCSObject<?> tcsObject,
                                    @Nullable ModelLayoutElement layoutElement) {
    Location location = requireNonNull((Location) tcsObject, "tcsObject");
    try {
      // Name 
      StringProperty pName
          = (StringProperty) getModel().getProperty(ModelComponent.NAME);
      pName.setText(location.getName());

      // Position in model
      CoordinateProperty cpx = (CoordinateProperty) getModel()
          .getProperty(AbstractFigureComponent.MODEL_X_POSITION);
      cpx.setValueAndUnit(location.getPosition().getX(), LengthProperty.Unit.MM);

      CoordinateProperty cpy = (CoordinateProperty) getModel()
          .getProperty(AbstractFigureComponent.MODEL_Y_POSITION);
      cpy.setValueAndUnit(location.getPosition().getY(), LengthProperty.Unit.MM);

      // Type
      LocationTypeProperty pType
          = (LocationTypeProperty) getModel().getProperty(LocationModel.TYPE);
      pType.setValue(location.getType().getName());

      // Misc properties
      updateMiscModelProperties(location);
      // look for label and symbol
      KeyValueSetProperty miscellaneous = (KeyValueSetProperty) getModel()
          .getProperty(ModelComponent.MISCELLANEOUS);
      updateRepresentation(miscellaneous);
      if (layoutElement != null) {
        updateModelLayoutElements(layoutElement);
      }
    }
    catch (CredentialsException | IllegalArgumentException e) {
      log.warn("", e);
    }
  }

  @Override  // OpenTCSProcessAdapter
  public void updateProcessProperties(Kernel kernel, PlantModelCache plantModel) {
    LocationType locType = plantModel.getLocationTypes().get(getModel().getLocationType().getName());
    Location location = kernel.createLocation(locType.getReference());
    TCSObjectReference<Location> reference = location.getReference();

    StringProperty pName = (StringProperty) getModel().getProperty(
        ModelComponent.NAME);
    String name = pName.getText();

    try {
      // Name
      kernel.renameTCSObject(reference, name);
      pName.unmarkChanged();
      updateProcessPosition(kernel, reference);

      // Write new position into the model layout element
      for (VisualLayout layout : plantModel.getVisualLayouts()) {
        updateLayoutElement(layout, reference);
      }

      updateMiscProcessProperties(kernel, reference);
      
      plantModel.getLocations().put(name, location);
    }
    catch (KernelRuntimeException e) {
      log.warn("", e);
    }
  }

  private void updateRepresentation(KeyValueSetProperty miscellaneous) {
    for (KeyValueProperty kvp : miscellaneous.getItems()) {
      switch (kvp.getKey()) {
////          case LocationModel.LABEL:
////            StringProperty pLabel = (StringProperty) getModel().getProperty(LocationModel.LABEL);
////            pLabel.setText(value);
////            pLabel.unmarkChanged();          
////            break;

        case ObjectPropConstants.LOC_DEFAULT_REPRESENTATION:
          SymbolProperty pSymbol = (SymbolProperty) getModel()
              .getProperty(ObjectPropConstants.LOC_DEFAULT_REPRESENTATION);
          pSymbol.setLocationRepresentation(
              LocationRepresentation.valueOf(kvp.getValue()));
          break;
        default:
      }
    }
  }

  private void updateModelLayoutElements(ModelLayoutElement layoutElement) {
    Map<String, String> properties = layoutElement.getProperties();
    // Save the properties of the kernel object in the model
    StringProperty sp
        = (StringProperty) getModel().getProperty(ElementPropKeys.LOC_POS_X);
    sp.setText(properties.get(ElementPropKeys.LOC_POS_X));

    sp = (StringProperty) getModel().getProperty(ElementPropKeys.LOC_POS_Y);
    sp.setText(properties.get(ElementPropKeys.LOC_POS_Y));

    sp = (StringProperty) getModel()
        .getProperty(ElementPropKeys.LOC_LABEL_OFFSET_X);
    sp.setText(properties.get(ElementPropKeys.LOC_LABEL_OFFSET_X));

    sp = (StringProperty) getModel()
        .getProperty(ElementPropKeys.LOC_LABEL_OFFSET_Y);
    sp.setText(properties.get(ElementPropKeys.LOC_LABEL_OFFSET_Y));

    sp = (StringProperty) getModel()
        .getProperty(ElementPropKeys.LOC_LABEL_ORIENTATION_ANGLE);
    sp.setText(properties.get(ElementPropKeys.LOC_LABEL_ORIENTATION_ANGLE));
  }

  private void updateProcessPosition(Kernel kernel,
                                     TCSObjectReference<Location> reference)
      throws KernelRuntimeException {
    CoordinateProperty cpx = (CoordinateProperty) getModel().getProperty(
        LocationModel.MODEL_X_POSITION);
    CoordinateProperty cpy = (CoordinateProperty) getModel().getProperty(
        LocationModel.MODEL_Y_POSITION);

    kernel.setLocationPosition(reference, convertToTriple(cpx, cpy));
    cpx.unmarkChanged();
    cpy.unmarkChanged();
  }

  @Override  // OpenTCSProcessAdapter
  protected void updateMiscProcessProperties(Kernel kernel,
                                             TCSObjectReference<?> ref)
      throws KernelRuntimeException {
    kernel.clearTCSObjectProperties(ref);
    KeyValueSetProperty pMisc = (KeyValueSetProperty) getModel().getProperty(
        ModelComponent.MISCELLANEOUS);

    if (pMisc != null) {
      // file for the symbol
      SymbolProperty pSymbol = (SymbolProperty) getModel().getProperty(
          ObjectPropConstants.LOC_DEFAULT_REPRESENTATION);
      LocationRepresentation locationRepresentation = pSymbol
          .getLocationRepresentation();

      if (locationRepresentation != null) {
        KeyValueProperty kvp = new KeyValueProperty(getModel(),
                                                    ObjectPropConstants.LOC_DEFAULT_REPRESENTATION,
                                                    locationRepresentation
                                                    .name());
        pMisc.addItem(kvp);
      }
      else {
        for (KeyValueProperty kvp : pMisc.getItems()) {
          if (kvp.getKey()
              .equals(ObjectPropConstants.LOC_DEFAULT_REPRESENTATION)) {
            pMisc.removeItem(kvp);
            break;
          }
        }
      }

      for (KeyValueProperty kvp : pMisc.getItems()) {
        kernel.setTCSObjectProperty(ref, kvp.getKey(), kvp.getValue());
      }
    }
  }

  private Triple convertToTriple(CoordinateProperty cpx, CoordinateProperty cpy) {
    Triple result = new Triple();
    result.setX((int) cpx.getValueByUnit(LengthProperty.Unit.MM));
    result.setY((int) cpy.getValueByUnit(LengthProperty.Unit.MM));

    return result;
  }

  /**
   * Refreshes the properties of the layout element and saves it in the kernel.
   *
   * @param layout The VisualLayout.
   */
  private void updateLayoutElement(VisualLayout layout,
                                   TCSObjectReference<?> ref) {

    LabeledLocationFigure llf = getModel().getFigure();
    LocationFigure lf = llf.getPresentationFigure();
    double scaleX = layout.getScaleX();
    double scaleY = layout.getScaleY();
    int xPos = (int) ((lf.getBounds().x + lf.getBounds().width / 2) * scaleX);
    int yPos = (int) -((lf.getBounds().y + lf.getBounds().height / 2) * scaleY);
    TCSLabelFigure label = llf.getLabel();
    Point2D.Double offset = label.getOffset();

    ModelLayoutElement layoutElement = new ModelLayoutElement(ref);

    layoutElement.getProperties().put(ElementPropKeys.LOC_POS_X, xPos + "");
    layoutElement.getProperties().put(ElementPropKeys.LOC_POS_Y, yPos + "");
    layoutElement.getProperties().put(ElementPropKeys.LOC_LABEL_OFFSET_X, (int) offset.x + "");
    layoutElement.getProperties().put(ElementPropKeys.LOC_LABEL_OFFSET_Y, (int) offset.y + "");
    // TODO:
//    layoutProperties.put(ElementPropKeys.LOC_LABEL_ORIENTATION_ANGLE, ...);

    layout.getLayoutElements().add(layoutElement);
  }
}
