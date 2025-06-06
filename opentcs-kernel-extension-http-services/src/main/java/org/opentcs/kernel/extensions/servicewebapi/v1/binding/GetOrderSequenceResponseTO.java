// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.List;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.OrderConstantsTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.Property;

/**
 * The current state of an order sequence.
 */
public class GetOrderSequenceResponseTO {

  @Nonnull
  private String name;

  @Nonnull
  private String type = OrderConstantsTO.TYPE_NONE;

  @Nonnull
  private List<String> orders = List.of();

  private int finishedIndex;

  private boolean complete;

  private boolean finished;

  private boolean failureFatal;

  private Instant creationTime;

  private Instant finishedTime;

  @Nullable
  private String intendedVehicle;

  @Nullable
  private String processingVehicle;

  @Nonnull
  private List<Property> properties = List.of();

  public GetOrderSequenceResponseTO(
      @Nonnull
      String name
  ) {
    this.name = requireNonNull(name, "name");
  }

  @Nonnull
  public String getName() {
    return name;
  }

  public GetOrderSequenceResponseTO setName(
      @Nonnull
      String name
  ) {
    this.name = requireNonNull(name, "name");
    return this;
  }

  @Nonnull
  public String getType() {
    return type;
  }

  public GetOrderSequenceResponseTO setType(
      @Nonnull
      String type
  ) {
    this.type = requireNonNull(type, "type");
    return this;
  }

  @Nonnull
  public List<String> getOrders() {
    return orders;
  }

  public GetOrderSequenceResponseTO setOrders(
      @Nonnull
      List<String> orders
  ) {
    this.orders = requireNonNull(orders, "orders");
    return this;
  }

  public int getFinishedIndex() {
    return finishedIndex;
  }

  public GetOrderSequenceResponseTO setFinishedIndex(int finishedIndex) {
    this.finishedIndex = finishedIndex;
    return this;
  }

  public boolean isComplete() {
    return complete;
  }

  public GetOrderSequenceResponseTO setComplete(boolean complete) {
    this.complete = complete;
    return this;
  }

  public boolean isFinished() {
    return finished;
  }

  public GetOrderSequenceResponseTO setFinished(boolean finished) {
    this.finished = finished;
    return this;
  }

  public boolean isFailureFatal() {
    return failureFatal;
  }

  public GetOrderSequenceResponseTO setFailureFatal(boolean failureFatal) {
    this.failureFatal = failureFatal;
    return this;
  }

  public Instant getCreationTime() {
    return creationTime;
  }

  public GetOrderSequenceResponseTO setCreationTime(Instant creationTime) {
    this.creationTime = creationTime;
    return this;
  }

  public Instant getFinishedTime() {
    return finishedTime;
  }

  public GetOrderSequenceResponseTO setFinishedTime(Instant finishedTime) {
    this.finishedTime = finishedTime;
    return this;
  }

  @Nullable
  public String getIntendedVehicle() {
    return intendedVehicle;
  }

  public GetOrderSequenceResponseTO setIntendedVehicle(
      @Nullable
      String intendedVehicle
  ) {
    this.intendedVehicle = intendedVehicle;
    return this;
  }

  @Nullable
  public String getProcessingVehicle() {
    return processingVehicle;
  }

  public GetOrderSequenceResponseTO setProcessingVehicle(
      @Nullable
      String processingVehicle
  ) {
    this.processingVehicle = processingVehicle;
    return this;
  }

  @Nonnull
  public List<Property> getProperties() {
    return properties;
  }

  public GetOrderSequenceResponseTO setProperties(
      @Nonnull
      List<Property> properties
  ) {
    this.properties = requireNonNull(properties, "properties");
    return this;
  }
}
