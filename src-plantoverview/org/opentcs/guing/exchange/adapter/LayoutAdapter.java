/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.exchange.adapter;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectPropConstants;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.type.KeyValueProperty;
import org.opentcs.guing.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.components.properties.type.LocationThemeProperty;
import org.opentcs.guing.components.properties.type.ModelAttribute;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.components.properties.type.VehicleThemeProperty;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.LayoutModel;
import org.opentcs.guing.plugins.themes.StandardLocationTheme;
import org.opentcs.guing.plugins.themes.StandardVehicleTheme;
import org.opentcs.guing.util.DefaultLocationThemeManager;
import org.opentcs.guing.util.DefaultVehicleThemeManager;

/**
 * EventManager for Visual Layouts.
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
public class LayoutAdapter
    extends OpenTCSProcessAdapter {

  /**
   * This class's logger.
   */
  private static final Logger log
      = Logger.getLogger(LayoutAdapter.class.getName());

  /**
   * Creates a new instance of LayoutAdapter.
   */
  public LayoutAdapter() {
    super();
  }

  @Override
  @SuppressWarnings("unchecked")
  public TCSObjectReference<VisualLayout> getProcessObject() {
    return (TCSObjectReference<VisualLayout>) super.getProcessObject();
  }

  @Override
  public LayoutModel getModel() {
    return (LayoutModel) super.getModel();
  }

  @Override
  public void setModel(ModelComponent model) {
    if (!LayoutModel.class.isInstance(model)) {
      throw new IllegalArgumentException(model + " is not a LayoutModel");
    }
    super.setModel(model);
  }

  @Override // AbstractProcessAdapter
  public void releaseProcessObject() {
    try {
      kernel().removeTCSObject(getProcessObject());
      super.releaseProcessObject(); // also delete the Adapter
    }
    catch (KernelRuntimeException e) {
      log.log(Level.WARNING, null, e);
    }
  }

  @Override // AbstractProcessAdapter
  public VisualLayout createProcessObject() throws KernelRuntimeException {
    if (!hasModelingState()) {
      return null;
    }

    VisualLayout visualLayout = kernel().createVisualLayout();
    setProcessObject(visualLayout.getReference());

    StringProperty pName = (StringProperty) getModel().getProperty(ModelComponent.NAME);
    pName.setText(visualLayout.getName());
    pName.markChanged();

    LengthProperty pScale = (LengthProperty) getModel().getProperty(LayoutModel.SCALE_X);
    pScale.setValueAndUnit(visualLayout.getScaleX(), LengthProperty.Unit.MM);

    pScale = (LengthProperty) getModel().getProperty(LayoutModel.SCALE_Y);
    pScale.setValueAndUnit(visualLayout.getScaleY(), LengthProperty.Unit.MM);

    getModel().propertiesChanged(this);

    register();

    return visualLayout;
  }

  @Override // OpenTCSProcessAdapter
  public void propertiesChanged(AttributesChangeEvent event) {
    if (hasModelingState() && event.getInitiator() != this) {
      updateProcessProperties(false);
    }
  }

  @Override // OpenTCSProcessAdapter
  public void updateModelProperties() {
    TCSObjectReference<VisualLayout> reference = getProcessObject();

    if (reference != null) {
      // Nur im Modelling Mode!
      synchronized (reference) {
        try {
          VisualLayout layout = kernel().getTCSObject(VisualLayout.class, reference);

          StringProperty name = (StringProperty) getModel().getProperty(ModelComponent.NAME);
          name.setText(layout.getName());
          name.markChanged();
          updateModelLengthProperty(layout);
          updateModelThemes(layout);

          updateMiscModelProperties(layout);
        }
        catch (Exception e) {
          log.log(Level.WARNING, null, e);
        }
      }
    }
  }

  private void updateModelThemes(VisualLayout layout) {
    LocationThemeProperty tp = (LocationThemeProperty) getModel().getProperty(LayoutModel.LOCATION_THEME);
    String themeName = layout.getProperties().get(ObjectPropConstants.LOCATION_THEME_CLASS);

    if (themeName == null || "".equals(themeName)) {
      themeName = StandardLocationTheme.class.getName();
    }

    if (!themeName.equals(tp.getTheme())) {
      tp.setTheme(themeName);
      DefaultLocationThemeManager.getInstance().setThemeProperty(tp);
      tp.markChanged();
    }

    VehicleThemeProperty vtp = (VehicleThemeProperty) getModel().getProperty(LayoutModel.VEHICLE_THEME);
    themeName = layout.getProperties().get(ObjectPropConstants.VEHICLE_THEME_CLASS);

    if (themeName == null || "".equals(themeName)) {
      themeName = StandardVehicleTheme.class.getName();
    }

    if (!themeName.equals(vtp.getTheme())) {
      vtp.setTheme(themeName);
      DefaultVehicleThemeManager.getInstance().setThemeProperty(vtp);
      vtp.markChanged();
    }
  }

  private void updateModelLengthProperty(VisualLayout layout) throws Exception {
    LengthProperty lp = (LengthProperty) getModel().getProperty(LayoutModel.SCALE_X);
    double scale = layout.getScaleX();
    lp.setValueAndUnit(scale, LengthProperty.Unit.MM);
    lp.markChanged();

    lp = (LengthProperty) getModel().getProperty(LayoutModel.SCALE_Y);
    scale = layout.getScaleY();
    lp.setValueAndUnit(scale, LengthProperty.Unit.MM);
    lp.markChanged();
  }

  @Override // OpenTCSProcessAdapter
  public void updateProcessProperties(boolean updateAllProperties) {
    super.updateProcessProperties(updateAllProperties);
    TCSObjectReference<VisualLayout> reference = getProcessObject();

    if (isInTransition()) {
      return;
    }

    if (reference != null) {
      synchronized (reference) {
        StringProperty pName = (StringProperty) getModel().getProperty(ModelComponent.NAME);
        String name = pName.getText();

        try {
          if (updateAllProperties || pName.hasChanged()) {
            kernel().renameTCSObject(reference, name);
          }
          LengthProperty pScale = (LengthProperty) getModel().getProperty(LayoutModel.SCALE_X);
          double scale = pScale.getValueByUnit(LengthProperty.Unit.MM);

          if (updateAllProperties || pScale.hasChanged()) {
            kernel().setVisualLayoutScaleX(reference, scale);
          }

          pScale = (LengthProperty) getModel().getProperty(LayoutModel.SCALE_Y);
          scale = pScale.getValueByUnit(LengthProperty.Unit.MM);

          if (updateAllProperties || pScale.hasChanged()) {
            kernel().setVisualLayoutScaleY(reference, scale);
          }
          updateProcessThemes(updateAllProperties);

          updateMiscProcessProperties(updateAllProperties);
        }
        catch (ObjectExistsException e) {
          undo(name, e);
        }
        catch (ObjectUnknownException | CredentialsException e) {
          log.log(Level.WARNING, null, e);
        }
      }
    }
  }

  private void updateProcessThemes(boolean updateAllProperties) {
    LocationThemeProperty pLocationTheme = (LocationThemeProperty) getModel().getProperty(LayoutModel.LOCATION_THEME);

    if (updateAllProperties || pLocationTheme.hasChanged()) {
      KeyValueSetProperty misc = (KeyValueSetProperty) getModel().getProperty(ModelComponent.MISCELLANEOUS);

      if (misc != null) {
        KeyValueProperty kvp = new KeyValueProperty(getModel(), ObjectPropConstants.LOCATION_THEME_CLASS, pLocationTheme.getTheme());
        misc.addItem(kvp);
        kvp.setChangeState(ModelAttribute.ChangeState.CHANGED);
      }

      DefaultLocationThemeManager.getInstance().updateDefaultTheme();
    }

    VehicleThemeProperty pVehicleTheme = (VehicleThemeProperty) getModel().getProperty(LayoutModel.VEHICLE_THEME);

    if (updateAllProperties || pVehicleTheme.hasChanged()) {
      KeyValueSetProperty misc = (KeyValueSetProperty) getModel().getProperty(ModelComponent.MISCELLANEOUS);

      if (misc != null) {
        KeyValueProperty kvp = new KeyValueProperty(getModel(), ObjectPropConstants.VEHICLE_THEME_CLASS, pVehicleTheme.getTheme());
        misc.addItem(kvp);
        kvp.setChangeState(ModelAttribute.ChangeState.CHANGED);
      }

      DefaultVehicleThemeManager.getInstance().updateDefaultTheme();
    }
  }
}
