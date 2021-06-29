package com.blakebr0.mysticalcustomization.create;

import com.blakebr0.mysticalagriculture.api.crop.CropTier;
import com.blakebr0.mysticalcustomization.loader.CropTierLoader;
import com.blakebr0.mysticalcustomization.util.ParsingUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public final class CropTierCreator {
    public static CropTier create(ResourceLocation id, JsonObject json) throws JsonSyntaxException {
        int value = JSONUtils.getAsInt(json, "value");
        String colorString = JSONUtils.getAsString(json, "color", "ffffff");
        int color = ParsingUtils.parseHex(colorString, "color");

        CropTier tier = new CropTier(id, value, color, TextFormatting.WHITE);

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

        return tier;
    }
}
