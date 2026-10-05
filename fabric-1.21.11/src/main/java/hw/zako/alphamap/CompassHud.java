package hw.zako.alphamap;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

public final class CompassHud implements HudElement {

    @Override
    public void render(GuiGraphics graphics, DeltaTracker delta) {
        HeadingCompass.draw(new Canvas(graphics));
    }
}
