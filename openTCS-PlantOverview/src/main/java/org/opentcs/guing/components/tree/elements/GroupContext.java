/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.tree.elements;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.inject.Inject;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Context for the group tree view.
 *
 * @author Philipp Seifert (Philipp.Seifert@iml.fraunhofer.de)
 */
public class GroupContext
    implements UserObjectContext {

  private final OpenTCSView openTCSView;

  /**
   * Creates a new instance.
   * 
   * @param openTCSView The openTCS view
   */
  @Inject
  public GroupContext(OpenTCSView openTCSView) {
    this.openTCSView = Objects.requireNonNull(openTCSView, "openTCSView");
  }

  @Override
  public JPopupMenu getPopupMenu(final Set<UserObject> selectedUserObjects) {
    JPopupMenu menu = new JPopupMenu();
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle();

    JMenuItem item = new JMenuItem(labels.getString("tree.removeFromGroup"));
    item.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        openTCSView.removeGroupMembers(selectedUserObjects);
      }
    });

    menu.add(item);

    return menu;
  }

  @Override
  public boolean removed(UserObject userObject) {
    Set<UserObject> set = new HashSet<>(1);
    set.add(userObject);
    return openTCSView.removeGroupMembers(set);
  }

  @Override
  public ContextType getType() {
    return ContextType.GROUP;
  }
}
