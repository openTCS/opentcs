// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.annotation.Nonnull;

/**
 * Describes a reference to an error.
 */
public class ErrorReference
    implements
      Serializable {

  /**
   * The reference key.
   */
  private String referenceKey;
  /**
   * The reference value.
   */
  private String referenceValue;

  @JsonCreator
  public ErrorReference(
      @Nonnull
      @JsonProperty(required = true, value = "referenceKey")
      String referenceKey,
      @Nonnull
      @JsonProperty(required = true, value = "referenceValue")
      String referenceValue
  ) {
    this.referenceKey = requireNonNull(referenceKey, "referenceKey");
    this.referenceValue = requireNonNull(referenceValue, "referenceValue");
  }

  public String getReferenceKey() {
    return referenceKey;
  }

  public ErrorReference setReferenceKey(
      @Nonnull
      String referenceKey
  ) {
    this.referenceKey = requireNonNull(referenceKey, "referenceKey");
    return this;
  }

  public String getReferenceValue() {
    return referenceValue;
  }

  public ErrorReference setReferenceValue(
      @Nonnull
      String referenceValue
  ) {
    this.referenceValue = requireNonNull(referenceValue, "referenceValue");
    return this;
  }

  @Override
  public String toString() {
    return "ErrorReference{" + "referenceKey=" + referenceKey
        + ", referenceValue=" + referenceValue
        + '}';
  }

}
