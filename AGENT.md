# UoGoPro Agent Guide

## Product Goal

UoGoPro is a Kotlin Android app for controlling GoPro cameras. The first supported target is GoPro HERO4 Black and HERO4 Silver over the camera Wi-Fi HTTP API. HERO4 Session and newer models should be considered future extensions and must not complicate the first implementation.

The repository includes the `goprowifihack` submodule as the command reference. For HERO4 work, prefer these documents:

- `goprowifihack/HERO4/WifiCommands.md`
- `goprowifihack/HERO4/CameraStatus.md`
- `goprowifihack/HERO4/Mediabrowsing.md`
- `goprowifihack/HERO4/Livestreaming.md`
- `goprowifihack/HERO4/Framerates-Resolutions.md`
- `goprowifihack/HERO4/gpControl-Hero4Black.json`
- `goprowifihack/HERO4/gpControl-Hero4Silver.json`

## Initial Scope

The first release should support HERO4 Black/Silver:

- Detect/control a camera at `http://10.5.5.9`.
- Read camera status from `/gp/gpControl/status`.
- Pair with a 4-digit PIN using `/gpPair`.
- Switch primary modes: Video, Photo, MultiShot.
- Switch common HERO4 sub-modes.
- Start and stop shutter.
- Turn locate/beep on and off.
- Power off the camera.
- Show battery, SD card, recording state, remaining photos, remaining video time, and free space.
- Configure core video settings: resolution, frame rate, FOV, low light, spot meter, Protune, white balance, ISO, sharpness, EV compensation.
- Enforce HERO4 Black/Silver resolution/frame-rate/FOV compatibility in the UI.
- Start and stop HERO4 live preview from the Control screen.

Out of first scope:

- HERO4 Session wake-on-LAN behavior.
- Media download/delete.
- Multi-camera control.
- Bluetooth control.

## Android Stack

- Language: Kotlin.
- UI: Jetpack Compose with Material 3.
- Architecture: MVVM with repository-style camera access.
- Async/state: Kotlin coroutines and `StateFlow`.
- Networking: a small HTTP client abstraction first; OkHttp/Ktor can replace it later if needed.
- JSON: `org.json` is acceptable for early Android implementation; migrate to kotlinx.serialization when build tooling and schemas settle.
- Local settings: DataStore later, once real connection profiles are implemented.

Avoid React MUI in the native Android app. Use Material 3 Compose components to express the same design language.

## Architecture

Keep HERO4 command details out of UI code.

Recommended package layout:

```text
app/src/main/java/com/uogopro/
  MainActivity.kt
  data/
    GoProHttpClient.kt
    GoProRepository.kt
    Hero4CommandCatalog.kt
    Hero4StatusParser.kt
  domain/
    CameraState.kt
    CameraSettings.kt
    CaptureMode.kt
    Hero4Compatibility.kt
  ui/
    AppRoot.kt
    ConnectionScreen.kt
    DashboardScreen.kt
    SettingsScreen.kt
    Theme.kt
  viewmodel/
    CameraViewModel.kt
```

## HERO4 API Facts

Default camera host:

```text
10.5.5.9
```

Control examples:

```text
GET /gp/gpControl/status
GET /gp/gpControl/command/shutter?p=1
GET /gp/gpControl/command/shutter?p=0
GET /gp/gpControl/command/mode?p=0
GET /gp/gpControl/command/mode?p=1
GET /gp/gpControl/command/mode?p=2
GET /gp/gpControl/command/sub_mode?mode=0&sub_mode=0
GET /gp/gpControl/setting/{settingId}/{value}
GET /gp/gpControl/command/storage/tag_moment
GET /gp/gpControl/command/system/locate?p=1
GET /gp/gpControl/command/system/locate?p=0
GET /gp/gpControl/command/system/sleep
```

Pairing:

```text
GET https://10.5.5.9/gpPair?c=start&pin={PIN}&mode=0
GET https://10.5.5.9/gpPair?c=finish&pin={PIN}&mode=0
```

Media browsing, for later phases:

```text
GET http://10.5.5.9:8080/gp/gpMediaList
```

Live preview:

```text
GET http://10.5.5.9/gp/gpControl/execute?p1=gpStream&a1=proto_v2&c1=restart
GET http://10.5.5.9/gp/gpControl/execute?p1=gpStream&c1=restart
udp://@:8554
GET http://10.5.5.9/gp/gpControl/execute?p1=gpStream&c1=stop
```

While preview is active, send this UDP keep-alive message to `10.5.5.9:8554` about every 2.5 seconds:

```text
_GPHD_:0:0:2:0.000000
```

## Important Status IDs

From `CameraStatus.md`:

- `status[1]`: internal battery available.
- `status[2]`: internal battery level.
- `status[8]`: recording/processing status.
- `status[13]`: current recording duration.
- `status[31]`: number of clients connected.
- `status[32]`: streaming feed status.
- `status[33]`: SD card inserted.
- `status[34]`: remaining photos.
- `status[35]`: remaining video time.
- `status[43]`: current mode.
- `status[44]`: current sub-mode.
- `status[54]`: remaining free space in bytes.

Important video settings:

- `settings[2]`: video resolution.
- `settings[3]`: frame rate.
- `settings[4]`: FOV.
- `settings[8]`: low light.
- `settings[9]`: spot meter.
- `settings[10]`: Protune.
- `settings[11]`: white balance.
- `settings[13]`: ISO limit.
- `settings[14]`: sharpness.
- `settings[15]`: EV compensation.
- `settings[73]`: manual exposure.
- `settings[74]`: ISO mode.

## UI Plan

Use three tabs for the first implementation:

1. Connection
   - Camera host field, default `10.5.5.9`.
   - Pairing PIN input.
   - Connect/refresh status action.
   - Pair action.
   - Connection, battery, SD card, and mode summary.

2. Control
   - Live preview panel using the HERO4 UDP stream.
   - Current mode selector: Video, Photo, MultiShot.
   - Large shutter start/stop button.
   - Tag moment, locate on/off, power off actions.
   - Recording duration and remaining capacity.

3. Settings
   - Model selector: HERO4 Black, HERO4 Silver.
   - Video resolution, frame rate, and FOV selectors filtered by compatibility.
   - Low light, spot meter, Protune toggles.
   - White balance, ISO, sharpness, EV controls.

Visual direction:

- Dark, professional control surface.
- GoPro-style blue accent for selected controls.
- Red only for active recording/destructive capture state.
- Dense settings layout, but no invalid combinations.

## Compatibility Rules

Do not expose invalid video combinations. Use `Framerates-Resolutions.md` as the source for HERO4 Black/Silver compatibility.

Examples:

- HERO4 Black 4K: 30, 25, 24 fps; Wide only.
- HERO4 Black 1080p: 120, 90, 60, 50, 48, 30, 25, 24 fps; Wide, Medium, Narrow.
- HERO4 Silver 4K: 15, 12.5 fps; Wide only.
- HERO4 Silver 720p: 120, 60, 50, 30, 25 fps; Wide, Medium, Narrow.

When a resolution changes, pick the closest valid frame rate and FOV if the current values are no longer valid.

## Implementation Rules

- Keep command path construction in `Hero4CommandCatalog`.
- Keep JSON field mapping in `Hero4StatusParser`.
- Keep model-specific capability rules in `Hero4Compatibility`.
- UI should work from typed domain models, not raw HERO4 numeric IDs.
- Prefer small, testable pure Kotlin functions for compatibility and parsing.
- Do not implement HERO4 Session behavior until Black/Silver support is stable.

## Known Risks

- Android may disconnect from GoPro Wi-Fi because it has no internet; network binding may be needed later.
- Android Wi-Fi connection APIs are restricted on modern Android versions; first release can require the user to connect to GoPro Wi-Fi manually.
- HERO4 pairing uses HTTPS on an old device endpoint; certificate handling may need a custom client.
- UDP live preview may require libVLC or lower-level handling if Media3 cannot play it directly.
