package com.blakebr0.mysticalcustomization.create;

import com.blakebr0.mysticalagriculture.api.crop.CropTier;
import com.blakebr0.mysticalcustomization.loader.CropTierLoader;
import com.blakebr0.mysticalcustomization.util.ParsingUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public final class CropTierCreator {
    public static CropTier create(ResourceLocation id, JsonObject json) throws JsonSyntaxException {
        var value = GsonHelper.getAsInt(json, "value");
        var colorString = GsonHelper.getAsString(json, "color", "ffffff");
        var color = ParsingUtils.parseHex(colorString, "color");

        var tier = new CropTier(id, value, color, ChatFormatting.WHITE);

        if (json.has("name")) {
            var name = GsonHelper.getAsString(json, "name");
            tier.setDisplayName(Component.literal(name));
        }

        if (json.has("fertilizable")) {
            var fertilizable = GsonHelper.getAsBoolean(json, "fertilizable");
            tier.setFertilizable(fertilizable);
        }

        if (json.has("secondarySeedDrop")) {
            var secondarySeedDrop = GsonHelper.getAsBoolean(json, "secondarySeedDrop");
            tier.setSecondarySeedDrop(secondarySeedDrop);
        }

        if (json.has("baseSecondaryChance")) {
            var chance = GsonHelper.getAsDouble(json, "baseSecondaryChance");
            tier.setBaseSecondaryChance(chance);
        }

        if (json.has("farmland")) {
            var blockId = GsonHelper.getAsString(json, "farmland");
            CropTierLoader.FARMLAND_MAP.put(tier, new ResourceLocation(blockId));
        }

        if (json.has("essence")) {
            var itemId = GsonHelper.getAsString(json, "essence");
            CropTierLoader.ESSENCE_MAP.put(tier, new ResourceLocation(itemId));
        }

        return tier;
    }
}
