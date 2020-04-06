package com.blakebr0.mysticalcustomization.modify;

import com.blakebr0.mysticalagriculture.api.crop.CropTier;
import com.blakebr0.mysticalcustomization.loader.CropTierLoader;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

public class CropTierModifier {
    public static void modify(CropTier tier, JsonObject json) {
        if (json.has("fertilizable")) {
            boolean fertilizable = JSONUtils.getBoolean(json, "fertilizable");
            tier.setFertilizable(fertilizable);
        }

        if (json.has("secondarySeedDrop")) {
            boolean secondarySeedDrop = JSONUtils.getBoolean(json, "secondarySeedDrop");
            tier.setSecondarySeedDrop(secondarySeedDrop);
        }

        if (json.has("farmland")) {
            String blockId = JSONUtils.getString(json, "farmland");
            CropTierLoader.FARMLAND_MAP.put(tier, new ResourceLocation(blockId));
        }

        if (json.has("ingredient")) {
            JsonObject ingredient = JSONUtils.getJsonObject(json, "ingredient");
            if (ingredient.has("item")) {
                String itemId = JSONUtils.getString(ingredient, "item");
                CropTierLoader.ESSENCE_MAP.put(tier, new ResourceLocation(itemId));
            } else {
                throw new JsonSyntaxException("Ingredient must have an item property");
            }
        }
    }
}
