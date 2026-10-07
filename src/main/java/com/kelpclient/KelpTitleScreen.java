package com.kelpclient;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class KelpTitleScreen extends Screen {
    private static final Identifier KELP = Identifier.of("minecraft", "textures/item/kelp.png");

    public KelpTitleScreen() { super(Text.literal("Kelp Client")); }

    @Override
    protected void init() {
        int w = 200, x = width / 2 - w / 2, y = height / 2 - 30, step = 24;
        addDrawableChild(ButtonWidget.builder(Text.translatable("menu.singleplayer"), b -> client.setScreen(new SelectWorldScreen(this))).dimensions(x, y, w, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("menu.multiplayer"), b -> client.setScreen(new MultiplayerScreen(this))).dimensions(x, y + step, w, 20).build());
        int row = 2;
        if (FabricLoader.getInstance().isModLoaded("modmenu")) {
            addDrawableChild(ButtonWidget.builder(Text.literal("Mods"), b -> openModMenu()).dimensions(x, y + step * row++, w, 20).build());
        }
        addDrawableChild(ButtonWidget.builder(Text.literal("Kelp Mods"), b -> client.setScreen(new KelpModsScreen(this))).dimensions(x, y + step * row++, w, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("menu.options"), b -> client.setScreen(new OptionsScreen(this, client.options))).dimensions(x, y + step * row, 98, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("menu.quit"), b -> client.scheduleStop()).dimensions(x + 102, y + step * row, 98, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Account: " + client.getSession().getUsername()),
                b -> client.setScreen(new KelpAccountScreen(this))).dimensions(width - 124, 4, 120, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Minecraft UI"), b -> {
            KelpConfig.get().kelpUi = false; KelpConfig.save(); client.setScreen(new TitleScreen());
        }).dimensions(4, 4, 80, 20).build());
    }

    private void openModMenu() {
        try {
            Class<?> c = Class.forName("com.terraformersmc.modmenu.gui.ModsScreen");
            client.setScreen((Screen) c.getConstructor(Screen.class).newInstance(this));
        } catch (Throwable t) { /* Mod Menu API changed: ignore */ }
    }

    @Override
    public void renderBackground(DrawContext ctx, int mx, int my, float delta) {
        ctx.fillGradient(0, 0, width, height, 0xFF04222A, 0xFF0B5A3C);
        double t = Util.getMeasuringTimeMs() / 1000.0;
        for (int i = 0; i < 16; i++) {
            int baseX = (int) (width * (i + 0.5) / 16.0);
            int seg = 14;
            for (int y = height; y > height / 3 + (i * 37 % 60); y -= seg) {
                int sway = (int) (Math.sin(t * 1.2 + y * 0.03 + i) * (8 + (height - y) * 0.03));
                int shade = 0xFF1E8B3A + ((i % 3) * 0x00061200);
                ctx.fill(baseX + sway, y - seg, baseX + sway + 5, y, shade);
            }
        }
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);
        int bob = (int) (Math.sin(Util.getMeasuringTimeMs() / 500.0) * 3);
        ctx.drawTexture(RenderPipelines.GUI_TEXTURED, KELP, width / 2 - 24, height / 2 - 110 + bob, 0f, 0f, 48, 48, 16, 16, 16, 16);
        var m = ctx.getMatrices();
        m.pushMatrix(); m.translate(width / 2f, height / 2f - 55); m.scale(2.5f, 2.5f);
        ctx.drawCenteredTextWithShadow(textRenderer, "Kelp Client", 0, 0, 0xFF7CFC7C);
        m.popMatrix();
        ctx.drawCenteredTextWithShadow(textRenderer, "Minecraft 1.21.11", width / 2, height - 12, 0xFFAAAAAA);
    }

    @Override public boolean shouldCloseOnEsc() { return false; }
}
