// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state;

import static org.opentcs.commadapter.vehicle.vda5050.common.Limits.UINT32_MAX_VALUE;
import static org.opentcs.util.Assertions.checkInRange;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;

/**
 * Load object that describes the load if the AGV has information about it.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Load
    implements
      Serializable {

  /**
   * [Optional] Unique identification number of the load. (E.g. barcode or RFID).
   * <p>
   * Empty, if the AGV can identify the load but didn't identify the load, yet. Optional, if the
   * AGV cannot identify the load.
   */
  private String loadId;
  /**
   * [Optional] Type of the load.
   */
  private String loadType;
  /**
   * [Optional] Indicates which load handling/carrying unit of the AGV is used. (E.g. in case the
   * AGV has multiple spots/positions to carry loads.
   * <p>
   * Examples: "front", "back", "positionC1". Optional for vehicles with only one load position.
   */
  private String loadPosition;
  /**
   * [Optional] Absolute weight of the load measured (in kg). Range: [0.0 ... infinity]
   */
  private Long weight;
  /**
   * [Optional] Point of reference for the location of the bounding box.
   * <p>
   * The point of reference is always the center of the bounding box's bottom surface
   * (at height = 0) and is described in coordinates of the AGV's coordinate system.
   */
  private BoundingBoxReference boundingBoxReference;
  /**
   * [Optional] Dimensions of the load's bounding box (in m).
   */
  private LoadDimensions loadDimensions;

  @JsonCreator
  public Load() {
  }

  public Long getWeight() {
    return weight;
  }

  public Load setWeight(Long weight) {
    if (weight != null) {
      checkInRange(weight, 0L, UINT32_MAX_VALUE, "weight");
    }
    this.weight = weight;
    return this;
  }

  public String getLoadId() {
    return loadId;
  }

  public Load setLoadId(String loadId) {
    this.loadId = loadId;
    return this;
  }

  public String getLoadType() {
    return loadType;
  }

  public Load setLoadType(String loadType) {
    this.loadType = loadType;
    return this;
  }

  public String getLoadPosition() {
    return loadPosition;
  }

  public Load setLoadPosition(String loadPosition) {
    this.loadPosition = loadPosition;
    return this;
  }

  public BoundingBoxReference getBoundingBoxReference() {
    return boundingBoxReference;
  }

  public Load setBoundingBoxReference(BoundingBoxReference boundingBoxReference) {
    this.boundingBoxReference = boundingBoxReference;
    return this;
  }

  public LoadDimensions getLoadDimensions() {
    return loadDimensions;
  }

  public Load setLoadDimensions(LoadDimensions loadDimensions) {
    this.loadDimensions = loadDimensions;
    return this;
  }

  @Override
  public String toString() {
    return "Load{" + "loadId=" + loadId
        + ", loadType=" + loadType
        + ", loadPosition=" + loadPosition
        + ", weight=" + weight
        + ", boundingBoxReference=" + boundingBoxReference
        + ", loadDimensions=" + loadDimensions
        + '}';
  }

}
