/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.tree;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.jhotdraw.draw.Figure;
import org.opentcs.guing.base.model.AbstractConnectableModelComponent;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.base.model.SimpleFolder;
import org.opentcs.guing.base.model.elements.LayoutModel;
import org.opentcs.guing.common.application.ComponentsManager;
import org.opentcs.guing.common.components.EditableComponent;
import org.opentcs.guing.common.components.tree.elements.LayoutUserObject;
import org.opentcs.guing.common.components.tree.elements.SimpleFolderUserObject;
import org.opentcs.guing.common.components.tree.elements.UserObject;
import static org.opentcs.guing.common.model.SystemModel.FolderKey.LINKS;
import static org.opentcs.guing.common.model.SystemModel.FolderKey.LOCATIONS;
import static org.opentcs.guing.common.model.SystemModel.FolderKey.LOCATION_TYPES;
import static org.opentcs.guing.common.model.SystemModel.FolderKey.OTHER_GRAPHICAL_ELEMENTS;
import static org.opentcs.guing.common.model.SystemModel.FolderKey.PATHS;
import static org.opentcs.guing.common.model.SystemModel.FolderKey.POINTS;
import org.opentcs.guing.common.persistence.ModelManager;
import org.opentcs.guing.common.util.I18nPlantOverview;
import org.opentcs.thirdparty.guing.common.jhotdraw.application.action.edit.DeleteAction;
import org.opentcs.thirdparty.guing.common.jhotdraw.application.action.edit.UndoRedoManager;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;

/**
 * Abstract implementation of a tree view to display model components in a tree.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public abstract class AbstractTreeViewPanel
    extends JPanel
    implements TreeView,
               EditableComponent {

  /**
   * The root node.
   */
  private SortableTreeNode fRootNode;
  /**
   * The model for the JTree.
   */
  private DefaultTreeModel fTreeModel;
  /**
   * The model manager.
   */
  private final ModelManager modelManager;
  /**
   * The components manager.
   */
  private final ComponentsManager componentsManager;
  /**
   * The undo manger.
   */
  protected final UndoRedoManager fUndoRedoManager;
  /**
   *
   */
  protected List<UserObject> bufferedUserObjects = new ArrayList<>();
  /**
   *
   */
  protected final List<Figure> bufferedFigures = new ArrayList<>();

  /**
   * Creates a new instance.
   *
   * @param undoRedoManager The undo redo manager
   */
  @Inject
  public AbstractTreeViewPanel(UndoRedoManager undoRedoManager,
                               ModelManager modelManager,
                               ComponentsManager componentsManager) {
    this.fUndoRedoManager = requireNonNull(undoRedoManager, "undoRedoManager");
    this.modelManager = requireNonNull(modelManager, "modelManager");
    this.componentsManager = requireNonNull(componentsManager, "componentsManager");

    initComponents();
    objectTree.setCellRenderer(new TreeViewCellRenderer());

    // Remove JTree's standard keyboard actions to enable the actions defined
    // in ActionManager
    ActionMap treeActionMap = objectTree.getActionMap();
    treeActionMap.getParent().remove("cut");  // <Ctrl> + X
    treeActionMap.getParent().remove("copy");  // <Ctrl> + C
    treeActionMap.getParent().remove("paste");  // <Ctrl> + V
    treeActionMap.getParent().remove("duplicate");  // <Ctrl> + D
    treeActionMap.getParent().remove("selectAll");  // <Ctrl> + A
    // Add a keyboard handler for the "Delete" action
    InputMap inputMap = objectTree.getInputMap();
    inputMap.getParent().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), DeleteAction.ID);
    inputMap.getParent().put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), DeleteAction.ID);
    treeActionMap.getParent().put(DeleteAction.ID, new DeleteAction());
  }

  @Override
  public JTree getTree() {
    return objectTree;
  }

  /**
   * Copy the selected tree components and the associated figures in the drawing
   * to abuffer.
   *
   * @param doDelete true if the original object will be deleted after
   * copying it to the buffer
   */
  protected void bufferSelectedItems(boolean doDelete) {
    if (objectTree.getSelectionPaths() != null) {
      bufferedUserObjects.clear();
      bufferedFigures.clear();

      for (TreePath treePath : objectTree.getSelectionPaths()) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
        UserObject userObject = (UserObject) node.getUserObject();
        boolean removed = false;
        // Let the user object decide how to remove the item from the tree.
        if (doDelete) {
          removed = userObject.removed();
        }

        if (removed || !doDelete) {
          bufferedUserObjects.add(userObject);
        }
        // Save deleted figures to allow undo-ing the delete operation
        if (doDelete) {
          ModelComponent modelComponent = userObject.getModelComponent();

          if (modelComponent instanceof AbstractConnectableModelComponent) {
            bufferedFigures.add(modelManager.getModel().getFigure(modelComponent));
          }
        }
      }
    }
  }

  @Override
  public boolean hasBufferedObjects() {
    return !bufferedFigures.isEmpty() || !bufferedUserObjects.isEmpty();
  }

  protected void restoreItems(List<UserObject> userObjects, List<Figure> figures) {
    // Restore deleted model components
    bufferedUserObjects = componentsManager.restoreModelComponents(userObjects);
    // Restore the figures associated with these model components
    bufferedFigures.clear();

    for (Figure figure : figures) {
      bufferedFigures.add(figure);
    }
  }

  @Override
  public synchronized void sortItems(TreeNode treeNode) {
    SortableTreeNode sortable = (SortableTreeNode) treeNode;
    // Sort children recursively.

    @SuppressWarnings("unchecked")
    Enumeration<TreeNode> en = sortable.children();

    while (en.hasMoreElements()) {
      TreeNode node = en.nextElement();

      if (node.getChildCount() > 0) {
        sortItems(node);
      }
    }

    if (sortable.isSorting()) {
      if (sortable.getChildCount() > 0) {
        sortable.sort(createSortComparator());
      }
    }

    int size = treeNode.getChildCount();
    boolean[] expanded = new boolean[size];

    for (int i = 0; i < size; i++) {
      DefaultMutableTreeNode child = (DefaultMutableTreeNode) treeNode.getChildAt(i);
      expanded[i] = objectTree.isExpanded(new TreePath(child.getPath()));
    }

    fTreeModel.reload(sortable);

    for (int i = 0; i < expanded.length; i++) {
      DefaultMutableTreeNode child = (DefaultMutableTreeNode) treeNode.getChildAt(i);
      objectTree.expandPath(new TreePath(child.getPath()));
    }
  }

  /**
   * Searches the tree to find the node that is associated with the specified model component.
   *
   * @param dataObject the object to search for.
   * @return the node that holds the dataObject.
   */
  public DefaultMutableTreeNode findFirst(Object dataObject) {
    DefaultMutableTreeNode searchNode = null;

    List<DefaultMutableTreeNode> children = Collections.list(fRootNode.preorderEnumeration()).stream()
        .map(treeNode -> (DefaultMutableTreeNode) treeNode)
        .collect(Collectors.toList());

    for (DefaultMutableTreeNode child : children) {
      UserObject userObject = (UserObject) child.getUserObject();

      // Select point and path "directly", not the entries in a block area.
      if (dataObject != null && dataObject.equals(userObject.getModelComponent())) {
        searchNode = child;
        break;
      }
    }

    return searchNode;
  }

  /**
   * Returns the user object the user clicked on.
   *
   * @param e The mouse event.
   * @return The clicked user object or null, if none was found.
   */
  @Override
  public UserObject getDraggedUserObject(MouseEvent e) {
    return getUserObject();
  }

  @Override
  public void sortRoot() {
    sortItems((TreeNode) objectTree.getModel().getRoot());
  }

  @Override
  public void sortChildren() {
    List<DefaultMutableTreeNode> children = Collections.list(fRootNode.preorderEnumeration()).stream()
        .map(treeNode -> (DefaultMutableTreeNode) treeNode)
        .collect(Collectors.toList());

    for (DefaultMutableTreeNode child : children) {
      sortItems(child);
    }
  }

  @Override // TreeView
  public void addItem(Object parent, UserObject item) {
    if (parent == null) {
      setRoot(item);
    }
    else {
      SimpleFolder folder;
      boolean sorting = true;

      if (item instanceof LayoutUserObject) {
        sorting = false;
      }

      SortableTreeNode treeItem = createTreeNode(item, sorting);
      DefaultMutableTreeNode parentItem = findFirst(parent);

      if (parent instanceof LayoutModel) {

        folder = ((SimpleFolder) ((SimpleFolderUserObject) item).getModelComponent());

        if (Objects.equals(folder,
                           modelManager.getModel().getMainFolder(POINTS))) {
          fTreeModel.insertNodeInto(treeItem, parentItem, 0);
        }
        else if (Objects.equals(folder,
                                modelManager.getModel().getMainFolder(PATHS))) {
          insertElementAt(treeItem, parentItem, 1);
        }
        else if (Objects.equals(folder,
                                modelManager.getModel().getMainFolder(LOCATIONS))) {
          insertElementAt(treeItem, parentItem, 2);
        }
        else if (Objects.equals(folder,
                                modelManager.getModel().getMainFolder(LOCATION_TYPES))) {
          insertElementAt(treeItem, parentItem, 3);
        }
        else if (Objects.equals(folder,
                                modelManager.getModel().getMainFolder(LINKS))) {
          insertElementAt(treeItem, parentItem, 4);
        }
        else if (Objects.equals(folder,
                                modelManager.getModel().getMainFolder(OTHER_GRAPHICAL_ELEMENTS))) {
          insertElementAt(treeItem, parentItem, 7);
        }
      }
      else {
        if (parentItem == null) {
          return;
        }
        fTreeModel.insertNodeInto(treeItem, parentItem, parentItem.getChildCount());
        if (parent instanceof ModelComponent) {
          item.setParent((ModelComponent) parent);
        }
      }

      objectTree.scrollPathToVisible(new TreePath(treeItem.getPath()));
    }
  }

  @Override // TreeView
  public void removeItem(Object item) {
    List<DefaultMutableTreeNode> myList = (item instanceof UserObject)
        ? findAll(((UserObject) item))
        : findAll(item);

    for (DefaultMutableTreeNode node : myList) {
      fTreeModel.removeNodeFromParent(node);
    }
  }

  @Override // TreeView
  public void removeChildren(Object item) {
    DefaultMutableTreeNode node = findFirst(item);
    int size = node.getChildCount();

    for (int i = size - 1; i > -1; i--) {
      fTreeModel.removeNodeFromParent((DefaultMutableTreeNode) node.getChildAt(i));
    }
  }

  @Override // TreeView
  public void selectItem(Object item) {
    DefaultMutableTreeNode itemToSelect = findFirst(item);

    if (itemToSelect == null) {
      return;
    }

    TreePath treePath = new TreePath(itemToSelect.getPath());
    objectTree.setSelectionPath(treePath);
    objectTree.scrollPathToVisible(treePath);
  }

  @Override
  public void selectItems(Set<?> items) {
    objectTree.removeSelectionPaths(objectTree.getSelectionPaths());

    if (items == null) {
      return;
    }
    for (Object item : items) {
      DefaultMutableTreeNode itemToSelect = findFirst(item);

      if (itemToSelect == null) {
        break;
      }

      TreePath treePath = new TreePath(itemToSelect.getPath());
      objectTree.addSelectionPath(treePath);
    }
  }

  @Override // TreeView
  public void itemChanged(Object item) {
    for (DefaultMutableTreeNode node : findAll(item)) {
      sortItems(node.getParent());
    }
  }

  @Override // TreeView
  public UserObject getSelectedItem() {
    TreePath treePath = objectTree.getSelectionPath();

    if (treePath != null) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
      return (UserObject) node.getUserObject();
    }
    else {
      return null;
    }
  }

  @Override
  public Set<UserObject> getSelectedItems() {
    Set<UserObject> objects = new HashSet<>();
    TreePath[] selectionPaths = objectTree.getSelectionPaths();

    if (selectionPaths != null) {
      for (TreePath path : selectionPaths) {
        if (path != null) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
          objects.add((UserObject) node.getUserObject());
        }
      }
    }

    return objects;
  }

  /**
   * Updates the model name.
   *
   * @param text The new name.
   */
  @Override
  public void updateText(String text) {
    textFieldModelName.setText(text);
  }

  /**
   * Adds an element at the specified index or the end if the parent item has fewer elements.
   *
   * @param treeItem The item to add.
   * @param parentItem the parent item.
   * @param index The index at which to add the item.
   */
  private void insertElementAt(SortableTreeNode treeItem,
                               DefaultMutableTreeNode parentItem,
                               int index) {
    if (parentItem.getChildCount() < index) {
      fTreeModel.insertNodeInto(treeItem, parentItem, parentItem.getChildCount());
    }
    else {
      fTreeModel.insertNodeInto(treeItem, parentItem, index);
    }
  }

  private void setRoot(UserObject root) {
    fRootNode = new SortableTreeNode(root);
    // The root node should not be re-sorted.
    fRootNode.setSorting(false);
    fTreeModel = new DefaultTreeModel(fRootNode);
    objectTree.setModel(fTreeModel);
  }

  /**
   * Creates a new tree node.
   *
   * @param item The item the node should represent.
   * @param sorting Whether or not the child components should be sorted.
   * @return The new tree node.
   */
  private SortableTreeNode createTreeNode(UserObject item, boolean sorting) {
    SortableTreeNode treeNode = new SortableTreeNode(item);
    treeNode.setSorting(sorting);

    return treeNode;
  }

  /**
   * Creates a new comparator to sort the tree.
   *
   * @return a new comparator to sort the tree.
   */
  private Comparator<Object> createSortComparator() {
    return new AscendingTreeViewNameComparator();
  }

  /**
   * Searches the tree to find all the nodes that are associated with the specified data object.
   *
   * @param dataObject The object to search for.
   * @return A list of all tree nodes that are associated with the data object.
   */
  private List<DefaultMutableTreeNode> findAll(Object dataObject) {
    List<DefaultMutableTreeNode> searchNodes = new ArrayList<>();

    List<DefaultMutableTreeNode> children = Collections.list(fRootNode.preorderEnumeration()).stream()
        .map(treeNode -> (DefaultMutableTreeNode) treeNode)
        .collect(Collectors.toList());

    for (DefaultMutableTreeNode child : children) {
      UserObject userObject = (UserObject) child.getUserObject();

      if (dataObject.equals(userObject.getModelComponent())) {
        searchNodes.add(child);
      }
    }

    return searchNodes;
  }

  /**
   * Looks for nodes that contain the given user object.
   *
   * @param o The user object to look for.
   * @return All nodes that contain this user object.
   */
  private List<DefaultMutableTreeNode> findAll(UserObject o) {
    List<DefaultMutableTreeNode> searchNodes = new ArrayList<>();

    List<DefaultMutableTreeNode> children = Collections.list(fRootNode.preorderEnumeration()).stream()
        .map(treeNode -> (DefaultMutableTreeNode) treeNode)
        .collect(Collectors.toList());

    for (DefaultMutableTreeNode child : children) {
      UserObject userObject = (UserObject) child.getUserObject();

      if (userObject == o) {
        searchNodes.add(child);
      }
    }

    return searchNodes;
  }

  /**
   * Return the user object for the selected path.
   *
   * @return the user object for the selected path.
   */
  private UserObject getUserObject() {
    DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) objectTree.getLastSelectedPathComponent();

    return treeNode != null ? (UserObject) treeNode.getUserObject() : null;
  }

  /**
   * Called by delete(): Undo/Redo the "Delete" action.
   */
  protected class DeleteEdit
      extends AbstractUndoableEdit {

    private final List<UserObject> userObjects = new ArrayList<>();
    private final List<Figure> figures = new ArrayList<>();

    public DeleteEdit(List<UserObject> userObjects, List<Figure> figures) {
      this.userObjects.addAll(userObjects);
      this.figures.addAll(figures);
    }

    @Override
    public String getPresentationName() {
      return ResourceBundleUtil.getBundle(I18nPlantOverview.MENU_PATH).getString("abstractTreeViewPanel.deleteEdit.presentationName");
    }

    @Override
    public void undo()
        throws CannotUndoException {
      super.undo();
      restoreItems(userObjects, figures);
    }

    @Override
    public void redo()
        throws CannotRedoException {
      super.redo();
      // TODO: Delete again ...
      for (UserObject userObject : userObjects) {
        userObject.removed();
      }
    }
  }

  /**
   * Called by paste(): Undo/Redo the "Paste" action
   */
  protected class PasteEdit
      extends AbstractUndoableEdit {

    private final List<UserObject> userObjects = new ArrayList<>();
    private final List<Figure> figures = new ArrayList<>();

    public PasteEdit(List<UserObject> userObjects, List<Figure> figures) {
      this.userObjects.addAll(userObjects);
      this.figures.addAll(figures);
    }

    @Override
    public String getPresentationName() {
      return ResourceBundleUtil.getBundle(I18nPlantOverview.MENU_PATH).getString("abstractTreeViewPanel.pasteEdit.presentationName");
    }

    @Override
    public void undo()
        throws CannotUndoException {
      super.undo();

      for (UserObject userObject : userObjects) {
        userObject.removed();

        Figure figure = modelManager.getModel().getFigure(userObject.getModelComponent());
        if (figure != null) {
          figures.add(figure);
        }
      }
    }

    @Override
    public void redo()
        throws CannotRedoException {
      super.redo();
      restoreItems(userObjects, figures);
    }
  }

  // CHECKSTYLE:OFF
  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    scrollPaneTree = new javax.swing.JScrollPane();
    objectTree = new StandardActionTree(this);
    textFieldModelName = new javax.swing.JTextField();

    setLayout(new java.awt.BorderLayout());

    scrollPaneTree.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
    scrollPaneTree.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

    objectTree.setRootVisible(false);
    scrollPaneTree.setViewportView(objectTree);

    add(scrollPaneTree, java.awt.BorderLayout.CENTER);

    textFieldModelName.setEditable(false);
    textFieldModelName.setBackground(new java.awt.Color(153, 153, 255));
    textFieldModelName.setFont(textFieldModelName.getFont().deriveFont(textFieldModelName.getFont().getStyle() | java.awt.Font.BOLD));
    textFieldModelName.setForeground(new java.awt.Color(255, 255, 255));
    textFieldModelName.setText("Model");
    add(textFieldModelName, java.awt.BorderLayout.PAGE_START);
  }// </editor-fold>//GEN-END:initComponents
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JTree objectTree;
  private javax.swing.JScrollPane scrollPaneTree;
  private javax.swing.JTextField textFieldModelName;
  // End of variables declaration//GEN-END:variables
  // CHECKSTYLE:ON
}
