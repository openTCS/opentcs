/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.base.model.elements;

import java.util.HashMap;
import java.util.ResourceBundle;
import static org.opentcs.guing.base.I18nPlantOverviewBase.BUNDLE_PATH;
import org.opentcs.guing.base.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.base.components.properties.type.LayerGroupsProperty;
import org.opentcs.guing.base.components.properties.type.LayerWrappersProperty;
import org.opentcs.guing.base.components.properties.type.LengthProperty;
import org.opentcs.guing.base.components.properties.type.StringProperty;
import org.opentcs.guing.base.model.CompositeModelComponent;

/**
 * Basic implementation of a layout.
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class LayoutModel
    extends CompositeModelComponent {

  public static final String SCALE_X = "scaleX";
  public static final String SCALE_Y = "scaleY";
  public static final String LAYERS_WRAPPERS = "layerWrappers";
  public static final String LAYER_GROUPS = "layerGroups";
  /**
   * This class's resource bundle.
   */
  private final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_PATH);

  /**
   * Creates a new instance.
   */
  public LayoutModel() {
    super(ResourceBundle.getBundle(BUNDLE_PATH).getString("layoutModel.treeViewName"));
    createProperties();
  }

  @Override // AbstractModelComponent
  public String getDescription() {
    return bundle.getString("layoutModel.description");
  }

  public LengthProperty getPropertyScaleX() {
    return (LengthProperty) getProperty(SCALE_X);
  }

  public LengthProperty getPropertyScaleY() {
    return (LengthProperty) getProperty(SCALE_Y);
  }

  public KeyValueSetProperty getPropertyMiscellaneous() {
    return (KeyValueSetProperty) getProperty(MISCELLANEOUS);
  }

  public LayerWrappersProperty getPropertyLayerWrappers() {
    return (LayerWrappersProperty) getProperty(LAYERS_WRAPPERS);
  }

  public LayerGroupsProperty getPropertyLayerGroups() {
    return (LayerGroupsProperty) getProperty(LAYER_GROUPS);
  }

  private void createProperties() {
    StringProperty pName = new StringProperty(this);
    pName.setDescription(bundle.getString("layoutModel.property_name.description"));
    pName.setHelptext(bundle.getString("layoutModel.property_name.helptext"));
    setProperty(NAME, pName);

    LengthProperty pScaleX = new LengthProperty(this);
    pScaleX.setDescription(bundle.getString("layoutModel.property_scaleX.description"));
    pScaleX.setHelptext(bundle.getString("layoutModel.property_scaleX.helptext"));
    setProperty(SCALE_X, pScaleX);

    LengthProperty pScaleY = new LengthProperty(this);
    pScaleY.setDescription(bundle.getString("layoutModel.property_scaleY.description"));
    pScaleY.setHelptext(bundle.getString("layoutModel.property_scaleY.helptext"));
    setProperty(SCALE_Y, pScaleY);

    KeyValueSetProperty pMiscellaneous = new KeyValueSetProperty(this);
    pMiscellaneous.setDescription(bundle.getString("layoutModel.property_miscellaneous.description"));
    pMiscellaneous.setHelptext(bundle.getString("layoutModel.property_miscellaneous.helptext"));
    pMiscellaneous.setOperatingEditable(true);
    setProperty(MISCELLANEOUS, pMiscellaneous);

    LayerWrappersProperty pLayerWrappers = new LayerWrappersProperty(this, new HashMap<>());
    pLayerWrappers.setDescription(bundle.getString("layoutModel.property_layerWrappers.description"));
    pLayerWrappers.setHelptext(bundle.getString("layoutModel.property_layerWrappers.helptext"));
    pLayerWrappers.setModellingEditable(false);
    setProperty(LAYERS_WRAPPERS, pLayerWrappers);

    LayerGroupsProperty pLayerGroups = new LayerGroupsProperty(this, new HashMap<>());
    pLayerGroups.setDescription(bundle.getString("layoutModel.property_layerGroups.description"));
    pLayerGroups.setHelptext(bundle.getString("layoutModel.property_layerGroups.helptext"));
    pLayerGroups.setModellingEditable(false);
    setProperty(LAYER_GROUPS, pLayerGroups);
  }
}
