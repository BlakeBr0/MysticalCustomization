package com.blakebr0.mysticalcustomization.create;

import com.blakebr0.mysticalagriculture.api.crop.CropType;
import com.blakebr0.mysticalcustomization.loader.CropTypeLoader;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

public class CropTypeCreator {
    public static CropType create(String name, JsonObject json) throws JsonSyntaxException {
        JsonObject textures = JSONUtils.getJsonObject(json, "textures");
        String stem = JSONUtils.getString(textures, "stem");

        CropType type = new CropType(name, new ResourceLocation(stem));

        if (json.has("craftingSeed")) {
            String itemId = JSONUtils.getString(json, "craftingSeed");
            CropTypeLoader.CRAFTING_SEED_MAP.put(type, new ResourceLocation(itemId));
        }

        return type;
    }
}
