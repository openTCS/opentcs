/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.exchange.adapter;

import java.awt.geom.Point2D;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectPropConstants;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.data.model.visualization.LayoutElement;
import org.opentcs.data.model.visualization.LocationRepresentation;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.guing.components.drawing.figures.LabeledLocationFigure;
import org.opentcs.guing.components.drawing.figures.LocationFigure;
import org.opentcs.guing.components.drawing.figures.TCSLabelFigure;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.type.CoordinateProperty;
import org.opentcs.guing.components.properties.type.KeyValueProperty;
import org.opentcs.guing.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.components.properties.type.SelectionProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.components.properties.type.SymbolProperty;
import org.opentcs.guing.exchange.OpenTCSEventDispatcher;
import org.opentcs.guing.model.AbstractFigureComponent;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.LocationTypeModel;

/**
 * An adapter for locations.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class LocationAdapter
    extends OpenTCSProcessAdapter {

  /**
   * This class's logger.
   */
  private static final Logger log
      = Logger.getLogger(LocationAdapter.class.getName());

  /**
   * Creates a new instance.
   */
  public LocationAdapter() {
    super();
  }

  @Override
  @SuppressWarnings("unchecked")
  public TCSObjectReference<Location> getProcessObject() {
    return (TCSObjectReference<Location>) super.getProcessObject();
  }

  @Override
  public LocationModel getModel() {
    return (LocationModel) super.getModel();
  }

  @Override
  public void setModel(ModelComponent model) {
    if (!LocationModel.class.isInstance(model)) {
      throw new IllegalArgumentException(model + " is not a LocationModel");
    }
    super.setModel(model);
  }

  @Override	// AbstractProcessAdapter
  public Location createProcessObject() throws KernelRuntimeException {
    if (!hasModelingState()) {
      return null;
    }
    OpenTCSEventDispatcher dispatcher = (OpenTCSEventDispatcher) getEventDispatcher();
    LocationTypeAdapter adapter = (LocationTypeAdapter) dispatcher.findProcessAdapter(getModel().getLocationType());
    // At first the default type of the adapter is assigned to the location type
    Location location = kernel().createLocation(adapter.getProcessObject());
    setProcessObject(location.getReference());
    nameToModel(location);
    register();

    return location;
  }

  @Override	// AbstractProcessAdapter
  public void releaseProcessObject() {
    try {
      releaseLayoutElement();
      kernel().removeTCSObject(getProcessObject());
      super.releaseProcessObject();
    }
    catch (KernelRuntimeException e) {
      log.log(Level.WARNING, null, e);
    }
  }

  @Override	// OpenTCSProcessAdapter
  public void setLayoutElement(ModelLayoutElement layoutElement) {
    fLayoutElement = layoutElement;
  }

  @Override	// OpenTCSProcessAdapter
  public void propertiesChanged(AttributesChangeEvent event) {
    if (hasModelingState() && event.getInitiator() != this) {
      updateProcessProperties(false);
    }
  }

  @Override	// OpenTCSProcessAdapter
  public void updateModelProperties() {
    TCSObjectReference<Location> reference = getProcessObject();

    synchronized (reference) {
      try {
        Location location = kernel().getTCSObject(Location.class, reference);

        if (location == null) {
          return;
        }
        // Name 
        StringProperty pName = (StringProperty) getModel().getProperty(ModelComponent.NAME);
        pName.setText(location.getName());

        // Position in model
        CoordinateProperty cpx = (CoordinateProperty) getModel().getProperty(AbstractFigureComponent.MODEL_X_POSITION);
        cpx.setValueAndUnit(location.getPosition().getX(), LengthProperty.Unit.MM);

        CoordinateProperty cpy = (CoordinateProperty) getModel().getProperty(AbstractFigureComponent.MODEL_Y_POSITION);
        cpy.setValueAndUnit(location.getPosition().getY(), LengthProperty.Unit.MM);

        // Type
        SelectionProperty pType = (SelectionProperty) getModel().getProperty(LocationModel.TYPE);
        pType.setValue(location.getType().getName());

        // Misc properties
        updateMiscModelProperties(location);
        // look for label and symbol
        KeyValueSetProperty miscellaneous = (KeyValueSetProperty) getModel().getProperty(ModelComponent.MISCELLANEOUS);
        updateRepresentation(miscellaneous);
        updateModelLayoutElements();
      }
      catch (CredentialsException | IllegalArgumentException e) {
        log.log(Level.WARNING, null, e);
      }
    }
  }

  private void updateRepresentation(KeyValueSetProperty miscellaneous) {
    for (KeyValueProperty kvp : miscellaneous.getItems()) {
      switch (kvp.getKey()) {
////					case LocationModel.LABEL:
////						StringProperty pLabel = (StringProperty) getModel().getProperty(LocationModel.LABEL);
////						pLabel.setText(value);
////						pLabel.unmarkChanged();					
////						break;

        case ObjectPropConstants.LOC_DEFAULT_REPRESENTATION:
          SymbolProperty pSymbol = (SymbolProperty) getModel().getProperty(ObjectPropConstants.LOC_DEFAULT_REPRESENTATION);
          pSymbol.setLocationRepresentation(LocationRepresentation.valueOf(kvp.getValue()));
          break;
      }
    }
  }

  private void updateModelLayoutElements() {
    if (fLayoutElement != null) {
      Map<String, String> properties = fLayoutElement.getProperties();
      // Save the properties of the kernel object in the model
      StringProperty sp = (StringProperty) getModel().getProperty(ElementPropKeys.LOC_POS_X);
      sp.setText(properties.get(ElementPropKeys.LOC_POS_X));

      sp = (StringProperty) getModel().getProperty(ElementPropKeys.LOC_POS_Y);
      sp.setText(properties.get(ElementPropKeys.LOC_POS_Y));

      sp = (StringProperty) getModel().getProperty(ElementPropKeys.LOC_LABEL_OFFSET_X);
      sp.setText(properties.get(ElementPropKeys.LOC_LABEL_OFFSET_X));

      sp = (StringProperty) getModel().getProperty(ElementPropKeys.LOC_LABEL_OFFSET_Y);
      sp.setText(properties.get(ElementPropKeys.LOC_LABEL_OFFSET_Y));

      sp = (StringProperty) getModel().getProperty(ElementPropKeys.LOC_LABEL_ORIENTATION_ANGLE);
      sp.setText(properties.get(ElementPropKeys.LOC_LABEL_ORIENTATION_ANGLE));
    }
  }

  @Override	// OpenTCSProcessAdapter
  public void updateProcessProperties(boolean updateAllProperties) {
    super.updateProcessProperties(updateAllProperties);
    TCSObjectReference<Location> reference = getProcessObject();

    if (isInTransition()) {
      return;
    }

    synchronized (reference) {
      StringProperty pName = (StringProperty) getModel().getProperty(ModelComponent.NAME);
      String name = pName.getText();

      try {
        // Name
        if (updateAllProperties || pName.hasChanged()) {
          kernel().renameTCSObject(reference, name);
          pName.unmarkChanged();
        }
        updateProcessPosition(updateAllProperties, reference);

        // Location-Type
        LocationTypeModel locationType = getModel().getLocationType();
        LocationTypeAdapter adapter = (LocationTypeAdapter) getEventDispatcher().findProcessAdapter(locationType);
        kernel().setLocationType(reference, adapter.getProcessObject());

        // Write new position into the model layout element
        Set<VisualLayout> layouts = kernel().getTCSObjects(VisualLayout.class);

        for (VisualLayout layout : layouts) {
          updateLayoutElement(layout, updateAllProperties);
        }

        updateMiscProcessProperties(updateAllProperties);
      }
      catch (ObjectExistsException e) {
        undo(name, e);
      }
      catch (CredentialsException | ObjectUnknownException e) {
        log.log(Level.WARNING, null, e);
      }
    }
  }

  private void updateProcessPosition(boolean updateAllProperties,
                                     TCSObjectReference<Location> reference)
      throws ObjectUnknownException, CredentialsException {
    CoordinateProperty cpx = (CoordinateProperty) getModel().getProperty(LocationModel.MODEL_X_POSITION);
    CoordinateProperty cpy = (CoordinateProperty) getModel().getProperty(LocationModel.MODEL_Y_POSITION);

    if (updateAllProperties || cpx.hasChanged() || cpy.hasChanged()) {
      kernel().setLocationPosition(reference, convertToTriple(cpx, cpy));
      cpx.unmarkChanged();
      cpy.unmarkChanged();
    }
  }

  @Override	// OpenTCSProcessAdapter
  protected void updateMiscProcessProperties(boolean updateAllProperties)
      throws ObjectUnknownException, CredentialsException {
    kernel().clearTCSObjectProperties(getProcessObject());
    KeyValueSetProperty pMisc = (KeyValueSetProperty) getModel().getProperty(ModelComponent.MISCELLANEOUS);

    if (pMisc != null) {
      // file for the symbol
      SymbolProperty pSymbol = (SymbolProperty) getModel().getProperty(ObjectPropConstants.LOC_DEFAULT_REPRESENTATION);
      LocationRepresentation locationRepresentation = pSymbol.getLocationRepresentation();

      if (locationRepresentation != null) {
        KeyValueProperty kvp = new KeyValueProperty(getModel(), ObjectPropConstants.LOC_DEFAULT_REPRESENTATION, locationRepresentation.name());
        pMisc.addItem(kvp);
      }
      else {
        for (KeyValueProperty kvp : pMisc.getItems()) {
          if (kvp.getKey().equals(ObjectPropConstants.LOC_DEFAULT_REPRESENTATION)) {
            pMisc.removeItem(kvp);
            break;
          }
        }
      }

      for (KeyValueProperty kvp : pMisc.getItems()) {
        kernel().setTCSObjectProperty(getProcessObject(), kvp.getKey(), kvp.getValue());
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
  private void updateLayoutElement(VisualLayout layout, boolean updateAllProperties) {
    StringProperty spx = (StringProperty) getModel().getProperty(ElementPropKeys.LOC_POS_X);
    StringProperty spy = (StringProperty) getModel().getProperty(ElementPropKeys.LOC_POS_Y);
    StringProperty splox = (StringProperty) getModel().getProperty(ElementPropKeys.LOC_LABEL_OFFSET_X);
    StringProperty sploy = (StringProperty) getModel().getProperty(ElementPropKeys.LOC_LABEL_OFFSET_Y);
    StringProperty sploa = (StringProperty) getModel().getProperty(ElementPropKeys.LOC_LABEL_ORIENTATION_ANGLE);

    if (updateAllProperties || spx.hasChanged() || spy.hasChanged() || splox.hasChanged() || sploy.hasChanged() || sploa.hasChanged()) {
      LabeledLocationFigure llf = getModel().getFigure();
      LocationFigure lf = (LocationFigure) llf.getPresentationFigure();
      double scaleX = layout.getScaleX();
      double scaleY = layout.getScaleY();
      int xPos = (int) ((lf.getBounds().x + lf.getBounds().width / 2) * scaleX);
      int yPos = (int) -((lf.getBounds().y + lf.getBounds().height / 2) * scaleY);
      TCSLabelFigure label = llf.getLabel();
      Point2D.Double offset = label.getOffset();

      if (fLayoutElement == null) {
        fLayoutElement = new ModelLayoutElement(getProcessObject());
      }

      Map<String, String> layoutProperties = fLayoutElement.getProperties();
      layoutProperties.put(ElementPropKeys.LOC_POS_X, xPos + "");
      layoutProperties.put(ElementPropKeys.LOC_POS_Y, yPos + "");
      layoutProperties.put(ElementPropKeys.LOC_LABEL_OFFSET_X, (int) offset.x + "");
      layoutProperties.put(ElementPropKeys.LOC_LABEL_OFFSET_Y, (int) offset.y + "");
      // TODO:
//		layoutProperties.put(ElementPropKeys.LOC_LABEL_ORIENTATION_ANGLE, ...);
      fLayoutElement.setProperties(layoutProperties);

      Set<LayoutElement> layoutElements = layout.getLayoutElements();

      for (LayoutElement element : layoutElements) {
        ModelLayoutElement mle = (ModelLayoutElement) element;

        if (mle.getVisualizedObject().getId() == fLayoutElement.getVisualizedObject().getId()) {
          layoutElements.remove(element);
          break;
        }
      }

      layoutElements.add(fLayoutElement);
      kernel().setVisualLayoutElements(layout.getReference(), layoutElements);
    }
  }
}
