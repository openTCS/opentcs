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

/**
 * Generic image data that can be stored along with a layout.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ImageData
    implements Serializable {

  /**
   * The binary data describing the actual image.
   */
  private byte[] content = new byte[0];
  /**
   * The type of image.
   * Examples for possible values: "SVG", "WMF", "PNG", "JPEG".
   */
  private String contentFormat = "";
  /**
   * A label/name for this image.
   */
  private String label = "";

  /**
   * Creates a new ImageData.
   */
  public ImageData() {
    // Do nada.
  }

  /**
   * Returns the binary data describing the actual image.
   *
   * @return The binary data describing the actual image.
   */
  public byte[] getContent() {
    return content;
  }

  /**
   * Sets the binary data describing the actual image.
   *
   * @param content The new binary data.
   */
  public void setContent(byte[] content) {
    this.content = Objects.requireNonNull(content, "content is null");
  }

  /**
   * Returns the content type/format of this image.
   * Examples for possible values: "SVG", "WMF", "PNG", "JPEG".
   *
   * @return The type of this image.
   */
  public String getContentFormat() {
    return contentFormat;
  }

  /**
   * Sets this image's content type/format.
   *
   * @param contentFormat The new type/format.
   */
  public void setContentFormat(String contentFormat) {
    this.contentFormat = Objects.requireNonNull(contentFormat,
                                                "contentFormat is null");
  }

  /**
   * Returns a label/name for this image.
   *
   * @return A label/name for this image.
   */
  public String getLabel() {
    return label;
  }

  /**
   * Sets a label/name for this image.
   *
   * @param label The new label.
   */
  public void setLabel(String label) {
    this.label = Objects.requireNonNull(label, "label is null");
  }
}
