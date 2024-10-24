<!--
SPDX-FileCopyrightText: The openTCS Authors
SPDX-License-Identifier: CC-BY-4.0
-->

# Contributing to openTCS

The following is a set of guidelines for contributing to openTCS.

This project is maintained by the openTCS development team of [Fraunhofer IML](https://www.iml.fraunhofer.de/en.html).
A public mirror of the development repository is available at [GitHub](https://github.com/opentcs/opentcs).

You are very welcome to contribute to this project when you find a bug, want to suggest an improvement, or have an idea for a useful feature.
For this, please always create an issue and/or a pull request, and follow our style guides as described below.

## Issues

It is required to create an issue if you want to integrate a bugfix, improvement, or feature.
Briefly and clearly describe the purpose of your contribution in the corresponding issue, using the appropriate template for it.

## Pull requests / merge requests

Pull requests fixing bugs, adding new features or improving the quality of the code and/or the documentation are very welcome!
To contribute changes, please follow these steps:

1. Before putting any significant amount of work into changes you want to make, get in touch with the maintainers.
   Informing and coordinating helps to avoid conflicts with other changes made elsewhere.
   If there already is an issue or a forum discussion related to the intended changes, announce your will to work on it there; if there isn't, yet, create one.
   (Specific bug reports and feature/improvement suggestions should be handled in issues; loose ideas should be discussed in the forum first.)
2. Fork the repository and create a new branch for your changes.
3. Make your changes in the new branch, adhering to the coding guidelines.
4. If your changes add a new feature, consider adding documentation for it to the user's guide or the developer's guide.
   If your changes change an existing feature's behaviour, check these documents and update them if necessary.
5. If your changes affect users, document them in the [changelog](opentcs-documentation/src/docs/release-notes/changelog.adoc).
6. Push your branch to your forked repository.
7. Open a pull request against the main repository's main branch.
   1. Fill out the pull request template and provide a clear description of your changes and the problem they solve.
   2. Provide a commit message for the pull request, adhering to the commit message guidelines described below.

When contributing, keep unrelated changes in separate pull requests!

### Commit message guidelines

When writing commit messages, please follow these guidelines:

* Use descriptive and meaningful commit messages.
* Start the subject line with a verb in the imperative mood (e.g., "Add," "Fix," "Update").
* Keep the subject line short and concise (preferably 50 characters or less).
* Provide additional details in the body if needed.
* Separate the subject from the body with a blank line.
* Keep the length of body lines less than or equal to 72.
* Reference relevant commit hashes in the commit message if applicable.

Example:

```
Make vehicle energy level thresholds configurable

Allow a vehicle's set of energy level thresholds to be modified during
runtime via the Operations Desk application and the web API.

Co-authored-by: Martin Grzenia <martin.grzenia@iml.fraunhofer.de>
Co-authored-by: Stefan Walter <stefan.walter@iml.fraunhofer.de>
```

## Coding guidelines

In general, please adhere to the following guidelines to maintain code consistency, readability, and quality:

* Write lean and efficient code.
  Avoid unnecessary complexity or redundant logic.
* Use meaningful and descriptive names for classes, methods and variables.
* Write clear and concise comments when necessary, but strive for code that is self-explanatory.
* Prioritize code readability over cleverness.

The following subsections contain a few more detailed guidelines we consider important in this project.
Note that parts of the existing code may not fully adhere to these rules, yet.
When updating such code, do improve it by applying the guidelines where it makes sense, but avoid modifying large unrelated sections.

### Primary formatting rules

For consistent formatting of the project's code, [Spotless](https://github.com/diffplug/spotless) is used.
After making changes, make sure you run `./gradlew spotlessApply` to re-format the code.

### Automatic tests

* New or changed non-trivial code should be covered by tests.
* This project uses [JUnit](https://junit.org/) for unit testing.
* JUnit test classes and methods should omit the `public` modifier unless there is a technical reason for adding it.
* For assertions, `assertThat()` should be preferred over `assertTrue()`, as the former provides more information when failing.

### Check preconditions in subroutines

Methods belonging to a class's interface, i.e. `public` or `protected` methods, should check their preconditions.
They should at least check their input parameters for validity:

* Reference types that may not be `null` should be checked using `java.util.Objects.requireNonNull()`.
  It also makes sense to mark them using the `@Nonnull` annotation to document that they may not be `null`.
* Parameters of numeric types for which only certain ranges of values are acceptable should be checked using `org.opentcs.util.Assertions.checkInRange()`.
* Parameters for which only certain values are acceptable should be checked using `org.opentcs.util.Assertions.checkArgument()`.

Checking the object's internal state may also make sense for a method.
For such checks, `org.opentcs.util.Assertions.checkState()` should be used.

### Avoid single-use local variables

Declaring local variables in subroutines that are then used only once creates unnecessary noise for the reader of the code.
In many cases, eliminating the variable by inlining its value improves the readability of the code.

## Development setup

### IDE: NetBeans

To build the project from NetBeans, register a Java platform named "JDK 21 - openTCS" (without the quotes) within NetBeans.
This JDK will be used by NetBeans for running the build process.
