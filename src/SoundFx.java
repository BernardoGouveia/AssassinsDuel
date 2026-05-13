import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.util.Random;

/**
 * Synthesised sound effects (no external files). Each public method spawns
 * a daemon thread that streams a short PCM buffer to a SourceDataLine.
 *
 * The primitives below combine sine harmonics, pitch sweeps, and filtered
 * noise to give each event a distinct, smooth, thematic character.
 */
public class SoundFx {
    private static final float SAMPLE_RATE = 44100f;
    private static final Random RNG = new Random();
    private static volatile boolean enabled = true;

    public static void setEnabled(boolean e) { enabled = e; }
    public static boolean isEnabled() { return enabled; }

    // ===================== PUBLIC API =====================

    /** Soft footstep tap — short low blip. */
    public static void move() {
        if (!enabled) return;
        async(() -> {
            byte[] buf = renderTone(180, 90, 50, 0.22, 0.005, 0.040, 0.5);
            play(mix(buf, renderNoise(60, 0.06, 0.012, 0.020, 1500)));
        });
    }

    /**
     * Sword slash — whoosh + damage thwack baked into the same buffer.
     * Combining into one PCM buffer means only ONE SourceDataLine opens per
     * attack, which is far more reliable than firing two parallel audio
     * threads (the second often gets dropped on Windows).
     */
    public static void melee() {
        if (!enabled) return;
        async(() -> {
            byte[] sweep = renderSweep(1100, 140, 140, 0.26, 0.005, 0.060, 0.6);
            byte[] noise = renderNoise(120, 0.18, 0.004, 0.080, 3500);
            byte[] whoosh = mix(sweep, noise);
            // Damage lands ~40ms into the swing.
            play(mixAt(whoosh, damageBuffer(), 40));
        });
    }

    /**
     * Shuriken throw — whoosh + damage thwack baked at the projectile-arrival
     * offset (60ms per cell). Pass the Chebyshev distance from caster to target.
     */
    public static void shuriken(int distanceCells) {
        if (!enabled) return;
        async(() -> {
            byte[] sweep = renderSweepVibrato(1600, 520, 180, 0.22, 0.003, 0.080, 0.6, 28, 14);
            byte[] noise = renderNoise(80, 0.12, 0.002, 0.060, 5500);
            byte[] whoosh = mix(sweep, noise);
            int offsetMs = 60 * Math.max(1, distanceCells);
            play(mixAt(whoosh, damageBuffer(), offsetMs));
        });
    }

    /** Backwards-compatible shuriken (assumes 1-cell distance). */
    public static void shuriken() { shuriken(1); }

    /** Heal — bright bell-like ascending arpeggio with overtones. */
    public static void heal() {
        if (!enabled) return;
        async(() -> {
            // C5, E5, G5, C6 with a soft bell envelope (each note slightly overlaps the next)
            double[] freqs = {523.25, 659.25, 783.99, 1046.50};
            int noteMs = 110;
            byte[] out = new byte[0];
            for (int i = 0; i < freqs.length; i++) {
                double vol = (i == freqs.length - 1) ? 0.26 : 0.20;
                int durMs = (i == freqs.length - 1) ? 320 : noteMs;
                byte[] note = renderBell(freqs[i], durMs, vol);
                out = concat(out, note);
            }
            play(out);
        });
    }

    /** Powerup pickup — ascending chime arpeggio. */
    public static void powerup() {
        if (!enabled) return;
        async(() -> {
            double[] freqs = {880, 1108.73, 1318.51, 1760};  // A5 C#6 E6 A6
            byte[] out = new byte[0];
            for (int i = 0; i < freqs.length; i++) {
                int durMs = (i == freqs.length - 1) ? 200 : 60;
                double vol = 0.22;
                out = concat(out, renderBell(freqs[i], durMs, vol));
            }
            play(out);
        });
    }

    /** Standalone damage thwack (rarely used externally — melee/shuriken bake it in). */
    public static void damage() {
        if (!enabled) return;
        async(() -> play(damageBuffer()));
    }

    /** PCM buffer for the damage thwack: sharp transient + meaty body + crunchy noise. */
    private static byte[] damageBuffer() {
        byte[] transient_ = renderSweep(2200, 700, 35, 0.42, 0.0005, 0.020, 0.4);
        byte[] body = renderSweep(380, 95, 160, 0.40, 0.002, 0.110, 0.6);
        byte[] noise = renderNoise(90, 0.28, 0.001, 0.050, 2400);
        return mix(mix(transient_, body), noise);
    }

    /** Death — mournful descending sweep + low rumble. */
    public static void death() {
        if (!enabled) return;
        async(() -> {
            byte[] descend = renderSweep(440, 110, 600, 0.26, 0.010, 0.250, 0.7);
            byte[] rumble = renderTone(73, 41, 500, 0.16, 0.020, 0.250, 0.6); // A1 + sub
            play(mix(descend, rumble));
        });
    }

    /** Game over — triumphant fanfare. */
    public static void victory() {
        if (!enabled) return;
        async(() -> {
            double[] freqs = {523.25, 659.25, 783.99, 1046.50, 1318.51};
            byte[] out = new byte[0];
            for (int i = 0; i < freqs.length; i++) {
                int durMs = (i == freqs.length - 1) ? 480 : 110;
                out = concat(out, renderBell(freqs[i], durMs, 0.26));
            }
            play(out);
        });
    }

    /** Buzz on invalid action. */
    public static void fail() {
        if (!enabled) return;
        async(() -> play(renderSweep(220, 90, 220, 0.22, 0.004, 0.080, 0.6)));
    }

    /** Arcade intro fanfare for the welcome screen. */
    public static void intro() {
        if (!enabled) return;
        async(() -> {
            // 8-bit style ascending fanfare with rhythm
            byte[] out = new byte[0];
            out = concat(out, renderBell(392.00, 90, 0.22)); // G4
            out = concat(out, renderBell(523.25, 90, 0.22)); // C5
            out = concat(out, renderBell(659.25, 90, 0.24)); // E5
            out = concat(out, renderBell(1046.50, 140, 0.26)); // C6
            out = concat(out, silence(60));
            out = concat(out, renderBell(880.00, 80, 0.22));  // A5
            out = concat(out, renderBell(1046.50, 80, 0.24)); // C6
            out = concat(out, renderBell(1318.51, 360, 0.28)); // E6 sustained
            play(out);
        });
    }

    // ===================== PRIMITIVES =====================

    /**
     * Tone with optional 2nd harmonic for richer timbre.
     * attackSec / releaseSec are envelope times. Both ≤ duration.
     */
    private static byte[] renderTone(double freq, double harmonicFreq, int durationMs, double volume,
                                     double attackSec, double releaseSec, double harmonicAmp) {
        int n = (int) (SAMPLE_RATE * durationMs / 1000.0);
        byte[] buf = new byte[n * 2];
        int aS = Math.max(1, (int) (SAMPLE_RATE * attackSec));
        int rS = Math.max(1, (int) (SAMPLE_RATE * releaseSec));
        double phase = 0, phase2 = 0;
        for (int i = 0; i < n; i++) {
            double env;
            if (i < aS) env = i / (double) aS;
            else if (i > n - rS) env = (n - i) / (double) rS;
            else env = 1.0;
            phase += 2 * Math.PI * freq / SAMPLE_RATE;
            phase2 += 2 * Math.PI * harmonicFreq / SAMPLE_RATE;
            double s = (Math.sin(phase) + Math.sin(phase2) * harmonicAmp) / (1.0 + harmonicAmp);
            writeSample(buf, i, s * env * volume);
        }
        return buf;
    }

    /** Linear pitch sweep from startHz to endHz. */
    private static byte[] renderSweep(double startHz, double endHz, int durationMs, double volume,
                                      double attackSec, double releaseSec, double harmonicAmp) {
        return renderSweepVibrato(startHz, endHz, durationMs, volume, attackSec, releaseSec,
                harmonicAmp, 0, 0);
    }

    /** Pitch sweep with sinusoidal vibrato (depthHz around the current freq). */
    private static byte[] renderSweepVibrato(double startHz, double endHz, int durationMs, double volume,
                                             double attackSec, double releaseSec, double harmonicAmp,
                                             double vibratoDepthHz, double vibratoRateHz) {
        int n = (int) (SAMPLE_RATE * durationMs / 1000.0);
        byte[] buf = new byte[n * 2];
        int aS = Math.max(1, (int) (SAMPLE_RATE * attackSec));
        int rS = Math.max(1, (int) (SAMPLE_RATE * releaseSec));
        double phase = 0, phase2 = 0;
        for (int i = 0; i < n; i++) {
            double t = i / (double) n;
            double f = startHz + (endHz - startHz) * t;
            if (vibratoDepthHz > 0)
                f += vibratoDepthHz * Math.sin(2 * Math.PI * vibratoRateHz * i / SAMPLE_RATE);
            phase += 2 * Math.PI * f / SAMPLE_RATE;
            phase2 += 2 * Math.PI * (2 * f) / SAMPLE_RATE;
            double env;
            if (i < aS) env = i / (double) aS;
            else if (i > n - rS) env = (n - i) / (double) rS;
            else env = 1.0;
            double s = (Math.sin(phase) + Math.sin(phase2) * harmonicAmp) / (1.0 + harmonicAmp);
            writeSample(buf, i, s * env * volume);
        }
        return buf;
    }

    /** White noise with single-pole low-pass at cutoffHz. */
    private static byte[] renderNoise(int durationMs, double volume,
                                      double attackSec, double releaseSec, double cutoffHz) {
        int n = (int) (SAMPLE_RATE * durationMs / 1000.0);
        byte[] buf = new byte[n * 2];
        int aS = Math.max(1, (int) (SAMPLE_RATE * attackSec));
        int rS = Math.max(1, (int) (SAMPLE_RATE * releaseSec));
        double prev = 0;
        double alpha = Math.min(0.99, cutoffHz / (SAMPLE_RATE / 2));
        for (int i = 0; i < n; i++) {
            double raw = RNG.nextDouble() * 2 - 1;
            prev = prev + alpha * (raw - prev);
            double env;
            if (i < aS) env = i / (double) aS;
            else if (i > n - rS) env = (n - i) / (double) rS;
            else env = 1.0;
            writeSample(buf, i, prev * env * volume);
        }
        return buf;
    }

    /** Bell-like note: fundamental + 2nd harmonic with exponential decay. */
    private static byte[] renderBell(double freq, int durationMs, double volume) {
        int n = (int) (SAMPLE_RATE * durationMs / 1000.0);
        byte[] buf = new byte[n * 2];
        int aS = Math.max(1, (int) (SAMPLE_RATE * 0.004));
        double phase = 0, phase2 = 0, phase3 = 0;
        double decay = 4.5 / (durationMs / 1000.0); // exponential decay rate
        for (int i = 0; i < n; i++) {
            double t = i / SAMPLE_RATE;
            phase += 2 * Math.PI * freq / SAMPLE_RATE;
            phase2 += 2 * Math.PI * (freq * 2.01) / SAMPLE_RATE;
            phase3 += 2 * Math.PI * (freq * 3.0) / SAMPLE_RATE;
            double env;
            if (i < aS) env = i / (double) aS;
            else env = Math.exp(-decay * (t - aS / SAMPLE_RATE));
            double s = (Math.sin(phase) + Math.sin(phase2) * 0.45 + Math.sin(phase3) * 0.18) / 1.63;
            writeSample(buf, i, s * env * volume);
        }
        return buf;
    }

    private static byte[] silence(int durationMs) {
        int n = (int) (SAMPLE_RATE * durationMs / 1000.0);
        return new byte[n * 2];
    }

    // ===================== UTIL =====================

    /** Sample-by-sample mix of two PCM buffers (same sample rate). Result is max(a, b) length. */
    private static byte[] mix(byte[] a, byte[] b) {
        int na = a.length / 2, nb = b.length / 2;
        int n = Math.max(na, nb);
        byte[] out = new byte[n * 2];
        for (int i = 0; i < n; i++) {
            int sA = i < na ? readSample(a, i) : 0;
            int sB = i < nb ? readSample(b, i) : 0;
            int mixed = sA + sB;
            if (mixed > 32767) mixed = 32767;
            if (mixed < -32768) mixed = -32768;
            out[i * 2] = (byte) (mixed & 0xff);
            out[i * 2 + 1] = (byte) ((mixed >> 8) & 0xff);
        }
        return out;
    }

    /** Mix {@code b} into {@code a} starting at {@code offsetMs}. Output is the longer of the two. */
    private static byte[] mixAt(byte[] a, byte[] b, int offsetMs) {
        int offsetSamples = (int) (SAMPLE_RATE * offsetMs / 1000.0);
        int na = a.length / 2;
        int nb = b.length / 2;
        int totalLen = Math.max(na, offsetSamples + nb);
        byte[] out = new byte[totalLen * 2];
        // copy A
        System.arraycopy(a, 0, out, 0, a.length);
        // mix in B at offset
        for (int i = 0; i < nb; i++) {
            int outIdx = offsetSamples + i;
            if (outIdx >= totalLen) break;
            int sA = (outIdx < na) ? readSample(a, outIdx) : 0;
            int sB = readSample(b, i);
            int mixed = sA + sB;
            if (mixed > 32767) mixed = 32767;
            if (mixed < -32768) mixed = -32768;
            out[outIdx * 2] = (byte) (mixed & 0xff);
            out[outIdx * 2 + 1] = (byte) ((mixed >> 8) & 0xff);
        }
        return out;
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] out = new byte[a.length + b.length];
        System.arraycopy(a, 0, out, 0, a.length);
        System.arraycopy(b, 0, out, a.length, b.length);
        return out;
    }

    private static int readSample(byte[] buf, int i) {
        int lo = buf[i * 2] & 0xff;
        int hi = buf[i * 2 + 1];
        return (hi << 8) | lo;
    }

    private static void writeSample(byte[] buf, int i, double sample) {
        if (sample > 1.0) sample = 1.0;
        if (sample < -1.0) sample = -1.0;
        short s = (short) (sample * 32767);
        buf[i * 2] = (byte) (s & 0xff);
        buf[i * 2 + 1] = (byte) ((s >> 8) & 0xff);
    }

    private static void async(Runnable r) {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.start();
    }

    private static void play(byte[] buf) {
        if (!enabled || buf.length == 0) return;
        try {
            AudioFormat fmt = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
            try (SourceDataLine line = AudioSystem.getSourceDataLine(fmt)) {
                line.open(fmt);
                line.start();
                line.write(buf, 0, buf.length);
                line.drain();
                line.stop();
            }
        } catch (Exception ignored) {
            // audio not available
        }
    }
}
