package com.kelpclient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class KelpModsScreen extends Screen {
    private final Screen parent;
    private int col, row;

    public KelpModsScreen(Screen parent) { super(Text.literal("Kelp Mods")); this.parent = parent; }

    private void toggle(String name, BooleanSupplier get, Consumer<Boolean> set) {
        int x = width / 2 - 154 + col * 156, y = 30 + row * 24;
        addDrawableChild(ButtonWidget.builder(label(name, get.getAsBoolean()), b -> {
            set.accept(!get.getAsBoolean()); KelpConfig.save(); b.setMessage(label(name, get.getAsBoolean()));
        }).dimensions(x, y, 152, 20).build());
        if (++col == 2) { col = 0; row++; }
    }
    private static Text label(String n, boolean on) { return Text.literal(n + ": " + (on ? "ON" : "OFF")); }

    private void action(Text t, ButtonWidget.PressAction a) {
        int x = width / 2 - 154 + col * 156, y = 30 + row * 24;
        addDrawableChild(ButtonWidget.builder(t, a).dimensions(x, y, 152, 20).build());
        if (++col == 2) { col = 0; row++; }
    }

    @Override
    protected void init() {
        col = 0; row = 0;
        var c = KelpConfig.get();
        toggle("FPS", () -> c.fps, v -> c.fps = v);
        toggle("Coordinates", () -> c.coords, v -> c.coords = v);
        toggle("Direction", () -> c.direction, v -> c.direction = v);
        toggle("Day Counter", () -> c.day, v -> c.day = v);
        toggle("Ping", () -> c.ping, v -> c.ping = v);
        toggle("Server Info", () -> c.serverInfo, v -> c.serverInfo = v);
        toggle("Potion Effects", () -> c.potions, v -> c.potions = v);
        toggle("Combo Counter", () -> c.combo, v -> c.combo = v);
        toggle("Waypoints", () -> c.waypoints, v -> c.waypoints = v);
        toggle("Custom Crosshair", () -> c.customCrosshair, v -> c.customCrosshair = v);
        action(Text.literal("Style: " + KelpConfig.STYLES[c.crosshairStyle]), b -> {
            c.crosshairStyle = (c.crosshairStyle + 1) % KelpConfig.STYLES.length; KelpConfig.save();
            b.setMessage(Text.literal("Style: " + KelpConfig.STYLES[c.crosshairStyle])); });
        action(Text.literal("Color #" + (c.crosshairColor + 1)), b -> {
            c.crosshairColor = (c.crosshairColor + 1) % KelpConfig.COLORS.length; KelpConfig.save();
            b.setMessage(Text.literal("Color #" + (c.crosshairColor + 1))); });
        action(Text.literal("Size: " + c.crosshairSize), b -> {
            c.crosshairSize = c.crosshairSize >= 15 ? 2 : c.crosshairSize + 1; KelpConfig.save();
            b.setMessage(Text.literal("Size: " + c.crosshairSize)); });
        action(Text.literal("Gap: " + c.crosshairGap), b -> {
            c.crosshairGap = c.crosshairGap >= 8 ? 0 : c.crosshairGap + 1; KelpConfig.save();
            b.setMessage(Text.literal("Gap: " + c.crosshairGap)); });
        action(Text.literal("Thickness: " + c.crosshairThick), b -> {
            c.crosshairThick = c.crosshairThick >= 4 ? 1 : c.crosshairThick + 1; KelpConfig.save();
            b.setMessage(Text.literal("Thickness: " + c.crosshairThick)); });
        action(Text.literal("Add waypoint here"), b -> {
            if (client.player == null) return;
            c.waypointList.add(new KelpConfig.Waypoint("WP" + (c.waypointList.size() + 1),
                    MathHelper.floor(client.player.getX()), MathHelper.floor(client.player.getY()), MathHelper.floor(client.player.getZ())));
            KelpConfig.save(); });
        action(Text.literal("Clear waypoints"), b -> { c.waypointList.clear(); KelpConfig.save(); });
        action(Text.literal("UI: " + (c.kelpUi ? "Kelp" : "Minecraft")), b -> {
            c.kelpUi = !c.kelpUi; KelpConfig.save(); b.setMessage(Text.literal("UI: " + (c.kelpUi ? "Kelp" : "Minecraft")));
            if (client.world == null && parent instanceof KelpTitleScreen && !c.kelpUi) client.setScreen(new TitleScreen()); });
        action(Text.literal("Accounts"), b -> client.setScreen(new KelpAccountScreen(this)));
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close()).dimensions(width / 2 - 100, height - 28, 200, 20).build());
    }

    @Override public void close() { client.setScreen(parent); }

    @Override
    public void render(DrawContext ctx, int mx, int my, float d) {
        super.render(ctx, mx, my, d);
        ctx.drawCenteredTextWithShadow(textRenderer, "Kelp Mods", width / 2, 12, 0xFF7CFC7C);
    }
}
