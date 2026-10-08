package com.kelpclient;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class KelpTitleScreen extends Screen {
    private final List<Consumer<DrawContext>> overlays = new ArrayList<>();
    private int topY;

    public KelpTitleScreen() { super(Text.literal("Kelp Client")); }

    private void card(ButtonWidget b, String icon, boolean accent) {
        addDrawableChild(b);
        overlays.add(c -> KelpUi.card(c, textRenderer, b, icon, accent));
    }

    @Override
    protected void init() {
        overlays.clear();
        boolean modMenu = FabricLoader.getInstance().isModLoaded("modmenu");
        int rows = modMenu ? 5 : 4, bw = 220, bh = 24, step = 28;
        topY = Math.min(height / 2 - 6, height - rows * step - 14);
        int x = width / 2 - bw / 2, y = topY;

        card(ButtonWidget.builder(Text.translatable("menu.singleplayer"), b -> client.setScreen(new SelectWorldScreen(this))).dimensions(x, y, bw, bh).build(), "singleplayer", false);
        y += step;
        card(ButtonWidget.builder(Text.translatable("menu.multiplayer"), b -> client.setScreen(new MultiplayerScreen(this))).dimensions(x, y, bw, bh).build(), "multiplayer", false);
        y += step;
        if (modMenu) {
            card(ButtonWidget.builder(Text.literal("Mods"), b -> openModMenu()).dimensions(x, y, bw, bh).build(), "mods", false);
            y += step;
        }
        card(ButtonWidget.builder(Text.literal("Kelp Mods"), b -> client.setScreen(new KelpModsScreen(this))).dimensions(x, y, bw, bh).build(), "kelpmods", true);
        y += step;
        card(ButtonWidget.builder(Text.translatable("menu.options"), b -> client.setScreen(new OptionsScreen(this, client.options))).dimensions(x, y, 108, bh).build(), "options", false);
        card(ButtonWidget.builder(Text.translatable("menu.quit"), b -> client.scheduleStop()).dimensions(x + 112, y, 108, bh).build(), "quit", false);

        card(ButtonWidget.builder(Text.literal("Minecraft UI"), b -> {
            KelpConfig.get().kelpUi = false; KelpConfig.save(); client.setScreen(new TitleScreen());
        }).dimensions(6, 6, 124, 22).build(), "switch", false);
        card(ButtonWidget.builder(Text.literal(client.getSession().getUsername()),
                b -> client.setScreen(new KelpAccountScreen(this))).dimensions(width - 166, 6, 160, 22).build(), "account", false);
    }

    private void openModMenu() {
        try {
            Class<?> c = Class.forName("com.terraformersmc.modmenu.gui.ModsScreen");
            client.setScreen((Screen) c.getConstructor(Screen.class).newInstance(this));
        } catch (Throwable t) { /* Mod Menu API changed: ignore */ }
    }

    @Override
    public void renderBackground(DrawContext ctx, int mx, int my, float delta) {
        KelpUi.background(ctx, width, height, false);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);
        for (var o : overlays) o.accept(ctx);

        int cx = width / 2;
        int logo = 56, bob = (int) (Math.sin(Util.getMeasuringTimeMs() / 600.0) * 3);
        int ly = Math.max(2, topY - 118) + bob;
        ctx.fill(cx - 44, ly + 4, cx + 44, ly + 56, 0x103DDC6B);
        ctx.fill(cx - 36, ly + 8, cx + 36, ly + 52, 0x143DDC6B);
        ctx.drawTexture(RenderPipelines.GUI_TEXTURED, KelpUi.LOGO, cx - logo / 2, ly, 0f, 0f, logo, logo, 32, 32, 32, 32);

        var m = ctx.getMatrices();
        int ty = Math.max(2, topY - 118) + 66;
        String a = "KELP", b = " CLIENT";
        int wa = textRenderer.getWidth(a), wb = textRenderer.getWidth(b);
        m.pushMatrix(); m.translate(cx, ty); m.scale(2.6f, 2.6f);
        ctx.drawTextWithShadow(textRenderer, a, -(wa + wb) / 2, 0, KelpUi.ACCENT);
        ctx.drawTextWithShadow(textRenderer, b, -(wa + wb) / 2 + wa, 0, 0xFFFFFFFF);
        m.popMatrix();
        ctx.drawCenteredTextWithShadow(textRenderer, "Minecraft 1.21.11  |  Fabric", cx, ty + 26, KelpUi.DIM);
        ctx.drawTextWithShadow(textRenderer, "Kelp Client", 6, height - 12, KelpUi.DIM);
    }

    @Override public boolean shouldCloseOnEsc() { return false; }
}
