/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.dialogs;

import com.google.inject.assistedinject.Assisted;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.swing.DefaultListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.opentcs.guing.application.GuiManager;
import org.opentcs.guing.components.properties.type.SpeedProperty.Unit;
import org.opentcs.guing.model.elements.AbstractConnection;
import org.opentcs.guing.model.elements.PathModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Benutzerschnittstelle zum Hinzufügen von Knoten zu einer statischen Route.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class AddNodesToStaticRoutePanel
    extends DialogContent {

  /**
   * Die hinzugefügten Knoten.
   */
  private List<PointModel> fAddedPoints;
  /**
   * Die möglichen Knoten.
   */
  private List<PointModel> fAvailablePoints;
  /**
   * Der Startknoten.
   */
  private PointModel fStartPoint;
  /**
   * Alle Knoten des Fahrkurses.
   */
  private List<PointModel> fAllPoints;

  /**
   * Creates new form AddNodesToStaticRoutePanel.
   *
   * @param guiManager The GUI manager.
   * @param point der Knoten, von dem die nachfolgenden Knoten bestimmt werden
   * sollen
   * @param allPoints alle im Fahrkurs verfügbaren Knoten
   */
  @Inject
  public AddNodesToStaticRoutePanel(final GuiManager guiManager,
                                    @Assisted @Nullable PointModel point,
                                    @Assisted List<PointModel> allPoints) {
    fStartPoint = point;
    fAllPoints = requireNonNull(allPoints, "allPoints");

    initComponents();
    fAddedPoints = new ArrayList<>();
    setDialogTitle(ResourceBundleUtil.getBundle().getString("staticRoute.dialog.title"));
    listAvailablePoints.addListSelectionListener(new ListSelectionListener() {

      @Override
      public void valueChanged(ListSelectionEvent evt) {
        if (evt.getValueIsAdjusting()) {
          return;
        }

        int selectedIndex = listAvailablePoints.getSelectedIndex();

        if (selectedIndex >= 0) {
          buttonSelect.setEnabled(true);
          String entry = listAvailablePoints.getModel().getElementAt(selectedIndex);
          Iterator<PointModel> e = fAvailablePoints.iterator();

          while (e.hasNext()) {
            PointModel point = e.next();

            if (point.getName().equals(entry)) {
              guiManager.figureSelected(point);
            }
          }
        }
        else {
          buttonSelect.setEnabled(false);
        }
      }
    });
  }

  /**
   * Liefert die hinzugefügten Knoten.
   *
   * @return die hinzugefügten Knoten
   */
  public List<PointModel> getAddedPoints() {
    return fAddedPoints;
  }

  @Override // DialogContent
  public void update() {
  }

  @Override // DialogContent
  public void initFields() {
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
    DefaultListModel<String> listModel = new DefaultListModel<>();

    if (fStartPoint == null) {
      fAvailablePoints = fAllPoints;
      labelFrom.setEnabled(false);
      textFieldFrom.setText("");
      labelTo.setText(bundle.getString("staticRoute.start.text"));
      buttonSelect.setText(bundle.getString("staticRoute.select.text"));
    }
    else {
      fAvailablePoints = getPointsFrom(fStartPoint);
      labelFrom.setEnabled(true);
      textFieldFrom.setText(fStartPoint.getName());
      labelTo.setText(bundle.getString("staticRoute.to.text"));
      buttonSelect.setText(bundle.getString("staticRoute.add.text"));
    }

    Collections.sort(fAvailablePoints);
    Iterator<PointModel> ePoints = fAvailablePoints.iterator();

    while (ePoints.hasNext()) {
      PointModel point = ePoints.next();
      listModel.addElement(point.getName());
    }

    listAvailablePoints.setModel(listModel);

    if (!listModel.isEmpty()) {
      listAvailablePoints.setSelectedIndex(0);
    }
  }

  /**
   * Sucht zu einem Knoten alle benachbarten Knoten heraus, die direkt erreicht
   * werden können.
   *
   * @param point der Knoten, dessen erreichbare benachbarte Knoten
   * herausgesucht werden sollen
   * @return die Liste der erreichbaren Knoten
   */
  private List<PointModel> getPointsFrom(PointModel point) {
    List<PointModel> points = new ArrayList<>();

    for (AbstractConnection connection : point.getConnections()) {
      if (!(connection instanceof PathModel)) {
        continue;
      }

      PathModel pathConnection = (PathModel) connection;

      if (connection.getStartComponent() == point) { // Outgoing path
        if (pathConnection.getPropertyMaxVelocity().getValueByUnit(Unit.MM_S) > 0.0) {
          // Point can be reached driving forward
          points.add((PointModel) connection.getEndComponent());
        }
      }

      if (connection.getEndComponent() == point) {  // Incoming path
        if (pathConnection.getPropertyMaxReverseVelocity().getValueByUnit(Unit.MM_S) > 0.0) {
          // Point can be reached driving backward
          points.add((PointModel) connection.getStartComponent());
        }
      }
    }

    return points;
  }

  // CHECKSTYLE:OFF
  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    labelFrom = new javax.swing.JLabel();
    textFieldFrom = new javax.swing.JTextField();
    labelTo = new javax.swing.JLabel();
    scrollPaneAvailablePoints = new javax.swing.JScrollPane();
    listAvailablePoints = new javax.swing.JList<>();
    buttonSelect = new javax.swing.JButton();

    setName("AddNodesPanel"); // NOI18N
    setLayout(new java.awt.GridBagLayout());

    labelFrom.setFont(labelFrom.getFont());
    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/opentcs/guing/res/labels"); // NOI18N
    labelFrom.setText(bundle.getString("staticRoute.from.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
    add(labelFrom, gridBagConstraints);

    textFieldFrom.setEditable(false);
    textFieldFrom.setColumns(15);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
    add(textFieldFrom, gridBagConstraints);

    labelTo.setFont(labelTo.getFont());
    labelTo.setText(bundle.getString("staticRoute.to.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
    add(labelTo, gridBagConstraints);

    scrollPaneAvailablePoints.setMinimumSize(new java.awt.Dimension(180, 132));
    scrollPaneAvailablePoints.setPreferredSize(new java.awt.Dimension(180, 132));

    listAvailablePoints.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    scrollPaneAvailablePoints.setViewportView(listAvailablePoints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.weighty = 0.5;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
    add(scrollPaneAvailablePoints, gridBagConstraints);

    buttonSelect.setFont(buttonSelect.getFont());
    buttonSelect.setText(bundle.getString("staticRoute.add.text")); // NOI18N
    buttonSelect.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        buttonSelectActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
    add(buttonSelect, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents

  /**
   * Fügt den markierten Knoten hinzu und setzt diesen gleich wieder als
   * Ausgangsknoten ein.
   *
   * @param evt das auslösende Ereignis
   */
    private void buttonSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSelectActionPerformed
      int i = listAvailablePoints.getSelectedIndex();

      if (i == -1) {
        return; // no Point selected
      }

      fStartPoint = fAvailablePoints.get(i);
      fAddedPoints.add(fStartPoint);
      initFields();
    }//GEN-LAST:event_buttonSelectActionPerformed
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton buttonSelect;
  private javax.swing.JLabel labelFrom;
  private javax.swing.JLabel labelTo;
  private javax.swing.JList<String> listAvailablePoints;
  private javax.swing.JScrollPane scrollPaneAvailablePoints;
  private javax.swing.JTextField textFieldFrom;
  // End of variables declaration//GEN-END:variables
  // CHECKSTYLE:ON
}
