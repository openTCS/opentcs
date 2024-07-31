package org.opentcs.peripheralcustomadapter;

import java.util.logging.Logger;

public class LocationSensor2Status {

  private static final Logger LOG = Logger.getLogger(
      LocationSensor2Status.class.getName()
  );
  /**
   * The Status for the STK Port1.
   */
  private boolean isMagazineInSTKPort1;
  /**
   * The Status for the STK Port2.
   */
  private boolean isMagazineInSTKPort2;
  /**
   * The Status for the OHB1.
   */
  private boolean isMagazineInOHB1;
  /**
   * The Status for the OHB2.
   */
  private boolean isMagazineInOHB2;
  /**
   * The Status for the Side Fork1.
   */
  private boolean isMagazineInSideFork1;
  /**
   * The Status for the Side Fork2.
   */
  private boolean isMagazineInSideFork2;

  /**
   * Creates a new instance.
   */
  public LocationSensor2Status() {
    isMagazineInSTKPort1 = false;
    isMagazineInSTKPort2 = false;
    isMagazineInOHB1 = false;
    isMagazineInOHB2 = false;
    isMagazineInSideFork1 = false;
    isMagazineInSideFork2 = false;
  }

  public void setSTKPort1Status(boolean hasCargo) {
    this.isMagazineInSTKPort1 = hasCargo;
  }

  public void setSTKPort2Status(boolean hasCargo) {
    this.isMagazineInSTKPort2 = hasCargo;
  }

  public void setOHB1Status(boolean hasCargo) {
    this.isMagazineInOHB1 = hasCargo;
  }

  public void setOHB2Status(boolean hasCargo) {
    this.isMagazineInOHB2 = hasCargo;
  }

  public void setSideFork1Status(boolean hasCargo) {
    this.isMagazineInSideFork1 = hasCargo;
  }

  public void setSideFork2Status(boolean hasCargo) {
    this.isMagazineInSideFork2 = hasCargo;
  }

  /**
   * Is there Magazine in STK Port1.
   *
   * @return STK Port1 Status.
   */
  public boolean hasMagazineSTKPort1() {
    return this.isMagazineInSTKPort1;
  }

  /**
   * Is there Magazine in STK Port2.
   *
   * @return STK Port2 Status.
   */
  public boolean hasMagazineSTKPort2() {
    return this.isMagazineInSTKPort2;
  }

  /**
   * Is there Magazine in OHB1.
   *
   * @return OHB1 Status.
   */
  public boolean hasMagazineOHB1() {
    return this.isMagazineInOHB1;
  }

  /**
   * Is there Magazine in OHB2.
   *
   * @return OHB2 Status.
   */
  public boolean hasMagazineOHB2() {
    return this.isMagazineInOHB2;
  }

  /**
   * Is there Magazine in Side Fork1.
   *
   * @return Side Fork1 Status.
   */
  public boolean hasMagazineSTKSideFork1() {
    return this.isMagazineInSideFork1;
  }

  /**
   * Is there Magazine in Side Fork2.
   *
   * @return Side Fork2 Status.
   */
  public boolean hasMagazineSideFork2() {
    return this.isMagazineInSideFork2;
  }
}
