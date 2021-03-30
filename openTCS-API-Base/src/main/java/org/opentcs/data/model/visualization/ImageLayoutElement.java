/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.model.visualization;

import java.io.Serializable;
import java.util.Objects;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * A layout element describing an image to be displayed.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @deprecated Will be removed.
 */
@Deprecated
@ScheduledApiChange(details = "Will be removed.", when = "6.0")
public class ImageLayoutElement
    extends LayoutElement
    implements Serializable {

  /**
   * The image to be displayed.
   */
  private ImageData imageData = new ImageData();

  /**
   * Creates a new ImageLayoutElement.
   */
  public ImageLayoutElement() {
    // Do nada.
  }

  /**
   * Returns this layout element's image data.
   *
   * @return This layout element's image data.
   */
  public ImageData getImageData() {
    return imageData;
  }

  /**
   * Sets this layout element's image data.
   *
   * @param imageData The new image data.
   */
  public void setImageData(ImageData imageData) {
    this.imageData = Objects.requireNonNull(imageData, "imageData is null");
  }
}
