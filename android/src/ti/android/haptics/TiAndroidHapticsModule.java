/**
 * ti.android.haptics - Advanced Android Haptic Feedback Module
 * Exposes VibrationEffect, HapticFeedbackConstants, Waveforms, and Amplitude Control
 * Minimum API: 29 (Android 10)
 */
package ti.android.haptics;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.HapticFeedbackConstants;
import android.view.View;

import androidx.annotation.RequiresApi;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.proxy.TiViewProxy;

import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.Q)
@Kroll.module(name = "TiAndroidHaptics", id = "ti.android.haptics")
public class TiAndroidHapticsModule extends KrollModule {

    private static final String TAG = "TiAndroidHaptics";

    // ─── Predefined Effect Constants ──────────────────────────────────────────
    @Kroll.constant public static final int EFFECT_CLICK        = VibrationEffect.EFFECT_CLICK;
    @Kroll.constant public static final int EFFECT_DOUBLE_CLICK = VibrationEffect.EFFECT_DOUBLE_CLICK;
    @Kroll.constant public static final int EFFECT_HEAVY_CLICK  = VibrationEffect.EFFECT_HEAVY_CLICK;
    @Kroll.constant public static final int EFFECT_TICK         = VibrationEffect.EFFECT_TICK;

    // ─── HapticFeedbackConstants ───────────────────────────────────────────────
    @Kroll.constant public static final int FEEDBACK_LONG_PRESS      = HapticFeedbackConstants.LONG_PRESS;
    @Kroll.constant public static final int FEEDBACK_VIRTUAL_KEY     = HapticFeedbackConstants.VIRTUAL_KEY;
    @Kroll.constant public static final int FEEDBACK_KEYBOARD_PRESS  = HapticFeedbackConstants.KEYBOARD_PRESS;
    @Kroll.constant public static final int FEEDBACK_KEYBOARD_RELEASE = HapticFeedbackConstants.KEYBOARD_RELEASE;
    @Kroll.constant public static final int FEEDBACK_KEYBOARD_TAP    = HapticFeedbackConstants.KEYBOARD_TAP;
    @Kroll.constant public static final int FEEDBACK_CLOCK_TICK      = HapticFeedbackConstants.CLOCK_TICK;
    @Kroll.constant public static final int FEEDBACK_CONTEXT_CLICK   = HapticFeedbackConstants.CONTEXT_CLICK;
    @Kroll.constant public static final int FEEDBACK_CONFIRM         = HapticFeedbackConstants.CONFIRM;
    @Kroll.constant public static final int FEEDBACK_REJECT          = HapticFeedbackConstants.REJECT;
    // API 33+ — hardcoded int values (inlined by javac), usage guarded at runtime in performHapticFeedback()
    @Kroll.constant public static final int FEEDBACK_GESTURE_START          = 49;  // HapticFeedbackConstants.GESTURE_START
    @Kroll.constant public static final int FEEDBACK_GESTURE_END            = 50;  // HapticFeedbackConstants.GESTURE_END
    @Kroll.constant public static final int FEEDBACK_DRAG_START             = 51;  // HapticFeedbackConstants.DRAG_START
    @Kroll.constant public static final int FEEDBACK_SEGMENT_TICK           = 52;  // HapticFeedbackConstants.SEGMENT_TICK
    @Kroll.constant public static final int FEEDBACK_SEGMENT_FREQUENT_TICK  = 53;  // HapticFeedbackConstants.SEGMENT_FREQUENT_TICK
    // API 34+
    @Kroll.constant public static final int FEEDBACK_TOGGLE_ON   = 54;  // HapticFeedbackConstants.TOGGLE_ON
    @Kroll.constant public static final int FEEDBACK_TOGGLE_OFF  = 55;  // HapticFeedbackConstants.TOGGLE_OFF
    @Kroll.constant public static final int FEEDBACK_NO_HAPTICS  = -1;  // HapticFeedbackConstants.NO_HAPTICS

    // ─── Amplitude convenience constants ──────────────────────────────────────
    @Kroll.constant public static final int AMPLITUDE_DEFAULT = VibrationEffect.DEFAULT_AMPLITUDE;
    @Kroll.constant public static final int AMPLITUDE_MAX     = 255;
    @Kroll.constant public static final int AMPLITUDE_LIGHT   = 64;
    @Kroll.constant public static final int AMPLITUDE_MEDIUM  = 128;
    @Kroll.constant public static final int AMPLITUDE_HEAVY   = 255;

    // ──────────────────────────────────────────────────────────────────────────

    public TiAndroidHapticsModule() {
        super();
    }

    // ─── Internal helpers ──────────────────────────────────────────────────────

    private Vibrator getVibrator() {
        // We try two strategies to get a valid Vibrator instance:
        //
        // 1. VibratorManager (API 31+, preferred) — but getSystemService() can return null
        //    on some OEM ROMs (e.g. Samsung OneUI) when called on applicationContext.
        //    In that case we fall through to the legacy approach.
        //
        // 2. Legacy VIBRATOR_SERVICE — deprecated in API 31 but still functional up to
        //    at least API 34. Used as a reliable fallback.
        //
        // Both paths are wrapped in try/catch to guard against unexpected OEM behaviour.

        Context ctx = TiApplication.getInstance().getApplicationContext();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                VibratorManager vm = (VibratorManager) ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                if (vm != null) {
                    // Strategy 1: try each physical vibrator by ID (fixes Samsung OneUI bug where
                    // getDefaultVibrator().hasVibrator() incorrectly returns false on Galaxy devices)
                    int[] ids = vm.getVibratorIds();
                    if (ids != null && ids.length > 0) {
                        for (int id : ids) {
                            Vibrator v = vm.getVibrator(id);
                            if (v != null && v.hasVibrator()) {
                                Log.d(TAG, "getVibrator: using VibratorManager id=" + id);
                                return v;
                            }
                        }
                        Log.w(TAG, "getVibrator: no vibrator ID reported hasVibrator()=true, trying getDefaultVibrator()");
                    }

                    // Strategy 2: fallback to getDefaultVibrator() even if hasVibrator() was false
                    // (some OEMs report false but the vibrator still works)
                    Vibrator def = vm.getDefaultVibrator();
                    if (def != null) {
                        Log.d(TAG, "getVibrator: using VibratorManager.getDefaultVibrator() (hasVibrator=" + def.hasVibrator() + ")");
                        return def;
                    }
                }
                Log.w(TAG, "getVibrator: VibratorManager returned null, falling back to legacy VIBRATOR_SERVICE");
            } catch (Exception e) {
                Log.w(TAG, "getVibrator: VibratorManager failed (" + e.getMessage() + "), falling back to legacy VIBRATOR_SERVICE");
            }
        }

        // Legacy fallback — works on all API levels including 34
        try {
            Vibrator v = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) {
                Log.d(TAG, "getVibrator: using legacy VIBRATOR_SERVICE");
            }
            return v;
        } catch (Exception e) {
            Log.e(TAG, "getVibrator: legacy VIBRATOR_SERVICE also failed: " + e.getMessage());
            return null;
        }
    }

    private void vibrate(VibrationEffect effect) {
        Vibrator vibrator = getVibrator();
        if (vibrator == null) {
            Log.w(TAG, "Haptics not supported: vibrator service unavailable");
            return;
        }
        // NOTE: we intentionally do NOT gate on vibrator.hasVibrator() here.
        // Some Samsung OneUI devices return hasVibrator()=false from getDefaultVibrator()
        // even though the hardware is present and vibrate() works correctly.
        // getVibrator() already applies best-effort selection logic via getVibratorIds().
        try {
            vibrator.vibrate(effect);
        } catch (Exception e) {
            Log.e(TAG, "vibrate() error: " + e.getMessage());
        }
    }

    // ─── Device Capabilities ──────────────────────────────────────────────────

    /**
     * Returns true if the device has a vibrator.
     *
     * On some Samsung OneUI devices, getDefaultVibrator().hasVibrator() incorrectly
     * returns false even though the hardware exists. We try getVibratorIds() first —
     * if at least one ID is present, the hardware is real and we return true.
     */
    @Kroll.method
    public boolean isSupported() {
        Context ctx = TiApplication.getInstance().getApplicationContext();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                VibratorManager vm = (VibratorManager) ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                if (vm != null) {
                    int[] ids = vm.getVibratorIds();
                    if (ids != null && ids.length > 0) {
                        Log.d(TAG, "isSupported: true via getVibratorIds() count=" + ids.length);
                        return true;
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "isSupported: VibratorManager check failed: " + e.getMessage());
            }
        }

        // Legacy fallback
        Vibrator v = getVibrator();
        boolean supported = v != null && v.hasVibrator();
        Log.d(TAG, "isSupported: " + supported + " (legacy path)");
        return supported;
    }

    /**
     * Returns true if the device supports independent amplitude control
     * (required for precise intensity tuning via oneShot/waveform).
     */
    @Kroll.method
    public boolean hasAmplitudeControl() {
        Vibrator v = getVibrator();
        return v != null && v.hasAmplitudeControl();
    }

    // ─── Predefined Effects ───────────────────────────────────────────────────

    /**
     * Triggers a predefined system haptic effect by constant ID.
     * @param effectId One of: EFFECT_CLICK, EFFECT_DOUBLE_CLICK, EFFECT_HEAVY_CLICK, EFFECT_TICK
     */
    @Kroll.method
    public void effect(int effectId) {
        vibrate(VibrationEffect.createPredefined(effectId));
    }

    /**
     * Convenience: impact feedback mapped by string intensity.
     * @param intensity "light" | "medium" | "heavy" — maps to TICK, CLICK, HEAVY_CLICK
     */
    @Kroll.method
    public void impact(String intensity) {
        int effectId;
        switch (intensity.toLowerCase()) {
            case "light":  effectId = VibrationEffect.EFFECT_TICK;         break;
            case "heavy":  effectId = VibrationEffect.EFFECT_HEAVY_CLICK;  break;
            default:       effectId = VibrationEffect.EFFECT_CLICK;        break; // "medium"
        }
        vibrate(VibrationEffect.createPredefined(effectId));
    }

    /**
     * Convenience: selection feedback (light tick, ideal for scroll/swipe).
     */
    @Kroll.method
    public void selection() {
        vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK));
    }

    /**
     * Convenience: double-click feedback.
     */
    @Kroll.method
    public void doubleClick() {
        vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK));
    }

    /**
     * Convenience: notification feedback mapped by type.
     * @param type "success" | "warning" | "error"
     */
    @Kroll.method
    public void notification(String type) {
        switch (type.toLowerCase()) {
            case "success":
                // Click + small pause + tick = positive confirmation feel
                long[] timings    = { 0, 40, 60, 20 };
                int[]  amplitudes = { 0, 180, 0, 100 };
                vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1));
                break;
            case "error":
                // Three sharp hits = error/failure feel
                long[] eTimings    = { 0, 50, 40, 50, 40, 50 };
                int[]  eAmplitudes = { 0, 220, 0, 220, 0, 220 };
                vibrate(VibrationEffect.createWaveform(eTimings, eAmplitudes, -1));
                break;
            default: // "warning"
                // Two medium hits = alert feel
                long[] wTimings    = { 0, 60, 50, 60 };
                int[]  wAmplitudes = { 0, 160, 0, 160 };
                vibrate(VibrationEffect.createWaveform(wTimings, wAmplitudes, -1));
                break;
        }
    }

    // ─── Amplitude Control ────────────────────────────────────────────────────

    /**
     * Single vibration pulse with precise duration and amplitude.
     * Falls back to DEFAULT_AMPLITUDE on devices without amplitude control.
     * @param durationMs  Duration in milliseconds
     * @param amplitude   Intensity 1–255, or AMPLITUDE_DEFAULT (-1)
     */
    @Kroll.method
    public void oneShot(int durationMs, int amplitude) {
        vibrate(VibrationEffect.createOneShot(durationMs, amplitude));
    }

    // ─── Custom Waveforms ─────────────────────────────────────────────────────

    /**
     * Plays a custom waveform pattern.
     *
     * Accepts a KrollDict with:
     *   timings    {int[]}  — alternating off/on durations in ms (first = delay before start)
     *   amplitudes {int[]}  — amplitude per segment 0–255 (optional; uses DEFAULT_AMPLITUDE if absent)
     *   repeat     {int}    — index to loop from, -1 = no repeat (optional, default -1)
     *
     * Example (JS):
     *   Haptics.waveform({ timings: [0, 100, 50, 100], amplitudes: [0, 255, 0, 180], repeat: -1 })
     */
    @Kroll.method
    public void waveform(KrollDict options) {
        if (!options.containsKey("timings")) {
            Log.e(TAG, "waveform() requires a 'timings' array");
            return;
        }

        Object[] timingsObj = (Object[]) options.get("timings");
        long[] timings = new long[Objects.requireNonNull(timingsObj).length];
        for (int i = 0; i < timingsObj.length; i++) {
            timings[i] = ((Number) timingsObj[i]).longValue();
        }

        int repeat = options.containsKey("repeat") ? options.getInt("repeat") : -1;

        if (options.containsKey("amplitudes")) {
            Object[] ampObj = (Object[]) options.get("amplitudes");
            int[] amplitudes = new int[Objects.requireNonNull(ampObj).length];
            for (int i = 0; i < ampObj.length; i++) {
                amplitudes[i] = ((Number) ampObj[i]).intValue();
            }
            vibrate(VibrationEffect.createWaveform(timings, amplitudes, repeat));
        } else {
            vibrate(VibrationEffect.createWaveform(timings, repeat));
        }
    }

    /**
     * Stops any currently active repeating waveform.
     */
    @Kroll.method
    public void cancel() {
        Vibrator vibrator = getVibrator();
        if (vibrator != null) vibrator.cancel();
    }

    // ─── HapticFeedbackConstants (View-level) ─────────────────────────────────

    /**
     * Performs a system-level HapticFeedbackConstant on a Titanium View.
     * This uses the Android View.performHapticFeedback() API which respects
     * system accessibility settings automatically.
     *
     * @param proxy     A Titanium View proxy (e.g. a Button, Label, etc.)
     * @param constant  One of the FEEDBACK_* constants exposed by this module
     */
    @Kroll.method(runOnUiThread = true)
    public void performHapticFeedback(TiViewProxy proxy, int constant) {
        if (proxy == null) {
            Log.e(TAG, "performHapticFeedback() requires a valid Titanium view proxy");
            return;
        }

        // Guard constants that require newer API levels.
        // Android silently ignores unknown constants, but we log clearly to avoid confusion.
        // FEEDBACK_GESTURE_START/END (49,50), DRAG_START (51), SEGMENT_* (52,53) -> API 33
        // FEEDBACK_TOGGLE_ON/OFF (54,55), NO_HAPTICS (-1)                         -> API 34
        if (constant >= 49 && constant <= 53 && Build.VERSION.SDK_INT < 33) {
            Log.w(TAG, "performHapticFeedback: constant " + constant + " requires API 33+. Skipping.");
            return;
        }
        if ((constant == 54 || constant == 55) && Build.VERSION.SDK_INT < 34) {
            Log.w(TAG, "performHapticFeedback: constant " + constant + " requires API 34+. Skipping.");
            return;
        }

        try {
            View nativeView = proxy.getOrCreateView().getNativeView();
            if (nativeView != null) {
                nativeView.performHapticFeedback(constant);
            } else {
                Log.w(TAG, "Native view not yet available for proxy");
            }
        } catch (Exception e) {
            Log.e(TAG, "performHapticFeedback() error: " + e.getMessage());
        }
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    @Override
    public void onDestroy(android.app.Activity activity) {
        cancel();
        super.onDestroy(activity);
    }
}