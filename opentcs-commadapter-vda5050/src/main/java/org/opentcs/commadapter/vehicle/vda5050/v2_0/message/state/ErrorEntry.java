// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nonnull;

/**
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorEntry
    implements
      Serializable {

  /**
   * The type/name of the error.
   */
  private String errorType;
  /**
   * [Optional] List of references to identify the source of the error. (E.g. headerId, orderId,
   * actionId.)
   */
  private List<ErrorReference> errorReferences;
  /**
   * [Optional] Verbose description of the error.
   */
  private String errorDescription;
  /**
   * The error level.
   */
  private ErrorLevel errorLevel;

  @JsonCreator
  public ErrorEntry(
      @Nonnull
      @JsonProperty(required = true, value = "errorType")
      String errorType,
      @Nonnull
      @JsonProperty(required = true, value = "errorLevel")
      ErrorLevel errorLevel
  ) {
    this.errorType = requireNonNull(errorType, "errorType");
    this.errorLevel = requireNonNull(errorLevel, "errorLevel");
  }

  public String getErrorType() {
    return errorType;
  }

  public ErrorEntry setErrorType(
      @Nonnull
      String errorType
  ) {
    this.errorType = requireNonNull(errorType, "errorType");
    return this;
  }

  public List<ErrorReference> getErrorReferences() {
    return errorReferences;
  }

  public ErrorEntry setErrorReferences(List<ErrorReference> errorReferences) {
    this.errorReferences = errorReferences;
    return this;
  }

  public String getErrorDescription() {
    return errorDescription;
  }

  public ErrorEntry setErrorDescription(String errorDescription) {
    this.errorDescription = errorDescription;
    return this;
  }

  public ErrorLevel getErrorLevel() {
    return errorLevel;
  }

  public ErrorEntry setErrorLevel(
      @Nonnull
      ErrorLevel errorLevel
  ) {
    this.errorLevel = requireNonNull(errorLevel, "errorLevel");
    return this;
  }

  @Override
  public String toString() {
    return "Error{" + "errorType=" + errorType
        + ", errorReferences=" + errorReferences
        + ", errorDescription=" + errorDescription
        + ", errorLevel=" + errorLevel
        + '}';
  }

}
