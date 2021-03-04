/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernelcontrolcenter.vehicles;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;
import org.opentcs.drivers.vehicle.management.AttachmentInformation;
import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;

/**
 * An entry for a vehicle present in kernel with detailed information about its attachment state
 * and latest process model.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class LocalVehicleEntry {

  /**
   * Used for implementing property change events.
   */
  private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
  /**
   * Detailed information about the attachment state.
   */
  private AttachmentInformation attachmentInformation;
  /**
   * The current process model to this entry.
   */
  private VehicleProcessModelTO processModel;

  /**
   * Creates a new instance.
   *
   * @param attachmentInformation Detailed information about the attachment state.
   * @param processModel The current process model to this entry.
   */
  public LocalVehicleEntry(AttachmentInformation attachmentInformation,
                           VehicleProcessModelTO processModel) {
    this.attachmentInformation = requireNonNull(attachmentInformation, "attachmentInformation");
    this.processModel = requireNonNull(processModel, "processModel");
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    pcs.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    pcs.removePropertyChangeListener(listener);
  }

  @Nonnull
  public AttachmentInformation getAttachmentInformation() {
    return attachmentInformation;
  }

  @Nonnull
  public VehicleProcessModelTO getProcessModel() {
    return processModel;
  }

  @Nonnull
  public String getVehicleName() {
    return attachmentInformation.getVehicleReference().getName();
  }

  @Nonnull
  public VehicleCommAdapterDescription getAttachedCommAdapterDescription() {
    return attachmentInformation.getAttachedCommAdapter();
  }

  public void setAttachmentInformation(@Nonnull AttachmentInformation attachmentInformation) {
    AttachmentInformation oldAttachmentInformation = this.attachmentInformation;
    this.attachmentInformation = requireNonNull(attachmentInformation, "attachmentInformation");

    pcs.firePropertyChange(Attribute.ATTACHMENT_INFORMATION.name(),
                           oldAttachmentInformation,
                           attachmentInformation);
  }

  public void setProcessModel(@Nonnull VehicleProcessModelTO processModel) {
    VehicleProcessModelTO oldProcessModel = this.processModel;
    this.processModel = requireNonNull(processModel, "processModel");

    pcs.firePropertyChange(Attribute.PROCESS_MODEL.name(),
                           oldProcessModel,
                           processModel);
  }

  /**
   * Enum elements used as notification arguments to specify which argument changed.
   */
  public static enum Attribute {
    /**
     * Indicates a change of the process model reference.
     */
    PROCESS_MODEL,
    /**
     * Indicates a change of the attachment information reference.
     */
    ATTACHMENT_INFORMATION
  }
}
