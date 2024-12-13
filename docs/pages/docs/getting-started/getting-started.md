---
title: Getting Started
tags:
   - getting-started
permalink: getting-started.html
sidebar: docs
folder: docs
---

## Cloning

### Install an IDE

- It is recommended to use `IntelliJ IDEA`

### Install the latest version of the Android SDK

 - This can be done in `InteliJ IDEA` (recommended)
     - Make sure you have the `Android` plugin installed
     - Go to `Languages & Framworks` in IntelliJ's settings
     - Go to `Android SDK Updater`
 - This can be done with the command line

### Clone the project with git

 - This can be done in `IntelliJ IDEA` (recommended)
     - Select `New Project From Version Control`
     - Enter the URL: `https://github.com/KingsSack/HootAndCompute.git`
 - This can be done with the command line
     - Enter the command: `git clone https://github.com/KingsSack/HootAndCompute.git`

### Add path to Android SDK

 - Create a file in the root of the project called `local.properties`
 - Enter the contents `sdk.dir=path/to/sdk`
 - Replace `path/to/sdk` with the path to the Android SDK

### Reload the Gradle project

 - This can be done in `IntelliJ IDEA` (recommended)
     - Go to the Gradle tab
     - Press `Sync All Gradle Projects`

## Contributing

- If you would like to contribute to the project, please follow the steps below

>See Next
> 
>- [Creating an Autonomous OpMode](autonomous.html)
>- [Creating a Manual OpMode](manual.html)