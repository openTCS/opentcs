// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0.controlcenter;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.Action;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.instantactions.InstantActions;

/**
 * Renders orders when displayed in a list.
 */
public class InstantActionsListCellRenderer
    extends
      DefaultListCellRenderer {

  /**
   * A prototype for the list to compute its preferred size.
   */
  public static final InstantActions PROTOTYPE_INSTANT_ACTIONS = new InstantActions();

  /**
   * Creates a new instance.
   */
  public InstantActionsListCellRenderer() {
  }

  @Override
  public Component getListCellRendererComponent(
      JList<?> list,
      Object value,
      int index,
      boolean isSelected,
      boolean cellHasFocus
  ) {
    Component component = super.getListCellRendererComponent(
        list,
        value,
        index,
        isSelected,
        cellHasFocus
    );

    if (value instanceof InstantActions) {
      InstantActions instantActions = (InstantActions) value;
      JLabel label = (JLabel) component;

      if (!instantActions.getActions().isEmpty()) {
        Action action = instantActions.getActions().get(0);

        StringBuilder sb = new StringBuilder();
        sb.append("Type: ");
        sb.append(action.getActionType());
        sb.append(", ID: ");
        sb.append(action.getActionId());
        sb.append(", Blocking type: ");
        sb.append(action.getBlockingType().name());
        if (action.getActionParameters() != null) {
          action.getActionParameters().forEach(parameter -> {
            sb.append(", ");
            sb.append(parameter.getKey());
            sb.append(": ");
            sb.append(parameter.getValue());
          });
        }

        String labelText = sb.toString();
        label.setText(labelText);
        label.setToolTipText(labelText);
      }
    }

    return component;
  }
}
