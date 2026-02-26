/**
 * ti.android.haptics — Example App
 * Demonstrates all available haptic feedback features
 */
const Haptics = require('ti.android.haptics')

// ─── Safety Check ─────────────────────────────────────────────────────────────
if (!Haptics.isSupported()) {
  Ti.API.warn('Haptics: device has no vibrator')
}

Ti.API.info(`Amplitude control: ${Haptics.hasAmplitudeControl()}`)

// ─── 1. Predefined Effects ─────────────────────────────────────────────────────

// Via string (convenient for most use cases)
Haptics.impact('light')   // → EFFECT_TICK
Haptics.impact('medium')  // → EFFECT_CLICK
Haptics.impact('heavy')   // → EFFECT_HEAVY_CLICK

// Via constant (explicit control)
Haptics.effect(Haptics.EFFECT_CLICK)
Haptics.effect(Haptics.EFFECT_DOUBLE_CLICK)
Haptics.effect(Haptics.EFFECT_HEAVY_CLICK)
Haptics.effect(Haptics.EFFECT_TICK)

// Convenience methods
Haptics.selection()    // ideal for list scroll / item selection
Haptics.doubleClick()  // ideal for double-tap gestures

// ─── 2. Notification Feedback ─────────────────────────────────────────────────
Haptics.notification('success')  // 2-pulse positive pattern
Haptics.notification('warning')  // 2-medium-pulse alert pattern
Haptics.notification('error')    // 3-sharp-pulse failure pattern

// ─── 3. Amplitude Control (oneShot) ───────────────────────────────────────────
// Precise single vibration: duration (ms) + amplitude (1-255)
Haptics.oneShot(80, Haptics.AMPLITUDE_LIGHT)   // 80ms, soft
Haptics.oneShot(80, Haptics.AMPLITUDE_MEDIUM)  // 80ms, medium
Haptics.oneShot(80, Haptics.AMPLITUDE_HEAVY)   // 80ms, strong
Haptics.oneShot(200, Haptics.AMPLITUDE_DEFAULT) // let the system decide

// Custom amplitude value
Haptics.oneShot(100, 192)  // 100ms at ~75% intensity

// ─── 4. Custom Waveforms ──────────────────────────────────────────────────────

// Simple waveform (no amplitudes — system default per segment)
Haptics.waveform({
  timings: [0, 100, 50, 100],  // [delay, on, off, on] in ms
  repeat: -1                   // -1 = play once
})

// Full control: timings + amplitudes (arrays must be same length)
Haptics.waveform({
  timings:    [0,  80, 40, 80, 40, 120],
  amplitudes: [0, 180,  0, 220,  0, 255],
  repeat: -1
})

// Repeating waveform (e.g. phone ringing) — loop from index 0
Haptics.waveform({
  timings:    [0, 400, 200],
  amplitudes: [0, 200,   0],
  repeat: 0  // loop back to index 0 indefinitely
})

// Stop the repeating waveform
setTimeout(() => Haptics.cancel(), 3000)

// ─── 5. HapticFeedbackConstants on a View ─────────────────────────────────────
// This uses View.performHapticFeedback() which respects system accessibility settings

const btn = Ti.UI.createButton({ title: 'Tap me' })

btn.addEventListener('click', () => {
  Haptics.performHapticFeedback(btn, Haptics.FEEDBACK_VIRTUAL_KEY)
})

btn.addEventListener('longpress', () => {
  Haptics.performHapticFeedback(btn, Haptics.FEEDBACK_LONG_PRESS)
})

// All available FEEDBACK_* constants:
// Haptics.FEEDBACK_LONG_PRESS
// Haptics.FEEDBACK_VIRTUAL_KEY
// Haptics.FEEDBACK_KEYBOARD_PRESS
// Haptics.FEEDBACK_KEYBOARD_RELEASE
// Haptics.FEEDBACK_KEYBOARD_TAP
// Haptics.FEEDBACK_CLOCK_TICK
// Haptics.FEEDBACK_CONTEXT_CLICK
// Haptics.FEEDBACK_CONFIRM          (API 30+)
// Haptics.FEEDBACK_REJECT           (API 30+)
// Haptics.FEEDBACK_GESTURE_START    (API 33+)
// Haptics.FEEDBACK_GESTURE_END      (API 33+)
// Haptics.FEEDBACK_DRAG_START       (API 33+)
// Haptics.FEEDBACK_SEGMENT_TICK     (API 33+)
// Haptics.FEEDBACK_SEGMENT_FREQUENT_TICK (API 33+)
// Haptics.FEEDBACK_TOGGLE_ON        (API 34+)
// Haptics.FEEDBACK_TOGGLE_OFF       (API 34+)
// Haptics.FEEDBACK_NO_HAPTICS       (disables feedback on a view)

// ─── Simple Demo Window ───────────────────────────────────────────────────────
const win = Ti.UI.createWindow({ backgroundColor: '#1a1a2e', layout: 'vertical' })

const buttons = [
  { title: 'Light Impact',      action: () => Haptics.impact('light')           },
  { title: 'Medium Impact',     action: () => Haptics.impact('medium')          },
  { title: 'Heavy Impact',      action: () => Haptics.impact('heavy')           },
  { title: 'Double Click',      action: () => Haptics.doubleClick()             },
  { title: 'Selection',         action: () => Haptics.selection()               },
  { title: 'Success',           action: () => Haptics.notification('success')   },
  { title: 'Warning',           action: () => Haptics.notification('warning')   },
  { title: 'Error',             action: () => Haptics.notification('error')     },
  { title: 'Custom Waveform',   action: () => Haptics.waveform({
      timings: [0, 60, 30, 60, 30, 120], amplitudes: [0, 180, 0, 220, 0, 255], repeat: -1
    })
  },
]

buttons.forEach(({ title, action }) => {
  const b = Ti.UI.createButton({
    title,
    top: 10,
    width: '85%',
    height: 50,
    borderRadius: 10,
    backgroundColor: '#16213e',
    color: '#e94560',
    font: { fontSize: 15, fontWeight: 'bold' }
  })
  b.addEventListener('click', action)
  win.add(b)
})

win.add(btn)
win.open()