/*
 *
 * Created on 18.09.2013 11:22:08
*/
package org.opentcs.guing.components.drawing;

/**
 * An utility class for managing the behaviour with different
 * <code>OpenTCSDrawingView</code>s.
 * 
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class OpenTCSDockableUtil {
  
  private OpenTCSDrawingView drawingView;
  private Ruler horizontalRuler;
  private Ruler verticalRuler;
  
  public OpenTCSDockableUtil(OpenTCSDrawingView drawingView,
                             Ruler horizontalRuler,
                             Ruler verticalRuler) {
    this.drawingView = drawingView;
    this.horizontalRuler = horizontalRuler;
    this.verticalRuler = verticalRuler;
  }

  public OpenTCSDrawingView getDrawingView() {
    return drawingView;
  }

  public void setDrawingView(OpenTCSDrawingView drawingView) {
    this.drawingView = drawingView;
  }

  public Ruler getHorizontalRuler() {
    return horizontalRuler;
  }

  public void setHorizontalRuler(Ruler horizontalRuler) {
    this.horizontalRuler = horizontalRuler;
  }

  public Ruler getVerticalRuler() {
    return verticalRuler;
  }

  public void setVerticalRuler(Ruler verticalRuler) {
    this.verticalRuler = verticalRuler;
  }
}


