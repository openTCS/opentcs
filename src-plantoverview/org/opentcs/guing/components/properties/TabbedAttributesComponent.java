/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.properties;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import org.opentcs.guing.application.action.edit.UndoRedoManager;
import org.opentcs.guing.model.ModelComponent;

/**
 * Eine PropertiesComponent in Registerkartenform.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class TabbedAttributesComponent
    extends AttributesComponent {

  /**
   * Die Inhalte der Registerkarten.
   */
  protected ArrayList fContents;
  /**
   * Die Registerkarte.
   */
  protected JTabbedPane fTabbedPane;

  /**
   * Creates a new instance of TabbedPropertiesComponent
   *
   * @param undoRedoManager
   */
  public TabbedAttributesComponent(UndoRedoManager undoRedoManager) {
    super(undoRedoManager);
    initComponents();
    fContents = new ArrayList();
  }

  /**
   * Initialisiert die Swing-Komponenten.
   */
  protected final void initComponents() {
    descriptionLabel = new JLabel();
    setLayout(new BorderLayout());
    descriptionLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    descriptionLabel.setBorder(new EmptyBorder(new Insets(3, 3, 3, 3)));
    descriptionLabel.setFont(new Font("Arial", Font.PLAIN, 11));
    add(descriptionLabel, BorderLayout.NORTH);

    fTabbedPane = new JTabbedPane();
    fTabbedPane.setTabPlacement(JTabbedPane.BOTTOM);
    add(fTabbedPane, BorderLayout.CENTER);
  }

  /**
   * Fügt einen PropertiesContent in einer neuen Registerkarte hinzu.
   *
   * @param title
   * @param content
   */
  public void addTab(String title, AttributesContent content) {
    fContents.add(content);
    content.setup(fUndoRedoManager);
    fTabbedPane.addTab(title, content.getComponent());
  }

  /**
   * Entfernt einen PropertiesContent und damit die entsprechende Registerkarte.
   *
   * @param content
   */
  public void removeTab(AttributesContent content) {
    fTabbedPane.removeTabAt(fContents.indexOf(content));
  }

  @Override
  public void setModel(ModelComponent model) {
    Iterator iContents = fContents.iterator();

    while (iContents.hasNext()) {
      AttributesContent content = (AttributesContent) iContents.next();
      int index = fContents.indexOf(content);
      fTabbedPane.setEnabledAt(index, true);
      content.setModel(model);
    }

    setDescription(model.getDescription());
  }

  /**
   * Setzt den Text, der im oberen Teil der Komponente angezeigt wird. Der Text
   * bezeichnet das Objekt, dessen Eigenschaften in der Tabelle angezeigt
   * werden.
   *
   * @param text
   */
  protected void setDescription(String text) {
    descriptionLabel.setText(text);
  }
}
