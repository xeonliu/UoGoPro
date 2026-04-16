# UoGoPro

Kotlin Android app for controlling GoPro cameras over Wi-Fi. The first target is GoPro HERO4 Black/Silver using the HTTP API documented in the `goprowifihack` submodule.

## Current Scope

- HERO4 Black/Silver status polling from `http://10.5.5.9/gp/gpControl/status`.
- HERO4 PIN pairing flow.
- Video/Photo/MultiShot mode switching.
- Shutter start/stop, tag moment, locate on/off, power off.
- Typed video setting controls for resolution, frame rate, FOV, Protune, low light, spot meter, white balance, ISO, sharpness, and EV.
- HERO4 Black/Silver resolution, frame-rate, and FOV compatibility filtering.

## Project

Open this repository in Android Studio and run the `app` configuration. The app assumes the phone is already connected to the GoPro Wi-Fi network; Android Wi-Fi onboarding will be implemented later.

The detailed implementation guide is in `AGENT.md`.
