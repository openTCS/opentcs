/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.order.OrderConstants;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.Property;

/**
 * The current state of an order sequence.
 */
public class GetOrderSequenceResponseTO {

  @Nonnull
  private String name;

  @Nonnull
  private String type = OrderConstants.TYPE_NONE;

  @Nonnull
  private List<String> orders = List.of();

  private int finishedIndex;

  private boolean complete;

  private boolean finished;

  private boolean failureFatal;

  @Nullable
  private String intendedVehicle;

  @Nullable
  private String processingVehicle;

  @Nonnull
  private List<Property> properties = List.of();

  public GetOrderSequenceResponseTO(@Nonnull String name) {
    this.name = requireNonNull(name, "name");
  }

  @Nonnull
  public String getName() {
    return name;
  }

  public GetOrderSequenceResponseTO setName(@Nonnull String name) {
    this.name = requireNonNull(name, "name");
    return this;
  }

  @Nonnull
  public String getType() {
    return type;
  }

  public GetOrderSequenceResponseTO setType(@Nonnull String type) {
    this.type = requireNonNull(type, "type");
    return this;
  }

  @Nonnull
  public List<String> getOrders() {
    return orders;
  }

  public GetOrderSequenceResponseTO setOrders(@Nonnull List<String> orders) {
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

  @Nullable
  public String getIntendedVehicle() {
    return intendedVehicle;
  }

  public GetOrderSequenceResponseTO setIntendedVehicle(@Nullable String intendedVehicle) {
    this.intendedVehicle = intendedVehicle;
    return this;
  }

  @Nullable
  public String getProcessingVehicle() {
    return processingVehicle;
  }

  public GetOrderSequenceResponseTO setProcessingVehicle(@Nullable String processingVehicle) {
    this.processingVehicle = processingVehicle;
    return this;
  }

  @Nonnull
  public List<Property> getProperties() {
    return properties;
  }

  public GetOrderSequenceResponseTO setProperties(@Nonnull List<Property> properties) {
    this.properties = requireNonNull(properties, "properties");
    return this;
  }

  public static GetOrderSequenceResponseTO fromOrderSequence(OrderSequence orderSequence) {
    return new GetOrderSequenceResponseTO(orderSequence.getName())
        .setComplete(orderSequence.isComplete())
        .setFailureFatal(orderSequence.isFailureFatal())
        .setFinished(orderSequence.isFinished())
        .setFinishedIndex(orderSequence.getFinishedIndex())
        .setType(orderSequence.getType())
        .setOrders(orderSequence.getOrders()
            .stream()
            .map(TCSObjectReference::getName)
            .collect(Collectors.toList()))
        .setProcessingVehicle(nameOfNullableReference(orderSequence.getProcessingVehicle()))
        .setIntendedVehicle(nameOfNullableReference(orderSequence.getIntendedVehicle()))
        .setProperties(convertProperties(orderSequence.getProperties()));
  }

  private static String nameOfNullableReference(@Nullable TCSObjectReference<?> reference) {
    return reference == null ? null : reference.getName();
  }

  private static List<Property> convertProperties(Map<String, String> properties) {
    return properties.entrySet().stream()
        .map(property -> new Property(property.getKey(), property.getValue()))
        .collect(Collectors.toList());
  }

}
