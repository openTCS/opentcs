/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */

package org.opentcs.guing.components.properties.panel;

import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import org.opentcs.guing.components.dialogs.StandardDetailsDialog;
import org.opentcs.guing.model.elements.LinkModel;
import org.opentcs.guing.model.elements.LocationTypeModel;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Allows editing of actions that a vehicle can execute at a station.
 * Which actions are possible is determined by the station type.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class LinkActionsEditorPanel
    extends StringSetPropertyEditorPanel {

  /**
   * The bundle to be used.
   */
  private final ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();

  /**
   * Creates a new instance.
   */
  public LinkActionsEditorPanel() {
  }

  @Override
  protected void edit() {
    String value = getItemsList().getSelectedValue();

    if (value == null) {
      return;
    }

    int index = getItemsList().getSelectedIndex();
    JDialog parent = (JDialog) getTopLevelAncestor();
    SelectionPanel content = new SelectionPanel(
        bundle.getString("LinkActionsEditorPanel.editAction"),
        bundle.getString("LinkActionsEditorPanel.action"),
        getPossibleItems(),
        value);
    StandardDetailsDialog dialog = new StandardDetailsDialog(parent, true, content);
    dialog.setLocationRelativeTo(parent);
    dialog.setVisible(true);

    if (dialog.getReturnStatus() == StandardDetailsDialog.RET_OK) {
      DefaultListModel<String> model = (DefaultListModel<String>) getItemsList().getModel();
      model.setElementAt(content.getValue().toString(), index);
    }
  }

  @Override
  protected void add() {
    JDialog parent = (JDialog) getTopLevelAncestor();
    SelectionPanel content = new SelectionPanel(
        bundle.getString("LinkActionsEditorPanel.addAction"),
        bundle.getString("LinkActionsEditorPanel.action"),
        getPossibleItems());
    StandardDetailsDialog dialog = new StandardDetailsDialog(parent, true, content);
    dialog.setLocationRelativeTo(parent);
    dialog.setVisible(true);

    if (dialog.getReturnStatus() == StandardDetailsDialog.RET_OK) {
      DefaultListModel<String> model = (DefaultListModel<String>) getItemsList().getModel();
      Object value = content.getValue();
      if (value != null) {
        model.addElement(value.toString());
      }
    }
  }

  /**
   * Returns the possible actions that can be executed at a station.
   * The actions are determined by the station type.
   *
   * @return The possible actions.
   */
  private List<String> getPossibleItems() {
    LinkModel ref = (LinkModel) getProperty().getModel();
    LocationTypeModel type = ref.getLocation().getLocationType();

    return new ArrayList<>(type.getPropertyAllowedOperations().getItems());
  }
}
