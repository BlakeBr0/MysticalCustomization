package com.blakebr0.mysticalcustomization.modify;

import com.blakebr0.mysticalagriculture.api.crop.CropType;
import com.blakebr0.mysticalcustomization.loader.CropTypeLoader;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

public final class CropTypeModifier {
    public static void modify(CropType type, JsonObject json) throws JsonSyntaxException {
        if (json.has("craftingSeed")) {
            String itemId = JSONUtils.getString(json, "craftingSeed");
            CropTypeLoader.CRAFTING_SEED_MAP.put(type, new ResourceLocation(itemId));
        }
    }
}
