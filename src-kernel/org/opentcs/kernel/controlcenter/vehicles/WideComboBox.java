/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.controlcenter.vehicles;

import java.awt.Dimension;
import java.util.Observable;
import java.util.Observer;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import org.opentcs.drivers.CommunicationAdapterFactory;

/**
 * A wider combo box that can be used as a cell editor in tables.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 * @param <E> The type of this combo box's elements.
 */
final class WideComboBox<E>
    extends JComboBox<E>
    implements Observer {

  /**
   * Indicates whether we are currently layout out or not.
   */
  private boolean layingOut;
  
  /**
   * Creates an empty WideComboBox.
   */
  public WideComboBox() {
  }

  /**
   * Creates a new WideComboBox containing the given items.
   * 
   * @param items The values in the new combo box.
   */
  public WideComboBox(final E[] items) {
    super(items);
  }

  /**
   * Creates a new WideComboBox with the given model.
   * 
   * @param aModel The model this combo box should use.
   */
  public WideComboBox(ComboBoxModel<E> aModel) {
    super(aModel);
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
      dim.width = Math.max(dim.width, getPreferredSize().width);
    }
    return dim;
  }

  @Override
  public void update(Observable o, Object arg) {
    if(arg instanceof CommunicationAdapterFactory) {
      getModel().setSelectedItem(arg);      
    }
  }
}
