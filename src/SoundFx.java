import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

/**
 * Simple synthesized sound effects (no external WAV files needed).
 * Each call spawns a daemon thread that writes a short tone to the audio line.
 */
public class SoundFx {
    private static final float SAMPLE_RATE = 44100f;
    private static volatile boolean enabled = true;

    public static void setEnabled(boolean e) { enabled = e; }
    public static boolean isEnabled() { return enabled; }

    /** Play a single tone asynchronously. */
    public static void tone(double freqHz, int durationMs, double volume) {
        if (!enabled) return;
        Thread t = new Thread(() -> playTone(freqHz, durationMs, volume));
        t.setDaemon(true);
        t.start();
    }

    private static void playTone(double freqHz, int durationMs, double volume) {
        try {
            int numSamples = (int) (SAMPLE_RATE * durationMs / 1000.0);
            byte[] buffer = new byte[numSamples * 2];
            int attackSamples = (int) (SAMPLE_RATE * 0.005);
            int releaseSamples = (int) (SAMPLE_RATE * 0.030);
            for (int i = 0; i < numSamples; i++) {
                double env = 1.0;
                if (i < attackSamples) env = i / (double) attackSamples;
                else if (i > numSamples - releaseSamples) env = (numSamples - i) / (double) releaseSamples;
                double sample = Math.sin(2 * Math.PI * freqHz * i / SAMPLE_RATE) * env * volume;
                short s = (short) (sample * 32767);
                buffer[i * 2] = (byte) (s & 0xff);
                buffer[i * 2 + 1] = (byte) ((s >> 8) & 0xff);
            }
            AudioFormat fmt = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
            try (SourceDataLine line = AudioSystem.getSourceDataLine(fmt)) {
                line.open(fmt);
                line.start();
                line.write(buffer, 0, buffer.length);
                line.drain();
                line.stop();
            }
        } catch (Exception ignored) {
            // Audio not available — silently ignore
        }
    }

    private static void chord(double... freqsAndDurations) {
        // Pairs of [freqHz, durationMs] played sequentially in one daemon thread.
        if (!enabled) return;
        Thread t = new Thread(() -> {
            for (int i = 0; i + 1 < freqsAndDurations.length; i += 2) {
                playTone(freqsAndDurations[i], (int) freqsAndDurations[i + 1], 0.25);
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public static void move()     { tone(440, 45, 0.12); }
    public static void melee()    { tone(170, 130, 0.35); }
    public static void shuriken() { tone(900, 80, 0.22); }
    public static void heal()     { chord(523, 60, 659, 60, 784, 110); }
    public static void powerup()  { chord(700, 50, 1050, 80); }
    public static void death()    { chord(220, 200, 110, 350); }
    public static void victory()  { chord(523, 90, 659, 90, 784, 90, 1046, 220); }
    public static void fail()     { tone(120, 120, 0.25); }
}
