import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Programmatically generates the application icon (a red shuriken on a dark
 * background) at multiple resolutions so Windows can pick the right one for
 * the taskbar / alt-tab switcher / window decoration.
 */
public final class AppIcon {
    private AppIcon() {}

    /** Build a list of icon sizes suitable for {@code setIconImages}. */
    public static List<Image> images() {
        List<Image> out = new ArrayList<>();
        for (int size : new int[]{16, 24, 32, 48, 64, 128}) {
            out.add(build(size));
        }
        return out;
    }

    private static BufferedImage build(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Rounded dark red background
        g.setColor(new Color(20, 6, 10));
        g.fillRoundRect(0, 0, size, size, size / 4, size / 4);
        // Inner border
        g.setColor(new Color(170, 30, 40));
        g.setStroke(new BasicStroke(Math.max(1, size / 32f)));
        g.drawRoundRect(size / 16, size / 16, size - size / 8, size - size / 8, size / 5, size / 5);

        // Shuriken
        int cx = size / 2, cy = size / 2;
        int r = (int) (size * 0.36);
        g.translate(cx, cy);
        // Glow
        g.setColor(new Color(255, 60, 60, 110));
        g.fillOval(-r - size / 16, -r - size / 16, (r + size / 16) * 2, (r + size / 16) * 2);
        int[] xs = {0, r / 3, r, r / 3, 0, -r / 3, -r, -r / 3};
        int[] ys = {-r, -r / 3, 0, r / 3, r, r / 3, 0, -r / 3};
        // Steel body
        g.setColor(new Color(230, 230, 235));
        g.fillPolygon(xs, ys, 8);
        // Red rim
        g.setColor(new Color(255, 70, 70));
        g.setStroke(new BasicStroke(Math.max(1, size / 28f)));
        g.drawPolygon(xs, ys, 8);
        // Center stud
        g.setColor(new Color(255, 50, 60));
        int s = Math.max(2, size / 10);
        g.fillOval(-s / 2, -s / 2, s, s);

        g.dispose();
        return img;
    }
}
