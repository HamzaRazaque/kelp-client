package com.kelpclient;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.util.Identifier;

public final class KelpIcons {
    private KelpIcons() {}
    public static Identifier id(String name) { return Identifier.of("kelpclient", "textures/gui/icons/" + name + ".png"); }
    public static void at(DrawContext ctx, String name, int x, int y) {
        ctx.drawTexture(RenderPipelines.GUI_TEXTURED, id(name), x, y, 0f, 0f, 16, 16, 16, 16, 16, 16);
    }
    public static void draw(DrawContext ctx, ButtonWidget b, String name) { at(ctx, name, b.getX() + 4, b.getY() + 2); }
}
