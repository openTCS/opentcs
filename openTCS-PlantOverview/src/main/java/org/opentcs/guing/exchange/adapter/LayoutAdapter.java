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
import static java.util.Objects.requireNonNull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.opentcs.access.Kernel;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.data.ObjectPropConstants;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.guing.components.properties.type.KeyValueProperty;
import org.opentcs.guing.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.components.properties.type.LocationThemeProperty;
import org.opentcs.guing.components.properties.type.ModelAttribute;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.components.properties.type.VehicleThemeProperty;
import org.opentcs.guing.exchange.EventDispatcher;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.LayoutModel;
import org.opentcs.guing.plugins.themes.StandardLocationTheme;
import org.opentcs.guing.plugins.themes.StandardVehicleTheme;
import org.opentcs.guing.util.LocationThemeManager;
import org.opentcs.guing.util.VehicleThemeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An adapter for VisualLayout instances.
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class LayoutAdapter
    extends AbstractProcessAdapter {

  /**
   * This class's logger.
   */
  private static final Logger log
      = LoggerFactory.getLogger(LayoutAdapter.class);
  /**
   * Manages the location themes.
   */
  private final LocationThemeManager locationThemeManager;
  /**
   * Manages the vehicle themes.
   */
  private final VehicleThemeManager vehicleThemeManager;

  /**
   * Creates a new instance.
   * 
   * @param model The corresponding model comoponent.
   * @param eventDispatcher The event dispatcher.
   * @param locationThemeManager Manages the location themes.
   * @param vehicleThemeManager Manages the vehicle themes.
   */
  @Inject
  public LayoutAdapter(@Assisted LayoutModel model,
                       @Assisted EventDispatcher eventDispatcher,
                       LocationThemeManager locationThemeManager,
                       VehicleThemeManager vehicleThemeManager) {
    super(model, eventDispatcher);
    this.locationThemeManager = requireNonNull(locationThemeManager,
                                               "locationThemeManager");
    this.vehicleThemeManager = requireNonNull(vehicleThemeManager,
                                              "vehicleThemeManager");
  }

  @Override
  public LayoutModel getModel() {
    return (LayoutModel) super.getModel();
  }

  @Override // OpenTCSProcessAdapter
  public void updateModelProperties(Kernel kernel,
                                    TCSObject<?> tcsObject,
                                    @Nullable ModelLayoutElement layoutElement) {
    VisualLayout layout = requireNonNull((VisualLayout) tcsObject, "tcsObject");

    try {
      StringProperty name = (StringProperty) getModel().getProperty(ModelComponent.NAME);
      name.setText(layout.getName());
      name.markChanged();
      updateModelLengthProperty(layout);
      updateModelThemes(layout);

      updateMiscModelProperties(layout);
    }
    catch (Exception e) {
      log.warn("", e);
    }
  }

  @Override // OpenTCSProcessAdapter
  public void updateProcessProperties(Kernel kernel) {
    VisualLayout layout = kernel.createVisualLayout();
    TCSObjectReference<VisualLayout> reference = layout.getReference();

    StringProperty pName = (StringProperty) getModel().getProperty(ModelComponent.NAME);
    String name = pName.getText();

    try {
      kernel.renameTCSObject(reference, name);
      LengthProperty pScale = (LengthProperty) getModel().getProperty(LayoutModel.SCALE_X);
      double scale = pScale.getValueByUnit(LengthProperty.Unit.MM);

      kernel.setVisualLayoutScaleX(reference, scale);

      pScale = (LengthProperty) getModel().getProperty(LayoutModel.SCALE_Y);
      scale = pScale.getValueByUnit(LengthProperty.Unit.MM);

      kernel.setVisualLayoutScaleY(reference, scale);
      updateProcessThemes();

      updateMiscProcessProperties(kernel, reference);
    }
    catch (KernelRuntimeException e) {
      log.warn("", e);
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
      locationThemeManager.setThemeProperty(tp);
      tp.markChanged();
    }

    VehicleThemeProperty vtp = (VehicleThemeProperty) getModel().getProperty(LayoutModel.VEHICLE_THEME);
    themeName = layout.getProperties().get(ObjectPropConstants.VEHICLE_THEME_CLASS);

    if (themeName == null || "".equals(themeName)) {
      themeName = StandardVehicleTheme.class.getName();
    }

    if (!themeName.equals(vtp.getTheme())) {
      vtp.setTheme(themeName);
      vehicleThemeManager.setThemeProperty(vtp);
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

  private void updateProcessThemes() {

    KeyValueSetProperty misc
        = (KeyValueSetProperty) getModel().getProperty(ModelComponent.MISCELLANEOUS);

    LocationThemeProperty pLocationTheme
        = (LocationThemeProperty) getModel().getProperty(LayoutModel.LOCATION_THEME);
    if (misc != null) {
      KeyValueProperty kvp
          = new KeyValueProperty(getModel(),
                                 ObjectPropConstants.LOCATION_THEME_CLASS,
                                 pLocationTheme.getTheme());
      misc.addItem(kvp);
      kvp.setChangeState(ModelAttribute.ChangeState.CHANGED);
    }

    locationThemeManager.updateDefaultTheme();

    VehicleThemeProperty pVehicleTheme
        = (VehicleThemeProperty) getModel().getProperty(LayoutModel.VEHICLE_THEME);
    if (misc != null) {
      KeyValueProperty kvp
          = new KeyValueProperty(getModel(),
                                 ObjectPropConstants.VEHICLE_THEME_CLASS,
                                 pVehicleTheme.getTheme());
      misc.addItem(kvp);
      kvp.setChangeState(ModelAttribute.ChangeState.CHANGED);
    }

    vehicleThemeManager.updateDefaultTheme();
  }
}
