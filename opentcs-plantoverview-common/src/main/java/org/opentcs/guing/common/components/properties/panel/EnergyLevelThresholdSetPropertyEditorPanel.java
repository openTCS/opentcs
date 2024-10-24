// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.components.properties.panel;

import static java.util.Objects.requireNonNull;
import static org.opentcs.util.Assertions.checkInRange;

import javax.swing.JOptionPane;
import org.opentcs.guing.base.components.properties.type.EnergyLevelThresholdSetModel;
import org.opentcs.guing.base.components.properties.type.EnergyLevelThresholdSetProperty;
import org.opentcs.guing.base.components.properties.type.Property;
import org.opentcs.guing.common.components.dialogs.DetailsDialogContent;
import org.opentcs.guing.common.util.I18nPlantOverview;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;

/**
 * An editor panel for the energy level threshold set of a vehicle.
 */
public class EnergyLevelThresholdSetPropertyEditorPanel
    extends
      javax.swing.JPanel
    implements
      DetailsDialogContent {

  /**
   * The bundle to be used.
   */
  private static final ResourceBundleUtil BUNDLE
      = ResourceBundleUtil.getBundle(I18nPlantOverview.PROPERTIES_PATH);
  /**
   * The property to edit.
   */
  private EnergyLevelThresholdSetProperty fProperty;

  /**
   * Creates new instance.
   */
  @SuppressWarnings("this-escape")
  public EnergyLevelThresholdSetPropertyEditorPanel() {
    initComponents();
  }

  @Override
  public void updateValues() {
    fProperty.setValue(createEnergyLevelThresholdSetFromInput());
  }

  @Override
  public String getTitle() {
    return BUNDLE.getString("energyLevelThresholdSetPropertyEditorPanel.title");
  }

  @Override
  public void setProperty(Property property) {
    requireNonNull(property, "property");
    fProperty = (EnergyLevelThresholdSetProperty) property;

    energyLevelCriticalTextField.setText(
        String.valueOf(fProperty.getValue().getEnergyLevelCritical())
    );
    energyLevelGoodTextField.setText(String.valueOf(fProperty.getValue().getEnergyLevelGood()));
    energyLevelSufficientlyRechargedTextField.setText(
        String.valueOf(fProperty.getValue().getEnergyLevelSufficientlyRecharged())
    );
    energyLevelFullyRechargedTextField.setText(
        String.valueOf(fProperty.getValue().getEnergyLevelFullyRecharged())
    );
  }

  @Override
  public Property getProperty() {
    return fProperty;
  }

  @SuppressWarnings("checkstyle:LineLength")
  private EnergyLevelThresholdSetModel createEnergyLevelThresholdSetFromInput() {
    int energyLevelCritical;
    int energyLevelGood;
    int energyLevelSufficientlyRecharged;
    int energyLevelFullyRecharged;

    try {
      energyLevelCritical = checkInRange(
          Integer.parseInt(energyLevelCriticalTextField.getText()),
          0,
          100,
          "energyLevelCritical"
      );
      energyLevelGood = checkInRange(
          Integer.parseInt(energyLevelGoodTextField.getText()),
          0,
          100,
          "energyLevelGood"
      );
      energyLevelSufficientlyRecharged = checkInRange(
          Integer.parseInt(energyLevelSufficientlyRechargedTextField.getText()),
          0,
          100,
          "energyLevelSufficientlyRecharged"
      );
      energyLevelFullyRecharged = checkInRange(
          Integer.parseInt(energyLevelFullyRechargedTextField.getText()),
          0,
          100,
          "energyLevelFullyRecharged"
      );
    }
    catch (NumberFormatException e) {
      JOptionPane.showMessageDialog(
          this,
          BUNDLE.getString(
              "energyLevelThresholdSetPropertyEditorPanel.optionPane_numberFormatError.message"
          ),
          BUNDLE.getString(
              "energyLevelThresholdSetPropertyEditorPanel.optionPane_numberFormatError.title"
          ),
          JOptionPane.ERROR_MESSAGE
      );
      // Re-throw the exception to prevent the editor panel from closing.
      throw e;
    }
    catch (IllegalArgumentException e) {
      JOptionPane.showMessageDialog(
          this,
          BUNDLE.getString(
              "energyLevelThresholdSetPropertyEditorPanel.optionPane_thresholdsNotInRange.message"
          ),
          BUNDLE.getString(
              "energyLevelThresholdSetPropertyEditorPanel.optionPane_thresholdsNotInRange.title"
          ),
          JOptionPane.ERROR_MESSAGE
      );
      // Re-throw the exception to prevent the editor panel from closing.
      throw e;
    }

    if (energyLevelGood < energyLevelCritical) {
      JOptionPane.showMessageDialog(
          this,
          BUNDLE.getString(
              "energyLevelThresholdSetPropertyEditorPanel.optionPane_goodSmallerCritical.message"
          ),
          BUNDLE.getString(
              "energyLevelThresholdSetPropertyEditorPanel.optionPane_goodSmallerCritical.title"
          ),
          JOptionPane.ERROR_MESSAGE
      );
      // Throw the exception to prevent the editor panel from closing.
      throw new IllegalArgumentException("Energy level good has to be >= energy level critical.");
    }

    if (energyLevelFullyRecharged < energyLevelSufficientlyRecharged) {
      JOptionPane.showMessageDialog(
          this,
          BUNDLE.getString(
              "energyLevelThresholdSetPropertyEditorPanel.optionPane_fullyRechargedSmallerSufficientlyRecharged.message"
          ),
          BUNDLE.getString(
              "energyLevelThresholdSetPropertyEditorPanel.optionPane_fullyRechargedSmallerSufficientlyRecharged.title"
          ),
          JOptionPane.ERROR_MESSAGE
      );
      // Throw the exception to prevent the editor panel from closing.
      throw new IllegalArgumentException(
          "Energy level fully recharged has to be >= energy level sufficiently recharged."
      );
    }

    return new EnergyLevelThresholdSetModel(
        energyLevelCritical,
        energyLevelGood,
        energyLevelSufficientlyRecharged,
        energyLevelFullyRecharged
    );
  }

  /**
   * This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the FormEditor.
   */
  // FORMATTER:OFF
  // CHECKSTYLE:OFF
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    thresholdsPanel = new javax.swing.JPanel();
    energyLevelGoodLabel = new javax.swing.JLabel();
    energyLevelCriticalLabel = new javax.swing.JLabel();
    energyLevelSufficientlyRechargedLabel = new javax.swing.JLabel();
    energyLevelFullyRechargedLabel = new javax.swing.JLabel();
    energyLevelGoodTextField = new javax.swing.JTextField();
    energyLevelFullyRechargedTextField = new javax.swing.JTextField();
    energyLevelSufficientlyRechargedTextField = new javax.swing.JTextField();
    energyLevelCriticalTextField = new javax.swing.JTextField();

    setLayout(new java.awt.GridBagLayout());

    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/org/opentcs/plantoverview/panels/propertyEditing"); // NOI18N
    thresholdsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("energyLevelThresholdSetPropertyEditorPanel.panel_thresholds.border.title"))); // NOI18N
    thresholdsPanel.setLayout(new java.awt.GridBagLayout());

    energyLevelGoodLabel.setText(bundle.getString("energyLevelThresholdSetPropertyEditorPanel.label_energyLevelGood.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
    thresholdsPanel.add(energyLevelGoodLabel, gridBagConstraints);

    energyLevelCriticalLabel.setText(bundle.getString("energyLevelThresholdSetPropertyEditorPanel.label_energyLevelCritical.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
    thresholdsPanel.add(energyLevelCriticalLabel, gridBagConstraints);

    energyLevelSufficientlyRechargedLabel.setText(bundle.getString("energyLevelThresholdSetPropertyEditorPanel.label_energyLevelSufficientlyRecharged.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
    thresholdsPanel.add(energyLevelSufficientlyRechargedLabel, gridBagConstraints);

    energyLevelFullyRechargedLabel.setText(bundle.getString("energyLevelThresholdSetPropertyEditorPanel.label_energyLevelFullyRecharged.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
    thresholdsPanel.add(energyLevelFullyRechargedLabel, gridBagConstraints);

    energyLevelGoodTextField.setColumns(3);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 3);
    thresholdsPanel.add(energyLevelGoodTextField, gridBagConstraints);

    energyLevelFullyRechargedTextField.setColumns(3);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    thresholdsPanel.add(energyLevelFullyRechargedTextField, gridBagConstraints);

    energyLevelSufficientlyRechargedTextField.setColumns(3);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 3);
    thresholdsPanel.add(energyLevelSufficientlyRechargedTextField, gridBagConstraints);

    energyLevelCriticalTextField.setColumns(3);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 3);
    thresholdsPanel.add(energyLevelCriticalTextField, gridBagConstraints);

    add(thresholdsPanel, new java.awt.GridBagConstraints());
  }// </editor-fold>//GEN-END:initComponents

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel energyLevelCriticalLabel;
  private javax.swing.JTextField energyLevelCriticalTextField;
  private javax.swing.JLabel energyLevelFullyRechargedLabel;
  private javax.swing.JTextField energyLevelFullyRechargedTextField;
  private javax.swing.JLabel energyLevelGoodLabel;
  private javax.swing.JTextField energyLevelGoodTextField;
  private javax.swing.JLabel energyLevelSufficientlyRechargedLabel;
  private javax.swing.JTextField energyLevelSufficientlyRechargedTextField;
  private javax.swing.JPanel thresholdsPanel;
  // End of variables declaration//GEN-END:variables
  // CHECKSTYLE:ON
  // FORMATTER:ON
}
