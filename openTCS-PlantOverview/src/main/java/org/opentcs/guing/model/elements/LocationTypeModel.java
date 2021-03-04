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

import org.opentcs.data.ObjectPropConstants;
import org.opentcs.guing.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.components.properties.type.StringSetProperty;
import org.opentcs.guing.components.properties.type.SymbolProperty;
import org.opentcs.guing.model.AbstractModelComponent;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Basic implementation of a location type.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class LocationTypeModel
    extends AbstractModelComponent {

  /**
   * The key for the possible actions on this type.
   */
  public static final String ALLOWED_OPERATIONS = "AllowedOperations";

  /**
   * Creates a new instance.
   */
  public LocationTypeModel() {
    createProperties();
  }

  @Override
  public String getDescription() {
    return ResourceBundleUtil.getBundle().getString("locationType.description");
  }

  @Override
  public String getTreeViewName() {
    String treeViewName = getDescription() + " " + getName();

    return treeViewName;
  }
  
  public StringSetProperty getPropertyAllowedOperations() {
    return (StringSetProperty) getProperty(ALLOWED_OPERATIONS);
  }

  public KeyValueSetProperty getPropertyMiscellaneous() {
    return (KeyValueSetProperty) getProperty(MISCELLANEOUS);
  }
  
  public SymbolProperty getPropertyDefaultRepresentation() {
    return (SymbolProperty) getProperty(ObjectPropConstants.LOCTYPE_DEFAULT_REPRESENTATION);
  }

  private void createProperties() {
    ResourceBundleUtil r = ResourceBundleUtil.getBundle();

    StringProperty pName = new StringProperty(this);
    pName.setDescription(r.getString("locationType.name.text"));
    pName.setHelptext(r.getString("locationType.name.helptext"));
    setProperty(NAME, pName);

    StringSetProperty pOperations = new StringSetProperty(this);
    pOperations.setDescription(r.getString("locationType.allowedOperations.text"));
    pOperations.setHelptext(r.getString("locationType.allowedOperations.helptext"));
    setProperty(ALLOWED_OPERATIONS, pOperations);

    SymbolProperty pSymbol = new SymbolProperty(this);
    pSymbol.setDescription(r.getString("locationType.symbol.text"));
    pSymbol.setHelptext(r.getString("locationType.symbol.helptext"));
    setProperty(ObjectPropConstants.LOCTYPE_DEFAULT_REPRESENTATION, pSymbol);

    KeyValueSetProperty pMiscellaneous = new KeyValueSetProperty(this);
    pMiscellaneous.setDescription(r.getString("locationType.miscellaneous.text"));
    pMiscellaneous.setHelptext(r.getString("locationType.miscellaneous.helptext"));
    setProperty(MISCELLANEOUS, pMiscellaneous);
  }
}
