package com.blakebr0.mysticalcustomization.create;

import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import com.blakebr0.mysticalagriculture.api.crop.Crop;
import com.blakebr0.mysticalagriculture.api.crop.CropTier;
import com.blakebr0.mysticalagriculture.api.crop.CropType;
import com.blakebr0.mysticalagriculture.api.crop.ICrop;
import com.blakebr0.mysticalagriculture.api.lib.LazyIngredient;
import com.blakebr0.mysticalcustomization.util.ParsingUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

public class CropCreator {
    public static ICrop create(ResourceLocation id, JsonObject json) throws JsonSyntaxException {
        String tierId = JSONUtils.getString(json, "tier");
        String typeId = JSONUtils.getString(json, "type");
        JsonObject ingredient = JSONUtils.getJsonObject(json, "ingredient");

        CropTier tier = MysticalAgricultureAPI.getCropTierById(new ResourceLocation(tierId));
        if (tier == null)
            throw new JsonSyntaxException("Invalid crop tier provided: " + tierId);

        CropType type = MysticalAgricultureAPI.getCropTypeByName(typeId);
        if (type == null)
            throw new JsonSyntaxException("Invalid crop type provided: " + typeId);

        LazyIngredient material;
        if (ingredient.has("tag")) {
            String tag = JSONUtils.getString(ingredient, "tag");
            material = LazyIngredient.tag(tag);
        } else if (ingredient.has("item")) {
            String item = JSONUtils.getString(ingredient, "item");
            if (ingredient.has("nbt")) {
                CompoundNBT nbt = ParsingUtils.parseNBT(ingredient);
                material = LazyIngredient.item(item, nbt);
            } else {
                material = LazyIngredient.item(item);
            }
        } else {
            throw new JsonSyntaxException("Ingredient must have either 'item' or 'tag' property");
        }

        Crop crop = new Crop(id, tier, type, material);

        if (json.has("color")) {
            String color = JSONUtils.getString(json, "color");
            int i = ParsingUtils.parseHex(color, "color");
            crop.setColor(i);
        } else if (json.has("colors")) {
            JsonObject colors = JSONUtils.getJsonObject(json, "colors");
            if (colors.has("flower")) {
                String color = JSONUtils.getString(colors, "flower");
                int i = ParsingUtils.parseHex(color, "flower");
                crop.setFlowerColor(i);
            }

            if (colors.has("essence")) {
                String color = JSONUtils.getString(colors, "essence");
                int i = ParsingUtils.parseHex(color, "essence");
                crop.setEssenceColor(i);
            }

            if (colors.has("seeds")) {
                String color = JSONUtils.getString(colors, "seeds");
                int i = ParsingUtils.parseHex(color, "seeds");
                crop.setSeedColor(i);
            }
        } else {
            throw new JsonSyntaxException("Missing 'color' or 'colors' property");
        }

        if (json.has("enabled")) {
            boolean enabled = JSONUtils.getBoolean(json, "enabled");
            crop.setEnabled(enabled);
        }

        if (json.has("crux")) {
            // TODO: crux
        }

        return crop;
    }
}
