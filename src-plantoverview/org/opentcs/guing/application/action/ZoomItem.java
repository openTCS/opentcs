/**
 * (c): IML.
 *
 */
package org.opentcs.guing.application.action;

/**
 * An item to show in a combo box.
 * 
 * @author Heinz Huber (Fraunhofer IML)
 * @author Philipp Seifert (Fraunhofer IML)
 */
final class ZoomItem {

  private final double scaleFactor;

  public ZoomItem(double scaleFactor) {
    this.scaleFactor = scaleFactor;
  }

  public double getScaleFactor() {
    return scaleFactor;
  }

  @Override
  public String toString() {
    String s = String.format("%d %%", (int) (scaleFactor * 100));
    //      System.out.println(s);
    return s;
  }
}
