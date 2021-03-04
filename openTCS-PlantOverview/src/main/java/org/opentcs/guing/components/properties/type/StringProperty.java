/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.properties.type;

import static java.util.Objects.requireNonNull;
import org.opentcs.guing.model.ModelComponent;

/**
 * Ein Property für einen String.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class StringProperty
    extends AbstractProperty {

  /**
   * Der String.
   */
  private String fText;

  /**
   * Creates a new instance of StringProperty
   *
   * @param model
   */
  public StringProperty(ModelComponent model) {
    this(model, "");
  }

  /**
   * Konstruktor, dem der String übergeben wird.
   *
   * @param model
   * @param text
   */
  public StringProperty(ModelComponent model, String text) {
    super(model);
    fText = requireNonNull(text, "text");
  }

  @Override
  public Object getComparableValue() {
    return fText;
  }

  /**
   * Setzt den String.
   *
   * @param text
   */
  public void setText(String text) {
    fText = text;
  }

  /**
   * Liefert den String.
   *
   * @return
   */
  public String getText() {
    return fText;
  }

  @Override
  public String toString() {
    return fText;
  }

  @Override
  public void copyFrom(Property property) {
    StringProperty stringProperty = (StringProperty) property;
    setText(stringProperty.getText());
  }
}
