/**
 * (c): IML.
 *
 */
package org.opentcs.guing.application.action.file;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.opentcs.guing.application.GuiManager;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
public class SaveModelAsAction
    extends AbstractAction {

  public final static String ID = "file.saveModelAs";
  /**
   * The manager this instance is working with.
   */
  private final GuiManager guiManager;

  /**
   * Creates a new instance.
   *
   * @param manager
   */
  public SaveModelAsAction(final GuiManager manager) {
    this.guiManager = manager;
    ResourceBundleUtil.getBundle().configureAction(this, ID);
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    guiManager.saveModelAs();
  }
}
