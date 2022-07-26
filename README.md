# openTCS

[![Contributor Covenant](https://img.shields.io/badge/Contributor%20Covenant-2.1-4baaaa.svg)](CODE_OF_CONDUCT.md)

* Homepage: https://www.opentcs.org/
* Changelog: [changelog.adoc](./openTCS-Documentation/src/docs/release-notes/changelog.adoc)

openTCS (short for _open Transportation Control System_) is a free platform for controlling fleets of [automated guided vehicles (AGVs)](https://en.wikipedia.org/wiki/Automated_guided_vehicle) and mobile robots.
It should generally be possible to control any automatic vehicle with communication capabilities with it, but AGVs are the main target.

openTCS is being maintained by the openTCS team at the [Fraunhofer Institute for Material Flow and Logistics](https://www.iml.fraunhofer.de/).

The software runs on the Java platform version 13, with the recommended Java distribution being the one provided by the [Adoptium project](https://adoptium.net/).
All libraries required for compiling and/or using it are freely available, too.

openTCS itself is not a complete product you can use out-of-the-box to control AGVs with.
Primarily, it is a framework/an implementation of the basic data structures and algorithms (routing, dispatching, scheduling) needed for running an AGV system with more than one vehicle.
It tries to be as generic as possible to allow interoperation with vehicles of practically any vendor.
Thus it is usually necessary to at least create and integrate a vehicle driver (called _communication adapter_ in openTCS-speak) that translates between the abstract interface of the openTCS kernel and the communication protocol your vehicle understands.
Depending on your needs, it might also be necessary to adapt algorithms or add project-specific strategies.

## Getting started

To get started with openTCS, please refer to the user's guide, the developer's guide and the API documentation.
These documents are included in the binary distribution and can also be read online on the [openTCS homepage](https://www.opentcs.org/).

## Licensing

### Code

All of this software project's source code, including scripts and configuration files, is distributed under the [MIT License](LICENSE.txt).

### Assets

Unless stated otherwise, all of this software project's documentation, resource bundles and media files are distributed under the [Creative Commons Attribution 4.0 International (CC BY 4.0)](LICENSE.assets.txt) license.

## Contributing

You are very welcome to contribute to this project.
Please see [CONTRIBUTING.md](./CONTRIBUTING.md) for a few guidelines related to this.
