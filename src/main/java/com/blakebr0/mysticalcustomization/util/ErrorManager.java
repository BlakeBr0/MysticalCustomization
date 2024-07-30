package com.blakebr0.mysticalcustomization.util;

import com.blakebr0.mysticalcustomization.MysticalCustomization;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ErrorManager {
    public static final ErrorManager INSTANCE = new ErrorManager();

    private final Map<String, List<String>> errors = new HashMap<>();

    public void addError(String category, String message) {
        this.errors.computeIfAbsent(category, k -> new ArrayList<>()).add(message);
        MysticalCustomization.LOGGER.error("Error ({}): {}", category, message);
    }

    public void addFatalError(String category, String message, Exception exception) {
        this.errors.computeIfAbsent(category, k -> new ArrayList<>()).add(message + " Check the log for more information.");
        MysticalCustomization.LOGGER.error(message, exception);
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        var player = event.getEntity();

        if (!this.errors.isEmpty()) {
            player.sendSystemMessage(Component.literal("Mystical Customization errors have occurred:"));
        }

        for (var category : this.errors.entrySet()) {
            player.sendSystemMessage(Component.literal(" %s:".formatted(category.getKey())));

            for (var error : category.getValue()) {
                player.sendSystemMessage(Component.literal("  - %s".formatted(error)));
            }
        }
    }
}
