package com.blakebr0.mysticalcustomization.modify;

import com.blakebr0.mysticalagriculture.api.crop.CropType;
import com.blakebr0.mysticalcustomization.loader.CropTypeLoader;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

public class CropTypeModifier {
    public static void modify(CropType type, JsonObject json) throws JsonSyntaxException {
        if (json.has("craftingSeed")) {
            JsonObject craftingSeed = JSONUtils.getJsonObject(json, "craftingSeed");
            if (craftingSeed.has("item")) {
                String itemId = JSONUtils.getString(craftingSeed, "item");
                CropTypeLoader.CRAFTING_SEED_MAP.put(type, new ResourceLocation(itemId));
            } else {
                throw new JsonSyntaxException("Ingredient (craftingSeed) must have an item property");
            }
        }
    }
}
