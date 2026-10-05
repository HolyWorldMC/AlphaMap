package hw.zako.alphamap;

import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

@UtilityClass
public class HeadingCompass {

    private final int PIXELS_PER_DEGREE = 2;
    private final int BOSS_BAR_STEP = 19;
    private final int MARK = 0xFFD84A;

    private final String[] SIDES = {"north", "east", "south", "west"};

    public void draw(Canvas canvas) {
        Minecraft client = Minecraft.getInstance();
        MapSettings settings = MapSettings.get();
        if (client.player == null || Vanilla.hudHidden(client) || !settings.headingCompass()
                || !settings.headingEverywhere() && !AtlasClient.available()) return;

        int bosses = Vanilla.bossBars(client);
        int top = (bosses == 0 ? 3 : bosses * BOSS_BAR_STEP + 2) + settings.headingOffset();
        int span = settings.headingSpan();
        int half = span * PIXELS_PER_DEGREE;
        double opacity = settings.headingOpacity();

        // Minecraft yaw 0 points south; Rust-style bearing has north at 0 growing clockwise.
        double heading = Math.floorMod(Math.round((client.player.getYRot() + 180) * 10), 3600) / 10.0;

        canvas.push(canvas.width() / 2, top, (float) settings.headingScale());
        canvas.fill(-half, 10, half, 11, colour(0x60, opacity, 0xFFFFFF));

        int first = (int) Math.ceil((heading - span) / 5) * 5;
        for (int degree = first; degree <= heading + span; degree += 5) {
            int x = (int) Math.round((degree - heading) * PIXELS_PER_DEGREE);
            double edge = Math.abs(x) / (double) half;
            int alpha = (int) (255 * Math.clamp(1 - edge * edge, 0, 1) * opacity);
            if (alpha < 8) continue;

            int bearing = Math.floorMod(degree, 360);
            boolean labelled = bearing % 15 == 0;
            canvas.fill(x, labelled ? 7 : 9, x + 1, 10, alpha << 24 | 0xFFFFFF);
            if (!labelled) continue;

            if (bearing % 45 == 0) {
                int colour = bearing % 90 == 0 ? 0xFFFFFF : 0xD0D0D0;
                canvas.centered(client.font, side(bearing / 45), x, -2, alpha << 24 | colour);
            } else if (settings.headingDegrees()) {
                canvas.centered(client.font, String.valueOf(bearing), x, -1, alpha << 24 | 0xA0A0A0);
            }
        }

        canvas.fill(0, 7, 1, 14, colour(0xFF, opacity, MARK));
        if (settings.headingBearing()) {
            canvas.centered(client.font, String.valueOf(Math.floorMod(Math.round(heading), 360)),
                    0, 15, colour(0xFF, opacity, MARK));
        }
        canvas.pop();
    }

    private int colour(int alpha, double opacity, int rgb) {
        return (int) (alpha * opacity) << 24 | rgb;
    }

    private Component side(int octant) {
        if (octant % 2 == 0) return letter(octant / 2);
        int vertical = octant == 1 || octant == 7 ? 0 : 2;
        int horizontal = octant < 4 ? 1 : 3;
        return letter(vertical).copy().append(letter(horizontal));
    }

    private Component letter(int side) {
        return Component.translatable("alphamap.compass." + SIDES[side]);
    }
}
