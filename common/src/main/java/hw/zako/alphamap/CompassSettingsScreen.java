package hw.zako.alphamap;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class CompassSettingsScreen extends Screen {

    private static final int WIDTH = 150;
    private static final int HEIGHT = 20;
    private static final int GAP = 24;
    private static final int COLUMN_GAP = 6;

    private static final int SPAN_STEP = 5;
    private static final int OFFSET_STEP = 2;

    private final @Nullable Screen parent;

    public CompassSettingsScreen(@Nullable Screen parent) {
        super(Component.translatable("alphamap.heading.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        MapSettings settings = MapSettings.get();
        int left = (width - WIDTH * 2 - COLUMN_GAP) / 2;
        int right = left + WIDTH + COLUMN_GAP;
        int top = height / 4;

        toggle(left, top, settings::headingCompass, settings::headingCompass,
                "alphamap.heading.on", "alphamap.heading.off");

        addRenderableWidget(new SettingsSlider(left, top + GAP, WIDTH, HEIGHT,
                (Math.clamp(settings.headingScale(), MapSettings.MIN_SCALE, MapSettings.MAX_SCALE) - MapSettings.MIN_SCALE)
                        / (MapSettings.MAX_SCALE - MapSettings.MIN_SCALE)) {
            @Override
            protected void updateMessage() {
                setMessage(Component.translatable("alphamap.heading.size", Math.round(scale(value) * 100) + "%"));
            }

            @Override
            protected void applyValue() {
                settings.headingScale(scale(value));
                settings.clampAndSave();
            }
        });

        addRenderableWidget(new SettingsSlider(left, top + GAP * 2, WIDTH, HEIGHT,
                fraction(settings.headingSpan(), MapSettings.MIN_HEADING_SPAN, MapSettings.MAX_HEADING_SPAN)) {
            @Override
            protected void updateMessage() {
                setMessage(Component.translatable("alphamap.heading.span", span(value) * 2));
            }

            @Override
            protected void applyValue() {
                settings.headingSpan(span(value));
                settings.clampAndSave();
            }
        });

        addRenderableWidget(new SettingsSlider(left, top + GAP * 3, WIDTH, HEIGHT,
                (settings.headingOpacity() - 0.1) / 0.9) {
            @Override
            protected void updateMessage() {
                setMessage(Component.translatable("alphamap.heading.opacity", Math.round(opacity(value) * 100) + "%"));
            }

            @Override
            protected void applyValue() {
                settings.headingOpacity(opacity(value));
                settings.clampAndSave();
            }
        });

        addRenderableWidget(new SettingsSlider(right, top, WIDTH, HEIGHT,
                fraction(settings.headingOffset(), 0, MapSettings.MAX_HEADING_OFFSET)) {
            @Override
            protected void updateMessage() {
                setMessage(Component.translatable("alphamap.heading.offset", offset(value)));
            }

            @Override
            protected void applyValue() {
                settings.headingOffset(offset(value));
                settings.clampAndSave();
            }
        });

        toggle(right, top + GAP, settings::headingDegrees, settings::headingDegrees,
                "alphamap.heading.degrees.on", "alphamap.heading.degrees.off");
        toggle(right, top + GAP * 2, settings::headingBearing, settings::headingBearing,
                "alphamap.heading.bearing.on", "alphamap.heading.bearing.off");
        toggle(right, top + GAP * 3, settings::headingEverywhere, settings::headingEverywhere,
                "alphamap.heading.everywhere.on", "alphamap.heading.everywhere.off");

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds((width - WIDTH) / 2, top + GAP * 5, WIDTH, HEIGHT)
                .build());
    }

    @Override
    public void onClose() {
        Vanilla.setScreen(minecraft, parent);
    }

    private void toggle(int x, int y, BooleanSupplier reader, Consumer<Boolean> writer,
                        String on, String off) {
        addRenderableWidget(Button.builder(label(reader.getAsBoolean(), on, off), button -> {
            writer.accept(!reader.getAsBoolean());
            MapSettings.get().clampAndSave();
            button.setMessage(label(reader.getAsBoolean(), on, off));
        }).bounds(x, y, WIDTH, HEIGHT).build());
    }

    private static Component label(boolean value, String on, String off) {
        return Component.translatable(value ? on : off);
    }

    private static double scale(double slider) {
        return MapSettings.MIN_SCALE + slider * (MapSettings.MAX_SCALE - MapSettings.MIN_SCALE);
    }

    private static double opacity(double slider) {
        return 0.1 + slider * 0.9;
    }

    private static int span(double slider) {
        return stepped(slider, MapSettings.MIN_HEADING_SPAN, MapSettings.MAX_HEADING_SPAN, SPAN_STEP);
    }

    private static int offset(double slider) {
        return stepped(slider, 0, MapSettings.MAX_HEADING_OFFSET, OFFSET_STEP);
    }

    private static int stepped(double slider, int min, int max, int step) {
        return min + (int) Math.round(slider * ((max - min) / step)) * step;
    }

    private static double fraction(int value, int min, int max) {
        return (Math.clamp(value, min, max) - min) / (double) (max - min);
    }
}
