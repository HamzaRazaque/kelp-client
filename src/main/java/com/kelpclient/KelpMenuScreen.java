package com.kelpclient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/** Right Shift menu: icon bar on top of the live game, HUD modules can be dragged around. */
public class KelpMenuScreen extends Screen {
    private static final String[] ICONS = {"kelpmods", "cosmetics", "pin", "account", "switch", "options"};
    private final Screen parent;
    private final List<ButtonWidget> btns = new ArrayList<>();
    private KelpHud.HudModule dragging;
    private int offX, offY, barX, barY, barW;
    private boolean wasL, wasR, armed;

    public KelpMenuScreen(Screen parent) { super(Text.literal("Kelp Client")); this.parent = parent; }

    private String label(int i) {
        return switch (i) {
            case 0 -> "Kelp Mods";
            case 1 -> "Cosmetics";
            case 2 -> "Waypoints";
            case 3 -> "Accounts";
            case 4 -> KelpConfig.get().kelpUi ? "Title screen: Kelp UI" : "Title screen: Minecraft UI";
            default -> "Options";
        };
    }

    private void open(int i) {
        switch (i) {
            case 0 -> client.setScreen(new KelpModsScreen(this));
            case 1 -> client.setScreen(new KelpCosmeticsScreen(this));
            case 2 -> { KelpModsScreen.openTab(3); client.setScreen(new KelpModsScreen(this)); }
            case 3 -> client.setScreen(new KelpAccountScreen(this));
            case 4 -> { var c = KelpConfig.get(); c.kelpUi = !c.kelpUi; KelpConfig.save(); }
            default -> client.setScreen(new OptionsScreen(this, client.options));
        }
    }

    @Override
    protected void init() {
        btns.clear();
        KelpHud.editing = true;
        armed = false;
        int bs = 40, gap = 6;
        barW = ICONS.length * bs + (ICONS.length - 1) * gap;
        barX = (width - barW) / 2;
        barY = height / 2 - 6;
        for (int i = 0; i < ICONS.length; i++) {
            final int idx = i;
            btns.add(addDrawableChild(ButtonWidget.builder(Text.literal(label(i)), b -> open(idx))
                    .dimensions(barX + i * (bs + gap), barY, bs, bs).build()));
        }
    }

    @Override public void removed() { KelpHud.editing = false; KelpConfig.save(); }
    @Override public boolean shouldPause() { return false; }
    @Override public void close() { client.setScreen(parent); }
    @Override public void renderBackground(DrawContext ctx, int mx, int my, float d) {}

    private boolean over(KelpHud.HudModule m, int mx, int my) { return mx >= m.x && mx < m.x + m.w && my >= m.y && my < m.y + m.h; }

    @Override
    public void render(DrawContext ctx, int mx, int my, float d) {
        super.render(ctx, mx, my, d);
        // header chip
        String title = "KELP CLIENT";
        for (int i = 0; i < btns.size(); i++) if (btns.get(i).isHovered()) title = label(i);
        KelpUi.rect(ctx, barX, barY - 30, barW, 22, 0xF00A1A16, KelpUi.BORDER);
        KelpIcons.at(ctx, "kelpmods", barX + 5, barY - 27);
        ctx.drawTextWithShadow(textRenderer, title, barX + 26, barY - 23, KelpUi.TEXT);
        for (int i = 0; i < btns.size(); i++) {
            ButtonWidget b = btns.get(i);
            boolean hov = b.isHovered();
            KelpUi.rect(ctx, b.getX(), b.getY(), b.getWidth(), b.getHeight(), hov ? KelpUi.CARD_HOVER : 0xF00A1A16, hov ? KelpUi.ACCENT : KelpUi.BORDER);
            KelpIcons.scaled(ctx, ICONS[i], b.getX() + 4, b.getY() + 4, 32);
        }
        ctx.drawCenteredTextWithShadow(textRenderer, "Drag modules to reposition  |  Right-click to disable  |  Right Shift to close",
                width / 2, height - 46, KelpUi.DIM);

        // editor: outlines + dragging, using raw mouse state so no event overrides are needed
        for (KelpHud.HudModule m : KelpHud.MODULES) {
            if (!m.isOn() || !m.hasContent()) continue;
            boolean hov = over(m, mx, my) || m == dragging;
            KelpUi.outline(ctx, m.x - 1, m.y - 1, m.w + 2, m.h + 2, hov ? KelpUi.ACCENT : 0x99FFFFFF);
            ctx.drawTextWithShadow(textRenderer, m.name, m.x, m.y > 11 ? m.y - 11 : m.y + m.h + 2, KelpUi.DIM);
        }
        long win = GLFW.glfwGetCurrentContext();
        boolean l = GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean r = GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        boolean shift = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
        if (!shift) armed = true; else if (armed) { close(); return; }

        boolean overBar = my >= barY - 32 && my <= barY + 42 && mx >= barX - 2 && mx <= barX + barW + 2;
        if (l && !wasL && !overBar) {
            for (int i = KelpHud.MODULES.size() - 1; i >= 0; i--) {
                var m = KelpHud.MODULES.get(i);
                if (m.isOn() && m.hasContent() && over(m, mx, my)) { dragging = m; offX = mx - m.x; offY = my - m.y; break; }
            }
        }
        if (!l && dragging != null) { KelpConfig.save(); dragging = null; }
        if (dragging != null) {
            var m = dragging;
            int nx = Math.max(0, Math.min(width - m.w, mx - offX)), ny = Math.max(0, Math.min(height - m.h, my - offY));
            float fx = width - m.w <= 0 ? 0 : (float) nx / (width - m.w), fy = height - m.h <= 0 ? 0 : (float) ny / (height - m.h);
            KelpConfig.get().hudPos.put(m.id, new float[]{fx, fy});
        }
        if (r && !wasR && !overBar) {
            for (var m : KelpHud.MODULES) if (m.isOn() && m.hasContent() && over(m, mx, my)) { m.setOn(false); KelpConfig.save(); break; }
        }
        wasL = l; wasR = r;
    }
}
