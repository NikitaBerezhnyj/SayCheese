# SayCheese

<p align="center">
  <img src="./assets/icon.png" alt="App logo" width="200"/>
</p>

**SayCheese** is an Android mobile application that allows you to conveniently take photos using voice commands. Just say **"cheese"** to take a photo, or **"time"** to activate the timer — and the camera will do everything for you!

## Overview

**SayCheese** is built in Kotlin using modern Android Jetpack libraries.
The main goal is to make the photo-taking process as simple, convenient, and interactive as possible.
The app supports manual photo capture, timer-based photos, and voice control through offline speech recognition.

## Features

- **Photo Capture** — take a photo instantly with a single tap.
- **Timer Photos** — automatic photo capture after a set interval.
- **Voice Control** — offline command recognition:
  - **"cheese"** — take a photo immediately.
  - **"time"** — take a photo with a timer.
- **Offline Recognition** — works without an internet connection thanks to the built-in Vosk model.
- **Photo Storage** — automatically saves captured photos to local storage.
- **Modern UI** — interface built with Jetpack Compose featuring smooth animations.

## Technologies Used

- **Kotlin** — primary programming language.
- **Jetpack Compose** — modern UI framework for building interfaces.
- **CameraX** — library for camera functionality.
- **Vosk API** — offline voice recognition.
- **Android SDK** — Android development platform.
- **Gradle** — build system for the project.

## Getting Started

To get started with **SayCheese**, follow these steps:

1. **Clone the repository**

   ```bash
   git clone https://github.com/NikitaBerezhnyj/SayCheese.git
   cd SayCheese
   ```

2. **Open the project**
   Open the project in **Android Studio** (recommended latest stable version).

3. **Sync Gradle**
   Wait for Gradle synchronization to complete or click **Sync Project with Gradle Files**.

4. **Run on device**
   Connect an Android device or start an emulator, then click **Run**.

## Usage

Once the project is running, you can:

1. Open the camera and take a photo manually.
2. Tap the timer icon to take a photo after a delay.
3. Activate voice control — say **"cheese"** or **"time"**, and the app will take a photo automatically.

> Note: microphone permission is required for voice commands.

## License & Community Guidelines

- [License](LICENSE) — project license
- [Code of Conduct](CODE_OF_CONDUCT.md) — expected behavior for contributors
- [Contributing Guide](CONTRIBUTING.md) — how to help the project
- [Security Policy](SECURITY.md) — reporting security issues
