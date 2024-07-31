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
  private int eFEMMagazineNumber;
  /**
   * The Status for the OHB1.
   */
  private EFEMStatus eFEMStatus;

  /**
   * Creates a new instance.
   */
  public LocationSensor1Status() {
    this.isMagazineInEFEM = false;
    this.eFEMMagazineNumber = 0;
    this.eFEMStatus = EFEMStatus.Idle;
  }

  public void setEFEMMagazineStatus(boolean hasMagazine) {
    this.isMagazineInEFEM = hasMagazine;
  }

  public boolean hasMagazineEFEM(boolean hasMagazine) {
    return isMagazineInEFEM;
  }

  public void setEFEMMagazineNumber(int number) {
    this.eFEMMagazineNumber = number;
  }

  public int getEFEMMagazineNumber() {
    return eFEMMagazineNumber;
  }

  public void setEFEMStatus(int status) {
    switch (status)
    {
      case 1 -> this.eFEMStatus = EFEMStatus.Run;
      case 2 -> this.eFEMStatus = EFEMStatus.Stop;
      case 4-> this.eFEMStatus = EFEMStatus.Idle;
      case 8 -> this.eFEMStatus = EFEMStatus.Alarm;
      case 16 -> this.eFEMStatus = EFEMStatus.Warning;
      default -> throw new IllegalStateException("Unexpected value: " + status);
    }
  }

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
