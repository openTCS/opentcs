/**
 * (c): IML.
 *
 */
package org.opentcs.guing.application.action;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Objects;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.tool.Tool;

/**
 * A listener if a tool was (de)selected.
 * 
 * @author Heinz Huber (Fraunhofer IML)
 * @author Philipp Seifert (Fraunhofer IML)
 */
final class ToolButtonListener
    implements ItemListener {

  private final Tool tool;
  private final DrawingEditor editor;

  public ToolButtonListener(Tool tool, DrawingEditor editor) {
    this.tool = Objects.requireNonNull(tool, "tool is null");
    this.editor = Objects.requireNonNull(editor, "editor is null");
  }

  @Override
  public void itemStateChanged(ItemEvent evt) {
    if (evt.getStateChange() == ItemEvent.SELECTED) {
      editor.setTool(tool);
    }
  }
}
