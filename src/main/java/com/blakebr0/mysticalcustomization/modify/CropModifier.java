package com.blakebr0.mysticalcustomization.modify;

import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import com.blakebr0.mysticalagriculture.api.crop.CropTier;
import com.blakebr0.mysticalagriculture.api.crop.CropType;
import com.blakebr0.mysticalagriculture.api.crop.ICrop;
import com.blakebr0.mysticalagriculture.api.lib.LazyIngredient;
import com.blakebr0.mysticalcustomization.loader.CropLoader;
import com.blakebr0.mysticalcustomization.util.ParsingUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

public class CropModifier {
    public static void modify(ICrop crop, JsonObject json) {
        if (json.has("name")) {
            String name = JSONUtils.getString(json, "name");
            crop.setDisplayName(new StringTextComponent(name));
        }

        if (json.has("tier")) {
            String tierId = JSONUtils.getString(json, "tier");
            CropTier tier = MysticalAgricultureAPI.getCropTierById(new ResourceLocation(tierId));
            if (tier == null)
                throw new JsonSyntaxException("Invalid crop tier provided: " + tierId);

            crop.setTier(tier);
        }

        if (json.has("type")) {
            String typeId = JSONUtils.getString(json, "type");
            CropType type = MysticalAgricultureAPI.getCropTypeByName(typeId);
            if (type == null)
                throw new JsonSyntaxException("Invalid crop type provided: " + typeId);

            crop.setType(type);
        }

        if (json.has("ingredient")) {
            JsonObject ingredient = JSONUtils.getJsonObject(json, "ingredient");
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

            crop.setCraftingMaterial(material);
        }

        if (json.has("enabled")) {
            boolean enabled = JSONUtils.getBoolean(json, "enabled");
            crop.setEnabled(enabled);
        }

        if (json.has("crux")) {
            String crux = JSONUtils.getString(json, "crux");
            CropLoader.CRUX_MAP.put(crop, new ResourceLocation(crux));
        }
    }
}
