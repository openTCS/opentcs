package org.opentcs.peripheralcustomadapter;

import java.util.logging.Logger;

public class LocationSensor2Status {

  private static final Logger LOG = Logger.getLogger(
      LocationSensor2Status.class.getName()
  );
  /**
   * The Status for the STK Port1.
   */
  private boolean sTKPort1Status;
  /**
   * The Status for the STK Port2.
   */
  private boolean sTKPort2Status;
  /**
   * The Status for the OHB1.
   */
  private boolean oHB1Status;
  /**
   * The Status for the OHB2.
   */
  private boolean oHB2Status;
  /**
   * The Status for the Side Fork1.
   */
  private boolean sideFork1Status;
  /**
   * The Status for the Side Fork2.
   */
  private boolean sideFork2Status;

  /**
   * Creates a new instance.
   */
  public LocationSensor2Status() {
    sTKPort1Status = false;
    sTKPort2Status = false;
    oHB1Status = false;
    oHB2Status = false;
    sideFork1Status = false;
    sideFork2Status = false;
  }

  public void setSTKPort1Status(boolean hasCargo) {
    this.sTKPort1Status = hasCargo;
  }

  public void setSTKPort2Status(boolean hasCargo) {
    this.sTKPort2Status = hasCargo;
  }

  public void setOHB1Status(boolean hasCargo) {
    this.oHB1Status = hasCargo;
  }

  public void setOHB2Status(boolean hasCargo) {
    this.oHB2Status = hasCargo;
  }

  public void setSideFork1Status(boolean hasCargo) {
    this.sideFork1Status = hasCargo;
  }

  public void setSideFork2Status(boolean hasCargo) {
    this.sideFork2Status = hasCargo;
  }

  /**
   * Is there Cargo in STK Port1.
   *
   * @return STK Port1 Status.
   */
  public boolean hasCargoSTKPort1() {
    return this.sTKPort1Status;
  }

  /**
   * Is there Cargo in STK Port2.
   *
   * @return STK Port2 Status.
   */
  public boolean hasCargoSTKPort2() {
    return this.sTKPort2Status;
  }

  /**
   * Is there Cargo in OHB1.
   *
   * @return OHB1 Status.
   */
  public boolean hasCargoOHB1() {
    return this.oHB1Status;
  }

  /**
   * Is there Cargo in OHB2.
   *
   * @return OHB2 Status.
   */
  public boolean hasCargoOHB2() {
    return this.oHB2Status;
  }

  /**
   * Is there Cargo in Side Fork1.
   *
   * @return Side Fork1 Status.
   */
  public boolean hasCargoSTKSideFork1() {
    return this.sideFork1Status;
  }

  /**
   * Is there Cargo in Side Fork2.
   *
   * @return Side Fork2 Status.
   */
  public boolean hasCargoSideFork2() {
    return this.sideFork2Status;
  }
}
