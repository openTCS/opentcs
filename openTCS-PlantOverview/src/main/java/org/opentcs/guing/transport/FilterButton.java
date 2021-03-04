/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.transport;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;

/**
 * A button for filtering transport orders..
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class FilterButton
    extends JToggleButton {

  /**
   * Das TableModel, das die Filterung vornimmt.
   */
  private final FilterTableModel fTableModel;
  /**
   * Der Filter.
   */
  private final Object fFilter;

  /**
   * Creates a new instance.
   *
   * @param icon The image that the button should display
   * @param tableModel The table model to be filtered
   * @param filter The actual filter
   */
  public FilterButton(ImageIcon icon, FilterTableModel tableModel, Object filter) {
    super(icon);
    fTableModel = tableModel;
    fFilter = filter;

    addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        changed();
      }
    });

    setSelected(true);
  }

  /**
   * Called when the button has changed.
   */
  private void changed() {
    if (isSelected()) {
      fTableModel.removeFilter(fFilter);
    }
    else {
      fTableModel.addFilter(fFilter);
    }
  }
}
