/**
 * (c): IML, JHotDraw.
 *
 */
package org.opentcs.guing.application.action.edit;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.inject.Inject;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import org.opentcs.guing.event.SystemModelTransitionEvent;
import static org.opentcs.guing.event.SystemModelTransitionEvent.Stage.UNLOADING;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.opentcs.util.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Same as javax.swing.UndoManager but provides actions for undo and redo
 * operations.
 *
 * @author Werner Randelshofer
 * @author Heinz Huber (Fraunhofer IML)
 */
public class UndoRedoManager
    extends UndoManager
    implements EventHandler {

  public static final String UNDO_ACTION_ID = "edit.undo";
  public static final String REDO_ACTION_ID = "edit.redo";

  /**
   * Sending this UndoableEdit event to the UndoRedoManager
   * disables the Undo and Redo functions of the manager.
   */
  public final static UndoableEdit DISCARD_ALL_EDITS = new AbstractUndoableEdit() {
    @Override
    public boolean canUndo() {
      return false;
    }

    @Override
    public boolean canRedo() {
      return false;
    }
  };

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(UndoRedoManager.class);

  protected PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);

  /**
   * This flag is set to true when at least one significant UndoableEdit
   * has been added to the manager since the last call to discardAllEdits.
   */
  private boolean hasSignificantEdits = false;

  /**
   * This flag is set to true when an undo or redo operation is in progress.
   * The UndoRedoManager ignores all incoming UndoableEdit events while
   * this flag is true.
   */
  private boolean undoOrRedoInProgress;

  /**
   * The undo action instance.
   */
  private final UndoAction undoAction;
  /**
   * The redo action instance.
   */
  private final RedoAction redoAction;

  /**
   * Creates a new UndoRedoManager.
   */
  @Inject
  public UndoRedoManager() {
    undoAction = new UndoAction();
    redoAction = new RedoAction();
  }

  @Override
  public void discardAllEdits() {
    super.discardAllEdits();
    updateActions();
    setHasSignificantEdits(false);
  }

  @Override
  public void onEvent(Object event) {
    if (event instanceof SystemModelTransitionEvent) {
      handleSystemModelTransition((SystemModelTransitionEvent) event);
    }
  }

  private void handleSystemModelTransition(SystemModelTransitionEvent evt) {
    switch (evt.getStage()) {
      case UNLOADING:
        discardAllEdits();
        break;
      case LOADED:
        discardAllEdits();
        break;
      default:
      // Do nada.
    }
  }

  public void setHasSignificantEdits(boolean newValue) {
    boolean oldValue = hasSignificantEdits;
    hasSignificantEdits = newValue;
    firePropertyChange("hasSignificantEdits", oldValue, newValue);
  }

  /**
   * Returns true if at least one significant UndoableEdit
   * has been added since the last call to discardAllEdits.
   *
   * @return
   */
  public boolean hasSignificantEdits() {
    return hasSignificantEdits;
  }

  /**
   * If inProgress, inserts anEdit at indexOfNextAdd, and removes
   * any old edits that were at indexOfNextAdd or later. The die
   * method is called on each edit that is removed is sent, in the
   * reverse of the order the edits were added. Updates
   * indexOfNextAdd.
   * <p>
   * If not inProgress, acts as a CompoundEdit</p>
   * <p>
   * Regardless of inProgress, if undoOrRedoInProgress,
   * calls die on each edit that is sent.</p>
   *
   * @return
   * @see CompoundEdit#end
   * @see CompoundEdit#addEdit
   */
  @Override
  public boolean addEdit(UndoableEdit anEdit) {
    if (undoOrRedoInProgress) {
      anEdit.die();
      return true;
    }

    boolean success = super.addEdit(anEdit);

    updateActions();

    if (success && anEdit.isSignificant() && editToBeUndone() == anEdit) {
      setHasSignificantEdits(true);
    }

    return success;
  }

  /**
   * Gets the undo action for use as an
   *
   * @return Undo menu item.
   */
  public Action getUndoAction() {
    return undoAction;
  }

  /**
   * Gets the redo action for use as a Redo menu item.
   *
   * @return
   */
  public Action getRedoAction() {
    return redoAction;
  }

  /**
   * Updates the properties of the UndoAction
   * and of the RedoAction.
   */
  private void updateActions() {
    String label;

    if (canUndo()) {
      undoAction.setEnabled(true);
      label = getUndoPresentationName();
    }
    else {
      undoAction.setEnabled(false);
      label = ResourceBundleUtil.getBundle().getString(UNDO_ACTION_ID + ".text");
    }

    undoAction.putValue(Action.NAME, label);
    undoAction.putValue(Action.SHORT_DESCRIPTION, label);

    if (canRedo()) {
      redoAction.setEnabled(true);
      label = getRedoPresentationName();
    }
    else {
      redoAction.setEnabled(false);
      label = ResourceBundleUtil.getBundle().getString(REDO_ACTION_ID + ".text");
    }

    redoAction.putValue(Action.NAME, label);
    redoAction.putValue(Action.SHORT_DESCRIPTION, label);
  }

  /**
   * Undoes the last edit event.
   * The UndoRedoManager ignores all incoming UndoableEdit events,
   * while undo is in progress.
   */
  @Override
  public void undo()
      throws CannotUndoException {
    undoOrRedoInProgress = true;

    try {
      super.undo();
    }
    finally {
      undoOrRedoInProgress = false;
      updateActions();
    }
  }

  /**
   * Redoes the last undone edit event.
   * The UndoRedoManager ignores all incoming UndoableEdit events,
   * while redo is in progress.
   */
  @Override
  public void redo()
      throws CannotUndoException {
    undoOrRedoInProgress = true;

    try {
      super.redo();
    }
    finally {
      undoOrRedoInProgress = false;
      updateActions();
    }
  }

  /**
   * Undoes or redoes the last edit event.
   * The UndoRedoManager ignores all incoming UndoableEdit events,
   * while undo or redo is in progress.
   */
  @Override
  public void undoOrRedo()
      throws CannotUndoException, CannotRedoException {
    undoOrRedoInProgress = true;

    try {
      super.undoOrRedo();
    }
    finally {
      undoOrRedoInProgress = false;
      updateActions();
    }
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    propertySupport.addPropertyChangeListener(listener);
  }

  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    propertySupport.addPropertyChangeListener(propertyName, listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    propertySupport.removePropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    propertySupport.removePropertyChangeListener(propertyName, listener);
  }

  protected void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
    propertySupport.firePropertyChange(propertyName, oldValue, newValue);
  }

  protected void firePropertyChange(String propertyName, int oldValue, int newValue) {
    propertySupport.firePropertyChange(propertyName, oldValue, newValue);
  }

  protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
    propertySupport.firePropertyChange(propertyName, oldValue, newValue);
  }

  /**
   * Undo Action for use in a menu bar.
   */
  private class UndoAction
      extends AbstractAction {

    public UndoAction() {
      ResourceBundleUtil.getBundle().configureAction(this, UNDO_ACTION_ID);
      setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
      try {
        undo();
      }
      catch (CannotUndoException e) {
        LOG.debug("Cannot undo: {}", e);
      }
    }
  }

  /**
   * Redo Action for use in a menu bar.
   */
  private class RedoAction
      extends AbstractAction {

    public RedoAction() {
      ResourceBundleUtil.getBundle().configureAction(this, REDO_ACTION_ID);
      setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
      try {
        redo();
      }
      catch (CannotRedoException e) {
        LOG.debug("Cannot redo: {}", e);
      }
    }
  }

}
