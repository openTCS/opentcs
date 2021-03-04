/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.tree;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Set;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import org.opentcs.guing.components.tree.elements.UserObject;

/**
 * Ein Interface für die Baumansicht auf der linken Seite. Eine Baumansicht
 * verwaltet ein Modell, das eine Menge von DefaultMutableTreeNode-Objekten
 * besitzt. Jeder DefaultMutableTreeNode referenziert ein UserObject. Ein
 * UserObject kapselt ein richtiges Objekt (z.B. Article, Figure, ...). Es weiß,
 * welche Methode der Applikation aufgerufen werden muss, wenn sein Objekt im
 * Baum selektiert, gelöscht oder doppelt angeklickt wird.
 * <p>
 * <b>Entwurfsmuster:</b> Befehl. TreeView (bzw. eine Unterklasse) ist der
 * Auslöser für die Ausführung eines konkreten UserObjects, welches ja einen
 * Befehl darstellt.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @see UserObject
 */
public interface TreeView {

  /**
   * Fügt dem Baum ein Item hinzu. Übergebenes Elternobjekt ist ein richtiges
   * Objekt, das im Baum gesucht werden muss. Item ist ein UserObject.
   *
   * @param parent
   * @param item
   */
  void addItem(Object parent, UserObject item);

  /**
   * Sorts the children of the given node.
   *
   * @param treeNode The node whose children shall be sorted.
   */
  void sortItems(TreeNode treeNode);

  /**
   * Returns the <code>JTree</code> that actually holds the objects.
   *
   * @return The tree.
   */
  JTree getTree();

  /**
   * Adds the given listener to the <code>JTree</code>.
   *
   * @param mouseListener The listener.
   */
  void addMouseListener(MouseListener mouseListener);

  /**
   * Adds the given motion listener to the <code>JTree</code>.
   *
   * @param mouseMotionListener The motion listener.
   */
  void addMouseMotionListener(MouseMotionListener mouseMotionListener);

  /**
   * Updates the text at the top of the <code>JTree</code>.
   *
   * @param text The new text.
   */
  void updateText(String text);

  /**
   * Returns whether the tree has buffered objects.
   *
   * @return <code>true</code> if it has some.
   */
  boolean hasBufferedObjects();

  /**
   * Returns the dragged user object.
   *
   * @param e The event where the mouse click happened.
   * @return The user object that was dragged.
   */
  UserObject getDraggedUserObject(MouseEvent e);

  /**
   * Sets the cursor of the <code>JTree</code>.
   *
   * @param cursor The new cursor.
   */
  void setCursor(Cursor cursor);

  /**
   * Löscht aus dem Baum ein Item. Item ist ein richtiges Objekt, d.h. kein
   * DefaultMutableTreeNode und kein UserObject sondern beispielsweise ein
   * Article.
   *
   * @param item
   */
  void removeItem(Object item);

  /**
   * Entfernt alle Kindelemente eines Items.
   *
   * @param item
   */
  void removeChildren(Object item);

  /**
   * Selektiert ein Item im Baum. Item ist ein richtiges Objekt, d.h. kein
   * DefaultMutableTreeNode und kein UserObject sondern beispielsweise ein
   * Article.
   *
   * @param item
   */
  void selectItem(Object item);

  /**
   * Selektiert mehrere Items im Baum.
   *
   * @param items
   */
  void selectItems(Set<?> items);

  /**
   * Teilt dem Baum mit, dass sich die Eigenschaften des übergeben Objekts
   * geändert haben und seine Darstellung aktualisiert werden muss. Item ist ein
   * richtiges Objekt, d.h. kein DefaultMutableTreeNode und kein UserObject
   * sondern beispielsweise ein Article.
   *
   * @param item
   */
  void itemChanged(Object item);

  /**
   * Liefert das selektierte Element.
   *
   * @return
   */
  UserObject getSelectedItem();

  /**
   * Liefert die selektierten Elemente.
   *
   * @return
   */
  Set<UserObject> getSelectedItems();

  /**
   * Sorts the root element of the tree.
   */
  void sortRoot();

  /**
   * Sorts all children.
   */
  void sortChildren();
}
