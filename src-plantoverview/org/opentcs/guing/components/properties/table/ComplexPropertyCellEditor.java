/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.properties.table;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import static java.util.Objects.requireNonNull;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import org.opentcs.guing.application.GuiManager;
import org.opentcs.guing.components.dialogs.DetailsDialogContent;
import org.opentcs.guing.components.dialogs.StandardDetailsDialog;
import org.opentcs.guing.components.properties.type.AbstractComplexProperty;

/**
 * Ein CellEditor für Attribute vom Typ {
 *
 * @see AbstractComplexProperty} sowie Unterklassen. Der Editor ist ein Button;
 * beim Anklicken öffnet sich sofort ein DetailsDialog. Einen Button mit drei
 * Punkten gibt es nicht extra. Das liegt daran, dass Attribute vom Typ {
 * @see AbstractComplexProperty} so speziell und mitunter komplex sind, dass
 * eine Bearbeitung mit einer ComboBox, einem Textfeld oder einer CheckBox in
 * aller Regel keinen Sinn macht.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class ComplexPropertyCellEditor
    extends javax.swing.AbstractCellEditor
    implements javax.swing.table.TableCellEditor {

  /**
   * This class's Logger.
   */
  private static final Logger log
      = Logger.getLogger(ComplexPropertyCellEditor.class.getName());
  /**
   * The Property.
   */
  private AbstractComplexProperty fProperty;
  /**
   * Der Button, bei dessen Anklicken sich der DetailsDialog öffnet. Der Button
   * füllt die gesamte Zelle aus.
   */
  private final JButton fButton;
  /**
   * Der Besitzer des DetailsDialog (Panel).
   */
  private final GuiManager guiManager;
  /**
   * A parent for dialogs created by this instance.
   */
  private final JPanel dialogParent;

  /**
   * Creates a new instance.
   *
   * @param guiManager Used as a provider for the SystemModel.
   * @param dialogParent A parent for dialogs created by this instance.
   */
  public ComplexPropertyCellEditor(GuiManager guiManager, JPanel dialogParent) {
    this.guiManager = requireNonNull(guiManager, "guiManager");
    this.dialogParent = requireNonNull(dialogParent, "dialogParent");
    fButton = new JButton();
    fButton.setFont(new Font("Dialog", Font.PLAIN, 12));
    fButton.setBorder(null);
    fButton.setHorizontalAlignment(JButton.LEFT);
    fButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        showDialog();
      }
    });
  }

  @Override
  public Object getCellEditorValue() {
    return fProperty;
  }

  @Override
  public Component getTableCellEditorComponent(
      JTable table, Object value, boolean isSelected, int row, int column) {

    fProperty = (AbstractComplexProperty) value;
    fButton.setText(fProperty.toString());
    fButton.setBackground(table.getBackground());

    return fButton;
  }

  /**
   * Öffnet den Dialog, mit dessen Hilfe die Eigenschaften eines Attributs
   * komfortabler eingestellt werden können.
   */
  private void showDialog() {
    Class<? extends JPanel> editorPanel = fProperty.getPropertyEditorPanel();
    if (editorPanel == null) {
      return;
    }

    DetailsDialogContent content;
    try {
      content = (DetailsDialogContent) editorPanel.newInstance();
      // Give it a reference to the SystemModel
      content.setSystemModel(guiManager.getSystemModel());
    }
    catch (InstantiationException | IllegalAccessException e) {
      log.log(Level.SEVERE, "Exception creating property editor panel: ", e);
      return;
    }

    StandardDetailsDialog detailsDialog
        = new StandardDetailsDialog(dialogParent, true, content);
    detailsDialog.setLocationRelativeTo(dialogParent);

    detailsDialog.getDialogContent().setProperty(fProperty);
    detailsDialog.activate();
    detailsDialog.setVisible(true);

    if (detailsDialog.getReturnStatus() == StandardDetailsDialog.RET_OK) {
      stopCellEditing();
//      fireEditingStopped(); // bewirkt nichts
    }
    else {
      cancelCellEditing();
    }
  }
}
