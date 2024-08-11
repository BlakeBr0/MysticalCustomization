package com.blakebr0.mysticalcustomization;

import com.blakebr0.mysticalcustomization.command.ModCommands;
import com.blakebr0.mysticalcustomization.loader.CropLoader;
import com.blakebr0.mysticalcustomization.loader.CropTierLoader;
import com.blakebr0.mysticalcustomization.loader.CropTypeLoader;
import com.blakebr0.mysticalcustomization.util.ErrorManager;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(MysticalCustomization.MOD_ID)
public final class MysticalCustomization {
    public static final String MOD_ID = "mysticalcustomization";
    public static final String NAME = "Mystical Customization";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

    public MysticalCustomization(IEventBus bus) {
        bus.register(this);
    }

    @SubscribeEvent
    public void onCommonSetup(FMLCommonSetupEvent event) {
        NeoForge.EVENT_BUS.register(new ModCommands());
        NeoForge.EVENT_BUS.register(ErrorManager.INSTANCE);

        event.enqueueWork(() -> {
            CropTierLoader.onCommonSetup();
            CropTypeLoader.onCommonSetup();
            CropLoader.onCommonSetup();
        });
    }

    public static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
