/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.configuration.gestalt;

import java.util.Objects;

/**
 * A dummy class for testing sample configuration entries.
 */
public class DummyClass {

  private final String name;
  private final String surname;
  private final int age;

  public DummyClass() {
    name = "";
    surname = "";
    age = 0;
  }

  public DummyClass(String paramString) {
    String[] split = paramString.split("\\|", 3);
    name = split[0];
    surname = split[1];
    age = Integer.parseInt(split[2]);
  }

  public DummyClass(String name, String surname, int age) {
    this.name = name;
    this.surname = surname;
    this.age = age;
  }

  public String getName() {
    return name;
  }

  public String getSurname() {
    return surname;
  }

  public int getAge() {
    return age;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof DummyClass)) {
      return false;
    }
    DummyClass other = (DummyClass) o;
    return name.equals(other.name) && surname.equals(other.surname) && age == other.age;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 23 * hash + Objects.hashCode(this.name);
    hash = 23 * hash + Objects.hashCode(this.surname);
    hash = 23 * hash + this.age;
    return hash;
  }

  @Override
  public String toString() {
    return getName() + " - " + getSurname() + ":" + getAge();
  }
}
