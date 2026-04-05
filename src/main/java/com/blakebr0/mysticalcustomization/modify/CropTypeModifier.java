package com.blakebr0.mysticalcustomization.modify;

import com.blakebr0.mysticalagriculture.api.crop.CropType;
import com.blakebr0.mysticalcustomization.loader.CropTypeLoader;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.IdentifierException;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;

public final class CropTypeModifier {
    public static void modify(CropType type, JsonObject json) throws JsonSyntaxException, IdentifierException {
        if (json.has("craftingSeed")) {
            var itemId = GsonHelper.getAsString(json, "craftingSeed");
            CropTypeLoader.CRAFTING_SEED_MAP.put(type, Identifier.parse(itemId));
        }
    }
}
