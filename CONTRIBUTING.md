# Contributing to openTCS

The following is a set of guidelines for contributing to openTCS.

This project is maintained by the openTCS development team of [Fraunhofer IML](https://www.iml.fraunhofer.de/en.html).
A public mirror of the development repository is available at [GitHub](https://github.com/opentcs/opentcs).

You are very welcome to contribute to this project when you find a bug, want to suggest an improvement, or have an idea for a useful feature.
For this, please always create an issue and/or a pull request, and follow our style guides as described below.

## Changelog

We document changes in the [changelog](openTCS-Documentation/src/docs/release-notes/changelog.adoc).

## Issues

It is required to create an issue if you want to integrate a bugfix, improvement, or feature.
Briefly and clearly describe the purpose of your contribution in the corresponding issue, using the appropriate template for it.

## Versioning

The openTCS project uses the [SemVer](https://semver.org/) for versioning.
The release versions are tagged with their respective version.

## Working on the code in this project

### Primary formatting rules

* Maximum line length: 100 characters.
* Do not use tabs, use spaces only.
* Indentation step size: 2 spaces.

### Automatic tests

* New or changed non-trivial code should be covered by tests.
* JUnit test classes and methods should omit the `public` modifier unless there is a technical reason for adding it.

### IDE: NetBeans

To build the project from NetBeans, register a Java platform named "JDK 13 - openTCS" (without the quotes) within NetBeans.
This JDK will be used by NetBeans for running the build process.

The NetBeans settings contained in this project include formatting rules.
Please apply them before submitting contributions to keep the formatting consistent.
