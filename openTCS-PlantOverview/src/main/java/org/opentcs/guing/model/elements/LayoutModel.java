/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.model.elements;

import org.opentcs.guing.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.model.CompositeModelComponent;
import org.opentcs.guing.util.ResourceBundleUtil;

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

  /**
   * Creates a new instance.
   */
  public LayoutModel() {
    super(ResourceBundleUtil.getBundle().getString("tree.layout.text"));
    createProperties();
  }

  @Override // AbstractModelComponent
  public String getDescription() {
    return ResourceBundleUtil.getBundle().getString("layout.description");
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

  private void createProperties() {
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();

    StringProperty pName = new StringProperty(this);
    pName.setDescription(bundle.getString("layout.name.text"));
    pName.setHelptext(bundle.getString("layout.name.helptext"));
    setProperty(NAME, pName);

    LengthProperty pScaleX = new LengthProperty(this);
    pScaleX.setDescription(bundle.getString("layout.scaleX.text"));
    pScaleX.setHelptext(bundle.getString("layout.scaleX.helptext"));
    setProperty(SCALE_X, pScaleX);

    LengthProperty pScaleY = new LengthProperty(this);
    pScaleY.setDescription(bundle.getString("layout.scaleY.text"));
    pScaleY.setHelptext(bundle.getString("layout.scaleY.helptext"));
    setProperty(SCALE_Y, pScaleY);

    KeyValueSetProperty pMiscellaneous = new KeyValueSetProperty(this);
    pMiscellaneous.setDescription(bundle.getString("layout.miscellaneous.text"));
    pMiscellaneous.setHelptext(bundle.getString("layout.miscellaneous.helptext"));
    setProperty(MISCELLANEOUS, pMiscellaneous);
  }
}
