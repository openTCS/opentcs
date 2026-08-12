// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * Represents information messages.
 * <p>
 * Information messages are only for visualization/debugging.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InfoEntry
    implements
      Serializable {

  /**
   * The type/name of the information.
   */
  private String infoType;
  /**
   * [Optional] Verbose description of the information.
   */
  private String infoDescription;
  /**
   * The level of the information.
   */
  private InfoLevel infoLevel;
  /**
   * [Optional] List of information references.
   */
  private List<InfoReference> infoReferences;

  @JsonCreator
  public InfoEntry(
      @Nonnull
      @JsonProperty(required = true, value = "infoType")
      String infoType,
      @Nonnull
      @JsonProperty(required = true, value = "infoLevel")
      InfoLevel infoLevel
  ) {
    this.infoType = requireNonNull(infoType, "infoType");
    this.infoLevel = requireNonNull(infoLevel, "infoLevel");
  }

  public String getInfoType() {
    return infoType;
  }

  public InfoEntry setInfoType(
      @Nonnull
      String infoType
  ) {
    this.infoType = requireNonNull(infoType, "infoType");
    return this;
  }

  public String getInfoDescription() {
    return infoDescription;
  }

  public InfoEntry setInfoDescription(String infoDescription) {
    this.infoDescription = infoDescription;
    return this;
  }

  public InfoLevel getInfoLevel() {
    return infoLevel;
  }

  public InfoEntry setInfoLevel(
      @Nonnull
      InfoLevel infoLevel
  ) {
    this.infoLevel = requireNonNull(infoLevel, "infoLevel");
    return this;
  }

  public List<InfoReference> getInfoReferences() {
    return infoReferences;
  }

  public InfoEntry setInfoReferences(List<InfoReference> infoReferences) {
    this.infoReferences = infoReferences;
    return this;
  }

  @Override
  public String toString() {
    return "Information{" + "infoType=" + infoType
        + ", infoDescription=" + infoDescription
        + ", infoLevel=" + infoLevel
        + ", infoReferences=" + infoReferences
        + '}';
  }

}
