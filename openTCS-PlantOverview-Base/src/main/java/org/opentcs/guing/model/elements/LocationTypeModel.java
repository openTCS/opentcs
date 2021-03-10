/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.model.elements;

import java.util.ResourceBundle;
import org.opentcs.data.ObjectPropConstants;
import static org.opentcs.guing.I18nPlantOverviewBase.BUNDLE_PATH;
import org.opentcs.guing.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.components.properties.type.StringSetProperty;
import org.opentcs.guing.components.properties.type.SymbolProperty;
import org.opentcs.guing.model.AbstractModelComponent;

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
   * This class's resource bundle.
   */
  private final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_PATH);

  /**
   * Creates a new instance.
   */
  public LocationTypeModel() {
    createProperties();
  }

  @Override
  public String getDescription() {
    return bundle.getString("locationType.description");
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
    StringProperty pName = new StringProperty(this);
    pName.setDescription(bundle.getString("locationType.name.text"));
    pName.setHelptext(bundle.getString("locationType.name.helptext"));
    setProperty(NAME, pName);

    StringSetProperty pOperations = new StringSetProperty(this);
    pOperations.setDescription(bundle.getString("locationType.allowedOperations.text"));
    pOperations.setHelptext(bundle.getString("locationType.allowedOperations.helptext"));
    setProperty(ALLOWED_OPERATIONS, pOperations);

    SymbolProperty pSymbol = new SymbolProperty(this);
    pSymbol.setDescription(bundle.getString("locationType.symbol.text"));
    pSymbol.setHelptext(bundle.getString("locationType.symbol.helptext"));
    setProperty(ObjectPropConstants.LOCTYPE_DEFAULT_REPRESENTATION, pSymbol);

    KeyValueSetProperty pMiscellaneous = new KeyValueSetProperty(this);
    pMiscellaneous.setDescription(bundle.getString("locationType.miscellaneous.text"));
    pMiscellaneous.setHelptext(bundle.getString("locationType.miscellaneous.helptext"));
    setProperty(MISCELLANEOUS, pMiscellaneous);
  }
}
