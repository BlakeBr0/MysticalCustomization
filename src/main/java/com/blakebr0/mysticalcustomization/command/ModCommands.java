package com.blakebr0.mysticalcustomization.command;

import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import com.blakebr0.mysticalagriculture.api.crop.CropTier;
import com.blakebr0.mysticalagriculture.api.crop.CropType;
import com.blakebr0.mysticalcustomization.MysticalCustomization;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.stream.Collectors;

public final class ModCommands {
    public static final LiteralArgumentBuilder<CommandSourceStack> ROOT = Commands.literal(MysticalCustomization.MOD_ID);

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        var dispatcher = event.getDispatcher();

        dispatcher.register(ROOT.then(Commands.literal("tiers").requires(source -> source.hasPermission(4)).executes(context -> {
            var tiers = MysticalAgricultureAPI.getCropRegistry().getTiers().stream()
                    .map(CropTier::getId)
                    .map(ResourceLocation::toString)
                    .collect(Collectors.joining("\n"));

            context.getSource().sendSuccess(new TextComponent(tiers), false);

            return 0;
        })));

        dispatcher.register(ROOT.then(Commands.literal("types").requires(source -> source.hasPermission(4)).executes(context -> {
            var types = MysticalAgricultureAPI.getCropRegistry().getTypes().stream()
                    .map(CropType::getId)
                    .map(ResourceLocation::toString)
                    .collect(Collectors.joining("\n"));

            context.getSource().sendSuccess(new TextComponent(types), false);

            return 0;
        })));
    }
}
