# Ti.Android.Haptics

> Advanced Android Haptic Feedback module for [Titanium SDK](https://titaniumsdk.com/)

The Titanium SDK already exposes `Ti.UI.iOS.FeedbackGenerator` for rich haptic feedback on iPhone. This module brings the same level of control to Android, exposing `VibrationEffect` predefined effects, custom waveforms, precise amplitude control, and `HapticFeedbackConstants` for view-level feedback.

[![Platform](https://img.shields.io/badge/platform-Android-green.svg)](https://www.android.com/)
[![Min API](https://img.shields.io/badge/minAPI-29%20(Android%2010)-blue.svg)](https://developer.android.com/tools/releases/platforms#10)
[![Titanium SDK](https://img.shields.io/badge/Titanium%20SDK-13.1.1%2B-red.svg)](https://titaniumsdk.com/)
[![License](https://img.shields.io/badge/license-MIT-lightgrey.svg)](LICENSE)

---

## Features

- **Predefined effects** — `EFFECT_CLICK`, `EFFECT_TICK`, `EFFECT_HEAVY_CLICK`, `EFFECT_DOUBLE_CLICK`
- **Semantic convenience methods** — `impact()`, `selection()`, `notification()`, `doubleClick()`
- **Amplitude control** — `oneShot(ms, amplitude)` with precise 1–255 intensity
- **Custom waveforms** — arbitrary timing + amplitude patterns, with optional looping
- **View-level feedback** — full `HapticFeedbackConstants` support via `performHapticFeedback()`, respecting system accessibility settings
- **Capability detection** — `isSupported()` and `hasAmplitudeControl()` checks
- **Safe API-level guards** — constants for API 33/34 features are skipped on older devices with a log warning

---

## Requirements

| Requirement     | Value               |
|-----------------|---------------------|
| Titanium SDK    | 13.1.1.GA or higher  |
| Android min SDK | 29 (Android 10)     |
| compileSdkVersion | 34               |

---

## Installation

### 1. Copy the module

Copy the module to your project's  `modules/android/`  folder

### 2. Add to `tiapp.xml`

```xml
<modules>
    <module platform="android">ti.android.haptics</module>
</modules>
```

> The `VIBRATE` permission is automatically merged from the module's manifest. You do **not** need to add it manually.

---

## Usage

```javascript
const Haptics = require('ti.android.haptics')
```

### Check device support

```javascript
if (!Haptics.isSupported()) {
  // device has no vibrator — skip haptics
}

// Check if device supports per-step amplitude control
const hasAmplitude = Haptics.hasAmplitudeControl()
```

---

## API Reference

### Predefined Effects

The simplest way to trigger haptic feedback.

```javascript
// Impact — maps to Android VibrationEffect predefined effects
Haptics.impact('light')   // EFFECT_TICK
Haptics.impact('medium')  // EFFECT_CLICK  (default)
Haptics.impact('heavy')   // EFFECT_HEAVY_CLICK

// Convenience methods
Haptics.selection()    // light tick — ideal for scroll / list navigation
Haptics.doubleClick()  // double-click pattern

// Trigger by explicit constant
Haptics.effect(Haptics.EFFECT_CLICK)
Haptics.effect(Haptics.EFFECT_DOUBLE_CLICK)
Haptics.effect(Haptics.EFFECT_HEAVY_CLICK)
Haptics.effect(Haptics.EFFECT_TICK)
```

#### `impact(intensity)` mapping

| Intensity   | Android Effect       |
|-------------|----------------------|
| `"light"`   | `EFFECT_TICK`        |
| `"medium"`  | `EFFECT_CLICK`       |
| `"heavy"`   | `EFFECT_HEAVY_CLICK` |

---

### Notification Feedback

Compound waveform patterns for system events.

```javascript
Haptics.notification('success')  // 2-pulse positive confirmation
Haptics.notification('warning')  // 2 medium pulses — alert
Haptics.notification('error')    // 3 sharp pulses — failure
```

---

### Amplitude Control

Single-pulse vibration with precise duration and intensity.

```javascript
// Using convenience amplitude constants
Haptics.oneShot(80, Haptics.AMPLITUDE_LIGHT)   // 80ms, ~25% intensity
Haptics.oneShot(80, Haptics.AMPLITUDE_MEDIUM)  // 80ms, ~50% intensity
Haptics.oneShot(80, Haptics.AMPLITUDE_HEAVY)   // 80ms, maximum intensity
Haptics.oneShot(80, Haptics.AMPLITUDE_DEFAULT) // let the system decide

// Custom amplitude value (1–255)
Haptics.oneShot(100, 192)  // 100ms at ~75% intensity
```

> On devices without amplitude control (`hasAmplitudeControl() === false`), the vibrator falls back to on/off at full power.

---

### Custom Waveforms

Build any vibration pattern with full control over timing and amplitude.

```javascript
// Simple pattern — alternating off/on in ms, system-default amplitude
Haptics.waveform({
  timings: [0, 100, 50, 100],   // [delay, on, off, on]
})

// Full control — timings + amplitudes (arrays must be the same length)
Haptics.waveform({
  timings:    [0,  80, 40, 80, 40, 120],
  amplitudes: [0, 180,  0, 220,  0, 255],
  repeat: -1  // -1 = play once (default)
})

// Repeating waveform — e.g. phone ringing, alarm
Haptics.waveform({
  timings:    [0, 400, 200],
  amplitudes: [0, 200,   0],
  repeat: 0   // loop back to index 0 indefinitely
})

// Always cancel repeating waveforms when the UI is destroyed
Haptics.cancel()
```

#### `waveform(options)` options

| Option       | Type    | Required | Description |
|--------------|---------|----------|-------------|
| `timings`    | `Int[]` | ✅ | Alternating off/on durations in ms. First element = initial delay before vibration starts. |
| `amplitudes` | `Int[]` | ❌ | Amplitude per segment, 0–255. Must match `timings` length. Omit to use system defaults. |
| `repeat`     | `Int`   | ❌ | Index in `timings` to loop from. `-1` = play once (default). |

---

### View-Level Feedback (`HapticFeedbackConstants`)

Attaches haptic feedback directly to an Android `View`. This API automatically respects the user's **"Touch feedback"** system accessibility setting — the recommended approach for standard UI interactions.

```javascript
const btn = Ti.UI.createButton({ title: 'Press me' })

btn.addEventListener('click', () => {
  Haptics.performHapticFeedback(btn, Haptics.FEEDBACK_VIRTUAL_KEY)
})

btn.addEventListener('longpress', () => {
  Haptics.performHapticFeedback(btn, Haptics.FEEDBACK_LONG_PRESS)
})
```

> ⚠️ The view must be part of the window hierarchy before calling `performHapticFeedback()`.

#### `FEEDBACK_*` constants

| Constant | Min API | Use Case |
|----------|---------|----------|
| `FEEDBACK_LONG_PRESS` | 29 | Long press gesture |
| `FEEDBACK_VIRTUAL_KEY` | 29 | Virtual keyboard key press |
| `FEEDBACK_KEYBOARD_PRESS` | 29 | Physical keyboard press |
| `FEEDBACK_KEYBOARD_RELEASE` | 29 | Physical keyboard release |
| `FEEDBACK_KEYBOARD_TAP` | 29 | IME keyboard tap |
| `FEEDBACK_CLOCK_TICK` | 29 | Clock/timer tick |
| `FEEDBACK_CONTEXT_CLICK` | 29 | Context menu click |
| `FEEDBACK_CONFIRM` | 30 | Confirmation action |
| `FEEDBACK_REJECT` | 30 | Rejection / cancel action |
| `FEEDBACK_GESTURE_START` | 33 | Gesture interaction begins |
| `FEEDBACK_GESTURE_END` | 33 | Gesture interaction ends |
| `FEEDBACK_DRAG_START` | 33 | Drag operation starts |
| `FEEDBACK_SEGMENT_TICK` | 33 | Segment scroll tick |
| `FEEDBACK_SEGMENT_FREQUENT_TICK` | 33 | Frequent segment scroll tick |
| `FEEDBACK_TOGGLE_ON` | 34 | Toggle switch ON |
| `FEEDBACK_TOGGLE_OFF` | 34 | Toggle switch OFF |
| `FEEDBACK_NO_HAPTICS` | 29 | Disables haptics for the view |

Constants that require a higher API than the current device log a warning and are silently skipped — no crash.

---

### All Constants

#### Effect constants

| Constant | Description |
|----------|-------------|
| `EFFECT_CLICK` | Standard click |
| `EFFECT_DOUBLE_CLICK` | Double-click |
| `EFFECT_HEAVY_CLICK` | Strong/heavy click |
| `EFFECT_TICK` | Light tick |

#### Amplitude constants

| Constant | Value | Description |
|----------|-------|-------------|
| `AMPLITUDE_DEFAULT` | -1 | System-determined |
| `AMPLITUDE_LIGHT` | 64 | ~25% intensity |
| `AMPLITUDE_MEDIUM` | 128 | ~50% intensity |
| `AMPLITUDE_HEAVY` | 255 | Maximum intensity |
| `AMPLITUDE_MAX` | 255 | Alias for `AMPLITUDE_HEAVY` |

---

## Cross-Platform Wrapper

Place this service in `app/lib/services/haptics.js` (Alloy) for a single unified API across iOS and Android:

```javascript
// app/lib/services/haptics.js

const _android = OS_ANDROID ? require('ti.android.haptics') : null

exports.Haptics = {
  impact(intensity = 'medium') {
    if (OS_IOS) {
      const styleMap = {
        light:  Ti.UI.iOS.FEEDBACK_IMPACT_LIGHT,
        medium: Ti.UI.iOS.FEEDBACK_IMPACT_MEDIUM,
        heavy:  Ti.UI.iOS.FEEDBACK_IMPACT_HEAVY,
      }
      const gen = Ti.UI.iOS.createFeedbackGenerator({
        type: Ti.UI.iOS.FEEDBACK_GENERATOR_IMPACT,
        style: styleMap[intensity],
      })
      gen.prepare()
      gen.impactOccurred()
    } else {
      _android.impact(intensity)
    }
  },

  selection() {
    if (OS_IOS) {
      const gen = Ti.UI.iOS.createFeedbackGenerator({ type: Ti.UI.iOS.FEEDBACK_GENERATOR_SELECTION })
      gen.prepare()
      gen.selectionChanged()
    } else {
      _android.selection()
    }
  },

  notification(type = 'success') {
    if (OS_IOS) {
      const typeMap = {
        success: Ti.UI.iOS.FEEDBACK_NOTIFICATION_SUCCESS,
        warning: Ti.UI.iOS.FEEDBACK_NOTIFICATION_WARNING,
        error:   Ti.UI.iOS.FEEDBACK_NOTIFICATION_ERROR,
      }
      const gen = Ti.UI.iOS.createFeedbackGenerator({ type: Ti.UI.iOS.FEEDBACK_GENERATOR_NOTIFICATION })
      gen.prepare()
      gen.notificationOccurred(typeMap[type])
    } else {
      _android.notification(type)
    }
  },
}
```

Usage in any Alloy controller:

```javascript
const { Haptics } = require('services/haptics')

Haptics.impact('medium')
Haptics.selection()
Haptics.notification('success')
```

---

## Notes

- **Amplitude fallback** — On devices where `hasAmplitudeControl()` returns `false`, `oneShot()` and amplitude-based waveforms fall back to full-power on/off vibration automatically.
- **Repeating waveforms** — Always call `Haptics.cancel()` in your window's `close` event or controller `cleanup()` to prevent the vibrator from running after the UI is destroyed.
- **Accessibility** — `performHapticFeedback()` honours the global "Touch feedback" setting in Android Accessibility options. For UI-driven interactions, prefer this over direct `VibrationEffect` calls.
- **API level guards** — Calling `performHapticFeedback()` with a constant that requires a higher API than the current device (e.g. `FEEDBACK_TOGGLE_ON` on API 29) logs a warning and returns immediately without crashing.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---