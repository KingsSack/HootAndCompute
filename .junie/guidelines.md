# Volt - FTC Development Library

## Project Overview

Volt is a library for FTC (FIRST Tech Challenge) robotics developed in Kotlin. It provides a structured, object-oriented
approach to robot programming, making it easier to develop and maintain code for FTC competitions.

The library includes components for:

- Custom operation modes (Autonomous and TeleOp)
- Robot hardware abstraction
- Web-based user interfaces (For Autonomous Mode (Auto) and Manual Mode (TeleOp) creation)
- Utility functions and helpers

## Goals

### Make a more structured object-oriented approach to development

Volt provides abstract base classes and well-defined interfaces that encourage good software engineering practices. By
separating concerns and providing clear abstractions, Volt helps teams write more maintainable and reusable code.

### Create User Interfaces for custom TeleOps and Autos

Volt includes a web server component that allows teams to create custom web-based interfaces for monitoring and
controlling their robots. This makes it easier to debug and tune robot behavior during development and competition.

> [!More Info]
> The web server is built with Tailwind CSS for design and Alpine.js for interactivity.

### Provide pre-made classes that can speed up development

The library includes pre-built components for common FTC tasks, reducing the amount of boilerplate code teams need to
write. This allows teams to focus on their robot's unique capability rather than reimplementing common patterns.