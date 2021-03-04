/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.util;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Liefert Methoden für die Datumsberechnung, insbesondere was die Sommerzeit
 * angeht.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public final class DateUtility {

  /**
   * Prevents undesired instantiation.
   */
  private DateUtility() {
    // Do nada.
  }

  /**
   * Prüft, ob ein Zeitpunkt innerhalb der Sommerzeit liegt. Dies ist vom ersten
   * Sonntag im März 2.00 Uhr bis zum letzten Sonntag im Oktober 3.00 Uhr der
   * Fall. Zu Beginn der Sommerzeit wird die Uhr eine Stunde vorgestellt von
   * 2.00 Uhr auf 3.00 Uhr. Zum Ende der Sommerzeit wird die Uhr eine Stunde
   * zurückgestellt von 3.00 Uhr auf 2.00 Uhr.
   *
   * @param time der Zeitpunkt
   * @return
   * <code> true </code>, wenn der Zeitpunkt innerhalb der Sommerzeit liegt
   */
  public static boolean isWithinDaylightSavingTime(long time) {
    Calendar calendar = new GregorianCalendar();
    calendar.setTimeInMillis(time);
    int year = calendar.get(Calendar.YEAR);
    // letzten Sonntag im März finden, März hat immer 31 Tage
    Calendar begin = new GregorianCalendar(year, 2, 31, 2, 0);
    int weekday = begin.get(Calendar.DAY_OF_WEEK);

    while (weekday != Calendar.SUNDAY) {
      begin.add(Calendar.DAY_OF_MONTH, -1);
      weekday = begin.get(Calendar.DAY_OF_WEEK);
    }
    // letzten Sonntag im Oktober finden, Oktober hat immer 31 Tage
    Calendar end = new GregorianCalendar(year, 9, 31, 3, 0);
    weekday = end.get(Calendar.DAY_OF_WEEK);

    while (weekday != Calendar.SUNDAY) {
      end.add(Calendar.DAY_OF_MONTH, -1);
      weekday = end.get(Calendar.DAY_OF_WEEK);
    }

    if (time < begin.getTimeInMillis()) {
      return false;
    }

    if (time >= end.getTimeInMillis()) {
      return false;
    }

    return true;
  }
}
