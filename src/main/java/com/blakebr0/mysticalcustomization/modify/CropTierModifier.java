package com.blakebr0.mysticalcustomization.modify;

import com.blakebr0.mysticalagriculture.api.crop.CropTier;
import com.blakebr0.mysticalcustomization.loader.CropTierLoader;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

public final class CropTierModifier {
    public static void modify(CropTier tier, JsonObject json) throws JsonSyntaxException {
        if (json.has("name")) {
            String name = JSONUtils.getAsString(json, "name");
            tier.setDisplayName(new StringTextComponent(name));
        }

        if (json.has("fertilizable")) {
            boolean fertilizable = JSONUtils.getAsBoolean(json, "fertilizable");
            tier.setFertilizable(fertilizable);
        }

        if (json.has("secondarySeedDrop")) {
            boolean secondarySeedDrop = JSONUtils.getAsBoolean(json, "secondarySeedDrop");
            tier.setSecondarySeedDrop(secondarySeedDrop);
        }

        if (json.has("farmland")) {
            String blockId = JSONUtils.getAsString(json, "farmland");
            CropTierLoader.FARMLAND_MAP.put(tier, new ResourceLocation(blockId));
        }

        if (json.has("essence")) {
            String itemId = JSONUtils.getAsString(json, "essence");
            CropTierLoader.ESSENCE_MAP.put(tier, new ResourceLocation(itemId));
        }
    }
}
