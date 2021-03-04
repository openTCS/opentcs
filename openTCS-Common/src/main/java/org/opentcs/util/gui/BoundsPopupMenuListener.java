/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicComboPopup;

/**
 * Changes the bounds of a JComboBox's popup menu to allow the popup to be wieder than the combo
 * box.
 * Register it with a combo box using
 * {@link JComboBox#addPopupMenuListener(javax.swing.event.PopupMenuListener)}.
 *
 * <p>
 * This class will only work for a JComboBox that uses a BasicComboPop.
 * </p>
 * 
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class BoundsPopupMenuListener
    implements PopupMenuListener {

  /**
   * The scrollpane of the combobox.
   */
  private JScrollPane scrollPane;

  /**
   * General purpose constructor to set all popup properties at once.
   *
   */
  public BoundsPopupMenuListener() {
  }

  /**
   * Alter the bounds of the popup just before it is made visible.
   *
   * @param e The event.
   */
  @Override
  public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
    JComboBox<?> comboBox = (JComboBox) e.getSource();
    if (comboBox.getItemCount() == 0) {
      return;
    }
    final Object child = comboBox.getAccessibleContext().getAccessibleChild(0);

    if (child instanceof BasicComboPopup) {
      SwingUtilities.invokeLater(() -> customizePopup((BasicComboPopup) child));
    }
  }

  @Override
  public void popupMenuCanceled(PopupMenuEvent e) {
  }

  @Override
  public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    //  In its normal state the scrollpane does not have a scrollbar
    if (scrollPane != null) {
      scrollPane.setHorizontalScrollBar(null);
    }
  }

  protected void customizePopup(BasicComboPopup popup) {
    scrollPane = getScrollPane(popup);
    popupWider(popup);

    //  For some reason in JDK7 the popup will not display at its preferred
    //  width unless its location has been changed from its default
    //  (ie. for normal "pop down" shift the popup and reset)
    Component comboBox = popup.getInvoker();
    Point location = comboBox.getLocationOnScreen();

    int height = comboBox.getSize().height;
    popup.setLocation(location.x, location.y + height - 1);
    popup.setLocation(location.x, location.y + height);
  }

  /**
   * Adjusts the width of the scrollpane used by the popup.
   */
  protected void popupWider(BasicComboPopup popup) {
    JList<?> list = popup.getList();

    //  Determine the maximimum width to use:
    //  a) determine the popup preferred width
    //  b) ensure width is not less than the scroll pane width
    int popupWidth = list.getPreferredSize().width
        + 5 // make sure horizontal scrollbar doesn't appear
        + getScrollBarWidth(popup, scrollPane);

    Dimension scrollPaneSize = scrollPane.getPreferredSize();
    popupWidth = Math.max(popupWidth, scrollPaneSize.width);

    //  Adjust the width
    scrollPaneSize.width = popupWidth;
    scrollPane.setPreferredSize(scrollPaneSize);
    scrollPane.setMaximumSize(scrollPaneSize);
  }

  /**
   * Returns the scroll pane used by the popup so its bounds can be adjusted.
   */
  protected JScrollPane getScrollPane(BasicComboPopup popup) {
    JList<?> list = popup.getList();
    Container c = SwingUtilities.getAncestorOfClass(JScrollPane.class, list);

    return (JScrollPane) c;
  }

  protected int getScrollBarWidth(BasicComboPopup popup, JScrollPane scrollPane) {
    // I can't find any property on the scrollBar to determine if it will be
    // displayed or not so use brute force to determine this.
    JComboBox<?> comboBox = (JComboBox) popup.getInvoker();

    if (comboBox.getItemCount() > comboBox.getMaximumRowCount()) {
      return scrollPane.getVerticalScrollBar().getPreferredSize().width;
    }
    else {
      return 0;
    }
  }

}
