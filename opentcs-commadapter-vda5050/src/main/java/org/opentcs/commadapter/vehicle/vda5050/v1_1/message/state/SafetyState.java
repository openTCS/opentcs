// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.annotation.Nonnull;

/**
 * Holds information about the safety status.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SafetyState
    implements
      Serializable {

  /**
   * Acknowledge-type of e-stop.
   */
  private EStop eStop;
  /**
   * Protective field violation.
   * <p>
   * {@code true}, if the field is violated.
   */
  private Boolean fieldViolation;

  @JsonCreator
  public SafetyState(
      @Nonnull
      @JsonProperty(required = true, value = "eStop")
      EStop eStop,
      @Nonnull
      @JsonProperty(required = true, value = "fieldViolation")
      Boolean fieldViolation
  ) {
    this.eStop = requireNonNull(eStop, "eStop");
    this.fieldViolation = requireNonNull(fieldViolation, "fieldViolation");
  }

  public EStop geteStop() {
    return eStop;
  }

  public SafetyState seteStop(
      @Nonnull
      EStop eStop
  ) {
    this.eStop = requireNonNull(eStop, "eStop");
    return this;
  }

  public Boolean isFieldViolation() {
    return fieldViolation;
  }

  public SafetyState setFieldViolation(
      @Nonnull
      Boolean fieldViolation
  ) {
    this.fieldViolation = requireNonNull(fieldViolation, "fieldViolation");
    return this;
  }

  @Override
  public String toString() {
    return "SafetyState{" + "eStop=" + eStop + ", fieldViolation=" + fieldViolation + '}';
  }

}
