package com.kelpclient;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class KelpCosmeticsScreen extends Screen {
    private final Screen parent;
    private final List<Consumer<DrawContext>> overlays = new ArrayList<>();
    private int px, py, pw, ph, msgColor = KelpUi.DIM;
    private String msg = "Only you can see your skin and cape.";

    public KelpCosmeticsScreen(Screen parent) { super(Text.literal("Cosmetics")); this.parent = parent; }

    private void ok(String m) { msg = m; msgColor = KelpUi.ACCENT; }
    private void fail(String m) { msg = m == null ? "Something went wrong" : m; msgColor = KelpUi.RED; }

    private void upload(boolean skin) {
        String path = skin ? Cosmetics.pickFile("Choose a skin", "PNG images", "*.png")
                           : Cosmetics.pickFile("Choose a cape", "PNG or GIF images", "*.png", "*.gif");
        if (path == null) return;
        try {
            if (skin) { Cosmetics.setSkin(Path.of(path)); ok("Skin applied"); }
            else { Cosmetics.setCape(Path.of(path)); ok(Cosmetics.capeFrames() > 1 ? "Animated cape applied (" + Cosmetics.capeFrames() + " frames)" : "Cape applied"); }
        } catch (Exception e) { fail(e.getMessage()); }
        clearAndInit();
    }

    @Override
    protected void init() {
        overlays.clear();
        var c = KelpConfig.get();
        pw = Math.min(450, width - 16); ph = Math.min(300, height - 16);
        px = (width - pw) / 2; py = (height - ph) / 2;
        int rx = px + 190, rw = pw - 190 - 12;

        ButtonWidget upSkin = addDrawableChild(ButtonWidget.builder(Text.literal("Upload skin"), b -> upload(true)).dimensions(rx, py + 44, rw - 72, 22).build());
        ButtonWidget resetSkin = addDrawableChild(ButtonWidget.builder(Text.literal("Reset"), b -> { Cosmetics.clearSkin(); ok("Skin reset"); clearAndInit(); }).dimensions(rx + rw - 68, py + 44, 68, 22).build());
        ButtonWidget skinOn = addDrawableChild(ButtonWidget.builder(Text.empty(), b -> { c.skinOn = !c.skinOn; KelpConfig.save(); }).dimensions(rx, py + 72, rw, 28).build());
        ButtonWidget arms = addDrawableChild(ButtonWidget.builder(Text.literal("Arms"), b -> { c.slim = !c.slim; KelpConfig.save(); }).dimensions(rx, py + 104, rw, 22).build());

        ButtonWidget upCape = addDrawableChild(ButtonWidget.builder(Text.literal("Upload cape"), b -> upload(false)).dimensions(rx, py + 152, rw - 72, 22).build());
        ButtonWidget resetCape = addDrawableChild(ButtonWidget.builder(Text.literal("Reset"), b -> { Cosmetics.clearCape(); ok("Cape reset"); clearAndInit(); }).dimensions(rx + rw - 68, py + 152, 68, 22).build());
        ButtonWidget capeOn = addDrawableChild(ButtonWidget.builder(Text.empty(), b -> { c.capeOn = !c.capeOn; KelpConfig.save(); }).dimensions(rx, py + 180, rw, 28).build());
        ButtonWidget done = addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close()).dimensions(px + pw - 92, py + ph - 28, 84, 22).build());

        overlays.add(g -> {
            KelpUi.card(g, textRenderer, upSkin, "plus", true);
            KelpUi.card(g, textRenderer, resetSkin, "trash", false);
            KelpUi.toggleRow(g, textRenderer, skinOn, "Use custom skin", c.skinFile.isEmpty() ? "No skin uploaded yet" : c.skinFile, c.skinOn);
            KelpUi.card(g, textRenderer, arms, null, false);
            g.drawCenteredTextWithShadow(textRenderer, "Arms: " + (c.slim ? "Slim (Alex)" : "Classic (Steve)"), arms.getX() + arms.getWidth() / 2, arms.getY() + 7, KelpUi.TEXT);
            KelpUi.card(g, textRenderer, upCape, "plus", true);
            KelpUi.card(g, textRenderer, resetCape, "trash", false);
            KelpUi.toggleRow(g, textRenderer, capeOn, "Show cape", c.capeFile.isEmpty() ? "PNG or animated GIF" : c.capeFile, c.capeOn);
            KelpUi.card(g, textRenderer, done, null, true);
        });
    }

    private void part(DrawContext g, Identifier tex, int x, int y, int s, int u, int v, int w, int h, int dx, int dy) {
        g.drawTexture(RenderPipelines.GUI_TEXTURED, tex, x + dx * s, y + dy * s, (float) u, (float) v, w * s, h * s, w, h, 64, 64);
    }

    private void figure(DrawContext g, Identifier tex, int x, int y, int s, boolean slim) {
        int aw = slim ? 3 : 4;
        part(g, tex, x, y, s, 8, 8, 8, 8, 4, 0);          // head
        part(g, tex, x, y, s, 20, 20, 8, 12, 4, 8);       // body
        part(g, tex, x, y, s, 44, 20, aw, 12, 4 - aw, 8); // right arm
        part(g, tex, x, y, s, 36, 52, aw, 12, 12, 8);     // left arm
        part(g, tex, x, y, s, 4, 20, 4, 12, 4, 20);       // right leg
        part(g, tex, x, y, s, 20, 52, 4, 12, 8, 20);      // left leg
        part(g, tex, x, y, s, 40, 8, 8, 8, 4, 0);         // hat
        part(g, tex, x, y, s, 20, 36, 8, 12, 4, 8);       // jacket
        part(g, tex, x, y, s, 44, 36, aw, 12, 4 - aw, 8); // right sleeve
        part(g, tex, x, y, s, 52, 52, aw, 12, 12, 8);     // left sleeve
        part(g, tex, x, y, s, 4, 36, 4, 12, 4, 20);       // right pants
        part(g, tex, x, y, s, 4, 52, 4, 12, 8, 20);       // left pants
    }

    @Override public void close() { client.setScreen(parent); }

    @Override
    public void renderBackground(DrawContext ctx, int mx, int my, float d) {
        KelpUi.background(ctx, width, height, client.world != null);
        KelpUi.panel(ctx, px, py, pw, ph, KelpUi.PANEL, KelpUi.BORDER);
        ctx.drawTextWithShadow(textRenderer, "COSMETICS", px + 12, py + 10, KelpUi.ACCENT);
        ctx.drawTextWithShadow(textRenderer, "SKIN", px + 190, py + 30, KelpUi.DIM);
        ctx.drawTextWithShadow(textRenderer, "CAPE", px + 190, py + 138, KelpUi.DIM);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float d) {
        super.render(ctx, mx, my, d);
        for (var o : overlays) o.accept(ctx);

        var c = KelpConfig.get();
        int s = ph >= 290 ? 5 : 3;
        int bx = px + 12, by = py + 30, bw = 166, bh = ph - 70;
        KelpUi.rect(ctx, bx, by, bw, bh, 0xFF0B1512, KelpUi.BORDER);
        Identifier tex = null;
        boolean slim = c.slim;
        if (Cosmetics.hasSkin() && c.skinOn) tex = Cosmetics.skinId();
        else if (client.player != null) { tex = client.player.getSkin().body().texturePath(); slim = false; }
        int fx = bx + 10, fy = by + 14;
        if (tex != null) figure(ctx, tex, fx, fy, s, slim);
        else ctx.drawCenteredTextWithShadow(textRenderer, "No skin", fx + 8 * s, fy + 14 * s, KelpUi.DIM);
        ctx.drawCenteredTextWithShadow(textRenderer, "Front", fx + 8 * s, fy + 32 * s + 6, KelpUi.DIM);

        int cxp = fx + 16 * s + 12;
        Identifier ct = Cosmetics.capeTexture();
        if (c.capeOn && ct != null) {
            double k = Cosmetics.capeW() / 64.0;
            ctx.drawTexture(RenderPipelines.GUI_TEXTURED, ct, cxp, fy + 8 * s, (float) (1 * k), (float) (1 * k), 10 * s, 16 * s,
                    (int) (10 * k), (int) (16 * k), Cosmetics.capeW(), Cosmetics.capeH());
        } else ctx.drawCenteredTextWithShadow(textRenderer, "No cape", cxp + 5 * s, fy + 16 * s, KelpUi.DIM);
        ctx.drawCenteredTextWithShadow(textRenderer, "Cape", cxp + 5 * s, fy + 32 * s + 6, KelpUi.DIM);

        ctx.drawTextWithShadow(textRenderer, "Skin: 64x64 PNG.  Cape: PNG or GIF.", px + 190, py + 214, KelpUi.DIM);
        ctx.drawTextWithShadow(textRenderer, msg, px + 12, py + ph - 24, msgColor);
    }
}
