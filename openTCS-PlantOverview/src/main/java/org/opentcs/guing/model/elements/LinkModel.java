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

import org.opentcs.guing.components.properties.type.LinkActionsProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.components.properties.type.StringSetProperty;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Basic implementation of link between a point and a location.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class LinkModel
    extends AbstractConnection {

  /**
   * The key for the possible actions on the location.
   */
  public static final String ALLOWED_OPERATIONS = "AllowedOperations";

  /**
   * Creates a new instance.
   */
  public LinkModel() {
    createProperties();
  }

  /**
   * Returns the connected point.
   *
   * @return The model of the connected Point.
   */
  public PointModel getPoint() {
    if (getStartComponent() instanceof PointModel) {
      return (PointModel) getStartComponent();
    }

    if (getEndComponent() instanceof PointModel) {
      return (PointModel) getEndComponent();
    }

    return null;
  }

  /**
   * Returns the connected location.
   *
   * @return The model of the connected Location.
   */
  public LocationModel getLocation() {
    if (getStartComponent() instanceof LocationModel) {
      return (LocationModel) getStartComponent();
    }

    if (getEndComponent() instanceof LocationModel) {
      return (LocationModel) getEndComponent();
    }

    return null;
  }

  @Override // AbstractModelComponent
  public String getDescription() {
    return ResourceBundleUtil.getBundle().getString("link.description");
  }

  public StringSetProperty getPropertyAllowedOperations() {
    return (StringSetProperty) getProperty(ALLOWED_OPERATIONS);
  }

  private void createProperties() {
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();

    StringProperty pName = new StringProperty(this);
    pName.setDescription(bundle.getString("link.name.text"));
    pName.setHelptext(bundle.getString("link.name.helptext"));
    // The name of a link cannot be changed because it is not stored in the kernel model
    pName.setModellingEditable(false);
    setProperty(NAME, pName);

    StringSetProperty pOperations = new LinkActionsProperty(this);
    pOperations.setDescription(bundle.getString("link.action.text"));
    pOperations.setHelptext(bundle.getString("link.action.helptext"));
    setProperty(ALLOWED_OPERATIONS, pOperations);

    StringProperty startComponent = new StringProperty(this);
    startComponent.setDescription(bundle.getString("element.startComponent.text"));
    startComponent.setModellingEditable(false);
    startComponent.setOperatingEditable(false);
    setProperty(START_COMPONENT, startComponent);

    StringProperty endComponent = new StringProperty(this);
    endComponent.setDescription(bundle.getString("element.endComponent.text"));
    endComponent.setModellingEditable(false);
    endComponent.setOperatingEditable(false);
    setProperty(END_COMPONENT, endComponent);
  }
}
