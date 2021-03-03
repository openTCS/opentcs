/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.gui;

import java.awt.Dimension;
import java.util.Optional;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import static org.opentcs.util.Assertions.checkInRange;

/**
 * A wider combo box that can be used as a cell editor in tables.
 * If no popup width is specified within the constructor, this class uses the preferred size
 * of the combo box as the popup width.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 * @author Mats Wilhelm (Fraunhofer IML)
 * @param <E> The type of this combo box's elements.
 */
public class WideComboBox<E>
    extends JComboBox<E> {

  /**
   * The popup width.
   */
  private final Optional<Integer> popupWidth;

  /**
   * Indicates whether we are currently layout out or not.
   */
  private boolean layingOut;

  /**
   * Creates a new instance.
   */
  public WideComboBox() {
    this.popupWidth = Optional.empty();
  }

  /**
   * Creates a new instance with the given popup width.
   *
   * @param popupWidth the popup width
   */
  public WideComboBox(int popupWidth) {
    checkInRange(popupWidth, 1, Integer.MAX_VALUE, "popupWidth");
    this.popupWidth = Optional.of(popupWidth);
  }

  /**
   * Creates a new instance with the given content.
   *
   * @param items The values in the new combo box.
   */
  public WideComboBox(final E[] items) {
    super(items);
    this.popupWidth = Optional.empty();
  }

  /**
   * Creates a new instance with the given model.
   *
   * @param aModel The model this combo box should use.
   */
  public WideComboBox(ComboBoxModel<E> aModel) {
    super(aModel);
    this.popupWidth = Optional.empty();
  }

  @Override
  public void doLayout() {
    try {
      layingOut = true;
      super.doLayout();
    }
    finally {
      layingOut = false;
    }
  }

  @Override
  public Dimension getSize() {
    Dimension dim = super.getSize();
    if (!layingOut) {
      dim.width = Math.max(dim.width, popupWidth.orElse(getPreferredSize().width));
    }
    return dim;
  }
}
