package hw.zako.alphamap;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public abstract class WaypointScreenBase extends Screen {

    private static final int WIDTH = 220;
    private static final int HEIGHT = 20;
    private static final int SWATCH = 22;
    private static final int DELETE = 0xFF5555;
    private static final int REMOVE = 70;
    private static final int COLUMN_GAP = 4;
    private static final int LABEL = 10;
    private static final String[] AXES = {"X", "Y", "Z"};

    private final int index;
    private final @Nullable Screen parent;

    private EditBox name;
    private final EditBox[] coords = new EditBox[3];
    private int colour;

    protected WaypointScreenBase(int index, @Nullable Screen parent) {
        super(Component.translatable("alphamap.waypoint.title"));
        this.index = index;
        this.parent = parent;
        this.colour = Waypoints.all().get(index).colour();
    }

    @Override
    protected void init() {
        Waypoint waypoint = Waypoints.all().get(index);
        int x = (width - WIDTH) / 2;
        int y = height / 3;

        name = new EditBox(font, x, y, WIDTH, HEIGHT, Component.translatable("alphamap.waypoint.name"));
        name.setMaxLength(32);
        name.setValue(waypoint.name());
        name.setResponder(text -> Waypoints.replace(index, current().renamed(text)));
        addRenderableWidget(name);

        int column = (WIDTH - 2 * COLUMN_GAP) / 3;
        double[] values = {Math.floor(waypoint.x()), Math.floor(waypoint.y()), Math.floor(waypoint.z())};
        for (int i = 0; i < 3; i++) {
            EditBox box = new EditBox(font, x + i * (column + COLUMN_GAP) + LABEL, y + 28,
                    column - LABEL, HEIGHT, Component.literal(AXES[i]));
            box.setMaxLength(9);
            box.setValue(String.valueOf((long) values[i]));
            box.setResponder(text -> move());
            coords[i] = box;
            addRenderableWidget(box);
        }
        y += 28;

        int palette = Waypoints.PALETTE.length;
        int paletteWidth = palette * SWATCH;
        int paletteX = (width - paletteWidth) / 2;
        for (int i = 0; i < palette; i++) {
            int chosen = Waypoints.PALETTE[i];
            addRenderableWidget(Button.builder(Component.empty(), button -> {
                colour = chosen;
                Waypoints.replace(index, current().coloured(chosen));
            }).bounds(paletteX + i * SWATCH, y + 28, SWATCH - 2, SWATCH - 2).build());
        }

        addRenderableWidget(Button.builder(world(), button -> {
            Waypoints.replace(index, current().worldHidden(!current().worldHidden()));
            button.setMessage(world());
        }).bounds(x, y + 56, WIDTH - REMOVE - COLUMN_GAP, HEIGHT).build());

        addRenderableWidget(Button.builder(
                Component.translatable("alphamap.waypoints.delete").withColor(DELETE), button -> {
                    Waypoints.remove(index);
                    onClose();
                }).bounds(x + WIDTH - REMOVE, y + 56, REMOVE, HEIGHT).build());

        Button done = Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(x, y + 84, WIDTH, HEIGHT).build();
        addRenderableWidget(done);
        setInitialFocus(done);
    }

    private void move() {
        try {
            long blockX = Long.parseLong(coords[0].getValue());
            long blockY = Long.parseLong(coords[1].getValue());
            long blockZ = Long.parseLong(coords[2].getValue());
            Waypoints.replace(index, current().moved(blockX + 0.5, blockY, blockZ + 0.5));
        } catch (NumberFormatException ignored) {
        }
    }

    private Component world() {
        return Component.translatable(current().worldHidden()
                ? "alphamap.waypoints.world.off"
                : "alphamap.waypoints.world.on");
    }

    private Waypoint current() {
        return Waypoints.all().get(index);
    }

    protected void paint(Canvas canvas) {
        int palette = Waypoints.PALETTE.length;
        int paletteX = (width - palette * SWATCH) / 2;
        int y = height / 3 + 56;
        for (int i = 0; i < palette; i++) {
            int swatch = Waypoints.PALETTE[i];
            int left = paletteX + i * SWATCH;
            canvas.fill(left + 3, y + 3, left + SWATCH - 5, y + SWATCH - 5, 0xFF000000 | swatch);
            if (swatch == colour) {
                canvas.fill(left + 1, y + 1, left + SWATCH - 3, y + 2, 0xFFFFFFFF);
                canvas.fill(left + 1, y + SWATCH - 4, left + SWATCH - 3, y + SWATCH - 3, 0xFFFFFFFF);
            }
        }
        int column = (WIDTH - 2 * COLUMN_GAP) / 3;
        for (int i = 0; i < 3; i++) {
            canvas.text(font, AXES[i], (width - WIDTH) / 2 + i * (column + COLUMN_GAP), height / 3 + 34, 0xFFFFFFFF);
        }
        canvas.centered(font, title, width / 2, height / 3 - 24, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        Vanilla.setScreen(minecraft, parent);
    }

    protected static boolean exists(int index) {
        return index >= 0 && index < Waypoints.all().size();
    }
}
