package com.blakebr0.mysticalcustomization.command;

import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import com.blakebr0.mysticalagriculture.api.crop.CropTier;
import com.blakebr0.mysticalagriculture.api.crop.CropType;
import com.blakebr0.mysticalcustomization.MysticalCustomization;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

import java.util.stream.Collectors;

public final class ModCommands {
    public static final LiteralArgumentBuilder<CommandSource> ROOT = Commands.literal(MysticalCustomization.MOD_ID);

    public static void onServerStarting(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(ROOT.then(Commands.literal("tiers").requires(source -> source.hasPermissionLevel(4)).executes(context -> {
            String tiers = MysticalAgricultureAPI.CROP_TIERS.stream()
                    .map(CropTier::getId)
                    .map(ResourceLocation::toString)
                    .collect(Collectors.joining("\n"));

            context.getSource().sendFeedback(new StringTextComponent(tiers), false);

            return 0;
        })));

        dispatcher.register(ROOT.then(Commands.literal("types").requires(source -> source.hasPermissionLevel(4)).executes(context -> {
            String types = MysticalAgricultureAPI.CROP_TYPES.stream()
                    .map(CropType::getName)
                    .collect(Collectors.joining("\n"));

            context.getSource().sendFeedback(new StringTextComponent(types), false);

            return 0;
        })));
    }
}
