package com.kelpclient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import org.lwjgl.glfw.GLFW;

public class KelpClient implements ClientModInitializer {
    public static KeyBinding MENU_KEY;

    @Override
    public void onInitializeClient() {
        KelpConfig.load();
        MENU_KEY = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.kelpclient.menu", GLFW.GLFW_KEY_RIGHT_SHIFT, KeyBinding.Category.MISC));

        KelpHud.init();

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient()) KelpHud.onAttack();
            return ActionResult.PASS;
        });

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            // Swap vanilla title screen for the Kelp one (exact class check so our own screen is never replaced)
            if (mc.currentScreen != null && mc.currentScreen.getClass() == TitleScreen.class && KelpConfig.get().kelpUi) {
                mc.setScreen(new KelpTitleScreen());
            }
            while (MENU_KEY.wasPressed()) mc.setScreen(new KelpModsScreen(mc.currentScreen));
            KelpHud.tick(mc);
        });

        // Button on the vanilla title screen to go back to the Kelp UI
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
