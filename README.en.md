# Blocksmith

**An 8×8 block puzzle. Free, no ads, fully offline.**

Three pieces at a time. Drag them onto the board, complete lines, clear them,
start again.

[Français](README.md) · [Download the APK](https://github.com/joynix28/blocksmith/releases/latest)

## Install

1. Download the APK from [releases](https://github.com/joynix28/blocksmith/releases/latest).
2. Open it and allow installation from this source if Android asks.

Android 8+. No permissions, no network, no account. 1.1 MB.

## Play

- Drag a piece from the tray onto the board.
- A full row or column clears instantly.
- Clearing on consecutive placements builds a **combo** that multiplies points.
- The run ends when no piece fits.
- The bulb button shows a free **hint**.

Modes: endless run, 48 levels across 4 chapters, daily challenge.

Settings: French/English, interface size, sound, vibration, stats, reset.

## Build

```bash
./gradlew :app:assembleRelease
./gradlew :desktop:run          # play on desktop
./gradlew :shared:desktopTest   # engine and level tests
```

## Licence

Apache 2.0. See `LICENSE` and `NOTICE`.

> If you reuse this code, its design or its idea, **credit Joynix** with a link
> back to this repository.
