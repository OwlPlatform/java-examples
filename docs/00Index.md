Java Programming with Owl Platform
==================================

Author: Rob Moore

Last Update: April 12, 2014

## Getting Started ##
The following articles are intended to get programmers started using the Owl
Platform wireless sensor network architecture, using the official Java
libraries. I will assume you are using Pipsqueak sensors of various types
throughout the tutorials, and are able to manipulate these sensors in some
way.

## Planned Tutorials ##
The tutorials listed below will be written as soon as I have the time, or not
at all.  Please contact me if you want me to write one up, or if you have a
suggestion for a tutorial.

### Solvers ###
* [Sensor Signal Parser][1] - A simple solver that will read the Received Signal
	Strength Indicator (RSSI) value from wireless devices and update the World
	Model.
* Pipsqueak Temperature Parser - A simple solver that will read either the
  7-bit or 16-bit temperature values from a Pipsqueak sensor and update the
	World Model.
* Pipsqueak Binary Parser - A simple solver that will read the binary state
  from a Pipsqueak sensor and update the World Model.

### Applications ###
* Sensor Signal Reader - A simple application that will retrieve, average, and
  print some basic signal statistics.
* Temperature Email Alert - An application that monitors a temperature value
  and sends an email alert when detects a hot or cold value.
* Sensor Presence Detector - A simple application that detects when a specific
  sensor "enters" and "leaves" an area.

[1]: 01SolverSensorSignal.html    "Sensor Signal Parser Solver"
