/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.to.order;

import java.io.Serializable;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.access.to.CreationTO;

/**
 * A transfer object describing a destination of a drive order.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DestinationCreationTO
    extends CreationTO
    implements Serializable {

  /**
   * The name of the destination location (or point).
   */
  @Nonnull
  private String destLocationName;
  /**
   * The operation to be performed at the destination.
   */
  @Nonnull
  private String destOperation;

  /**
   * Creates a new instance.
   *
   * @param destLocationName The name of the destination location (or destination point).
   * @param destOperation The operation to be performed at the destination.
   */
  public DestinationCreationTO(@Nonnull String destLocationName,
                               @Nonnull String destOperation) {
    super("");
    this.destLocationName = requireNonNull(destLocationName, "destLocationName");
    this.destOperation = requireNonNull(destOperation, "destOperation");
  }

  @Nonnull
  @Override
  public DestinationCreationTO setName(@Nonnull String name) {
    return (DestinationCreationTO) super.setName(name);
  }

  @Nonnull
  @Override
  public DestinationCreationTO setProperties(@Nonnull Map<String, String> properties) {
    return (DestinationCreationTO) super.setProperties(properties);
  }

  @Nonnull
  @Override
  public DestinationCreationTO setProperty(@Nonnull String key, @Nonnull String value) {
    return (DestinationCreationTO) super.setProperty(key, value);
  }

  /**
   * Returns the destination location (or point) name.
   *
   * @return The destination location (or point) name.
   */
  @Nonnull
  public String getDestLocationName() {
    return destLocationName;
  }

  /**
   * Sets the destination location (or point) name.
   *
   * @param destLocationName The destination location (or point) name.
   * @return This instance.
   */
  @Nonnull
  public DestinationCreationTO setDestLocationName(@Nonnull String destLocationName) {
    this.destLocationName = requireNonNull(destLocationName, "destLocationName");
    return this;
  }

  /**
   * Returns the operation to be performed at the destination.
   *
   * @return The operation to be performed at the destination.
   */
  @Nonnull
  public String getDestOperation() {
    return destOperation;
  }

  /**
   * Sets the operation to be performed at the destination.
   *
   * @param destOperation The operation.
   * @return This instance.
   */
  @Nonnull
  public DestinationCreationTO setDestOperation(@Nonnull String destOperation) {
    this.destOperation = requireNonNull(destOperation, "destOperation");
    return this;
  }

}
