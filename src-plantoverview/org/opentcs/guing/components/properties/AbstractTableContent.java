/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.properties;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.opentcs.guing.components.properties.event.TableChangeListener;
import org.opentcs.guing.components.properties.event.TableSelectionChangeEvent;
import org.opentcs.guing.components.properties.table.AttributesTable;
import org.opentcs.guing.components.properties.type.Property;

/**
 * Basisimplementierung für Inhalte, die Eigenschaften eines Modells in einer
 * Tabelle darstellen. Im unteren Bereich wird zu jeder Eigenschaft ein
 * Hilfetext angezeigt.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public abstract class AbstractTableContent
    extends AbstractAttributesContent
    implements TableChangeListener {

  /**
   * Die Tabelle zur Darstellung der Eigenschaften.
   */
  protected AttributesTable fTable;
  /**
   * Die CellEditoren.
   */
  protected List<TableCellEditor> fCellEditors;
  /**
   * Zeigt an, ob Änderungen am Tabellenmodell ausgewertet, das heißt, an
   * ModelComponent weitergeleitet werden sollen.
   */
  protected boolean fEvaluateTableChanges;

  /**
   * Creates a new instance of AbstractTableContent
   */
  public AbstractTableContent() {
    super();
    fEvaluateTableChanges = true;
    fCellEditors = new ArrayList<>();
  }

  @Override // AbstractAttributesContent
  protected JComponent createComponent() {
    JPanel component = new JPanel();

    initTable();
    JScrollPane scrollPane = new JScrollPane(fTable);

    component.setLayout(new BorderLayout());
    component.add(scrollPane, BorderLayout.CENTER);

    return component;
  }

  @Override // TableChangeListener
  public void tableSelectionChanged(TableSelectionChangeEvent e) {
  }

  @Override // TableChangeListener
  public void tableModelChanged() {
  }

  /**
   * Schablonenmethode zur Erzeugung und Konfiguration der Tabelle.
   */
  protected void initTable() {
    fTable = new AttributesTable();
    setTableCellRenderers();
    setTableCellEditors();
    fTable.addTableChangeListener(this);
  }

  /**
   * Setzt die Renderer für die Tabelle.
   */
  protected void setTableCellRenderers() {
  }

  /**
   * Setzt die Editoren für die Tabelle.
   */
  protected void setTableCellEditors() {
  }

  /**
   * Weist der Tabelle einen neuen Inhalt zu (neue Eigenschaften und Werte).
   * Die Eigenschaften und ihre Werte befinden sich in der übergebenen
   * <code>Hashtable</code>.
   *
   * @param content
   */
  protected void setTableContent(Map<String, Property> content) {
    fEvaluateTableChanges = false;

    // Spaltenbreiten retten
    TableColumnModel columnModel = fTable.getColumnModel();
    int[] widths = new int[columnModel.getColumnCount()];

    for (int i = 0; i < widths.length; i++) {
      widths[i] = columnModel.getColumn(i).getWidth();
    }

    // neues Modell setzen
    fTable.setModel(createTableModel(content));

    // Spaltenbreiten wieder herstellen
    for (int i = 0; i < widths.length; i++) {
      columnModel.getColumn(i).setPreferredWidth(widths[i]);
    }

    fEvaluateTableChanges = true;
  }

  /**
   * Erzeugt aus der übergebenen <code>Hashtable</code> ein neues
   * <code>TableModel</code>.
   *
   * @param content
   * @return Das erzeugte <code>TableModel</code>.
   */
  protected abstract TableModel createTableModel(Map<String, Property> content);

}
