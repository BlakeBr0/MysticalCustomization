package com.blakebr0.mysticalcustomization.modify;

import com.blakebr0.mysticalagriculture.api.crop.CropTier;
import com.blakebr0.mysticalcustomization.loader.CropTierLoader;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.TextComponent;

public final class CropTierModifier {
    public static void modify(CropTier tier, JsonObject json) throws JsonSyntaxException {
        if (json.has("name")) {
            String name = GsonHelper.getAsString(json, "name");
            tier.setDisplayName(new TextComponent(name));
        }

        if (json.has("fertilizable")) {
            boolean fertilizable = GsonHelper.getAsBoolean(json, "fertilizable");
            tier.setFertilizable(fertilizable);
        }

        if (json.has("secondarySeedDrop")) {
            boolean secondarySeedDrop = GsonHelper.getAsBoolean(json, "secondarySeedDrop");
            tier.setSecondarySeedDrop(secondarySeedDrop);
        }

        if (json.has("farmland")) {
            String blockId = GsonHelper.getAsString(json, "farmland");
            CropTierLoader.FARMLAND_MAP.put(tier, new ResourceLocation(blockId));
        }

        if (json.has("essence")) {
            String itemId = GsonHelper.getAsString(json, "essence");
            CropTierLoader.ESSENCE_MAP.put(tier, new ResourceLocation(itemId));
        }
    }
}
