package org.opentcs.peripheralcustomadapter;

import java.util.logging.Logger;

public class LocationSensor1Status {

  private static final Logger LOG = Logger.getLogger(
      LocationSensor1Status.class.getName()
  );

  /**
   * The Status for the EFEM.
   */
  private boolean isMagazineInEFEM;
  /**
   * The Status for the STK Port2.
   */
  private int eFEMMagazineQantity;
  /**
   * The Status for the OHB1.
   */
  private EFEMStatus eFEMStatus;

  /**
   * Creates a new instance.
   */
  public LocationSensor1Status() {
    this.isMagazineInEFEM = false;
    this.eFEMMagazineQantity = 0;
    this.eFEMStatus = EFEMStatus.Idle;
  }

  public void setEFEMMagazineStatus(boolean hasMagazine) {
    this.isMagazineInEFEM = hasMagazine;
  }

  /**
   * Is there Magazine in EFEM.
   *
   * @return EFEM Magazine Status.
   */
  public boolean hasMagazineEFEM() {
    return isMagazineInEFEM;
  }

  public void setEFEMMagazineNumber(int quantity) {
    this.eFEMMagazineQantity = quantity;
  }

  /**
   * Get EFEM Magazine Quantity.
   *
   * @return Magazine Quantity.
   */
  public int getEFEMMagazineQuantity() {
    return eFEMMagazineQantity;
  }

  /**
   * Set EFEM Status.
   *
   */
  public void setEFEMStatus(int status) {
    if (status == 1) {
      this.eFEMStatus = EFEMStatus.Run;
    }
    else if (status == 2) {
      this.eFEMStatus = EFEMStatus.Stop;
    }
    else if (status == 4) {
      this.eFEMStatus = EFEMStatus.Idle;
    }
    else if (status == 8) {
      this.eFEMStatus = EFEMStatus.Alarm;
    }
    else if (status == 16) {
      this.eFEMStatus = EFEMStatus.Warning;
    }
    else {
      throw new IllegalStateException("Unexpected value: " + status);
    }
  }

  /**
   * Get EFEM Status.
   *
   * @return EFEM Status.
   */
  public EFEMStatus getEFEMStatus() {
    return eFEMStatus;
  }

  public enum EFEMStatus {
    /**
     * Indicates EFEM Status is Run.
     */
    Run,
    /**
     * Indicates EFEM Status is Stop.
     */
    Stop,
    /**
     * Indicates EFEM Status is Idle.
     */
    Idle,
    /**
     * Indicates EFEM Status is Alarm.
     */
    Alarm,
    /**
     * Indicates EFEM Status is Warning.
     */
    Warning
  }
}
