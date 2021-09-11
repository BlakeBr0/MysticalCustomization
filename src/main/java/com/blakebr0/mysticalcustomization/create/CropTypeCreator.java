package com.blakebr0.mysticalcustomization.create;

import com.blakebr0.mysticalagriculture.api.crop.CropType;
import com.blakebr0.mysticalcustomization.loader.CropTypeLoader;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public final class CropTypeCreator {
    public static CropType create(String name, JsonObject json) throws JsonSyntaxException {
        var textures = GsonHelper.getAsJsonObject(json, "textures");
        var stem = GsonHelper.getAsString(textures, "stem");

        var type = new CropType(name, new ResourceLocation(stem));

        if (json.has("craftingSeed")) {
            var itemId = GsonHelper.getAsString(json, "craftingSeed");
            CropTypeLoader.CRAFTING_SEED_MAP.put(type, new ResourceLocation(itemId));
        }

        return type;
    }
}
