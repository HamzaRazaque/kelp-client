package com.kelpclient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import org.lwjgl.glfw.GLFW;

public class KelpClient implements ClientModInitializer {
    public static KeyBinding MENU_KEY, WAYPOINT_KEY;

    @Override
    public void onInitializeClient() {
        KelpConfig.load();
        MENU_KEY = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.kelpclient.menu", GLFW.GLFW_KEY_RIGHT_SHIFT, KeyBinding.Category.MISC));
        WAYPOINT_KEY = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.kelpclient.waypoint", GLFW.GLFW_KEY_B, KeyBinding.Category.MISC));

        KelpHud.init();

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient()) KelpHud.onAttack(entity);
            return ActionResult.PASS;
        });

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (mc.currentScreen != null && mc.currentScreen.getClass() == TitleScreen.class && KelpConfig.get().kelpUi) {
                mc.setScreen(new KelpTitleScreen());
            }
            while (MENU_KEY.wasPressed()) mc.setScreen(new KelpMenuScreen(mc.currentScreen));
            while (WAYPOINT_KEY.wasPressed()) {
                if (mc.world != null && mc.player != null && mc.currentScreen == null)
                    mc.setScreen(new WaypointEditScreen(null, Waypoints.newHere(mc), true));
            }
            Cosmetics.tick(mc);
            Waypoints.tick(mc);
            KelpHud.tick(mc);
        });

        ScreenEvents.AFTER_INIT.register((mc, screen, w, h) -> {
            if (screen.getClass() == TitleScreen.class) {
                Screens.getButtons(screen).add(ButtonWidget.builder(Text.literal("Kelp UI"), b -> {
                    KelpConfig.get().kelpUi = true; KelpConfig.save();
                    mc.setScreen(new KelpTitleScreen());
                }).dimensions(4, 4, 60, 20).build());
            }
        });
    }
}
